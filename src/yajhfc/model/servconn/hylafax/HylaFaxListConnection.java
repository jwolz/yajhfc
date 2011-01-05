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

public class HylaFaxListConnection implements FaxListConnection {
    static final Logger log = Logger.getLogger(HylaFaxListConnection.class.getName());
    
    protected final List<FaxListConnectionListener> listeners = new ArrayList<FaxListConnectionListener>();
    protected HylaClientManager clientManager;
    protected Window parentWindow;
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
    
    protected Cache listCache;
    
    public synchronized void addFaxListConnectionListener(FaxListConnectionListener l) {
        listeners.add(l);
    }

    public synchronized void removeFaxListConnectionListener(FaxListConnectionListener l) {
        listeners.remove(l);
    }

    protected synchronized void setConnectionState(ConnectionState newState) {
        if (newState != connectionState) {
            log.fine("Connection state change: " + connectionState + "->" + newState);
            for (FaxListConnectionListener l : listeners) {
                l.connectionStateChange(connectionState, newState);
            }
            connectionState = newState;
        }
    }

    protected void saveToCache() {
        if (!Cache.useForNextLogin) {
            log.finer("Not using cache this time.");
            return;
        }
        try {
            Map<String,Object> cacheMap = listCache.getCachedData();
            cacheMap.clear();
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
            listCache.writeToCache(fo);
            // Clear map as objects inside are no longer needed
            cacheMap.clear();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error saving cache:", e);
        }
    }
    
    protected void loadFromCache() {
        if (!Cache.useForNextLogin) {
            log.finer("Not using cache this time.");
            Cache.useForNextLogin = true;
            return;
        }
        try {
            if (listCache.readFromCache(fo)) {
                Map<String,Object> cacheMap = listCache.getCachedData();
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
                // Clear map as objects inside are no longer needed
                cacheMap.clear();
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
        log.fine("Connecting; adminMode=" + adminMode);
        setConnectionState(ConnectionState.CONNECTING);
        setStatusText(_("Connecting..."));
        if (fo.useFaxListCache) {
            log.fine("Loading cache...");
            putDefaultCacheCheckData();
            loadFromCache();
        }
        clientManager.setAdminMode(adminMode);
        if (clientManager.forceLogin(parentWindow) != null) {
            log.fine("ClientManager successfully logged in");
            refreshTimer.schedule(statusRefresher = createStatusRefresher(), 
                    0, fo.statusUpdateInterval);
            refreshTimer.schedule(jobRefresher = createJobListRefresher(), 
                    0, fo.tableUpdateInterval);
            log.fine("Refreshers scheduled");
            setConnectionState(ConnectionState.CONNECTED);
            return true;
        } else {
            log.fine("ClientManager failed to log in");
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
        log.fine("Disconnecting...");
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
        log.fine("Cancelled refresh tasks");
        if (oldState == ConnectionState.CONNECTED) {
            // Only save cache if cleanly connected...
            if (fo.useFaxListCache) {
                log.fine("Saving cache");
                // Do not put check data here, options may have changed...
                saveToCache();
            }
        }
        clientManager.forceLogout();
        log.fine("forceLogout completed");
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
        log.fine("disconnectCleanups completed");
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
        createOrDestroyOptionalObjects();
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
    
    public void setUI(Window parentWindow) {
        this.parentWindow = parentWindow;
    }
    
    public HylaFaxListConnection(FaxOptions fo, Window parentWindow) {
        setUI(parentWindow);
        clientManager = new HylaClientManager(fo);
        this.fo = fo;
        receivedJobs = createRecvdList();
        sentJobs = createSentList();
        sendingJobs = createSendingList();
        createOrDestroyOptionalObjects();
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
    
    protected void createOrDestroyOptionalObjects() {
        if (archiveJobs == null) {
            if (fo.showArchive) {
                log.finer("Created new archive list");
                archiveJobs = createArchiveList();
            }
        } else {
            if (!fo.showArchive) {
                log.finer("Removed archive list");
                archiveJobs = null;
            } else {
                log.finer("Reloaded archive list settings");
                archiveJobs.reloadSettings(fo);
            }
        }
        if (listCache == null) {
            if (fo.useFaxListCache) {
                listCache = new Cache();
            }
        } else {
            if (!fo.useFaxListCache) {
                listCache = null;
            }
        }
    }
    
    protected void putDefaultCacheCheckData() {
        Map<String,Object> checkData = listCache.getCheckData();
        checkData.clear();
        checkData.put("AppVersion", Utils.AppVersion);
        checkData.put("host", fo.host);
        checkData.put("port", fo.port);
        checkData.put("user", fo.user);
        checkData.put("showArchive", fo.showArchive);
        checkData.put("archiveLocation", fo.archiveLocation);
        checkData.put("dateOffsetSecs", fo.dateOffsetSecs);
        checkData.put("faxListConnectionType", fo.faxListConnectionType);
        checkData.put("directAccessSpoolPath", fo.directAccessSpoolPath);
        
        checkData.put("tzone", fo.tzone);
        checkData.put("recvfmt", fo.recvfmt.toString());
        checkData.put("sentfmt", fo.sentfmt.toString());
        checkData.put("sendingfmt", fo.sendingfmt.toString());
        checkData.put("archivefmt", fo.archiveFmt.toString());
    }
    
    public String getStatusText() {
        return statusText;
    }

    protected synchronized void setStatusText(String statusText) {
        if (!this.statusText.equals(statusText)) {
            if (Utils.debugMode)
                log.finer("Set new status text: " + statusText);
            this.statusText = statusText;
            
            for (FaxListConnectionListener l : listeners) {
                l.serverStatusChanged(statusText);
            }
        }
    }
    
    protected synchronized void fireRefreshComplete(RefreshKind refreshKind, boolean success) {
        if (Utils.debugMode)
            log.fine("refreshComplete: refreshKind=" + refreshKind + ", success=" + success);
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
        log.fine("Begin multi operation");
        beginServerTransaction();
    }
    
    public void endMultiOperation() {
        log.fine("End multi operation");
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
        private static final boolean PROFILING = false;
        private final Level PROFILING_LOGLEVEL = Level.FINE;
        private boolean cancelled = false;
        
        public synchronized void run() {
            boolean success = false;
            try {
                long time = 0;
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
                                if (PROFILING || Utils.debugMode) {
                                    log.log(PROFILING_LOGLEVEL, "About to poll recvq");
                                    time = System.currentTimeMillis();
                                }
                                
                                receivedJobs.pollForNewJobs(hyfc);
                                
                                if (PROFILING || Utils.debugMode) {
                                    log.log(PROFILING_LOGLEVEL, "recvq polled successfully; time to poll was: " + (System.currentTimeMillis() - time));
                                }
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
                                if (PROFILING || Utils.debugMode) {
                                    log.log(PROFILING_LOGLEVEL, "About to poll doneq");
                                    time = System.currentTimeMillis();
                                }
                                
                                sentJobs.pollForNewJobs(hyfc);
                                if (PROFILING || Utils.debugMode) {
                                    log.log(PROFILING_LOGLEVEL, "doneq polled successfully; time to poll was: " + (System.currentTimeMillis() - time));
                                }
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
                                if (PROFILING || Utils.debugMode) {
                                    log.log(PROFILING_LOGLEVEL, "About to poll sendq");
                                    time = System.currentTimeMillis();
                                }
                                
                                sendingJobs.pollForNewJobs(hyfc);
                                
                                if (PROFILING || Utils.debugMode) {
                                    log.log(PROFILING_LOGLEVEL, "sendq polled successfully; time to poll was: " + (System.currentTimeMillis() - time));
                                }
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
                    if (PROFILING || Utils.debugMode) {
                        log.log(PROFILING_LOGLEVEL, "About to poll archive");
                        time = System.currentTimeMillis();
                    }
                    
                    archiveJobs.pollForNewJobs(null);
                    
                    if (PROFILING || Utils.debugMode) {
                        log.log(PROFILING_LOGLEVEL, "archive polled successfully; time to poll was: " + (System.currentTimeMillis() - time));
                    }
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
