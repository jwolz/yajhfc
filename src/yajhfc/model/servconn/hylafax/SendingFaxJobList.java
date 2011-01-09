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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Vector;

import yajhfc.model.JobFormat;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxJob;

/**
 * @author jonas
 *
 */
public class SendingFaxJobList extends AbstractHylaFaxJobList<JobFormat> {
    
    protected SendingFaxJobList(HylaFaxListConnection parent) {
        super(parent, parent.fo.getParent().sendingfmt);
    }

    @Override
    protected FaxJob<JobFormat> createFaxJob(String[] data) {
        return new SendingFaxJob(this, data);
    }

    @Override
    protected Vector<?> getJobListing(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        synchronized (hyfc) {
            hyfc.jobfmt(columns.getFormatString(SPLIT_CHAR));
            return hyfc.getList("sendq");
        }
    }

    @Override
    public TableType getJobType() {
        return TableType.SENDING;
    }

}
