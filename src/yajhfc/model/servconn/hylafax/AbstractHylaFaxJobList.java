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
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.defimpl.AbstractFaxJobList;
import yajhfc.server.ServerOptions;

public abstract class AbstractHylaFaxJobList<T extends FmtItem> extends AbstractFaxJobList<T> implements ManagedFaxJobList<T>  {
    protected static final char SPLIT_CHAR = '|';
    static final Logger log = Logger.getLogger(AbstractHylaFaxJobList.class.getName());
    protected final HylaFaxListConnection parent;
    
    protected Vector<?> lastJobListing = null;

    public void disconnectCleanup() {
        setJobs(Collections.<FaxJob<T>>emptyList());
        lastJobListing = null;
    }

    public void pollForNewJobs(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        if (Utils.debugMode)
            log.finer("Getting job listing with columns: " + columns.getCompleteView());
        Vector<?> newJobs = getJobListing(hyfc);
        if (lastJobListing == null || !newJobs.equals(lastJobListing)) {
            log.fine("Job listings differ, reloading jobs");
            
            List<FaxJob<T>> newFaxJobs = new ArrayList<FaxJob<T>>(newJobs.size());
            for (int i = 0; i < newJobs.size(); i++) {
                newFaxJobs.add(createFaxJob(Utils.fastSplit((String)newJobs.get(i), SPLIT_CHAR)));
            }
            lastJobListing = newJobs;
            
            setJobs(newFaxJobs);
        } else {
            log.fine("Job listings are the same.");
        }
    }
    
    public void reloadSettings(ServerOptions fo) {
        // NOP
    }
    
    protected AbstractHylaFaxJobList(HylaFaxListConnection parent, FmtItemList<T> columns) {
        super(columns, parent);
        this.parent = parent;
    }

    public abstract TableType getJobType();
    
    protected abstract FaxJob<T> createFaxJob(String[] data);
    
    protected abstract Vector<?> getJobListing(HylaFAXClient hyfc) throws IOException, ServerResponseException;
}
