package yajhfc.model.servconn.directaccess;
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

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.defimpl.AbstractFaxJob;

public abstract class DirectAccessFaxJob<T extends FmtItem> extends AbstractFaxJob<T> {
    static final Logger log = Logger.getLogger(DirectAccessFaxJob.class.getName());
    
    protected final String jobID;
    protected final String fileName;
    protected long lastModified = -1;

    protected DirectAccessFaxJob(DirectAccessFaxJobList<T> parent, String queueNr, String fileName) throws IOException {
        super(parent);
        this.jobID = queueNr;
        this.fileName = fileName;
        this.documents = new ArrayList<FaxDocument>();
        readSpoolFile(getDirAccessor());
    }

    public HylaDirAccessor getDirAccessor() {
        return ((DirectAccessFaxJobList<T>)parent).getDirAccessor();
    }

    public void resume() throws IOException, ServerResponseException {
        throw new UnsupportedOperationException();
    }

    public void suspend() throws IOException, ServerResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getIDValue() {
        return jobID;
    }
    
    
    /**
     * Polls for changes. Returns true if the data changed
     * @return
     * @throws IOException
     */
    public boolean pollForChanges() throws IOException {
        HylaDirAccessor hyda = getDirAccessor();
        long newModified = hyda.getLastModified(fileName);
        if (Utils.debugMode)
            log.fine(fileName + ": poll for changes: lastModified="+lastModified +"; newModified=" + newModified);
        if (newModified != lastModified) {
            readSpoolFile(hyda);
            lastModified = newModified;
            return true;
        } else {
            return false;
        }
    }
    
    protected abstract void readSpoolFile(HylaDirAccessor hyda) throws IOException;
    

    @Override
    protected List<FaxDocument> calcDocuments() {
        // this.documents is set in the constructor, so this method
        // actually gets never called
        return null;
    }

}