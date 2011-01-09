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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yajhfc.model.FmtItemList;
import yajhfc.model.JobFormat;
import yajhfc.model.TableType;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.defimpl.AbstractFaxJobList;
import yajhfc.model.servconn.hylafax.ManagedFaxJobList;
import yajhfc.server.ServerOptions;

/**
 * @author jonas
 *
 */
public class PseudoSentFaxJobList extends AbstractFaxJobList<JobFormat> 
    implements ManagedFaxJobList<JobFormat>, FaxJobListListener<QueueFileFormat> {
    
    protected final JobQueueFaxJobList wrapped;
    
    public PseudoSentFaxJobList(FmtItemList<JobFormat> columns, JobQueueFaxJobList wrapped, FaxListConnection parent) {
        super(columns, parent);
        this.wrapped = wrapped;
        wrapped.addFaxJobListListener(this);
    }

    public void disconnectCleanup() {
        wrapped.disconnectCleanup();
    }

    public void pollForNewJobs(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        wrapped.pollForNewJobs(hyfc);
    }

    public TableType getJobType() {
        return TableType.SENT;
    }
    
    @Override
    public boolean isShowingErrorsSupported() {
        return wrapped.isShowingErrorsSupported();
    }
    
    public void reloadSettings(ServerOptions fo) {
        JobToQueueMapping.getRequiredFormats(columns, wrapped.getColumns());
        wrapped.reloadSettings(fo);
    }

    public void faxJobsUpdated(FaxJobList<QueueFileFormat> source,
            List<FaxJob<QueueFileFormat>> oldJobList,
            List<FaxJob<QueueFileFormat>> newJobList) {
        // TODO: Optimize
        List<FaxJob<JobFormat>> result = new ArrayList<FaxJob<JobFormat>>(newJobList.size());
        for (FaxJob<QueueFileFormat> job : newJobList) {
            result.add(new PseudoSentFaxJob(this, job));
        }
        setJobs(result);
    }

    public void readStateChanged(FaxJobList<QueueFileFormat> source,
            FaxJob<QueueFileFormat> job, boolean oldState, boolean newState) {
        // TODO: Optimize
        for (FaxJob<JobFormat> wrapperJob : jobs) {
            if (((PseudoSentFaxJob)wrapperJob).wrapped == job) {
                fireReadStateChanged(wrapperJob, oldState, newState);
                return;
            }
        }
    }
}
