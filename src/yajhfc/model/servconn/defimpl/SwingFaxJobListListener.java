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
    public final boolean enableFaxJobsUpdated;
    public final boolean enableReadStateChanged;
    public final boolean enableColumnChanged;
    

    public void faxJobsUpdated(final FaxJobList<T> source,
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

    public void readStateChanged(final FaxJobList<T> source, final FaxJob<T> job,
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

    protected void readStateChangedSwing(FaxJobList<T> source, FaxJob<T> job,
            boolean oldState, boolean newState) {
        // Do nothing
    }
    
    public void columnChanged(final FaxJobList<T> source, final FaxJob<T> job, final T column,
            final int columnIndex, final Object oldValue, final Object newValue) {
        if (!enableColumnChanged)
            return;
        
        if (SwingUtilities.isEventDispatchThread()) {
            columnChangedSwing(source, job, column, columnIndex, oldValue, newValue);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    columnChangedSwing(source, job, column, columnIndex, oldValue, newValue);
                } 
            });
        }
    }

    protected void columnChangedSwing(FaxJobList<T> source, FaxJob<T> job, T column,
            int columnIndex, Object oldValue, Object newValue) {
        // Do nothing
    }

    public SwingFaxJobListListener(boolean enableFaxJobsUpdated,
            boolean enableReadStateChanged) {
        this(true, true, false);
    }
    
    public SwingFaxJobListListener(boolean enableFaxJobsUpdated,
            boolean enableReadStateChanged, boolean enableColumnChanged) {
        super();
        this.enableFaxJobsUpdated = enableFaxJobsUpdated;
        this.enableReadStateChanged = enableReadStateChanged;
        this.enableColumnChanged = enableColumnChanged;
    }
    
    public SwingFaxJobListListener() {
        this(true, true, true);
    }
}
