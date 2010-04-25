/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.launch;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import yajhfc.HylaClientManager;
import yajhfc.MainWin;
import yajhfc.NoGUISender;
import yajhfc.Utils;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginManager.PluginInfo;
import yajhfc.readstate.PersistentReadState;
import yajhfc.shutdown.ShutdownManager;
import yajhfc.splashscreen.YJSplashScreen;
import yajhfc.util.ExternalProcessExecutor;

/**
 * Main launcher class containing the main method and some utility functions that may be used
 * before yajhfc.Utils has initialized.
 * @author jonas
 *
 */
public class Launcher2 {

    static final Logger launchLog = Logger.getLogger(Launcher2.class.getName());
    
    // Configuration directory as set on the command line
    // Needs to be set *before* Utils initializes in order to have an effect!
    public static String cmdLineConfDir = null;
    
    public static MainApplicationFrame application;
    
    /**
     * The main method
     * @param args
     */
    public static void main(String[] args) {
        CommandLineOpts opts = new CommandLineOpts(args);
        cmdLineConfDir = opts.configDir;
        
        // IMPORTANT: Don't access Utils before this line!
        Utils.debugMode = opts.debugMode;
        
        if (opts.debugMode) {
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler theHandler;
            if (opts.logFile != null) {
                try {
                    theHandler = new StreamHandler(new FileOutputStream(opts.logFile, opts.appendToLog), new SimpleFormatter());
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
            launchLog.config("" + args.length + " command line arguments:");
            for (String arg : args) {
                launchLog.config(arg);
            }
        }
        
        Utils.initializeUIProperties();
        if (opts.noGUI) {
            noGUIStartup(opts);
        } else {
            if (needSubmitProtocol(opts) && opts.closeAfterSubmit && !opts.forkNewInst) {
                sendOnlyStartup(opts);
            } else {
                normalStartup(opts);
            }
        } 
    }
    
    /**
     * Normal application startup
     * @param opts
     */
    public static void normalStartup(CommandLineOpts opts) {
        if (Utils.debugMode) {
            launchLog.info("Normal startup...");
        }
        Lock oldInst = Lock.checkLock();
        if (opts.forkNewInst && (oldInst == null)) {
            oldInst = forkNewInstance(opts);
        }
        
        if (oldInst == null) {
            if (Utils.debugMode) {
                launchLog.info("No old instance found, creating lock...");
            }
            Lock.createLock();
            initializePlugins(opts.plugins, opts.noPlugins);
            
            SendWinSubmitProtocol submitProto;
            if (needSubmitProtocol(opts)) {
                try {
                    submitProto = new SendWinSubmitProtocol();
                    fillSubmitProtocol(submitProto, opts);
                    submitProto.setCloseAfterSubmit(opts.closeAfterSubmit);
                    submitProto.prepareSubmit();
                } catch (IOException e) {
                    launchLog.log(Level.WARNING, "Error preparing SendWin:", e);
                    submitProto = null;
                }
            } else {
                submitProto = null;
            }
            SwingUtilities.invokeLater(new NewInstRunner(submitProto,
                    opts.adminMode, opts.selectedTab, opts.desiredWindowState));
            
            Lock.startLockThread();
            if (Utils.debugMode) {
                launchLog.info("Lock and listener created.");
            }
            ShutdownManager.getInstance().registerShutdownHook(new Runnable() {
                public void run() {                      
                    if (Utils.debugMode)
                        System.err.println("Doing shutdown work...");
                    
                    application.saveWindowSettings();
                    
                    PersistentReadState.getCurrent().persistReadState();

                    Utils.storeOptionsToFile();
                    Lock.releaseLock();
                    
                    if (Utils.debugMode)
                        System.err.println("Shutdown work finished.");
                } 
            });
        } else {
            try {
                launchLog.info("Connecting to old instance...");
                if (needSubmitProtocol(opts)) {
                    launchLog.info("Submitting fax using the old instance");
                    fillSubmitProtocol(oldInst, opts);
                    oldInst.submit(!opts.noWait);
                } else {
                    launchLog.fine("There already is a running instance!");
                    getConsoleWriter().println(Utils._("There already is a running instance!"));
                    oldInst.bringToFront();
                }
                oldInst.close();
                launchLog.info("All information submitted to old instance, terminating cleanly.");
                System.exit(0);
            } catch (ResponseException re) {
                int errorCode = re.getErrorCode();
                launchLog.log(Level.INFO, "The old instance gave back an error", re);
                if (errorCode >= 0 && errorCode <= 255) {
                    System.exit(errorCode);
                } else {
                    System.exit(Lock.RESPONSE_GENERAL_ERROR);
                }
            } catch (Exception e) {
                launchLog.log(Level.WARNING, "An error occured communicating with the old instance: ", e);
                System.exit(Lock.RESPONSE_GENERAL_ERROR);
            }
        }
    }
    
    /**
     * Startup with a minimal GUI
     * @param opts
     */
    public static void noGUIStartup(CommandLineOpts opts) {
        if (Utils.debugMode) {
            launchLog.info("No GUI startup...");
        }
        NoGUISender.startUpWithoutUI(opts);
    }
    
    /**
     * Start up only to show the send dialog
     * @param opts
     */
    public static void sendOnlyStartup(CommandLineOpts opts) {
        if (Utils.debugMode) {
            launchLog.info("Send only startup...");
        }
        try {
            initializePlugins(opts.plugins, opts.noPlugins);
            
            final SendWinSubmitProtocol submitProto = new SendWinSubmitProtocol();
            fillSubmitProtocol(submitProto, opts);
            submitProto.setCloseAfterSubmit(true);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //Utils.initializeUIProperties();
                    
                    NoGUISender dummyFrame = new NoGUISender();
                    dummyFrame.clientManager = new HylaClientManager(Utils.getFaxOptions());
                    Launcher2.application = dummyFrame;
                    try {
                        submitProto.submit(true);
                    } catch (IOException e) {
                        launchLog.log(Level.WARNING, "Error submitting fax", e);
                    }
                    System.exit(0);
                } 
            });
            ShutdownManager.getInstance().registerShutdownHook(new Runnable() {
                public void run() {   
                    if (Utils.debugMode)
                        System.err.println("Doing shutdown work...");
                    
                    //PersistentReadState.getCurrent().persistReadState();

                    Utils.storeOptionsToFile();
                    //Lock.releaseLock();
                    
                    if (Utils.debugMode)
                        System.err.println("Shutdown work finished.");
                } 
            });
        } catch (Exception e) {
            launchLog.log(Level.WARNING, "Error submitting fax", e);
        }
    }
    
    /**
     * Checks if the command line options imply showing a SendWin
     */
    static boolean needSubmitProtocol(CommandLineOpts opts) {
        return ((opts.fileNames.size() > 0) || 
                opts.useStdin || 
                (opts.recipients.size() > 0));
    }
    
    static void fillSubmitProtocol(SubmitProtocol sp, CommandLineOpts opts) throws IOException{
        if (opts.fileNames.size() > 0) {
            sp.addFiles(opts.fileNames);
        }
        if (opts.recipients.size() > 0) {
            sp.addRecipients(opts.recipients);
        }
        if (opts.useStdin) {
            sp.setInputStream(System.in, null);
        }
        if (opts.useCover != null) {
            sp.setCover(opts.useCover);
        }
        if (opts.subject != null) {
            sp.setSubject(opts.subject);
        }
        if (opts.comment != null) {
            sp.setComments(opts.comment);
        }
        if (opts.modem != null) {
            sp.setModem(opts.modem);
        }
    }
    
    
    public static void initializePlugins(List<PluginInfo> plugins, boolean noPluginLst) {
        // Load plugins:
        if (!noPluginLst) {
            PluginManager.readPluginList();
        }
        PluginManager.addPlugins(plugins);
        PluginManager.loadAllKnownPlugins();
    }
    
    /**
     * Creates a new YajHFC instance with the given command line parameters.
     * @param opts
     * @return
     */
    public static Lock forkNewInstance(CommandLineOpts opts) {
        try {
            launchLog.info("Forking new instance of YajHFC...");
            
            Lock rv;
            
            List<String> launchArgs = new ArrayList<String>(20);
            launchArgs.add(System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java");
            launchArgs.add("-classpath");
            launchArgs.add(System.getProperty("java.class.path"));
            launchArgs.add(Launcher2.class.getCanonicalName());


            if (opts.adminMode) {
                launchArgs.add("--admin");
            }
            if (Utils.debugMode) {
                launchArgs.add("--debug");
            }
            if (opts.logFile != null) {
                launchArgs.add("--logfile");

                // Create a file name for the new log file
                File log = new File(opts.logFile);
                String name = log.getName();
                String ext;
                int pos = name.lastIndexOf('.');
                if (pos >= 0) {
                    ext = name.substring(pos);
                    name = name.substring(0, pos);
                } else {
                    ext = "";
                }
                launchArgs.add(new File(log.getParentFile(), name + "-background" + ext).getAbsolutePath());
            }
            if (!opts.closeAfterSubmit) {
                launchArgs.add("--noclose");
            }
            if (cmdLineConfDir != null) {
                launchArgs.add("--configdir");
                launchArgs.add(cmdLineConfDir);
            }
            if (opts.selectedTab >= 0) {
                launchArgs.add("--showtab=" + opts.selectedTab);
            }
            if (opts.noPlugins) {
                launchArgs.add("--no-plugins");
            }
            for (PluginInfo entry : opts.plugins) {
                switch (entry.type) {
                case PLUGIN:
                    launchArgs.add("--loadplugin");
                    launchArgs.add(entry.file.getAbsolutePath());
                    break;
                case JDBCDRIVER:
                    launchArgs.add("--loaddriver");
                    launchArgs.add(entry.file.getAbsolutePath());
                    break;
                }
            }
            if (opts.desiredWindowState != CommandLineOpts.WINDOWSTATE_NOCHANGE) {
                char winState;
                switch (opts.desiredWindowState) {
                case CommandLineOpts.WINDOWSTATE_TOTRAY:
                    winState = 't';
                    break;
                case Frame.ICONIFIED:
                    winState = 'i';
                    break;
                case Frame.MAXIMIZED_BOTH:
                    winState = 'm';
                    break;
                case Frame.NORMAL:
                default:
                    winState = 'n';
                    break;
                }
                launchArgs.add("--windowstate="+winState);
            }
            
            if (Utils.debugMode) {
                launchLog.info("Launching new instance:");
                for (int i = 0; i < launchArgs.size(); i++) {
                    launchLog.info("launchArgs[" + i + "] = " + launchArgs.get(i));
                }
            }
            ExternalProcessExecutor.executeProcess(launchArgs);

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

                rv = Lock.checkLock();
            } while (rv == null);

            if (Utils.debugMode) {
                launchLog.info("New instance has been started.");
            }
            return rv;
        } catch (Exception e) {
            if (Utils.debugMode) {
                launchLog.log(Level.WARNING, "Exception launching new instance:", e);
                //e.printStackTrace(Utils.debugOut);
            }
            JOptionPane.showMessageDialog(null, Utils._("Cannot launch new program instance, continuing with the existing one!\nReason: ") + e.toString() );
            return null;
        }
    }
    
    
    private static PrintWriter consoleWriter;
    /**
     * Returns a print writer writing on standard output on the console. <br>
     * On Windows with Java 6 the correct console encoding is used (in contrast to System.out) 
     * @return
     */
    public static PrintWriter getConsoleWriter() {
        if (consoleWriter == null) {
            try {
                // Call System.console().writer() using reflection to avoid problems with Java 5 (we get a MethodNotFoundException which we can catch)
                Method consoleMethod = System.class.getMethod("console");
                Class<?> consoleClass = Class.forName("java.io.Console");
                Method writerMethod = consoleClass.getMethod("writer");
                consoleWriter = (PrintWriter)writerMethod.invoke(consoleMethod.invoke(null));
                if (consoleWriter == null) {
                    consoleWriter = new PrintWriter(System.out);
                }
            } catch (Exception ex) {
                // Java 5 or another problem -> fall back to System.out
                consoleWriter = new PrintWriter(System.out);
            }
        }
        return consoleWriter;
    }
    
    static class NewInstRunner implements Runnable{
        protected final boolean adminMode;
        protected final int selectedTab;
        protected final Runnable loginRunner;
        protected final int desiredWindowState;
        
        public void run() {
            boolean setVisible = true;
            //Utils.initializeUIProperties();
            
            MainWin mainWin = new MainWin(adminMode);
            Launcher2.application = mainWin;
            
            if (desiredWindowState != CommandLineOpts.WINDOWSTATE_NOCHANGE) {
                if (desiredWindowState >= 0) {
                    mainWin.setVisible(true);
                    mainWin.setExtendedState(desiredWindowState);
                } else if (desiredWindowState == CommandLineOpts.WINDOWSTATE_TOTRAY) {
                    setVisible = !mainWin.hasTrayIcon();
                } else {
                    Logger.getLogger(NewInstRunner.class.getName()).warning("Unknown window state: " + desiredWindowState);
                }
            }           
            
            mainWin.setVisible(setVisible);            
            
            mainWin.reconnectToServer(loginRunner);
            if (selectedTab >= 0) {
                mainWin.setSelectedTab(selectedTab);
            }
            YJSplashScreen splash = YJSplashScreen.getSplashScreen();
            if (splash != null && splash.isVisible()) {
                try {
                    splash.close();
                } catch (IllegalStateException e) {
                    // Ignore errors here...
                }
            }
        }   
        
        public NewInstRunner(Runnable loginRunner, boolean adminMode, int selectedTab, int desiredWindowState) {
            this.loginRunner = loginRunner;
            
            this.adminMode = adminMode;
            this.selectedTab = selectedTab;
            this.desiredWindowState = desiredWindowState;
        }
    }
}
