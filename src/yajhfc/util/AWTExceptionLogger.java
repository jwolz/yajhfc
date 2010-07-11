/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * Logs uncaught AWT Exception from the EDT
 * 
 * @author jonas
 *
 */
public class AWTExceptionLogger implements Thread.UncaughtExceptionHandler {
    private static final Logger log = Logger.getLogger(AWTExceptionLogger.class.getName());
    
    public AWTExceptionLogger() {
        super();
    }
    
    public void handle(Throwable throwable) {
        log.log(Level.SEVERE, "An uncaught exception occurred during event dispatching:", throwable);
    }
    
    public void uncaughtException(Thread t, Throwable e) {
        log.log(Level.SEVERE, "An uncaught exception occurred during event dispatching (Thread: " + t + "):", e);
    }
    
    public static void register() {
        System.setProperty("sun.awt.exception.handler", AWTExceptionLogger.class.getName());
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               Thread.currentThread().setUncaughtExceptionHandler(new AWTExceptionLogger());
            } 
        });
    }
}
