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
 */
package yajhfc.export;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;

import javax.swing.table.TableModel;

import yajhfc.model.FmtItem;
import yajhfc.model.table.FaxListTableModel;
import yajhfc.util.TableSorter;

/**
 * @author jonas
 *
 */
public class TooltipJTableHTMLExporter extends HTMLExporter {  
    
    @Override
    protected DateFormat getDateFormat(TableModel model, int colIndex) {
        TableSorter sorter = (TableSorter)model;
        FaxListTableModel<? extends FmtItem> realModel = getRealModel(sorter);
        
        return realModel.getColumns().get(colIndex).getDisplayDateFormat();
    }
    
    @Override
    protected Color getCellBackground(TableModel model, int row, int col) {
        TableSorter sorter = (TableSorter)model;
        FaxListTableModel<? extends FmtItem> realModel = getRealModel(sorter);
        int realRow = sorter.modelIndex(row);
        
        return realModel.getCellBackgroundColor(realRow, col);
    }

    @SuppressWarnings("unchecked")
    private FaxListTableModel<? extends FmtItem> getRealModel(TableSorter sorter) {
        return (FaxListTableModel<? extends FmtItem>)sorter.getTableModel();
    }
    
    @Override
    protected Color getCellForeground(TableModel model, int row, int col) {
        TableSorter sorter = (TableSorter)model;
        FaxListTableModel<? extends FmtItem> realModel = getRealModel(sorter);
        int realRow = sorter.modelIndex(row);
        
        return realModel.getCellForegroundColor(realRow, col);
    }
    
    @Override
    protected Font getFont(TableModel model, int row, int col) {
        TableSorter sorter = (TableSorter)model;
        FaxListTableModel<? extends FmtItem> realModel = getRealModel(sorter);
        int realRow = sorter.modelIndex(row);
        
        return realModel.getCellFont(realRow, col);
    }

}
