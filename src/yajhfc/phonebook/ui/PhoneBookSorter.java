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
package yajhfc.phonebook.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookEntryList;
import yajhfc.phonebook.PhonebookEvent;
import yajhfc.phonebook.PhonebookEventListener;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * @author jonas
 *
 */
public class PhoneBookSorter implements PhonebookEventListener, PhoneBookEntryList {
    protected final PhoneBook phoneBook;
    protected final ArrayList<Row> originalList = new ArrayList<Row>();
    protected final ArrayList<Row> sortedList = new ArrayList<Row>();
    protected final List<PhoneBookEntry> sortedView = Collections.<PhoneBookEntry>unmodifiableList(sortedList);
    protected List<PhoneBookEntry> filteredList = null;
    protected final List<PhonebookEventListener> listeners = new ArrayList<PhonebookEventListener>();
    
    protected RowComparator comparator;
    
    protected Filter<PhoneBookEntry,PBEntryField> filter;
  
    private static final Comparator<Row> SORT_INDEX_COMPARATOR = new Comparator<PhoneBookSorter.Row>() {
        public int compare(Row o1, Row o2) {
            return o1.sortedIndex - o2.sortedIndex;
        }
    };
    private static final Row[] dummy = new Row[0];
    
    public PhoneBookSorter(PhoneBook phoneBook) {
        this(phoneBook, null, null);
    }
    
    public PhoneBookSorter(PhoneBook phoneBook, Comparator<PhoneBookEntry> comparator, Filter<PhoneBookEntry,PBEntryField> filter) {
        super();
        this.phoneBook = phoneBook;
        this.comparator = (comparator == null) ? null : new RowComparator(comparator);
        this.filter = filter;
        
        refresh();
        phoneBook.addPhonebookEventListener(this);
    }

    public PhoneBook getPhoneBook() {
        return phoneBook;
    }
    
    public Comparator<PhoneBookEntry> getComparator() {
        return comparator.wrapped;
    }
    
    public Filter<PhoneBookEntry, PBEntryField> getFilter() {
        return filter;
    }
    
    public void setFilter(Filter<PhoneBookEntry, PBEntryField> filter) {
        if (this.filter != filter) {
            this.filter = filter;
            
            List<PhoneBookEntry> oldEntries = getEntries();
            refreshFilter();
            if (!Utils.listQuickEquals(oldEntries, getEntries())) {
                firePhonebookReloaded();
            }
        }
    }

    
    public void setComparator(Comparator<PhoneBookEntry> comparator) {
        if (this.comparator == null && comparator != null ||
                this.comparator.wrapped != comparator) {
            this.comparator = (comparator == null) ? null : new RowComparator(comparator);
            
            refreshRowSort(originalList.toArray(dummy));
            refreshFilterSort();
            firePhonebookReloaded();
        }
    }
    
//    private Row getRowForEntry(PhoneBookEntry e) {
//        for (Row r : originalList) {
//            if (r.entry == e)
//                return r;
//        }
//        return null;
//    }
//    
    private static Row getRowForEntry(Row[] rows, PhoneBookEntry e) {
        for (Row r : rows) {
            if (r.entry == e)
                return r;
        }
        return null;
    }

    private void refreshFilter() {
        List<PhoneBookEntry> filteredEntries = phoneBook.applyFilter(filter);
        if (filteredEntries == null) {
            filteredList = null;
        } else {
            filteredList = new ArrayList<PhoneBookEntry>(filteredEntries.size());
            Row[] rows = originalList.toArray(new Row[originalList.size()]);
            for (PhoneBookEntry e : filteredEntries) {
                Row r = getRowForEntry(rows, e);
                if (r != null) {
                    filteredList.add(r);
                } else {
                    Logger.getLogger(PhoneBookSorter.class.getName()).warning("No Row found for entry: " + e + " (" + e.getClass() + ")");
                    filteredList.add(e);
                }
            }
        }
        refreshFilterSort();
    }
    
    private void refreshFilterSort() {
        if (filteredList != null) {
            if (comparator == null) {
                Collections.sort(filteredList);
            } else {
                Collections.sort(filteredList, comparator.wrapped);
            }
        }
    }
        
    /**
     * Refreshes and sorts the entries from the phone book
     */
    public void refresh() {
        final List<PhoneBookEntry> entries = phoneBook.getEntries();
        Row[] rows = new Row[entries.size()];
        originalList.clear();
        originalList.ensureCapacity(rows.length);
        int i=0;
        for (PhoneBookEntry entry : entries) {
            originalList.add(rows[i] = createRow(entry, i));
            i++;
        }

        refreshRowSort(rows);
        refreshFilter();
        
        firePhonebookReloaded();
    }
    
    private void refreshRowSort(Row[] rows) {
        int i;
        Arrays.sort(rows, comparator);

        sortedList.clear();
        sortedList.ensureCapacity(rows.length);
        for (i=0; i<rows.length; i++) {
            rows[i].sortedIndex = i;
            sortedList.add(rows[i]);
        }
    }
    
    /**
     * Creates an event object for the specified rows.
     * The array must be sorted in ascending order according to the sortedIndex
     * @param rows
     * @return
     */
    protected PhonebookEvent eventObjectForRows(Row[] rows) {
        int[] indices = new int[rows.length];
        PhoneBookEntry[] entries = new PhoneBookEntry[rows.length];
        
        for (int i=0; i<rows.length; i++) {
            indices[i] = rows[i].sortedIndex;
            entries[i] = rows[i].entry;
        }
        
        return new PhonebookEvent(this, entries, indices);
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookEventListener#elementsRemoved(yajhfc.phonebook.PhonebookEvent)
     */
    public void elementsRemoved(PhonebookEvent e) {
        if (e.getEntries().length > phoneBook.getEntries().size() / 2) {
            // If more than half the list has changed -> simply reload
            refresh();
        } else {
            int[] indices = e.getIndices();

            Row[] rows = new Row[indices.length];
            for (int i=indices.length-1; i>=0; i--) {
                rows[i] = originalList.remove(indices[i]);
            }
            
            Arrays.sort(rows, SORT_INDEX_COMPARATOR);
            for (int i=rows.length-1; i>=0; i--) {
                sortedList.remove(rows[i].sortedIndex);
            }
            for (int i=rows[0].sortedIndex; i<sortedList.size(); i++) {
                sortedList.get(i).sortedIndex = i;
            }
            
            if (isShowingFilteredResults()) {
                // Do something smarter here?
                refreshFilter();
            } else {
                fireEntriesRemoved(eventObjectForRows(rows));   
            }
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookEventListener#elementsAdded(yajhfc.phonebook.PhonebookEvent)
     */
    public void elementsAdded(PhonebookEvent e) {
        if (e.getEntries().length > phoneBook.getEntries().size() / 2) {
            // If more than half the list has changed -> simply reload
            refresh();
        } else {
            int[] indices = e.getIndices();
            PhoneBookEntry[] items = e.getEntries();
            Row[] rows = new Row[items.length]; 
            
            int minAffectedIndex = Integer.MAX_VALUE;
            int minOriginalIndex = Integer.MAX_VALUE;
            int originalSize = originalList.size();
            
            for (int i=0; i<items.length; i++) {
                final int originalIndex = indices[i];
                Row row = rows[i] = createRow(items[i], originalIndex);
                originalList.add(originalIndex, row);
                if (originalIndex < minOriginalIndex)
                    minOriginalIndex = originalIndex;
                
                int insertIndex = Utils.sortedInsert(sortedList, row, comparator);
                if (insertIndex < minAffectedIndex)
                    minAffectedIndex = insertIndex;
            }
            for (int i=minAffectedIndex; i<sortedList.size(); i++) {
                sortedList.get(i).sortedIndex = i;
            }
            if (minOriginalIndex < originalSize) {
                for (int i=minOriginalIndex; i<originalList.size(); i++) {
                    originalList.get(i).originalIndex = i;
                }
            }
            
            if (isShowingFilteredResults()) {
                // Do something smarter here?
                refreshFilter();
            } else {
                Arrays.sort(rows, SORT_INDEX_COMPARATOR);
                fireEntriesAdded(eventObjectForRows(rows));
            }
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookEventListener#elementsChanged(yajhfc.phonebook.PhonebookEvent)
     */
    public void elementsChanged(PhonebookEvent e) {
        if (isShowingFilteredResults() || e.getEntries().length > phoneBook.getEntries().size() / 2) {
            // If more than half has changed -> simply reload
            refreshRowSort(originalList.toArray(dummy));
            refreshFilter();
            firePhonebookReloaded();
        } else {
            int[] indices = e.getIndices();
            
            Row[] rows = new Row[indices.length];
            for (int i=0; i<indices.length; i++) {
                rows[i] = originalList.get(indices[i]);
            }
            Arrays.sort(rows, SORT_INDEX_COMPARATOR);
            
            for (int i=rows.length-1; i>=0; i--) {
                final int sortedIndex = rows[i].sortedIndex;
                sortedList.remove(sortedIndex);
            }
            // rows is sorted
            int minAffectedIndex = rows[0].sortedIndex;
            int maxAffectedIndex = rows[rows.length-1].sortedIndex;
            
            for (int i=0; i<rows.length; i++) {
                Row row = rows[i];
                int insertIndex = Utils.sortedInsert(sortedList, row, comparator);
                if (insertIndex < minAffectedIndex)
                    minAffectedIndex = insertIndex;
            }
            for (int i=minAffectedIndex; i<sortedList.size(); i++) {
                sortedList.get(i).sortedIndex = i;
            }
            
            for (int i=0; i<rows.length; i++) {
                final int sortedIndex = rows[i].sortedIndex;
                if (sortedIndex > maxAffectedIndex) 
                    maxAffectedIndex = sortedIndex;
            }

            fireEntriesChanged(PhonebookEvent.createForInterval(this, minAffectedIndex, maxAffectedIndex));
        }
    }
    
    public void phonebookReloaded(PhonebookEvent e) {
        refresh();
    }

    
    public List<PhoneBookEntry> getEntries() {
        if (isShowingFilteredResults())
            return filteredList;
        else                   
            return sortedView;
    }
    
    public List<PhoneBookEntry> getSortedEntries() {
        return sortedView;
    }
    
    public List<PhoneBookEntry> getFilteredEntries() {
        return filteredList;
    }
    
    public boolean isShowingFilteredResults() {
        return (filteredList != null);
    }

    public void addEntries(Collection<? extends PBEntryFieldContainer> items) {
        phoneBook.addEntries(items);
    }

    public PhoneBookEntry addNewEntry() {
        return phoneBook.addNewEntry();
    }

    public PhoneBookEntry addNewEntry(PBEntryFieldContainer item) {
        return phoneBook.addNewEntry(item);
    }

    public boolean isReadOnly() {
        return phoneBook.isReadOnly();
    }
    
    public void addPhonebookEventListener(PhonebookEventListener pel) {
        listeners.add(pel);
    }
    
    public void removePhonebookEventListener(PhonebookEventListener pel) {
        listeners.remove(pel);
    }
    
    protected void fireEntriesAdded(PhonebookEvent pbe) {
        for (PhonebookEventListener pel : listeners) {
            pel.elementsAdded(pbe);
        }
    }
    
    protected void fireEntriesChanged(PhonebookEvent pbe) {
        for (PhonebookEventListener pel : listeners) {
            pel.elementsChanged(pbe);
        }
    }
    
    protected void fireEntriesRemoved(PhonebookEvent pbe) {
        for (PhonebookEventListener pel : listeners) {
            pel.elementsRemoved(pbe);
        }
    }
    
    protected void firePhonebookReloaded() {
        firePhonebookReloaded(new PhonebookEvent(this, null, null));
    }
    
    protected void firePhonebookReloaded(PhonebookEvent pbe) {
        for (PhonebookEventListener pel : listeners) {
            pel.phonebookReloaded(pbe);
        }
    }
    
    @Override
    public String toString() {
        return phoneBook.toString();
    }
    
    /**
     * Detach this sorter from the underlying phone book
     */
    public void detach() {
        phoneBook.removePhonebookEventListener(this);
        sortedList.clear();
        originalList.clear();
        
        filteredList = null;
    }

    private static class RowComparator implements Comparator<Row> {
        public final Comparator<PhoneBookEntry> wrapped;
        
        public int compare(Row o1, Row o2) {
            return wrapped.compare(o1.entry, o2.entry);
        }

        public RowComparator(Comparator<PhoneBookEntry> wrapped) {
            super();
            this.wrapped = wrapped;
        }

    }
    
    static Row createRow(PhoneBookEntry entry, int originalIndex) {
        if (entry instanceof DistributionList)
            return new DistListRow((DistributionList)entry, originalIndex);
        return new Row(entry, originalIndex);
    }
    
    private static class Row implements Comparable<PhoneBookEntry>, PhoneBookEntry {
        public final PhoneBookEntry entry;
        public int originalIndex;
        public int sortedIndex = -1;
        
//        public int compareTo(Row o) {
//            return this.entry.compareTo(o.entry);
//        }
        
        public int compareTo(PhoneBookEntry o) {
            return entry.compareTo(o);
        }
        
        public String getField(PBEntryField field) {
            return entry.getField(field);
        }

        public Object getFilterData(Object key) {
            return entry.getFilterData(key);
        }

        public PhoneBook getParent() {
            return entry.getParent();
        }

        public void setField(PBEntryField field, String value) {
            entry.setField(field, value);
        }

        public void delete() {
            entry.delete();
        }

        public void commit() {
            entry.commit();
        }

        public void updateDisplay() {
            entry.updateDisplay();
        }

        public void copyFrom(PBEntryFieldContainer other) {
            entry.copyFrom(other);
        }

        public void refreshToStringRule() {
            entry.refreshToStringRule();
        }
        
        @Override
        public String toString() {
            return entry.toString();
        }

        @Override
        public boolean equals(Object obj) {
            // Special equals to make the JTree happy...
            if (obj == this) 
                return true;
            if (obj == this.entry)
                return true;
            return false;
        }
        
        public Row(PhoneBookEntry entry, int originalIndex) {
            super();
            this.entry = entry;
            this.originalIndex = originalIndex;
        }
    }
    
    private static class DistListRow extends Row implements DistributionList {
        
        public List<PhoneBookEntry> getEntries() {
            return ((DistributionList)entry).getEntries();
        }

        public void addEntries(Collection<? extends PBEntryFieldContainer> items) {
            ((DistributionList)entry).addEntries(items);
        }


        public PhoneBookEntry addNewEntry() {
            return ((DistributionList)entry).addNewEntry();
        }


        public PhoneBookEntry addNewEntry(PBEntryFieldContainer item) {
            return ((DistributionList)entry).addNewEntry(item);
        }


        public void addPhonebookEventListener(PhonebookEventListener pel) {
            ((DistributionList)entry).addPhonebookEventListener(pel);
        }


        public void removePhonebookEventListener(PhonebookEventListener pel) {
            ((DistributionList)entry).removePhonebookEventListener(pel);
        }


        public boolean isReadOnly() {
            return ((DistributionList)entry).isReadOnly();
        }


        public DistListRow(DistributionList entry, int originalIndex) {
            super(entry, originalIndex);
        }
        
    }
    
//    /**
//     * An unmodifiable view on the row list
//     * @author jonas
//     *
//     */
//    private static class RowToPBList extends AbstractList<PhoneBookEntry> {
//        protected final List<Row> wrapped;
//        
//        public RowToPBList(List<Row> wrapped) {
//            super();
//            this.wrapped = wrapped;
//        }
//
//        @Override
//        public PhoneBookEntry get(int index) {
//            return wrapped.get(index).entry;
//        }
//
//        @Override
//        public int size() {
//            return wrapped.size();
//        }
//        
//    }
}
