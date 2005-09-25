package yajhfc;
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


import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

//JTable with tooltips and other extensions
//Create a new table with: new TooltipJTable(realTableModel)
public class TooltipJTable extends JTable {
 
 @Override
 protected JTableHeader createDefaultTableHeader() {
     return new JTableHeader(columnModel) {
         public String getToolTipText(MouseEvent event) {
             int index = columnModel.getColumnIndexAtX(event.getPoint().x);
             int realIndex = 
                     columnModel.getColumn(index).getModelIndex();
             return getRealModel().columns.get(realIndex).longdesc;
         }
     };
 }
 
 public MyTableModel getRealModel() {
     return (MyTableModel)((TableSorter)dataModel).tableModel;
 }  
 
 public TableSorter getSorter() {
     return (TableSorter)dataModel;
 }
 
 public TooltipJTable(MyTableModel model) {
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
     
     String[] cfg = newCfg.split("\\|");            
     if (cfg.length < 1)
         return;
     
     try {
         int sort = Integer.parseInt(cfg[0]);
         if ((sort != 0) && (Math.abs(sort) <= getColumnCount()))
             getSorter().setSortingStatus(Math.abs(sort) - 1, (sort > 0) ? TableSorter.ASCENDING : TableSorter.DESCENDING);
     } catch (NumberFormatException e1) {
         System.err.println("Couldn't parse value: " + cfg[0]);
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
             System.err.println("Couldn't parse value: " + cfg[i]);
         }
     } 
 }
 
 @Override
 public void columnAdded(TableColumnModelEvent e) {
     // Set identifier 
     getColumnModel().getColumn(e.getToIndex()).setIdentifier(getRealModel().columns.get(e.getToIndex()).fmt);
     super.columnAdded(e);
 }
 
 @Override
 public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
     Component comp = super.prepareRenderer(renderer, row, column);
     Font customFnt = getRealModel().getCellFont(getSorter().modelIndex(row), getColumnModel().getColumn(column).getModelIndex());
     if (customFnt != null)
         comp.setFont(customFnt);
     return comp;
 }
}

class MyTableModel extends AbstractTableModel {
    
    protected String[][] data;
    public Vector<FmtItem> columns;
    private HashMap<Long,Object> dataCache; // cache to avoid too much parsing
    
    /**
     * Returns a custom font for the table cell.
     * A return value of null means "use default font"
     * @param row
     * @param col
     * @return
     */
    public Font getCellFont(int row, int col) {
        return null;
    }
    
    public void flushCache() {
        dataCache.clear();
    }
    
    private Long getCacheKey(int row, int col) {
        return ((long)row << 32) | (long)col;
    }
    
    public void setData(String[][] newData) {
        if (!Arrays.deepEquals(data, newData)) {
            data = newData;
            flushCache();
            fireTableDataChanged();
        }
    }
    public int getColumnCount() {
        if (columns != null)
            return columns.size();
        else
            return 0;
    }
    
    public int getRowCount() {
        if (data == null)
            return 0;
        else
            return data.length;
    }
    
    public String getStringAt(int rowIndex, int columnIndex) {
        try {
            return data[rowIndex][columnIndex];
        }
        catch (ArrayIndexOutOfBoundsException a) {
            return " ";
        }
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Class <?>dataClass = getColumnClass(columnIndex);
        //System.out.println("getValue: " + rowIndex + "," + columnIndex);
        
        if (dataClass == String.class) { // shortcut for String -> non-"cached"
            /* String res = getStringAt(rowIndex, columnIndex).trim();
            if (res.length() == 0)
                return null;
            else
                return res; */
            return getStringAt(rowIndex, columnIndex);
        } else {                
            Long key = getCacheKey(rowIndex, columnIndex);
            
            Object result = dataCache.get(key);
            if ((result != null) || dataCache.containsKey(key))
                return result; // value is cached -> return it
            
            String res = getStringAt(rowIndex, columnIndex).trim();
            if (res.length() > 0) {
                //System.out.println("Parse: " + res);
                try {
                    if (dataClass == Integer.class)
                        result = Integer.valueOf(res);
                    else if (dataClass == Float.class)
                        result = Float.valueOf(res);
                    else if (dataClass == Double.class)
                        result = Double.valueOf(res);
                    else if (dataClass == Boolean.class) // "*" if true, " " otherwise
                        result = (res.trim().length() > 0);
                    else if (dataClass == Date.class)
                        result = columns.get(columnIndex).dateFormat.fmtIn.parse(res);
                    else
                        result = res;
                } catch (NumberFormatException e) {
                    // If not parseable, return NaN
                    System.err.println("Not a number: " + res);
                    //result = Float.NaN;
                    result = null;
                } catch (ParseException e) {
                    System.err.println("Not a parseable date: " + res);
                    result = null;
                }    
            } else
                result = null;
            dataCache.put(key, result);
            return result;
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public String getColumnName(int column) {
        return columns.get(column).desc;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).dataClass;
    }
    
    public MyTableModel() {
        dataCache = new HashMap<Long,Object>();
    }
}

class UnreadItemEvent extends EventObject {
    private Set<String> items = null;
    
    public Set<String> getItems() {
        return items;
    }
    
    public UnreadItemEvent(Object source, Set<String> items) {
        super(source);
        this.items = items;
    }
}

interface UnreadItemListener extends EventListener {
    public void newUnreadItemsAvailable(UnreadItemEvent evt);
}

// Tablemodel with read/unread state
class UnReadMyTableModel extends MyTableModel {
    public Font readFont = null;
    public Font unreadFont = null;    
    public boolean defaultState = false;
    private FmtItem hashCol = null;
    private int hashColIdx = -3;
    EventListenerList listenerList = new EventListenerList();
    protected HashMap<String,Boolean> readMap = null;

    public void addUnreadItemListener(UnreadItemListener l) {
        listenerList.add(UnreadItemListener.class, l);
    }

    public void removeUnreadItemListener(UnreadItemListener l) {
        listenerList.remove(UnreadItemListener.class, l);
    }
        
    protected void fireNewUnreadItemsAvailable(Set<String> items) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        UnreadItemEvent evt = new UnreadItemEvent(this, items);
        
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==UnreadItemListener.class) {
                ((UnreadItemListener)listeners[i+1]).newUnreadItemsAvailable(evt);
            }
        }
    }
    
    @Override
    public void setData(String[][] arg0) {
        Set<String> oldDataKeys = getDataKeySet();
        super.setData(arg0);
        Set<String> newDataKeys = getDataKeySet();
        if (newDataKeys != null && oldDataKeys != null) {
            newDataKeys.removeAll(oldDataKeys);
            if (newDataKeys.size() > 0)
                fireNewUnreadItemsAvailable(newDataKeys);
        }
    }
    
    public void setHashCol(FmtItem hashCol) {
        this.hashCol = hashCol;
        refreshHashColIdx();
    }
    
    public FmtItem getHashCol() {
        return hashCol;
    }
    
    public void setRead(String key, boolean state) {
        readMap.put(key, state);
        
        //fireTableRowsUpdated(row, row);
    }
    
    public boolean getRead(String key) {
        Boolean val = readMap.get(key);
        if (val == null)
            return defaultState;
        else
            return val;
    }
    
    private void refreshHashColIdx() {
        if (columns != null)
            hashColIdx = columns.indexOf(hashCol);
        else
            hashColIdx = -2;
    }
    
    public Set<String> getDataKeySet() {
        if (hashColIdx < 0)
            return null;
        
        int rowC = getRowCount();
        HashSet<String> dataKeys = new HashSet<String>(rowC);           
        for (int i=0; i < rowC; i++) {
            dataKeys.add(data[i][hashColIdx]);
        }
        return dataKeys;
    }
    
    // Delete entries removed from the data
    public void shrinkReadState() {                        
        Set<String> dataKeys = getDataKeySet();
        if (dataKeys != null)
            readMap.keySet().retainAll(dataKeys);
    }
    
    /*@Override
    public void fireTableDataChanged() {
        shrinkReadState();
        super.fireTableDataChanged();
    }*/
    
    @Override
    public void fireTableStructureChanged() {
        refreshHashColIdx();
        super.fireTableStructureChanged();
    }
    
    @Override
    public Font getCellFont(int row, int col) {
        if (hashColIdx < 0)
            return null;
        
        if (getRead(data[row][hashColIdx]))
            return readFont;
        else
            return unreadFont;
    }
    
    public void readFromStream(InputStream fin) throws IOException {
        BufferedReader bIn = new BufferedReader(new InputStreamReader(fin));
        
        readMap.clear();
        String line = null;
        while ((line = bIn.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#") && line.length() > 0) {
                    readMap.put(line, !defaultState);
            }
        }
        bIn.close();
    }
    
    public void storeToStream(OutputStream fOut) throws IOException {
        BufferedWriter bOut = new BufferedWriter(new OutputStreamWriter(fOut));
        
        shrinkReadState();
        bOut.write("# " + utils.AppShortName + " " + utils.AppVersion + " configuration file\n");
        bOut.write("# This file contains a list of faxes considered read\n\n");
        
        for ( String key : readMap.keySet() ) {
            if (readMap.get(key).booleanValue() != defaultState)
                bOut.write(key + "\n");
        }
        bOut.close();
    }
    
    /*public String getStateString() {
        StringBuilder res = new StringBuilder();
        
        shrinkReadState();
        for ( String key : readMap.keySet() ) {
            if (readMap.get(key).booleanValue() != defaultState)
                res.append(key).append('|');
        }
        return res.toString();
    }
    
    public void setStateString(String str) {
        readMap.clear();
        if (str.length() == 0)
            return;
        
        String[] selKeys = str.split("\\|");
        for (int i=0; i < selKeys.length; i++)
            readMap.put(selKeys[i], !defaultState);
    }*/
    
    public UnReadMyTableModel(FmtItem hashCol) {
        super();
        this.hashCol = hashCol;
        readMap = new HashMap<String,Boolean>();
    }
}
