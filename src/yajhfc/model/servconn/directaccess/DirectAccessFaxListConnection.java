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
package yajhfc.model.servconn.directaccess;

import java.awt.Window;
import java.io.File;

import yajhfc.model.FmtItemList;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.FileHylaDirAccessor;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.directaccess.jobq.JobQueueFaxJobList;
import yajhfc.model.servconn.directaccess.jobq.JobToQueueMapping;
import yajhfc.model.servconn.directaccess.jobq.PseudoSentFaxJobList;
import yajhfc.model.servconn.directaccess.recvq.RecvQFaxJobList;
import yajhfc.model.servconn.hylafax.HylaFaxListConnection;
import yajhfc.model.servconn.hylafax.ManagedFaxJobList;
import yajhfc.server.ServerOptions;

/**
 * @author jonas
 *
 */
public class DirectAccessFaxListConnection extends HylaFaxListConnection {
    protected HylaDirAccessor hyda;
    
    public DirectAccessFaxListConnection(ServerOptions fo, Window parentWindow) {
        super(fo, parentWindow);
        refreshDirAccessor();
    }

    @Override
    protected ManagedFaxJobList<RecvFormat> createRecvdList() {
        return new RecvQFaxJobList(this, fo.getParent().recvfmt, fo, "recvq");
    }

    @Override
    protected ManagedFaxJobList<JobFormat> createSentList() {
        FmtItemList<QueueFileFormat> wrappedList = new FmtItemList<QueueFileFormat>(QueueFileFormat.values(), new QueueFileFormat[0]);
        JobToQueueMapping.getRequiredFormats(fo.getParent().sentfmt, wrappedList);
        return new PseudoSentFaxJobList(fo.getParent().sentfmt,
                new JobQueueFaxJobList(this, wrappedList, fo, "doneq"), this);
    }

    protected void refreshDirAccessor() {
        if (hyda == null || !fo.directAccessSpoolPath.equals(hyda.getBasePath())) {
            hyda = new FileHylaDirAccessor(new File(fo.directAccessSpoolPath), fo);
        }
    }
    
    @Override
    public void setOptions(ServerOptions so) {
        refreshDirAccessor();
        super.setOptions(so);
    }
    
    /**
     * @return the hyda
     */
    public HylaDirAccessor getDirAccessor() {
        return hyda;
    }
}
