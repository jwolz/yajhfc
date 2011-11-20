package yajhfc.phonebook.ui;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

public class PBEntryFieldTableModel extends AbstractTableModel {
    protected List<PBEntryFieldContainer> list;
    protected boolean editable = true;

    private static int[] columnIndexMap;    
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
    
    protected static int indexOfColumn(PBEntryField column) {
        if (columnIndexMap == null) {
            // Assumes that columns contains all PBEntryField values
            columnIndexMap = new int[columns.length];
            for (int i=0; i<columns.length; i++) {
                columnIndexMap[columns[i].ordinal()] = i;
            }
        }
        return columnIndexMap[column.ordinal()];
    }
    
    public PBEntryFieldTableModel(List<PBEntryFieldContainer> backingList) {
        this.list = backingList;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getRowCount() {
        return (list == null) ? 0 : list.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        PBEntryFieldContainer item = list.get(rowIndex);
        return item.getField(columns[columnIndex]);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (!editable)
            return;
        
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
    
    public void addRows(Collection<? extends PBEntryFieldContainer> newItems) {
        list.addAll(newItems);
        fireTableRowsInserted(list.size()-newItems.size(), list.size()-1);
    }
    
    public void removeRow(int index) {
        list.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    public PBEntryFieldContainer getRow(int index) {
        return list.get(index);
    }
    
    public PBEntryField getColumn(int index) {
    	return columns[index];
    }
    
    public int indexOfField(PBEntryField field) {
    	return indexOfColumn(field);
    }
    
    public void setList(List<PBEntryFieldContainer> list) {
		this.list = list;
		fireTableDataChanged();
	}
}
