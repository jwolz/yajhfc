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
package yajhfc.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import yajhfc.FaxOptions;


public class PanelTreeNode implements TreeNode {

    private List<PanelTreeNode> children;
    private PanelTreeNode parent;
    private OptionsPage<FaxOptions> optionsPage;
    private String label;
    private String longLabel;
    private Icon icon;
    private JPopupMenu popupMenu;
    
    boolean settingsAndUILoaded = false;
    
    public Enumeration<PanelTreeNode> children() {
        if (children == null)
            return null;
        
        return Collections.enumeration(children);
    }

    public boolean getAllowsChildren() {
        return !isLeaf();
    }

    public TreeNode getChildAt(int childIndex) {
        if (children == null)
            return null;
        
        return children.get(childIndex);
    }

    public int getChildCount() {
        if (children == null)
            return 0;
        
        return children.size();
    }

    public int getIndex(TreeNode node) {
        if (children == null)
            return -1;
        
        return children.indexOf(node);
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return (children == null);
    }

    public List<PanelTreeNode> getChildren() {
        return children;
    }

    public void initializeChildren() {
        children = new ArrayList<PanelTreeNode>();
    }
    
    public void addChild(PanelTreeNode child) {
        if (children == null) {
            initializeChildren();
        }
        children.add(child);
    }

    public OptionsPage<FaxOptions> getOptionsPage() {
        return optionsPage;
    }
    
    public Icon getIcon() {
        return icon;
    }

    public String getLongLabel() {
        return longLabel;
    }
    
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }
    
    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }
    
    @Override
    public String toString() {
        return label;
    }
    
    public DefaultTreeModel getTreeModel() {
        return parent.getTreeModel();
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setLongLabel(String longLabel) {
        this.longLabel = longLabel;
    }
    
    public PanelTreeNode(PanelTreeNode parent, OptionsPage<FaxOptions> page, String label, Icon icon) {
        this(parent,page,label,icon,label);
    }
    
    public PanelTreeNode(PanelTreeNode parent, OptionsPage<FaxOptions> page, String label, Icon icon, String longLabel) {
        super();
        this.label = label;
        this.optionsPage = page;
        this.parent = parent;
        this.icon = icon;
        this.longLabel = longLabel;
    }
}