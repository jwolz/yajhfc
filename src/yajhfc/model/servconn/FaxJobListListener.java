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
package yajhfc.model.servconn;

import java.util.List;

import yajhfc.model.FmtItem;

public interface FaxJobListListener<T extends FmtItem> {
 
    /**
     * Called when some or all fax jobs have changed.
     * @param source the list which changed
     */
    public void faxJobsUpdated(FaxJobList<T> source, List<FaxJob<T>> oldJobList, List<FaxJob<T>> newJobList);
    
    /**
     * Called when the read/unread state of a job changes
     * @param source
     * @param job
     * @param oldState
     * @param newState
     */
    public void readStateChanged(FaxJobList<T> source, FaxJob<T> job, boolean oldState, boolean newState);
}
