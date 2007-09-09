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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.EventListenerList;

/**
 * Implements a table model with read/unread state
 */
class UnReadMyTableModel extends MyTableModel {
    public Font readFont = null;
    public Font unreadFont = null;    
    EventListenerList listenerList = new EventListenerList();

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
        return new RecvYajJob(columns, data);
    }
    
    @Override
    public void setData(String[][] arg0) {
        Map<Object,JobState> oldRead = getReadMap();
        super.setData(arg0);
        
        ArrayList<RecvYajJob> newUnread = new ArrayList<RecvYajJob>();
        if (arg0 != null) {
            for ( int i=0; i < jobs.length; i++ ) {
                RecvYajJob j = (RecvYajJob)jobs[i];
                if (j != null) {                   
                    JobState val;
                    if (oldRead != null)
                        val = oldRead.get(j.getIDValue());
                    else
                        val = null;
                    
                    if (val != null) {
                        j.setRead(val.read);
                        if (!val.read && val.inProgress && !j.isInProgress()) {
                            newUnread.add(j);
                        }
                    } else if (!j.isInProgress()) {
                        newUnread.add(j);
                    }
                }
            }
            if (newUnread.size() > 0)
                fireNewUnreadItemsAvailable(newUnread, (oldRead == null ? true : oldRead.size() == 0));
        }
    }
    
    /**
     * Returns a map containing the state of all jobs
     * @return
     */
    public Map<Object, JobState> getReadMap() {
        if (jobs == null)
            return null;
        
        HashMap<Object,JobState> dataMap = new HashMap<Object,JobState>();  
        for (int i=0; i < jobs.length; i++) {
            RecvYajJob j = (RecvYajJob)jobs[i];
            if (j != null)
                dataMap.put(j.getIDValue(), JobState.getInstance(j.isRead(), j.isInProgress()));
        }
        return dataMap;
    }
    
    
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
    
    public void loadReadState(PersistentReadState loadFrom) {
        Set<String> oldRead = loadFrom.loadReadFaxes();
        
        for ( int i=0; i < jobs.length; i++ ) {
            RecvYajJob j = (RecvYajJob)jobs[i];
            if (j != null)
                j.setRead(oldRead.contains(j.getIDValue()));
        }
    }
    
    public void storeReadState(PersistentReadState storeTo) {
        List<String> readFaxes = new ArrayList<String>();
        for ( int i=0; i < jobs.length; i++ ) {
            RecvYajJob j = (RecvYajJob)jobs[i];
            if (j.isRead()) {
                readFaxes.add(j.getIDValue().toString());
            }
        }
        
        storeTo.persistReadState(readFaxes);
    }
    
    public UnReadMyTableModel() {
        super();
    }
    
    public static class JobState {
        public final boolean read;
        public final boolean inProgress;
        
        public JobState(boolean read, boolean inProgress) {
            this.read = read;
            this.inProgress = inProgress;
        }
        
        public static final JobState READ_INPROGRESS = new JobState(true, true);
        public static final JobState UNREAD_INPROGRESS = new JobState(false, true);
        public static final JobState READ_NOTINPROGRESS = new JobState(true, false);
        public static final JobState UNREAD_NOTINPROGRESS = new JobState(false, false);
        
        public static JobState getInstance(boolean read, boolean inProgress) {
            if (read) {
                if (inProgress) {
                    return READ_INPROGRESS;
                } else {
                    return READ_NOTINPROGRESS;
                }
            } else {
                if (inProgress) {
                    return UNREAD_INPROGRESS;
                } else {
                    return UNREAD_NOTINPROGRESS;
                }
            }
        }
    }
}

