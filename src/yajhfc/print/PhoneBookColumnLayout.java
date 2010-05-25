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
