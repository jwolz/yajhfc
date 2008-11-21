/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.tray;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.utils;

/**
 * Returns if the system tray is available and returns a tray manager if it is available.
 * 
 * This is used to hide java.awt.SystemTray and friends to keep YajHFC Java 5 compatible while
 * still adding tray support when it is run under Java 6
 * @author jonas
 *
 */
public class TrayFactory {
    private static final Logger log = Logger.getLogger(TrayFactory.class.getName());
    
    private static boolean haveAWTSupport = true;
    
    private static TrayManager trayManager;
    
    private static final String trayManagerClassName = "yajhfc.tray.DefaultTrayManager";
    
    /**
     * Returns if the system tray is available. On Java 5 this will always return false.
     * @return
     */
    public static boolean trayIsAvailable() {
        if (haveAWTSupport) {
            try {
                Class<?> tray = Class.forName("java.awt.SystemTray");
                Method isSupported = tray.getMethod("isSupported");
                Boolean result = (Boolean)isSupported.invoke(null);
                if (utils.debugMode) {
                    log.fine("Tray is available: " + result);
                }
                return result.booleanValue();
            } catch (Exception e) {
                if (utils.debugMode) {
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
}
