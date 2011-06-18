/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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
package yajhfc.ui.swing;

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import yajhfc.Utils;
import yajhfc.ui.YajOptionPane;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.PasswordDialog;
import yajhfc.util.ProgressWorker.ProgressUI;

/**
 * Shows the messages using Swing/AWT
 * 
 * @author jonas
 *
 */
public class SwingYajOptionPane extends YajOptionPane {
    static final Logger log = Logger.getLogger(SwingYajOptionPane.class.getName());
    
    protected final Window parent;
    
    public SwingYajOptionPane(Window parent) {
        super();
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see yajhfc.ui.YajOptionPane#showExceptionDialog(java.lang.String, java.lang.String, java.lang.Exception)
     */
    @Override
    public void showExceptionDialog(String title, String message, Exception exc) {
        ExceptionDialog.showExceptionDialog(parent, title, message, exc);
    }

    /* (non-Javadoc)
     * @see yajhfc.ui.YajOptionPane#showExceptionDialog(java.lang.String, java.lang.Exception)
     */
    @Override
    public void showExceptionDialog(String message, Exception exc) {
        ExceptionDialog.showExceptionDialog(parent, message, exc);
    }

    /* (non-Javadoc)
     * @see yajhfc.ui.YajOptionPane#showPasswordDialog(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public String[] showPasswordDialog(String title, String prompt,
            String userName, boolean editableUsername) {
        return PasswordDialog.showPasswordDialog(parent, title, prompt, userName, editableUsername);
    }

    /* (non-Javadoc)
     * @see yajhfc.ui.YajOptionPane#showPasswordDialog(java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
     */
    @Override
    public String[] showPasswordDialog(String title, String prompt,
            String userName, boolean editableUsername,
            boolean allowEmptyPassword) {
        return PasswordDialog.showPasswordDialog(parent, title, prompt, userName, editableUsername, allowEmptyPassword);
    }

    /* (non-Javadoc)
     * @see yajhfc.ui.YajOptionPane#showMessageDialog(java.lang.String, java.lang.String, int)
     */
    @Override
    public void showMessageDialog(final String message, final String title, final int messageType) {
        if (SwingUtilities.isEventDispatchThread()) {
            JOptionPane.showMessageDialog(parent, message, title, messageType);
        } else {
            try {
                if (Utils.debugMode) {
                    log.info("showMessageDialog: msg=\"" + message + "\", title = \"" + title + "\", msgType=" + messageType);
                }
                SwingUtilities.invokeAndWait(new Runnable() { 
                   public void run() {
                       JOptionPane.showMessageDialog(parent, message, title, messageType);
                    } 
                });
            } catch (InterruptedException e) {
                // NOP
            } catch (InvocationTargetException e) {
                // NOP
            }
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.ui.YajOptionPane#showConfirmDialog(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public int showConfirmDialog(String message, String title, int optionType,
            int messageType) {
        if (SwingUtilities.isEventDispatchThread()) {
            return JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
        } else {
            ConfirmDlgDisplayer cdd = new ConfirmDlgDisplayer(parent, message, title, optionType, messageType);
            try {
                SwingUtilities.invokeAndWait(cdd);
                return cdd.returnValue;
            } catch (InterruptedException e) {
                return -1;
            } catch (InvocationTargetException e) {
                return -1;
            }
        }

    }
    
    @Override
    public Window getParent() {
        return parent;
    }
    
    @Override
    public void invokeLater(Runnable toRun) {
        SwingUtilities.invokeLater(toRun);
    }
    
    @Override
    public ProgressUI createDefaultProgressMonitor(String message, String note, int min, int max) {
        return new MyProgressMonitor(parent, message, note, min, max);
    }
    
    /**
     * Wrapper class around ProgressMonitor implementing the ProgressUI interface
     * @author jonas
     *
     */
    protected static class MyProgressMonitor extends ProgressMonitor implements ProgressUI {
        public MyProgressMonitor(Component parentComponent, Object message,
                String note, int min, int max) {
            super(parentComponent, message, note, min, max);
        }
        
        public void showDeterminateProgress(String message, String initialNote, int min,
                int max) {
            throw new IllegalStateException("Can not reinitialize a progress monitor.");            
        }

        public void showIndeterminateProgress(String message, String initialNote) {
            throw new UnsupportedOperationException("Indeterminate progress not supported.");
        }

        public boolean supportsIndeterminateProgress() {
            return false;
        }

        public boolean isShowingIndeterminate() {
            return false;
        }
    }

    private static class ConfirmDlgDisplayer implements Runnable {
        private Component parent;
        private String msg;
        private String title;
        private int msgType;
        private int optionType = Integer.MIN_VALUE;
        
        public int returnValue = 0;
        
        public void run() {
            returnValue = JOptionPane.showConfirmDialog(parent, msg, title, optionType, msgType);
            if (Utils.debugMode) {
                log.info("showConfirmDialog: returnValue=" + returnValue + " (for msg=\"" + msg + "\", title = \"" + title + "\", msgType=" + msgType + ", optionType=" + optionType + ")");
            }
        }
        
        public ConfirmDlgDisplayer(Component parent, String msg, String title, int optionType, int msgType) {
            this.parent = parent;
            this.msg = msg;
            this.title = title;
            this.msgType = msgType;
            this.optionType = optionType;
            
            if (Utils.debugMode) {
                log.info("showConfirmDialog: msg=\"" + msg + "\", title = \"" + title + "\", msgType=" + msgType + ", optionType=" + optionType);
            }
        }
    }
}
