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
 */
package yajhfc.model.servconn.hylafax;

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;

import yajhfc.model.JobFormat;

public class SendingFaxJob extends SentFaxJob {
    private static final long serialVersionUID = 1;
    
    protected SendingFaxJob(AbstractHylaFaxJobList<JobFormat> parent,
            String[] data) {
        super(parent, data);
    }
    
    @Override
    public void deleteImpl(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        synchronized (hyfc) {
            Job job = getJob(hyfc);
            hyfc.kill(job);
        }
    }
    
    @Override
    public void suspendImpl(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        synchronized (hyfc) {
            Job job = getJob(hyfc);

            hyfc.suspend(job);
        }
    }
    
    @Override
    public void resumeImpl(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        synchronized (hyfc) {
            Job job = getJob(hyfc);
            hyfc.submit(job);
        }
    }
    
    
}
