package yajhfc.util;
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

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;

public class ActionJRadioButtonMenuItem extends JRadioButtonMenuItem {
    
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
    
    public ActionJRadioButtonMenuItem() {
        super();
    }
    
    public ActionJRadioButtonMenuItem(Action a) {
        super(a);
    }
    
}
