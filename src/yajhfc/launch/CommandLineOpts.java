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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import yajhfc.plugin.PluginType;
import yajhfc.plugin.PluginManager.PluginInfo;
import yajhfc.util.ExampleFileFilter;

/**
 * Class holding all command line options and the code to parse them
 * @author jonas
 *
 */
public class CommandLineOpts extends CommonCommandLineOpts { //IMPORTANT!: Do not use Utils here!
    /**
     * Files to submit. Communicated over socket.
     */
    public final List<String> fileNames = new ArrayList<String>();
    /**
     * Recipients to submit to. Communicated over socket.
     */
    public final List<String> recipients = new ArrayList<String>();
    /**
     * Submit file from stdin. Communicated over socket.
     */
    public boolean useStdin = false;
    /**
     * Use admin mode? Communicated over command line.
     */
    public boolean adminMode = false;
    /**
     * Create a new instance (--background)? Never communicated.
     */
    public boolean forkNewInst = false;
    /**
     * Close after submitting fax? Never communicated.
     */
    public boolean closeAfterSubmit = true;
    /**
     * Start without GUI. Never communicated.
     */
    public boolean noGUI = false;
    /**
     * Tab to show in MainWin. Communicated over command line.
     */
    public int selectedTab = -1;
    /**
     * Use cover? null: Don't change, else use booleanValue()
     * Communicated over socket.
     */
    public Boolean useCover = null;
    /**
     * Subject for fax. Communicated over socket.
     */
    public String subject = null;
    /**
     * Comment for fax. Communicated over socket.
     */
    public String comment = null;
    /**
     * Modem to use. Communicated over socket.
     */
    public String modem = null;
    /**
     * Wait for submit to complete. Communicated over socket.
     */
    public boolean noWait = false;
    public final static int WINDOWSTATE_NOCHANGE = -1;
    public final static int WINDOWSTATE_TOTRAY = -2;
    /**
     * Desired window state. Communicated over command line.
     */
    public int desiredWindowState = WINDOWSTATE_NOCHANGE;
    
    /**
     * The server to use.
     * Communicated over socket.
     */
    public String serverToUse = null;
    
    /**
     * The identity to use.
     * Communicated over socket.
     */
    public String identityToUse = null;
    
    /**
     * Parses the command line arguments and does some initial processing for the --help and --logfile options.
     * @param args
     */
    public void parse(String[] args) {
        // Also modify CommandLineOpts.properties if new options are added
        // ... and do not forget forkNewInstance
        final LongOpt[] longOpts = new LongOpt[] {
                new LongOpt("recipient", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
                new LongOpt("use-cover", LongOpt.OPTIONAL_ARGUMENT, null, 'C'),
                new LongOpt("subject", LongOpt.REQUIRED_ARGUMENT, null, 's'),
                new LongOpt("comment", LongOpt.REQUIRED_ARGUMENT, null, 9),
                new LongOpt("modem", LongOpt.REQUIRED_ARGUMENT, null, 'M'),
                new LongOpt("server", LongOpt.REQUIRED_ARGUMENT, null, 'S'),
                new LongOpt("identity", LongOpt.REQUIRED_ARGUMENT, null, 'I'),
                new LongOpt("stdin", LongOpt.NO_ARGUMENT, null, 1),
                new LongOpt("print-jobids", LongOpt.OPTIONAL_ARGUMENT, null, 13),
                new LongOpt("admin", LongOpt.NO_ARGUMENT, null, 'A'),
                new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
                new LongOpt("logfile", LongOpt.REQUIRED_ARGUMENT, null, 'l'),
                new LongOpt("appendlogfile", LongOpt.REQUIRED_ARGUMENT, null, 6),
                new LongOpt("background", LongOpt.NO_ARGUMENT, null, 2),
                new LongOpt("noclose", LongOpt.NO_ARGUMENT, null, 3),
                new LongOpt("no-wait", LongOpt.NO_ARGUMENT, null, 10),
                new LongOpt("showtab", LongOpt.REQUIRED_ARGUMENT, null, 'T'),
                new LongOpt("windowstate", LongOpt.REQUIRED_ARGUMENT, null, 11),
                new LongOpt("loadplugin", LongOpt.REQUIRED_ARGUMENT, null, 4),
                new LongOpt("loaddriver", LongOpt.REQUIRED_ARGUMENT, null, 5),
                new LongOpt("override-setting", LongOpt.REQUIRED_ARGUMENT, null, 12),
                new LongOpt("no-plugins", LongOpt.NO_ARGUMENT, null, 7),
                new LongOpt("no-gui", LongOpt.NO_ARGUMENT, null, 8),
                new LongOpt("no-check", LongOpt.NO_ARGUMENT, null, -2),
                new LongOpt("configdir", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
                new LongOpt("help", LongOpt.OPTIONAL_ARGUMENT, null, 'h'),
                new LongOpt("Xprint-manpage", LongOpt.NO_ARGUMENT, null, -3),
        };
        final String[] argsWork = args.clone();
        
        Getopt getopt = new Getopt("yajhfc", argsWork, "h::Adc:r:T:l:C::s:M:I:S:", longOpts);
        int opt;
        String optarg;
        while ((opt = getopt.getopt()) != -1) {
            switch (opt) {
            case -3: //Xprint-manpage
                try {
                    new ManPrinter().printManPage(Launcher2.getConsoleWriter(), longOpts);
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                break;
            case -2: // no-check (in general: ignore)
                break;
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
            case 10: // no-wait
                noWait = true;
                forkNewInst = true;
                break;
            case 11: // windowstate
                if (getopt.getOptarg().length() >= 1) {
                    switch (getopt.getOptarg().charAt(0)) {
                    case 'N':
                    case 'n':
                        desiredWindowState = Frame.NORMAL;
                        break;
                    case 'M':
                    case 'm':
                        desiredWindowState = Frame.MAXIMIZED_BOTH;
                        break;
                    case 'I':
                    case 'i':
                        desiredWindowState = Frame.ICONIFIED;
                        break;
                    case 'T':
                    case 't':
                        desiredWindowState = WINDOWSTATE_TOTRAY;
                        break;
                    default:
                        System.err.println("Unknown window state: " + getopt.getOptarg());
                    }
                }
                break;
            case 12: // override-setting
                optarg = getopt.getOptarg();
                for (int i = 0; i < optarg.length(); i++) {
                    char c = optarg.charAt(i);
                    if (c < 128) {
                        overrideSettings.append(c);
                    } else {
                        // Escape non-ASCII chars
                        overrideSettings.append("\\u")
                                        .append(Character.forDigit((c >> 12) & 0xf, 16))
                                        .append(Character.forDigit((c >>  8) & 0xf, 16))
                                        .append(Character.forDigit((c >>  4) & 0xf, 16))
                                        .append(Character.forDigit( c        & 0xf, 16));
                    }
                }
                overrideSettings.append('\n');
                break;
            case 13: //print-jobids
                optarg = getopt.getOptarg();
                if (optarg == null) {
                    jobIDOutput = "-"; //stdout
                } else {
                    jobIDOutput = optarg;
                }
                break;
            case 'C': // use-cover
                optarg = getopt.getOptarg();
                if (optarg == null || optarg.equals("") || Character.toLowerCase(optarg.charAt(0)) == 'y' || optarg.equals("true")) {
                    useCover = Boolean.TRUE;
                } else {
                    useCover = Boolean.FALSE;
                }
                break;
            case 's': // subject
                subject = getopt.getOptarg();
                break;
            case 'h': // help
                new HelpPrinter().printHelp(Launcher2.getConsoleWriter(), longOpts, getopt.getOptarg());
                System.exit(0);
                break;
            case 'A': // admin
                adminMode = true;
                break;
            case 'd': // debug
                debugMode = true;
                break;
            case 'c': // configdir
                configDir = getopt.getOptarg();
                break;
            case 'r': // recipient
                recipients.add(getopt.getOptarg());
                break;
            case 'T': // showtab
                if (getopt.getOptarg().length() >= 1) {
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
                    case '3':
                    case 'A':
                    case 'a':
                        selectedTab = 3;
                        break;
                    default:
                        System.err.println("Unknown tab: " + getopt.getOptarg());
                    }
                    break;
                }
                break;
            case 'M': // modem
                modem = getopt.getOptarg();
                break;
            case 'I': // identity
                identityToUse = getopt.getOptarg();
                break;
            case 'S': // server
                serverToUse = getopt.getOptarg();
                break;
            case '?':
                break;
            default:
                System.err.println("Unknown option \'" + (char)opt + "\' in " + argsWork[getopt.getOptind()]);
                break;
            }
        }
        // Add non-option arguments:
        for (int i=getopt.getOptind(); i<argsWork.length; i++) {
            fileNames.add(argsWork[i]);
        }
        
        
        if (debugMode && ":prompt:".equals(logFile)) {
            logFile = (new LogFilePrompter()).promptForLogfile();
            if (logFile == null) {
                debugMode = false;
            }
        }
    }
    
    public CommandLineOpts() {
    }
    
    public CommandLineOpts(String[] args) {
        parse(args);
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
}
