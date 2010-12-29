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
package yajhfc.model.servconn.defimpl;

import java.util.List;

import javax.swing.SwingUtilities;

import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.FaxJobListListener;

/**
 * FaxJobListListener that makes sure the event processing is done
 * in the event dispatching thread.
 * @author jonas
 *
 */
public abstract class SwingFaxJobListListener<T extends FmtItem> implements
FaxJobListListener<T> {
    public boolean enableFaxJobsUpdated;
    public boolean enableReadStateChanged;
    

    public final void faxJobsUpdated(final FaxJobList<T> source,
            final List<FaxJob<T>> oldJobList, final List<FaxJob<T>> newJobList) {
        if (!enableFaxJobsUpdated)
            return;
        
        if (SwingUtilities.isEventDispatchThread()) {
            faxJobsUpdatedSwing(source, oldJobList, newJobList);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    faxJobsUpdatedSwing(source, oldJobList, newJobList);
                } 
            });
        }
    }


    protected void faxJobsUpdatedSwing(FaxJobList<T> source,
            List<FaxJob<T>> oldJobList, List<FaxJob<T>> newJobList) {
        // Do nothing       
    }

    public final void readStateChanged(final FaxJobList<T> source, final FaxJob<T> job,
            final boolean oldState, final boolean newState) {
        if (!enableReadStateChanged)
            return;
        
        if (SwingUtilities.isEventDispatchThread()) {
            readStateChangedSwing(source, job, oldState, newState);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    readStateChangedSwing(source, job, oldState, newState);
                } 
            });
        }
    }

    public void readStateChangedSwing(FaxJobList<T> source, FaxJob<T> job,
            boolean oldState, boolean newState) {
        // Do nothing
    }


    public SwingFaxJobListListener(boolean enableFaxJobsUpdated,
            boolean enableReadStateChanged) {
        super();
        this.enableFaxJobsUpdated = enableFaxJobsUpdated;
        this.enableReadStateChanged = enableReadStateChanged;
    }
    
    public SwingFaxJobListListener() {
        this(true, true);
    }
}
