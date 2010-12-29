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
import java.util.Collections;
import java.util.List;

import yajhfc.file.FileFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.JobState;

public class RecvdFaxJob extends AbstractHylaFaxJob<RecvFormat> {
    private static final long serialVersionUID = 1;
    
    protected RecvdFaxJob(RecvdFaxJobList parent,
            String[] data) {
        super(parent, data);
    }

    @Override
    protected JobState calculateJobState() {
        String errorDesc = getRawData(RecvFormat.e);
        if ((errorDesc != null) && (errorDesc.length() > 0)) {
            return JobState.FAILED;
        }
        Boolean inProgress = (Boolean)getData(RecvFormat.z);
        if ((inProgress != null) && inProgress.booleanValue()) { // If in progress...
            return JobState.RUNNING;
        } else {
            return JobState.DONE;
        }
    }
    
    @Override
    protected void deleteImpl(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        hyfc.dele(getServerFileName());
    }

    protected String getServerFileName() {
        return "recvq/" + getData(RecvFormat.f);
    }
    
    @Override
    public Object getIDValue() {
        return getData(RecvFormat.f);
    }
    
    @Override
    protected List<FaxDocument> calcDocuments() {
        return Collections.<FaxDocument>singletonList(
                new HylaServerDoc<RecvFormat>(this, getServerFileName(), FileFormat.TIFF));
    }    
}
