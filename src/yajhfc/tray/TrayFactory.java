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
package yajhfc.tray;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import yajhfc.PlatformInfo;
import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.util.DoNotAskAgainDialog;

/**
 * Returns if the system tray is available and returns a tray manager if it is available.
 * 
 * This is used to hide java.awt.SystemTray and friends to keep YajHFC Java 5 compatible while
 * still adding tray support when it is run under Java 6
 * @author jonas
 *
 */
public class TrayFactory {
    static final Logger log = Logger.getLogger(TrayFactory.class.getName());
    
    private static boolean haveAWTSupport = true;
    
    private static TrayManager trayManager;
    private static Method isSupported;
    
    private static final String trayManagerClassName = "yajhfc.tray.DefaultTrayManager";
    
    /**
     * Returns if the system tray is available. On Java 5 this will always return false.
     * @return
     */
    public static boolean trayIsAvailable() {
        if (haveAWTSupport) {
            try {
                if (isSupported == null) {
                    Class<?> tray = Class.forName("java.awt.SystemTray");
                    isSupported = tray.getMethod("isSupported");
                }
                Boolean result = (Boolean)isSupported.invoke(null);
                if (Utils.debugMode) {
                    log.fine("Tray is available: " + result);
                }
                return result.booleanValue();
            } catch (Exception e) {
                if (Utils.debugMode) {
                    log.log(Level.INFO, "No tray available.", e);
                }
                haveAWTSupport = false;
                return false;
            } 
        } else {
            return false;
        }
    }
    
    /**
     * Returns the tray manager or null if it is not available.
     * @return
     */
    public static TrayManager getTrayManager() {
        if (trayIsAvailable()) {
            if (trayManager == null) {
                try {
                    Class<?> trayManagerClass = Class.forName(trayManagerClassName);
                    trayManager = (TrayManager)trayManagerClass.newInstance();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Could not create a tray manager.", ex);
                    haveAWTSupport = false;
                    return null;
                }
            }
            return trayManager;
        } else {
            return null;
        }
    }
    
    /**
     * Checks if we have a "problematic" platform with regard to the tray icon and displays a proper error message
     */
    public static void checkForProblematicPlatformAsync() {
        if (PlatformInfo.isGNOME()) {
            log.fine("Running GNOME, check version of it...");
            Utils.executorService.submit(new Runnable() {
                public void run() {
                    if (PlatformInfo.getGNOMEMajorVersion() >= 3) {
                        log.info("We have GNOME >= 3, show a warning.");
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                DoNotAskAgainDialog.showMessageDialog("GNOME3-TrayIcon", Launcher2.application.getFrame(), Utils._("You seem to be running GNOME3:\nThis environment may cause problems when the tray icon is enabled (e.g. after minimizing YajHFC you may not be able to restore the main window again).\nIf you experience such problems, please disable the tray icon (Options->General->Show tray icon)."), "GNOME 3", JOptionPane.INFORMATION_MESSAGE);
                            }
                        });
                    }
                } 
            });
        } else {
            log.fine("Not a known problematic platform.");
        }
    }
}
