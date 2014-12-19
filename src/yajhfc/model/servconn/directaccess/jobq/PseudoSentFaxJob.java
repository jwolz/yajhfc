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
package yajhfc.model.servconn.directaccess.jobq;

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import yajhfc.model.JobFormat;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.HylafaxWorker;
import yajhfc.model.servconn.JobState;
import yajhfc.model.servconn.defimpl.SerializableFaxJob;

/**
 * @author jonas
 *
 */
public class PseudoSentFaxJob implements SerializableFaxJob<JobFormat> {
    private static final long serialVersionUID = 1;
    protected final FaxJob<QueueFileFormat> wrapped;
    protected transient PseudoSentFaxJobList parent;
    
    public PseudoSentFaxJob(PseudoSentFaxJobList parent, FaxJob<QueueFileFormat> wrapped) {
        super();
        this.parent = parent;
        this.wrapped = wrapped;
    }

    public void delete() throws IOException, ServerResponseException {
        wrapped.delete();
    }

    public FaxDocument getCommunicationsLog() throws IOException, ServerResponseException {
        return wrapped.getCommunicationsLog();
    }

    public Collection<FaxDocument> getDocuments() throws IOException,
            ServerResponseException {
        return wrapped.getDocuments();
    }

    public Collection<FaxDocument> getDocuments(
            Collection<String> inaccessibleDocs) throws IOException,
            ServerResponseException {
        return wrapped.getDocuments(inaccessibleDocs);
    }

    public Object getFilterData(Object key) {
        return wrapped.getFilterData(key);
    }

    public Object getIDValue() {
        return wrapped.getIDValue();
    }

    public Map<String, String> getJobProperties(String... properties) {
        return wrapped.getJobProperties(properties);
    }
    
    public JobState getJobState() {
        return wrapped.getJobState();
    }
    
    public JobState getCurrentJobState() {
        return wrapped.getCurrentJobState();
    }
    
    public void initializeRead(boolean isRead) {
        wrapped.initializeRead(isRead);
    }

    public boolean isError() {
        return wrapped.isError();
    }

    public boolean isRead() {
        return wrapped.isRead();
    }

    public void resume() throws IOException, ServerResponseException {
        wrapped.resume();
    }

    public void setRead(boolean isRead) {
        wrapped.setRead(isRead);
    }

    public void suspend() throws IOException, ServerResponseException {
        wrapped.suspend();
    }

    public Object getData(int columnIndex) {
        //TODO: Optimize?
        return getData(parent.getColumns().getCompleteView().get(columnIndex));
    }

    public Object getData(JobFormat column) {
        return JobToQueueMapping.getMappingFor(column).mapParsedData(wrapped);
    }

    public FaxJobList<JobFormat> getParent() {
        return parent;
    }

    public void setParent(FaxJobList<JobFormat> parent) {
        this.parent = (PseudoSentFaxJobList)parent;
        ((SerializableFaxJob<QueueFileFormat>)wrapped).setParent(this.parent.wrapped);
    }

    public Object doHylafaxWork(HylafaxWorker worker)
            throws IOException, ServerResponseException {
        return wrapped.doHylafaxWork(worker);
    }

}
