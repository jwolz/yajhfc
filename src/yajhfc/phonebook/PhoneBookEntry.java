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
 */
package yajhfc.phonebook;

import yajhfc.filters.FilterableObject;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * @author jonas
 *
 */
public interface PhoneBookEntry extends Comparable<PhoneBookEntry>, FilterableObject, PBEntryFieldContainer {
    
    public abstract PhoneBook getParent();
    
    /**
     * Deletes this entry from the phonebook
     */
    public abstract void delete();

    /**
     * Commits all changes made by the get/set-Methods
     */
    public abstract void commit();

    /**
     * Just update the displayed position (don't necessarily write through)
     */
    public void updateDisplay();

    public void copyFrom(PBEntryFieldContainer other);

    /**
     * Refreshes the return value of toString()
     */
    public void refreshToStringRule();
}
