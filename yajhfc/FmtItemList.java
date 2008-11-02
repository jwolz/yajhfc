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
public class FmtItemList extends ArrayList<FmtItem> implements FilterKeyList<FmtItem> {
    private static final Logger log = Logger.getLogger(FmtItemList.class.getName());
    
    protected FmtItem[] availableItems;
    protected FmtItem[] obligateItems;
    protected List<FmtItem> completeView;

    /**
     * Returns a view of this list that has the obligateItems appended to the end if necessary.
     * @return
     */
    public List<FmtItem> getCompleteView() {
        if (completeView == null) {
            FmtItem[] additionalItems = new FmtItem[obligateItems.length];
            int arrayPtr = 0;
            for (FmtItem fi : obligateItems) {
                if (!this.contains(fi)) {
                    additionalItems[arrayPtr++] = fi;
                }
            }
            if (arrayPtr > 0) {
                completeView = new CompleteView(additionalItems, arrayPtr);
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
        return utils.listToString(getCompleteView(), "|");
    }
    
    private static final char sep = '|';
    
    /**
     * Creates a String representing the content of this list for storage 
     * @return
     */
    public String saveToString() {
        StringBuilder saveval = new StringBuilder();
        for (FmtItem fi : this) {
            saveval.append(fi.fmt).append(sep);
        }
        return saveval.toString();
    }
    
    /**
     * Loads the content previously saved by saveToString()
     * @param saved
     */
    public void loadFromString(String saved) {
        String[] fields = utils.fastSplit(saved, sep);
       
        this.clear();
        
        for (int i=0; i < fields.length; i++) {
            FmtItem res = (FmtItem)utils.findInArray(availableItems, fields[i]);
            if (res == null) {
                log.log(Level.WARNING, "FmtItem for " + fields[i] + "not found.");
            } else {
                if (!this.contains(res)) {
                    this.add(res);
                }
            }
        }
    }
    
    // Override methods modifying the list to reset the complete view
    @Override
    public boolean add(FmtItem o) {
        completeView = null;
        return super.add(o);
    }

    @Override
    public void add(int index, FmtItem element) {
        completeView = null;
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends FmtItem> c) {
        completeView = null;
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends FmtItem> c) {
        completeView = null;
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        completeView = null;
        super.clear();
    }

    @Override
    public FmtItem remove(int index) {
        completeView = null;
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        completeView = null;
        return super.remove(o);
    }

    @Override
    public FmtItem set(int index, FmtItem element) {
        completeView = null;
        return super.set(index, element);
    }

    public FmtItemList(FmtItem[] allItems, FmtItem[] obligateItems) {
        super();
        this.availableItems = allItems;
        this.obligateItems = obligateItems;
    }
    
    protected class CompleteView extends AbstractList<FmtItem> {
        protected FmtItem[] additionalItems;
        protected int additionalSize; 
        
        @Override
        public FmtItem get(int index) {
            int additionalIdx = index - FmtItemList.this.size();
            if (additionalIdx < 0) {
                return FmtItemList.this.get(index);
            } else if (additionalIdx < additionalSize){
                return additionalItems[additionalIdx];
            } else {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }

        @Override
        public int size() {
            return FmtItemList.this.size() + additionalSize;
        }
        
        public CompleteView(FmtItem[] additionalItems, int additionalSize) {
            this.additionalItems = additionalItems;
            this.additionalSize = additionalSize;
        }
    }

    public boolean containsKey(FmtItem key) {
        return getCompleteView().contains(key);
    }

    public FmtItem[] getAvailableKeys() {
        return getCompleteView().toArray(new FmtItem[getCompleteView().size()]);
    }

    public Object translateKey(FmtItem key) {
        return getCompleteView().indexOf(key);
    }
}
