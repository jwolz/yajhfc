/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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

import java.lang.reflect.Array;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

/**
 * @author jonas
 *
 */
public class SelectionTableModel<T> extends AbstractTableModel {
	protected T[] items;
	protected boolean [] selected;
	
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return items.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return selected[rowIndex];
		case 1:
			return items[rowIndex];
		default:
			return null;
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			selected[rowIndex] = (Boolean)aValue;
			break;
		default:
			throw new UnsupportedOperationException("Not editable");
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == 0);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return getItemClass();
		default:
			return null;
		}
	}
	
	public void setItems(T[] items) {
		this.items = items;
		this.selected = new boolean[items.length];
		fireTableDataChanged();
	}
	
	public int countNumberOfSelectedItems() {
		int count = 0;
		for (boolean b : selected) {
			if (b)
				count++;
		}
		return count;
	}
	
	public int[] getSelectedIndices() {
		int[] res = new int[countNumberOfSelectedItems()];
		int j = 0;
		for (int i=0; i<selected.length; i++) {
			if (selected[i])
				res[j++] = i;
		}
		return res;                 
	}
	
	public T[] getSelectedObjects() {
		T[] res = newArray(countNumberOfSelectedItems());
		int j = 0;
		for (int i=0; i<selected.length; i++) {
			if (selected[i])
				res[j++] = items[i];
		}
		return res;                 
	}
	
	public void setSelectedIndices(int[] selIdx) {
		Arrays.fill(selected, false);
		for (int idx : selIdx) {
			selected[idx] = true;
		}
		fireTableDataChanged();
	}
	
	public void selectAll(boolean state) {
		Arrays.fill(selected, state);
		fireTableDataChanged();
	}
	
	public boolean getSelectedState(int row) {
		return selected[row];
	}
	
	public void setSelectedState(int row, boolean state) {
		selected[row] = state;
		fireTableCellUpdated(row, 0);
	}
	
    @SuppressWarnings("unchecked")
    private T[] newArray(int size) {
        return (T[])Array.newInstance(getItemClass(), size);
    }
    
    @SuppressWarnings("unchecked")
    private Class<T> getItemClass() {
        return (Class<T>)items.getClass().getComponentType();
    }
	
	public SelectionTableModel(T[] items) {
		setItems(items);
	}
}
