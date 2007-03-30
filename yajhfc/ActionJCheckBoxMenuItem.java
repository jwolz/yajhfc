package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

public class ActionJCheckBoxMenuItem extends JCheckBoxMenuItem {
    
    public static final String SELECTED_PROPERTY = "selected";
    
    @Override
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new MyPropertyChangeListener(super.createActionPropertyChangeListener(a));
    }
    
    @Override
    protected void configurePropertiesFromAction(Action a) {
        Boolean selValue = (Boolean)a.getValue(SELECTED_PROPERTY);
        if (selValue != null)
            setSelected(selValue);
        
        super.configurePropertiesFromAction(a);
    }
    
    public ActionJCheckBoxMenuItem() {
        super();
    }
    
    public ActionJCheckBoxMenuItem(Action a) {
        super(a);
    }
    
    // Wrapper to update the Selected property as needed
    private class MyPropertyChangeListener implements PropertyChangeListener {
        private PropertyChangeListener orgPCL = null;
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(SELECTED_PROPERTY)) {
                setSelected((Boolean)evt.getNewValue());
            }
            orgPCL.propertyChange(evt);                
        }
        
        public MyPropertyChangeListener(PropertyChangeListener orgPCL) {
            this.orgPCL = orgPCL;
        }
    }
}
