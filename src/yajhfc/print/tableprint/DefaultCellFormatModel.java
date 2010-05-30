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
import java.awt.Font;

import javax.swing.table.TableModel;

/**
 * @author jonas
 *
 */
public class DefaultCellFormatModel implements CellFormatModel {    
    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.CellFormatModel#getCellBackgroundColor(yajhfc.print.tableprint.TablePrintColumn, javax.swing.table.TableModel, int)
     */
    public Color getCellBackgroundColor(TablePrintColumn col, TableModel model,
            int rowIndex) {
        if (col.getBackgroundColor() != null)
            return col.getBackgroundColor();
        
        Color[] cellBackground = col.getParent().getCellBackground();
        if (cellBackground == null || cellBackground.length == 0) {
            return null;
        } else {
            return cellBackground[rowIndex%cellBackground.length];
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.CellFormatModel#getCellFont(yajhfc.print.tableprint.TablePrintColumn, javax.swing.table.TableModel, int)
     */
    public Font getCellFont(TablePrintColumn col, TableModel model, int rowIndex) {
        return col.getEffectiveFont();
    }
}
