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
package yajhfc.model.table;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;
import yajhfc.model.servconn.defimpl.SwingFaxJobListListener;

public class FaxListTableModel<T extends FmtItem> extends AbstractTableModel {
    static final Logger log = Logger.getLogger(FaxListTableModel.class.getName());
    
    /**
     * jobs: *All* Jobs from the HylaFAX server
     */
    protected FaxJobList<T> jobs; 
    
    protected Filter<FaxJob<T>,T> jobFilter = null;
    /**
     * visibleJobs: Only the visible Jobs (after jobFilter has been applied). <br>
     * n.b.: if jobFilter == null then visibleJobs == jobs <br>
     * If a filter is applied, only the rowCount first elements of visibleJobs are valid
     * (i.e. visibleJobs.length > rowCount is possible)
     */
    protected List<FaxJob<T>> visibleJobs;
    
    private static final Color defErrorColor = new Color(255, 230, 230);
    
    protected Color errorColor = defErrorColor;
    
    public void setJobFilter(Filter<FaxJob<T>,T> jobFilter) {
        if (jobFilter != this.jobFilter) {
            this.jobFilter = jobFilter;
            refreshVisibleJobs();
        }
    }
    
    public Filter<FaxJob<T>,T> getJobFilter() {
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
    
    /**
     * Reloads the visible Jobs array. Called if either jobs[] or jobFilter has changed.
     */
    protected void refreshVisibleJobs() {
        refreshVisibleJobsWithoutEvent();
        log.fine("Jobs refreshed, firing table data changed");
        fireTableDataChanged();
    }

    private void refreshVisibleJobsWithoutEvent() {
        if (Utils.debugMode) {
            log.finest("refreshing jobs: jobFilter=" + jobFilter + "; jobs=" + (jobs == null ? "<jobs null>" : jobs.getJobs()));
        }
        if (jobs == null) {
            visibleJobs = null;
        } else {
            if (jobFilter == null) {
                visibleJobs = jobs.getJobs();
            } else {
                visibleJobs = new ArrayList<FaxJob<T>>(jobs.getJobs().size());
                jobFilter.initFilter(getColumns());
                
                for (FaxJob<T> job : jobs.getJobs()) {
                    if (jobFilter.matchesFilter(job)) {
                        visibleJobs.add(job);
                    }
                }
                if (Utils.debugMode) {
                    log.finest("refreshing jobs: resulting visibleJobs=" + visibleJobs);
                }
            }
        }
    }
    
    public FmtItemList<T> getColumns() {
        return (jobs != null) ? jobs.getColumns() : null;
    }
    
    public int getColumnCount() {
        if (getColumns() != null)
            return getColumns().size();
        else
            return 0;
    }
    
    public int getRowCount() {
        return (visibleJobs == null) ? 0 : visibleJobs.size();
    }
    
    public int getRealRowCount() {
        if (jobs == null)
            return 0;
        else
            return jobs.getJobs().size();
    }
        
    public Object getValueAt(int rowIndex, int columnIndex) {
        return visibleJobs.get(rowIndex).getData(columnIndex);
    }
    
    public FaxJob<T> getJob(int rowIndex) {
        return visibleJobs.get(rowIndex);
    }
    
    public FaxJob<T> getRealJob(int rowIndex) {
        return jobs.getJobs().get(rowIndex);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public String getColumnName(int column) {
        return getColumns().get(column).getDescription();
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumns().get(columnIndex).getDataType();
    }
    
    public TableType getTableType() {
        return jobs.getJobType();
    }

    public Color getErrorColor() {
        return errorColor;
    }
    
    public void setErrorColor(Color errorColor) {
        this.errorColor = errorColor;
    }
    
    public FaxJobList<T> getJobs() {
        return jobs;
    }
   
    private FaxJobListListener<T> updateListener;
    private FaxJobListListener<T> getUpdateListener() {
        if (updateListener == null) {
            updateListener = new SwingFaxJobListListener<T>(true, false) {
                @Override
                protected void faxJobsUpdatedSwing(FaxJobList<T> source,
                        List<FaxJob<T>> oldJobList, List<FaxJob<T>> newJobList) {
                    refreshVisibleJobs();
                }
            };
        }
        return updateListener;
    }
    public void setJobs(FaxJobList<T> jobs) {
        if (this.jobs != jobs) {
            if (this.jobs != null) {
                this.jobs.removeFaxJobListListener(getUpdateListener());
            }
            this.jobs = jobs;
            if (jobs != null) {
                jobs.addFaxJobListListener(getUpdateListener());
            }
            refreshVisibleJobsWithoutEvent();
            log.fine("New Jobs have been set, firing tableStructureChanged");
            fireTableStructureChanged();
        }
    }
    

    public FaxListTableModel(FaxJobList<T> jobs) {
        super();
        setJobs(jobs);
    }
}
