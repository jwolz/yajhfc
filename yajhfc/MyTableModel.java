package yajhfc;
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

import java.awt.Font;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel {
    
    protected String[][] rawData;
    /**
     * jobs: *All* Jobs from the HylaFAX server
     */
    protected YajJob[] jobs; 
    protected YajJobFilter jobFilter = null;
    protected int rowCount = 0;
    /**
     * visibleJobs: Only the visible Jobs (after jobFilter has been applied).
     * n.b.: if jobFilter == null then visibleJobs == jobs
     */
    protected YajJob[] visibleJobs;
    
    public Vector<FmtItem> columns;
    
    
    public void setJobFilter(YajJobFilter jobFilter) {
        this.jobFilter = jobFilter;
        refreshVisibleJobs();
    }
    
    public YajJobFilter getJobFilter() {
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
    
    protected YajJob createYajJob(String[] data) {
        return new SentYajJob(columns, data);
    }
    
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
                    if (jobFilter.jobIsVisible(jobs[i])) {
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
    
    public YajJob getJob(int rowIndex) {
        return visibleJobs[rowIndex];
    }
    
    public YajJob getRealJob(int rowIndex) {
        return jobs[rowIndex];
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
}
