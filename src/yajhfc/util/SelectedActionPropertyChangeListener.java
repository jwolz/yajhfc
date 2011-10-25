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
package yajhfc.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;

public class SelectedActionPropertyChangeListener implements PropertyChangeListener {
    private final AbstractButton actionToggleButton;
    private final PropertyChangeListener orgPCL;
    public static final String SELECTED_PROPERTY = "selected";
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SELECTED_PROPERTY)) {
            this.actionToggleButton.setSelected((Boolean)evt.getNewValue());
        }
        orgPCL.propertyChange(evt);                
    }
    
    public SelectedActionPropertyChangeListener(AbstractButton actionToggleButton, PropertyChangeListener orgPCL) {
        this.actionToggleButton = actionToggleButton;
        this.orgPCL = orgPCL;
    }
}