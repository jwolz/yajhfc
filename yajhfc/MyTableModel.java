package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import java.awt.Font;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel {
    
    protected String[][] data;
    public Vector<FmtItem> columns;
    protected YajJob[] jobs;
    
    
    /**
     * Returns a custom font for the table cell.
     * A return value of null means "use default font"
     * @param row
     * @param col
     * @return
     */
    public Font getCellFont(int row, int col) {
        return null;
    }
    
    protected YajJob createYajJob(String[] data) {
        return new SentYajJob(columns, data);
    }
    
    public void setData(String[][] newData) {
        if (!Arrays.deepEquals(data, newData)) {
            data = newData;
            jobs = new YajJob[data.length];
            for (int i=0; i < data.length; i++) {
                jobs[i] = createYajJob(data[i]);
            }
            fireTableDataChanged();
        }
    }
    public int getColumnCount() {
        if (columns != null)
            return columns.size();
        else
            return 0;
    }
    
    public int getRowCount() {
        if (data == null)
            return 0;
        else
            return data.length;
    }
    
    public String getStringAt(int rowIndex, int columnIndex) {
        return jobs[rowIndex].getStringData(columnIndex);
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return jobs[rowIndex].getData(columnIndex);
    }
    
    public YajJob getJob(int rowIndex) {
        return jobs[rowIndex];
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public String getColumnName(int column) {
        return columns.get(column).desc;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).dataClass;
    }
}
