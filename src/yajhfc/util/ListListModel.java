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
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * Implements a list model based on a List
 * @author jonas
 *
 */
public class ListListModel<T> extends AbstractListModel implements Collection<T> {

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
    
    public void changeNotify(T element) {
        int index = list.indexOf(element);
        fireContentsChanged(this, index, index);
    }
    
    public void changeNotify(int index) {
        fireContentsChanged(this, index, index);
    }

    public boolean add(T element) {
        list.add(element);
        int pos = list.size() - 1;
        fireIntervalAdded(this, pos, pos);
        return true;
    }
    
    public void add(int index, T element) {
        list.add(index, element);
        fireIntervalAdded(this, index, index);
    }
    
    public void remove(int index) {
        list.remove(index);
        fireIntervalRemoved(this, index, index);
    }
    
    public void clear() {
        int oldMax = list.size() - 1;
        list.clear();
        if (oldMax >= 0) {
            fireIntervalRemoved(this, 0, oldMax);
        }
    }
    
    public boolean addAll(Collection<? extends T> elements) {
        int min, max;
        min = list.size();
        if (list.addAll(elements)) {
            max = list.size() - 1;
            if (max >= 0)
                fireIntervalAdded(this, min, max);
            return true;
        } else {
            return false;
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
    
    /**
     * Swaps the elements at the given positions
     * @param index1
     * @param index2
     */
    public void swap(int index1, int index2) {
        Collections.swap(list, index1, index2);
        fireContentsChanged(this, index1, index1);
        fireContentsChanged(this, index2, index2);
    }
        
    /**
     * Sort the list using the given comparator
     * @param comparator
     */
    public void sort(Comparator<? super T> comparator) {
        Collections.sort(list, comparator);
        fireContentsChanged(this, 0, list.size()-1);
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
            Collections.swap(list, i, i-1);
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
            Collections.swap(list, i, i+1);
        }
        fireContentsChanged(this, indexes[0], indexes[indexes.length-1]+1);
    }
    
    public T get(int index) {
        return list.get(index);
    }

    /**
     * Returns the list used internally by this model.
     * WARNING: Modifying this list can have undesirable effects!
     * @return
     */
    public List<T> getList() {
        return list;
    }
    
    /**
     * Sets the list internally used by this model
     * @param list
     */
    public void setList(List<T> list) {
        if (this.list != list) {
            int oldEnd = this.list.size()-1;
            if (oldEnd >= 0) {
                this.list = Collections.emptyList();
                fireIntervalRemoved(this, 0, oldEnd);
            }
            
            this.list = list;
            fireIntervalAdded(this, 0, list.size()-1);
        }
    }

    /**
     * Returns an iterator to iterate over this model
     */
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <U> U[] toArray(U[] a) {
        return list.toArray(a);
    }

    public boolean remove(Object o) {
        int idx = list.indexOf(o);
        if (idx >= 0) {
            remove(idx);
            return true;
        } else {
            return false;
        }
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        boolean res = false;
        for (Object o : c) {
            res |= remove(o);
        }
        return res;
    }

    public boolean retainAll(Collection<?> c) {
        boolean res = false;
        for (Object o : list.toArray()) {
            if (!c.contains(o)) {
                remove(o);
                res = true;
            }
        }
        return res;
    }
}
