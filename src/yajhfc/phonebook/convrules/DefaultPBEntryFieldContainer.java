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

import java.util.EnumMap;

import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public class DefaultPBEntryFieldContainer extends EnumMap<PBEntryField, String>
        implements PBEntryFieldContainer {

    /* (non-Javadoc)
     * @see yajhfc.phonebook.convrules.PBEntryFieldContainer#getField(yajhfc.phonebook.PBEntryField)
     */
    public String getField(PBEntryField field) {
        return get(field);
    }
    
    public void setField(PBEntryField field, String value) {
        put(field, value);
    }
    
    public void copyFrom(PBEntryFieldContainer other) {
        for (PBEntryField field : PBEntryField.values()) {
            put(field, other.getField(field));
        }
    }
    
    public DefaultPBEntryFieldContainer() {
        super(PBEntryField.class);
    }

    public DefaultPBEntryFieldContainer(String defValue) {
        this();
        for (PBEntryField field : PBEntryField.values()) {
            put(field, defValue);
        }
    }
    
    public DefaultPBEntryFieldContainer(PBEntryFieldContainer other) {
        this();
        copyFrom(other);
    }
}
