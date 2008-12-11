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
 * Launcher2.java:
 * This is a (somewhat half-baked) implementation to detect already running instances
 * and to pass the command line parameters/data (in the case of --stdin) to the already
 * running instance.
 * I'm using a combination of a lock file and a ServerPort here to ensure that 
 * every user on a multi-user machine can run his own instance (I want to restrict it to 
 * one instance *per user* and _not_ one instance per machine).
 * The current implementation is far from perfect, so if someone with more java
 * experience knows a better way to accomplish this functionality, please let me know.
 */

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import yajhfc.PluginManager.PluginInfo;
import yajhfc.PluginManager.PluginType;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.send.SendController;
import yajhfc.send.SendWinControl;
import yajhfc.util.ExampleFileFilter;

public final class Launcher2 {
    private static final Logger launchLog = Logger.getLogger(Launcher2.class.getName());
    
    // Configuration directory as set on the command line
    // Needs to be set *before* Utils initializes in order to have an effect!
    public static String cmdLineConfDir = null;
    
    static ServerSocket sockBlock = null;
    static SockBlockAcceptor blockThread;
    public static MainWin application;
    static boolean isLocking = false;
    
    final static int codeSubmitStream = 1;
    final static int codeSubmitFile = 2;
    final static int codeMultiSubmitFile = 4;
    final static int codeToForeground = 3;
    final static int codeAddRecipients = 5;
    final static int codeUseCover = 6;
    final static int codeSetSubject = 7;
    final static int codeSetComment = 8;
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
        return new File(Utils.getConfigDir(), "lock");
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
    
    
    
    /**
     * The minimum value of a "real" short option
     */
    private static final int MIN_OPT_VAL = 'A';
    /**
     * Prints usage information
     * @param out
     */
    public static void printHelp(PrintStream outStream, LongOpt[] options) {
        StringBuilder out = new StringBuilder();
        
        int optionwidth = 0;
        for (LongOpt option : options) {
            if (option instanceof ExtLongOpt) {
                String argDesc = ((ExtLongOpt)option).getArgDesc();
                optionwidth = Math.max(optionwidth, option.getName().length() + ((argDesc == null) ? 0 : argDesc.length() + 1) + ((option.getHasArg() == LongOpt.OPTIONAL_ARGUMENT) ? 2 : 0) );
            }
        }
        optionwidth += 1;
        out.append("Usage:\n");
        out.append("java -jar yajhfc.jar [OPTIONS]... [FILES TO SEND]...\n\n");
        out.append("Argument description:\n");
        for (LongOpt option : options) {
            if (!(option instanceof ExtLongOpt))
                continue;
            
             if (option.getVal() < MIN_OPT_VAL) {
                 out.append("  ");
             } else {
                 out.append('-');
                 out.append((char)option.getVal());
             }
             if (option.getName() == null) {
                 appendSpaces(out, optionwidth + 4);
             } else {
                 if (option.getVal() < MIN_OPT_VAL) {
                     out.append("  --");
                 } else {
                     out.append(", --");
                 }
                 out.append(option.getName());
                 String argDesc = ((ExtLongOpt)option).getArgDesc();
                 if (argDesc != null) {
                     if (option.getHasArg() == LongOpt.OPTIONAL_ARGUMENT) {
                         out.append('[');
                     }
                     out.append('=').append(argDesc);
                     if (option.getHasArg() == LongOpt.OPTIONAL_ARGUMENT) {
                         out.append(']');
                     }
                     appendSpaces(out, optionwidth-option.getName().length()-argDesc.length()-((option.getHasArg() == LongOpt.OPTIONAL_ARGUMENT) ? 3 : 1));
                 } else {
                     appendSpaces(out, optionwidth-option.getName().length());
                 }
             }
             printWrapped(out, optionwidth + 6, ((ExtLongOpt)option).getDescription());
             out.append('\n');
        }
        
        outStream.print(out);
    }
    
    private static void appendSpaces(StringBuilder out, int numspaces) {
        for (int i=0; i<numspaces; i++) {
            out.append(' ');
        }
    }
    
    private static final int SCREEN_WIDTH = 80;
    private static Pattern wordSplitter;
    private static void printWrapped(StringBuilder out, int indent, String text) {
        int pos = indent;
        if (wordSplitter == null)
            wordSplitter = Pattern.compile("(\\s|\n)+");
        for (String word: wordSplitter.split(text)) {
            pos += word.length();
            if (pos > SCREEN_WIDTH) {
                out.append('\n');
                appendSpaces(out, indent);
                pos = indent + word.length();
            }
            out.append(word);
            if (pos < SCREEN_WIDTH-1) {
                out.append(' ');
                pos+=1;
            }
        }
    }
    
//    private static void printHelp() {
//        System.out.print(
//            "General usage:\n"+
//            "java -jar yajhfc.jar [--help] [--debug] [--admin] [--background|--noclose] \n" +
//            "         [--configdir=directory] [--loadplugin=filename] [--logfile=filename]\n" +
//            "         [--showtab=0|R|1|S|2|T] [--recipient=...] [--stdin | filename ...]\n"+
//            "Argument description:\n"+
//            "filename     One or more file names of PostScript files to send.\n"+
//            "--stdin      Read the file to send from standard input.\n"+
//            "--recipient  Specifies the phone number of a recipient to send the fax to.\n"+
//            "             You may specify multiple arguments for multiple recipients.\n"+
//            "--admin      Start up in admin mode.\n"+
//            "--debug      Output some debugging information.\n"+
//            "--logfile    The logfile to log debug information to (if not specified, use stdout).\n"+
//            "--background If there is no already running instance, launch a new instance \n" +
//            "             and terminate (after submitting the file to send).\n" +
//            "--noclose    Do not close YajHFC after submitting the fax.\n"+
//            "--showtab    Sets the tab to display on startup. Specify 0 or R for the \"Received\", \n"+
//            "             1 or S for the \"Sent\" or 2 or T for the \"Transmitting\" tab.\n"+
//            "--loadplugin Specifies the jar file of a YajHFC plugin to load.\n" +
//            "--loaddriver Specifies the location of a JDBC driver JAR file.\n" +
//            "--no-plugins Disables loading plugins from the plugin.lst file.\n" +
//            "--no-gui     Sends a fax with a minimal GUI.\n" +
//            "--configdir  Sets a configuration directory to use instead of ~/.yajhfc\n" +
//            "--help       Displays this text.\n"
//            );   
//    }
//
//    /**
//     * Strips quotes (" or ') at the beginning and end of the specified String
//     * @param str
//     * @return
//     */
//    public static String stripQuotes(String str)
//    {
//        char c = str.charAt(0);
//        if (c == '\"' || c == '\'') {
//            c = str.charAt(str.length()-1);
//            if (c == '\"' || c == '\'') {
//                return str.substring(1, str.length()-1);
//            }
//        }
//        return str;
//    }
    
   
    
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
        boolean noGUI = false;
        int selectedTab = -1;
        String logFile = null;
        boolean appendToLog = false;
        Boolean useCover = null; // Use cover? null: Don't change, else use booleanValue()
        String subject = null;
        String comment = null;
        
        final LongOpt[] longOpts = new LongOpt[] {
                new ExtLongOpt("recipient", LongOpt.REQUIRED_ARGUMENT, null, 'r', "RECIPIENT", "Specifies a recipient to send the fax to. You may specify either a fax number or detailed cover page information (see the FAQ for the format in the latter case). You may specify --recipient multiple times for multiple recipients."),
                new ExtLongOpt("use-cover", LongOpt.OPTIONAL_ARGUMENT, null, 'C', "yes|no", "Use a cover page for sending a fax?"),
                new ExtLongOpt("subject", LongOpt.REQUIRED_ARGUMENT, null, 's', "SUBJECT", "The fax subject for the cover page."),
                new ExtLongOpt("comment", LongOpt.REQUIRED_ARGUMENT, null, 9, "COMMENT", "The comment for the cover page."),
                new ExtLongOpt("stdin", LongOpt.NO_ARGUMENT, null, 1, null, "Read the file to send from standard input."),
                new ExtLongOpt("admin", LongOpt.NO_ARGUMENT, null, 'A', null, "Start up in admin mode."),
                new ExtLongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd', null, "Output some debugging information."),
                new ExtLongOpt("logfile", LongOpt.REQUIRED_ARGUMENT, null, 'l', "LOGFILE", "The logfile to log debug information to (if not specified, use stdout)."),
                new ExtLongOpt("appendlogfile", LongOpt.REQUIRED_ARGUMENT, null, 6, "LOGFILE", "Append debug information to the given log file."),
                new ExtLongOpt("background", LongOpt.NO_ARGUMENT, null, 2, null, "If there is no already running instance, launch a new instance and terminate (after submitting the file to send)."),
                new ExtLongOpt("noclose", LongOpt.NO_ARGUMENT, null, 3, null, "Do not close YajHFC after submitting the fax."),
                new ExtLongOpt("showtab", LongOpt.REQUIRED_ARGUMENT, null, 'T', "0|R|1|S|2|T", "Sets the tab to display on startup. Specify 0 or R for the \"Received\", 1 or S for the \"Sent\" or 2 or T for the \"Transmitting\" tab."),
                new ExtLongOpt("loadplugin", LongOpt.REQUIRED_ARGUMENT, null, 4, "JARFILE", "Specifies a jar file of a YajHFC plugin to load."),
                new ExtLongOpt("loaddriver", LongOpt.REQUIRED_ARGUMENT, null, 5, "JARFILE", "Specifies the location of a JDBC driver JAR file to load."),
                new ExtLongOpt("no-plugins", LongOpt.NO_ARGUMENT, null, 7, null, "Disables loading plugins from the plugin.lst file."),
                new ExtLongOpt("no-gui", LongOpt.NO_ARGUMENT, null, 8,  null, "Sends a fax with only a minimal GUI."),
                new ExtLongOpt("configdir", LongOpt.REQUIRED_ARGUMENT, null, 'c', "DIRECTORY", "Sets the configuration directory to use instead of ~/.yajhfc"),
                new ExtLongOpt("help", LongOpt.NO_ARGUMENT, null, 'h', null, "Displays this text.")
        };
        final String[] origArgs = args.clone();
        
        Getopt getopt = new Getopt("yajhfc", args, "hAdc:r:T:l:C::s:", longOpts);
        int opt;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case 1: //stdin
                useStdin = true;
                break;
            case 2: //background
                forkNewInst = true;
                break;
            case 3: //noclose
                closeAfterSubmit = false;
                break;
            case 4: // loadplugin
                plugins.add(new PluginInfo(new File(getopt.getOptarg()),
                        PluginType.PLUGIN, false));
                break;
            case 5: //loaddriver
                plugins.add(new PluginInfo(new File(getopt.getOptarg()),
                        PluginType.JDBCDRIVER, false));
                break;
            case 'l': //logfile
                logFile = getopt.getOptarg();
                appendToLog = false;
                break;
            case 6: //appendlogfile
                logFile = getopt.getOptarg();
                appendToLog = true;
                break;
            case 7: // no-plugins
                noPlugins = true;
                break;
            case 8: // no-gui
                noGUI = true;
                break;
            case 9: // comment
                comment = getopt.getOptarg();
                break;
            case 'C': // use-cover
                String optarg = getopt.getOptarg();
                if (optarg == null || optarg.equals("") || optarg.startsWith("y") || optarg.equals("true")) {
                    useCover = Boolean.TRUE;
                } else {
                    useCover = Boolean.FALSE;
                }
                break;
            case 's': // subject
                subject = getopt.getOptarg();
                break;
            case 'h': // help
                printHelp(System.out, longOpts);
                System.exit(0);
                break;
            case 'A': // admin
                adminMode = true;
                break;
            case 'd': // debug
                debugMode = true;
                break;
            case 'c': // configdir
                cmdLineConfDir = getopt.getOptarg();
                break;
            case 'r': // recipient
                recipients.add(getopt.getOptarg());
                break;
            case 'T': // showtab
                switch (getopt.getOptarg().charAt(0)) {
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
                    System.err.println("Unknown tab: " + getopt.getOptarg());
                }
                break;
            case '?':
                break;
            default:
                System.err.println("Unknown option \'" + (char)opt + "\' in " + args[getopt.getOptind()]);
                break;
            }
        }
        // Add non-option arguments:
        for (int i=getopt.getOptind(); i<args.length; i++) {
            fileNames.add(args[i]);
        }
        
//        for (int i = 0; i < args.length; i++) {
//            String curArg = args[i];
//            if (curArg.startsWith("--")) { // command line argument
//                if (curArg.equals("--help")) {
//                    printHelp();
//                    System.exit(0);
//                } else if (curArg.equals("--stdin"))
//                    useStdin = true;
//                else if (curArg.equals("--admin"))
//                    adminMode = true;
//                else if (curArg.equals("--debug"))
//                    debugMode = true;
//                else if (curArg.equals("--background"))
//                    forkNewInst = true;
//                else if (curArg.equals("--noclose")) 
//                    closeAfterSubmit = false;
//                else if (curArg.startsWith("--configdir="))
//                    cmdLineConfDir = stripQuotes(curArg.substring(12)); // 12 == "--configdir=".length()
//                else if (curArg.startsWith("--recipient="))
//                    recipients.add(stripQuotes(curArg.substring(12))); // 12 == "--recipient=".length()
//                else if (curArg.startsWith("--showtab=") && curArg.length() > 10) { // 10 == ""--showtab="".length()
//                    switch (curArg.charAt(10)) {
//                    case '0':
//                    case 'R':
//                    case 'r':
//                        selectedTab = 0;
//                        break;
//                    case '1':
//                    case 'S':
//                    case 's':
//                        selectedTab = 1;
//                        break;
//                    case '2':
//                    case 'T':
//                    case 't':
//                        selectedTab = 2;
//                        break;
//                    default:
//                        System.err.println("Unknown tab: " + curArg.substring(10));
//                    }
//                } else if (curArg.startsWith("--loadplugin=")) {
//                    plugins.add(new PluginInfo(new File(stripQuotes(curArg.substring("--loadplugin=".length()))),
//                            PluginType.PLUGIN, false));
//                } else if (curArg.startsWith("--loaddriver=")) {
//                    plugins.add(new PluginInfo(new File(stripQuotes(curArg.substring("--loaddriver=".length()))), 
//                            PluginType.JDBCDRIVER, false));
//                } else if (curArg.startsWith("--logfile=")) {
//                    logFile = stripQuotes(curArg.substring("--logfile=".length()));
//                    appendToLog = false;
//                } else if (curArg.startsWith("--appendlogfile=")) {
//                    logFile = stripQuotes(curArg.substring("--appendlogfile=".length()));
//                    appendToLog = true;
//                } else if (curArg.equals("--no-plugins")) {
//                    noPlugins = true;
//                } else if (curArg.equals("--no-gui")) {
//                    noGUI = true;
//                } else {
//                    System.err.println("Unknown command line argument: " + curArg);
//                }
//            } else if (curArg.startsWith("-"))
//                System.err.println("Unknown command line argument: " + curArg);
//            else // treat argument as file name to send
//                fileNames.add(curArg);
//        }
        
        if (debugMode && ":prompt:".equals(logFile)) {
            logFile = (new LogFilePrompter()).promptForLogfile();
            if (logFile == null) {
                debugMode = false;
            }
        }
        
        // IMPORTANT: Don't access Utils before this line!
        Utils.debugMode = debugMode;
        
        if (debugMode) {
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler theHandler;
            if (logFile != null) {
//              try {
//              Utils.debugOut = new PrintStream(new FileOutputStream(logFile));
//              } catch (FileNotFoundException e) {
//              Utils.debugOut = System.out;
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

            //ConsoleLogger.setAllOutputStreams(Utils.debugOut);

            launchLog.config("YajHFC version: " + Utils.AppVersion);
            launchLog.config("---- BEGIN System.getProperties() dump");
            Utils.dumpProperties(System.getProperties(), launchLog);
            launchLog.config("---- END System.getProperties() dump");
            launchLog.config("" + origArgs.length + " command line arguments:");
            for (String arg : origArgs) {
                launchLog.config(arg);
            }
        }
        
        if (noGUI) {
            launchLog.fine("Starting up without GUI...");
            NoGUISender.startUpWithoutUI(recipients, fileNames, plugins, noPlugins, useStdin, (useCover == null) ? false : useCover, subject, comment);
            return;
        }
        
        Socket oldinst = checkLock();
        
        if (forkNewInst && (oldinst == null)) {
            try {                
                List<String> launchArgs = new ArrayList<String>(20);
                launchArgs.add(System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java");
                launchArgs.add("-classpath");
                launchArgs.add(System.getProperty("java.class.path"));
                launchArgs.add(Launcher2.class.getCanonicalName());
                

                if (adminMode) {
                    launchArgs.add("--admin");
                }
                if (Utils.debugMode) {
                    launchArgs.add("--debug");
                }
                if (logFile != null) {
                    launchArgs.add("--appendlogfile=\"" + logFile + "\"");
                }
                if (!closeAfterSubmit) {
                    launchArgs.add("--noclose");
                }
                if (cmdLineConfDir != null) {
                    launchArgs.add("--configdir=" + cmdLineConfDir);
                }
                if (selectedTab >= 0) {
                    launchArgs.add("--showtab=" + selectedTab);
                }
                if (noPlugins) {
                    launchArgs.add("--no-plugins");
                }
                for (PluginInfo entry : plugins) {
                    switch (entry.type) {
                    case PLUGIN:
                        launchArgs.add("--loadplugin=\"" + entry.file.getAbsolutePath() + "\"");
                        break;
                    case JDBCDRIVER:
                        launchArgs.add("--loaddriver=\"" + entry.file.getAbsolutePath() + "\"");
                        break;
                    }
                }
                
                if (Utils.debugMode) {
                    launchLog.info("Launching new instance:");
                    for (int i = 0; i < launchArgs.size(); i++) {
                        launchLog.info("launchArgs[" + i + "] = " + launchArgs.get(i));
                    }
                }
                new ProcessBuilder(launchArgs).start();
                
                int time = 0;
                if (Utils.debugMode) {
                    launchLog.info("Waiting for new instance... ");
                }
                
                do {
                    Thread.sleep(200);
                    time += 200;
                    if (time > 20000) {
                        throw new TimeoutException(Utils._("The new instance did not start after 20 seconds."));
                    }
                    
                    oldinst = checkLock();
                } while (oldinst == null);
                
                if (Utils.debugMode) {
                    launchLog.info("New instance has been started.");
                }
            } catch (Exception e) {
                if (Utils.debugMode) {
                    launchLog.log(Level.WARNING, "Exception launching new instance:", e);
                    //e.printStackTrace(Utils.debugOut);
                }
                JOptionPane.showMessageDialog(null, Utils._("Cannot launch new program instance, continuing with the existing one!\nReason: ") + e.toString() );
            }
        }
        
        if (oldinst == null) {
            if (Utils.debugMode) {
                launchLog.info("No old instance found, creating lock...");
            }
            createLock();
            
            // Load plugins:
            if (!noPlugins) {
                PluginManager.readPluginList();
            }
            PluginManager.addPlugins(plugins);
            PluginManager.loadAllKnownPlugins();
            
            SwingUtilities.invokeLater(new NewInstRunner(
                    SubmitRunner.createWhenNecessary(fileNames, useStdin, recipients, closeAfterSubmit, comment, subject, useCover), 
                    adminMode, selectedTab));
            blockThread = new SockBlockAcceptor();
            blockThread.start();
            if (Utils.debugMode) {
                launchLog.info("Lock and listener created.");
            }
        } else {            
            try {
                if (Utils.debugMode) {
                    launchLog.info("Found old instance at: " + oldinst);
                }
                DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(oldinst.getOutputStream()));
                DataInputStream inStream = new DataInputStream(oldinst.getInputStream());
                
                if (recipients.size() > 0)
                {
                    outStream.write(codeAddRecipients);
                    for (String number : recipients) {
                        outStream.writeUTF(number);
                    }
                    outStream.writeUTF(multiFileEOF);
                    
                    outStream.flush();
                    checkResponse(inStream);
                }
                if (useCover != null) {
                    outStream.write(codeUseCover);
                    outStream.writeBoolean(useCover.booleanValue());
                    outStream.flush();
                    checkResponse(inStream);
                }
                if (subject != null) {
                    outStream.write(codeSetSubject);
                    outStream.writeUTF(subject);
                    outStream.flush();
                    checkResponse(inStream);
                }
                if (comment != null) {
                    outStream.write(codeSetComment);
                    outStream.writeUTF(comment);
                    outStream.flush();
                    checkResponse(inStream);
                }
                
                if (useStdin) { 
                    outStream.write(codeSubmitStream);
                    Utils.copyStream(System.in, outStream);
                } else if ( fileNames != null && fileNames.size() > 0) {
                    outStream.write(codeMultiSubmitFile);
                    for (String fileName : fileNames) {
                        File f = new File(fileName);
                        outStream.writeUTF(f.getAbsolutePath());
                    }
                    outStream.writeUTF(multiFileEOF);
                } else if (forkNewInst) {
                    outStream.write(codeToForeground);
                } else {
                    outStream.write(codeToForeground);
                    
                    launchLog.log(Level.WARNING, Utils._("There already is a running instance!"));
                }
                //outStream.write(codeQuit);
                outStream.flush();
                oldinst.shutdownOutput();
                
                int response = inStream.read();
                
                if (!oldinst.isClosed()) {
                    oldinst.close();
                }
                
                if (Utils.debugMode) {
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
    
    private static void checkResponse(InputStream inStream) throws IOException {
        int response = inStream.read();
        if (response != 0) {
            if (response > 0) {
                System.exit(response);
            } else {
                System.exit(responseGeneralError);
            }
        }
    }
    
    static class LogFilePrompter implements Runnable {
        protected String selection = null;
        
        public void run() {
            JFileChooser chooser = new yajhfc.util.SafeJFileChooser();
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
        protected final boolean adminMode;
        protected final int selectedTab;
        protected final SubmitRunner loginRunner;
        
        public void run() {
            Utils.setLookAndFeel(Utils.getFaxOptions().lookAndFeel);
            
            application = new MainWin(adminMode);
            application.setVisible(true);
            
            application.reconnectToServer(loginRunner);
            if (selectedTab >= 0) {
                application.setSelectedTab(selectedTab);
            }
        }   
        
        public NewInstRunner(SubmitRunner loginRunner, boolean adminMode, int selectedTab) {
            this.loginRunner = loginRunner;
            
            this.adminMode = adminMode;
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
                DataInputStream strIn = null;
                DataOutputStream strOut = null;
                List<String> recipients = null;
                Boolean useCover = null;
                String subject = null;
                String comment = null;
                
                try {
                    srv = sockBlock.accept();
                    strIn = new DataInputStream(srv.getInputStream());
                    strOut = new DataOutputStream(srv.getOutputStream());
                    boolean doLoop = true;

                    do {
                        switch (strIn.read()) {
                        case codeSubmitStream:
                            if (Utils.debugMode) {
                                log.info("Received codeSubmitStream:");
                            }
                            int ok = waitSubmitOK();
                            if (ok == responseOK) {
                                SwingUtilities.invokeAndWait(new SubmitRunner(null, strIn, recipients, false, comment, subject, useCover)); // Accept new faxes only sequentially
                                recipients = null;
                                useCover = null;
                                subject = null;
                                comment = null;
                            }
                            strOut.write(ok);
                            if (Utils.debugMode) {
                                log.info("Wrote response: " + ok);
                            }
                            break;
                        case codeSubmitFile:
                            ok = waitSubmitOK();
                            if (ok == responseOK) {
                                String[] fileNames = { strIn.readUTF() };

                                SwingUtilities.invokeAndWait(new SubmitRunner(Arrays.asList(fileNames), null, recipients, false, comment, subject, useCover)); // Accept new faxes only sequentially
                                recipients = null;
                                useCover = null;
                                subject = null;
                                comment = null;
                            }
                            strOut.write(ok);
                            break;
                        case codeMultiSubmitFile:
                            if (Utils.debugMode) {
                                log.info("Received codeMultiSubmitFiles:");
                            }
                            ok = waitSubmitOK();
                            if (ok == responseOK) {
                                ArrayList<String> fileNames = new ArrayList<String>();
                                String line = strIn.readUTF();

                                while (line != null && !line.equals(multiFileEOF)) {
                                    if (Utils.debugMode) {
                                        log.finer(line);
                                    }
                                    fileNames.add(line);
                                    line = strIn.readUTF();
                                }

                                SwingUtilities.invokeAndWait(new SubmitRunner(fileNames, null, recipients, false , comment, subject, useCover)); // Accept new faxes only sequentially
                                recipients = null;
                                useCover = null;
                                subject = null;
                                comment = null;
                            }
                            strOut.write(ok);
                            if (Utils.debugMode) {
                                log.info("Wrote response: " + ok);
                            }
                            break;
                        case codeAddRecipients:
                        {
                            if (Utils.debugMode) {
                                log.info("Received codeAddRecipients:");
                            }
                            recipients = new ArrayList<String>();
                            String line = strIn.readUTF();
                            while (line != null && !line.equals(multiFileEOF)) {
                                if (Utils.debugMode) {
                                    log.finer(line);
                                }
                                recipients.add(line);
                                line = strIn.readUTF();
                            }
                            
                            strOut.write(responseOK);
                        }    
                        break;
                        case codeUseCover:
                            useCover = Boolean.valueOf(strIn.readBoolean());
                            strOut.write(responseOK);
                            break;
                        case codeSetComment:
                            comment = strIn.readUTF();
                            strOut.write(responseOK);
                            break;
                        case codeSetSubject:
                            subject = strIn.readUTF();
                            strOut.write(responseOK);
                            break;
                        case codeToForeground:
                        case -1: // Connection closed without sending any data
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    application.bringToFront();
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
                    
                    if (Utils.debugMode) {
                        log.info("Closed connection cleanly.");
                    }
                } catch (Exception e) {
                    if (Utils.debugMode) {
                        log.log(Level.INFO, "Maybe error listening for connections:", e);
                        //e.printStackTrace(Utils.debugOut);
                    }
                } finally {
                    try {
                        strIn.close();
                        strOut.close();
                    } catch(Exception e) {
                        // NOP
                    }
                    try {
                        if (srv != null && !srv.isClosed()) {                        
                            srv.close();
                        }
                    } catch(Exception e) {
                        // NOP
                    }
                    srv = null;
                    strIn = null;
                    strOut = null;
                }
            }
        }
        
        public SockBlockAcceptor() {
            super(SockBlockAcceptor.class.getName());
        }
    }
    
    static class SubmitRunner implements Runnable {
        protected final InputStream inStream;
        protected final List<String> fileNames;
        protected final List<String> recipients;
        protected final boolean closeAfterSubmit;
        protected final Boolean useCover;
        protected final String subject;
        protected final String comment;
        
        public void run() {
            application.bringToFront();
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
                    sw.addRecipient(new DefaultPBEntryFieldContainer().parseFromString(num));
                }
            }
            if (useCover != null) {
                sw.setUseCover(useCover);
            }
            if (subject != null) {
                sw.setSubject(subject);
            }
            if (comment != null) {
                sw.setComment(comment);
            }
            sw.setVisible(true);
        }
        
        
        
        public SubmitRunner(List<String> fileNames, InputStream inStream,
                List<String> recipients, boolean closeAfterSubmit,
                String comment, String subject, Boolean useCover) {
            super();
            this.fileNames = fileNames;
            this.inStream = inStream;
            this.recipients = recipients;
            this.closeAfterSubmit = closeAfterSubmit;
            this.comment = comment;
            this.subject = subject;
            this.useCover = useCover;
        }
        
        /**
         * Creates a SubmitRunner if one is necessary given the command line information.
         * If no runner is necessary, returns null
         * @param fileNames
         * @param inStream
         * @param recipients
         * @param closeAfterSubmit
         * @param comment
         * @param subject
         * @param useCover
         * @return
         */
        public static SubmitRunner createWhenNecessary(List<String> fileNames, boolean useStdin,
                List<String> recipients, boolean closeAfterSubmit,
                String comment, String subject, Boolean useCover) {
            
            if ((fileNames != null && fileNames.size() > 0) || useStdin) {
                return new SubmitRunner(fileNames, useStdin ? System.in : null, recipients, closeAfterSubmit, comment, subject, useCover);
            } else {
                return null;
            }
        }
       
    }
}
