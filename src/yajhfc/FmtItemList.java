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

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.filters.FilterKeyList;

/**
 * Contains a list of FmtItems.
 * @author jonas
 *
 */
public class FmtItemList<T extends FmtItem> extends ArrayList<T> implements FilterKeyList<T> {
    private static final Logger log = Logger.getLogger(FmtItemList.class.getName());
    
    protected T[] availableItems;
    protected T[] obligateItems;
    protected List<T> completeView;

    /**
     * Returns a view of this list that has the obligateItems appended to the end if necessary.
     * @return
     */
    public List<T> getCompleteView() {
        if (completeView == null) {
            List<T> additionalItems = new ArrayList<T>(obligateItems.length);
            for (T fi : obligateItems) {
                if (!this.contains(fi)) {
                    additionalItems.add(fi);
                }
            }
            if (additionalItems.size() > 0) {
                completeView = new CompleteView(additionalItems);
            } else {
                completeView = this; // Optimization: If all required items are present, the "full view" is identical
            }
        }
        return completeView;
    }
    
    /**
     * Returns a format string suitable for the RECVFMT/JOBFMT commands
     * @return
     */
    public String getFormatString() {
        StringBuilder res = new StringBuilder();
        for (T item : getCompleteView()) {
            res.append('%').append(item.getHylaFmt()).append('|');
        }
        res.deleteCharAt(res.length()-1);
        return res.toString();
    }
    
    private static final char sep = '|';
    
    /**
     * Creates a String representing the content of this list for storage 
     * @return
     */
    public String saveToString() {
        StringBuilder saveval = new StringBuilder();
        for (T fi : this) {
            saveval.append(fi.name()).append(sep);
        }
        return saveval.toString();
    }
    
    /**
     * Loads the content previously saved by saveToString()
     * @param saved
     */
    public void loadFromString(String saved) {
        String[] fields = Utils.fastSplit(saved, sep);
       
        this.clear();
        
        for (int i=0; i < fields.length; i++) {
            T res = getKeyForName(fields[i]);
            if (res == null) {
                log.log(Level.WARNING, "FmtItem for " + fields[i] + "not found.");
            } else {
                if (!this.contains(res)) {
                    this.add(res);
                }
            }
        }
    }
    
    /**
     * Returns the item with the given name or null if it is not found
     */
    @SuppressWarnings("unchecked")
    public T getKeyForName(String name) {
        Class<?> actualT = availableItems.getClass().getComponentType();
        if (Enum.class.isAssignableFrom(actualT)) {
            try {
                return (T)Enum.valueOf((Class<Enum>)actualT, name);
            } catch (Exception e) {
                log.log(Level.INFO, "Enum constant not found: ", e);
                return null;
            }
        } else {
            for (T item : availableItems) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
            return null;
        }
    }
    
    // Override methods modifying the list to reset the complete view
    @Override
    public boolean add(T o) {
        completeView = null;
        return super.add(o);
    }

    @Override
    public void add(int index, T element) {
        completeView = null;
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        completeView = null;
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        completeView = null;
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        completeView = null;
        super.clear();
    }

    @Override
    public T remove(int index) {
        completeView = null;
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        completeView = null;
        return super.remove(o);
    }

    @Override
    public T set(int index, T element) {
        completeView = null;
        return super.set(index, element);
    }

    public FmtItemList(T[] allItems, T[] obligateItems) {
        super();
        this.availableItems = allItems;
        this.obligateItems = obligateItems;
    }
    
    protected class CompleteView extends AbstractList<T> {
        protected List<T> additionalItems;
        
        @Override
        public T get(int index) {
            int additionalIdx = index - FmtItemList.this.size();
            if (additionalIdx < 0) {
                return FmtItemList.this.get(index);
            } else if (additionalIdx < additionalItems.size()){
                return additionalItems.get(additionalIdx);
            } else {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }

        @Override
        public int size() {
            return FmtItemList.this.size() + additionalItems.size();
        }
        
        public CompleteView(List<T> additionalItems) {
            this.additionalItems = additionalItems;
        }
    }

    public boolean containsKey(T key) {
        return getCompleteView().contains(key);
    }

    public T[] getAvailableKeys() {
        return getCompleteView().toArray(newArray(getCompleteView().size()));
    }

    public Object translateKey(T key) {
        return getCompleteView().indexOf(key);
    }
    
    @SuppressWarnings("unchecked")
    private T[] newArray(int size) {
        return (T[])Array.newInstance(availableItems.getClass().getComponentType(), size);
    }
}
