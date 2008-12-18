package yajhfc.model;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.FmtItemList;
import yajhfc.HylaServerFile;
import yajhfc.JobFormat;
import yajhfc.Utils;
import yajhfc.file.FormattedFile.FileFormat;

public class SentYajJob extends YajJob<JobFormat> {
    private static final Logger log = Logger.getLogger(SentYajJob.class.getName());
    
    protected int statusCol;
    protected int jobIDCol;
    
    public static final char JOBSTATE_UNDEFINED = '?';
    public static final char JOBSTATE_FAILED = 'F';
    public static final char JOBSTATE_SUSPENDED = 'T';
    public static final char JOBSTATE_PENDING = 'P';
    public static final char JOBSTATE_SLEEPING = 'S';
    public static final char JOBSTATE_BLOCKED = 'B';
    public static final char JOBSTATE_WAITING = 'W';
    public static final char JOBSTATE_RUNNING = 'R';
    public static final char JOBSTATE_DONE = 'D';
    
    
    @Override
    public boolean isError() {
        // Also update MainWin.MenuViewListener if this is changed!
        char status = getJobState();
        return (status == JOBSTATE_FAILED || status == JOBSTATE_UNDEFINED);
    }
    
    public Job getJob(HylaFAXClient hyfc) throws ServerResponseException, IOException {
        return hyfc.getJob((Integer)getData(jobIDCol));
    }
    
    @Override
    public List<HylaServerFile> getServerFilenames(HylaFAXClient hyfc, List<String> inaccessibleFiles) throws IOException, ServerResponseException {
        String[] files;
        ArrayList<HylaServerFile> availFiles = new ArrayList<HylaServerFile>();
        synchronized (hyfc) {
            files = getJob(hyfc).getDocumentName().split("\n");
        }
        
        // The last entry is "End of Documents"!
        for (int i = 0; i < files.length - 1; i++) {
            String[] fields = files[i].split("\\s");
            String fileName = fields[1];
            String fileType = fields[0];
            FileFormat fileFormat;
            
            if (Utils.debugMode) {
                log.info("Trying to access file " + fileName + "; type: " + fileType);
            }
            try {
                hyfc.stat(fileName); // will throw FileNotFoundException if file doesn't exist
                // Bugfix for certain HylaFAX versions that always return "PCL"
                // as file type for all documents
                if (Utils.getFaxOptions().pclBug && fileType.equalsIgnoreCase("pcl")) {
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
                
//                if (Utils.getFaxOptions().preferRenderedTIFF) {
//                    // Use a hack to get the rendered TIFF instead of the
//                    // original file (if available)
//                    String tiff = findRenderedTIFF(hyfc, fileName);
//                    if (tiff != null) {
//                        fileName = tiff;
//                        fileFormat = FileFormat.TIFF;
//                    }
//                }
                
                availFiles.add(new HylaServerFile(fileName, fileFormat));
            } catch (FileNotFoundException e) {
                // do nothing
                //System.err.println(e.toString());
                if (Utils.debugMode) {
                    log.log(Level.FINER, "Could not access " + files[i], e);
                }
                if (inaccessibleFiles != null) {
                    inaccessibleFiles.add(fileName + ": \"" + e.getMessage() + '\"');
                }
            }
        }
        
        return availFiles;
    }
    
//    /**
//     * Tries to find a rendered TIFF on the server for the file "serverName".
//     */
//    @SuppressWarnings("unchecked")
//    private String findRenderedTIFF(HylaFAXClient hyfc, String serverName) {
//        if (Utils.debugMode)
//            log.info("Trying to find the rendered TIFF for " + serverName);
//            
//        int pos = serverName.indexOf('/');
//        if (pos < 0)
//            return null;
//        
//        try {
//            String dir = serverName.substring(0, pos+1);
//            Vector fileList = hyfc.getNameList(dir);
//            
//            // Simple assumption:
//            // The rendered TIFF can be found by appending ";" plus a number to the document's filename
//            String searchPrefix = serverName.substring(pos+1) + ";";
//            for (Object fobj : fileList) {
//                String file = (String)fobj;
//                if (file.startsWith(searchPrefix)) {
//                    file = dir + file;
//                    if (Utils.debugMode)
//                        log.info("Found a TIFF: " + file);
//                    
//                    hyfc.stat(file); //Throws an exception if the TIFF is not accessible...
//                    
//                    return file;
//                }
//            }
//            return null; //Nothing found;
//        } catch (Exception ex) {
//            if (Utils.debugMode) {
//                log.log(Level.INFO, "Got an exception:", ex);
//                //ex.printStackTrace(Utils.debugOut);
//            }
//            return null;
//        }
//    }
    
    @Override
    public void delete(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        synchronized (hyfc) {
            hyfc.delete(getJob(hyfc));
        }
    }
    
    @Override
    public Object getIDValue() {
        return getData(jobIDCol);
    }

    public char getJobState() {
        String state = getStringData(statusCol);
        if (state != null && state.length() > 0) {
            return state.charAt(0);
        } else {
            return '?';
        }
    }
    
    @Override
    public void setColumns(FmtItemList<JobFormat> columns) {
        jobIDCol = columns.getCompleteView().indexOf(JobFormat.j);
        statusCol = columns.getCompleteView().indexOf(JobFormat.a);
        
        super.setColumns(columns);
    }
    
    public SentYajJob(FmtItemList<JobFormat> cols, String[] stringData) {
        super(cols, stringData);
    }
    
    /**
     * Returns a descriptive text for the given job state or null
     * if the state is unknown.
     * @param state
     * @return
     */
    public static String getDescriptionForJobState(char state) {
        switch (state) {
        case JOBSTATE_BLOCKED:
            return Utils._("Blocked (by concurrent activity to the same destination)");
        case JOBSTATE_DONE:
            return Utils._("Done");
        case JOBSTATE_FAILED:
            return Utils._("Failed");
        case JOBSTATE_PENDING:
            return Utils._("Pending (waiting for the time to send to arrive)");
        case JOBSTATE_RUNNING:
            return Utils._("Running");
        case JOBSTATE_SLEEPING:
            return Utils._("Sleeping (waiting for a scheduled timeout such as a delay between attempts to send)");
        case JOBSTATE_SUSPENDED:
            return Utils._("Suspended (not being scheduled)");
        case JOBSTATE_UNDEFINED:
            return Utils._("Undefined");
        case JOBSTATE_WAITING:
            return Utils._("Waiting (for resources such as a free modem)");
        default:
            return null;
        }
    }
    
    /**
     * Returns a label for the given job state or state
     * if the state is unknown.
     * @param state
     * @return
     */
    public static String getLabelForJobState(char state) {
        switch (state) {
        case JOBSTATE_BLOCKED:
            return Utils._("Blocked");
        case JOBSTATE_DONE:
            return Utils._("Done");
        case JOBSTATE_FAILED:
            return Utils._("Failed");
        case JOBSTATE_PENDING:
            return Utils._("Pending");
        case JOBSTATE_RUNNING:
            return Utils._("Running");
        case JOBSTATE_SLEEPING:
            return Utils._("Sleeping");
        case JOBSTATE_SUSPENDED:
            return Utils._("Suspended");
        case JOBSTATE_UNDEFINED:
            return Utils._("Undefined");
        case JOBSTATE_WAITING:
            return Utils._("Waiting");
        default:
            return String.valueOf(state);
        }
    }
}
