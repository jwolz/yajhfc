/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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

import java.util.Collection;
import java.util.Collections;

import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.send.PBEntryFieldTableModel;

/**
 * @author jonas
 *
 */
public class PhoneBookTableModel extends PBEntryFieldTableModel implements
        PhonebookEventListener {

    protected PhoneBookEntryList phoneBook;
    
    /**
     * @param backingList
     */
    public PhoneBookTableModel(PhoneBookEntryList phoneBook) {
        super(null);
        setPhoneBook(phoneBook);
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookEventListener#elementsAdded(yajhfc.phonebook.PhonebookEvent)
     */
    public void elementsAdded(PhonebookEvent e) {
        int startIdx = -2;
        int endIdx = -2;
        for (int idx : e.indices) {
            //Try to find continuous intervals
            if ((idx - endIdx) > 1) {
                if (endIdx >= 0) {
                    fireTableRowsInserted(startIdx, endIdx);
                }
                startIdx = endIdx = idx;
            } else {
                endIdx = idx;
            }
        }
        if (endIdx >= 0) {
            fireTableRowsInserted(startIdx, endIdx);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookEventListener#elementsChanged(yajhfc.phonebook.PhonebookEvent)
     */
    public void elementsChanged(PhonebookEvent e) {
        int startIdx = -2;
        int endIdx = -2;
        for (int idx : e.indices) {
            //Try to find continuous intervals
            if ((idx - endIdx) > 1) {
                if (endIdx >= 0) {
                    fireTableRowsUpdated(startIdx, endIdx);
                }
                startIdx = endIdx = idx;
            } else {
                endIdx = idx;
            }
        }
        if (endIdx >= 0) {
            fireTableRowsUpdated(startIdx, endIdx);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookEventListener#elementsRemoved(yajhfc.phonebook.PhonebookEvent)
     */
    public void elementsRemoved(PhonebookEvent e) {
        int startIdx = -2;
        int endIdx = -2;
        for (int idx : e.indices) {
            //Try to find continuous intervals
            if ((idx - endIdx) > 1) {
                if (endIdx >= 0) {
                    fireTableRowsDeleted(startIdx, endIdx);
                }
                startIdx = endIdx = idx;
            } else {
                endIdx = idx;
            }
        }
        if (endIdx >= 0) {
            fireTableRowsDeleted(startIdx, endIdx);
        }
    }

    @Override
    public void addRow() {
        phoneBook.addNewEntry();
    }

    @Override
    public void addRow(PBEntryFieldContainer item) {
        phoneBook.addNewEntry(item);
    }

    @Override
    public void addRows(Collection<? extends PBEntryFieldContainer> newItems) {
        phoneBook.addEntries(newItems);
    }
    
    @Override
    public void removeRow(int index) {
        phoneBook.getEntries().get(index).delete();
    }

    public PhoneBookEntryList getPhoneBook() {
        return phoneBook;
    }
    
    public void setPhoneBook(PhoneBookEntryList phoneBook) {
        if (this.phoneBook != null) {
            this.phoneBook.removePhonebookEventListener(this);
        }
        
        this.phoneBook = phoneBook;
        
        if (phoneBook == null) {
            this.list = null;
        } else {
            this.list = Collections.<PBEntryFieldContainer>unmodifiableList(phoneBook.getEntries());
            phoneBook.addPhonebookEventListener(this);
        }
        
        fireTableDataChanged();
    }
}
