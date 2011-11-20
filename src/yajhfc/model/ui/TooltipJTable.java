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
    
    @SuppressWarnings("unchecked")
    public FaxJob<T>[] getSelectedJobs() {
        int[] selRows = getSelectedRows();
        FaxJob<T>[] jobs = new FaxJob[selRows.length];
        
        for (int i=0; i<selRows.length; i++) {
            jobs[i] = getJobForRow(selRows[i]);
        }
        return jobs;
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

