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
import yajhfc.model.servconn.JobState;

/**
 * @author jonas
 *
 */
public class PseudoSentFaxJob implements FaxJob<JobFormat> {
    private static final long serialVersionUID = 1;
    protected final FaxJob<QueueFileFormat> wrapped;
    protected final PseudoSentFaxJobList parent;
    
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

}
