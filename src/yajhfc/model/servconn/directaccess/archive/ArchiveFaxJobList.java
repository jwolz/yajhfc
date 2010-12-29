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
package yajhfc.model.servconn.directaccess.archive;

import java.io.File;
import java.io.IOException;

import yajhfc.FaxOptions;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.jobq.FileHylaDirAccessor;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.model.servconn.directaccess.jobq.JobQueueFaxJobList;

public class ArchiveFaxJobList extends JobQueueFaxJobList {
    
    public TableType getJobType() {
        return TableType.ARCHIVE;
    }
    
    public void reloadSettings(FaxOptions fo) {        
        super.reloadSettings(fo);
        setDirAccessor(new FileHylaDirAccessor(new File(fo.archiveLocation)));
    }
    
    public ArchiveFaxJobList(FaxListConnection parent,
            FmtItemList<QueueFileFormat> columns, FaxOptions fo) {
        super(parent, columns, fo, ".");
    }

    @Override
    protected DirectAccessFaxJob<QueueFileFormat> createJob(String queueNr) throws IOException {
        String fileName = queueNr + "/q" + queueNr;
        return new ArchiveFaxJob(this, queueNr, fileName);
    }
    
    @Override
    protected String[] translateDirectoryEntries(String[] listing) {
        return listing;
    }
    
}
