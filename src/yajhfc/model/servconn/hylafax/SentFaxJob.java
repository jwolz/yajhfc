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
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc.model.servconn.hylafax;

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.model.JobFormat;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.HylafaxWorker;
import yajhfc.model.servconn.JobState;

public class SentFaxJob extends AbstractHylaFaxJob<JobFormat> {
    private static final long serialVersionUID = 1;
    static final Logger log = Logger.getLogger(SentFaxJob.class.getName());
    
    protected SentFaxJob(AbstractHylaFaxJobList<JobFormat> parent, String[] data) {
        super(parent, data);
    }

    protected JobState calculateJobState() {
        String s = getRawData(JobFormat.a);

        if (s == null || s.length() < 1) {
            return JobState.UNDEFINED;
        } else {
            return JobState.getJobStateFromCharCode(Character.toUpperCase(s.charAt(0)));
        }
    }
    
    @Override
    public Map<String, String> getJobProperties(String... properties) {
        if (Utils.debugMode) {
            log.finer("Retrieving properties for job " + getIDValue() + ": " + Arrays.toString(properties));
        }
        Map<String,String> result = new HashMap<String,String>();
        try {
            HylaFAXClient hyfc = getConnection().beginServerTransaction();
            try {
                synchronized (hyfc) {
                    Job hyJob = getJob(hyfc);
                    for (String key : properties) {
                        try {
                            result.put(key, hyJob.getProperty(key));
                        } catch (Exception e) {
                            log.log(Level.INFO, "Error retrieving property " + key, e);
                        }
                    }
                }
            } finally {
                getConnection().endServerTransaction();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error retrieving the job properties", e);
        } 
        if (Utils.debugMode) {
            log.finer("Retrieved properties for job " + getIDValue() + ": " + result);
        }
        return result;
    }


    
    
    protected static final Pattern lineSplitter = Pattern.compile("\\s");
    @Override
    protected List<FaxDocument> calcDocuments() {
        final List<FaxDocument> availFiles = new ArrayList<FaxDocument>();
        inaccessibleDocuments = new ArrayList<String>();

        try {
            HylaFAXClient hyfc = getConnection().beginServerTransaction();
            try {
                String[] files;
                synchronized (hyfc) {
                    files = Utils.fastSplit(getJob(hyfc).getDocumentName(), '\n');
                }

                // The last entry is "End of Documents"!
                for (int i = 0; i < files.length - 1; i++) {
                    String[] fields = lineSplitter.split(files[i]);
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

                        availFiles.add(new HylaServerDoc<JobFormat>(SentFaxJob.this, fileName, fileFormat));
                    } catch (FileNotFoundException e) {
                        if (Utils.debugMode) {
                            log.log(Level.FINER, "Could not access " + files[i], e);
                        }
                        if (inaccessibleDocuments != null) {
                            inaccessibleDocuments.add(fileName + ": \"" + e.getMessage() + '\"');
                        }
                    }
                }
            } finally {
                getConnection().endServerTransaction();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error retrieving the associated documents", e);
        } 
        return availFiles;
}

    @Override
    public FaxDocument getCommunicationsLog() throws IOException {
        String commID = (String)getData(JobFormat.W);
        if (Utils.debugMode) {
            log.finer("CommID for job " + getIDValue() + " is: " + commID);
        }
        if (commID == null || commID.length() == 0) {
            return null;
        } else {
            return new HylaServerDoc<JobFormat>(this, "log/c" + commID, FileFormat.PlainText);
        }
    }
    
    @Override
    protected void deleteImpl(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        synchronized (hyfc) {
            hyfc.delete(getJob(hyfc));
        }
    }

    public Job getJob(HylaFAXClient hyfc) throws ServerResponseException, IOException {
        Object jobid = getData(JobFormat.j);
        if (jobid==null)
            throw new IOException("Cannot get job ID for fax job");
        return hyfc.getJob((Integer)jobid);
    }
    
    @Override
    public Object doHylafaxWork(HylafaxWorker worker)
            throws IOException, ServerResponseException {
        HylaFAXClient hyfc = getConnection().beginServerTransaction();
        try {
            if (Utils.debugMode)
                log.fine("doHylafaxWork on " + getIDValue() + ": worker=" + worker);
            
            return worker.work(hyfc, getJob(hyfc));
        } finally {
            getConnection().endServerTransaction();
        }
    }
    
    @Override
    public Object getIDValue() {
        return getData(JobFormat.j);
    }

}
