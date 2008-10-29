package yajhfc.phonebook;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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

public abstract class SimplePhoneBookEntry extends PhoneBookEntry {

    public String surname;
    public String givenname;
    public String title;
    public String company;
    public String location;
    public String voicenumber;
    public String faxnumber;
    public String comment;
    
    protected boolean dirty = false;
    
    @Override
    public String getName() {
        return surname;
    }

    @Override
    public String getGivenName() {
        return givenname;
    }

    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getVoiceNumber() {
        return voicenumber;
    }

    @Override
    public String getFaxNumber() {
        return faxnumber;
    }
    
    @Override
    public String getComment() {
        return comment;
    }
    
    @Override
    public void setComment(String newComment) {
        if (!newComment.equals(comment)) {
            dirty = true;
            comment = newComment;
        }     
    }

    @Override
    public void setCompany(String newCompany) {
        if (!newCompany.equals(company)) {
            dirty = true;
            company = newCompany;
        }
    }

    @Override
    public void setFaxNumber(String newFaxNumber) {
        if (!newFaxNumber.equals(faxnumber)) {
            dirty = true;
            faxnumber = newFaxNumber;
        }
    }

    @Override
    public void setGivenName(String newGivenName) {
        if (!newGivenName.equals(givenname)) {
            dirty = true;
            givenname = newGivenName;
        }
    }

    @Override
    public void setLocation(String newLocation) {
        if (!newLocation.equals(location)) {
            dirty = true;
            location = newLocation;
        }
    }

    @Override
    public void setName(String newName) {
        if (!newName.equals(surname)) {
            dirty = true;
            surname = newName;
        }
    }

    @Override
    public void setTitle(String newTitle) {
        if (!newTitle.equals(title)) {
            dirty = true;
            title = newTitle;
        }
    }

    @Override
    public void setVoiceNumber(String newVoiceNumber) {
        if (!newVoiceNumber.equals(voicenumber)) {
            dirty = true;
            voicenumber = newVoiceNumber;
        }
    } 

    public boolean isDirty() {
        return dirty;
    }
}
