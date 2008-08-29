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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import yajhfc.PluginManager.PluginInfo;
import yajhfc.PluginManager.PluginType;
import yajhfc.send.SendController;
import yajhfc.send.SendWinControl;

public final class Launcher {
    private static final Logger launchLog = Logger.getLogger(Launcher.class.getName());
    
    // Configuration directory as set on the command line
    // Needs to be set *before* utils initializes in order to have an effect!
    public static String cmdLineConfDir = null;
    
    static ServerSocket sockBlock = null;
    static SockBlockAcceptor blockThread;
    public static mainwin application;
    static boolean isLocking = false;
    
    final static int codeSubmitStream = 1;
    final static int codeSubmitFile = 2;
    final static int codeMultiSubmitFile = 4;
    final static int codeToForeground = 3;
    final static int codeAddRecipients = 5;
    final static int codeQuit = 255;
    
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
            launchLog.log(Level.WARNING, "Could not create lock: ", e);
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
            "java -jar yajhfc.jar [--help] [--debug] [--admin] [--background|--noclose] \n" +
            "         [--configdir=directory] [--loadplugin=filename] [--logfile=filename]\n" +
            "         [--showtab=0|R|1|S|2|T] [--recipient=...] [--stdin | filename ...]\n"+
            "Argument description:\n"+
            "filename     One or more file names of PostScript files to send.\n"+
            "--stdin      Read the file to send from standard input.\n"+
            "--recipient  Specifies the phone number of a recipient to send the fax to.\n"+
            "             You may specify multiple arguments for multiple recipients.\n"+
            "--admin      Start up in admin mode.\n"+
            "--debug      Output some debugging information.\n"+
            "--logfile    The logfile to log debug information to (if not specified, use stdout).\n"+
            "--background If there is no already running instance, launch a new instance \n" +
            "             and terminate (after submitting the file to send).\n" +
            "--noclose    Do not close YajHFC after submitting the fax.\n"+
            "--showtab    Sets the tab to display on startup. Specify 0 or R for the \"Received\", \n"+
            "             1 or S for the \"Sent\" or 2 or T for the \"Transmitting\" tab.\n"+
            "--loadplugin Specifies the jar file of a YajHFC plugin to load.\n" +
            "--loaddriver Specifies the location of a JDBC driver JAR file.\n" +
            "--no-plugins Disables loading plugins from the plugin.lst file.\n" +
            "--configdir  Sets a configuration directory to use instead of ~/.yajhfc\n" +
            "--help       Displays this text.\n"
            );   
    }
    
    /**
     * Strips quotes (" or ') at the beginning and end of the specified String
     * @param str
     * @return
     */
    public static String stripQuotes(String str)
    {
        char c = str.charAt(0);
        if (c == '\"' || c == '\'') {
            c = str.charAt(str.length()-1);
            if (c == '\"' || c == '\'') {
                return str.substring(1, str.length()-2);
            }
        }
        return str;
    }
    
   
    
    /**
     * Launches this application
     */
    public static void main(String[] args) {
        // parse command line
        ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<String> recipients = new ArrayList<String>();
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        boolean useStdin = false;
        boolean adminMode = false;
        boolean forkNewInst = false;
        boolean closeAfterSubmit = true;
        boolean debugMode = false;
        boolean noPlugins = false;
        int selectedTab = -1;
        String logFile = null;
        boolean appendToLog = false;
        
        for (int i = 0; i < args.length; i++) {
            String curArg = args[i];
            if (curArg.startsWith("--")) { // command line argument
                if (curArg.equals("--help")) {
                    printHelp();
                    System.exit(0);
                } else if (curArg.equals("--stdin"))
                    useStdin = true;
                else if (curArg.equals("--admin"))
                    adminMode = true;
                else if (curArg.equals("--debug"))
                    debugMode = true;
                else if (curArg.equals("--background"))
                    forkNewInst = true;
                else if (curArg.equals("--noclose")) 
                    closeAfterSubmit = false;
                else if (curArg.startsWith("--configdir="))
                    cmdLineConfDir = stripQuotes(curArg.substring(12)); // 12 == "--configdir=".length()
                else if (curArg.startsWith("--recipient="))
                    recipients.add(stripQuotes(curArg.substring(12))); // 12 == "--recipient=".length()
                else if (curArg.startsWith("--showtab=") && curArg.length() > 10) { // 10 == ""--showtab="".length()
                    switch (curArg.charAt(10)) {
                    case '0':
                    case 'R':
                    case 'r':
                        selectedTab = 0;
                        break;
                    case '1':
                    case 'S':
                    case 's':
                        selectedTab = 1;
                        break;
                    case '2':
                    case 'T':
                    case 't':
                        selectedTab = 2;
                        break;
                    default:
                        System.err.println("Unknown tab: " + curArg.substring(10));
                    }
                } else if (curArg.startsWith("--loadplugin=")) {
                    plugins.add(new PluginInfo(new File(stripQuotes(curArg.substring("--loadplugin=".length()))),
                            PluginType.PLUGIN, false));
                } else if (curArg.startsWith("--loaddriver=")) {
                    plugins.add(new PluginInfo(new File(stripQuotes(curArg.substring("--loaddriver=".length()))), 
                            PluginType.JDBCDRIVER, false));
                } else if (curArg.startsWith("--logfile=")) {
                    logFile = stripQuotes(curArg.substring("--logfile=".length()));
                    appendToLog = false;
                } else if (curArg.startsWith("--appendlogfile=")) {
                    logFile = stripQuotes(curArg.substring("--appendlogfile=".length()));
                    appendToLog = true;
                } else if (curArg.equals("--no-plugins")) {
                    noPlugins = true;
                } else {
                    System.err.println("Unknown command line argument: " + curArg);
                }
            } else if (curArg.startsWith("-"))
                System.err.println("Unknown command line argument: " + curArg);
            else // treat argument as file name to send
                fileNames.add(curArg);
        }
        
        if (debugMode && ":prompt:".equals(logFile)) {
            logFile = (new LogFilePrompter()).promptForLogfile();
            if (logFile == null) {
                debugMode = false;
            }
        }
        
        // IMPORTANT: Don't access utils before this line!
        utils.debugMode = debugMode;
        
        if (debugMode) {
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler theHandler;
            if (logFile != null) {
//              try {
//              utils.debugOut = new PrintStream(new FileOutputStream(logFile));
//              } catch (FileNotFoundException e) {
//              utils.debugOut = System.out;
//              }
                try {
                    theHandler = new StreamHandler(new FileOutputStream(logFile, appendToLog), new SimpleFormatter());
                } catch (FileNotFoundException e) {
                    launchLog.log(Level.WARNING, "Could not open log file.", e);
                    theHandler = new StreamHandler(System.out, new SimpleFormatter());
                }
            } else {
                theHandler = new StreamHandler(System.out, new SimpleFormatter());
            }
            theHandler.setLevel(Level.FINEST);
            rootLogger.setLevel(Level.FINEST);
            rootLogger.addHandler(theHandler);
            
            Logger.getLogger("sun").setLevel(Level.INFO);
            Logger.getLogger("java.awt").setLevel(Level.INFO);
            Logger.getLogger("javax.swing").setLevel(Level.INFO);

            //ConsoleLogger.setAllOutputStreams(utils.debugOut);

            launchLog.config("YajHFC version: " + utils.AppVersion);
            launchLog.config("---- BEGIN System.getProperties() dump");
            utils.dumpProperties(System.getProperties(), launchLog);
            launchLog.config("---- END System.getProperties() dump");
            launchLog.config("" + args.length + " command line arguments:");
            for (String arg : args) {
                launchLog.config(arg);
            }
        }
        
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
                if (logFile != null) 
                    argcount++;
                if (cmdLineConfDir != null)
                    argcount++;
                if (selectedTab >= 0)
                    argcount++;
                if (noPlugins)
                    argcount++;
                argcount += plugins.size();
                
                String[] launchArgs = new String[argcount];
                launchArgs[0] = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";
                launchArgs[1] = "-classpath";
                launchArgs[2] = System.getProperty("java.class.path");
                launchArgs[3] = Launcher.class.getCanonicalName();
                
                //When adding something here, also modify argcount above!
                int argidx = 4;
                if (adminMode) {
                    launchArgs[argidx] = "--admin";
                    argidx++;
                }
                if (utils.debugMode) {
                    launchArgs[argidx] = "--debug";
                    argidx++;
                }
                if (logFile != null) {
                    launchArgs[argidx] = "--appendlogfile=\"" + logFile + "\"";
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
                if (selectedTab >= 0) {
                    launchArgs[argidx] = "--showtab=" + selectedTab;
                    argidx++;
                }
                if (noPlugins) {
                    launchArgs[argidx] = "--no-plugins";
                    argidx++;
                }
                for (PluginInfo entry : plugins) {
                    switch (entry.type) {
                    case PLUGIN:
                        launchArgs[argidx++] = "--loadplugin=\"" + entry.file.getAbsolutePath() + "\"";
                        break;
                    case JDBCDRIVER:
                        launchArgs[argidx++] = "--loaddriver=\"" + entry.file.getAbsolutePath() + "\"";
                        break;
                    }
                }
                
                if (utils.debugMode) {
                    launchLog.info("Launching new instance:");
                    for (int i = 0; i < launchArgs.length; i++) {
                        launchLog.info("launchArgs[" + i + "] = " + launchArgs[i]);
                    }
                }
                Runtime.getRuntime().exec(launchArgs);
                
                int time = 0;
                if (utils.debugMode) {
                    launchLog.info("Waiting for new instance... ");
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
                    launchLog.info("New instance has been started.");
                }
            } catch (Exception e) {
                if (utils.debugMode) {
                    launchLog.log(Level.WARNING, "Exception launching new instance:", e);
                    //e.printStackTrace(utils.debugOut);
                }
                JOptionPane.showMessageDialog(null, utils._("Cannot launch new program instance, continuing with the existing one!\nReason: ") + e.toString() );
            }
        }
        
        if (oldinst == null) {
            if (utils.debugMode) {
                launchLog.info("No old instance found, creating lock...");
            }
            createLock();
            
            // Load plugins:
            if (!noPlugins) {
                PluginManager.readPluginList();
            }
            PluginManager.addPlugins(plugins);
            PluginManager.loadAllKnownPlugins();
            
            SwingUtilities.invokeLater(new NewInstRunner(fileNames, useStdin, recipients, adminMode, closeAfterSubmit, selectedTab));
            blockThread = new SockBlockAcceptor();
            blockThread.start();
            if (utils.debugMode) {
                launchLog.info("Lock and listener created.");
            }
        } else {            
            try {
                if (utils.debugMode) {
                    launchLog.info("Found old instance at: " + oldinst);
                }
                OutputStream outStream = oldinst.getOutputStream();
                InputStream inStream = oldinst.getInputStream();
                
                if (recipients.size() > 0)
                {
                    outStream.write(codeAddRecipients);
                    BufferedWriter bufOut = new BufferedWriter(new OutputStreamWriter(outStream));
                    for (String number : recipients) {
                        bufOut.write(number + "\n");
                    }
                    bufOut.write(multiFileEOF + "\n");
                    bufOut.flush();
                    
                    int response = inStream.read();
                    if (response != 0) {
                        if (response > 0) {
                            System.exit(response);
                        } else {
                            System.exit(responseGeneralError);
                        }
                    }
                }
                
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
                    
                    launchLog.log(Level.WARNING, utils._("There already is a running instance!"));
                }
                //outStream.write(codeQuit);
                outStream.flush();
                oldinst.shutdownOutput();
                
                int response = inStream.read();
                
                if (!oldinst.isClosed()) {
                    oldinst.close();
                }
                
                if (utils.debugMode) {
                    launchLog.info("Submitted information to old inst, terminating with code " + response);
                }
                
                if (response >= 0)
                    System.exit(response);
                else
                    System.exit(responseGeneralError);
            } catch (IOException e) {
                launchLog.log(Level.WARNING, "An error occured communicating with the old instance: ", e);
                System.exit(responseGeneralError);
            }
        }
    }
    
    private static class LogFilePrompter implements Runnable {
        protected String selection = null;
        
        public void run() {
            JFileChooser chooser = new JFileChooser();
            FileFilter logFiles = new ExampleFileFilter("log", "Log files");
            chooser.addChoosableFileFilter(logFiles);
            chooser.setFileFilter(logFiles);
            chooser.setDialogTitle("Select log file location");
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                selection = chooser.getSelectedFile().getPath();
                if (chooser.getFileFilter() == logFiles && selection.indexOf('.') == -1) {
                    selection = selection + ".log";
                }
            } else {
                selection = null;
            }
        }
        
        public String promptForLogfile() {
            try {
                SwingUtilities.invokeAndWait(this);
                return selection;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    
    static class NewInstRunner implements Runnable{
        protected boolean adminMode;
        protected boolean closeAfterSubmit;
        protected int selectedTab;
        protected InputStream inStream;
        protected List<String> fileNames;
        protected List<String> recipients;
        
        public void run() {
            utils.setLookAndFeel(utils.getFaxOptions().lookAndFeel);
            
            application = new mainwin(adminMode);
            application.setVisible(true);
            Runnable loginAction = null;
            if ((fileNames != null && fileNames.size() > 0) || this.inStream != null) {
                loginAction = new SubmitRunner(fileNames, inStream, recipients, closeAfterSubmit);
            }
            
            application.reconnectToServer(loginAction);
            if (selectedTab >= 0) {
                application.setSelectedTab(selectedTab);
            }
        }   
        
        public NewInstRunner(List<String> fileNames, boolean useStdin, List<String> recipients, boolean adminMode, boolean closeAfterSubmit, int selectedTab) {
            //super(fileNames, useStdin ? System.in : null, recipients);
            this.fileNames = fileNames;
            this.inStream = useStdin ? System.in : null;
            this.recipients = recipients;
            
            this.adminMode = adminMode;
            this.closeAfterSubmit = closeAfterSubmit;
            this.selectedTab = selectedTab;
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
            Logger log = Logger.getLogger(SockBlockAcceptor.class.getName());
            while (isLocking) {
                Socket srv = null;
                InputStream strIn = null;
                OutputStream strOut = null;
                List<String> recipients = null;
                
                try {
                    srv = sockBlock.accept();
                    strIn = srv.getInputStream();
                    strOut = srv.getOutputStream();
                    boolean doLoop = true;

                    do {
                        switch (strIn.read()) {
                        case codeSubmitStream:
                            if (utils.debugMode) {
                                log.info("Received codeSubmitStream:");
                            }
                            int ok = waitSubmitOK();
                            if (ok == responseOK) {
                                SwingUtilities.invokeAndWait(new SubmitRunner(strIn, recipients)); // Accept new faxes only sequentially
                                recipients = null;
                            }
                            strOut.write(ok);
                            if (utils.debugMode) {
                                log.info("Wrote response: " + ok);
                            }
                            break;
                        case codeSubmitFile:
                            ok = waitSubmitOK();
                            if (ok == responseOK) {
                                BufferedReader bufR = new BufferedReader(new InputStreamReader(strIn));
                                String[] fileNames = { bufR.readLine() };

                                SwingUtilities.invokeAndWait(new SubmitRunner(Arrays.asList(fileNames), recipients)); // Accept new faxes only sequentially
                                recipients = null;
                                //bufR.close();
                            }
                            strOut.write(ok);
                            break;
                        case codeMultiSubmitFile:
                            if (utils.debugMode) {
                                log.info("Received codeMultiSubmitFiles:");
                            }
                            ok = waitSubmitOK();
                            if (ok == responseOK) {
                                BufferedReader bufR = new BufferedReader(new InputStreamReader(strIn));
                                ArrayList<String> fileNames = new ArrayList<String>();
                                String line = bufR.readLine();

                                while (line != null && !line.equals(multiFileEOF)) {
                                    if (utils.debugMode) {
                                        log.finer(line);
                                    }
                                    fileNames.add(line);
                                    line = bufR.readLine();
                                }

                                SwingUtilities.invokeAndWait(new SubmitRunner(fileNames, recipients)); // Accept new faxes only sequentially
                                recipients = null;
                                //bufR.close();
                            }
                            strOut.write(ok);
                            if (utils.debugMode) {
                                log.info("Wrote response: " + ok);
                            }
                            break;
                        case codeAddRecipients:
                        {
                            if (utils.debugMode) {
                                log.info("Received codeAddRecipients:");
                            }
                            recipients = new ArrayList<String>();
                            BufferedReader bufR = new BufferedReader(new InputStreamReader(strIn));
                            String line = bufR.readLine();

                            while (line != null && !line.equals(multiFileEOF)) {
                                if (utils.debugMode) {
                                    log.finer(line);
                                }
                                recipients.add(line);
                                line = bufR.readLine();
                            }
                            
                            strOut.write(responseOK);
                        }    
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
                        case codeQuit:
                            doLoop = false;
                            break;
                        default:
                            log.log(Level.WARNING, "Unknown code received.");
                            strOut.write(responseUnknownOpCode);
                            break;
                        }

                        strOut.flush();
                    } while (doLoop && !srv.isClosed());
                    
                    if (utils.debugMode) {
                        log.info("Closed connection cleanly.");
                    }
                } catch (Exception e) {
                    if (utils.debugMode) {
                        log.log(Level.INFO, "Maybe error listening for connections:", e);
                        //e.printStackTrace(utils.debugOut);
                    }
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
        protected InputStream inStream;
        protected List<String> fileNames;
        protected List<String> recipients;
        protected boolean closeAfterSubmit;
        
        public void run() {
            application.toFront();
            doSubmit();
            if (closeAfterSubmit)
                application.dispose();
        }
        
        protected void doSubmit() {
            SendWinControl sw = SendController.createSendWindow(application, application.clientManager, false, true);

            if (inStream != null) {                
                sw.addInputStream(inStream);
            } else {
                for (String fileName : fileNames)
                    sw.addLocalFile(fileName);
            }
            if (recipients != null && recipients.size() > 0) {
                for (String num : recipients) {
                    sw.addRecipient(num, "", "", "", "");
                }
            }
            sw.setVisible(true);
        }
        
        public SubmitRunner(List<String> fileNames, InputStream strIn, List<String> recipients, boolean closeAfterSubmit) {
            super();
            this.fileNames = fileNames;
            this.inStream = strIn;
            this.recipients = recipients;
            this.closeAfterSubmit = closeAfterSubmit;
        }
        
        
        public SubmitRunner(List<String> fileNames, List<String> recipients) {
            this(fileNames, null, recipients, false);
        }
        
        public SubmitRunner(InputStream strIn, List<String> recipients) {
            this(null, strIn, recipients, false);
        }
    }
}
