package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import yajhfc.model.archive.QueueFileDateFormat;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;
import yajhfc.util.ExternalProcessExecutor;


public final class Utils {
    public static final String AppName = "Yet Another Java HylaFAX Client (YajHFC)";
    public static final String AppShortName = "YajHFC";
    public static final String AppCopyright = "Copyright © 2005-2009 by Jonas Wolz";
    public static final String AppVersion = "0.4.0beta6";
    public static final String AuthorName = "Jonas Wolz";
    public static final String AuthorEMail = "jwolz@freenet.de";
    public static final String HomepageURL = "http://yajhfc.berlios.de/"; 
    
    /**
     * Input format for "long" HylaFax dates
     */
    public static final RegExDateFormat HYLA_LONG_DATE_FORMAT = new RegExDateFormat("(\\d{2,4})[\\.:/](\\d{1,2})[\\.:/](\\d{1,2})\\s+(\\d{1,2})[\\.:/](\\d{1,2})[\\.:/](\\d{1,2})",
            Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND);
    /**
     * Input format for HylaFax duration values (i.e. hour is optional)
     */
    public static final RegExDateFormat HYLA_DURATION_FORMAT = new RegExDateFormat("(?:(\\d{1,2})[\\.:/])?(\\d{1,2})[\\.:/](\\d{1,2})",
            Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND);
    
    /**
     * Input format for time only HylaFax dates (i.e. second is optional)
     */
    public static final RegExDateFormat HYLA_TIME_ONLY_FORMAT = new RegExDateFormat("(\\d{1,2})[\\.:/](\\d{1,2})(?:[\\.:/](\\d{1,2}))?",
            Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND);  
//    /**
//     * Input format for short HylaFax dates
//     */
//    public static final DateFormat HYLA_SHORT_DATE_FORMAT = new SimpleDateFormat("ddMMMyy", Locale.US) {
//        private final String[] weekdays = new DateFormatSymbols(Locale.US).getShortWeekdays();
//        
//        public java.util.Date parse(String text, java.text.ParsePosition pos) {
//            Date result = super.parse(text, pos);
//            if (result == null) {
//                // Recognize Strings of the form "Mon03PM"
//                int startIndex = pos.getIndex();
//                if (text.length() < startIndex + 7)
//                    return null;
//                
//                String weekday = text.substring(startIndex, startIndex+3);
//                String hour = text.substring(startIndex+3, startIndex+5);
//                String am_pm = text.substring(startIndex+5, startIndex+7);
//                
//                calendar.setTimeInMillis(System.currentTimeMillis());
//                int iWeekday = indexOfArray(weekdays, weekday);
//                if (iWeekday >= 0) {
//                    calendar.set(Calendar.DAY_OF_WEEK, iWeekday);
////                    int curWeekday = calendar.get(Calendar.DAY_OF_WEEK);
////                    int weekdayDiff = iWeekday - curWeekday;
////                    if (weekdayDiff > 0) { // The weekdays meant are always in the past
////                        weekdayDiff-=7;
////                    }
////                    calendar.add(Calendar.DAY_OF_MONTH, weekdayDiff);
//                } else {
//                    return null;
//                }
//                
//                try {
//                    int iHour = Integer.parseInt(hour);
//                    calendar.set(Calendar.HOUR, iHour-1);
//                } catch (NumberFormatException e) {
//                    log.log(Level.INFO, "Cannot parse hour: ", e);
//                    return null;
//                }
//                calendar.set(Calendar.MINUTE, 0);
//                calendar.set(Calendar.SECOND, 0);
//                calendar.set(Calendar.MILLISECOND, 0);
//                
//                if (am_pm.equalsIgnoreCase("AM")) {
//                    calendar.set(Calendar.AM_PM, Calendar.AM);
//                } else if (am_pm.equalsIgnoreCase("PM")) {
//                    calendar.set(Calendar.AM_PM, Calendar.PM);
//                }
//                long resultMillis = calendar.getTimeInMillis();
//                if (resultMillis >= System.currentTimeMillis()) {
//                    resultMillis -= (7 * 24 * 3600 * 1000); // If the date is in the future, subtract one week
//                }
//                
//                result = new Date(resultMillis);
//                
//                pos.setIndex(startIndex + 7);
//            }
//            return result;
//        }
//    };
//    
    /**
     * Date format for "milliseconds since epoch" with server-side time zone correction
     */
    public static final DateFormat HYLA_UNIX_DATE_FORMAT = new QueueFileDateFormat(false);
    
    /**
     * Date format for "milliseconds since epoch" with client-side time zone correction
     */
    public static final DateFormat HYLA_UNIX_DATE_FORMAT_GMT = new QueueFileDateFormat(true);
    
    public static boolean debugMode = false;
    
    /**
     * True if we run under the Windows platform
     */
    public static final boolean IS_WINDOWS;
    private static final boolean buggyLocationByPlatform;
    static {
        final String osname = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osname.contains("windows");
        
        // Do we have a buggy Java/Windows combination?
        buggyLocationByPlatform = (IS_WINDOWS && (osname.equals("windows 95") || osname.equals("windows 98") || osname.equals("windows me")));
    }

    static final Logger log = Logger.getLogger(Utils.class.getName());
    
    private static FaxOptions theoptions = null;
    private static ResourceBundle msgs = null;
    private static boolean TriedMsgLoad = false;
    private static File configDir = null;
    
    private static File applicationDir;
    public static File getApplicationDir() {
        if (applicationDir == null) {
            // Try to determine where the JAR file is located
            URL utilURL = Utils.class.getResource("Utils.class");
            try {
                while (utilURL.getProtocol().equals("jar")) {
                    String path = utilURL.getPath();
                    int idx = path.lastIndexOf('!');
                    if (idx >= 0) {
                        path = path.substring(0, idx);
                    }
                    utilURL = new URL(path);
                }
            } catch (MalformedURLException e) {
                log.log(Level.WARNING, "Error determining application dir:", e);
            }
            if (utilURL.getProtocol().equals("file")) {
                try {
                    applicationDir = (new File(utilURL.toURI())).getParentFile();
                } catch (URISyntaxException e) {
                    applicationDir = new File(".");
                    log.log(Level.SEVERE, "Application directory not found, url was: " + Utils.class.getResource("Utils.class"), e);
                }
            } else {
                applicationDir = new File(".");
                log.severe("Application directory not found, url was: " + Utils.class.getResource("Utils.class"));
            }
        }
        return applicationDir;
    }

    public static String listToString(List<?> l, String delim) {
        StringBuilder s = new StringBuilder();
        if (l.size() == 0) {
            return "";
        }
        for (Object o : l) {
            s.append(o).append(delim);
        }
        s.delete(s.length() - delim.length(), s.length());
        return s.toString();
    }    
    
    public static FaxOptions getFaxOptions() {
        if (theoptions == null) {
            theoptions = new FaxOptions();
            theoptions.loadFromProperties(getSettingsProperties());
            //settingsProperties = null;
        }
        return theoptions;
    }
    
    private static Locale myLocale = null;
    public static Locale getLocale() {
        if (myLocale == null) {
            // Need to load locale setting manually to avoid a
            // chicken-egg problem with the initialization of enum descriptions
            Properties prop = getSettingsProperties();
            String locale = prop.getProperty("locale", "auto");
            if (locale.equals("auto")) {
                myLocale = Locale.getDefault();
            } else {
                myLocale = new Locale(locale);
            }
        }
        return myLocale;
    }
    
    public static File getDefaultConfigFile() {
        return new File(getConfigDir(), "settings");
    }
    
    private static Properties settingsProperties;
    /**
     * Returns the Properties from which the settings are loaded. This is primarily
     * useful for plugins storing their own settings there. 
     * @return
     */
    public static Properties getSettingsProperties() {
        if (settingsProperties == null) {
            /*
             * Load the settings properties from the specified files. The files are loaded in the specified
             * order, i.e. settings from "later" files override the earlier ones
             */
            final File[] files = {
                    new File(getApplicationDir(), "settings.default"),
                    getDefaultConfigFile(),
                    new File(getApplicationDir(), "settings.override")
            };

            Properties p = new Properties();
            for (File file : files) {
                if (Utils.debugMode) {
                    log.info("Loading prefs from " + file);
                }
                try {
                    if (file.exists()) {
                        FileInputStream filin = new FileInputStream(file);
                        p.load(filin);
                        filin.close();
                    } else {
                        if (Utils.debugMode) {
                            log.info(file + " not found");
                        }
                    }
                } catch (FileNotFoundException e) {
                    if (Utils.debugMode) {
                        log.log(Level.INFO, file + " not found", e);
                    }
                    continue; // No file yet
                } catch (IOException e) {
                    log.log(Level.WARNING, "Error reading file '" + file + "': " , e);
                    continue;
                }
            }
            settingsProperties = p;
        }
        return settingsProperties;
    }
    
    public static void storeOptionsToFile() {
        storeOptionsToFile(getDefaultConfigFile());
    }
    
    public static void storeOptionsToFile(File file) {
        if (theoptions != null) {
            Properties p = new Properties();
            for (PluginUI puc : PluginManager.pluginUIs) {
                puc.saveOptions(p);
            }
            theoptions.storeToProperties(p);
            
            try {
                FileOutputStream filout = new FileOutputStream(file);
                p.store(filout, Utils.AppShortName + " " + Utils.AppVersion + " configuration file");
                filout.close();
            } catch (Exception e) {
                log.log(Level.WARNING, "Couldn't save file '" + file + "': ", e);
            }
        }
    }
    
    public static String _(String key) {
        if (msgs == null)
            if (TriedMsgLoad)
                return key;
            else {
                LoadMessages();
                return _(key);
            }                
        else
            try {
                return msgs.getString(key);
            } catch (Exception e) {
                return key;
            }
    }
    
    private static void LoadMessages() {
        TriedMsgLoad = true;
        
        // Use special handling for english locale as we don't use
        // a ResourceBundle for it
        if (getLocale().equals(Locale.ENGLISH)) {
            msgs = null;
        } else {
            try {
                msgs = ResourceBundle.getBundle("yajhfc.i18n.Messages", getLocale());
            } catch (Exception e) {
                msgs = null;
            }
        }
    }
    
    public static String minutesToHylaTime(int mins) {
        String result;
        DecimalFormat fmt = new DecimalFormat("00");
        result = fmt.format(mins % 60);
        mins /= 60;
        result = fmt.format(mins % 24) + result;
        mins /= 24;
        result = fmt.format(mins) + result;
        return result;
    }
 
//    public static void addUniqueToVec(Vector<FmtItem> vec, FmtItem[] arr) {
//        for (int i = 0; i < arr.length; i++)
//            if (!vec.contains(arr[i]))
//                vec.add(arr[i]);
//    }

    public static ImageIcon loadIcon(String name) {
       return loadGeneralIcon("/toolbarButtonGraphics/" + name + "16.gif");
    }
    
    public static ImageIcon loadCustomIcon(String name) {
        return loadGeneralIcon("/yajhfc/images/" + name);
     }
    
    public static ImageIcon loadGeneralIcon(String name) {
        URL imgURL = Utils.class.getResource(name);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    }
    
    public static File getConfigDir() {
        if (configDir == null) {
            if (Launcher2.cmdLineConfDir == null) {
                configDir = new File(System.getProperty("user.home"), ".yajhfc");
            } else {
                configDir = new File(Launcher2.cmdLineConfDir);
            }

            if (!configDir.exists()) {
                configDir.mkdir();
            }
        }
        return configDir;
    }
    
    /***
     * Escapes all forbiddenChars using escapeChar + a letter between A and Z. 
     * Therefore forbiddenChars should be shorter than 26 characters. 
     * escapeChar should not be contained in forbiddenChar and not a letter between A and Z.
     * @param input
     * @param forbiddenChars
     * @param escapeChar
     * @return
     */
    public static String escapeChars(String input, String forbiddenChars, char escapeChar) {
        StringBuilder sBuf = new StringBuilder(input.length() * 2);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == escapeChar) {
                sBuf.append(escapeChar).append(escapeChar);
            } else {
                int idx = forbiddenChars.indexOf(c);
                if (idx >= 0) {
                    sBuf.append(escapeChar);
                    sBuf.append((char)('A' + idx));
                } else {
                    sBuf.append(c);
                }
            } 
        }
        return sBuf.toString();
    }
    
    public static String unEscapeChars(String input, String forbiddenChars, char escapeChar) {
        StringBuilder sBuf = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == escapeChar) {
                c = input.charAt(++i);
                if (c == escapeChar) 
                    sBuf.append(escapeChar);
                else
                    sBuf.append(forbiddenChars.charAt(c - 'A'));
            } else {
                sBuf.append(c);
            } 
        }
        return sBuf.toString();
    }
    
    public static void setDefWinPos(Window win) {
        // Do we have a buggy Java/Windows combination?
        if (buggyLocationByPlatform)
            win.setLocationRelativeTo(null);
            //win.setLocation(0,0);
        else
            win.setLocationByPlatform(true);
    }
    
    public static boolean setLookAndFeel(String className) {
        try {
            String lfClass;
            if (className.equals(FaxOptions.LOOKANDFEEL_SYSTEM)) {
                lfClass = UIManager.getSystemLookAndFeelClassName();
            } else if (className.equals(FaxOptions.LOOKANDFEEL_CROSSPLATFORM)) {
                lfClass = UIManager.getCrossPlatformLookAndFeelClassName();
            } else {
                lfClass = className;
            }
            UIManager.setLookAndFeel(lfClass);
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Couldn't load look&feel: " + className + ": ", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                log.log(Level.WARNING, "Couldn't load native look&feel: ", e1);
            }
            return false;
        }
    }
    
    public static URL getLocalizedFile(String path) {
        return getLocalizedFile(path, true);
    }
    public static URL getLocalizedFile(String path, boolean intlVersionValid) {
        String prefix;
        String suffix;
        Locale loc = Utils.getLocale();
        int pos = path.lastIndexOf('.');
        
        if (pos < 0) {
            prefix = path;
            suffix = "";
        } else {
            prefix = path.substring(0, pos);
            suffix = path.substring(pos);
        }
        
        String[] tryList = {
                prefix + "_" + loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant() + suffix,
                prefix + "_" + loc.getLanguage() + "_" + loc.getCountry() + suffix,
                prefix + "_" + loc.getLanguage() + suffix
        };
        URL lURL = null;
        for (int i = 0; i < tryList.length; i++) {
            lURL = Utils.class.getResource(tryList[i]);
            if (lURL != null)
                return lURL;
        }
        if (intlVersionValid)
            return Utils.class.getResource(path);
        else
            return null;
    }
    
    public static void unsetWaitCursorOnOpen(final Dialog dlgToSet, final Window target) {
        target.addWindowListener(new WindowAdapter() {
            //private final long creationTime = System.currentTimeMillis();
            
            @Override
            public void windowOpened(WindowEvent e) {
                //System.out.println("Time for showing: " + (System.currentTimeMillis() - creationTime));
                unsetWaitCursor(dlgToSet);
                target.removeWindowListener(this);
            }
        });
    }
    
    public static void setWaitCursor(Dialog dlgToSet) {
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        if (dlgToSet != null)
            dlgToSet.setCursor(waitCursor);
        for (Frame f : Frame.getFrames()) {
            f.setCursor(waitCursor);
        }
    }
    
    public static void unsetWaitCursor(Dialog dlgToSet) {
        Cursor defCursor = Cursor.getDefaultCursor();
        if (dlgToSet != null)
            dlgToSet.setCursor(defCursor);
        for (Frame f : Frame.getFrames()) {
            f.setCursor(defCursor);
        }
    }
    
    /**
     * Sets the enabled state for all children of the given Container (but not the container itself)
     * @param container
     * @param enable
     */
    public static void enableChildren(Container container, boolean enable) {
        for (Component comp : container.getComponents()) {
            comp.setEnabled(enable);
            if (comp instanceof Container) {
                enableChildren((Container)comp, enable);
            }
        }
    }
    
    /**
     * Splits the String at the locations of splitChar (just like String.split()).
     * This should be much faster than String.split(), however.
     * @param str
     * @param splitChar
     * @return
     */
    public static String[] fastSplit(String str, char splitChar) {
        ArrayList<String> resList = new ArrayList<String>();
        
        int pos = 0;
        int charPos = str.indexOf(splitChar);        
        while (charPos > -1) {
            resList.add(str.substring(pos, charPos));
            pos = charPos + 1;
            charPos = str.indexOf(splitChar, pos);
        }
        // Do not include a trailing empty String
        if (pos < str.length()) {
            resList.add(str.substring(pos));
        }
        
        return resList.toArray(new String[resList.size()]);
    }
    
    
    /**
     * Strips quotes (" or ') at the beginning and end of the specified String
     * @param str
     * @return
     */
    public static String stripQuotes(String str)
    {
        final char f = str.charAt(0);
        final char l = str.charAt(str.length()-1);
        if (f == l && (f == '\"' || f == '\'')) {
            return str.substring(1, str.length()-1);
        } else {
            return str;
        }
    }
    
    /**
     * Dumps the content of the specified properties object to the Logger
     * @param prop
     * @param out
     */
    public static void dumpProperties(Map<Object, ?> prop, Logger out, Object... censorKeys) {
        Object keys[] = prop.keySet().toArray();
        Arrays.sort(keys);
        StringBuilder s = new StringBuilder();
        final String newLine = System.getProperty("line.separator", "\n");
        
        for (Object key : keys) {
            //s.setLength(0);
            s.append(key).append('=');
            if (indexOfArray(censorKeys, key) == -1) {
                s.append(prop.get(key));
            } else {
                Object val = prop.get(key);
                if (val == null) {
                    s.append("null");
                } else {
                    s.append('<').append(val.toString().length()).append(" characters>");
                }
            }
            //out.println(s);
            s.append(newLine);
        }
        out.config(s.toString());
    }
    
    /**
     * Returns the index of the given object in the given array or -1 if it is not found
     * @param array
     * @param element
     * @return
     */
    public static int indexOfArray(Object[] array, Object element) {
        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }
 
    /**
     * Copies the content from inStream into outStream
     * @param inStream
     * @param outStream
     * @throws IOException 
     */
    public static void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
        int len = 0;
        final byte[] buf = new byte[8000];
        while ((len = inStream.read(buf)) >= 0) {
            outStream.write(buf, 0, len);
        }
    }
    
    /**
     * "Sanitizes" the given input by replacing any new line characters with spaces.
     * @param input
     * @param forbiddenChars
     * @param replacement
     */
    public static String sanitizeInput(String input) {
        return sanitizeInput(input, "\r\n", ' ', 255);    
    }
    
    /**
     * "Sanitizes" the given input by replacing all characters in forbiddenChars with replacement
     * @param input
     * @param forbiddenChars
     * @param replacement
     */
    public static String sanitizeInput(String input, String forbiddenChars, char replacement, int maxLen) {
        if (input == null)
            return null;
        if (input.length() > maxLen)
            input = input.substring(0, maxLen);
        
        char[] chars = input.toCharArray();
        boolean changed = false;
        for (int i = 0; i < chars.length; i++) {
            if (forbiddenChars.indexOf(chars[i]) >= 0) {
                chars[i] = replacement;
                changed = true;
            }
        }
        
        if (changed) {
            return new String(chars);
        } else {
            return input;
        }
    }
    
    /**
     * Returns the index of the element in the List that is == the given obj 
     * (i.e. returns the index of the given instance, not only that of an equal object).
     * @param list
     * @param obj
     * @return
     */
    public static int identityIndexOf(List<?> list, Object obj) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == obj) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Shortens the name of the given file to the desired length (for display to the user)
     * @param file
     * @param desiredLen
     * @return
     */
    public static String shortenFileNameForDisplay(File file, int desiredLen) {
        return shortenFileNameForDisplay(file.getAbsolutePath(), desiredLen);
    }
    
    /**
     * Shortens the name of the given file to the desired length (for display to the user)
     * @param file
     * @param desiredLen
     * @return
     */
    public static String shortenFileNameForDisplay(String fileName, int desiredLen) {
        if (fileName.length() > desiredLen) {
            return "..." + fileName.substring(fileName.length() - desiredLen + 3);
        } else {
            return fileName;
        }
    }
    
    /**
     * Extracts the executable name from the given command line
     * @param cmdLine
     * @return
     */
    public static String extractExecutableFromCmdLine(String cmdLine) {
        cmdLine = cmdLine.trim();
        if (cmdLine.startsWith("\"")) {
            int quotePos = cmdLine.indexOf('\"', 1);
            if (quotePos > 0) {
                return cmdLine.substring(1, quotePos);
            }
        }
        
        int spacePos = cmdLine.indexOf(' ');
        if (spacePos > 0) {
            return cmdLine.substring(0, spacePos);
        } else {
            return cmdLine;
        }
    }
    
    /**
     * Searches for the given executable in the system path. Returns a File pointing
     * to it if it is found or null if it can not be found.
     * @param exeToFind
     * @return
     */
    public static File searchExecutableInPath(String exeToFind) {
        String path = System.getenv("PATH");
        String[] pathComps;
        if (path != null) {
            pathComps = fastSplit(path, File.pathSeparatorChar);
        } else {
            pathComps = null;
        }
        
        File exe = searchInPath(pathComps, exeToFind);
        if (exe != null) {
            return exe;
        }
        
        // For Windows, try to append .exe, .com, ...
        if (IS_WINDOWS) {
            String exts = System.getenv("PATHEXT");
            String[] appendExts;
            if (exts != null) {
                appendExts = fastSplit(exts, File.pathSeparatorChar);
            } else {
                appendExts = new String[] { ".exe", ".com", ".bat", ".pif" };
            }
            for (String ext : appendExts) {
                exe = searchInPath(pathComps, exeToFind + ext);
                if (exe != null) {
                    return exe;
                }
            }
        }
        
        return null;
    }
    private static File searchInPath(String[] pathComps, String exeName) {
        File exe = new File(exeName);
        if (exe.exists()) {
            return exe;
        } else if (pathComps != null) {
            for (String dir : pathComps) {
                exe = new File(dir, exeName);
                if (exe.exists()) {
                    return exe;
                } 
            }
        }
        return null;
    }

    /**
     * Returns the command line of the default System file viewer or null
     * if it cannot be determined.
     * @return
     */
    public static String getSystemViewerCommandLine() {
        if (Utils.IS_WINDOWS) {
            String startCmd = System.getenv("COMSPEC");
            if (startCmd == null) startCmd = "COMMAND";
            startCmd += " /C start \"Viewer\" \"%s\"";

            return startCmd;
        } else if (System.getProperty("os.name").startsWith("Mac OS X")) {
            return "open \"%s\"";
        } else {
            if ("true".equals(System.getenv("KDE_FULL_SESSION"))) {
                return "kfmclient exec \"%s\"";
            } else {
                String gnome = System.getenv("GNOME_DESKTOP_SESSION_ID");
                if (gnome != null && gnome.length() > 0) {
                    return "gnome-open \"%s\"";
                } else {
                    return null;
                }
            }
        }
    }
    
    public static void startViewer(String viewerCommandLine, URI uri) throws IOException {
        startViewer(viewerCommandLine, uri.toString());
    }
    public static void startViewer(String viewerCommandLine, File file) throws IOException {
        startViewer(viewerCommandLine, file.getAbsolutePath());
    }
    private static void startViewer(String viewerCommandLine, String fileParam) throws IOException {
        if (viewerCommandLine.indexOf("%s") >= 0)
            viewerCommandLine = viewerCommandLine.replace("%s", fileParam);
        else
            viewerCommandLine += " \"" + fileParam + "\"";

        ExternalProcessExecutor.executeProcess(viewerCommandLine);
    }
}


