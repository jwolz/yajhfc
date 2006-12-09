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
 */
public abstract class PhoneBookEntry {
    
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
             if (givenname != null && givenname.length() > 0)
                 return givenname;
             else
                 return utils._("<no name>");
         }
     }
     
 }