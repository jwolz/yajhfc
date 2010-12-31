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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.defimpl.AbstractFaxJobList;

public abstract class AbstractHylaFaxJobList<T extends FmtItem> extends AbstractFaxJobList<T> implements ManagedFaxJobList<T>  {
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
                newFaxJobs.add(createFaxJob(Utils.fastSplit((String)newJobs.get(i), '|')));
            }
            lastJobListing = newJobs;
            
            setJobs(newFaxJobs);
        } else {
            log.fine("Job listings are the same.");
        }
    }
    
    public void reloadSettings(FaxOptions fo) {
        // NOP
    }
    
    protected AbstractHylaFaxJobList(HylaFaxListConnection parent, FmtItemList<T> columns) {
        super(columns);
        this.parent = parent;
    }

    public abstract TableType getJobType();
    
    protected abstract FaxJob<T> createFaxJob(String[] data);
    
    protected abstract Vector<?> getJobListing(HylaFAXClient hyfc) throws IOException, ServerResponseException;
}
