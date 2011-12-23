package yajhfc.phonebook;
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

import java.awt.Dialog;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.phonebook.convrules.ChoiceRule;
import yajhfc.phonebook.convrules.ConcatRule;
import yajhfc.phonebook.convrules.EntryToStringRule;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * Abstract class describing a phone book
 * Does not need to be sorted (use a PhonebookSorter if you need this).
 * 
 * Child Classes implementing an actual Phonebook *must* have the following fields
 * _or_ override getPrefix(), getDescription() and getDisplayName() and create their
 * own PhoneBookType subclass.
 * 
 * public static String PB_Prefix;      // The prefix of this Phonebook type's descriptor
 * public static String PB_DisplayName; // A user-readable name for this Phonebook type
 * public static String PB_Description; // A user-readable description of this Phonebook type
 * */

public abstract class PhoneBook implements PhoneBookEntryList {

    public static final EntryToStringRule DEFAULT_TOSTRINGRULE = toStringRuleFromNameRule(NameRule.GIVENNAME_NAME);
    
    public static EntryToStringRule toStringRuleFromNameRule(EntryToStringRule nameRule) {
        return new ChoiceRule(
                nameRule,
                new ConcatRule(PBEntryField.Department, ", ", PBEntryField.Company),
                new ConcatRule(Utils._("<no name>")));
    }
    
    protected EntryToStringRule entryToStringRule = DEFAULT_TOSTRINGRULE;
    
    protected List<PhonebookEventListener> listeners = new ArrayList<PhonebookEventListener>();
    
    protected static final int CAPTION_LENGTH = 40;
    
    /***
     * The dialog to be used as parent when a UI is shown.
     */
    public final Dialog parentDialog;
    
    protected String strDescriptor;
    
    /**
     * Field for internal use by the PhoneBookTreeModel to save the result of
     * the last filtering operation
     */
    public Object treeModelData = null;
    
    public String getDescriptor() {
        return strDescriptor;
    }
    
    public String getPrefix() {
        try {
            Field f = getClass().getField("PB_Prefix");
            return (String)f.get(null);
        } catch (Exception e) {
            return getClass().getCanonicalName() ;
        }
    }
    
    public String getDisplayName() {
        try {
            Field f = getClass().getField("PB_DisplayName");
            return (String)f.get(null);
        } catch (Exception e) {
            return getClass().getName();
        }
    }
    
    public String getDescription() {
        try {
            Field f = getClass().getField("PB_Description");
            return (String)f.get(null);
        } catch (Exception e) {
            return getClass().toString();
        }
    }
    
    /**
     * Searches for an entry in the phone book.
     * @param startIndex
     * The index to start searching at. Use -1 to start from the beginning (first or last item depending on searchBackwards)
     * @param searchBackwards
     * Search beginning at last item (true) in direction to the first one or beginning at the first item
     * @param filter
     * The filter specifying the condition to search for
     * @return 
     * The index of the matching index or -1 if none was found
     */
    public int findEntry(int startIndex, boolean searchBackwards, Filter<PhoneBookEntry,PBEntryField> filter) {
        List<PhoneBookEntry> entries = getEntries();
        int i, size, increment;
        
        filter.initFilter(PBEntryField.filterKeyList);
        
        size = entries.size();
        
        if (startIndex < 0) {
            if (searchBackwards)
                i = size - 1;
            else
                i = 0;
        } else {
            i = startIndex;
        }
        increment = searchBackwards ? -1 : 1;
            
        for (; i >= 0 && i < size; i+=increment) {
            if (filter.matchesFilter(entries.get(i))) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Adds a new "normal" phone book entry
     * @return
     */
    public abstract PhoneBookEntry addNewEntry();
    /**
     * Adds a new distribution list
     * @return the new distribution list 
     * @throws UnsupportedOperationException if no distribution lists are supported
     */
    public DistributionList addDistributionList() {
        throw new UnsupportedOperationException("No distribution lists supported.");
    }
    
    /**
     * Adds a new distribution list with the specified entries and name
     * @return the new distribution list or null if distribution lists are unsupported
     */
    public DistributionList addDistributionList(String name, Collection<? extends PBEntryFieldContainer> entries) {
        DistributionList res = addDistributionList();
        res.addEntries(entries);
        res.setField(PBEntryField.Name, name);
        return res;
    }
    
    public void addEntries(Collection<? extends PBEntryFieldContainer> items) {
        for (PBEntryFieldContainer item : items) {
            if (item instanceof DistributionList) {
                List<PhoneBookEntry> entryList = ((DistributionList)item).getEntries();
                if (supportsDistributionLists()) {
                    addDistributionList(item.getField(PBEntryField.Name), entryList);
                } else {
                    addEntries(entryList); // Resolve the distribution list
                }
            } else {
                addNewEntry(item);
            }
        }
    }
    
    public PhoneBookEntry addNewEntry(PBEntryFieldContainer item) {
        PhoneBookEntry newEntry = addNewEntry();
        newEntry.copyFrom(item);
        newEntry.commit();
        return newEntry;
    }
    
//    public int indexOf(PhoneBookEntry pbe) {
//        for (int i = 0; i < getSize(); i++) {
//            if (getElementAt(i).equals(pbe))
//                return i;
//        }
//        return -1;
//    }
    
    // new interface (no more list model because of incompatibility of the removed event
    //     with the corresponding tree event):
//    public abstract int getSize();
//    
//    public abstract PhoneBookEntry getElementAt(int index);
    
    /**
     * Returns the (unmodifiable) List of all phone book entries when no filter is applied.
     */
    public abstract List<PhoneBookEntry> getEntries();
    
    
    
    /**
     * Returns the entries matching the specified filter.
     * This is implemented as a member of Phonebook to allow subclasses to use
     * a more efficient method of filtering (e.g. a LDAP search) than 
     * iterating all entries returned by getEntries().
     * @return
     */
    public List<PhoneBookEntry> applyFilter(Filter<PhoneBookEntry,PBEntryField> filter) {
        if (filter != null) {
            List<PhoneBookEntry> allEntries = getEntries();
            List<PhoneBookEntry> rv = new ArrayList<PhoneBookEntry>(allEntries.size());

            filter.initFilter(PBEntryField.filterKeyList);
            for (PhoneBookEntry entry : allEntries) {
                if (filter.matchesFilter(entry)){
                    rv.add(entry);
                }
            }
            return rv;
        } else {
            return null;
        }
    }
    
    public void addPhonebookEventListener(PhonebookEventListener pel) {
        listeners.add(pel);
    }
    
    public void removePhonebookEventListener(PhonebookEventListener pel) {
        listeners.remove(pel);
    }
    
    protected void fireEntriesAdded(int index, PhoneBookEntry pbe) {
        fireEntriesAdded(new PhonebookEvent(this, new PhoneBookEntry[] { pbe }, new int[] { index }));
    }
    
    protected void fireEntriesChanged(int index, PhoneBookEntry pbe) {
        fireEntriesChanged(new PhonebookEvent(this, new PhoneBookEntry[] { pbe }, new int[] { index }));
    }
    
    protected PhonebookEvent eventObjectForInterval(int intervalStart, int intervalEnd) {
        return PhonebookEvent.createForInterval(this, intervalStart, intervalEnd);
    }
    
    protected void fireEntriesRemoved(int index, PhoneBookEntry pbe) {
        fireEntriesRemoved(new PhonebookEvent(this, new PhoneBookEntry[] { pbe }, new int[] { index }));
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
    
    /**
     * Show dialog to select a new Phonebook.
     * Returns a descriptor if the user selected a valid one or null if user selects cancel
     * @param exportMode 
     */
    public abstract String browseForPhoneBook(boolean exportMode);
    
    public void open(String descriptor) throws PhoneBookException {
        int pos = descriptor.indexOf(':');
        if (pos >= 0) {
            strDescriptor = descriptor;
            openInternal(descriptor.substring(pos+1));
        } else {
            strDescriptor = getPrefix() + ":" + descriptor;
            openInternal(descriptor);
        }
    }
    
    public abstract boolean isOpen();
    
    protected abstract void openInternal(String descriptorWithoutPrefix) throws PhoneBookException;
    
    //public abstract void reloadEntries() throws PhoneBookException;
    
    public abstract void close();
    
    public boolean isFieldAvailable(PBEntryField field) {
        return true;
    }
    
    /**
     * Returns the maximum length of values for the given field or 0
     * if the length is unlimited
     * @param field
     * @return
     */
    public int getMaxLength(PBEntryField field) {
        return 0;
    }
    
    /**
     * Return a caption for display to users.
     * Should be shortened to CAPTION_LENGTH chars or less.
     * @return
     */
    public String getDisplayCaption() {
        return getDescriptor().substring(0, CAPTION_LENGTH);
    }
    
    @Override
    public String toString() {
        return getDisplayCaption();
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    /**
     * Returns true if this phone book supports the addition of distribution lists.
     * @return
     */
    public boolean supportsDistributionLists() {
        return false;
    }
    
    public EntryToStringRule getEntryToStringRule() {
        return entryToStringRule;
    }
    
    public void setEntryToStringRule(EntryToStringRule toStringRule) {
        if (toStringRule != entryToStringRule) {
            this.entryToStringRule = toStringRule; 
            for (PhoneBookEntry entry : getEntries()) {
                entry.refreshToStringRule();
            }
        }
    }
    
    public PhoneBook (Dialog parent) {
        this.parentDialog = parent;
    }
}