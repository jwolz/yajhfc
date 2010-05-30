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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.text.Format;

/**
 * @author jonas
 *
 */
public interface TableCellRenderer {
    
    /**
     * Nothing has been drawn -> need page break
     */
    public static double DRAWCELL_NOTHING = 0.0;
    
    /**
     * After a page break nothing to draw remained
     */
    public static double DRAWCELL_NOTHING_REMAINED = Double.MIN_VALUE;
    
    
    /**
     * Draw the specified cell with the currently set font
     * @param graphics Graphics context to draw in
     * @param x horizontal position
     * @param y vertical position
     * @param value the value to draw
     * @param column the column to draw
     * @param background the background color for this cell
     * @param spaceX the horizontal cell inset
     * @param spaceY the vertical cell inset
     * @param maxY  the maximum y value usable before a page break should be requested
     * @param pageContinuation true if this row was already partially drawn on the previous page
     * @param pageData 
     * @param format the formatter to use to convert the value to String or null to use the toString() method
     * @return the cell's height, 0.0 if nothing fit on this page or -(drawn height) if this cell was partially drawn
     */
    double drawCell(Graphics2D graphics, double x, double y, 
            Object value, TablePrintColumn column, Color background, double spaceX, double spaceY, double maxY, boolean pageContinuation, ColumnPageData pageData);
    
    /**
     * Returns the preferred width (usually the text width) for the given value
     * @param graphics Graphics context to draw in
     * @param value the value to draw
     * @param fm the FontMetrics to use for measurement
     * @param format the formatter to use to convert the value to String or null to use the toString() method
     * @param column the column to draw
     * @return the cell's preferred width
     */
    double getPreferredWidth(Graphics2D graphics, Object value, FontMetrics fm, Format format, TablePrintColumn column);
    
    /**
     * Data saved from/for the previous page/row
     * @author jonas
     *
     */
    public static class ColumnPageData implements Cloneable {
        public Object remainingData;
        int lastDrawState;
        
        static final int LASTDRAW_COMPLETE = 0;
        static final int LASTDRAW_PARTIAL = 1;
        static final int LASTDRAW_NOTHING = 2;
        
        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                // Should never happen
                throw new AssertionError(e);
            }
        }
        
        public void clear() {
            remainingData = null;
            lastDrawState = LASTDRAW_COMPLETE;
        }
    }
}
