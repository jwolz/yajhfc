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