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