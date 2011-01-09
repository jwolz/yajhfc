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
package yajhfc.model.servconn.directaccess.jobq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJobList;
import yajhfc.server.ServerOptions;

public class JobQueueFaxJobList extends DirectAccessFaxJobList<QueueFileFormat>  {
    
    public JobQueueFaxJobList(FaxListConnection parent,
            FmtItemList<QueueFileFormat> columns, ServerOptions fo,
            String directory) {
        super(parent, columns, fo, directory);
    }

    protected Map<String,int[]> desiredItems;
    
    public void reloadSettings(ServerOptions fo) {        
        List<QueueFileFormat> completeCols = columns.getCompleteView();
        if (desiredItems == null)
            desiredItems = new HashMap<String,int[]>();
        else
            desiredItems.clear();
        // Create a mapping of which properties are to be put in which output column
        //  (e.g. "external" -> [3,7] : value of external is put in the 4th and 8th column)
        for (int i = 0; i < completeCols.size(); i++) {
            String hylaFmt = completeCols.get(i).getHylaFmt();
            
            int[] oldVal = desiredItems.get(hylaFmt);
            int[] val;
            if (oldVal == null || oldVal.length == 0) {
                val = new int[] { i };
            } else {
                val = new int[oldVal.length+1];
                System.arraycopy(oldVal, 0, val, 0, oldVal.length);
                val[oldVal.length] = i;
            }
            
            desiredItems.put(hylaFmt, val);
        }
    }
    
    public TableType getJobType() {
        return TableType.SENT;
    }
    
    protected DirectAccessFaxJob<QueueFileFormat> createJob(String queueNr) throws IOException {
        String fileName = directory + "/q" + queueNr;
        return new JobQueueFaxJob(this, queueNr, fileName);
    }
    
    protected static final Pattern queueFilePattern = Pattern.compile("q(\\d+)");
    protected String[] translateDirectoryEntries(String[] listing) {
        if (listing == null || listing.length == 0)
            return null;
        
        ArrayList<String> res = new ArrayList<String>(listing.length);
        for (String s : listing) {
            Matcher m = queueFilePattern.matcher(s);
            if (m.matches()) {
                res.add(m.group(1));
            }
        }
        return res.toArray(new String[res.size()]);
    }
    
}
