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
package yajhfc.model.servconn.defimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.VirtualColumnType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;
import yajhfc.model.servconn.FaxListConnection;

/**
 * @author jonas
 *
 */
public abstract class AbstractFaxJobList<T extends FmtItem> implements
        FaxJobList<T> {
    static final Logger log = Logger.getLogger(AbstractFaxJobList.class.getName());
    
    protected final FmtItemList<T> columns;
    protected final List<FaxJobListListener<T>> listeners = new ArrayList<FaxJobListListener<T>>();
    protected List<FaxJob<T>> jobs = Collections.emptyList();
    protected final FaxListConnection parent;
    
    public FmtItemList<T> getColumns() {
        return columns;
    }

    public synchronized List<FaxJob<T>> getJobs() {
        return jobs;
    }
        
    protected void setJobs(List<FaxJob<T>> newJobs) {
        List<FaxJob<T>> oldJobs;
        synchronized (this) {
            oldJobs = jobs;
            jobs = newJobs;
        }
        fireFaxJobsUpdated(newJobs, oldJobs);
    }

    public synchronized void fireFaxJobsUpdated(List<FaxJob<T>> newJobs, List<FaxJob<T>> oldJobs) {
        if (Utils.debugMode) {
            log.finest("Fire faxJobsUpdated; newJobs=" + newJobs + "; oldJobs=" + oldJobs);
        }
        for (FaxJobListListener<T> l : listeners) {
            l.faxJobsUpdated(this, oldJobs, newJobs);
        }
    }
    
    
    public synchronized void addFaxJobListListener(FaxJobListListener<T> l) {
        listeners.add(l);
    }
    
    public synchronized void removeFaxJobListListener(FaxJobListListener<T> l) {
        listeners.remove(l);
    }
    
//    public synchronized void fireReadStateChanged(FaxJob<T> job, boolean oldState, boolean newState) {
//        if (Utils.debugMode) {
//            log.fine("Fire read state changed for " + job + "; oldState=" + oldState + "; newState=" + newState);
//        }
//        for (FaxJobListListener<T> l : listeners) {
//            l.readStateChanged(this, job, oldState, newState);
//        }
//    }
    
    public synchronized void fireColumnChanged(FaxJob<T> job, T column, int columnIndex, Object oldValue, Object newValue) {
        if (Utils.debugMode) {
            log.fine("Fire column changed for " + job + "; column=" + column.name() + " (idx=" + columnIndex + "); oldValue=" + oldValue + "; newValue=" + newValue);
        }
        for (FaxJobListListener<T> l : listeners) {
            l.columnChanged(this, job, column, columnIndex, oldValue, newValue);
            if (column.getVirtualColumnType() == VirtualColumnType.READ)
                l.readStateChanged(this, job, (oldValue != null && ((Boolean)oldValue).booleanValue()), (newValue != null && ((Boolean)newValue).booleanValue()));
        }
    }
    
    public boolean isShowingErrorsSupported() {
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public void loadJobsFromCache(Map<String, Object> cache, String keyPrefix) {
        List<FaxJob<T>> newJobs = (List<FaxJob<T>>) cache.get(keyPrefix);
        if (newJobs != null) {
            log.fine("Loading jobs from cache with prefix " + keyPrefix);
            for (FaxJob<T> job : newJobs) {
                ((SerializableFaxJob<T>)job).setParent(this);
            }
            setJobs(newJobs);
        }
    }
    
    public void saveJobsToCache(Map<String, Object> cache, String keyPrefix) {
        cache.put(keyPrefix, jobs);
    }
    
    public FaxListConnection getParent() {
        return parent;
    }
    
    protected AbstractFaxJobList(FmtItemList<T> columns, FaxListConnection parent) {
        super();
        this.columns = columns;
        this.parent = parent;
    }


}
