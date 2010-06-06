/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.model.archive;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.FmtItemList;
import yajhfc.HylaServerFile;
import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.model.SentYajJob;
import yajhfc.model.YajJob;

/**
 * @author jonas
 *
 */
public class ArchiveYajJob extends YajJob<QueueFileFormat> {
    private static final Logger log = Logger.getLogger(ArchiveYajJob.class.getName());
    
    protected String queueNr;
    protected List<HylaServerFile> files;
    protected HylaDirAccessor hyda;

    // From man doneq:
    // state: The job scheduling state.  Recognized values are:
    // 1 (suspended, not being scheduled),
    public static final int STATE_SUSPENDED = 1;
    // 2 (pending, waiting for the time to send), 
    public static final int STATE_PENDING = 2;
    // 3 (sleeping,  waiting for a scheduled timeout),
    public static final int STATE_SLEEPING = 3;
    // 4 (blocked, waiting for concurrent activity to the same destination to complete),
    public static final int STATE_BLOCKED = 4;
    // 5 (ready, ready to be processed except for available resources),
    public static final int STATE_READY = 5;
    // 6 (active, actively being processed by HylaFAX),
    public static final int STATE_ACTIVE = 6;
    // 7 (done,  processing completed with success).
    public static final int STATE_DONE = 7;
    // 8 (failed, processing completed with a failure).
    public static final int STATE_FAILED = 8; 

    public static final int STATE_UNKNOWN = -1;
    
    /**
     * @param cols
     * @param stringData
     */
    public ArchiveYajJob(HylaDirAccessor hyda, FmtItemList<QueueFileFormat> cols, String[] stringData, String queueNr, List<HylaServerFile> files) {
        super(cols, stringData);
        this.queueNr = queueNr;
        this.files = files;
        this.hyda = hyda;
    }

    /* (non-Javadoc)
     * @see yajhfc.YajJob#delete(gnu.hylafax.HylaFAXClient)
     */
    @Override
    public void delete(HylaFAXClient hyfc) throws IOException,
            ServerResponseException {
        hyda.deleteTree(queueNr);
    }

    /* (non-Javadoc)
     * @see yajhfc.YajJob#getIDValue()
     */
    @Override
    public Object getIDValue() {
        return queueNr;
    }
    
    @Override
    public List<HylaServerFile> getServerFilenames(HylaFAXClient hyfc,
            List<String> inaccessibleFiles) throws IOException,
            ServerResponseException {
        return files;
    }
    
    @Override
    protected Object parseValue(QueueFileFormat fmtItem, String data) {
        if (fmtItem.getDataType() == Boolean.class) {
            if (data == null || data.length() == 0)
                return Boolean.FALSE;
            
            try {
                return Boolean.valueOf(Integer.parseInt(data) != 0);
            } catch (NumberFormatException e) {
                log.log(Level.INFO, "Invalid number for Boolean col " + fmtItem + ": " + data, e);
                return Boolean.FALSE;
            }
        } else {
            return super.parseValue(fmtItem, data);
        }
    }
    
    public int getJobState() {
        String status = getStringData(QueueFileFormat.state);
        if (status == null || status.length() == 0) {
            return STATE_UNKNOWN;
        } else {
            int state = status.charAt(0) - '0';
            if (state < STATE_ACTIVE || state > STATE_FAILED) {
                return STATE_UNKNOWN;
            } else {
                return state;
            }
        }
    }
        
    @Override
    public boolean isError() {
        // Also update MainWin.MenuViewListener if this is changed!
        int status = getJobState();
        return (status == STATE_FAILED);
    }
    
    @Override
    public HylaServerFile getCommunicationsLog() {
        String commID = getStringData(QueueFileFormat.commid);
        if (commID == null || commID.length() == 0) {
            return null;
        } else {
            return new ArchiveHylaServerFile(hyda, queueNr + "/c" + commID, FileFormat.PlainText);
        }
    }
    
    public static char mapArchiveStatusToOneCharCode(int intState) {
        switch (intState) { // Map to sent job one character code
        case ArchiveYajJob.STATE_ACTIVE:
            return SentYajJob.JOBSTATE_RUNNING;
        case ArchiveYajJob.STATE_BLOCKED:
            return  SentYajJob.JOBSTATE_BLOCKED;
        case ArchiveYajJob.STATE_DONE:
            return  SentYajJob.JOBSTATE_DONE;
        case ArchiveYajJob.STATE_FAILED:
            return SentYajJob.JOBSTATE_FAILED;
        case ArchiveYajJob.STATE_PENDING:
            return SentYajJob.JOBSTATE_PENDING;
        case ArchiveYajJob.STATE_READY:
            return SentYajJob.JOBSTATE_WAITING;
        case ArchiveYajJob.STATE_SLEEPING:
            return SentYajJob.JOBSTATE_SLEEPING;
        case ArchiveYajJob.STATE_SUSPENDED:
            return SentYajJob.JOBSTATE_SUSPENDED;
        default:
            return SentYajJob.JOBSTATE_UNDEFINED;
        }
    }
    
    private static final Map<String,FileFormat> fileEntries = new HashMap<String,FileFormat>();
    static {
        fileEntries.put("postscript", FileFormat.PostScript);
        fileEntries.put("!postscript", FileFormat.PostScript);
        fileEntries.put("pcl", FileFormat.PCL);
        fileEntries.put("!pcl", FileFormat.PCL);
        fileEntries.put("tiff", FileFormat.TIFF);
        fileEntries.put("!tiff", FileFormat.TIFF);
        fileEntries.put("data", FileFormat.Unknown);
        fileEntries.put("!data", FileFormat.Unknown);
        fileEntries.put("pdf", FileFormat.PDF);
        fileEntries.put("!pdf", FileFormat.PDF);
    }
    private static ArchiveYajJob processSingleEntry(HylaDirAccessor hyda, FmtItemList<QueueFileFormat> cols, Map<String,int[]> desiredItems, String queueNr) throws FileNotFoundException, IOException, ServerResponseException {
        BufferedReader qFileReader = new BufferedReader(hyda.getInputReader(queueNr + "/q" + queueNr));
        
        String[] result = new String[cols.getCompleteView().size()];
        List<HylaServerFile> files = new ArrayList<HylaServerFile>();

        String line;
        StringBuilder lineBuffer = null;
        while ((line = qFileReader.readLine()) != null) {
            if (lineBuffer != null) {
                lineBuffer.append(line);
            }
            
            // Handling of "continuation" lines:
            if (line.endsWith("\\")) {
               if (lineBuffer == null) {
                   lineBuffer = new StringBuilder(line);
               }
               lineBuffer.deleteCharAt(lineBuffer.length() - 1);
               continue;
            }
            
            // The "real" line to process (i.e. without continuation backslashes):
            String realLine;
            if (lineBuffer == null) {
                realLine = line;
            } else {
                realLine = lineBuffer.toString();
                lineBuffer = null;
            }
            
            int colonOffset = realLine.indexOf(':');
            
            if (colonOffset >= 0) {
                String key = realLine.substring(0, colonOffset);

                int[] resultOffsets = desiredItems.get(key);
                if (resultOffsets != null) {
                    for (int offset : resultOffsets) {
                        result[offset] = realLine.substring(colonOffset + 1);
                    }
                } else if (fileEntries.containsKey(key)) {
                    int lastSlash = realLine.lastIndexOf('/');
                    String fileName;
                    if (lastSlash >= 0 && lastSlash > colonOffset) {
                        fileName = realLine.substring(lastSlash+1);
                    } else {
                        fileName = realLine.substring(colonOffset+1);
                    }
                    files.add(new ArchiveHylaServerFile(hyda, queueNr + "/" + fileName, fileEntries.get(key)));
                } else {
                    if (Utils.debugMode) {
                        log.finest("Ignoring entry for queue file " + queueNr + ": " + realLine);
                    }
                }
            } else {
                log.info("Malformed queue file entry for q" + queueNr + ": " + realLine);
            }
        }
        return new ArchiveYajJob(hyda, cols, result, queueNr, files);
    }

    /**
     * Returns a list of archive YajJobs for the given directory accessor and format
     * @param hyda
     * @param cols
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ServerResponseException
     */
    public static List<ArchiveYajJob> getArchiveFiles(HylaDirAccessor hyda, FmtItemList<QueueFileFormat> cols) throws FileNotFoundException, IOException, ServerResponseException {
        final String[] files = hyda.listDirectory();
        if (files == null || files.length == 0)
            return Collections.emptyList();
        
        List<ArchiveYajJob> resultList = new ArrayList<ArchiveYajJob>(files.length);
        List<QueueFileFormat> archiveCols = cols.getCompleteView();
        Map<String,int[]> desiredItems = new HashMap<String,int[]>(archiveCols.size());
        for (int i = 0; i < archiveCols.size(); i++) {
            String hylaFmt = archiveCols.get(i).getHylaFmt();
            
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

        for (String file : files) {
            try {
                resultList.add(processSingleEntry(hyda, cols, desiredItems, file));
            } catch (FileNotFoundException e) {
                log.log(Level.INFO, "Could not add archive file:", e);
            }
        }
        return resultList;
    }

//    // Test code:
//    public static void main(String[] args) throws FileNotFoundException, IOException, ServerResponseException {
//        HylaDirAccessor hyda = new FileHylaDirAccessor(new File("/mnt/archive"));
//        final FmtItemList<QueueFileFormat> cols = new FmtItemList<QueueFileFormat>(QueueFileFormat.values(), QueueFileFormat.getRequiredFormats());
//        cols.addAll(Arrays.asList(QueueFileFormat.values()));
//        final List<ArchiveYajJob> jobs = getArchiveFiles(hyda, hyda.listDirectory(), cols);
//        
//        SwingUtilities.invokeLater(new Runnable() { 
//            public void run() {
//                JFrame frame = new JFrame("Archive");
//                JPanel contentPane = new JPanel(new BorderLayout());
//                ArchiveTableModel tableModel = new ArchiveTableModel();
//                tableModel.setData(jobs);
//                tableModel.columns = cols;
//                JTable table = new JTable(tableModel);
//                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//                
//                contentPane.add(new JScrollPane(table), BorderLayout.CENTER);
//                frame.setContentPane(contentPane);
//                frame.pack();
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.setVisible(true);
//                
//                InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//                for (KeyStroke key : im.allKeys()) {
//                    System.out.println(key + ": " + im.get(key));
//                }
//            }
//        });
//        
//        for (int i = 0; i < cols.size(); i++) {
//            System.out.print(cols.get(i).getDescription() + "\t");
//        }
//        System.out.println();
//        for (YajJob<QueueFileFormat> job : jobs) {
//            for (int i = 0; i < cols.size(); i++) {
//                System.out.print(job.getData(i) + "\t");
//            }
//            System.out.println();
//        }
//    }
}
