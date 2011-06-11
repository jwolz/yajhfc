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
package yajhfc.customprops;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import yajhfc.FaxOptions;
import yajhfc.options.AbstractOptionsPanel;

/**
 * @author jonas
 *
 */
public class CustomPropOptionsPanel extends AbstractOptionsPanel<FaxOptions> {
    JTextField proxyHost;
    JTextField proxyPort;
    JCheckBox useSystemProperties;
    
    Action actEditProps;
    
    
    public CustomPropOptionsPanel() {
        super(null);
    }

    public void loadSettings(FaxOptions foEdit) {
        // TODO Auto-generated method stub
        
    }

    public void saveSettings(FaxOptions foEdit) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void createOptionsUI() {
        // TODO Auto-generated method stub
        
    }
    
}
