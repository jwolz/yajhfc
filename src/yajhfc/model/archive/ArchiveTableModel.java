/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.model.archive;

import java.util.List;

import yajhfc.model.MyTableModel;
import yajhfc.model.YajJob;

public class ArchiveTableModel extends MyTableModel<QueueFileFormat> {
    @Override
    public void setData(String[][] newData) {
        if (newData == null) {
            setData((List<ArchiveYajJob>)null);
        } else {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
    
    @Override
    protected YajJob<QueueFileFormat> createYajJob(String[] data) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @SuppressWarnings("unchecked")
    public void setData(List<ArchiveYajJob> jobList) {
        if (jobList == null) {
            this.jobs = null;
        } else {
            this.jobs = jobList.toArray(new YajJob[jobList.size()]);
        }
        refreshVisibleJobs();
    }
}