package yajhfc.phonebook.csv;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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


import yajhfc.Utils;
import yajhfc.phonebook.DefaultPhoneBookEntry;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;

public class CSVPhonebookEntry extends DefaultPhoneBookEntry {

    protected CSVPhoneBook parent;
    protected String[] columnData;
    protected boolean dirty = false;

    public CSVPhonebookEntry(CSVPhoneBook parent, String[] columnData) {
        super();
        this.columnData = columnData;
        this.parent = parent;
    }

    @Override
    public void commit() {
        if (dirty) {
            parent.writeEntry(this);
        }
    }

    @Override
    public void delete() {
        parent.deleteEntry(this);
    }

    @Override
    public PhoneBook getParent() {
        return parent;
    }

    @Override
    public String getField(PBEntryField field) {
        Integer mapping = parent.columnMapping.get(field);
        if (mapping != null) {
            return columnData[mapping.intValue()];
        } else {
            return "";
        }
    }

    @Override
    public void setField(PBEntryField field, String value) {
        Integer mapping = parent.columnMapping.get(field);
        if (mapping != null) {
            value = Utils.sanitizeInput(value);
            if (!value.equals(columnData[mapping.intValue()])) {
                columnData[mapping.intValue()] = value;
                dirty = true;
            }
        }
    }
    
}
