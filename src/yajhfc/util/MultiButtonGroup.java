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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import yajhfc.Utils;
import yajhfc.launch.Launcher2;

/**
 * This class synchronizes multiple button groups.
 * @author jonas
 *
 */
public abstract class MultiButtonGroup implements ActionListener {
    /**
     * The label for this button group
     */
    public String label;
    
    protected List<Item> items = new ArrayList<Item>();
    protected List<AbstractButton> childItems = new ArrayList<AbstractButton>();
    protected String selectedActionCommand = null;
    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent e) {
        try {
            setSelectedActionCommand(e.getActionCommand(), e.getSource());
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

    public void addItem(String label, String actionCommand) {
        items.add(new Item(label, actionCommand));
    }
    
    public String getSelectedActionCommand() {
        return selectedActionCommand;
    }
    
    public void setSelectedActionCommand(String selectedActionCommand) {
        setSelectedActionCommand(selectedActionCommand, null);
    }
    
    protected void setSelectedActionCommand(String actionCommand, Object source) {
        if (!actionCommand.equals(selectedActionCommand)) {
            for (AbstractButton button : childItems) {
                if (button != source && actionCommand.equals(button.getActionCommand())) {
                    button.setSelected(true);
                }
            }
            selectedActionCommand = actionCommand;
        }
    }
    
    public JRadioButtonMenuItem[] createMenuItems() {
        JRadioButtonMenuItem[] menuItems = new JRadioButtonMenuItem[items.size()];
        ButtonGroup group = new ButtonGroup();
        
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(item.label);
            menuItem.setActionCommand(item.actionCommand);
            menuItem.addActionListener(this);
            group.add(menuItem);
            
            if (item.actionCommand.equals(selectedActionCommand)) {
                menuItem.setSelected(true);
            }
            menuItems[i] = menuItem;
            childItems.add(menuItem);
        }
        
        return menuItems;
    }
    
    public static class Item {
        public final String label;
        public final String actionCommand;
        
        protected Item(String label, String actionCommand) {
            super();
            this.label = label;
            this.actionCommand = actionCommand;
        }        
    }
}
