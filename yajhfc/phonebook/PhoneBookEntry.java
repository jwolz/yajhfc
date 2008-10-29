package yajhfc.phonebook;

import java.text.MessageFormat;

import yajhfc.utils;

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
public abstract class PhoneBookEntry implements Comparable<PhoneBookEntry>{

    public enum PBEntryField {
        Name(utils._("Name")),
        GivenName(utils._("Given name")),
        Title(utils._("Title")),
        Company(utils._("Company")),
        Location(utils._("Location")),
        VoiceNumber(utils._("Voice number")),
        FaxNumber(utils._("Fax number")),
        Comment(utils._("Comments"))
        ;
        
        private String desc;
        private PBEntryField(String desc) {
            this.desc = desc;
        }
        
        public String getDescription() {
            return desc;
        }
        
        public String toString() {
            return desc;
        }
    }
    
    public String getPBField(PBEntryField field) {
        switch (field) {
        case Name:
            return getName();
        case GivenName:
            return getGivenName();
        case Title:
            return getTitle();
        case Company:
            return getCompany();
        case Location:
            return getLocation();
        case VoiceNumber:
            return getVoiceNumber();
        case FaxNumber:
            return getFaxNumber();
        case Comment:
            return getComment();
        default:
            return null;
        }
    }
    
    public abstract String getName();
    public abstract void setName(String newName);

    public abstract String getGivenName();
    public abstract void setGivenName(String newGivenName);

    public abstract String getTitle();
    public abstract void setTitle(String newTitle);

    public abstract String getCompany();
    public abstract void setCompany(String newCompany);

    public abstract String getLocation();
    public abstract void setLocation(String newLocation);

    public abstract String getVoiceNumber();
    public abstract void setVoiceNumber(String newVoiceNumber);

    public abstract String getFaxNumber();
    public abstract void setFaxNumber(String newFaxNumber);

    public abstract String getComment();
    public abstract void setComment(String newComment);

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
        setName(other.getName());
        setGivenName(other.getGivenName());
        setTitle(other.getTitle());
        setCompany(other.getCompany());
        setLocation(other.getLocation());
        setVoiceNumber(other.getVoiceNumber());
        setFaxNumber(other.getFaxNumber());
        setComment(other.getComment());
    }

    public String toString() {
        String surname = getName();
        String givenname = getGivenName();

        if (surname != null && surname.length() > 0) {
            if (givenname != null && givenname.length() > 0)
                //return surname + ", " + givenname;
                return MessageFormat.format(utils._("{0} {1}"), givenname, surname);
            else
                return surname;
        } else {
            if (givenname != null && givenname.length() > 0) {
                return givenname;
            } else {
                String company = getCompany();
                if (company != null && company.length() > 0) {
                    return company;
                } else {
                    return utils._("<no name>");
                }
            }
        }
    }

    // The order items are compared in compareTo
    protected static final PBEntryField[] sortOrder = {
        PBEntryField.Name, PBEntryField.GivenName, PBEntryField.Company, PBEntryField.Location, PBEntryField.Title, PBEntryField.FaxNumber, PBEntryField.VoiceNumber, PBEntryField.Comment
    };
    public int compareTo(PhoneBookEntry o) {
        for (PBEntryField entry : sortOrder) {
            String val1 = getPBField(entry);
            String val2 = o.getPBField(entry);
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