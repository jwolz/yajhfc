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
package yajhfc.options;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;


public class PanelTreeNode implements TreeNode {

    private List<PanelTreeNode> children;
    private TreeNode parent;
    private OptionsPage optionsPage;
    private String label;
    private String longLabel;
    private Icon icon;
    
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

    public void setChildren(List<PanelTreeNode> children) {
        this.children = children;
    }

    public void setChildren(PanelTreeNode[] children) {
        this.children = Arrays.asList(children);
    }

    public OptionsPage getOptionsPage() {
        return optionsPage;
    }
    
    public Icon getIcon() {
        return icon;
    }

    public String getLongLabel() {
        return longLabel;
    }
    
    @Override
    public String toString() {
        return label;
    }
    
    public PanelTreeNode(TreeNode parent, OptionsPage page, String label, Icon icon) {
        this(parent,page,label,icon,label);
    }
    
    public PanelTreeNode(TreeNode parent, OptionsPage page, String label, Icon icon, String longLabel) {
        super();
        this.label = label;
        this.optionsPage = page;
        this.parent = parent;
        this.icon = icon;
        this.longLabel = longLabel;
    }
}