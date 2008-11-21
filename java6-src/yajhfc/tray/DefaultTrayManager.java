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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jonas
 *
 */
public class DefaultTrayManager implements TrayManager {

    private static final Logger log = Logger.getLogger(DefaultTrayManager.class.getName());
    
    public Object installTrayIcon(Image image, String tooltip, PopupMenu popup,
            ActionListener clickListener) {
        try {
            TrayIcon icon = new TrayIcon(image, tooltip, popup);
            icon.addActionListener(clickListener);
            icon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(icon);
            
            return icon;
        } catch (AWTException e) {
            log.log(Level.WARNING, "Error creating a tray icon:", e);
            return null;
        }
    }

    public void removeTrayIcon(Object trayIcon) {
        SystemTray.getSystemTray().remove((TrayIcon)trayIcon);
    }

    public void updateIcon(Object trayIcon, Image newIcon) {
        TrayIcon icon = (TrayIcon)trayIcon;
        icon.setImage(newIcon);
    }

    public void updateTooltip(Object trayIcon, String newTooltip) {
        TrayIcon icon = (TrayIcon)trayIcon;
        icon.setToolTip(newTooltip);
    }

    public void updateTooltipAndIcon(Object trayIcon, Image newIcon,
            String newTooltip) {
        TrayIcon icon = (TrayIcon)trayIcon;
        icon.setImage(newIcon);
        icon.setToolTip(newTooltip);
    }

    public void displayMessage(Object trayIcon, String caption, String message,
            int messageType) {
        TrayIcon icon = (TrayIcon)trayIcon;
        
        MessageType msgType;
        switch (messageType) {
        case MSGTYPE_INFO:
            msgType = MessageType.INFO;
            break;
        case MSGTYPE_WARNING:
            msgType = MessageType.WARNING;
            break;
        case MSGTYPE_ERROR:
            msgType = MessageType.ERROR;
            break;
        case MSGTYPE_NONE:
        default:
            msgType = MessageType.NONE;
            break;
        }
        
        icon.displayMessage(caption, message, msgType);
    }

}
