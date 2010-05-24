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

import java.awt.Graphics2D;

import javax.swing.table.TableModel;

/**
 * Defines column layouts of a table
 * 
 * @author jonas
 *
 */
public interface ColumnLayout {
    
    /**
     * Initializes the column layout for a new table model
     * @param parent
     * @param model
     */
    public void initializeLayout(TablePrintable parent, TableModel model);
    
    /**
     * Returns the total width of the table
     * @return
     */
    public double getTableWidth();
    
    /**
     * Returns the maximum possible count of columns returned by any call of getLayoutForRow
     * @return
     */
    public int getMaximumColumnCount();
    
    
    /**
     * Calculate the column widths for the given graphics context.
     * @param graphics the graphics context
     * @param width the total width of the drawing area
     * @param insetX the horizontal cell inset
     */
    public void calculateColumnWidths(Graphics2D graphics, double width, double insetX);
    
    /**
     * Returns the columns to use for the header
     * @return
     */
    public TablePrintColumn[] getHeaderLayout();
    
    /**
     * Returns the columns used in the given row
     * @param row
     * @return
     */
    public TablePrintColumn[] getLayoutForRow(int row);
}
