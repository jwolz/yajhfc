package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SentYajJob extends YajJob {
    //private Job curJob = null;

    public Job getJob(HylaFAXClient hyfc) throws ServerResponseException, IOException {
        /*if (curJob == null) {
            curJob =*/ return hyfc.getJob((Integer)getData(columns.indexOf(utils.jobfmt_JobID)));
        //}
        //return curJob;
    }
    
    @Override
    public List<HylaServerFile> getServerFilenames(HylaFAXClient hyfc) throws IOException, ServerResponseException {
        String[] files = getJob(hyfc).getDocumentName().split("\n");
        ArrayList<HylaServerFile> availFiles = new ArrayList<HylaServerFile>();
        
        // The last entry is "End of Documents"!
        for (int i = 0; i < files.length - 1; i++) {
            String[] fields = files[i].split("\\s");
            try {
                hyfc.stat(fields[1]); // will throw FileNotFoundException if file doesn't exist
                // Bugfix for certain HylaFAX versions that always return "PCL"
                // as file type for all documents
                if (utils.getFaxOptions().pclBug && fields[0].equalsIgnoreCase("pcl")) {
                    if (fields[1].contains(".ps"))
                        fields[0] = "ps";
                    else if (fields[1].contains(".pdf"))
                        fields[0] = "pdf";
                    else if (fields[1].contains(".tif"))
                        fields[0] = "tif";
                }
                availFiles.add(new HylaServerFile(fields[1], fields[0]));
            } catch (FileNotFoundException e) {
                // do nothing
                //System.err.println(e.toString());
            }
        }
        
        return availFiles;
    }
    
    @Override
    public void delete(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        hyfc.delete(getJob(hyfc));
    }
    
    @Override
    public Object getIDValue() {
        return getData(columns.indexOf(utils.jobfmt_JobID));
    }

    public SentYajJob(Vector<FmtItem> cols, String[] stringData) {
        super(cols, stringData);
    }
}
