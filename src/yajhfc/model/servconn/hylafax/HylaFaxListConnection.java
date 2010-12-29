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
package yajhfc.model.servconn.hylafax;

import static yajhfc.Utils._;
import gnu.hylafax.HylaFAXClient;

import java.awt.Window;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.FaxOptions;
import yajhfc.HylaClientManager;
import yajhfc.Utils;
import yajhfc.cache.Cache;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.ConnectionState;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.FaxListConnectionListener;
import yajhfc.model.servconn.FaxListConnectionListener.RefreshKind;
import yajhfc.model.servconn.directaccess.archive.ArchiveFaxJobList;
import yajhfc.util.ProgressWorker.ProgressUI;

public class HylaFaxListConnection implements FaxListConnection {
    static final Logger log = Logger.getLogger(HylaFaxListConnection.class.getName());
    
    protected final List<FaxListConnectionListener> listeners = new ArrayList<FaxListConnectionListener>();
    protected HylaClientManager clientManager;
    protected Window parentWindow;
    protected ProgressUI progressUI;
    protected ConnectionState connectionState = ConnectionState.DISCONNECTED;
    
    protected Timer refreshTimer = new Timer("ListRefresher", true);
    protected TimerTask statusRefresher;
    protected TimerTask jobRefresher;
    protected FaxOptions fo;
    
    protected String statusText = "";
    
    protected ManagedFaxJobList<RecvFormat> receivedJobs;
    protected ManagedFaxJobList<JobFormat> sentJobs;
    protected ManagedFaxJobList<JobFormat> sendingJobs;
    protected ManagedFaxJobList<QueueFileFormat> archiveJobs;
    
    public synchronized void addFaxListConnectionListener(FaxListConnectionListener l) {
        listeners.add(l);
    }

    public synchronized void removeFaxListConnectionListener(FaxListConnectionListener l) {
        listeners.remove(l);
    }

    protected synchronized void setConnectionState(ConnectionState newState) {
        if (newState != connectionState) {
            for (FaxListConnectionListener l : listeners) {
                l.connectionStateChange(connectionState, newState);
            }
            connectionState = newState;
        }
    }

    protected void saveToCache() {
        try {
            Cache c = new Cache();
            Map<String,Object> cacheMap = c.getCachedData();
            if (receivedJobs != null) {
                receivedJobs.saveJobsToCache(cacheMap, "receivedJobs");
            }
            if (sentJobs != null) {
                sentJobs.saveJobsToCache(cacheMap, "sentJobs");
            }
            if (sendingJobs != null) {
                sendingJobs.saveJobsToCache(cacheMap, "sendingJobs");
            }
            if (archiveJobs != null) {
                archiveJobs.saveJobsToCache(cacheMap, "archiveJobs");
            }
            c.writeToCache(fo);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error loading cache:", e);
        }
    }
    
    protected void loadFromCache() {
        if (!Cache.useForNextLogin) {
            log.finer("Not using cache this time.");
            Cache.useForNextLogin = true;
            return;
        }
        try {
            Cache c = new Cache();
            if (c.readFromCache(fo)) {
                Map<String,Object> cacheMap = c.getCachedData();
                if (receivedJobs != null) {
                    receivedJobs.loadJobsFromCache(cacheMap, "receivedJobs");
                }
                if (sentJobs != null) {
                    sentJobs.loadJobsFromCache(cacheMap, "sentJobs");
                }
                if (sendingJobs != null) {
                    sendingJobs.loadJobsFromCache(cacheMap, "sendingJobs");
                }
                if (archiveJobs != null) {
                    archiveJobs.loadJobsFromCache(cacheMap, "archiveJobs");
                }
                fireRefreshComplete(RefreshKind.FAXLISTS_FROM_CACHE, true);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error loading cache:", e);
        }
    }
    
    public boolean connect(boolean adminMode) {
        if (connectionState == ConnectionState.CONNECTED
         || connectionState == ConnectionState.CONNECTING) {
            throw new IllegalStateException("Already connecting.");
        }
        setConnectionState(ConnectionState.CONNECTING);
        clientManager.setAdminMode(adminMode);
        if (clientManager.forceLogin(parentWindow) != null) {
            if (fo.useFaxListCache) {
                loadFromCache();
            }
            refreshTimer.schedule(statusRefresher = createStatusRefresher(), 
                    0, fo.statusUpdateInterval);
            refreshTimer.schedule(jobRefresher = createJobListRefresher(), 
                    0, fo.tableUpdateInterval);
            setConnectionState(ConnectionState.CONNECTED);
            return true;
        } else {
            setConnectionState(ConnectionState.DISCONNECTED);
            return false;
        }
    }

    protected TimerTask createJobListRefresher() {
        return new JobListRefresher();
    }

    protected TimerTask createStatusRefresher() {
        return new StatusRefresher();
    }

    public void disconnect() {
        ConnectionState oldState = connectionState;
        setConnectionState(ConnectionState.DISCONNECTING);
        if (statusRefresher != null) {
            statusRefresher.cancel();
            statusRefresher = null;
            setStatusText(_("Disconnected."));
        }
        if (jobRefresher != null) {
            jobRefresher.cancel();
            jobRefresher = null;
        }
        
        if (oldState == ConnectionState.CONNECTED) {
            // Only save cache if cleanly connected...
            if (fo.useFaxListCache) {
                saveToCache();
            }
        }
        clientManager.forceLogout();
        if (receivedJobs != null) {
            receivedJobs.disconnectCleanup();
        }
        if (sentJobs != null) {
            sentJobs.disconnectCleanup();
        }
        if (sendingJobs != null) {
            sendingJobs.disconnectCleanup();
        }
        if (archiveJobs != null) {
            archiveJobs.disconnectCleanup();
        }
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    public FaxJobList<QueueFileFormat> getArchivedJobs() {
        return archiveJobs;
    }

    public HylaClientManager getClientManager() {
        return clientManager;
    }

    public FaxJobList<RecvFormat> getReceivedJobs() {
        return receivedJobs;
    }

    public FaxJobList<JobFormat> getSendingJobs() {
        return sendingJobs;
    }

    public FaxJobList<JobFormat> getSentJobs() {
        return sentJobs;
    }

    public boolean isStatusAvailable() {
        return true;
    }

    public void reloadSettings() {
        createOrDestroyArchiveList();
        clientManager.optionsChanged();
        if (receivedJobs != null) {
            receivedJobs.reloadSettings(fo);
        }
        if (sentJobs != null) {
            sentJobs.reloadSettings(fo);
        }
        if (sendingJobs != null) {
            sendingJobs.reloadSettings(fo);
        }
    }
    
    public void setUI(Window parentWindow, ProgressUI progressUI) {
        this.parentWindow = parentWindow;
        this.progressUI = progressUI;
    }
    
    public HylaFaxListConnection(FaxOptions fo, Window parentWindow, ProgressUI progressUI) {
        setUI(parentWindow, progressUI);
        clientManager = new HylaClientManager(fo);
        this.fo = fo;
        receivedJobs = createRecvdList();
        sentJobs = createSentList();
        sendingJobs = createSendingList();
        createOrDestroyArchiveList();
    }

    protected ManagedFaxJobList<JobFormat> createSendingList() {
        return new SendingFaxJobList(this);
    }

    protected ManagedFaxJobList<JobFormat> createSentList() {
        return new SentFaxJobList(this);
    }

    protected ManagedFaxJobList<RecvFormat> createRecvdList() {
        return new RecvdFaxJobList(this);
    }
    
    protected ManagedFaxJobList<QueueFileFormat> createArchiveList() {
        return new ArchiveFaxJobList(this, fo.archiveFmt, fo);
    }
    
    protected void createOrDestroyArchiveList() {
        if (archiveJobs == null) {
            if (fo.showArchive) {
                archiveJobs = createArchiveList();
            }
        } else {
            if (!fo.showArchive) {
                archiveJobs = null;
            } else {
                archiveJobs.reloadSettings(fo);
            }
        }
    }
    
    public String getStatusText() {
        return statusText;
    }

    protected synchronized void setStatusText(String statusText) {
        if (!this.statusText.equals(statusText)) {
            this.statusText = statusText;
            
            for (FaxListConnectionListener l : listeners) {
                l.serverStatusChanged(statusText);
            }
        }
    }
    
    protected synchronized void fireRefreshComplete(RefreshKind refreshKind, boolean success) {
        for (FaxListConnectionListener l : listeners) {
            l.refreshComplete(refreshKind, success);
        }
    }
        
    public HylaFAXClient beginServerTransaction() throws IOException {
        HylaFAXClient hyfc = clientManager.beginServerTransaction(parentWindow);
        if (hyfc == null) {
            throw new IOException("Could not get a HylaFAXClient connection.");
        }
        return hyfc;
    }
    
    public void endServerTransaction() {
        clientManager.endServerTransaction();
    }
    
    public void beginMultiOperation() throws IOException {
        beginServerTransaction();
    }
    
    public void endMultiOperation() {
        endServerTransaction();
    }
    
    public ConnectionState getConnectionState() {
        return connectionState;
    }
    
    class StatusRefresher extends TimerTask {
        public synchronized void run() {
            boolean success = false;
            try {
                HylaFAXClient hyfc = clientManager.beginServerTransaction(parentWindow);
                if (hyfc == null) {
                    setStatusText(Utils._("Could not log in"));
                    cancel();
                    disconnect();
                    return;
                } else {
                    try {
                        Vector<?> status;
                        synchronized (hyfc) {
                            log.finest("In hyfc monitor");
                            status = hyfc.getList("status");
                        }
                        log.finest("Out of hyfc monitor");
                        setStatusText(Utils.listToString(status, "\n"));
                        success = true;
                    } catch (SocketException se) {
                        log.log(Level.WARNING, "Socket Error refreshing the status, logging out.", se);
                        cancel();
                        disconnect();
                        return;
                    } catch (Exception e) {
                        setStatusText(_("Error refreshing the status:") + " " + e);
                        log.log(Level.WARNING, "Error refreshing the status:", e);
                    } finally {
                        clientManager.endServerTransaction();
                    }
                }                    
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Error refreshing the status:", ex);
            }
            fireRefreshComplete(RefreshKind.STATUS, success);
        }
    };
    
    class JobListRefresher extends TimerTask {
        private boolean cancelled = false;
        
        public synchronized void run() {
            boolean success = false;
            try {
                HylaFAXClient hyfc = clientManager.beginServerTransaction(parentWindow);
                if (hyfc == null) {
                    cancel();
                    disconnect();
                    return;
                } else {
                    if (cancelled)
                        return;
                    try {
                        try {
                            if (receivedJobs != null) {
                                //long time = System.currentTimeMillis();
                                receivedJobs.pollForNewJobs(hyfc);
                                //System.out.println("Time to poll recvq: " + (System.currentTimeMillis() - time));
                            }
                        } catch (SocketException se) {
                            log.log(Level.WARNING, "A socket error occured refreshing the recv table, logging out.", se);
                            cancel();
                            disconnect();
                            return;
                        } catch (Exception e) {
                            log.log(Level.WARNING, "An error occured refreshing the the recv table: ", e);
                        }
                        if (cancelled)
                            return;
                        try {
                            if (sentJobs != null) {
                                //long time = System.currentTimeMillis();
                                sentJobs.pollForNewJobs(hyfc);
                                //System.out.println("Time to poll doneq: " + (System.currentTimeMillis() - time));
                            }
                        } catch (SocketException se) {
                            log.log(Level.WARNING, "A socket error occured refreshing the sent table, logging out.", se);
                            cancel();
                            disconnect();
                            return;
                        } catch (Exception e) {
                            log.log(Level.WARNING, "An error occured refreshing the sent table: ", e);
                        }
                        if (cancelled)
                            return;
                        try {
                            if (sendingJobs != null) {
                                sendingJobs.pollForNewJobs(hyfc);
                            }
                        } catch (SocketException se) {
                            log.log(Level.WARNING, "A socket error occured refreshing the sending table, logging out.", se);
                            cancel();
                            disconnect();
                            return;
                        } catch (Exception e) {
                            log.log(Level.WARNING, "An error occured refreshing the sending table: ", e);
                        }
                        if (cancelled)
                            return;
                    } finally {
                        clientManager.endServerTransaction();
                    }
                }
                if (archiveJobs != null) {
                    archiveJobs.pollForNewJobs(null);
                }
                if (cancelled)
                    return;
                success = true;
            } catch (SocketException se) {
                log.log(Level.WARNING, "A socket error occured refreshing the tables, logging out.", se);
                cancel();
                disconnect();
                return;
            } catch (Exception e) {
                log.log(Level.WARNING, "An error occured refreshing the tables: ", e);
            }
            fireRefreshComplete(RefreshKind.FAXLISTS, success);
        }
        
        @Override
        public boolean cancel() {
            cancelled = true;
            return super.cancel();
        }
    }

    public void refreshFaxLists() {
        if (jobRefresher != null)
            jobRefresher.run();
    }

    public void refreshStatus() {
        if (statusRefresher != null)
            statusRefresher.run();
    }
}
