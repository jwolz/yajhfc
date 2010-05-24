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
    
    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#calculateColumnWidths(java.awt.Graphics2D, double, double)
     */
    public void calculateColumnWidths(Graphics2D graphics, double width,
            double insetX) {
        tableWidth = calculateColumnWidth(graphics, tableColumns, width, insetX);
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
                if (column.wordWrap)
                    throw new IllegalArgumentException("Column " + i + ": WordWrap together width WIDTH_PREFERRED not supported");
                // Measure header
                FontMetrics fm = graphics.getFontMetrics(column.getEffectiveHeaderFont());
                double maxWidth = parent.getDefaultRenderer().getPreferredWidth(graphics, column.getHeaderText(), fm, null, column);
                
                // Measure data cells
                fm = graphics.getFontMetrics(column.getEffectiveFont());
                TableCellRenderer rend = column.getEffectiveRenderer();
                for (int j = 0; j < parent.getModel().getRowCount(); j++) {
                    double prefW = rend.getPreferredWidth(graphics, column.getData(j), fm, column.getEffectiveFormat(), column);
                    if (prefW > maxWidth) {
                        maxWidth = prefW;
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
        
        if (numFillCols > 0 && widthSum < width) {
            // Second "pass" to set fillColumns to the rest of the available space
            double fillColWidth = (width - widthSum) / (double)numFillCols;
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

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#getHeaderLayout()
     */
    public TablePrintColumn[] getHeaderLayout() {
        return tableColumns;
    }

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#getLayoutForRow(int)
     */
    public TablePrintColumn[] getLayoutForRow(int row) {
        return tableColumns;
    }

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.ColumnLayout#getTableWidth()
     */
    public double getTableWidth() {
        return tableWidth;
    }
    
    public int getMaximumColumnCount() {
        return tableColumns.length;
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
