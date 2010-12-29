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
package yajhfc.model.servconn.directaccess.archive;

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;

import yajhfc.model.servconn.directaccess.jobq.JobQueueFaxJob;

public class ArchiveFaxJob extends JobQueueFaxJob {
    private static final long serialVersionUID = 1;
  
    protected ArchiveFaxJob(ArchiveFaxJobList parent, String queueNr,
            String fileName) throws IOException {
        super(parent, queueNr, fileName);
    }

    public void delete() throws IOException, ServerResponseException {
        getDirAccessor().deleteTree(jobID);
    }

    @Override
    protected String getLogFileName(String commID) {
        return  jobID + "/c" + commID;
    }
    
    @Override
    protected String translateFileName(String fileName) {
        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash >= 0) {
            fileName = fileName.substring(lastSlash+1);
        } 
        
        return jobID + "/" + fileName;
    }
}
