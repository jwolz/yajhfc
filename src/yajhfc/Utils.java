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

import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionListener;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import yajhfc.launch.Launcher2;
import yajhfc.macosx.MacOSXSupport;
import yajhfc.model.jobq.QueueFileDateFormat;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;
import yajhfc.util.AWTExceptionLogger;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExternalProcessExecutor;
import yajhfc.util.TransactFileOutputStream;


public final class Utils {
    public static final String AppName = "Yet Another Java HylaFAX Client (YajHFC)";
    public static final String AppShortName = "YajHFC";
    public static final String AppCopyright = "Copyright © 2005-2011 by Jonas Wolz";
    public static final String AppVersion = "0.5.0beta9";
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
     * Returns an executor service which may be used for various non time critical asynchronous computations
     */
    public static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    
    /**
     * True if we run under the Windows platform
     */
    public static final boolean IS_WINDOWS;
    /**
     * True if we run under Mac OS X
     */
    public static final boolean IS_MACOSX;
    private static final boolean buggyLocationByPlatform;
    static {
        final String osname = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osname.contains("windows");
        IS_MACOSX = osname.startsWith("mac os x");

        // Do we have a buggy Java/Windows combination?
        buggyLocationByPlatform = (IS_WINDOWS && (osname.equals("windows 95") || osname.equals("windows 98") || osname.equals("windows me")));
    }

    static final Logger log = Logger.getLogger(Utils.class.getName());
    
    private static FaxOptions theoptions = null;
    private static ResourceBundle msgs = null;
    private static boolean triedMsgLoad = false;
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
                    URI uri = utilURL.toURI();
                    if (IS_WINDOWS && uri.getAuthority() != null) {
                        // Work around a JDK bug with UNC paths
                        uri = new URI("file", null, "////" + uri.getAuthority() + '/' + uri.getPath(), null); 
                    }
                    applicationDir = (new File(uri)).getParentFile();
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
    
    private static File systemwideConfigDir;
    private static boolean systemwideConfigDirSet = false;
    /**
     * Returns the system wide configuration directory (/etc/yajhfc on Unix)
     * if it exists or null otherwise
     * @return
     */
    public static File getSystemwideConfigDir() {
        if (!systemwideConfigDirSet) {
            if (File.separatorChar == '/' && !IS_WINDOWS) { // Probably some flavor of Unix
                File configDir = new File("/etc/yajhfc");
                if (configDir.isDirectory()) {
                    systemwideConfigDir = configDir;
                    log.fine("Found system wide config dir at " + configDir);
                } else {
                    systemwideConfigDir = null;
                    log.fine("Did not find system wide config dir at " + configDir);
                }
            }
            systemwideConfigDirSet = true;
        }
        return systemwideConfigDir;
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
    /**
     * The host's original default locale before we changed it
     */
    public static final Locale DEFAULT_LOCALE = Locale.getDefault();
    
    public static Locale getLocale() {
        return getYajHFCLanguage().getLocale();
    }
   
    private static YajLanguage yajhfcLang;
    public static YajLanguage getYajHFCLanguage() {
        if (yajhfcLang == null) {
            // Need to load locale setting manually to avoid a
            // chicken-egg problem with the initialization of enum descriptions
            Properties prop = getSettingsProperties();
            String locale = prop.getProperty("locale", "auto");
            yajhfcLang = YajLanguage.languageFromLangCode(locale);
            Locale.setDefault(yajhfcLang.getLocale());
        }
        return yajhfcLang;
    }
    
    
    public static File getDefaultConfigFile() {
        return new File(getConfigDir(), "settings");
    }
    
    private static Properties settingsProperties;
    private static Properties settingsPropertiesNoOverride;
    /**
     * Settings that should not be saved.
     * The actually saved values are taken from settingsPropertiesNoOverride
     */
    private static Set<Object> settingsNoSave;
    /**
     * Returns the Properties from which the settings are loaded. This is primarily
     * useful for plugins storing their own settings there. 
     * @return
     */
    public static Properties getSettingsProperties() {
        if (settingsProperties == null) {
            File defaultConfigFile = getDefaultConfigFile();
            if (TransactFileOutputStream.checkRecovery(defaultConfigFile)) {
                File shutdownLog = new File(getConfigDir(), "shutdown.log");
                if (shutdownLog.exists()) {
                    // Save the shutdown log from the last failure
                    shutdownLog.renameTo(new File(getConfigDir(), "shutdown-fail.log"));
                }
            }
            
            /*
             * Load the settings properties from the specified files. The files are loaded in the specified
             * order, i.e. settings from "later" files override the earlier ones
             */
            final File settingsDefault  = new File(getApplicationDir(), "settings.default");
            final File settingsOverride = new File(getApplicationDir(), "settings.override");
            final File[] files;
            if (getSystemwideConfigDir() == null) {
                files = new File[] {
                        settingsDefault,
                        defaultConfigFile,
                        settingsOverride
                };
            } else {
                files = new File[] {
                        settingsDefault,
                        new File(getSystemwideConfigDir(), "settings.default"),
                        defaultConfigFile,
                        settingsOverride,
                        new File(getSystemwideConfigDir(), "settings.override"),
                };
            }

            Properties p = new Properties();
            for (File file : files) {
                if (Utils.debugMode) {
                    log.fine("Loading prefs from " + file);
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
            settingsPropertiesNoOverride = p; // Properties before overrides are applied
            
            if (Launcher2.overrideSettings != null) {
                p = new Properties(p);
                
                if (Utils.debugMode) {
                    log.config("---- Override settings found:");
                    dumpProperties(Launcher2.overrideSettings, log);    
                    log.config("---- End override settings");
                }
                p.putAll(Launcher2.overrideSettings);
                settingsNoSave = Launcher2.overrideSettings.keySet();
            } else {
                settingsNoSave = null;
            }
            if (Utils.debugMode) {
                log.config("---- BEGIN preferences dump");
                Utils.dumpProperties(p, log, "pass", "AdminPassword", "pass-obfuscated", "AdminPassword-obfuscated");
                log.config("---- END preferences dump");
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
            
            if (settingsNoSave != null) {
                // Remove the settings that should not be saved
                for (Object key : settingsNoSave) {
                    String value = settingsPropertiesNoOverride.getProperty((String)key);
                    if (value != null)
                        p.put(key, value);
                }
            }
            try {
                FileOutputStream filout = new TransactFileOutputStream(file, true);
                p.store(filout, Utils.AppShortName + " " + Utils.AppVersion + " configuration file");
                filout.close();
            } catch (Exception e) {
                log.log(Level.WARNING, "Couldn't save file '" + file + "': ", e);
            }
        }
    }
    
    /**
     * Returns the translation of key. If no translation is found, the
     * key is returned.
     * @param key
     * @return
     */
    public static String _(String key) {
        return _(key, key);
    }
    
    /**
     * Returns the translation of key. If no translation is found, the
     * defaultValue is returned.
     * @param key
     * @param defaultValue
     * @return
     */
    public static String _(String key, String defaultValue) {
        if (msgs == null)
            if (triedMsgLoad)
                return defaultValue;
            else {
                loadMessages();
                return _(key, defaultValue);
            }                
        else
            try {
                return msgs.getString(key);
            } catch (Exception e) {
                return defaultValue;
            }
    }
    
    private static void loadMessages() {
        triedMsgLoad = true;
        msgs = getYajHFCLanguage().getMessagesResourceBundle();
        UIManager.getDefaults().addResourceBundle("yajhfc.i18n.UIDefaults");
    }
    
    public static void initializeUIProperties() {
        if (IS_MACOSX) {
            MacOSXSupport.setUIProperties();
        }
        AWTExceptionLogger.register();
        
        setLookAndFeel(getFaxOptions().lookAndFeel);
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
        if (IS_MACOSX) {
        	makeWinAndOwnersVisible(target.getOwner());
        }
    }
    
    public static void makeWinAndOwnersVisible(Window win) {
    	if (win == null)
    		return;
    	
    	if (!win.isVisible())
    		win.setVisible(true);
    	makeWinAndOwnersVisible(win.getOwner());
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
        List<String> resList = fastSplitToList(str, splitChar);
        return resList.toArray(new String[resList.size()]);
    }
    
    /**
     * Splits the String at the locations of splitChar (just like String.split()).
     * This should be much faster than String.split(), however.
     * @param str
     * @param splitChar
     * @return
     */
    public static List<String> fastSplitToList(String str, char splitChar) {
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
        
        return resList;
    }
    
    /**
     * Strips quotes (" or ') at the beginning and end of the specified String
     * @param str
     * @return
     */
    public static String stripQuotes(String str) {
        if (str==null || str.length() < 2)
            return str;
        
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
    public static void dumpProperties(Properties prop, Logger out, Object... censorKeys) {
        List<String> keys = new ArrayList<String>(prop.size());
        Enumeration<?> keyEnum = prop.propertyNames();
        while (keyEnum.hasMoreElements()) {
            keys.add(keyEnum.nextElement().toString());
        }
        Collections.sort(keys);
        
        StringBuilder s = new StringBuilder();
        final String newLine = System.getProperty("line.separator", "\n");
        
        for (String key : keys) {
            //s.setLength(0);
            s.append(key).append('=');
            if (indexOfArray(censorKeys, key) == -1) {
                s.append(prop.getProperty(key));
            } else {
                Object val = prop.getProperty(key);
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
            Object arrayItem = array[i];
            if (arrayItem == element || (element != null && element.equals(arrayItem))) {
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
     * @param input the input string
     * @param forbiddenChars chars to filter out
     * @param replacement char to replace filtered chars with
     * @param maxLen the maximum allowed length of the output or 0 for unlimited length 
     */
    public static String sanitizeInput(String input, String forbiddenChars, char replacement, int maxLen) {
        if (input == null)
            return null;
        if (maxLen > 0 && input.length() > maxLen)
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
        return shortenFileNameForDisplay(file.getPath(), desiredLen);
    }
    
    /**
     * Shortens the name of the given file to the desired length (for display to the user)
     * @param file
     * @param desiredLen
     * @return
     */
    public static String shortenFileNameForDisplay(String fileName, int desiredLen) {
        if (fileName.length() > desiredLen) {
            return fileName.substring(0, 3) + "..." + fileName.substring(fileName.length() - desiredLen + 6); // 6 = 3 + "...".length()
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

    private static String systemViewer = "";
    /**
     * Returns the command line of the default System file viewer or null
     * if it cannot be determined.
     * @return
     */
    public static String getSystemViewerCommandLine() {
        if ("".equals(systemViewer)) {
            if (Utils.IS_WINDOWS) {
                String startCmd = System.getenv("COMSPEC");
                if (startCmd == null) startCmd = "COMMAND";
                startCmd += " /C start \"Viewer\" \"%s\"";

                systemViewer = startCmd;
            } else if (IS_MACOSX) {
                systemViewer = "open \"%s\"";
            } else { // Assume Unix
                String kde = System.getenv("KDE_FULL_SESSION");
                if (kde != null && kde.length() > 0) {
                    systemViewer = "kfmclient exec \"%s\"";
                } else {
                    String gnome = System.getenv("GNOME_DESKTOP_SESSION_ID");
                    if (gnome != null && gnome.length() > 0) {
                        systemViewer = "gnome-open \"%s\"";
                    } else {
                        if (searchExecutableInPath("exo-open") != null) {
                            systemViewer = "exo-open \"%s\"";
                        } else if (searchExecutableInPath("gnome-open") != null) {
                            systemViewer = "gnome-open \"%s\"";
                        } else if (searchExecutableInPath("kfmclient") != null) {
                            systemViewer = "kfmclient exec \"%s\"";
                        } else {
                            systemViewer = null;
                        }
                    }
                }
            }
        }
        return systemViewer;
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
    
    
    public static JLabel addWithLabel(JPanel pane, Component comp, String text, String layout) {
        return addWithLabel(pane, comp, text, new TableLayoutConstraints(layout));
    }
    public static JLabel addWithLabel(JPanel pane, Component comp, String text, TableLayoutConstraints c) { 
        pane.add(comp, c);
        
        JLabel lbl = new JLabel(text);
        lbl.setLabelFor(comp);
        c.row1 = c.row2 = c.row1 - 1;
        c.vAlign = TableLayoutConstants.BOTTOM;
        c.hAlign = TableLayoutConstants.LEFT;
        pane.add(lbl, c); 
        
        return lbl;
    }
    
    public static JLabel addWithLabelHorz(JPanel container, Component comp, String label, String layout) {
        return addWithLabelHorz(container, comp, label, new TableLayoutConstraints(layout));
    }
    public static JLabel addWithLabelHorz(JPanel container, Component comp, String label, TableLayoutConstraints layout) {
        JLabel lbl = new JLabel(label);
        lbl.setLabelFor(comp);
        
        container.add(comp, layout);
        
        layout.col1 = layout.col2 = layout.col1 - 2;
        layout.vAlign = TableLayoutConstraints.CENTER;
        layout.hAlign = TableLayoutConstraints.LEFT;
        container.add(lbl, layout);
        
        return lbl; 
    }
 
    /**
     * Returns the selected file with the file extension appended if the user entered none
     * @param chooser
     * @return
     */
    public static File getSelectedFileFromSaveChooser(JFileChooser chooser) {
        File selectedFile = chooser.getSelectedFile();
        FileFilter ff = chooser.getFileFilter();
        if (ff instanceof ExampleFileFilter) {
            String fileName = selectedFile.getName();
            int idx = fileName.lastIndexOf('.'); // Add the extension if none was specified
            if (idx < 0) {
                selectedFile = new File(selectedFile.getParent(), fileName + '.' + ((ExampleFileFilter)ff).getDefaultExtension());
            }
        }
        return selectedFile;
    }
    
    /**
     * Checks if the string is numeric, i.e. consists only of digits
     * @param s
     * @return
     */
    public static boolean isStringNumeric(String s) {
        for (int i=0; i<s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Creates a popup menu by cloning it from the specified menu. <br>
     * NOTE: At the moment the cloning is incomplete. Extend this method as needed.
     * @param menu
     * @return
     */
    public static JPopupMenu clonePopupFromMenu(JMenu menu) {
        JPopupMenu res = new JPopupMenu();
        
        for (int i=0; i<menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                res.addSeparator();
            } else {
                JMenuItem newItem;
                try {
                    newItem = item.getClass().newInstance();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error creating JMenuItem", e);
                    newItem = new JMenuItem();
                }

                newItem.setText(item.getText());
                newItem.setIcon(item.getIcon());
                newItem.setSelected(item.isSelected());
                newItem.setActionCommand(item.getActionCommand());
                for (ActionListener al : item.getActionListeners()) {
                    newItem.addActionListener(al);
                }

                res.add(newItem);
            }
        }
        return res;
    }
}


