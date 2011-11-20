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
package yajhfc.model.servconn.hylafax;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.defimpl.AbstractFaxJob;

public abstract class AbstractHylaFaxJob<T extends FmtItem> extends AbstractFaxJob<T> implements Serializable {
    static final Logger log = Logger.getLogger(AbstractHylaFaxJob.class.getName());
    private static final long serialVersionUID = 1;
    
    protected HylaFaxListConnection getConnection() {
        return ((AbstractHylaFaxJobList<T>)parent).parent;
    }
    
    public void delete() throws IOException, ServerResponseException {
        HylaFAXClient hyfc = getConnection().beginServerTransaction();
        try {
            if (Utils.debugMode)
                log.fine("Deleting job " + getIDValue());
            deleteImpl(hyfc);
        } finally {
            getConnection().endServerTransaction();
        }
    }

    /**
     * Actual implementation of delete using a HylaFAXClient
     * @param hyfc
     * @throws IOException
     * @throws ServerResponseException
     */
    protected abstract void deleteImpl(HylaFAXClient hyfc) throws IOException, ServerResponseException;
    
    public void resume() throws IOException, ServerResponseException {
        HylaFAXClient hyfc = getConnection().beginServerTransaction();
        try {
            if (Utils.debugMode)
                log.fine("Resuming job " + getIDValue());
            resumeImpl(hyfc);
        } finally {
            getConnection().endServerTransaction();
        }
    }
    
    /**
     * Actual implementation of resume using a HylaFAXClient
     * @param hyfc
     * @throws IOException
     * @throws ServerResponseException
     */
    protected void resumeImpl(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        throw new UnsupportedOperationException("Resume not supported");
    }
    
    public void suspend() throws IOException, ServerResponseException {
        HylaFAXClient hyfc = getConnection().beginServerTransaction();
        try {
            if (Utils.debugMode)
                log.fine("Suspending job " + getIDValue());
            suspendImpl(hyfc);
        } finally {
            getConnection().endServerTransaction();
        }
    }
    
    /**
     * Actual implementation of suspend using a HylaFAXClient
     * @param hyfc
     * @throws IOException
     * @throws ServerResponseException
     */
    protected void suspendImpl(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        throw new UnsupportedOperationException("Suspend not supported");
    }
    
    
    protected AbstractHylaFaxJob(AbstractHylaFaxJobList<T> parent, String[] data) {
        super(parent, data);
    }
}