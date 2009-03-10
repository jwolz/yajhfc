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
import java.util.List;

import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * @author jonas
 *
 */
public interface PhoneBookEntryList {
    
    /**
     * Returns the unmodifiable list of child entries in this distribution list.
     * @return
     */
    public List<PhoneBookEntry> getEntries();
    
    /**
     * Adds several new entries to the distribution list
     * @param item
     */
    public void addEntries(Collection<? extends PBEntryFieldContainer> items);
    
    /**
     * Adds a new empty entry to the distribution list
     * @param item
     */
    public PhoneBookEntry addNewEntry();
    
    /**
     * Adds a new entry to the distribution list
     * @param item
     */
    public PhoneBookEntry addNewEntry(PBEntryFieldContainer item);
    
    /**
     * Adds a listener which is notified when items are added/modified/removed 
     * @param pel
     */
    public void addPhonebookEventListener(PhonebookEventListener pel);
    
    /**
     * Removes a listener which is notified when items are added/modified/removed 
     * @param pel
     */
    public void removePhonebookEventListener(PhonebookEventListener pel);
    
    /**
     * Returns if this list of phone book entries may be modified.
     * @return
     */
    public boolean isReadOnly();
}
