/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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

import java.util.EventObject;

/**
 * @author jonas
 *
 */
public class PhonebookEvent extends EventObject {
    protected final int[] indices;
    protected final PhoneBookEntry[] entries;
    
    public PhonebookEvent(PhoneBook source, PhoneBookEntry[] entries, int[] indices) {
        super(source);
        this.entries = entries;
        this.indices = indices;
    }
    
    /**
     * The source phone book
     * @return
     */
    public PhoneBook getPhonebook() {
        return (PhoneBook)source;
    }

    /**
     * The affected indices
     * @return
     */
    public int[] getIndices() {
        return indices;
    }

    /**
     * The affected entries
     * @return
     */
    public PhoneBookEntry[] getEntries() {
        return entries;
    }
}
