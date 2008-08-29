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
package yajhfc.phonebook;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import yajhfc.utils;

/**
 * @author jonas
 *
 */
public class PhoneBookTreeModel implements TreeModel, ListDataListener {

    protected final List<PhoneBook> phoneBooks = new ArrayList<PhoneBook>();
    protected final List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    
    protected final RootNode rootNode;
    protected JTree tree;
    
    public void addPhoneBook(PhoneBook pb) {
        phoneBooks.add(pb);
        pb.addListDataListener(this);
        fireTreeNodesInserted(new TreeModelEvent(this, new Object[] { rootNode },
                new int[] { phoneBooks.size() - 1 }, new Object[] { pb }));
    }
    
    public void removePhoneBook(PhoneBook pb) {
        int idx = phoneBooks.indexOf(pb);
        if (idx < 0)
            return;
        
        phoneBooks.remove(idx);
        pb.removeListDataListener(this);
        fireTreeNodesRemoved(new TreeModelEvent(this, new Object[] { rootNode },
                    new int[] { idx }, new Object[] { pb }));
    }
    
    public List<PhoneBook> getPhoneBooks() {
        return phoneBooks;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index) {
        if (parent instanceof RootNode) {
            return phoneBooks.get(index);
        } else if (parent instanceof PhoneBook) {
            return ((PhoneBook)parent).getElementAt(index);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent) {
        if (parent instanceof RootNode) {
            return phoneBooks.size();
        } else if (parent instanceof PhoneBook) {
            return ((PhoneBook)parent).getSize();
        } else {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof RootNode) {
            return phoneBooks.indexOf(child);
        } else if (parent instanceof PhoneBook && child instanceof PhoneBookEntry) {
            return ((PhoneBook)parent).indexOf((PhoneBookEntry)child);
        } else {
            return -1;
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot() {
        return rootNode;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node) {
        return !((node instanceof RootNode) || (node instanceof PhoneBook));
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        // Not editable
        
    }
    
    protected void fireTreeStructureChanged(TreeModelEvent tme) {
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(tme);
        }
    }
    
    protected void fireTreeNodesChanged(TreeModelEvent tme) {
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesChanged(tme);
        }
    }
    
    protected void fireTreeNodesInserted(TreeModelEvent tme) {
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesInserted(tme);
        }
    }
    
    protected void fireTreeNodesRemoved(TreeModelEvent tme) {
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeNodesRemoved(tme);
        }
    }
    
    protected TreeModelEvent treeModelEventFromListDataEvent(ListDataEvent e) {
        PhoneBook pb = (PhoneBook)e.getSource();
        int[] indices = new int[e.getIndex1() - e.getIndex0() + 1];
        Object[] childs = new Object[indices.length];
        
        for (int i = 0; i < indices.length; i++) {
            int idx = e.getIndex0() + i;
            indices[i] = idx;
            childs[i] = pb.getElementAt(idx);
        }
        
        return new TreeModelEvent(this, new Object[] { rootNode, pb }, indices, childs);
    }
    
    public void contentsChanged(ListDataEvent e) {
        TreePath[] oldSelection = null;
        if (tree != null) {
            oldSelection = tree.getSelectionPaths();
        }
        
        //fireTreeNodesChanged(treeModelEventFromListDataEvent(e));
        fireTreeStructureChanged(new TreeModelEvent(this, new Object[] {rootNode, e.getSource()}));
        
        if (tree != null) {
            tree.setSelectionPaths(oldSelection);
        }
    }

    public void intervalAdded(ListDataEvent e) {
        fireTreeNodesInserted(treeModelEventFromListDataEvent(e));
    }

    public void intervalRemoved(ListDataEvent e) {
        fireTreeNodesRemoved(treeModelEventFromListDataEvent(e));
    }

    public JTree getTree() {
        return tree;
    }

    public void setTree(JTree tree) {
        this.tree = tree;
    }
    
    public PhoneBookTreeModel() {
        rootNode = new RootNode(utils._("All phone books"));
    }

    protected static class RootNode {
        private String caption;
        
        @Override
        public String toString() {
            return caption;
        }
        
        public RootNode(String caption) {
            this.caption = caption;
        }
    }
}
