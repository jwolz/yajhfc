/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.plugin;

import java.util.Properties;

import javax.swing.JMenuItem;

import yajhfc.options.PanelTreeNode;

/**
 * This abstract class is used to create an UI for plugins.
 * The default implementation of all methods just do nothing.
 * @author jonas
 *
 */
public abstract class PluginUI {
    
    /**
     * The options panel should be put at the top level
     */
    public static final int OPTION_PANEL_ROOT = 0;
    /**
     * The options panel should be put under the advanced panel
     */
    public static final int OPTION_PANEL_ADVANCED = 1;
    
    /**
     * Creates menu items shown in the options menu of MainWin. <br>
     * This method is called in the event dispatching thread when MainWin is constructed.
     * @return the menu items or null
     */
    public JMenuItem[] createMenuItems() {
        return null;
    }
    
    /**
     * Returns below which panel the options panel should be put.
     * @return one of the OPTION_PANEL constants
     */
    public int getOptionsPanelParent() {
        return OPTION_PANEL_ROOT;
    }
    
    /**
     * Returns a tree node used to display a options page for this plugin. <br>
     * The node's panel should implement the OptionsPage interface in order to allow for saving/loading
     * the settings. <br>
     * This method is called in the event dispatching thread whenever the Options dialog is opened.
     * @return the tree node or null if no options page is needed
     */
    public PanelTreeNode createOptionsPanel(PanelTreeNode parent) {
        return null;
    }
    
    /**
     * Allows this plugin to store settings in the given Properties file. <br>
     * This method is called when YajHFC is exited.
     */
    public void saveOptions(Properties p) {
        // Do nothing
    }
}