package yajhfc.phonebook;
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

import java.awt.Dialog;
import java.lang.reflect.Field;

import javax.swing.AbstractListModel;

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
    
    public abstract PhoneBookEntry addNewEntry();
    
    // read entry
    public PhoneBookEntry readEntry(int index) {
        return (PhoneBookEntry)getElementAt(index);
    }
    
    /**
     * Show dialog to select a new Phonebook.
     * Returns a descriptor if the user selected a valid one or null if user selects cancel
     */
    public abstract String browseForPhoneBook();
    
    public void openDefault() {
        // do nothing...
        // (this method only needs to get implemented if the phone book is a candidate 
        //  to be returned by PhoneBookFactory.createDefault())
    }
    
    public void open(String descriptor) {
        int pos = descriptor.indexOf(':');
        if (pos >= 0) {
            strDescriptor = descriptor;
            openInternal(descriptor.substring(pos+1));
        } else {
            strDescriptor = getPrefix() + ":" + descriptor;
            openInternal(descriptor);
        }
    }
    
    public abstract void resort();
    
    protected abstract void openInternal(String descriptorWithoutPrefix);
    
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
    
    public PhoneBook (Dialog parent) {
        this.parentDialog = parent;
    }
}