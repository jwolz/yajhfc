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
package yajhfc.model.servconn.directaccess.recvq;

import java.io.IOException;
import java.util.ArrayList;

import yajhfc.model.FmtItemList;
import yajhfc.model.RecvFormat;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJobList;
import yajhfc.server.ServerOptions;

public class RecvQFaxJobList extends DirectAccessFaxJobList<RecvFormat> {
    
    public RecvQFaxJobList(FaxListConnection parent,
            FmtItemList<RecvFormat> columns, ServerOptions fo, String directory) {
        super(parent, columns, fo, directory);
    }

    @Override
    protected DirectAccessFaxJob<RecvFormat> createJob(String jobID)
            throws IOException {
        String fileName = directory + "/" + jobID;
        return new RecvQFaxJob(this, jobID, fileName);
    }
    public void reloadSettings(ServerOptions fo) {
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
