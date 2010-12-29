/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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
package yajhfc.model.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.table.FaxListTableModel;
import yajhfc.util.TableSorter;

/**
 * JTable with tooltips and other extensions. <p>
 * Create a new table with: new TooltipJTable(realTableModel)
 */
public class TooltipJTable<T extends FmtItem> extends JTable {
    private static final Logger log = Logger.getLogger(TooltipJTable.class.getName());
    
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent event) {
                int index = columnModel.getColumnIndexAtX(event.getPoint().x);
                int realIndex = 
                    columnModel.getColumn(index).getModelIndex();
                return getRealModel().getColumns().get(realIndex).getLongDescription();
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public FaxListTableModel<T> getRealModel() {
        return (FaxListTableModel<T>)((TableSorter)dataModel).getTableModel();
    }  
    
    public TableSorter getSorter() {
        return (TableSorter)dataModel;
    }
    
    public TooltipJTable(FaxListTableModel<T> model) {
        super(new TableSorter(model));
        getSorter().setTableHeader(getTableHeader());
        getTableHeader().setReorderingAllowed(false);
        setRowHeight(getFontMetrics(getFont()).getHeight() + 4);
    }
    
    public String getColumnCfgString() {
        StringBuilder res = new StringBuilder();
        
        int recvCol = 0;
        for (int i = 0; i < getColumnCount(); i++) {
            recvCol = (i + 1) * getSorter().getSortingStatus(i); // HACK: getSortingStatus returns 1, -1, 0 in the "right way" for this
            if (recvCol != 0)
                break;
        }
        
        res.append(recvCol).append('|');
        
        Enumeration<TableColumn> colEnum = getColumnModel().getColumns();
        while (colEnum.hasMoreElements()) {
            TableColumn col = colEnum.nextElement();
            res.append(col.getIdentifier()).append(':').append(col.getWidth()).append('|');
        }
        
        return res.toString();
    }
    
    public void setColumnCfgString(String newCfg) {
        if ((newCfg == null) || (newCfg.length() == 0))
            return;
        
        String[] cfg = Utils.fastSplit(newCfg, '|'); //newCfg.split("\\|");            
        if (cfg.length < 1)
            return;
        
        try {
            int sort = Integer.parseInt(cfg[0]);
            if ((sort != 0) && (Math.abs(sort) <= getColumnCount()))
                getSorter().setSortingStatus(Math.abs(sort) - 1, (sort > 0) ? TableSorter.ASCENDING : TableSorter.DESCENDING);
        } catch (NumberFormatException e1) {
            log.log(Level.WARNING, "Couldn't parse value: " + cfg[0]);
        }
        
        for (int i = 1; i < cfg.length; i++) {
            try {
                int pos = cfg[i].indexOf(':');
                if (pos >= 0) {
                    String id = cfg[i].substring(0, pos);
                    int val = Integer.parseInt(cfg[i].substring(pos + 1));
                    
                    Enumeration<TableColumn> colEnum = getColumnModel().getColumns();
                    while (colEnum.hasMoreElements()) {
                        TableColumn col = colEnum.nextElement();
                        if (col.getIdentifier().equals(id)) {
                            col.setPreferredWidth(val);
                            break;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                log.log(Level.WARNING, "Couldn't parse value: " + cfg[i]);
            }
        } 
    }
    
    public FaxJob<T> getJobForRow(int rowIndex) {
        return getRealModel().getJob(getSorter().modelIndex(rowIndex));
    }
    
    @Override
    public void columnAdded(TableColumnModelEvent e) {
        // Set identifier 
        getColumnModel().getColumn(e.getToIndex()).setIdentifier(getRealModel().getColumns().get(e.getToIndex()).name());
        super.columnAdded(e);
    }
    
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        
        int realRow = getSorter().modelIndex(row);
        int realCol =  getColumnModel().getColumn(column).getModelIndex(); 
        FaxListTableModel<T> realModel = getRealModel(); 
        
        Font customFnt = realModel.getCellFont(realRow, realCol);
        if (customFnt != null) {
            comp.setFont(customFnt);
        }
        
        if (!isCellSelected(row, column)) {
            Color customColor = realModel.getCellBackgroundColor(realRow, realCol);
            comp.setBackground(customColor);
            
            customColor = realModel.getCellForegroundColor(realRow, realCol);
            comp.setForeground(customColor);
        }
        
        return comp;
    }
    
}

