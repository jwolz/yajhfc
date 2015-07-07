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
package yajhfc.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
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
    protected Map<T,Integer> itemIndices;
    protected Map<VirtualColumnType,Integer> virtualColumnIndexes;

    protected boolean haveItemMap = false;

    /**
     * Returns a view of this list that has the obligateItems appended to the end if necessary.
     * @return
     */
    public List<T> getCompleteView() {
        if (completeView == null) {
            itemIndices.clear();
            virtualColumnIndexes.clear();
            // Rebuild map
            for (int i = 0; i < size(); i++) {
                final T item = get(i);
                final Integer index = Integer.valueOf(i);
                
                itemIndices.put(item, index);
                if (item.getVirtualColumnType() != VirtualColumnType.NONE)
                    virtualColumnIndexes.put(item.getVirtualColumnType(), index);
                
            }
            haveItemMap = true;
            
            List<T> completeView = new ArrayList<T>(size() + obligateItems.length) {
                @Override
                public int indexOf(Object elem) {
                    return indexOfFromMap(elem);
                }
                
                @Override
                public boolean contains(Object elem) {
                    return itemIndices.containsKey(elem);
                }
            };
            completeView.addAll(this);
            for (T fi : obligateItems) {
                if (!this.contains(fi)) {
                    completeView.add(fi);
                    
                    final Integer index = Integer.valueOf(completeView.size()-1);
                    itemIndices.put(fi, index);
                    if (fi.getVirtualColumnType() != VirtualColumnType.NONE)
                        virtualColumnIndexes.put(fi.getVirtualColumnType(), index);
                }
            }
            if (completeView.size() > size()) {
                this.completeView = completeView;
            } else {
                this.completeView = this; // Optimization: If all required items are present, the "full view" is identical
            }
        }
        return completeView;
    }
    
    /**
     * Returns a format string suitable for the RECVFMT/JOBFMT commands.
     * 
     * @return
     */
    public String getFormatString(char splitChar, String prefix) {
        StringBuilder res = new StringBuilder(prefix);
        for (T item : getCompleteView()) {
            if (item.getHylaFmt() != null)
                res.append('%').append(item.getHylaFmt());
            res.append(splitChar);
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
                this.add(res);
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
        resetCompleteView();
        return super.add(o);
    }

    @Override
    public void add(int index, T element) {
        resetCompleteView();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        resetCompleteView();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        resetCompleteView();
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        resetCompleteView();
        super.clear();
    }

    @Override
    public T remove(int index) {
        resetCompleteView();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        resetCompleteView();
        return super.remove(o);
    }

    @Override
    public T set(int index, T element) {
        resetCompleteView();
        return super.set(index, element);
    }

    protected void resetCompleteView() {
        completeView = null;
        itemIndices.clear();
        virtualColumnIndexes.clear();
        haveItemMap = false;
    }

    protected int indexOfFromMap(Object elem) {
        if (!haveItemMap) {
            getCompleteView(); // Build the mapping
        }
        Integer mapping = itemIndices.get(elem);
        if (mapping == null)
            return -1;
        else
            return mapping.intValue();
    }
    
    @Override
    public int indexOf(Object elem) {
        int index = indexOfFromMap(elem);
        if (index >= size()) // Only in complete view
            return -1;
        else
            return index;
    }
    
    /**
     * Returns the index if the respective virtual column in the complete view.
     * @param vtCol
     * @return the index or -1 if the columnn is not present.
     */
    public int getVirtualColumnIndex(VirtualColumnType vtCol) {
        if (!haveItemMap) {
            getCompleteView(); // Build the mapping
        }
        Integer mapping = virtualColumnIndexes.get(vtCol);
        if (mapping == null)
            return -1;
        else
            return mapping.intValue();
    }
    
    public Map<VirtualColumnType, Integer> getVirtualColumnIndexes() {
        return virtualColumnIndexes;
    }
    
    @SuppressWarnings("unchecked")
    public FmtItemList(T[] allItems, T[] obligateItems) {
        super();
        this.availableItems = allItems;
        this.obligateItems = obligateItems;
        
        Class<T> itemClass = getItemClass();
        if (Enum.class.isAssignableFrom(itemClass)) {
            itemIndices = new EnumMap(itemClass);
        } else {
            itemIndices = new HashMap<T,Integer>();
        }
        virtualColumnIndexes = new EnumMap<VirtualColumnType,Integer>(VirtualColumnType.class);
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
        return (T[])Array.newInstance(getItemClass(), size);
    }
    
    @SuppressWarnings("unchecked")
    private Class<T> getItemClass() {
        return (Class<T>)availableItems.getClass().getComponentType();
    }
}
