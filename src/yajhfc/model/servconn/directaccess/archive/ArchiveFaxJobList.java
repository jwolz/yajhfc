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
package yajhfc.model.servconn.directaccess.archive;

import java.io.File;
import java.io.IOException;

import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.jobq.FileHylaDirAccessor;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.model.servconn.directaccess.jobq.JobQueueFaxJobList;
import yajhfc.server.ServerOptions;

public class ArchiveFaxJobList extends JobQueueFaxJobList {
    protected HylaDirAccessor hyda;
    
    public TableType getJobType() {
        return TableType.ARCHIVE;
    }
    
    public void reloadSettings(ServerOptions fo) {
        if (hyda == null || !fo.archiveLocation.equals(hyda.getBasePath())) {
            hyda = new FileHylaDirAccessor(new File(fo.archiveLocation), fo);
        }
        super.reloadSettings(fo);
    }
    
    @Override
    public HylaDirAccessor getDirAccessor() {
        return hyda;
    }
    
    public ArchiveFaxJobList(FaxListConnection parent,
            FmtItemList<QueueFileFormat> columns, ServerOptions fo) {
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
