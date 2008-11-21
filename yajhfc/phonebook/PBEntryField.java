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

import yajhfc.utils;
import yajhfc.filters.FilterKey;
import yajhfc.filters.FilterKeyList;

public enum PBEntryField implements FilterKey {
    GivenName(utils._("Given name"), "givenname", true),
    Name(utils._("Name"), "surname", true),
    Title(utils._("Title"), "title", true),
    Company(utils._("Company"), "company", true),
    Location(utils._("Location"), "location", false),
    VoiceNumber(utils._("Voice number"), "voicenumber", false),
    FaxNumber(utils._("Fax number"), "faxnumber", false),
    Comment(utils._("Comments"), "comment", false)
    ;
    
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
        
    };
}