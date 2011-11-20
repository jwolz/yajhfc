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
package yajhfc.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Implements a view port that can be used to implement
 * a row header displaying line numbers.
 * @author jonas
 *
 */
public class NumberRowViewport extends JViewport {
    static final Logger log = Logger.getLogger(NumberRowViewport.class.getName());
    
    protected JTable rowNumberTable;
    protected JTable realTable;
    
    public NumberRowViewport(JTable realTable, JScrollPane parent) {
        initialize(realTable);
        attachToScrollPane(parent);
    }
    
    public NumberRowViewport(JTable realTable) {
        initialize(realTable);
    }

    protected void initialize(JTable realTable) {
        rowNumberTable = new JTable(new RowNumberTableModel(realTable.getModel()));
        rowNumberTable.getTableHeader().setResizingAllowed(false);
        rowNumberTable.getTableHeader().setReorderingAllowed(false);
        rowNumberTable.getColumnModel().getColumn(0).setPreferredWidth(37);
        rowNumberTable.setFocusable(false);
        setTableColors();
        
        setView(rowNumberTable);
        setPreferredSize(rowNumberTable.getPreferredSize());
        
        new SelectionSyncer(realTable, rowNumberTable);
        
        this.realTable = realTable;
    }
    
    private void setTableColors() {
        if (rowNumberTable != null) {
            rowNumberTable.setBackground(UIManager.getColor("TableHeader.background"));
            rowNumberTable.setForeground(UIManager.getColor("TableHeader.foreground"));
            rowNumberTable.setFont(UIManager.getFont("TableHeader.font"));
        }
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        setTableColors();
    }
    
    /**
     * Attaches this Viewport as row header to the given ScrollPane
     * @param parent
     */
    public void attachToScrollPane(JScrollPane parent) {
        parent.setRowHeader(this);
        parent.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowNumberTable.getTableHeader());
    }
    
    /**
     * Detaches this Viewport from the ScrollPane
     * @param parent
     */
    public void detachFromScrollPane() {
        if (getParent() instanceof JScrollPane) {
            JScrollPane parent = (JScrollPane)getParent();
            parent.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
            parent.setRowHeader(null);
        }
    }
    
    protected void reconfigureTable() {
        rowNumberTable.setRowHeight(realTable.getRowHeight());
    }
    
    /**
     * This table model has one column with row numbers and
     * automatically synchronizes with the given TableModel.
     * @author jonas
     *
     */
    protected class RowNumberTableModel extends AbstractTableModel
        implements TableModelListener {
        
        protected TableModel realModel;
        
        @Override
        public String getColumnName(int column) {
            return "   ";
        }
        
        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return realModel.getRowCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return Integer.valueOf(rowIndex+1);
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Integer.class;
        }
        
        public void tableChanged(TableModelEvent e) {
            switch (e.getType()) {
            case TableModelEvent.INSERT:
            case TableModelEvent.DELETE:
                fireTableChanged(new TableModelEvent(this, e.getFirstRow(), e.getLastRow(), TableModelEvent.ALL_COLUMNS, e.getType()));
                fireTableRowsUpdated(e.getLastRow(), getRowCount());
                break;
            case TableModelEvent.UPDATE:
                if (e.getFirstRow() == TableModelEvent.HEADER_ROW ||
                        e.getLastRow() == Integer.MAX_VALUE) {
                    fireTableDataChanged();
                    reconfigureTable();
                }
                break;
            }
        }
        
        public RowNumberTableModel(TableModel realModel) {
            this.realModel = realModel;
            realModel.addTableModelListener(this);
        }
    }
    
    /**
     * Synchronizes selection between two tables
     * @author jonas
     *
     */
    protected static class SelectionSyncer implements ListSelectionListener {

        protected JTable table1;
        protected JTable table2;
        protected boolean inSync = false;
        
        public SelectionSyncer(JTable table1, JTable table2) {
            this.table1 = table1;
            this.table2 = table2;
            
            table1.getSelectionModel().addListSelectionListener(this);
            table2.getSelectionModel().addListSelectionListener(this);
        }
        
        public void valueChanged(ListSelectionEvent e) {
            if (inSync) {
                return;
            }
            ListSelectionModel source = (ListSelectionModel)e.getSource();
            ListSelectionModel dest;
            if (source == table1.getSelectionModel()) {
                dest = table2.getSelectionModel();
            } else if (source == table2.getSelectionModel()) {
                dest = table1.getSelectionModel();
            } else {
                log.log(Level.WARNING, "Unknown selection: " + e.toString());
                return;
            }

            // Synchronize the changes:
            inSync = true;    
            if (e.getValueIsAdjusting()) {
                dest.setValueIsAdjusting(true);
            }
            
            int selStartIdx, noselStartIdx;
            boolean inSelInterval = false;
            for (int i = selStartIdx = noselStartIdx = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                if (source.isSelectedIndex(i)) {
                    if (!inSelInterval) {
                        selStartIdx = i;
                        if (noselStartIdx < i) {
                            dest.removeSelectionInterval(noselStartIdx, i-1);
                        }
                        inSelInterval = true;
                    }
                } else {
                    if (inSelInterval) {
                        noselStartIdx = i;
                        if (selStartIdx < i) {
                            dest.addSelectionInterval(selStartIdx, i-1);
                        }
                        inSelInterval = false;
                    }
                }
            }
            if (inSelInterval) {
                dest.addSelectionInterval(selStartIdx, e.getLastIndex());
            } else {
                dest.removeSelectionInterval(noselStartIdx, e.getLastIndex());
            }
            
            if (!e.getValueIsAdjusting()) {
                dest.setValueIsAdjusting(false);
            }
            inSync = false;
        }
    }
}
