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
import java.util.logging.Logger;

import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public class DefaultPBEntryFieldContainer extends EnumMap<PBEntryField, String>
        implements PBEntryFieldContainer {

    private static final Logger log = Logger.getLogger(DefaultPBEntryFieldContainer.class.getName());
    
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
    
    public DefaultPBEntryFieldContainer parseFromString(final String numberOrFullFields) {
        setAllFieldsTo("");
        // If it contains no : or ;, assume it's a fax number
        if (numberOrFullFields.indexOf(':') < 0 || numberOrFullFields.indexOf(';') < 0) {
            put(PBEntryField.FaxNumber, numberOrFullFields);
        } else {
            int pos;
            int oldPos = 0;
            StringBuilder value = new StringBuilder();
            while ((pos = numberOrFullFields.indexOf(':', oldPos)) >= 0) {
                String key = numberOrFullFields.substring(oldPos, pos).trim().toLowerCase();
                value.setLength(0);

                boolean afterBackSlash = false;
                parseLoop: while (pos < numberOrFullFields.length()-1) {
                    final char c = numberOrFullFields.charAt(++pos);
                    if (afterBackSlash) {
                        switch (c) {
                        default:
                            value.append('\\'); // Fall through intended
                        case ';':
                        case '\\':
                            value.append(c);
                        }
                        afterBackSlash = false;
                    } else {
                        switch (c) {
                        case '\\':
                            afterBackSlash = true;
                            break;
                        case ';':
                            break parseLoop;
                        default:
                            value.append(c);
                        break;
                        }
                    }
                }

                PBEntryField pbField = PBEntryField.getKeyToFieldMap().get(key);
                if (pbField != null) {
                    put(pbField, value.toString().trim());
                } else {
                    log.info("Unknown field:value \"" + key + ':' + value + '"');
                }
                oldPos = pos+1;
            }
        }
        return this;
    }
    
    public void setAllFieldsTo(String value) {
        for (PBEntryField field : PBEntryField.values()) {
            put(field, value);
        }
    }
    
    public DefaultPBEntryFieldContainer() {
        super(PBEntryField.class);
    }

    public DefaultPBEntryFieldContainer(String defValue) {
        this();
        setAllFieldsTo(defValue);
    }
    
    public DefaultPBEntryFieldContainer(PBEntryFieldContainer other) {
        this();
        copyFrom(other);
    }
}
