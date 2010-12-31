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
package yajhfc.model.servconn.directaccess.recvq;

import java.io.IOException;
import java.util.ArrayList;

import yajhfc.FaxOptions;
import yajhfc.model.FmtItemList;
import yajhfc.model.RecvFormat;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJobList;

public class RecvQFaxJobList extends DirectAccessFaxJobList<RecvFormat> {
    
    public RecvQFaxJobList(FaxListConnection parent,
            FmtItemList<RecvFormat> columns, FaxOptions fo, String directory) {
        super(parent, columns, fo, directory);
    }

    @Override
    protected DirectAccessFaxJob<RecvFormat> createJob(String jobID)
            throws IOException {
        String fileName = directory + "/" + jobID;
        return new RecvQFaxJob(this, jobID, fileName);
    }
    public void reloadSettings(FaxOptions fo) {
        // NOP
    }

    @Override
    protected String[] translateDirectoryEntries(String[] listing) {
        if (listing == null || listing.length == 0) {
            return null;
        }
        ArrayList<String> result = new ArrayList<String>(listing.length);
        for (String file : listing) {
            if (file.startsWith("fax") && file.endsWith(".tif")) {
                result.add(file);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public TableType getJobType() {
        return TableType.RECEIVED;
    }

}
