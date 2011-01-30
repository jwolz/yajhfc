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
package yajhfc.model.servconn;

import java.awt.Window;
import java.io.IOException;

import yajhfc.HylaClientManager;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.server.ServerOptions;

/**
 * @author jonas
 *
 */
public interface FaxListConnection {

    /**
     * Returns the list of sent fax jobs.
     * @return
     */
    public FaxJobList<JobFormat> getSentJobs();
    
    /**
     * Returns the list of fax jobs in the send queue.
     * @return
     */
    public FaxJobList<JobFormat> getSendingJobs();
    
    /**
     * Returns the list of received faxes.
     * @return
     */
    public FaxJobList<RecvFormat> getReceivedJobs();
    
    /**
     * Returns the list of archived faxes or null if no archived jobs are available.
     * @return
     */
    public FaxJobList<QueueFileFormat> getArchivedJobs();
    
    /**
     * Connects to the server to retrieve the lists of faxes.
     * The lists may be come available (and the appropriate listeners fire) any time after this method has been called,
     * even before this method returns.
     * Returns true if the connection attempt was successful and false otherwise
     */
    public boolean connect(boolean adminMode);
    
    /**
     * Disconnects from the server.
     */
    public void disconnect();
    
    /**
     * Sets new options and reloads the settings.
     */
    public void setOptions(ServerOptions so);
    
    /**
     * Sets the UI components to use for feedback to the user 
     * @param parentWindow
     */
    public void setUI(Window parentWindow);
    
    /**
     * Returns if a server status is available for this connection type
     * @return
     */
    public boolean isStatusAvailable();
    /**
     * Returns a String representing the server status or null if no status is available.
     * @return
     */
    public String getStatusText();
    
    /**
     * Returns the underlying HylaClientManager if available or null
     */
    public HylaClientManager getClientManager();
    
    /**
     * Adds a fax list connection listener
     * @param l
     */
    public void addFaxListConnectionListener(FaxListConnectionListener l);
    
    /**
     * Removed the specified fax list connection listener 
     * @param l
     */
    public void removeFaxListConnectionListener(FaxListConnectionListener l);
    
    /**
     * Notifies the fax list connection handler that multiple related operations will
     * be made on the fax jobs.
     * This method can be used for optimization purposes (i.e. by keeping the connection alive).
     * Usage and implementation of this method are completely optional. It is allowed to call this method multiple times.
     * If you call this method, however, you will have to make sure that for every call to this method,
     * endMultiOperation() gets called once (i.e. two calls to begin -> two calls to end).
     */
    public void beginMultiOperation() throws IOException;
    
    /**
     * Notifies the fax list connection handler that the last multi operation has been completed.
     */
    public void endMultiOperation();
    
    /**
     * Returns the (logical) connection state of the connection
     * @return
     */
    public ConnectionState getConnectionState();
    
    /**
     * Refreshes the server status
     */
    public void refreshStatus();
    
    /**
     * Refreshes the fax lists
     */
    public void refreshFaxLists();
    
    /**
     * Returns the ServerOptions used to create this connection
     * @return
     */
    public ServerOptions getOptions();
}
