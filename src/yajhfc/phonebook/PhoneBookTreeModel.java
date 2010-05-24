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
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.phonebook.convrules.EntryToStringRule;

/**
 * @author jonas
 *
 */
public class PhoneBookTreeModel implements TreeModel, PhonebookEventListener {
    //private static final Logger log = Logger.getLogger(PhoneBookTreeModel.class.getName());
    
    protected final List<PhoneBook> phoneBooks = new ArrayList<PhoneBook>();
    protected final EventListenerList listeners = new EventListenerList();
    
    protected final RootNode rootNode;
    protected JTree tree;
    protected boolean showFilteredResults = false;
    
    protected EntryToStringRule entryToStringRule = PhoneBook.DEFAULT_TOSTRINGRULE;
    
    public void addPhoneBook(PhoneBook pb) {
        phoneBooks.add(pb);
        pb.setEntryToStringRule(entryToStringRule);
        pb.addPhonebookEventListener(this);
        fireTreeNodesInserted(new TreeModelEvent(this, new Object[] { rootNode },
                new int[] { phoneBooks.size() - 1 }, new Object[] { pb }));
    }
    
    public void removePhoneBook(PhoneBook pb) {
        int idx = phoneBooks.indexOf(pb);
        if (idx < 0)
            return;
        
        phoneBooks.remove(idx);
        pb.removePhonebookEventListener(this);
        fireTreeNodesRemoved(new TreeModelEvent(this, new Object[] { rootNode },
                    new int[] { idx }, new Object[] { pb }));
    }
    
    public void refreshPhoneBook(PhoneBook pb) {
        fireTreeStructureChanged(new TreeModelEvent(this, new Object[] {rootNode, pb}));
    }
    
    public List<PhoneBook> getPhoneBooks() {
        return phoneBooks;
    }
    
    public void sortPhonebooks() {
        Collections.sort(phoneBooks, new Comparator<PhoneBook>() {
           public int compare(PhoneBook o1, PhoneBook o2) {
                int res = o1.toString().compareToIgnoreCase(o2.toString());
                if (res == 0) {
                    res = o1.getDescriptor().compareTo(o2.getDescriptor());
                }
                return res;
            } 
        });
        fireTreeStructureChanged(new TreeModelEvent(this, new Object[] {rootNode}));
    }
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(TreeModelListener.class, l);
    }
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(TreeModelListener.class, l);
    }
    
    public void addPBTreeModelListener(PBTreeModelListener l) {
        listeners.add(PBTreeModelListener.class, l);
    }
    public void removePBTreeModelListener(PBTreeModelListener l) {
        listeners.remove(PBTreeModelListener.class, l);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index) {
        if (parent instanceof RootNode) {
            return phoneBooks.get(index);
        } else if (parent instanceof PhoneBook) {
            List<PhoneBookEntry> childs = (showFilteredResults ? 
                    ((PhoneBook)parent).lastFilterResult :
                        ((PhoneBook)parent).getEntries());
            return (childs == null) ? null : childs.get(index);
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
            List<PhoneBookEntry> childs = (showFilteredResults ? 
                    ((PhoneBook)parent).lastFilterResult :
                        ((PhoneBook)parent).getEntries());
            return (childs == null) ? 0 : childs.size();
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
            List<PhoneBookEntry> childs = (showFilteredResults ? 
                    ((PhoneBook)parent).lastFilterResult :
                        ((PhoneBook)parent).getEntries());
            return (childs == null) ? -1 : childs.indexOf(child);
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
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        // Not editable
        
    }
    
    /**
     * Used to reset the filter and signalize this fact to listeners
     */
    protected void resetFilter() {
        applyFilter((Filter<PhoneBookEntry,PBEntryField>)null);
        fireFilterWasReset();
    }
   
    
    /**
     * Applies a filter to this tree model. Specifying null resets the filtering (i.e.
     * all entries are shown) *without* firing the filterWasReset event. 
     * @param filter
     */
    public void applyFilter(Filter<PhoneBookEntry,PBEntryField> filter) {
        boolean oldFiltered = showFilteredResults;
        if (filter == null) {
            showFilteredResults = false;
        } else {
            showFilteredResults = true;
        }
        
        TreePath[] oldSelection = null;
        if (tree != null) {
            oldSelection = tree.getSelectionPaths();
        }
        
        for (PhoneBook pb : phoneBooks) {
            List<PhoneBookEntry> oldEntries = oldFiltered ? pb.lastFilterResult : pb.getEntries();
            pb.lastFilterResult = pb.applyFilter(filter);
            
            List<PhoneBookEntry> newEntries = showFilteredResults ? pb.lastFilterResult : pb.getEntries();
            if (!listQuickEquals(oldEntries, newEntries)) {
                fireTreeStructureChanged(new TreeModelEvent(this, new Object[] {rootNode, pb}));
            }
        }
        
        if (tree != null) {
            tree.setSelectionPaths(oldSelection);
        }
    }
    
    /**
     * Compares if the content of two lists consists of identical objects 
     * (in the sense of ==, not equals())
     * @param list1
     * @param list2
     * @return
     */
    private static boolean listQuickEquals(List<?> list1, List<?> list2) {
        if (list1 == list2)
            return true;
        else if (list1 == null || list2 == null) 
            return false;

        if (list1.size() != list2.size()) {
            return false;
        } else {
            for (int i = 0; i < list1.size(); i++) {
                if (list1.get(i) != list2.get(i)) {
                    return false;
                }
            }
            return true;
        }
    }
    
//    /**
//     * Computes the difference between the two phone books. It must be assured that
//     * smaller contains a sub set of larger *and* that the elements are in the same order
//     * if they exist.
//     * @param larger
//     * @param smaller
//     * @return null if one of the above conditions is not met.
//     */
//    private static final int[] zeroArray = new int[0];
//    protected int[] diffPhoneBooks(List<PhoneBookEntry> larger, List<PhoneBookEntry> smaller) {
//        if (larger.size() < smaller.size()) {
//            List<PhoneBookEntry> temp = larger;
//            larger = smaller;
//            smaller = temp;
//        }
//        
//        int lsize = larger.size();
//        int ssize = smaller.size();
//        if (lsize == ssize) {
//            return zeroArray;
//        }
//        
//        int[] res = new int[lsize];
//        int resPtr = 0;
//        
//        int l,s;
//        for (l = 0, s = 0 ;l < lsize && s < ssize; ) {
//            PhoneBookEntry lentry = larger.get(l);
//            PhoneBookEntry sentry = smaller.get(s);
//            
//            if (lentry == sentry) {
//                l++;
//                s++;
//            } else {
//                res[resPtr++] = l++;
//            }
//        }
//        if (s < ssize) {
//            log.warning("s < ssize!");
//            return null;
//        }
//        for (; l < lsize; l++) {
//            res[resPtr++] = l;
//        }
//        
//        if (resPtr == 0) {
//            return zeroArray;
//        } else if (resPtr < lsize) {
//            int[] rv = new int[resPtr];
//            System.arraycopy(res, 0, rv, 0, resPtr);
//            return rv;
//        } else {
//            return res;
//        }
//    }

    protected void fireTreeStructureChanged(TreeModelEvent tme) {
        Object[] l = listeners.getListenerList();
        for (int i = l.length-2; i>=0; i-=2) {
            if (l[i] == TreeModelListener.class) {
                ((TreeModelListener)l[i+1]).treeStructureChanged(tme);
            }
        }
    }
    
    protected void fireTreeNodesChanged(TreeModelEvent tme) {
        Object[] l = listeners.getListenerList();
        for (int i = l.length-2; i>=0; i-=2) {
            if (l[i] == TreeModelListener.class) {
                ((TreeModelListener)l[i+1]).treeNodesChanged(tme);
            }
        }
    }
    
    protected void fireTreeNodesInserted(TreeModelEvent tme) {
        Object[] l = listeners.getListenerList();
        for (int i = l.length-2; i>=0; i-=2) {
            if (l[i] == TreeModelListener.class) {
                ((TreeModelListener)l[i+1]).treeNodesInserted(tme);
            }
        }
    }
    
    protected void fireTreeNodesRemoved(TreeModelEvent tme) {
        Object[] l = listeners.getListenerList();
        for (int i = l.length-2; i>=0; i-=2) {
            if (l[i] == TreeModelListener.class) {
                ((TreeModelListener)l[i+1]).treeNodesRemoved(tme);
            }
        }
    }
    
    protected void fireFilterWasReset() {
        Object[] l = listeners.getListenerList();
        for (int i = l.length-2; i>=0; i-=2) {
            if (l[i] == PBTreeModelListener.class) {
                ((PBTreeModelListener)l[i+1]).filterWasReset();
            }
        }
    }

    public void elementsAdded(PhonebookEvent e) {
        if (showFilteredResults) {
            resetFilter();
        } else {
            fireTreeNodesInserted(new TreeModelEvent(this, new Object[] {rootNode, e.getPhonebook()}, e.getIndices(), e.getEntries()));
        }
    }

    public void elementsChanged(PhonebookEvent e) {
        if (showFilteredResults) {
            resetFilter();
        } else {
            TreePath[] oldSelection = null;
            if (tree != null) {
                oldSelection = tree.getSelectionPaths();
            }
            
            fireTreeStructureChanged(new TreeModelEvent(this, new Object[] {rootNode, e.getSource()}));
            
            if (tree != null) {
                tree.setSelectionPaths(oldSelection);
            }
        }
    }

    public void elementsRemoved(PhonebookEvent e) {
        if (showFilteredResults) {
            resetFilter();
        } else {
            fireTreeNodesRemoved(new TreeModelEvent(this, new Object[] {rootNode, e.getPhonebook()}, e.getIndices(), e.getEntries()));
        }
    }
    
    public JTree getTree() {
        return tree;
    }

    public void setTree(JTree tree) {
        this.tree = tree;
    }
    
    public EntryToStringRule getEntryToStringRule() {
        return entryToStringRule;
    }
    
    public void setEntryToStringRule(EntryToStringRule toStringRule) {
        if (toStringRule != entryToStringRule) {
            this.entryToStringRule = toStringRule; 
            for (PhoneBook pb : getPhoneBooks()) {
                pb.setEntryToStringRule(toStringRule);

                List<PhoneBookEntry> entries = showFilteredResults ? pb.lastFilterResult : pb.getEntries();
                final Object[] entryArray = entries.toArray();
                final int[] indexArray = new int[entryArray.length];
                for (int i = 0 ; i < indexArray.length; i++) {
                    indexArray[i] = i;
                }
                fireTreeNodesChanged(new TreeModelEvent(this, new Object[] { rootNode, pb }, indexArray, entryArray));            
            }
        }
    }
    
    public void setNameToStringRule(EntryToStringRule nameRule) {
        setEntryToStringRule(PhoneBook.toStringRuleFromNameRule(nameRule));
    }
    
    public PhoneBookTreeModel() {
        rootNode = new RootNode(Utils._("All phone books"));
    }

    /**
     * @return the rootNode
     */
    public RootNode getRootNode() {
        return rootNode;
    }

    public static class RootNode {
        private String caption;
        
        @Override
        public String toString() {
            return caption;
        }
        
        public RootNode(String caption) {
            this.caption = caption;
        }
    }
    
    public interface PBTreeModelListener extends EventListener {
        /**
         * Fired to signalize that the filter has been reset due to changes in 
         * the underlying data model.
         */
        public void filterWasReset();
    }
}
