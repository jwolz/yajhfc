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
package yajhfc.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import yajhfc.Utils;

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
		fireTableCellUpdated(rowIndex, columnIndex);
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
	
	public void setSelectedObjects(Collection<T> selObjects) {
	    Arrays.fill(selected, false);
	    for (T obj : selObjects) {
	        int idx = Utils.indexOfArray(items, obj);
	        if (idx >= 0)
	            selected[idx] = true;
	    }
	    fireTableDataChanged();
	}
	
	public void selectAll() {
	    selectAll(true);
	}
	
	public void deselectAll() {
	    selectAll(false);
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
