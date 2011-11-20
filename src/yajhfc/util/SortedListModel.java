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
package yajhfc.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * Implements an sorted list model
 * @author jonas
 *
 */
public class SortedListModel<T> extends AbstractListModel {

    protected List<T> list;
    protected Comparator<T> comparator;
    
    public SortedListModel(List<T> list, Comparator<T> comparator) {
        this.list = list;
        this.comparator = comparator;
        sort();
    }
    
    public void sort() {
        Collections.sort(list, comparator);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return list.get(index);
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return list.size();
    }

    public void add(T element) {
        int insertpos = Collections.binarySearch(list, element, comparator);
        if (insertpos < 0) {
            insertpos = -(insertpos + 1);
        }
        list.add(insertpos, element);
        fireIntervalAdded(this, insertpos, insertpos);
    }
        
    public void remove(int index) {
        list.remove(index);
        fireIntervalRemoved(this, index, index);
    }
    
    public void clear() {
        int oldMax = list.size() - 1;
        list.clear();
        fireIntervalRemoved(this, 0, oldMax);
    }
    
    public void addAll(Collection<? extends T> elements) {
        for (T el : elements) {
            add(el);
        }
    }
    
    /**
     * Removes all indices given in the array
     * @param indexes an array of indices sorted in ascending order (!)
     */
    public void removeAll(int[] indexes) {
        for (int i = indexes.length - 1; i >= 0; i--) {
            remove(indexes[i]);
        }
    }
    
    public T get(int index) {
        return list.get(index);
    }
}
