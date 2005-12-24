package yajhfc;

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SentYajJob extends YajJob {
    private Job curJob = null;

    public Job getJob(HylaFAXClient hyfc) throws ServerResponseException, IOException {
        if (curJob == null) {
            curJob = hyfc.getJob((Integer)getData(columns.indexOf(utils.jobfmt_JobID)));
        }
        return curJob;
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
