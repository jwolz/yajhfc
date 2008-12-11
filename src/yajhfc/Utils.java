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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.UIManager;


public final class Utils {
    public static final String AppName = "Yet Another Java HylaFAX Client (YajHFC)";
    public static final String AppShortName = "YajHFC";
    public static final String AppCopyright = "Copyright © 2005-2008 by Jonas Wolz";
    public static final String AppVersion = "0.3.9g";
    public static final String AuthorEMail = "Jonas Wolz &lt;jwolz@freenet.de&gt;";
    public static final String HomepageURL = "http://yajhfc.berlios.de/"; 
    
    
    public static boolean debugMode = false;
    //public static PrintStream debugOut = System.out;
    private static final Logger log = Logger.getLogger(Utils.class.getName());
    
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
                applicationDir = (new File(utilURL.getPath())).getParentFile();
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
            settingsProperties = null; // Not needed any more
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
    private static Properties getSettingsProperties() {
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
    
    private static boolean buggyLocationByPlatform;
    static {
        String osname = System.getProperty("os.name");
        // Do we have a buggy Java/Windows combination?
        buggyLocationByPlatform = ((osname.equalsIgnoreCase("Windows 95") || osname.equalsIgnoreCase("Windows 98") || osname.equalsIgnoreCase("Windows ME")));
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
    
    private static class WaitCursorUnsetter extends WindowAdapter {
        private Dialog dlgToSet;
        
        @Override
        public void windowOpened(WindowEvent e) {
           unsetWaitCursor(dlgToSet);
           e.getWindow().removeWindowListener(this);
        }
        
        public WaitCursorUnsetter(Dialog dlgToSet) {
            this.dlgToSet = dlgToSet;
        }
    }
    public static void unsetWaitCursorOnOpen(Dialog dlgToSet, Window target) {
        target.addWindowListener(new WaitCursorUnsetter(dlgToSet));
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
     * Dumps the content of the specified properties object to the Logger
     * @param prop
     * @param out
     */
    public static void dumpProperties(Map<Object, ?> prop, Logger out, Object... censorKeys) {
        Object keys[] = prop.keySet().toArray();
        Arrays.sort(keys);
        StringBuilder s = new StringBuilder();
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
            s.append('\n');
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
        return sanitizeInput(input, "\r\n", ' ');    
    }
    
    /**
     * "Sanitizes" the given input by replacing all characters in forbiddenChars with replacement
     * @param input
     * @param forbiddenChars
     * @param replacement
     */
    public static String sanitizeInput(String input, String forbiddenChars, char replacement) {
        if (input == null)
            return null;
        
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
     * (i.e. returns the index of the given instance, not only of an equal object).
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
        if (System.getProperty("os.name").contains("Windows")) {
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
}


