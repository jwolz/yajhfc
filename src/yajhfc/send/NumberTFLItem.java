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
package yajhfc.send;

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

public class NumberTFLItem extends TFLItem implements PBEntryFieldContainer {
    public final DefaultPBEntryFieldContainer fields = new DefaultPBEntryFieldContainer("");
    
    @Override
    public String getText() {
        return fields.get(PBEntryField.FaxNumber);
    }
    
    @Override
    public void setText(String newText) {
        fields.put(PBEntryField.FaxNumber, newText);
    }
    
    public void loadFromPBE(PhoneBookEntry pbe) {
        fields.copyFrom(pbe);
    }
    
    public NumberTFLItem(String number) {
        setText(number);
    }
    
    public NumberTFLItem(PhoneBookEntry pbe) {
        loadFromPBE(pbe);
    }

    public String getField(PBEntryField field) {
        return fields.get(field);
    }

    public void setField(PBEntryField field, String value) {
        fields.put(field, value);
    }
}