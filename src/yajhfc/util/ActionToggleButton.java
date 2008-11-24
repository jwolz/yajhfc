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
package yajhfc.util;

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JToggleButton;

/**
 * @author jonas
 *
 */
public class ActionToggleButton extends JToggleButton {
    
    @Override
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new SelectedActionPropertyChangeListener(this, super.createActionPropertyChangeListener(a));
    }
    
    @Override
    protected void configurePropertiesFromAction(Action a) {
        Boolean selValue = (Boolean)a.getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
        if (selValue != null)
            setSelected(selValue);
        
        super.configurePropertiesFromAction(a);
    }
    
    public ActionToggleButton() {
        super();
    }
    
    public ActionToggleButton(Action a) {
        super(a);
    }
}
