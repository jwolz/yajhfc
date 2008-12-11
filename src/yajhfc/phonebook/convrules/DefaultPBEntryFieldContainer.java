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

import yajhfc.Utils;
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
    
    public DefaultPBEntryFieldContainer parseFromString(String numberOrFullFields) {
        setAllFieldsTo("");
        // If it contains no : or ;, assume it's a fax number
        if (numberOrFullFields.indexOf(':') < 0 || numberOrFullFields.indexOf(';') < 0) {
            put(PBEntryField.FaxNumber, numberOrFullFields);
        } else {
            String[] fields = Utils.fastSplit(numberOrFullFields, ';');
            for (String field : fields) {
                int pos = field.indexOf(':');
                if (pos < 0) {
                    log.info("Ignoring invalid name:value pair: " + field);
                } else {
                    String fieldName = field.substring(0, pos).trim();
                    String fieldValue = field.substring(pos+1).trim();
                    PBEntryField pbField = PBEntryField.getKeyToFieldMap().get(fieldName.toLowerCase());
                    if (pbField != null) {
                        put(pbField, fieldValue);
                    } else {
                        log.info("Unknown field \"" + fieldName + "\" in '" + field + "'");
                    }
                }
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
