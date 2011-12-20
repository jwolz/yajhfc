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
     * The options panel should be put under the advanced node
     */
    public static final int OPTION_PANEL_ADVANCED = 1;
    /**
     * The options panel should be put under the general node
     */
    public static final int OPTION_PANEL_GENERAL = 2;
    /**
     * The options panel should be put under the paths&viewers node
     */
    public static final int OPTION_PANEL_PATHS_VIEWERS = 3;
    /**
     * The options panel should be put under the server node
     */
    public static final int OPTION_PANEL_SERVER = 4;
    /**
     * The options panel should be put under the cover page node
     */
    public static final int OPTION_PANEL_COVER = 5;
    /**
     * The options panel should be put under the plugins&JDBC node
     */
    public static final int OPTION_PANEL_PLUGINS = 6;
    /**
     * The options panel should be put under the tables node
     */
    public static final int OPTION_PANEL_TABLES = 7;
    
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