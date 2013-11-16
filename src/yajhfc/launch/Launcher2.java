/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc.launch;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import yajhfc.MainWin;
import yajhfc.NoGUISender;
import yajhfc.Utils;
import yajhfc.VersionInfo;
import yajhfc.logconsole.SwingLogHandler;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginManager.PluginInfo;
import yajhfc.send.SendController;
import yajhfc.server.ServerManager;
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
    
    /** 
     * Configuration directory as set on the command line
     *  Needs to be set *before* Utils initializes in order to have an effect!
     */
    public static String cmdLineConfDir = null;
    /** 
     * Settings to override
     *  Needs to be set *before* Utils initializes in order to have an effect!
     */
    public static Properties overrideSettings = null;
    
    public static MainApplicationFrame application;
    public static SwingLogHandler swingLogHandler;
    /**
     * A print writer to print the IDs of sent jobs to
     */
    public static PrintWriter jobIDWriter = null;
    
    /**
     * The main method
     * @param args
     */
    public static void main(String[] args) {
        CommandLineOpts opts = new CommandLineOpts(args);
        
        setupFirstStage(args, opts); // IMPORTANT: Don't access Utils before this line!
        
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

    public static void setupFirstStage(String[] args, CommonCommandLineOpts opts) {
        cmdLineConfDir = opts.configDir;
        if (opts.overrideSettings.length() > 0) {
            overrideSettings = new Properties();
            try {
                ByteArrayInputStream settingsInput = new ByteArrayInputStream(
                        opts.overrideSettings.toString().getBytes("ISO8859-1"));
                overrideSettings.load(settingsInput);
            } catch (Exception ex) {
                launchLog.log(Level.WARNING, "Error loading override settings", ex);
            }
        }
        
        // IMPORTANT: Don't access Utils before this line!
        Utils.debugMode = opts.debugMode;
        
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        swingLogHandler = new SwingLogHandler();
        rootLogger.addHandler(swingLogHandler);
        
        if (opts.debugMode) {
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

            launchLog.config("YajHFC version: " + VersionInfo.AppVersion);
            launchLog.config("---- BEGIN System.getProperties() dump");
            Utils.dumpProperties(System.getProperties(), launchLog);
            launchLog.config("---- END System.getProperties() dump");
            launchLog.config("" + args.length + " command line arguments:");
            for (String arg : args) {
                launchLog.config(arg);
            }
        }
        
        loadPlugins(opts.plugins, opts.noPlugins);
        
        // Set custom system properties:
        Utils.initializeUIProperties();
        
        if (opts.jobIDOutput != null) {
            if ("-".equals(opts.jobIDOutput)) {
                jobIDWriter = getConsoleWriter();
            } else {
                try {
                    jobIDWriter = new PrintWriter(opts.jobIDOutput);
                } catch (Exception e) {
                    launchLog.log(Level.WARNING, "Specified job ID file  \"" + opts.jobIDOutput + "\" could not be opened for writing", e);
                }
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
            
            PluginManager.initializeAllKnownPlugins(PluginManager.STARTUP_MODE_NORMAL);
            
            SendWinSubmitProtocol submitProto;
            if (needSubmitProtocol(opts)) {
                try {
                    submitProto = new SendWinSubmitProtocol();
                    opts.fillSubmitProtocol(submitProto);
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
                    opts.adminMode, opts.selectedTab, opts.desiredWindowState, opts.serverToUse));
            
            Lock.startLockThread();
            if (Utils.debugMode) {
                launchLog.info("Lock and listener created.");
            }
            ShutdownManager.getInstance().registerShutdownHook(new Runnable() {
                public void run() {                      
                    if (Utils.debugMode)
                        System.err.println("Doing shutdown work...");
                    
                    application.saveWindowSettings();
                    
                    ServerManager.getDefault().shutdownCleanup();

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
                    opts.fillSubmitProtocol(oldInst);
                    long[] ids = oldInst.submit(!opts.noWait);
                    if (ids != null) {
                        for (long id : ids) {
                            SendController.printJobIDIfRequested(id);
                        }
                    }
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
        PluginManager.initializeAllKnownPlugins(PluginManager.STARTUP_MODE_NO_GUI);
        
        if (opts.recipients.size() == 0) {
            System.err.println("In no GUI mode you have to specify at least one recipient.");
            System.exit(1);
        }
        if (opts.fileNames.size() == 0 && !opts.useStdin) {
            System.err.println("In no GUI mode you have to specify at least one file to send or --stdin.");
            System.exit(1);
        }
        
        NoGUISender.submitWithoutUI(opts, new SendControllerSubmitProtocol());
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
            PluginManager.initializeAllKnownPlugins(PluginManager.STARTUP_MODE_SEND_ONLY);
            
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
            
            NoGUISender.submitWithoutUI(opts, new SendWinSubmitProtocol());
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
    
    public static void loadPlugins(List<PluginInfo> plugins, boolean noPluginLst) {
        launchLog.fine("Initializing plugins...");
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
            // Pass the user.home property to the new instance
            launchArgs.add("-Duser.home=" + System.getProperty("user.home"));
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
            if (opts.overrideSettings.length() > 0) {
                String[] settings = Utils.fastSplit(opts.overrideSettings.toString(), '\n');
                for (String s : settings) {
                    launchArgs.add("--override-setting");
                    launchArgs.add(s);
                }
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
                    consoleWriter = new PrintWriter(System.out, true);
                }
            } catch (Exception ex) {
                // Java 5 or another problem -> fall back to System.out
                consoleWriter = new PrintWriter(System.out, true);
            }
        }
        return consoleWriter;
    }
    
    static class NewInstRunner implements Runnable {
        protected final String server;
        protected final boolean adminMode;
        protected final int selectedTab;
        protected final Runnable loginRunner;
        protected final int desiredWindowState;
        
        public void run() {
            boolean setVisible = true;
            //Utils.initializeUIProperties();
            
            MainWin mainWin = new MainWin();
            Launcher2.application = mainWin;
            mainWin.initialize(adminMode, server);
            
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
            
            mainWin.reconnectToServer(loginRunner, true);
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
        
        public NewInstRunner(Runnable loginRunner, boolean adminMode, int selectedTab, int desiredWindowState, String server) {
            this.loginRunner = loginRunner;
            
            this.adminMode = adminMode;
            this.selectedTab = selectedTab;
            this.desiredWindowState = desiredWindowState;
            this.server = server;
        }
    }

	/**
	 * Checks if the given system property is true.
	 * Defaults to false if the property is unset
	 * @param propName the property to check
	 * @return
	 */
	public static boolean isPropertyTrue(String propName) {
		return isPropertyTrue(propName, false);
	}
	
	/**
	 * Checks if the given system property is true
	 * @param propName the property to check
	 * @param defValue the default value to return if the property is not set or set to an unparseable value
	 * @return
	 */
	public static boolean isPropertyTrue(String propName, boolean defValue) {
	    String value = System.getProperty(propName);
	    if (value != null) {
	        if ("true".equals(value) || "yes".equals(value))
	            return true;
	        if ("false".equals(value) || "no".equals(value))
	            return false;
	        
	        int i_val;
	        try {
	            i_val = Integer.parseInt(value);
		        return (i_val != 0);
	        } catch (NumberFormatException e) {
	            launchLog.log(Level.WARNING, "Error parsing " + propName, e);
	            return defValue;
	        }
	    } else {
	        return defValue;
	    }
	}
}
