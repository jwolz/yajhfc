package yajhfc.phonebook;

import yajhfc.filters.FilterableObject;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

/**
 * 
 *  Phone book entry
 *  Phone book implementations can override this in order to
 *  save status information.
 *  New PhoneBookEntry classes are only to be created by (non-abstract) PhoneBook implementations.
 *  
 */
public abstract class PhoneBookEntry implements Comparable<PhoneBookEntry>, FilterableObject, PBEntryFieldContainer {
    
    public abstract PhoneBook getParent();
    
    public abstract String getField(PBEntryField field);
    public abstract void setField(PBEntryField field, String value);
    
    public Object getFilterData(Object key) {
        return getField((PBEntryField)key);
    }
    
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
    public void updateDisplay() {
        commit();
    }

    public void copyFrom(PhoneBookEntry other) {
        for (PBEntryField field : PBEntryField.values()) {
            setField(field, other.getField(field));
        }
    }

    public void refreshToStringRule() {
        // Do nothing here...
    }
    
    public String toString() {
        return getParent().getEntryToStringRule().applyRule(this);
//        String surname = getField(PBEntryField.Name);
//        String givenname = getField(PBEntryField.GivenName);
//
//        if (surname != null && surname.length() > 0) {
//            if (givenname != null && givenname.length() > 0)
//                //return surname + ", " + givenname;
//                return MessageFormat.format(Utils._("{0} {1}"), givenname, surname);
//            else
//                return surname;
//        } else {
//            if (givenname != null && givenname.length() > 0) {
//                return givenname;
//            } else {
//                String company = getField(PBEntryField.Company);
//                if (company != null && company.length() > 0) {
//                    return company;
//                } else {
//                    return Utils._("<no name>");
//                }
//            }
//        }
    }

    // The order items are compared in compareTo
    protected static final PBEntryField[] sortOrder = {
        PBEntryField.Name, PBEntryField.GivenName, PBEntryField.Company, PBEntryField.Location, PBEntryField.Title, PBEntryField.FaxNumber, PBEntryField.VoiceNumber, PBEntryField.Comment
    };
    public int compareTo(PhoneBookEntry o) {
        for (PBEntryField entry : sortOrder) {
            String val1 = getField(entry);
            String val2 = o.getField(entry);
            int cmp;
            if (val1 != null) {
                if (val2 != null) {
                    cmp = val1.compareToIgnoreCase(val2);
                } else {
                    cmp = 1;
                }
            } else {
                if (val2 != null) {
                    cmp = -1;
                } else {
                    cmp = 0;
                }
            }
            
            if (cmp != 0) {
                return cmp;
            }
        }
        
        return 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhoneBookEntry) {
            return compareTo((PhoneBookEntry)obj) == 0;
        } else {
            return false;
        }
    }
}