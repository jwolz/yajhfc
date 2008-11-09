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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import yajhfc.readstate.PersistentReadState;
import yajhfc.readstate.ReadStateChangedListener;

/**
 * Implements a table model with read/unread state
 */
public class UnReadMyTableModel extends MyTableModel implements ReadStateChangedListener {
    public Font readFont = null;
    public Font unreadFont = null;    
    EventListenerList listenerList = new EventListenerList();
    protected PersistentReadState persistentReadState;
    
    protected static final Comparator<YajJob> jobComparator = new Comparator<YajJob>() {
        @SuppressWarnings("unchecked")
        public int compare(YajJob o1, YajJob o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null) // Implicit: o2 != null
                return -1;
            if (o2 == null) // Implicit: o1 != null
                return 1;
            return ((Comparable)o1.getIDValue()).compareTo(o2.getIDValue());
        }
    };
    
    
    public UnReadMyTableModel(PersistentReadState persistentReadState) {
        super();
        setPersistentReadState(persistentReadState);
    }
    
    public void addUnreadItemListener(UnreadItemListener l) {
        listenerList.add(UnreadItemListener.class, l);
    }

    public void removeUnreadItemListener(UnreadItemListener l) {
        listenerList.remove(UnreadItemListener.class, l);
    }
        
    protected void fireNewUnreadItemsAvailable(Collection<RecvYajJob> items, boolean oldDataNull) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        UnreadItemEvent evt = new UnreadItemEvent(this, items, oldDataNull);
        
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==UnreadItemListener.class) {
                ((UnreadItemListener)listeners[i+1]).newItemsAvailable(evt);
            }
        }
    }
    
    @Override
    protected YajJob createYajJob(String[] data) {
        return new RecvYajJob(columns, data, persistentReadState);
    }
    
    @Override
    public void setData(String[][] arg0) {
        YajJob[] oldJobs = jobs;
        super.setData(arg0);
        
        // We have got new data...
        if (oldJobs != jobs && arg0 != null) {
            ArrayList<RecvYajJob> newUnread = new ArrayList<RecvYajJob>();   
            Arrays.sort(jobs, jobComparator);
            
            // Both arrays are sorted in the same way, so iterate through both of them to find out the new unread jobs
            int i = 0, j = 0;
            if (oldJobs != null) {
                for (; i < oldJobs.length && j < jobs.length; ) {
                    RecvYajJob oldJob = (RecvYajJob)oldJobs[i];
                    RecvYajJob newJob = (RecvYajJob)jobs[j];

                    int compValue = jobComparator.compare(oldJob, newJob);
                    if (compValue == 0) { // equal
                        if (!newJob.isRead() && !newJob.isInProgress() && oldJob.isInProgress()) {
                            // If the old job was in progress, but the new is no more, we have a new
                            // unread job ready for viewing
                            newUnread.add(newJob);
                        }
                        // Increment both indices:
                        i++;
                        j++;
                    } else if (compValue > 0) { // newJob is "lesser"
                        // If the newJob is "lesser than" the oldJob it is new
                        // => add it
                        if (!newJob.isRead() && !newJob.isInProgress()) {
                            newUnread.add(newJob);
                        }
                        // Increment only the index for the new jobs
                        j++;
                    } else { // compValue < 0 => newJob is "greater"
                        // The oldJob has been deleted
                        // Increment only the index for the old jobs:
                        i++;
                    }
                }
            }
            // Add any remaining new jobs:
            for ( ; j < jobs.length; j++) {
                RecvYajJob newJob = (RecvYajJob)jobs[j];
                if (!newJob.isRead() && !newJob.isInProgress()) {
                    newUnread.add(newJob);
                }
            }
            if (newUnread.size() > 0) {
                fireNewUnreadItemsAvailable(newUnread, (oldJobs == null ? true : oldJobs.length == 0));
            }
        }
//        if (arg0 != null) {
//            for ( int i=0; i < jobs.length; i++ ) {
//                RecvYajJob j = (RecvYajJob)jobs[i];
//                if (j != null) {                   
//                    JobState val;
//                    if (oldRead != null)
//                        val = oldRead.get(j.getIDValue());
//                    else
//                        val = null;
//                    
//                    if (val != null) {
//                        j.setRead(val.read);
//                        if (!val.read && val.inProgress && !j.isInProgress()) {
//                            newUnread.add(j);
//                        }
//                    } else if (!j.isInProgress()) {
//                        newUnread.add(j);
//                    }
//                }
//            }
//            if (newUnread.size() > 0)
//                fireNewUnreadItemsAvailable(newUnread, (oldRead == null ? true : oldRead.size() == 0));
//        }
    }
    
    
//    /**
//     * Returns a map containing the state of all jobs
//     * @return
//     */
//    public Map<Object, JobState> getReadMap() {
//        if (jobs == null)
//            return null;
//        
//        HashMap<Object,JobState> dataMap = new HashMap<Object,JobState>();  
//        for (int i=0; i < jobs.length; i++) {
//            RecvYajJob j = (RecvYajJob)jobs[i];
//            if (j != null)
//                dataMap.put(j.getIDValue(), JobState.getInstance(j.isRead(), j.isInProgress()));
//        }
//        return dataMap;
//    }
//    
    
    /*@Override
    public void fireTableDataChanged() {
        shrinkReadState();
        super.fireTableDataChanged();
    }*/
    
    @Override
    public Font getCellFont(int row, int col) {
        
        if (((RecvYajJob)getJob(row)).isRead())
            return readFont;
        else
            return unreadFont;
    }
    
//    public void loadReadState(PersistentReadState loadFrom) {
//        Set<String> oldRead = loadFrom.loadReadFaxes();
//        
//        for ( int i=0; i < jobs.length; i++ ) {
//            RecvYajJob j = (RecvYajJob)jobs[i];
//            if (j != null)
//                j.setRead(oldRead.contains(j.getIDValue()));
//        }
//    }
//    
//    public void storeReadState(PersistentReadState storeTo) {
//        List<String> readFaxes = new ArrayList<String>();
//        for ( int i=0; i < jobs.length; i++ ) {
//            RecvYajJob j = (RecvYajJob)jobs[i];
//            if (j.isRead()) {
//                readFaxes.add(j.getIDValue().toString());
//            }
//        }
//        
//        storeTo.persistReadState(readFaxes);
//    }
    
    
//    public static class JobState {
//        public final boolean read;
//        public final boolean inProgress;
//        
//        public JobState(boolean read, boolean inProgress) {
//            this.read = read;
//            this.inProgress = inProgress;
//        }
//        
//        public static final JobState READ_INPROGRESS = new JobState(true, true);
//        public static final JobState UNREAD_INPROGRESS = new JobState(false, true);
//        public static final JobState READ_NOTINPROGRESS = new JobState(true, false);
//        public static final JobState UNREAD_NOTINPROGRESS = new JobState(false, false);
//        
//        public static JobState getInstance(boolean read, boolean inProgress) {
//            if (read) {
//                if (inProgress) {
//                    return READ_INPROGRESS;
//                } else {
//                    return READ_NOTINPROGRESS;
//                }
//            } else {
//                if (inProgress) {
//                    return UNREAD_INPROGRESS;
//                } else {
//                    return UNREAD_NOTINPROGRESS;
//                }
//            }
//        }
//    }

    public void readStateChanged(PersistentReadState sender,
            Collection<String> changedFaxes) {
        int[] updateRows = new int[changedFaxes.size()];
        int updateRowPtr = 0;
        
        for (String id : changedFaxes) {
            int idx = findYajJobByID(id);
            if (idx >= 0) {
                ((RecvYajJob)jobs[idx]).setRead(sender.isRead(id));
                updateRows[updateRowPtr++] = idx;
            }
        }
        SwingUtilities.invokeLater(new TableUpdater(updateRows, updateRowPtr));
    }
    
    public void cleanupReadState() {
        if (jobs == null)
            return;
        
        Set<String> existingJobs = new HashSet<String>();
        for (YajJob j : jobs) {
            existingJobs.add((String)j.getIDValue());
        }
        persistentReadState.cleanupState(existingJobs);
    }
    
    /**
     * Returns the index of the YajJob with the given ID or -1 if it can not be found
     * @param id
     * @return
     */
    protected int findYajJobByID(String id) {
        int top = jobs.length-1;
        int bot = 0;
        int idx;

        while (bot <= top) {
            idx = (bot + top) / 2;
            int cmpVal = id.compareTo((String)jobs[idx].getIDValue());
            if (cmpVal == 0) {
                return idx;
            } else if (cmpVal < 0) { // id is smaller than job ID
                top = idx - 1;
            } else { // cmpVal > 0 => id is greater than job ID
                bot = idx + 1;
            }
        }
        return -1;
    }
    
    protected class TableUpdater implements Runnable {
        protected final int[] updateRows;
        protected final int numUpdateRows;
        
        public void run() {
            for (int i = 0; i < numUpdateRows; i++) {
                int row = updateRows[i];
                fireTableRowsUpdated(row, row);
            }
        }

        public TableUpdater(int[] updateRows, int numUpdateRows) {
            super();
            this.updateRows = updateRows;
            this.numUpdateRows = numUpdateRows;
        }

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
}

