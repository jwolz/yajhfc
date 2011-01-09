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
package yajhfc.model.servconn;

import java.util.List;

import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;

public interface FaxJobList<T extends FmtItem> {
    
    /**
     * Returns the columns the jobs contained in this job list contain
     * @return
     */
    public FmtItemList<T> getColumns();
    
    /**
     * Returns the jobs contained in this list
     * @return
     */
    public List<FaxJob<T>> getJobs();
    
    /**
     * Adds a fax job list listener to this fax job list
     * @param l
     */
    public void addFaxJobListListener(FaxJobListListener<T> l);
    
    /**
     * Removed the specified fax job list listener from this fax job list
     * @param l
     */
    public void removeFaxJobListListener(FaxJobListListener<T> l);
    
    /**
     * Returns the type of fax job list
     * @return
     */
    public TableType getJobType();

    /**
     * Returns if the isError() method of this list's jobs returns a sensible value
     * @return
     */
    public boolean isShowingErrorsSupported();
    
    /**
     * Returns the FaxListConnection this fax job list belongs to
     * @return
     */
    public FaxListConnection getParent();
}
