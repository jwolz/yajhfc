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
package yajhfc.model.table;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import yajhfc.virtualcolumnstore.VirtColChangeListener;
import yajhfc.virtualcolumnstore.VirtColPersister;

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
    
    protected VirtColPersister persistence;
    
    protected VirtColChangeListener persistenceListener = new VirtColChangeListener() {
        public void columnsChanged(Set<String> inserts, Set<String> updates,
                Set<String> deletes) {
            // deletes are simply ignored
            
            Map<String,FaxJob<T>> idMap = getIDMap();
            
            for (String key : inserts) {
                final FaxJob<T> job = idMap.get(key);
                if (job != null)
                    persistence.updateToFaxJob(job, true);
            }
            for (String key : updates) {
                final FaxJob<T> job = idMap.get(key);
                if (job != null)
                    persistence.updateToFaxJob(job, true);
            }
        }
    };
    
    private Map<String,FaxJob<T>> idMap;
    protected Map<String,FaxJob<T>> getIDMap() {
        if (idMap==null) {
            idMap = buildIDMap(getJobs().getJobs());
        }
        return idMap;
    }
    
    
    public VirtColPersister getPersistence() {
        return persistence;
    }


    public void setPersistence(VirtColPersister persistence) {
        if (persistence != this.persistence) {
            if (this.persistence != null) {
                this.persistence.removeVirtColChangeListener(persistenceListener);
            }
            this.persistence = persistence;
            if (persistence != null) {
                persistence.addVirtColChangeListener(persistenceListener);
                persistence.updateToAllFaxJobs(getJobs().getJobs(), true);
            }
        }
    }


    public static <U extends FmtItem> Map<String,FaxJob<U>> buildIDMap(List<FaxJob<U>> source) {
        Map<String,FaxJob<U>> idMap = new HashMap<String,FaxJob<U>>(source.size());
        for (FaxJob<U> job : source) {
            idMap.put(VirtColPersister.getKeyForFaxJob(job), job);
        }
        return idMap;
    }
    
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
        return getJob(rowIndex).getData(columnIndex);
    }
    
    public FaxJob<T> getJob(int rowIndex) {
        return visibleJobs.get(rowIndex);
    }
    
    public FaxJob<T> getRealJob(int rowIndex) {
        return jobs.getJobs().get(rowIndex);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !jobs.getColumns().get(columnIndex).isReadOnly();
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        getJob(rowIndex).setData(columnIndex, aValue);
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
            updateListener = new SwingFaxJobListListener<T>(true, false, true) {
                @Override
                protected void faxJobsUpdatedSwing(FaxJobList<T> source,
                        List<FaxJob<T>> oldJobList, List<FaxJob<T>> newJobList) {
                    refreshVisibleJobs();
                }
                
                @Override
                public void faxJobsUpdated(FaxJobList<T> source,
                        List<FaxJob<T>> oldJobList, List<FaxJob<T>> newJobList) {
                    if (persistence != null) {
                        persistence.updateToAllFaxJobs(newJobList, false);
                    }
                    super.faxJobsUpdated(source, oldJobList, newJobList);
                }
                
                @Override
                protected void columnChangedSwing(FaxJobList<T> source,
                        FaxJob<T> job, T column, int columnIndex,
                        Object oldValue, Object newValue) {
                    fireTableCellUpdated(Utils.identityIndexOf(jobs.getJobs(), job), columnIndex);
                }
                
                @Override
                public void columnChanged(FaxJobList<T> source, FaxJob<T> job,
                        T column, int columnIndex, Object oldValue,
                        Object newValue) {
                    if (persistence != null && column.getVirtualColumnType().isSaveable()) {
                        persistence.setValue(job, column.getVirtualColumnType(), newValue);
                    }
                    super.columnChanged(source, job, column, columnIndex, oldValue, newValue);
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
            idMap = null;
            if (jobs != null) {
                jobs.addFaxJobListListener(getUpdateListener());
                if (persistence != null) {
                    persistence.updateToAllFaxJobs(jobs.getJobs(), false);
                }
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
