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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.VirtualColumnType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;
import yajhfc.virtualcolumnstore.VirtColPersister;

public class ReadStateFaxListTableModel<T extends FmtItem> extends FaxListTableModel<T> {
    protected Font readFont = null;
    protected Font unreadFont = null;    
    protected EventListenerList listenerList = new EventListenerList();
    
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
    
    public ReadStateFaxListTableModel(FaxJobList<T> jobs) {
        super(jobs);
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
                    if (persistence==null)
                        return;
                    
                    Map<String,FaxJob<T>> oldJobMap = buildIDMap(oldJobList);
                    final List<FaxJob<T>> newUnread = new ArrayList<FaxJob<T>>();

                    for (FaxJob<T> newJob : newJobList) {
                        String key = VirtColPersister.getKeyForFaxJob(newJob);
                        Object oIsRead = persistence.getValue(key, VirtualColumnType.READ);
                        boolean isRead = (oIsRead != null && ((Boolean)oIsRead).booleanValue());

                        FaxJob<T> oldJob = oldJobMap.get(key);
                        if (oldJob == null) { // Job is new
                            if (!jobIsInProgress(newJob) && !isRead) {
                                // New Job that is not in progress (i.e. ready) and still unread
                                // -> consider as new job ready for viewing
                                newUnread.add(newJob);
                            }
                        } else { // Existing job
                            if (jobIsInProgress(oldJob) && !jobIsInProgress(newJob) && !isRead) {
                                // If the old job was in process, but the new one is ready and still unread
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

                public void readStateChanged(FaxJobList<T> source, final FaxJob<T> job,
                        boolean oldState, boolean newState) {
                    final int row = Utils.identityIndexOf(jobs.getJobs(), job);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireTableRowsUpdated(row,row);
                            fireReadStateChanged();
                        }
                    });
                }
                
                public void columnChanged(FaxJobList<T> source, FaxJob<T> job,
                        T column, int columnIndex, Object oldValue,
                        Object newValue) {
                    // Do nothing, handled in FaxListTableModel
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
   
    
}
