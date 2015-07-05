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
package yajhfc.model.servconn;

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import yajhfc.filters.FilterableObject;
import yajhfc.model.FmtItem;

public interface FaxJob<T extends FmtItem> extends FilterableObject {

    
    /**
     * Returns the data saved in the specified column
     * @param column
     * @return
     */
    public Object getData(T column);
    
    /**
     * Returns the "raw" value of the given job properties if available.
     * @param properties the properties to retrieve
     * @return A map containing the successfully loaded properties or null
     */
    public Map<String,String> getJobProperties(String... properties);
    
    /**
     * Does some work on the HylaFax job using a HylafaxClient.
     * Throws an UnsupportedOperationException if the fax job is not a HylafaxJob.
     */
    public Object doHylafaxWork(HylafaxWorker worker) throws IOException, ServerResponseException;
    
    /**
     * Returns the data saved in the specified column
     * @param columnIndex
     * @return
     */
    public Object getData(int columnIndex);
    
    /**
     * Sets the data saved in the specified column.
     * 
     * @param column
     * @return
     */
    public void setData(T column, Object value);
    
    /**
     * Sets the data saved in the specified column.
     * 
     * @param column
     * @param fireEvent determines if an event should fire if the data changed
     * @return
     */
    public void setData(T column, Object value, boolean fireEvent);
    
    /**
     * Sets the data saved in the specified column.
     * 
     * @param column
     * @return
     */
    public void setData(int columnIndex, Object value);
    
    /**
     * Sets the data saved in the specified column.
     * 
     * @param column
     * @param fireEvent determines if an event should fire if the data changed
     * @return
     */
    public void setData(int columnIndex, Object value, boolean fireEvent);
    
    /**
     * Returns a value that can be used to uniquely identify this
     * job in its JobList
     * @return
     */
    public Object getIDValue();
    
    /**
     * Returns if this Job had an error.
     * @return
     */
    public boolean isError();
    
    /**
     * Deletes this fax job
     */
    public void delete() throws IOException, ServerResponseException;
    
    /**
     * Returns the list of accessible documents associated with this job
     * @return
     */
    public Collection<FaxDocument> getDocuments() throws IOException, ServerResponseException;
    
    /**
     * Returns the list of accessible documents associated with this job.
     * Documents associated with the job but inaccessible are returned in inaccessibleDocs
     * @return
     */
    public Collection<FaxDocument> getDocuments(Collection<String> inaccessibleDocs) throws IOException, ServerResponseException;
    
    /**
     * Returns the communication log for this job or null if no log exists.
     * @return
     */
    public FaxDocument getCommunicationsLog() throws IOException, ServerResponseException;
    
    /**
     * Returns if this fax job has been read
     * @return
     */
    public boolean isRead();
    
    /**
     * Sets if this job has been read.
     * @param isRead
     */
    public void setRead(boolean isRead);
    
    /**
     * Sets if this job has been read without firing an event.
     * -> Use with care!
     * @param isRead
     * @param fireEvent fire an event?
     */
    public void setRead(boolean isRead, boolean fireEvent);
        
    /**
     * Suspends the transmit of this fax job
     */
    public void suspend() throws IOException, ServerResponseException;
    
    /**
     * Resumes the transmit of this fax job
     */
    public void resume() throws IOException, ServerResponseException;
    
    /**
     * Returns the JobState (usually a cached value)
     * @return
     */
    public JobState getJobState();
    
    /**
     * Returns the current JobState.
     * In contrast to getJobState, this call may make a round trip to the server get the most current state.
     * @return
     */
    public JobState getCurrentJobState();
    
    /**
     * Returns the fax job list this job belongs to
     * @return
     */
    public FaxJobList<T> getParent();
}
