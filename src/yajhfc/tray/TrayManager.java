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

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;

/**
 * @author jonas
 *
 */
public interface TrayManager {
    
    public static final int MSGTYPE_NONE = 0;
    public static final int MSGTYPE_INFO = 1;
    public static final int MSGTYPE_WARNING = 2;
    public static final int MSGTYPE_ERROR = 3;
    
    /**
     * Installs a new tray icon and returns the corresponding TrayIcon object
     * @param image
     * @param tooltip
     * @param popup
     * @return
     */
    public Object installTrayIcon(Image image, String tooltip, PopupMenu popup, ActionListener clickListener);
    
    /**
     * Updates the tray icon's tooltip
     * @param trayIcon
     * @param newTooltip
     */
    public void updateTooltip(Object trayIcon, String newTooltip);
    
    /**
     * Updates the tray icon
     * @param trayIcon
     * @param newIcon
     */
    public void updateIcon(Object trayIcon, Image newIcon);
    
    /**
     * Updates the tray icon and its tooltip
     * @param trayIcon
     * @param newTooltip
     */
    public void updateTooltipAndIcon(Object trayIcon, Image newIcon, String newTooltip);
    
    
    /**
     * Displays a message in the system tray.
     * @param trayIcon
     * @param caption
     * @param message
     * @param messageType
     */
    public void displayMessage(Object trayIcon, String caption, String message, int messageType);
    
    /**
     * Removes the tray icon
     * @param trayIcon
     */
    public void removeTrayIcon(Object trayIcon);
}
