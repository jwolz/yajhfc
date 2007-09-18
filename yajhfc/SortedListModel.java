/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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
package yajhfc;

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
