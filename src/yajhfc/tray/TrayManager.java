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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;

/**
 * This interface wraps the java.awt.SystemTray method and works
 * as a factory for ITrayIcon objects
 * @author jonas
 *
 */
public interface TrayManager {
    /**
     * Installs a new tray icon and returns the corresponding TrayIcon object
     * @param image
     * @param tooltip
     * @param popup
     * @return
     */
    public ITrayIcon installTrayIcon(Image image, String tooltip, PopupMenu popup, ActionListener clickListener);
    
    /**
     * Returns the size of the tray icon in the system tray
     * @return
     */
    public Dimension getTrayIconSize();
    
    /**
     * Removes the tray icon
     * @param trayIcon
     */
    public void removeTrayIcon(ITrayIcon trayIcon);
}
