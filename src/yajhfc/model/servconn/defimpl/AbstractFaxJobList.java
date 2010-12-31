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
package yajhfc.model.servconn.defimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;

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
    
    public synchronized void fireReadStateChanged(FaxJob<T> job, boolean oldState, boolean newState) {
        if (Utils.debugMode) {
            log.fine("Fire read state changed for " + job + "; oldState=" + oldState + "; newState=" + newState);
        }
        for (FaxJobListListener<T> l : listeners) {
            l.readStateChanged(this, job, oldState, newState);
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
    
    protected AbstractFaxJobList(FmtItemList<T> columns) {
        super();
        this.columns = columns;
    }


}
