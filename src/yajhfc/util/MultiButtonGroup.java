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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;

import yajhfc.Utils;
import yajhfc.launch.Launcher2;

/**
 * This class synchronizes multiple button groups.
 * @author jonas
 *
 */
public abstract class MultiButtonGroup {
    /**
     * The label for this button group
     */
    public String label;
    
    protected List<Item> items = new ArrayList<Item>();
    protected String selectedActionCommand = null;
    
    protected abstract void actualActionPerformed(ActionEvent e);

    public Item addItem(String label, String actionCommand) {
        Item item;
        items.add(item = new Item(label, actionCommand));
        return item;
    }
    
    public String getSelectedActionCommand() {
        return selectedActionCommand;
    }
    
    public void setSelectedActionCommand(String actionCommand) {
        if (!actionCommand.equals(selectedActionCommand)) {
            for (Item item : items) {
                item.setSelected(actionCommand.equals(item.getActionCommand()));
            }
            selectedActionCommand = actionCommand;
        }
    }

    public JRadioButtonMenuItem[] createMenuItems() {
        JRadioButtonMenuItem[] menuItems = new JRadioButtonMenuItem[items.size()];
        //ButtonGroup group = new ButtonGroup();
        
        for (int i = 0; i < items.size(); i++) {
            JRadioButtonMenuItem menuItem = new ActionJRadioButtonMenuItem(items.get(i));
            //group.add(menuItem);
            
            menuItems[i] = menuItem;
        }
        
        return menuItems;
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public class Item extends AbstractAction {
        
        public String getActionCommand() {
            return (String)getValue(Action.ACTION_COMMAND_KEY);
        }
        
        public void setSelected(boolean isSelected) {
            putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, isSelected);
        }

        public boolean isSelected() {
            Boolean selected = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
            return (selected != null) && selected.booleanValue();
        }
        
        public final void actionPerformed(ActionEvent e) {
            try {
                setSelectedActionCommand(e.getActionCommand());
                if (e.getSource() instanceof AbstractButton) {
                    ((AbstractButton)e.getSource()).setSelected(true);
                }
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
        
        protected Item(String label, String actionCommand) {
            super(label);
            
            putValue(Action.ACTION_COMMAND_KEY, actionCommand);
            setSelected(false);
        }
    }
}
