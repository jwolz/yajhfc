package yajhfc.model.servconn.directaccess;

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

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.defimpl.AbstractFaxJobList;
import yajhfc.model.servconn.defimpl.JobIDComparator;
import yajhfc.model.servconn.hylafax.ManagedFaxJobList;

public abstract class DirectAccessFaxJobList<T extends FmtItem> extends AbstractFaxJobList<T> implements ManagedFaxJobList<T> {

    static final Logger log = Logger.getLogger(DirectAccessFaxJobList.class.getName());
    protected final FaxListConnection parent;
    protected long lastDirectoryModification = -1;
    protected final String directory;

    public DirectAccessFaxJobList(FaxListConnection parent,
            FmtItemList<T> columns, FaxOptions fo, String directory) {
        super(columns);
        this.parent = parent;
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
        String[] listing = getDirAccessor().listDirectory(directory);
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