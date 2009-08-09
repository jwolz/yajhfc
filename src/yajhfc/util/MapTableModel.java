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
package yajhfc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import yajhfc.Utils;

/**
 * Table model to edit a Map<String,String>
 * @author jonas
 *
 */
public class MapTableModel extends AbstractTableModel {
    protected final List<Row> rows = new ArrayList<Row>();
    protected Map<String,String> mapToEdit;
    
    private static String[] cols = {
        Utils._("Key"), Utils._("Value")
    };
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return cols.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return cols[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return rows.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Row row = rows.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return row.getKey();
        case 1:
            return row.getValue();
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
        Row row = rows.get(rowIndex);
        switch (columnIndex) {
        case 0:
            if ("".equals(value))
                return;
            
            row.setKey(value.toString());
            break;
        case 1:
            row.setValue(value.toString());
            break;
        default:
            return;
        }
        
        fireTableCellUpdated(rowIndex, columnIndex);
        
        if (row.isNewRow) {
            row.isNewRow = false;
            rows.add(new Row(true));
            fireTableRowsInserted(rows.size()-1, rows.size()-1);
        }
    }

    public boolean rowIsDeletable(int index) {
        return (!rows.get(index).isNewRow);
    }
    
    public void deleteRow(int index) {
        Row row = rows.get(index);
        if (row.isNewRow) {
            throw new IllegalArgumentException("Cannot delete the new row!");
        }
        rows.remove(index);
        mapToEdit.remove(row.getKey());
        
        fireTableRowsDeleted(index, index);
    }
    
    protected void loadFromMap() {
        rows.clear();
        for (Map.Entry<String,String> entry : mapToEdit.entrySet()) {
            rows.add(new Row(entry));
        }
        rows.add(new Row(true));
        fireTableDataChanged();
    }
    
    public Map<String, String> getMapToEdit() {
        return mapToEdit;
    }
    
    public MapTableModel(Map<String, String> mapToEdit) {
        super();
        this.mapToEdit = mapToEdit;
        loadFromMap();
    }

    class Row implements Map.Entry<String,String>{
        private String key;
        private String value;
        public boolean isNewRow = false;
        
        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
        
        public String setValue(String value) {
            String old = this.value;
            this.value = value;
            mapToEdit.put(key, value);
            return old;
        }
        
        public void setKey(String newKey) {
            if (newKey != null && !newKey.equals(key)) {
                if (key != null) {
                    mapToEdit.remove(key);
                }
                mapToEdit.put(newKey, value);
                this.key = newKey;
            }
        }
        
        public Row(boolean isNewRow) {
            this.isNewRow = isNewRow;
        }
        
        public Row(Map.Entry<String,String> entry) {
            key = entry.getKey();
            value = entry.getValue();
        }
    }
}
