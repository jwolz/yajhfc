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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Timer;

import yajhfc.FaxOptions;
import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.VersionInfo;
import yajhfc.model.RecvFormat;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.table.ReadStateFaxListTableModel;
import yajhfc.model.table.UnreadItemEvent;
import yajhfc.model.table.UnreadItemListener;
import yajhfc.server.Server;
import yajhfc.server.ServerManager;
import yajhfc.util.ExcDialogAbstractAction;

/**
 * The default YajHFC tray icon.
 * @author jonas
 *
 */
public class YajHFCTrayIcon implements UnreadItemListener<RecvFormat>, WindowListener {

    private static final int BLINK_INTERVAL = 500;
    private static final long MINIMIZE_THRESHOLD = 2000;
    
    ITrayIcon trayIcon = null;
    MainWin mainw;
    ShowAction showAction;
    boolean connected = false;
    boolean minimizeToTray = false;
    ReadStateFaxListTableModel<RecvFormat> recvModel;
    
    Image faxIcon, emptyImage;
    Timer blinkTimer;
    long blinkStartTime = -1;
    
    long lastShowTime = -1;
    
    /**
     * Creates a new tray icon if it is available and configures the main window
     * to minimize to tray.
     * @param mainw
     * @param recvModel
     * @param actions the actions to add to the popup menu. Specify null to include a separator
     */
    public YajHFCTrayIcon(MainWin mainw, ReadStateFaxListTableModel<RecvFormat> recvModel, Action... actions) {
        this.mainw = mainw;
        this.recvModel = recvModel;
        
        try {
            if (TrayFactory.trayIsAvailable()) {
                showAction = new ShowAction();

                PopupMenu popup = new PopupMenu();
                popup.add(createMenuItemForAction(showAction));
                popup.addSeparator();
                for (Action act : actions) {
                    if (act == null) {
                        popup.addSeparator();
                    } else {
                        popup.add(createMenuItemForAction(act));
                    }
                }
                TrayManager manager = TrayFactory.getTrayManager();
                if (manager == null) {
                	Logger.getLogger(YajHFCTrayIcon.class.getName()).log(Level.WARNING, "Could not get a tray manager!");
                	trayIcon = null;
                	return;
                }
                Dimension traySize = manager.getTrayIconSize();
                faxIcon = Toolkit.getDefaultToolkit().getImage(YajHFCTrayIcon.class.getResource("/yajhfc/logo-large.png")).getScaledInstance(traySize.width, traySize.height, Image.SCALE_SMOOTH);
                emptyImage = new BufferedImage(traySize.width, traySize.height, BufferedImage.TRANSLUCENT);
                
                trayIcon = manager.installTrayIcon(faxIcon, VersionInfo.AppShortName, popup, showAction);
                trayIcon.addMouseListener(showAction);
                
                recvModel.addUnreadItemListener(this);
                mainw.addWindowListener(this);

                updateTooltip();
            }
        } catch (Exception ex) {
            Logger.getLogger(YajHFCTrayIcon.class.getName()).log(Level.SEVERE, "Error creating tray icon:", ex);
            trayIcon = null;
        }
    }
    
    public boolean isValid() {
    	return (trayIcon != null);
    }
    
    private MenuItem createMenuItemForAction(Action a) {
        MenuItem rv = new MenuItem((String)a.getValue(Action.NAME));
        rv.setEnabled(a.isEnabled());
        rv.addActionListener(a);
        a.addPropertyChangeListener(new MenuItemActionPropChangeListener(a, rv));
        
        return rv;
    }
    
    public void setConnectedState(boolean connected) {
        this.connected = connected;
        updateTooltip();
    }   

    public void newItemsAvailable(UnreadItemEvent<RecvFormat> evt) {
        if (trayIcon != null && !evt.isOldDataNull()) {
            if (Utils.getFaxOptions().newFaxTrayNotification) {
                StringBuffer msg = new StringBuffer();
                new MessageFormat(Utils._("{0} fax(es) received ({1} unread fax(es)):")).format(new Object[] { evt.getItems().size(), recvModel.getNumberOfUnreadFaxes()}, msg, null);
                int senderIdx = recvModel.getColumns().getCompleteView().indexOf(RecvFormat.s);
                if (senderIdx >= 0) {
                    for (FaxJob<RecvFormat> job : evt.getItems()) {
                        msg.append('\n');
                        msg.append(job.getData(senderIdx));
                    }
                }

                trayIcon.displayMessage(Utils._("New fax received"), msg.toString(), ITrayIcon.MSGTYPE_INFO);
            }
            updateTooltip();
            if ((Utils.getFaxOptions().newFaxAction & FaxOptions.NEWFAX_BLINKTRAYICON) != 0) {
                startBlinking();
            }
        }
    }
    
    private void updateTooltip() {
        if (trayIcon != null) {
            int numUnread = recvModel.getNumberOfUnreadFaxes();
            String text;
            if (connected) {
                StringBuffer textBuf = new StringBuffer();
                final Server currentServer = ServerManager.getDefault().getCurrent();
                if (currentServer != null) {
                    String userName = (currentServer.isConnected()) ? currentServer.getOptions().user : currentServer.getClientManager().getUser();
                    textBuf.append(userName).append('@').append(currentServer.getOptions().host);
                } else {
                    textBuf.append("<no server>");
                }
                textBuf.append(" - ");
                new MessageFormat(Utils._("{0} unread fax(es)")).format(new Object[] {numUnread}, textBuf, null);
                text = textBuf.toString();
            } else {
                text = Utils._("Disconnected");
            }           

            trayIcon.setToolTip(text);
        }
    }
    
    private void startBlinking() {
        if (trayIcon != null) {
            if (blinkTimer == null) {
                blinkTimer = new Timer(BLINK_INTERVAL, new ActionListener() {
                    private boolean blinkState = false;

                    public void actionPerformed(ActionEvent e) {
                        trayIcon.setImage(blinkState ? faxIcon : emptyImage);
                        blinkState = !blinkState;
                    }

                });
                blinkTimer.start();
            }
            blinkStartTime = System.currentTimeMillis();
        }
    }
    
    private void stopBlinking() {
        if (trayIcon != null) {
            if (blinkTimer != null && (blinkStartTime > 0) && (System.currentTimeMillis() - blinkStartTime) > BLINK_INTERVAL) {
                blinkTimer.stop();
                blinkTimer = null;
                trayIcon.setImage(faxIcon);
                blinkStartTime = -1;
            }
        }
    }

    public void setMinimizeToTray(boolean minimizeToTray) {
        this.minimizeToTray = minimizeToTray;
    }
    
    public boolean isMinimizeToTray() {
        return minimizeToTray;
    }
    
    public void windowIconified(WindowEvent e) {
        if (System.currentTimeMillis() < lastShowTime+MINIMIZE_THRESHOLD)
            return; // Ignore the minimize event if we have recently been displayed
        
        if (minimizeToTray && trayIcon != null) {
            mainw.setVisible(false);
        }
    }
    
    public void windowClosed(WindowEvent e) {
        dispose();
    }

    public void windowClosing(WindowEvent e) {
        // stub 
    }

    public void windowDeactivated(WindowEvent e) {
        // stub 
    }

    public void windowDeiconified(WindowEvent e) {
        stopBlinking();
    }

    public void windowOpened(WindowEvent e) {
        // stub 
    }
    
    public void windowActivated(WindowEvent e) {
        stopBlinking();
    }

    public void readStateChanged() {
        updateTooltip();
    }
    
    public void dispose() {
        if (trayIcon != null) {
            TrayFactory.getTrayManager().removeTrayIcon(trayIcon);
            trayIcon = null;
            mainw.removeWindowListener(this);
            recvModel.removeUnreadItemListener(this);
        }
    }
    
    final class ShowAction extends ExcDialogAbstractAction implements MouseListener {
        public ShowAction() {
            this.putValue(Action.NAME, Utils._("Restore"));
        }
        
        @Override
        protected void actualActionPerformed(ActionEvent e) {
            if (System.currentTimeMillis() < lastShowTime+100)
                return; // Ignore the event if we have recently been displayed
            
            lastShowTime = System.currentTimeMillis();
            YajHFCTrayIcon.this.mainw.bringToFront();
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                actionPerformed(null);
            }
        }

        public void mousePressed(MouseEvent e) { }

        public void mouseReleased(MouseEvent e) { }

        public void mouseEntered(MouseEvent e) { }

        public void mouseExited(MouseEvent e) { }
    }

    private static class MenuItemActionPropChangeListener implements PropertyChangeListener {
        private final Action action;
        private final WeakReference<MenuItem> itemRef;
        
        public void propertyChange(PropertyChangeEvent evt) {
            MenuItem menuItem = itemRef.get();
            if (menuItem == null) {
                action.removePropertyChangeListener(this);
            } else {
                String propName = evt.getPropertyName();
                if (Action.NAME.equals(propName)) {
                    menuItem.setLabel((String)action.getValue(Action.NAME));
                } else if ("enabled".equals(propName)) {
                    menuItem.setEnabled(action.isEnabled());
                }
            }
        }
        
        public MenuItemActionPropChangeListener(Action action, MenuItem menuItem) {
            this.action = action;
            itemRef = new WeakReference<MenuItem>(menuItem);
        }
    }
}
