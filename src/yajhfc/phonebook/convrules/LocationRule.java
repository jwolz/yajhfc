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
package yajhfc.phonebook.convrules;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public enum LocationRule {
    LOCATION(new Object[0], new Object[0]),
    STREET_LOCATION(new Object[] { PBEntryField.Street, ", " }, new Object[0]),
    LOCATION_COUNTRY(new Object[0], new Object[] { ", ", PBEntryField.Country } ),
    STREET_LOCATION_COUNTRY(new Object[] { PBEntryField.Street, ", " }, new Object[] { ", ", PBEntryField.Country } ),
    ;

    private final Object[] prefix;
    private final Object[] suffix;
    private final String description;

    public EntryToStringRule generateRule(ZIPCodeRule zipCodeRule) {
        int prefixLen = prefix.length;
        Object[] resArray = new Object[prefixLen + suffix.length + 1];
        
        System.arraycopy(prefix, 0, resArray, 0, prefixLen);
        resArray[prefixLen] = zipCodeRule;
        System.arraycopy(suffix, 0, resArray, prefixLen+1, suffix.length);
        
        return new ConcatRule(resArray);
    }

    public String getDescription() {    
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    private LocationRule(Object[] prefix, Object[] suffix) {
        StringBuilder desc = new StringBuilder();
        ConcatRule.objectArrayToString(desc, prefix);
        desc.append(Utils._("[Location with ZIP code]"));
        ConcatRule.objectArrayToString(desc, suffix);
        this.description = desc.toString();
        this.prefix = prefix;
        this.suffix = suffix;
    }

    
}
