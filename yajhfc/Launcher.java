package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/*
 * Launcher.java:
 * This is a (somewhat half-baked) implementation to detect already running instances
 * and to pass the command line parameters/data (in the case of --stdin) to the already
 * running instance.
 * I'm using a combination of a lock file and a ServerPort here to ensure that 
 * every user on a multi-user machine can run his own instance (I want to restrict it to 
 * one instance *per user* and _not_ one instance per machine).
 * The current implementation is far from perfect, so if someone with more java
 * experience knows a better way to accomplish this functionality, please let me know.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Launcher {

    // Configuration directory as set on the command line
    // Needs to be set *before* utils initializes in order to have an effect!
    public static String cmdLineConfDir = null;
    
    private static ServerSocket sockBlock = null;
    private static SockBlockAcceptor blockThread;
    private static mainwin application;
    static boolean isLocking = false;
    
    final static int codeSubmitStream = 1;
    final static int codeSubmitFile = 2;
    final static int codeMultiSubmitFile = 4;
    final static int codeToForeground = 3;
    
    final static int responseOK = 0;
    final static int responseNotConnected = 10;
    final static int responseGeneralError = 1;
    final static int responseUnknownOpCode = 255;
    
    final static String multiFileEOF = "\003\004"; // ETX + EOT
    
    private static InetAddress getLocalhost() 
        throws UnknownHostException {
            final byte[] addr = {127, 0, 0, 1};
            return InetAddress.getByAddress(addr);
    }
    
    private static File getLockFile() {
        return new File(utils.getConfigDir() + "lock");
    }
    
    private static Socket checkLock() {
        File lock = getLockFile();
        if (lock.exists()) {
            try {
                BufferedReader filin = new BufferedReader(new FileReader(lock));
                String strport = filin.readLine();
                filin.close();
                
                int port = Integer.parseInt(strport);
                Socket cli = new Socket(getLocalhost(), port);
                return cli;
            } catch (Exception e) {
               // do nothing
            }
        } 

        return null;
    }
    
    private static void createLock() {
        final int portStart = 64007;
        final int portEnd = 65269;
        int port;
        File lock = getLockFile();
        
        try {
            for (port = portStart; port <= portEnd; port++) {
                try {
                    sockBlock = new ServerSocket(port, 50, getLocalhost());
                    break;
                } catch (Exception e) {
                    // do nothing, try next port
                }
            }
            if (sockBlock != null) {
                FileWriter filout = new FileWriter(lock);
                filout.write(String.valueOf(port) + "\n");
                filout.close();
                lock.deleteOnExit();
                isLocking = true;
            }
        } catch (IOException e) {
            System.err.println("Could not create lock: " + e.getMessage());
        }
    }
    
    public static void releaseLock() {
        try {
            isLocking = false;
            if (sockBlock != null) {
                sockBlock.close();
                sockBlock = null;
            }
        } catch (IOException e) {
            // do nothing
        }
    }
    
    
    private static void printHelp() {
        System.out.print(
            "General usage:\n"+
            "java -jar yajhfc.jar [--help] [--debug] [--admin] [--background|--noclose] [--configdir=directory] [--stdin | filename ...]\n"+
            "Argument description:\n"+
            "filename     The file name of a PostScript file to send.\n"+
            "--stdin      Read the file to send from standard input\n"+
            "--admin      Start up in admin mode\n"+
            "--debug      Output some debugging information\n"+
            "--background If there is no already running instance, launch a new instance \n" +
            "             and terminate (after submitting the file to send)\n" +
            "--noclose    Do not close YajHFC after submitting the fax\n"+
            "--configdir  Sets a configuration directory to use instead of ~/.yajhfc\n" +
            "--help       Displays this text\n"
            );   
    }
    
    
    /**
     * Launches this application
     */
    public static void main(String[] args) {
        // parse command line
        ArrayList<String> fileNames = new ArrayList<String>();
        boolean useStdin = false;
        boolean adminMode = false;
        boolean forkNewInst = false;
        boolean closeAfterSubmit = true;
        boolean debugMode = false;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) { // command line argument
                if (args[i].equals("--help")) {
                    printHelp();
                    System.exit(0);
                } else if (args[i].equals("--stdin"))
                    useStdin = true;
                else if (args[i].equals("--admin"))
                    adminMode = true;
                else if (args[i].equals("--debug"))
                    debugMode = true;
                else if (args[i].equals("--background"))
                    forkNewInst = true;
                else if (args[i].equals("--noclose")) 
                    closeAfterSubmit = false;
                else if (args[i].startsWith("--configdir="))
                    cmdLineConfDir = args[i].substring(12); // 12 == "--configdir=".length()
                else
                    System.err.println("Unknown command line argument: " + args[i]);
            } else if (args[i].startsWith("-"))
                System.err.println("Unknown command line argument: " + args[i]);
            else // treat argument as file name to send
                fileNames.add(args[i]);
        }
        
        utils.debugMode = debugMode;
        
        Socket oldinst = checkLock();
        
        if (forkNewInst && (oldinst == null)) {
            try {
                int argcount = 4;
                if (adminMode)
                    argcount++;
                if (utils.debugMode)
                    argcount++;
                if (!closeAfterSubmit)
                    argcount++;
                if (cmdLineConfDir != null)
                    argcount++;
                
                String[] launchArgs = new String[argcount];
                launchArgs[0] = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";
                launchArgs[1] = "-classpath";
                launchArgs[2] = System.getProperty("java.class.path");
                launchArgs[3] = Launcher.class.getCanonicalName();
                
                int argidx = 4;
                if (adminMode) {
                    launchArgs[argidx] = "--admin";
                    argidx++;
                }
                if (utils.debugMode) {
                    launchArgs[argidx] = "--debug";
                    argidx++;
                }
                if (!closeAfterSubmit) {
                    launchArgs[argidx] = "--noclose";
                    argidx++;
                }
                if (cmdLineConfDir != null) {
                    launchArgs[argidx] = "--configdir=" + cmdLineConfDir;
                    argidx++;
                }
                
                if (utils.debugMode) {
                    System.out.println("Launching new instance:");
                    for (int i = 0; i < launchArgs.length; i++) {
                        System.out.println("launchArgs[" + i + "] = " + launchArgs[i]);
                    }
                }
                Runtime.getRuntime().exec(launchArgs);
                
                int time = 0;
                if (utils.debugMode) {
                    System.out.print("Waiting for new instance... ");
                }
                
                do {
                    Thread.sleep(200);
                    time += 200;
                    if (time > 20000) {
                        throw new TimeoutException(utils._("The new instance did not start after 20 seconds."));
                    }
                    
                    oldinst = checkLock();
                } while (oldinst == null);
                
                if (utils.debugMode) {
                    System.out.println("New instance has been started.");
                }
            } catch (Exception e) {
                if (utils.debugMode) {
                    System.out.println("Exception launching new instance:");
                    e.printStackTrace(System.out);
                }
                JOptionPane.showMessageDialog(null, utils._("Cannot launch new program instance, continuing with the existing one!\nReason: ") + e.toString() );
            }
        }
        
        if (oldinst == null) {
            createLock();
            SwingUtilities.invokeLater(new NewInstRunner(fileNames, useStdin, adminMode, closeAfterSubmit));
            blockThread = new SockBlockAcceptor();
            blockThread.start();
        } else {            
            try {
                OutputStream outStream = oldinst.getOutputStream();
                InputStream inStream = oldinst.getInputStream();
                
                if (useStdin) { 
                    BufferedOutputStream bufOut = new BufferedOutputStream(outStream);
                    BufferedInputStream bufIn = new BufferedInputStream(System.in);
                    bufOut.write(codeSubmitStream);
                    byte[] buf = new byte[16000];
                    int bytesRead = 0;
                    do {
                        bytesRead = bufIn.read(buf);
                        if (bytesRead > 0)
                            bufOut.write(buf, 0, bytesRead);
                    } while (bytesRead >= 0);
                    bufIn.close();
                    bufOut.flush();              
                } else if ( fileNames != null && fileNames.size() > 0) {
                    outStream.write(codeMultiSubmitFile);
                    BufferedWriter bufOut = new BufferedWriter(new OutputStreamWriter(outStream));
                    for (String fileName : fileNames) {
                        File f = new File(fileName);
                        bufOut.write(f.getAbsolutePath() + "\n");
                    }
                    bufOut.write(multiFileEOF + "\n");
                    bufOut.flush();
                } else if (forkNewInst) {
                    outStream.write(codeToForeground);
                } else {
                    outStream.write(codeToForeground);
                    
                    System.err.println(utils._("There already is a running instance!"));
                }
                outStream.flush();
                oldinst.shutdownOutput();
                
                int response = inStream.read();
                
                if (!oldinst.isClosed()) {
                    oldinst.close();
                }
                
                if (response >= 0)
                    System.exit(response);
                else
                    System.exit(responseGeneralError);
            } catch (IOException e) {
                System.err.println("An error occured communicating with the old instance: ");
                e.printStackTrace();
                System.exit(responseGeneralError);
            }
        }
    }
        
    static class NewInstRunner implements Runnable {
        private List<String> fileNames;
        private boolean useStdin;
        private boolean adminMode;
        private boolean closeAfterSubmit;
        
        public void run() {
            utils.setLookAndFeel(utils.getFaxOptions().lookAndFeel);
            
            application = new mainwin(adminMode);
            application.setVisible(true);
            application.reconnectToServer();
            
            if ((fileNames != null && fileNames.size() > 0) || useStdin) {
                SendWin sw = new SendWin(application.hyfc, application);
                sw.setModal(true);
                if (useStdin)
                    sw.addInputStream(System.in);
                else {
                    for (String fileName : fileNames)
                        sw.addLocalFile(fileName);
                }
                sw.setVisible(true);
                if (closeAfterSubmit)
                    application.dispose();
            }
        }   
        
        public NewInstRunner(List<String> fileNames, boolean useStdin, boolean adminMode, boolean closeAfterSubmit) {
            super();
            this.fileNames = fileNames;
            this.useStdin = useStdin;
            this.adminMode = adminMode;
            this.closeAfterSubmit = closeAfterSubmit;
        }
    }
    
    static class SockBlockAcceptor extends Thread {
        private int waitSubmitOK() throws InterruptedException  {
            while (true) {
                if (application != null) {
                    switch (application.getSendReadyState()) {
                    case Ready:
                        return responseOK;
                    case NotReady:
                        return responseNotConnected;
                    case NeedToWait:
                        //NOP
                    }
                }
                Thread.sleep(100);
            }
        }
        
        @Override
        public void run() {
            while (isLocking) {
                Socket srv = null;
                InputStream strIn = null;
                OutputStream strOut = null;
                try {
                    srv = sockBlock.accept();
                    strIn = srv.getInputStream();
                    strOut = srv.getOutputStream();

                    switch (strIn.read()) {
                    case codeSubmitStream:
                        int ok = waitSubmitOK();
                        if (ok == responseOK) {
                            SwingUtilities.invokeAndWait(new SubmitRunner(strIn)); // Accept new faxes only sequentially
                        }
                        strOut.write(ok);
                        break;
                    case codeSubmitFile:
                        ok = waitSubmitOK();
                        if (ok == responseOK) {
                            BufferedReader bufR = new BufferedReader(new InputStreamReader(strIn));
                            String[] fileNames = new String[1];
                            fileNames[0] = bufR.readLine();
                            
                            SwingUtilities.invokeAndWait(new SubmitRunner(Arrays.asList(fileNames))); // Accept new faxes only sequentially
                            //bufR.close();
                        }
                        strOut.write(ok);
                        break;
                    case codeMultiSubmitFile:
                        ok = waitSubmitOK();
                        if (ok == responseOK) {
                            BufferedReader bufR = new BufferedReader(new InputStreamReader(strIn));
                            ArrayList<String> fileNames = new ArrayList<String>();
                            String line = bufR.readLine();
                            
                            while (!line.equals(multiFileEOF)) {
                                fileNames.add(line);
                                line = bufR.readLine();
                            }
                            
                            SwingUtilities.invokeAndWait(new SubmitRunner(fileNames)); // Accept new faxes only sequentially
                            //bufR.close();
                        }
                        strOut.write(ok);
                        break;
                    case codeToForeground:
                    case -1: // Connection closed without sending any data
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                application.toFront();
                            };
                        });
                        if (!srv.isClosed()) {
                            strOut.write(responseOK);
                        }
                        break;
                    default:
                        System.err.println("Unknown code received.");
                        strOut.write(responseUnknownOpCode);
                    }
                    
                    strOut.flush();
                } catch (Exception e) {
                    //System.err.println("Error listening for connections: "  + e.getMessage());
                } finally {
                    try {
                        if (srv != null && !srv.isClosed()) {                        
                            srv.close();
                        }
                        srv = null;
                        strIn = null;
                        strOut = null;
                    } catch(Exception e) {
                        // NOP
                    }
                }
            }
        }
        
        public SockBlockAcceptor() {
            super(SockBlockAcceptor.class.getName());
        }
    }
    
    static class SubmitRunner implements Runnable {
        InputStream strIn = null;
        List<String> fileNames = null;
        
        public void run() {
            //try {
                application.toFront();
                
                SendWin sw = new SendWin(application.hyfc, application);
                sw.setModal(true);
                if (strIn != null)
                    sw.addInputStream(strIn);
                else {
                    for (String fileName : fileNames)
                        sw.addLocalFile(fileName);
                }
                
                sw.setVisible(true);
                /*if (strIn != null)
                    strIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
        
        public SubmitRunner(List<String> fileNames) {
            super();
            this.fileNames = fileNames;
        }
        
        public SubmitRunner(InputStream strIn) {
            super();
            this.strIn = strIn;
        }
    }
}
