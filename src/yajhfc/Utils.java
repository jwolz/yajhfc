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

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
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
    public static final String AppVersion = "0.3.9c";
    public static final String AuthorEMail = "Jonas Wolz &lt;jwolz@freenet.de&gt;";
    public static final String HomepageURL = "http://yajhfc.berlios.de/"; 
    
    public static boolean debugMode = false;
    //public static PrintStream debugOut = System.out;
    private static final Logger log = Logger.getLogger(Utils.class.getName());
    
    private static FaxOptions theoptions = null;
    private static ResourceBundle msgs = null;
    private static boolean TriedMsgLoad = false;
    private static File configDir = null;
    
    public static final FmtItem0 jobfmt_JobID =
        new FmtItem0("j", _("ID"), _("Job identifier"), Integer.class);
    
    public static final FmtItem0 jobfmt_Owner =
        new FmtItem0("o", _("Owner"),  _("Job owner"));
    
    public static final FmtItem0 jobfmt_Jobstate =
        new FmtItem0("a", _("Job state"), _("Job state (one-character symbol)"), IconMap.class);
    
    public static final FmtItem0 jobfmt_Status =
        new FmtItem0("s", _("Status"), _("Job status information from last failure"));
    
    public static final FmtItem0[] jobfmts =
    { 
            new FmtItem0("A", _("SubAddress"), _("Destination SubAddress")), 
            new FmtItem0("B", _("Password"), _("Destination Password")), 
            new FmtItem0("C", _("Company"), _("Destination company name")), 
            new FmtItem0("D", _("Dials: toal/max."), _("Total # dials/maximum # dials")), 
            new FmtItem0("E", _("Speed"), _("Desired signalling rate"), Integer.class), 
            new FmtItem0("F", _("Tagline format"), _("Client-specific tagline format string")), 
            new FmtItem0("G", _("Desired min-scanline time")), 
            new FmtItem0("H", _("Desired data format")), 
            new FmtItem0("I", _("Priority"), _("Client-specified scheduling priority"), Integer.class), 
            new FmtItem0("J", _("Tag string"), _("Client-specified job tag string")), 
            new FmtItem0("K", _("Use ECM?"), _("Desired use of ECM (one-character symbol)")), 
            new FmtItem0("L", _("Location"), _("Destination geographic location")), 
            new FmtItem0("M", _("Sender e-mail"), _("Notification e-mail address")), 
            new FmtItem0("N", _("Private Tagline?"), _("Desired use of private tagline (one-character symbol)")), 
            new FmtItem0("O", _("Use continuation cover"), _("Whether to use continuation cover page (one-character symbol)")), 
            new FmtItem0("P", _("Pages done/total"), _("# pages transmitted/total # pages to transmit")), 
            new FmtItem0("Q", _("Minimal signalling rate"), _("Client-specified minimum acceptable signalling rate"), Integer.class), 
            new FmtItem0("R", _("Receiver"), _("Destination person (receiver)")), 
            new FmtItem0("S", _("Sender"), _("Sender's identity")), 
            new FmtItem0("T", _("Tries: done/max."), _("Total # tries/maximum # tries")), 
            new FmtItem0("U", _("Page chopping threshold (inches)"), Float.class), 
            new FmtItem0("V", _("Job done operation")), 
            new FmtItem0("W", _("Communication identifier")), 
            new FmtItem0("X", _("Job type"), _("Job type (one-character symbol)")), 
            new FmtItem0("Y", _("Scheduled time"), _("Scheduled date and time"), new HylaDateField("yyyy/MM/dd HH.mm.ss", _("dd/MM/yyyy HH:mm:ss"))), 
            new FmtItem0("Z", _("Scheduled time in seconds since the UNIX epoch")), 
            jobfmt_Jobstate, 
            new FmtItem0("b", _("# consecutive failed tries"), Integer.class), 
            new FmtItem0("c", _("Client machine name")), 
            new FmtItem0("d", _("Total # dials"), Integer.class), 
            new FmtItem0("e", _("Number"), _("Public (external) format of dialstring")), 
            new FmtItem0("f", _("# consecutive failed dials")), 
            new FmtItem0("g", _("Group identifier")), 
            new FmtItem0("h", _("Page chop handling"), _("Page chop handling (one-character symbol)")), 
            new FmtItem0("i", _("Scheduling priority"), _("Current scheduling priority")), 
            jobfmt_JobID, 
            new FmtItem0("k", _("Job kill time")), 
            new FmtItem0("l", _("Page length"), _("Page length in mm"), Integer.class), 
            new FmtItem0("m", _("Modem"), _("Assigned modem")), 
            new FmtItem0("n", _("Notification"), _("E-mail notification handling (one-character symbol)"), IconMap.class), 
            jobfmt_Owner, 
            new FmtItem0("p", _("# pages"), _("# pages transmitted"), Integer.class), 
            new FmtItem0("q", _("Retry time"), _("Job retry time (MM::SS)")/*, new HylaDateField("mm:ss", _("mm:ss"))*/), 
            new FmtItem0("r", _("Resolution"), _("Document resolution in lines/inch"), Integer.class), 
            jobfmt_Status, 
            new FmtItem0("t", _("Tries"), _("Total # tries attempted"), Integer.class), 
            new FmtItem0("u", _("Max. tries"), _("Maximum # tries"), Integer.class), 
            new FmtItem0("v", _("Specified number"), _("Client-specified dialstring")), 
            new FmtItem0("w", _("Page width"), _("Page width in mm"), Integer.class), 
            new FmtItem0("x", _("Maximum # dials"), Integer.class), 
            new FmtItem0("y", _("Pages"), _("Total # pages to transmit"), Integer.class), 
            new FmtItem0("z", _("Time to send job"))
    };
    
    public static final FmtItem0 recvfmt_FileName 
        = new FmtItem0("f", _("Filename"), _("Document filename (relative to the recvq directory)"));
    
    public static final FmtItem0 recvfmt_Owner
        = new FmtItem0("o", _("Owner"), _("File owner"));
    
    public static final FmtItem0 recvfmt_ErrorDesc 
        = new FmtItem0("e", _("Error description"), _("Error description if an error occurred during receive"));
    
    public static final FmtItem0 recvfmt_InProgress 
        = new FmtItem0("z", _("In progress"), _("A ``*'' if receive is going on; otherwise `` '' (space)"), Boolean.class);
    
    public static final FmtItem0 recvfmt_Sender 
        = new FmtItem0("s", _("Sender"), _("Sender identity (TSI)"));
    
    public static final FmtItem0[] recvfmts = {
            new FmtItem0("Y", _("Time/Date"),  _("Extended representation of the time when the receive happened"), new HylaDateField("yyyy:MM:dd HH:mm:ss", _("dd/MM/yyyy HH:mm:ss"))), 
            new FmtItem0("a", _("SubAddress"), _("SubAddress received from sender (if any)")), 
            new FmtItem0("b", _("Speed"), _("Signalling rate used during receive"), Integer.class), 
            new FmtItem0("d", _("Format"), _("Data format used during receive")), 
            recvfmt_ErrorDesc, 
            //new FmtItem("f", "Document filename (relative to the recvq directory)"), 
            recvfmt_FileName,
            new FmtItem0("h", _("Time to receive"), _("Time spent receiving document (HH:MM:SS)"), new HylaDateField("m:ss", _("mm:ss"))), 
            new FmtItem0("i", _("CIDName"), _("CIDName value for received fax")), 
            new FmtItem0("j", _("CIDNumber"), _("CIDNumber value for received fax")), 
            new FmtItem0("l", _("Page length"), _("Page length in mm"), Integer.class), 
            new FmtItem0("m", _("Fax Protection"), _("Fax-style protection mode string (``-rwxrwx'')")), 
            new FmtItem0("n", _("File size"), _("File size (number of bytes)"), Integer.class), 
            recvfmt_Owner, 
            new FmtItem0("p", _("Pages"), _("Number of pages in document"), Integer.class), 
            new FmtItem0("q", _("Protection"), _("UNIX-style protection flags")), 
            new FmtItem0("r", _("Resolution"), _("Resolution of received data"), Integer.class), 
            recvfmt_Sender, 
            new FmtItem0("t", _("Date"), _("Compact representation of the time when the receive happened"), new HylaDateField("ddMMMyy", _("dd/MM/yyyy"))), 
            new FmtItem0("w", _("Page width"), _("Page width in mm"), Integer.class), 
            recvfmt_InProgress
    };
    
    
    public static final FmtItem0[] requiredSendingFmts = {
        jobfmt_JobID,
        jobfmt_Owner,
        jobfmt_Jobstate
    };

    public static final FmtItem0[] requiredSentFmts = requiredSendingFmts;

    public static final FmtItem0[] requiredRecvFmts = {
        recvfmt_FileName,
        recvfmt_Owner,
        recvfmt_InProgress,
        recvfmt_ErrorDesc
    };
    
    // Basic actions when a new fax is detected.
    // The constants should be powers of 2 to make it possible to combine several of them
    public static final int NEWFAX_NOACTION = 0;
    public static final int NEWFAX_BEEP = 1;
    public static final int NEWFAX_TOFRONT = 2;
    public static final int NEWFAX_VIEWER = 4;
    public static final int NEWFAX_MARKASREAD = 8;
    
    
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
            theoptions.loadFromFile(
                    new File(getApplicationDir(), "settings.default"),
                    FaxOptions.getDefaultConfigFile(),
                    new File(getApplicationDir(), "settings.override")
                    );
        }
        return theoptions;
    }
    
    private static Locale myLocale = null;
    public static Locale getLocale() {
        if (myLocale == null) {
            // Need to load locale setting manually to avoid a
            // chicken-egg problem with the initialization of the FmtItem-Arrays above
            Properties prop = new Properties();
            String locale;
            try {
                FileInputStream fIn = new FileInputStream(FaxOptions.getDefaultConfigFile());
                prop.load(fIn);
                fIn.close();
                locale = prop.getProperty("locale", "auto");
            } catch (Exception ex) {
                locale = "auto";
            }
            
            if (locale.equals("auto")) {
                myLocale = Locale.getDefault();
            } else {
                myLocale = new Locale(locale);
            }
        }
        return myLocale;
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
    
    public static MyManualMapObject findInArray(MyManualMapObject[] array, Object key) {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(key)) 
                return array[i];
        return null;
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


