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

import yajhfc.FormattedFile.FileFormat;

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
            String fileName = fields[1];
            String fileType = fields[0];
            FileFormat fileFormat;
            
            if (utils.debugMode) {
                System.out.println("Trying to access file " + fileName + "; type: " + fileType);
            }
            try {
                hyfc.stat(fileName); // will throw FileNotFoundException if file doesn't exist
                // Bugfix for certain HylaFAX versions that always return "PCL"
                // as file type for all documents
                if (utils.getFaxOptions().pclBug && fileType.equalsIgnoreCase("pcl")) {
                        fileFormat = FileFormat.Unknown;
                } else {
                    if (fileType.equalsIgnoreCase("tif") || fileType.equalsIgnoreCase("tiff")) 
                        fileFormat = FileFormat.TIFF;
                    else if (fileType.equalsIgnoreCase("ps"))
                        fileFormat = FileFormat.PostScript;
                    else if(fileType.equalsIgnoreCase("pdf"))
                        fileFormat = FileFormat.PDF;
                    else
                        fileFormat = FileFormat.Unknown;
                }
                
                if (utils.getFaxOptions().preferRenderedTIFF) {
                    // Use a hack to get the rendered TIFF instead of the
                    // original file (if available)
                    String tiff = findRenderedTIFF(hyfc, fileName);
                    if (tiff != null) {
                        fileName = tiff;
                        fileFormat = FileFormat.TIFF;
                    }
                }
                
                availFiles.add(new HylaServerFile(fileName, fileFormat));
            } catch (FileNotFoundException e) {
                // do nothing
                //System.err.println(e.toString());
                if (utils.debugMode) {
                    e.printStackTrace(System.out);
                }
            }
        }
        
        return availFiles;
    }
    
    /**
     * Tries to find a rendered TIFF on the server for the file "serverName".
     */
    private String findRenderedTIFF(HylaFAXClient hyfc, String serverName) {
        if (utils.debugMode)
            System.out.println("Trying to find the rendered TIFF for " + serverName);
            
        int pos = serverName.indexOf('/');
        if (pos < 0)
            return null;
        
        try {
            String dir = serverName.substring(0, pos+1);
            Vector fileList = hyfc.getNameList(dir);
            
            // Simple assumption:
            // The rendered TIFF can be found by appending ";" plus a number to the document's filename
            String searchPrefix = serverName.substring(pos+1) + ";";
            for (Object fobj : fileList) {
                String file = (String)fobj;
                if (file.startsWith(searchPrefix)) {
                    file = dir + file;
                    if (utils.debugMode)
                        System.out.println("Found a TIFF: " + file);
                    
                    hyfc.stat(file); //Throws an exception if the TIFF is not accessible...
                    
                    return file;
                }
            }
            return null; //Nothing found;
        } catch (Exception ex) {
            if (utils.debugMode) {
                System.out.println("Got an exception:");
                ex.printStackTrace(System.out);
            }
            return null;
        }
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
