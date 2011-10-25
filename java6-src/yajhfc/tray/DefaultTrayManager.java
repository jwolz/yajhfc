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
 */
package yajhfc.tray;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jonas
 *
 */
public class DefaultTrayManager implements TrayManager {

    private static final Logger log = Logger.getLogger(DefaultTrayManager.class.getName());
    
    public ITrayIcon installTrayIcon(Image image, String tooltip, PopupMenu popup,
            ActionListener clickListener) {
        try {
            DefaultTrayIcon icon = new DefaultTrayIcon(image, tooltip, popup);
            icon.addActionListener(clickListener);
            SystemTray.getSystemTray().add(icon);
            
            return icon;
        } catch (AWTException e) {
            log.log(Level.WARNING, "Error creating a tray icon:", e);
            return null;
        }
    }

    public void removeTrayIcon(ITrayIcon trayIcon) {
        SystemTray.getSystemTray().remove((TrayIcon)trayIcon);
    }

    public Dimension getTrayIconSize() {
        return SystemTray.getSystemTray().getTrayIconSize();
    }



}
