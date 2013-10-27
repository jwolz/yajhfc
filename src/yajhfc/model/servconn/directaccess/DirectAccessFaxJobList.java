package yajhfc.model.servconn.directaccess;
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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.defimpl.AbstractFaxJobList;
import yajhfc.model.servconn.defimpl.JobIDComparator;
import yajhfc.model.servconn.hylafax.ManagedFaxJobList;
import yajhfc.server.ServerOptions;

public abstract class DirectAccessFaxJobList<T extends FmtItem> extends AbstractFaxJobList<T> implements ManagedFaxJobList<T> {

    static final Logger log = Logger.getLogger(DirectAccessFaxJobList.class.getName());
    protected long lastDirectoryModification = -1;
    protected final String directory;

    public DirectAccessFaxJobList(FaxListConnection parent,
            FmtItemList<T> columns, ServerOptions fo, String directory) {
        super(columns, parent);
        this.directory = directory;
        reloadSettings(fo);
    }    

    public boolean isShowingErrorsSupported() {
        return true;
    }

    public void disconnectCleanup() {
        setJobs(Collections.<FaxJob<T>>emptyList());
        lastDirectoryModification = -1;
    }


    public void pollForNewJobs(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        pollForNewJobs();
    }
    
    public void pollForNewJobs() throws IOException {
        long modificationTime = getDirAccessor().getLastModified();
        if (Utils.debugMode)
            log.fine(directory + ": poll for changes: lastDirectoryModification="+lastDirectoryModification +"; modificationTime=" + modificationTime);
        if (lastDirectoryModification == -1 || lastDirectoryModification != modificationTime) {
            setJobs(updateQueueFiles());
            lastDirectoryModification = modificationTime;
        } else {
            log.fine("Directory unchanged; polling individual jobs for changes");
            boolean res = false;
            for (FaxJob<T> job : jobs) {
                res |= ((DirectAccessFaxJob<T>)job).pollForChanges();
            }
            if (res) {
                fireFaxJobsUpdated(jobs, jobs);
            }
        }
    }

    /**
     * Creates the job for the given job ID
     * @param jobID
     * @return
     * @throws IOException 
     */
    protected abstract DirectAccessFaxJob<T> createJob(String jobID) throws IOException;

    /**
     * Translate the directory entries into job ID numbers (e.g. queue nrs)
     * (e.g. [q12, q13, seqf] -> [12, 13])
     * @param listing
     * @return
     */
    protected abstract String[] translateDirectoryEntries(String[] listing);

    @SuppressWarnings("unchecked")
    public List<FaxJob<T>> updateQueueFiles() throws IOException {
        String[] listing;
        if (directory == null) {
            listing = getDirAccessor().listDirectory();
        } else {
            listing = getDirAccessor().listDirectory(directory);
        }
        if (Utils.debugMode) {
            log.finer(directory + " entries: " + Arrays.toString(listing));
        }
        final String[] jobIDs = translateDirectoryEntries(listing);
        if (jobIDs == null || jobIDs.length == 0) {
            log.fine("Directory is empty");
            return Collections.emptyList();
        }
        
        if (Utils.debugMode) {
            log.finer(directory + " new jobIDs=" + Arrays.toString(jobIDs) + "; old jobs=" + jobs);
            log.finer(directory + " columns are: " + columns.getCompleteView());
        }
        
        List<FaxJob<T>> resultList = new ArrayList<FaxJob<T>>(jobIDs.length);
        if (jobs.size() == 0) {
            // No old jobs, use "easy" algorithm
            log.fine("No old jobs");
            for (String nr : jobIDs) {
                try {
                    resultList.add(createJob(nr));
                } catch (FileNotFoundException e) {
                    log.log(Level.INFO, "Could not add " + directory + " file:", e);
                }
            }
        } else {
            log.fine("Comparing old job list with new");
            DirectAccessFaxJob<T>[] oldJobs = new DirectAccessFaxJob[jobs.size()];
            oldJobs = jobs.toArray(oldJobs);
            
            Arrays.sort(jobIDs);
            Arrays.sort(oldJobs, JobIDComparator.INSTANCE);
            
            // Compare the two lists to get new, deleted and unchanged jobs
            int i=0, j=0;
            for (; i < jobIDs.length && j < oldJobs.length;) {
                try {
                    String newID = jobIDs[i];
                    DirectAccessFaxJob<T> oldJob = oldJobs[j];
                    String oldID = oldJob.jobID;
                    int compVal = newID.compareTo(oldID);
    
                    if (compVal == 0) {
                        // Job is present in both the new and the old list
                        i++;
                        j++;
    
                        oldJob.pollForChanges();
                        resultList.add(oldJob);
                    } else if (compVal < 0) {
                        // new job is smaller than old job
                        // => new job is really "new"
                        i++;
                        
                        resultList.add(createJob(newID));
                    } else /* if (compVal > 0) */ {
                        // new job is greater than old job
                        // => old job has been deleted
                        j++;
                    }
                } catch (FileNotFoundException e) {
                    log.log(Level.INFO, "Could not add " + directory + " file:", e);
                }
            }
            
            // Add remaining new jobs
            for ( ; i < jobIDs.length; i++) {
                try {
                    resultList.add(createJob(jobIDs[i]));
                } catch (FileNotFoundException e) {
                    log.log(Level.INFO, "Could not add " + directory + " file:", e);
                }
            }
        }
    
        return resultList;
    }

    /**
     * @return the hyda
     */
    public HylaDirAccessor getDirAccessor() {
        return ((DirectAccessFaxListConnection)parent).getDirAccessor();
    }
}