package yajhfc.model;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import yajhfc.FmtItem;
import yajhfc.FmtItemList;
import yajhfc.Utils;
import yajhfc.filters.Filter;

public abstract class MyTableModel<T extends FmtItem> extends AbstractTableModel {
    
    protected String[][] rawData;
    /**
     * jobs: *All* Jobs from the HylaFAX server
     */
    protected YajJob<T>[] jobs; 
    protected Filter<YajJob<T>,T> jobFilter = null;
    protected int rowCount = 0;
    /**
     * visibleJobs: Only the visible Jobs (after jobFilter has been applied). <br>
     * n.b.: if jobFilter == null then visibleJobs == jobs <br>
     * If a filter is applied, only the rowCount first elements of visibleJobs are valid
     * (i.e. visibleJobs.length > rowCount is possible)
     */
    protected YajJob<T>[] visibleJobs;
    
    public FmtItemList<T> columns;
    
    private static final Color defErrorColor = new Color(255, 230, 230);
    
    public Color errorColor = defErrorColor;
    
    public void setJobFilter(Filter<YajJob<T>,T> jobFilter) {
        this.jobFilter = jobFilter;
        refreshVisibleJobs();
    }
    
    public Filter<YajJob<T>,T> getJobFilter() {
        return jobFilter;
    }
    
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
    
    /**
     * Returns a custom background color for the table cell.
     * A return value of null means "use default color"
     * @param row
     * @param col
     * @return
     */
    public Color getCellBackgroundColor(int row, int col) {       
        if (Utils.getFaxOptions().markFailedJobs && getJob(row).isError()) {
            return errorColor;
        } else {
            return null;
        }
    }
    
    /**
     * Returns a custom foreground color for the table cell.
     * A return value of null means "use default color"
     * @param row
     * @param col
     * @return
     */
    public Color getCellForegroundColor(int row, int col) {  
        return null;
    }
    
    protected abstract YajJob<T> createYajJob(String[] data);
    
    @SuppressWarnings("unchecked")
    public void setData(String[][] newData) {
        if (!Arrays.deepEquals(rawData, newData)) {
            rawData = newData;
            if (newData != null) {
                jobs = new YajJob[newData.length];
                for (int i=0; i < newData.length; i++) {
                    jobs[i] = createYajJob(rawData[i]);
                }
            } else {
                jobs = null;
            }
            //fireTableDataChanged();
            refreshVisibleJobs();
        }
    }
    
    /**
     * Reloads the visible Jobs array. Called if either jobs[] or jobFilter has changed.
     */
    @SuppressWarnings("unchecked")
    protected void refreshVisibleJobs() {
        if (jobs == null) {
            rowCount = 0;
            visibleJobs = null;
        } else 
            if (jobFilter == null) {
                visibleJobs = jobs;
                rowCount = jobs.length;
            } else {
                rowCount = 0;
                visibleJobs = new YajJob[jobs.length];
                jobFilter.initFilter(columns);
                
                for (int i = 0; i < jobs.length; i++) {
                    if (jobFilter.matchesFilter(jobs[i])) {
                        visibleJobs[rowCount] = jobs[i];
                        rowCount++;
                    }
                }
            }
        fireTableDataChanged();
    }
    
    public int getColumnCount() {
        if (columns != null)
            return columns.size();
        else
            return 0;
    }
    
    public int getRowCount() {
        /*if (data == null)
            return 0;
        else
            return data.length;*/
        return rowCount;
    }
    
    public int getRealRowCount() {
        if (jobs == null)
            return 0;
        else
            return jobs.length;
    }
    
    public String getStringAt(int rowIndex, int columnIndex) {
        return visibleJobs[rowIndex].getStringData(columnIndex);
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return visibleJobs[rowIndex].getData(columnIndex);
    }
    
    public YajJob<T> getJob(int rowIndex) {
        return visibleJobs[rowIndex];
    }
    
    public YajJob<T> getRealJob(int rowIndex) {
        return jobs[rowIndex];
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public String getColumnName(int column) {
        return columns.get(column).getDescription();
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).getDataType();
    }
}
