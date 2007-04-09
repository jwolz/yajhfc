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

import yajhfc.phonebook.GeneralConnectionSettings;


public class LDAPSettings extends GeneralConnectionSettings {
    public String serverName = "suse101-vmw";
    public int port = 389;
    public String baseDN = "o=Scalix";
    public boolean useAuth = false;
    public String bindDN = "";
    public String credential = "";
    public boolean askForCredential = false;
    
    public String objectFilter = "";
    public boolean searchSubTree = true;
    
    public String[] getAttributes() {
        int count = 0;
        for (int i=0; i < FIELD_COUNT; i++) 
            if (!isNoField(getMappingFor(i)))
                count++;
        
        String[] rv = new String[count];
        int j = 0;
        for (int i=0; i < FIELD_COUNT; i++) {
            String mapping = getMappingFor(i);
            if (!isNoField(mapping)) 
                rv[j++] = mapping;
        }
        
        return rv;
    }
    
    public LDAPSettings() {
        super();
        name = "surname";
        givenName = "givenName";
        location = "l";
        title = "title";
        company = "o";
        voiceNumber = "telephoneNumber";
        faxNumber = "facsimileTelephoneNumber";
        comment = "";
    }
    
    public LDAPSettings(String serialized) {
        this();
        loadFromString(serialized);
    }
    public LDAPSettings(LDAPSettings src) {
        this();
        copyFrom(src);
    }
}
