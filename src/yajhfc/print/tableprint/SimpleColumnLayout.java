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
package yajhfc.print.tableprint;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.table.TableModel;

/**
 * @author jonas
 *
 */
public class SimpleColumnLayout implements ColumnLayout {

    protected TablePrintable parent;
    protected TablePrintColumn[] tableColumns;
    protected double tableWidth = -1;
    protected double minFillColsWidth = 0;
    
    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#calculateColumnWidths(java.awt.Graphics2D, double, double)
     */
    public void calculateColumnWidths(Graphics2D graphics, double width,
            double insetX) {
        tableWidth = calculateColumnWidth(graphics, getTableColumns(), width, insetX);
    }
    
    /**
     * Returns if the specified row should be included in the preferred width calculation of
     * the specified column
     * @param column
     * @param row
     * @return
     */
    protected boolean useForPreferredWidth(TablePrintColumn column, int row) {
    	return true;
    }

    /**
     * Calculate the column widths of the specified columns and return the total width
     * @param graphics
     * @param width
     * @param insetX
     * @return
     */
    protected double calculateColumnWidth(Graphics2D graphics, TablePrintColumn[] columns, double width, double insetX) {
        final int columnCount = columns.length;
        double widthSum = 0;
        int numFillCols = 0;
        for (int i=0; i < columnCount; i++) {
            final TablePrintColumn column = columns[i];
            float w = column.getWidth();
            
            if (w == TablePrintColumn.WIDTH_PREFERRED) {
                // Measure header
                FontMetrics fm = graphics.getFontMetrics(column.getEffectiveHeaderFont());
                double maxWidth = parent.getDefaultRenderer().getPreferredWidth(graphics, column.getHeaderText(), fm, null, column);
                
                // Measure data cells
                fm = graphics.getFontMetrics(column.getEffectiveFont());
                TableCellRenderer rend = column.getEffectiveRenderer();
                for (int j = 0; j < parent.getModel().getRowCount(); j++) {
                	if (useForPreferredWidth(column, j)) {
                		double prefW = rend.getPreferredWidth(graphics, column.getData(j), fm, column.getEffectiveFormat(), column);
                		if (prefW > maxWidth) {
                			maxWidth = prefW;
                		}
                	}
                }
                widthSum += column.effectiveColumnWidth = maxWidth + 2*insetX;
            } else if (w == TablePrintColumn.WIDTH_FILL) {
                numFillCols++;
            } else if (w > 0f && w <= 1f) {
                widthSum += column.effectiveColumnWidth = width * w;
            } else if (w > 1f) {
                widthSum += column.effectiveColumnWidth = w;
            } else {
                throw new IllegalArgumentException("Column " + i + ": Illegal width " + w);
            }
        }
        
        if (numFillCols > 0 && (widthSum < width || minFillColsWidth > 0)) {
            // Second "pass" to set fillColumns to the rest of the available space
            double fillColWidth = Math.max(width - widthSum, minFillColsWidth) / (double)numFillCols;
            for (int i=0; i < columnCount; i++) {
                final TablePrintColumn column = columns[i];
                if (column.getWidth() == TablePrintColumn.WIDTH_FILL) {
                    column.effectiveColumnWidth = fillColWidth;
                }
            }
        }
        
        double totalWidth = 0;
        for (int i=0; i < columnCount; i++) {
            totalWidth += columns[i].effectiveColumnWidth;
        }
        return totalWidth;
    }

    /**
     * Returns the default array of table columns
     * @return
     */
    public TablePrintColumn[] getTableColumns() {
        return tableColumns;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#getHeaderLayout()
     */
    public TablePrintColumn[] getHeaderLayout() {
        return getTableColumns();
    }

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#getLayoutForRow(int)
     */
    public TablePrintColumn[] getLayoutForRow(int row) {
        return getTableColumns();
    }

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#getTableWidth()
     */
    public double getTableWidth() {
        return tableWidth;
    }
    
    public int getMaximumColumnCount() {
        return getTableColumns().length;
    }
    
    /**
     * The minimum width all "fill" columns will be assigned.
     * @return
     */
    public void setMinFillColsWidth(double minFillColsWidth) {
		this.minFillColsWidth = minFillColsWidth;
	}
    
    /**
     * The minimum width all "fill" columns will be assigned.
     * @return
     */
    public double getMinFillColsWidth() {
		return minFillColsWidth;
	}

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#intializeLayout(yajhfc.print.tableprint.TablePrintable, javax.swing.table.TableModel)
     */
    public void initializeLayout(TablePrintable parent, TableModel model) {
        this.parent = parent;
        final int columnCount = model.getColumnCount();
        tableColumns = new TablePrintColumn[columnCount];
        for (int i=0; i < columnCount; i++) {
            tableColumns[i] = new TablePrintColumn(parent, i);
        }
    }

}
