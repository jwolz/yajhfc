package yajhfc.phonebook;


/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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

public abstract class SimplePhoneBookEntry extends PhoneBookEntry {

    // Not using an EnumMap here to save some memory (we will have lots of Entries...)
    protected String[] data = new String[PBEntryField.values().length];
    
    protected boolean dirty = false;
    /**
     * The cached result of the last toString call. Reset this to null
     * when you also would set dirty to true.
     */
    protected String lastToString = null;
    
    @Override
    public String getField(PBEntryField field) {
        return data[field.ordinal()];
    }
    
    protected void setFieldUndirty(PBEntryField field, String value) {
        data[field.ordinal()] = value;
    }
    
    @Override
    public void setField(PBEntryField field, String value) {
        String oldVal = data[field.ordinal()];
        if (value != oldVal && (oldVal == null || !oldVal.equals(value))) {
            setFieldUndirty(field, value);
            dirty = true;
            lastToString = null;
        }
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    @Override
    public String toString() {
        if (lastToString == null) {
            lastToString = super.toString();
        }
        return lastToString;
    }
}
