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
package yajhfc.util;

import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * Implements a list model based on a List
 * @author jonas
 *
 */
public class ListListModel<T> extends AbstractListModel {

    protected List<T> list;

    public ListListModel(List<T> list) {
        this.list = list;
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
        list.add(element);
        int pos = list.size() - 1;
        fireIntervalAdded(this, pos, pos);
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
        int min, max;
        min = list.size();
        list.addAll(elements);
        max = list.size() - 1;
        fireIntervalAdded(this, min, max);
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
    
    private void swap(int index1, int index2) {
        T tmp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, tmp);
    }
    
    /**
     * Moves the elements at the specified positions one position up.
     * @param indexes an list of indexes, sorted in ascending order (!) 
     */
    public void moveUp(int[] indexes) {
        if (indexes.length == 0)
            return;
        if (indexes[0] <= 0 ) {
            throw new ArrayIndexOutOfBoundsException("Cannot move first element up.");
        }
        for (int i : indexes) {
            swap(i, i-1);
        }
        fireContentsChanged(this, indexes[0]-1, indexes[indexes.length-1]);
    }
    
    /**
     * Moves the elements at the specified positions one position down.
     * @param indexes an list of indexes, sorted in ascending order (!) 
     */
    public void moveDown(int[] indexes) {
        if (indexes.length == 0)
            return;
        if (indexes[indexes.length - 1] >= list.size()) {
            throw new ArrayIndexOutOfBoundsException("Cannot move last element down.");
        }
        for (int i : indexes) {
            swap(i, i+1);
        }
        fireContentsChanged(this, indexes[0], indexes[indexes.length-1]+1);
    }
    
    public T get(int index) {
        return list.get(index);
    }

    public List<T> getList() {
        return list;
    }
}
