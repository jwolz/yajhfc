package yajhfc.phonebook;
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

import java.awt.Dialog;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;

import yajhfc.filters.StringFilterOperator;

/**
 * Abstract class describing a phone book
 * Should be sorted.
 * 
 * Child Classes implementing an actual Phonebook *must* have the following fields
 * _or_ override getPrefix(), getDescription() and getDisplayName() and create their
 * own PhoneBookType subclass.
 * 
 * public static String PB_Prefix;      // The prefix of this Phonebook type's descriptor
 * public static String PB_DisplayName; // A user-readable name for this Phonebook type
 * public static String PB_Description; // A user-readable description of this Phonebook type
 * */

public abstract class PhoneBook extends AbstractListModel {
    
    /***
     * The dialog to be used as parent when a UI is shown.
     */
    public Dialog parentDialog = null;
    
    protected String strDescriptor;
    
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
     * @param caseSensitive
     * Should the search be case sensitive?
     * @param field
     * The phone book field to compare
     * @param op
     * The comparision operator
     * @param compareValue
     * The value to compare the field values with
     * @return 
     * The index of the matching index or -1 if none was found
     */
    public int findEntry(int startIndex, boolean searchBackwards, boolean caseSensitive, PhoneBookEntry.PBEntryField field, StringFilterOperator op, String compareValue) {
        Pattern regEx = null;
        int i, size, increment;
        
        if (op == StringFilterOperator.MATCHES) {
            regEx = Pattern.compile(compareValue, caseSensitive ? 0 : (Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE) );
        } else if (!caseSensitive) {
            compareValue = compareValue.toLowerCase();
        }
         
        size = getSize();
        
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
            PhoneBookEntry pbe = readEntry(i);
            String val = pbe.getPBField(field);
            if (!caseSensitive)
                val = val.toLowerCase();
            
            switch (op) {
            case EQUAL:
                if (val.equals(compareValue))
                    return i;
                break;
            case NOTEQUAL:
                if (!val.equals(compareValue))
                    return i;
                break;
            case CONTAINS:
                if (val.contains(compareValue)) 
                    return i;
                break;
            case ENDSWITH:
                if (val.endsWith(compareValue))
                    return i;
                break;
            case STARTSWITH:
                if (val.startsWith(compareValue))
                    return i;
                break;
            case MATCHES:
                if (regEx.matcher(val).matches()) 
                    return i;
                break;   
            }
        }
        return -1;
    }
    
    public abstract PhoneBookEntry addNewEntry();
    
    // read entry
    public PhoneBookEntry readEntry(int index) {
        return (PhoneBookEntry)getElementAt(index);
    }
    
    public int indexOf(PhoneBookEntry pbe) {
        for (int i = 0; i < getSize(); i++) {
            if (readEntry(i).equals(pbe))
                return i;
        }
        return -1;
    }
    
    /**
     * Show dialog to select a new Phonebook.
     * Returns a descriptor if the user selected a valid one or null if user selects cancel
     */
    public abstract String browseForPhoneBook();
    
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
    
    public abstract void resort();
    
    protected abstract void openInternal(String descriptorWithoutPrefix) throws PhoneBookException;
    
    public abstract void close();
    
    public boolean isFieldNameAvailable() {
        return true;
    }
    public boolean isFieldGivenNameAvailable() {
        return true;
    }
    public boolean isFieldTitleAvailable() {
        return true;
    }
    public boolean isFieldCompanyAvailable() {
        return true;
    }
    public boolean isFieldLocationAvailable() {
        return true;
    }
    public boolean isFieldVoiceNumberAvailable() {
        return true;
    }
    public boolean isFieldFaxNumberAvailable() {
        return true;
    }
    public boolean isFieldCommentAvailable() {
        return true;
    }
    
    /**
     * Return a caption for display to users.
     * Should be shortened to 30 chars or less.
     * @return
     */
    public String getDisplayCaption() {
        return getDescriptor().substring(0, 30);
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    public PhoneBook (Dialog parent) {
        this.parentDialog = parent;
    }
}