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

import yajhfc.utils;

public class NumberTFLItemTableModel extends AbstractTableModel {
    protected List<NumberTFLItem> list;
    
    protected static final String[] columnNames = {
        utils._("Fax number"),
        utils._("Name"),
        utils._("Company"),
        utils._("Location"),
        utils._("Voice number")
    };
    
    public NumberTFLItemTableModel(List<NumberTFLItem> backingList) {
        this.list = backingList;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return list.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        NumberTFLItem item = list.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return item.faxNumber;
        case 1:
            return item.name;
        case 2:
            return item.company;
        case 3:
            return item.location;
        case 4:
            return item.voiceNumber;
        default:
            return null;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        NumberTFLItem item = list.get(rowIndex);
        switch (columnIndex) {
        case 0:
            item.faxNumber = value.toString();
            break;
        case 1:
            item.name = value.toString();
            break;
        case 2:
            item.company = value.toString();
            break;
        case 3:
            item.location = value.toString();
            break;
        case 4:
            item.voiceNumber  = value.toString();
            break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
    
    public void addRow(NumberTFLItem item) {
        list.add(item);
        fireTableRowsInserted(list.size()-1, list.size()-1);
    }
    
    public void addRow() {
        addRow(new NumberTFLItem(""));
    }
    
    public void addRows(Collection<NumberTFLItem> newItems) {
        list.addAll(newItems);
        fireTableRowsInserted(list.size()-newItems.size(), list.size()-1);
    }
    
    public void removeRow(int index) {
        list.remove(index);
        fireTableRowsDeleted(index, index);
    }
}
