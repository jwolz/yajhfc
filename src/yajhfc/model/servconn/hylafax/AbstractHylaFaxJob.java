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