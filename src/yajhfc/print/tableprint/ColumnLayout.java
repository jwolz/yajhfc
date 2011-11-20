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
