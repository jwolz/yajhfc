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

import yajhfc.FaxOptions;
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
import yajhfc.util.ProgressWorker.ProgressUI;

/**
 * @author jonas
 *
 */
public class DirectAccessFaxListConnection extends HylaFaxListConnection {
    protected HylaDirAccessor hyda;
    
    public DirectAccessFaxListConnection(FaxOptions fo, Window parentWindow,
            ProgressUI progressUI) {
        super(fo, parentWindow, progressUI);
        refreshDirAccessor();
    }

    @Override
    protected ManagedFaxJobList<RecvFormat> createRecvdList() {
        return new RecvQFaxJobList(this, fo.recvfmt, fo, "recvq");
    }

    @Override
    protected ManagedFaxJobList<JobFormat> createSentList() {
        FmtItemList<QueueFileFormat> wrappedList = new FmtItemList<QueueFileFormat>(QueueFileFormat.values(), new QueueFileFormat[0]);
        JobToQueueMapping.getRequiredFormats(fo.sentfmt, wrappedList);
        return new PseudoSentFaxJobList(fo.sentfmt,
                new JobQueueFaxJobList(this, wrappedList, fo, "doneq"));
    }

    protected void refreshDirAccessor() {
        if (hyda == null || !fo.directAccessSpoolPath.equals(hyda.getBasePath())) {
            hyda = new FileHylaDirAccessor(new File(fo.directAccessSpoolPath));
        }
    }
    
    @Override
    public void reloadSettings() {
        refreshDirAccessor();
        super.reloadSettings();
    }
    
    /**
     * @return the hyda
     */
    public HylaDirAccessor getDirAccessor() {
        return hyda;
    }
}
