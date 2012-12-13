/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2012 Jonas Wolz <info@yajhfc.de>
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
package yajhfc;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jonas
 *
 */
public final class PlatformInfo { 
    private static final Logger log = Logger.getLogger(PlatformInfo.class.getName());
    
    /**
     * True if we run under the Windows platform
     */
    public static final boolean IS_WINDOWS;
    /**
     * True if we run under Mac OS X
     */
    public static final boolean IS_MACOSX;
    /**
     * True if we have a Unix-Like-Environment
     */
    public static final boolean IS_UNIX_LIKE; 
    /**
     * True if we run under X11
     */
    public static final boolean IS_X11;    
    
    static final boolean buggyLocationByPlatform;
    
    static {
        final String osname = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = osname.contains("windows");
        IS_MACOSX = osname.startsWith("mac os x");
        
        IS_UNIX_LIKE = IS_MACOSX || (!IS_WINDOWS && File.separatorChar=='/');
        IS_X11 = !IS_WINDOWS && !IS_MACOSX && (System.getProperty("java.awt.graphicsenv", "").contains("X11") || Utils.firstDefined(System.getenv("DISPLAY"), "").length()>0);

        // Do we have a buggy Java/Windows combination?
        buggyLocationByPlatform = (IS_WINDOWS && (osname.equals("windows 95") || osname.equals("windows 98") || osname.equals("windows me")));
        
        if (Utils.debugMode) {
            log.info("IS_WINDOWS=" + IS_WINDOWS + "; IS_MACOSX=" + IS_MACOSX + "; IS_UNIX_LIKE=" + IS_UNIX_LIKE + "; IS_X11=" + IS_X11);
        }
    }
    
    private static String systemViewer = "";

    public static boolean isKDE() {
        return IS_X11 && Utils.firstDefined(System.getenv("KDE_FULL_SESSION"), "").length() > 0;     
    }
    
    public static boolean isGNOME() {
        return IS_X11 && Utils.firstDefined(System.getenv("GNOME_DESKTOP_SESSION_ID"), "").length() > 0;    
    }
    
    /**
     * Returns the GNOME major version or -1 if we do not run GNOME or the version can't be determined
     * @return
     */
    public static int getGNOMEMajorVersion() {
        if (!isGNOME())
            return -1;
        
        try {
            Process p = new ProcessBuilder("gnome-session", "--version").redirectErrorStream(true).start();
            p.getOutputStream().close();
            
            StringBuilder output = new StringBuilder();
            char[] buf = new char[255];
            int c;
            Reader r = new InputStreamReader(p.getInputStream());
            
            while ((c=r.read(buf)) >= 0) {
                output.append(buf, 0, c);
            }
            r.close();

            // gnome-session always seems to return 1 as exit code
//            int exitCode = p.waitFor();
//            if (exitCode != 0)
//                return -1;
            
            String sOutput = output.toString();
            log.fine(sOutput);
            Matcher m = Pattern.compile("^[\\w\\-]+\\s+(\\d+)\\.(\\d+)").matcher(sOutput);
            if (m.find()) {
                int version=Integer.parseInt(m.group(1));
                log.fine("Detected gnome version: " + version);
                return version;
            } else {
                log.info("Cannot parse 'gnome-session --version'-output: " + sOutput);
                return -1;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Cannot determine GNOME version", e);
            return -1;
        } 
    }
    
    /**
     * Returns the command line of the default System file viewer or null
     * if it cannot be determined.
     * @return
     */
    public static String getSystemViewerCommandLine() {
        if ("".equals(systemViewer)) {
            if (IS_WINDOWS) {
                String startCmd = System.getenv("COMSPEC");
                if (startCmd == null) startCmd = "COMMAND";
                startCmd += " /C start \"Viewer\" \"%s\"";
    
                systemViewer = startCmd;
            } else if (IS_MACOSX) {
                systemViewer = "open \"%s\"";
            } else { // Assume Unix
                if (Utils.searchExecutableInPath("xdg-open") != null) {
                    systemViewer = "xdg-open \"%s\"";
                } else {
                    if (isKDE()) {
                        systemViewer = "kfmclient exec \"%s\"";
                    } else {
                        if (isGNOME()) {
                            systemViewer = "gnome-open \"%s\"";
                        } else {
                            if (Utils.searchExecutableInPath("exo-open") != null) {
                                systemViewer = "exo-open \"%s\"";
                            } else if (Utils.searchExecutableInPath("gnome-open") != null) {
                                systemViewer = "gnome-open \"%s\"";
                            } else if (Utils.searchExecutableInPath("kfmclient") != null) {
                                systemViewer = "kfmclient exec \"%s\"";
                            } else {
                                systemViewer = null;
                            }
                        }
                    }
                }
            }
        }
        return systemViewer;
    }

}
