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
package yajhfc.print;

import java.awt.Graphics2D;

import javax.swing.table.TableModel;

import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
import yajhfc.print.tableprint.SimpleColumnLayout;
import yajhfc.print.tableprint.TablePrintColumn;
import yajhfc.print.tableprint.TablePrintable;

/**
 * @author jonas
 *
 */
public class PhoneBookColumnLayout extends SimpleColumnLayout {

	protected PBEntryFieldTableModel model;
	protected TablePrintColumn[] filteredColumns;
	protected TablePrintColumn[] distListColumns;
	protected PBEntryField[] filter;
	
	private void applyFilter2(PBEntryField[] filter) {
		if (tableColumns == null)
			return;
		if (filter == null) {
			filteredColumns = tableColumns;
		} else {
			filteredColumns = new TablePrintColumn[filter.length];
			for (int i=0; i<filter.length; i++) {
				filteredColumns[i] = getColumnFor(filter[i]);
			}
		}
	}
	
	/**
	 * Applies a fitering of the columns to this table
	 * @param filter
	 */
	public void applyFilter(PBEntryField[] filter) {
		this.filter = filter;
		applyFilter2(filter);
	}
	
	@Override
	public void initializeLayout(TablePrintable parent, TableModel model) {
		super.initializeLayout(parent, model);
		this.model = (PBEntryFieldTableModel)model;
		applyFilter2(filter);
		
		distListColumns = new TablePrintColumn[] {
				new TablePrintColumn(parent, this.model.indexOfField(PBEntryField.Name)),
				new DistListColumn(parent)
		};
	}
	
	public TablePrintColumn getColumnFor(PBEntryField field) {
		return this.tableColumns[model.indexOfField(field)];
	}
	
	public TablePrintColumn getDistListColumn(boolean header) {
	    return distListColumns[header ? 0 : 1];
	}
	
	@Override
	public TablePrintColumn[] getTableColumns() {
		return filteredColumns;
	}
	
	private boolean calcDistLists;
	@Override
	protected boolean useForPreferredWidth(TablePrintColumn column, int row) {
		if (model.getRow(row) instanceof DistributionList) {
			return calcDistLists;
		} else {
			return !calcDistLists;
		}
	}
	@Override
	public void calculateColumnWidths(Graphics2D graphics, double width,
			double insetX) {
		double w1, w2;
		calcDistLists = false;
		w1 = calculateColumnWidth(graphics, filteredColumns, width, insetX);
		calcDistLists = true;
		w2 = calculateColumnWidth(graphics, distListColumns, Math.max(width, w1), insetX);
		tableWidth = Math.max(w1, w2);
	}
	
	@Override
	public TablePrintColumn[] getLayoutForRow(int row) {
		if (model.getRow(row) instanceof DistributionList) {
			return distListColumns;
		} else {
			return filteredColumns;
		}
	}
	
	protected static class DistListColumn extends TablePrintColumn {

		public DistListColumn(TablePrintable parent) {
			super(parent, -1);
			width = WIDTH_FILL;
		}

		@Override
		protected Class<?> getColumnClass() {
			return DistributionList.class;
		}

		@Override
		public Object getData(int rowIndex) {
			return ((PBEntryFieldTableModel)parent.getModel()).getRow(rowIndex);
		}

		@Override
		public String getHeaderText() {
			return "Dummy";
		}

	}
}
