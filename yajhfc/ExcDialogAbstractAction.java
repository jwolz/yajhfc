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
package yajhfc;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * An abstract Action where RuntimeExceptions occurring in actualActionPerformed
 * are caught and shown using an Exception Dialog.
 * 
 * @author jonas
 *
 */
public abstract class ExcDialogAbstractAction extends AbstractAction {

    /**
     * 
     */
    public ExcDialogAbstractAction() {
    }

    /**
     * @param name
     */
    public ExcDialogAbstractAction(String name) {
        super(name);
    }

    /**
     * @param name
     * @param icon
     */
    public ExcDialogAbstractAction(String name, Icon icon) {
        super(name, icon);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent e) {
        try {
            actualActionPerformed(e);
        } catch (Exception ex) {
            Object src = null;
            if (e != null) {
                src = e.getSource();
            }
            if (src == null || !(src instanceof Component)) {
                src = Launcher.application;
            } else if (!(src instanceof Window)) {
                src = SwingUtilities.getWindowAncestor((Component)src);
                if (src == null) {
                    src = Launcher.application;
                }
            }
            
            if (src instanceof Dialog) {
                ExceptionDialog.showExceptionDialog((Dialog)src, utils._("An Error occurred executing the desired action:"), ex);
            } else if (src instanceof Frame) {
                ExceptionDialog.showExceptionDialog((Frame)src, utils._("An Error occurred executing the desired action:"), ex);
            } else {
                JOptionPane.showMessageDialog(null, utils._("An Error occurred executing the desired action:") + "\n\n" + ex);
            }
        }
    }

    protected abstract void actualActionPerformed(ActionEvent e);
}
