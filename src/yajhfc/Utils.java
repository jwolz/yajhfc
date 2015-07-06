package yajhfc;
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

import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
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
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
import yajhfc.util.ArrayCharSequence;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExternalProcessExecutor;
import yajhfc.util.TransactFileOutputStream;


public final class Utils {
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

    static final Logger log = Logger.getLogger(Utils.class.getName());
    
    private static FaxOptions theoptions = null;
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
                    if (PlatformInfo.IS_WINDOWS && uri.getAuthority() != null) {
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
            if (File.separatorChar == '/' && !PlatformInfo.IS_WINDOWS) { // Probably some flavor of Unix
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

    public static String listToString(Collection<?> l, String delim) {
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
            UIManager.getDefaults().addResourceBundle("yajhfc.i18n.UIDefaults");
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
             * order, i.e. settings from "later" files override the earlier ones.
             * 
             * Load order is:
             * - application-dir/settings.d/*.default
             * - application-dir/settings.default
             * - /etc/yajhfc/settings.d/*.default
             * - /etc/yajhfc/settings.default
             * 
             * - ~/.yajhfc/settings
             * 
             * - application-dir/settings.d/*.override
             * - application-dir/settings.override
             * - /etc/yajhfc/settings.d/*.override
             * - /etc/yajhfc/settings.override
             */
            File appDir = getApplicationDir();
            File[] appAdditionalFiles = listSettingsD(appDir);
            
            File etcDir = getSystemwideConfigDir();
            File[] etcAdditionalFiles = listSettingsD(etcDir);

            final List<File> files = new ArrayList<File>();
            addSettingsD(files, appAdditionalFiles, ".default");
            files.add(new File(appDir, "settings.default"));
            if (etcDir != null) {
                addSettingsD(files, etcAdditionalFiles, ".default");
                files.add(new File(etcDir, "settings.default"));
            }
            
            files.add(defaultConfigFile);
            
            addSettingsD(files, appAdditionalFiles, ".override");
            files.add(new File(appDir, "settings.override"));
            if (etcDir != null) {
                addSettingsD(files, etcAdditionalFiles, ".override");
                files.add(new File(etcDir, "settings.override"));
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
                Utils.dumpProperties(p, log, "pass", "AdminPassword", Pattern.compile(".+-obfuscated$"));
                log.config("---- END preferences dump");
            }
            settingsProperties = p;
        }
        return settingsProperties;
    }
    
    private static File[] listSettingsD(File configDir) {
        
        if (configDir != null) {
            File settingsD = new File(configDir, "settings.d");
            if (settingsD.isDirectory()) {
                File[] result = settingsD.listFiles();
                Arrays.sort(result);
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    private static void addSettingsD(List<File> files, File[] dirList, String suffix) {
        if (dirList != null) {
            for (File f : dirList) {
                if (f.getName().endsWith(suffix)) {
                    files.add(f);
                }
            }
        }
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
                p.store(filout, VersionInfo.AppShortName + " " + VersionInfo.AppVersion + " configuration file");
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
        return getYajHFCLanguage()._(key, key);
    }
    
    /**
     * Returns the translation of key. If no translation is found, the
     * defaultValue is returned.
     * @param key
     * @param defaultValue
     * @return
     */
    public static String _(String key, String defaultValue) {
        return getYajHFCLanguage()._(key, defaultValue);
    }

    
    public static void initializeUIProperties() {
        if (PlatformInfo.IS_MACOSX) {
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
        if (PlatformInfo.buggyLocationByPlatform)
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
        if (PlatformInfo.IS_MACOSX) {
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
     * @param censorKeys Keys not to output. May be of type String or Pattern
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
            if (indexOfPatternArray(censorKeys, key) == -1) {
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
     * Returns the index of the given object in the given array or -1 if it is not found
     * @param array an array of Strings or Pattern
     * @param element the element to search for. 
     * @return
     */
    public static int indexOfPatternArray(Object[] array, Object element) {
        String stringElement = (element == null) ? "" : element.toString();
        
        for (int i = 0; i < array.length; i++) {
            Object arrayItem = array[i];
            
            if (arrayItem instanceof Pattern) {
                if (((Pattern) arrayItem).matcher(stringElement).matches()) {
                    return i;
                }
            } else if (arrayItem == element || (element != null && element.equals(arrayItem))) {
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
     * Copies the file source to target
     * @param source
     * @param target
     * @throws IOException
     */
    public static void copyFile(File source, File target) throws IOException {
        FileInputStream in = new FileInputStream(source);
        FileOutputStream out = new FileOutputStream(target);
        
        FileChannel inChannel = in.getChannel();
        FileChannel outChannel = out.getChannel();
        
        inChannel.transferTo(0, inChannel.size(), outChannel);
        
        in.close();
        out.close();        
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
     * "Sanitizes" the given input by removing all characters in forbiddenChars 
     * @param input the input string
     * @param forbiddenChars chars to filter out
     */
    public static String stringFilterOut(String input, String forbiddenChars) {
        return stringFilterOut(input, forbiddenChars, 0);
    }
    
    /**
     * "Sanitizes" the given input by removing all characters in forbiddenChars 
     * @param input the input string
     * @param forbiddenChars chars to filter out
     * @param maxLen the maximum allowed length of the output or 0 for unlimited length 
     */
    public static String stringFilterOut(String input, String forbiddenChars, int maxLen) {
        if (input == null)
            return null;
        if (maxLen > 0 && input.length() > maxLen)
            input = input.substring(0, maxLen);
        
        char[] chars = input.toCharArray();
        StringBuilder out = new StringBuilder(chars.length);
        boolean changed = false;
        for (char c : chars) {
            if (forbiddenChars.indexOf(c) < 0) {
                // If not a forbidden char, append to output
                out.append(c);
            } else {
                changed = true;
            }
        }
        
        if (changed) {
            return out.toString();
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
     * @param saveFile
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
     * Shortens the given String to the desired length (for display to the user)
     * @param saveFile
     * @param desiredLen
     * @return
     */
    public static String shortenStringForDisplay(String str, int desiredLen) {
        if (str.length() > desiredLen) {
            return str.substring(0, desiredLen-3) + "...";
        } else {
            return str;
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
        if (PlatformInfo.IS_WINDOWS) {
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

    /**
     * Compares if the content of two lists consists of identical objects 
     * (in the sense of ==, not equals())
     * @param list1
     * @param list2
     * @return
     */
    public static boolean listQuickEquals(List<?> list1, List<?> list2) {
        if (list1 == list2)
            return true;
        else if (list1 == null || list2 == null) 
            return false;
    
        if (list1.size() != list2.size()) {
            return false;
        } else {
            for (int i = 0; i < list1.size(); i++) {
                if (list1.get(i) != list2.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Inserts the given item into the specified sorted list at the correct position given its sort order 
     * @param list
     * @param item
     * @param comparator
     * @return the index position the item has been inserted in
     */
    public static <T> int sortedInsert(List<T> list, T item, Comparator<T> comparator) {
        int insertIndex = Collections.binarySearch(list, item, comparator);
        if (insertIndex < 0) // Should always be the case actually
            insertIndex = -(insertIndex + 1);
        
        list.add(insertIndex, item);
        return insertIndex;
    }
    
    /**
     * Sets the icon images for the given Frame.
     * If run on Java 1.5, this sets the image to the first image specified
     * @param frame
     * @param icons
     */
    public static void setIconImages(Frame frame, Image... icons) {
    	try {
			Method setIconImages = Window.class.getMethod("setIconImages", List.class);
			setIconImages.invoke(frame, Arrays.asList(icons));
		} catch (Exception e) {
			log.log(Level.INFO, "Could not use setIconImages, using Frame.setIconImage instead", e);
			frame.setIconImage(icons[0]);
		} 
    	
    }
    
    public static void setDefaultIcons(Frame frame) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        Utils.setIconImages(frame,
                toolkit.getImage(Utils.class.getResource("icon.png")),
                toolkit.getImage(Utils.class.getResource("icon-32x32.png")),              
                toolkit.getImage(Utils.class.getResource("icon-48x48.png")),              
                toolkit.getImage(Utils.class.getResource("icon-64x64.png")),              
                toolkit.getImage(Utils.class.getResource("logo-large.png")));
    }
    
    /**
     * Returns the first value != null or null if all are == null
     */
    public static <T> T firstDefined(T s1, T s2) {
        return (s1 != null) ? s1 : s2;
    }
    
    /**
     * Returns the first value != null or null if all are == null
     */
    public static <T> T firstDefined(T s1, T s2, T s3) {
        if (s1 != null)
            return s1;
        if (s2 != null)
            return s2;
        return s3;
    }
    
    /**
     * Reads all content from the specified Reader and returns it as String
     * @param r
     * @return
     * @throws IOException 
     */
    public static String readFully(Reader r) throws IOException {
        return ArrayCharSequence.readCompletely(r).toString();
    }
    
    /**
     * Deduplicates the specified list
     * @param sortedList
     * @return the number of duplicates removed
     */
    public static <T> int dedupSortedList(List<T> sortedList) {
        Iterator<T> it = sortedList.iterator();
        T lastItem = null;
        int numDups = 0;
        while (it.hasNext()) {
            T item = it.next();
            if (lastItem != null && lastItem.equals(item)) {
                it.remove();
                numDups++;
            }
            lastItem = item;
        }
        return numDups;
    }
}


