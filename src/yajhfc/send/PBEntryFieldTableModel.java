package yajhfc.send;
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
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

public class PBEntryFieldTableModel extends AbstractTableModel {
    protected List<PBEntryFieldContainer> list;
    
    protected static final PBEntryField[] columns;
    static {
        PBEntryField[] vals = PBEntryField.values();
        columns = new PBEntryField[vals.length];
        columns[0] = PBEntryField.FaxNumber;
        int ptr = 1;
        for (PBEntryField field : vals) {
            if (field != PBEntryField.FaxNumber) {
                columns[ptr++] = field;
            }
        }
    }
    
    public PBEntryFieldTableModel(List<PBEntryFieldContainer> backingList) {
        this.list = backingList;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getRowCount() {
        return list.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        PBEntryFieldContainer item = list.get(rowIndex);
        return item.getField(columns[columnIndex]);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        PBEntryFieldContainer item = list.get(rowIndex);
        item.setField(columns[columnIndex], (String)value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return columns[column].getDescription();
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
    
    public void addRow(PBEntryFieldContainer item) {
        list.add(item);
        fireTableRowsInserted(list.size()-1, list.size()-1);
    }
    
    public void addRow() {
        addRow(new DefaultPBEntryFieldContainer(""));
    }
    
    public void addRows(Collection<PBEntryFieldContainer> newItems) {
        list.addAll(newItems);
        fireTableRowsInserted(list.size()-newItems.size(), list.size()-1);
    }
    
    public void removeRow(int index) {
        list.remove(index);
        fireTableRowsDeleted(index, index);
    }
}
