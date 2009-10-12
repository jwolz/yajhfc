/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.phonebook;

import java.util.HashMap;
import java.util.Map;

import yajhfc.Utils;
import yajhfc.filters.FilterKey;
import yajhfc.filters.FilterKeyList;

public enum PBEntryField implements FilterKey {
    GivenName(Utils._("Given name"), "givenname", true),
    Name(Utils._("Name#(please translate as short form of surname)#", "Name"), "surname", true),
    Title(Utils._("Title"), "title", true),
    Company(Utils._("Company"), "company", true),
    Position(Utils._("Position"), "position", true),
    Department(Utils._("Department"), "department", true),
    Street(Utils._("Street"), "street", false),
    Location(Utils._("Location"), "location", true),
    Country(Utils._("Country"), "country", true),
    ZIPCode(Utils._("ZIP code"), "zipcode", true),
    State(Utils._("State/Region"), "state", true),
    EMailAddress(Utils._("e-mail address"), "email", true),
    WebSite(Utils._("Website"), "website", true),
    FaxNumber(Utils._("Fax number"), "faxnumber", true),
    VoiceNumber(Utils._("Voice number"), "voicenumber", true),
    Comment(Utils._("Comments"), "comment", false)
    ;
    // Classes to adapt when a field is added/removed:
    // - MarkupFaxcover
    // - FaxOptions
    // - LDAPSettings
    
    private final String desc;
    private final String key;
    private final boolean shortLength;
    
    private PBEntryField(String desc, String key, boolean shortLength) {
        this.desc = desc;
        this.key = key;
        this.shortLength = shortLength;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public String toString() {
        return desc;
    }

    public String getKey() {
        return key;
    }
    
    /**
     * Returns if the field's value has a short length (for automatic GUI creation)
     * @return
     */
    public boolean isShortLength() {
        return shortLength;
    }
    
    public Class<?> getDataType() {
        return String.class;
    }
    
    /**
     * The number of fields. Equals values().length, but is more efficient
     */
    public static final int FIELD_COUNT = values().length;
    
    public static final FilterKeyList<PBEntryField> filterKeyList = new FilterKeyList<PBEntryField>() {

        public boolean containsKey(PBEntryField key) {
            return true;
        }

        public PBEntryField[] getAvailableKeys() {
            return values();
        }

        public Object translateKey(PBEntryField key) {
            return key;
        }

        public PBEntryField getKeyForName(String name) {
            try {
                return Enum.valueOf(PBEntryField.class, name);
            } catch (Exception e) {
                return null;
            }
        }
        
    };

    private static Map<String,PBEntryField> keyToFieldMap;
    public static Map<String,PBEntryField> getKeyToFieldMap() {
        if (keyToFieldMap == null) {
            keyToFieldMap = new HashMap<String,PBEntryField>();
            for (PBEntryField field : PBEntryField.values()) {
                PBEntryField.keyToFieldMap.put(field.getKey(), field);
            }
        }
        return keyToFieldMap;
    }
    
}