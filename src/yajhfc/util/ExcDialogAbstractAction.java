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
package yajhfc.util;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import yajhfc.Utils;
import yajhfc.launch.Launcher2;

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
                src = Launcher2.application.getFrame();
            }
            
           ExceptionDialog.showExceptionDialog((Component)src, Utils._("An Error occurred executing the desired action:"), ex);
        }
    }

    protected abstract void actualActionPerformed(ActionEvent e);
    
    @Override
    public String toString() {
        String name = (String)getValue(Action.NAME);
        return (name == null) ? super.toString() : name; 
    }
}
