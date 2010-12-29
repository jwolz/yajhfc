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
     * @param column
     * @return A map containing the successfully loaded properties or null
     */
    public Map<String,String> getJobProperties(String... properties);
    
    /**
     * Returns the data saved in the specified column
     * @param columnIndex
     * @return
     */
    public Object getData(int columnIndex);
    
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
     */
    public void initializeRead(boolean isRead);
        
    /**
     * Suspends the transmit of this fax job
     */
    public void suspend() throws IOException, ServerResponseException;
    
    /**
     * Resumes the transmit of this fax job
     */
    public void resume() throws IOException, ServerResponseException;
    
    /**
     * Returns the JobState 
     * @return
     */
    public JobState getJobState();
    
    /**
     * Returns the fax job list this job belongs to
     * @return
     */
    public FaxJobList<T> getParent();
}
