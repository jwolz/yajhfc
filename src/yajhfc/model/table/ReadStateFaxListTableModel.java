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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;
import yajhfc.readstate.PersistentReadState;
import yajhfc.readstate.ReadStateChangedListener;

public class ReadStateFaxListTableModel<T extends FmtItem> extends FaxListTableModel<T> 
    implements ReadStateChangedListener {
    protected Font readFont = null;
    protected Font unreadFont = null;    
    protected EventListenerList listenerList = new EventListenerList();
    protected PersistentReadState persistentReadState;
    
    public Font getReadFont() {
        return readFont;
    }
    
    public void setReadFont(Font readFont) {
        this.readFont = readFont;
    }
    
    public Font getUnreadFont() {
        return unreadFont;
    }
    
    public void setUnreadFont(Font unreadFont) {
        this.unreadFont = unreadFont;
    }
    
    public ReadStateFaxListTableModel(FaxJobList<T> jobs, PersistentReadState persistentReadState) {
        super(jobs);
        setPersistentReadState(persistentReadState);
    }

    
    
    public void addUnreadItemListener(UnreadItemListener<T> l) {
        listenerList.add(UnreadItemListener.class, l);
    }

    public void removeUnreadItemListener(UnreadItemListener<T> l) {
        listenerList.remove(UnreadItemListener.class, l);
    }
    
    /**
     * Returns the number of unread faxes. Currently this iterates over the list
     * of jobs, so this method should not be called often.
     * @return
     */
    public int getNumberOfUnreadFaxes() {
        //TODO: More efficient implementation:
        if (visibleJobs == null)
            return 0;
        
        int numUnread = 0;
        for (FaxJob<T> job : visibleJobs) {
            if (!job.isRead()) {
                numUnread++;
            }
        }
        return numUnread; 
    }
        
    @SuppressWarnings("unchecked")
    protected void fireNewUnreadItemsAvailable(Collection<FaxJob<T>> items, boolean oldDataNull) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        UnreadItemEvent<T> evt = new UnreadItemEvent<T>(this, items, oldDataNull);
        
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==UnreadItemListener.class) {
                ((UnreadItemListener<T>)listeners[i+1]).newItemsAvailable(evt);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void fireReadStateChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==UnreadItemListener.class) {
                ((UnreadItemListener<T>)listeners[i+1]).readStateChanged();
            }
        }
    }
    
    protected Map<Object,FaxJob<T>> buildIDMap(List<FaxJob<T>> source) {
        Map<Object,FaxJob<T>> idMap = new HashMap<Object,FaxJob<T>>(source.size());
        for (FaxJob<T> job : source) {
            idMap.put(job.getIDValue(), job);
        }
        return idMap;
    }
    
    @Override
    public void setJobs(FaxJobList<T> jobs) {
        if (jobs != this.jobs) {
            if (this.jobs != null) {
                this.jobs.removeFaxJobListListener(getJobsListener());
            }
            super.setJobs(jobs);
            if (jobs != null) {
                jobs.addFaxJobListListener(getJobsListener());
            }
        }
    }
    
    private FaxJobListListener<T> jobsListener;
    private FaxJobListListener<T> getJobsListener() {
        if (jobsListener == null) {
            jobsListener = new FaxJobListListener<T>() {
                public void faxJobsUpdated(FaxJobList<T> source,
                        final List<FaxJob<T>> oldJobList, final List<FaxJob<T>> newJobList) {
                    Map<Object,FaxJob<T>> oldJobIDs = buildIDMap(oldJobList);
                    final List<FaxJob<T>> newUnread = new ArrayList<FaxJob<T>>();

                    for (FaxJob<T> newJob : newJobList) {
                        Object jobID = newJob.getIDValue();
                        newJob.initializeRead(persistentReadState.isRead(jobID.toString()));

                        FaxJob<T> oldJob = oldJobIDs.get(jobID);
                        if (oldJob == null) { // Job is new
                            if (!jobIsInProgress(newJob)) {
                                // New Job that is not in process (i.e. ready) 
                                // -> consider as new job ready for viewing
                                newUnread.add(newJob);
                            }
                        } else { // Existing job
                            if (jobIsInProgress(oldJob) && !jobIsInProgress(newJob)) {
                                // If the old job was in process, but the new one is ready
                                // -> consider as new job ready for viewing
                                newUnread.add(newJob);
                            }
                        }
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (newUnread.size() > 0) {
                                fireNewUnreadItemsAvailable(newUnread, (oldJobList.size() == 0));
                            }
                            fireReadStateChanged();
                        }
                    });
                }

                private boolean jobIsInProgress(FaxJob<T> job) {
                    switch (job.getJobState()) {
                    case DONE:
                    case FAILED:
                        return false;
                    default:
                        return true;
                    }
                }

                public void readStateChanged(FaxJobList<T> source, FaxJob<T> job,
                        boolean oldState, boolean newState) {
                    persistentReadState.setRead(job.getIDValue().toString(), newState);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireReadStateChanged();
                        }
                    });
                }
            };
        }
        return jobsListener;
    }

    
    @Override
    public Font getCellFont(int row, int col) {
        
        if (getJob(row).isRead())
            return readFont;
        else
            return unreadFont;
    }
    

    public void readStateChanged(PersistentReadState sender,
            Set<String> changedFaxes) {
        int[] updateRows = new int[changedFaxes.size()];
        int updateRowPtr = 0;
        List<FaxJob<T>> jobList = jobs.getJobs();
        
        for (int i=0; i<jobList.size(); i++) {
            FaxJob<T> job = jobList.get(i);
            String id = job.getIDValue().toString();
            
            if (changedFaxes.contains(id)) {
                // Do not use the normal setter here to avoid a unnecessary update of the read state persister
                job.initializeRead(sender.isRead(id));
                updateRows[updateRowPtr++] = i;
            }
        }

        int[] updRows;
        if (updateRowPtr == updateRows.length) {
            updRows = updateRows;
        } else {
            updRows = new int[updateRowPtr];
            System.arraycopy(updateRows, 0, updRows, 0, updateRowPtr);
        }
        SwingUtilities.invokeLater(new TableUpdater(updRows));
    }
    
    public void cleanupReadState() {
        if (jobs == null)
            return;
        
        Set<String> existingJobs = new HashSet<String>();
        for (FaxJob<T> j : jobs.getJobs()) {
            existingJobs.add((String)j.getIDValue());
        }
        persistentReadState.cleanupState(existingJobs);
    }
    
    public PersistentReadState getPersistentReadState() {
        return persistentReadState;
    }

    public void setPersistentReadState(PersistentReadState persistentReadState) {
        if (this.persistentReadState != null) {
            this.persistentReadState.removeReadStateChangedListener(this);
        }
        this.persistentReadState = persistentReadState;
        persistentReadState.addReadStateChangedListener(this);
    }
    
    protected class TableUpdater implements Runnable {
        protected final int[] updateRows;
        
        public void run() {
            for (int i = 0; i < updateRows.length; i++) {
                int row = updateRows[i];
                fireTableRowsUpdated(row, row);
            }
            
            fireReadStateChanged();
        }

        public TableUpdater(int[] updateRows) {
            super();
            this.updateRows = updateRows;
        }

    }
}
