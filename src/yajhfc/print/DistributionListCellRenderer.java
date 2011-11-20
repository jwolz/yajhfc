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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
import yajhfc.print.tableprint.HeaderPrintMode;
import yajhfc.print.tableprint.ResizeMode;
import yajhfc.print.tableprint.TableCellRenderer;
import yajhfc.print.tableprint.TablePrintColumn;
import yajhfc.print.tableprint.TablePrintable;

/**
 * @author jonas
 *
 */
public class DistributionListCellRenderer extends TablePrintable implements
		TableCellRenderer {

	protected PBEntryFieldTableModel tableModel;
	
	public DistributionListCellRenderer() {
		super(null);
		setModel(tableModel = new PBEntryFieldTableModel(null));
		setHeaderPrintMode(HeaderPrintMode.PRINT_ON_FIRST_PAGE);
		setResizeMode(ResizeMode.RESIZE_GROW_AND_SHRINK);
	}

	/* (non-Javadoc)
	 * @see yajhfc.print.tableprint.TableCellRenderer#drawCell(java.awt.Graphics2D, double, double, java.lang.Object, yajhfc.print.tableprint.TablePrintColumn, java.awt.Color, double, double, double, boolean, yajhfc.print.tableprint.TableCellRenderer.ColumnPageData)
	 */
	public double drawCell(Graphics2D graphics, double x, double y,
			Object value, TablePrintColumn column, Color background,
			double spaceX, double spaceY, double maxY,
			boolean pageContinuation, ColumnPageData pageData) {
		if (pageContinuation) {
			if (pageData.remainingData == null)
				return DRAWCELL_NOTHING_REMAINED;
			else
				readPageData((PageData)pageData.remainingData);
		} else {
			currentRow = 0;
			currentPage = 0;
			lastStartRow = 0;
			pageData.clear();
		}
		DistributionList distList = (DistributionList)value;
		tableModel.setList(Collections.<PBEntryFieldContainer>unmodifiableList(distList.getEntries()));
		Rectangle2D.Double drawArea = new Rectangle2D.Double(x, y, column.getEffectiveColumnWidth(), maxY-y);
		Shape oldClip = graphics.getClip();
		AffineTransform trans = graphics.getTransform();
		graphics.clip(drawArea);
		double drawnHeight = drawTable(graphics, drawArea, currentPage);
		graphics.setTransform(trans);
		graphics.setClip(oldClip);
		if (drawnHeight <= 0.0) {
			currentPage++;
			pageData.remainingData = createPageData();
		}
		return drawnHeight;
	}

	protected void readPageData(PageData pd) {
		this.currentRow = pd.currentRow;
		this.currentPage = pd.currentPage;
		this.lastStartRow = pd.lastStartRow;
		this.pageData.clear();
		this.pageData.addAll(pd.pageData);
	}
	
	protected PageData createPageData() {
		PageData pd = new PageData();
		pd.currentRow = this.currentRow;
		pd.currentPage = this.currentPage;
		pd.lastStartRow = this.lastStartRow;
		pd.pageData = new ArrayList<ColumnPageData[]>(this.pageData);
		return pd;
	}
	
	/* (non-Javadoc)
	 * @see yajhfc.print.tableprint.TableCellRenderer#getPreferredWidth(java.awt.Graphics2D, java.lang.Object, java.awt.FontMetrics, java.text.Format, yajhfc.print.tableprint.TablePrintColumn)
	 */
	public double getPreferredWidth(Graphics2D graphics, Object value,
			FontMetrics fm, Format format, TablePrintColumn column) {
		throw new UnsupportedOperationException();
	}

	
	protected static class PageData {
	    public int currentRow; 
	    public int lastStartRow;
	    public int currentPage;
	    public List<ColumnPageData[]> pageData;
	}
}
