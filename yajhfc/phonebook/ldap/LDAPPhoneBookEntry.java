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

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.SimplePhoneBookEntry;

public class LDAPPhoneBookEntry extends SimplePhoneBookEntry {
    
    LDAPPhoneBookEntry(LDAPPhoneBook parent, SearchResult res){
        
        Attributes attrs = res.getAttributes();
        LDAPSettings settings = parent.settings;
              
        for (PBEntryField field : PBEntryField.values()) {
            String attrname = settings.getMappingFor(field);
            if (attrname == null) {
                setFieldUndirty(field, "");
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
                        setFieldUndirty(field, val.toString().trim());
                    } else {
                        setFieldUndirty(field, "");
                    }
                } else {
                    setFieldUndirty(field, "");
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
    public void setField(PBEntryField field, String value) {
        //NOP
    }

}
