package yajhfc.model.servconn.directaccess;

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.defimpl.AbstractFaxJob;

public abstract class DirectAccessFaxJob<T extends FmtItem> extends AbstractFaxJob<T> {
    static final Logger log = Logger.getLogger(DirectAccessFaxJob.class.getName());
    
    protected final String jobID;
    protected final String fileName;
    protected long lastModified = -1;

    protected DirectAccessFaxJob(DirectAccessFaxJobList<T> parent, String queueNr, String fileName) throws IOException {
        super(parent);
        this.jobID = queueNr;
        this.fileName = fileName;
        this.documents = new ArrayList<FaxDocument>();
        readSpoolFile(getDirAccessor());
    }

    public HylaDirAccessor getDirAccessor() {
        return ((DirectAccessFaxJobList<T>)parent).getDirAccessor();
    }

    public void resume() throws IOException, ServerResponseException {
        throw new UnsupportedOperationException();
    }

    public void suspend() throws IOException, ServerResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getIDValue() {
        return jobID;
    }
    
    
    /**
     * Polls for changes. Returns true if the data changed
     * @return
     * @throws IOException
     */
    public boolean pollForChanges() throws IOException {
        HylaDirAccessor hyda = getDirAccessor();
        long newModified = hyda.getLastModified(fileName);
        if (Utils.debugMode)
            log.fine(fileName + ": poll for changes: lastModified="+lastModified +"; newModified=" + newModified);
        if (newModified != lastModified) {
            readSpoolFile(hyda);
            return true;
        } else {
            return false;
        }
    }
    
    protected abstract void readSpoolFile(HylaDirAccessor hyda) throws IOException;
    

    @Override
    protected List<FaxDocument> calcDocuments() {
        // this.documents is set in the constructor, so this method
        // actually gets never called
        return null;
    }

}