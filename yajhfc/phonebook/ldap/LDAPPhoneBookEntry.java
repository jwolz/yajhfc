package yajhfc.phonebook.ldap;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import yajhfc.phonebook.PhoneBookEntry;

public class LDAPPhoneBookEntry extends PhoneBookEntry {

    private String[] data;
    //private LDAPPhoneBook parent;
    
    LDAPPhoneBookEntry(LDAPPhoneBook parent, SearchResult res){
        //this.parent = parent;
        
        Attributes attrs = res.getAttributes();
        LDAPSettings settings = parent.settings;
        
        data = new String[LDAPSettings.FIELD_COUNT];
        
        for (int i=0; i < LDAPSettings.FIELD_COUNT; i++) {
            String attrname = settings.getMappingFor(i);
            if (attrname == null) {
                data[i] = "";
            } else {
                Attribute a = attrs.get(attrname);
                if (a != null) {
                    Object val = null;
                    try {
                        val = a.get();
                    } catch (NamingException e) {
                        //NOP
                    }
                    if (val != null) {
                        data[i] = val.toString().trim();
                    } else {
                        data[i] = "";
                    }
                } else {
                    data[i] = "";
                }
            }
        }
        
    }

    @Override
    public void commit() {
       //NOP
    }

    @Override
    public void delete() {
        //NOP
    }

    @Override
    public String getComment() {
        return data[LDAPSettings.COMMENT_FIELD];
    }

    @Override
    public String getCompany() {
        return data[LDAPSettings.COMPANY_FIELD];
    }

    @Override
    public String getFaxNumber() {
        return data[LDAPSettings.FAXNUMBER_FIELD];
    }

    @Override
    public String getGivenName() {
        return data[LDAPSettings.GIVENNAME_FIELD];
    }

    @Override
    public String getLocation() {
        return data[LDAPSettings.LOCATION_FIELD];
    }

    @Override
    public String getName() {
        return data[LDAPSettings.SURNAME_FIELD];
    }

    @Override
    public String getTitle() {
        return data[LDAPSettings.TITLE_FIELD];
    }

    @Override
    public String getVoiceNumber() {
        return data[LDAPSettings.VOICENUMBER_FIELD];
    }

    @Override
    public void setComment(String newComment) {
    }

    @Override
    public void setCompany(String newCompany) {
    }

    @Override
    public void setFaxNumber(String newFaxNumber) {
    }

    @Override
    public void setGivenName(String newGivenName) {
    }

    @Override
    public void setLocation(String newLocation) {
    }

    @Override
    public void setName(String newName) {
    }

    @Override
    public void setTitle(String newTitle) {
    }

    @Override
    public void setVoiceNumber(String newVoiceNumber) {
    }

}
