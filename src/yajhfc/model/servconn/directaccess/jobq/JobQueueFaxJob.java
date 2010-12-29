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

import gnu.inet.ftp.ServerResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.JobState;
import yajhfc.model.servconn.directaccess.DirectAccessFaxDoc;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;

/**
 * @author jonas
 *
 */
public class JobQueueFaxJob extends DirectAccessFaxJob<QueueFileFormat> {
    static final Logger log = Logger.getLogger(JobQueueFaxJob.class.getName());
        
    protected JobQueueFaxJob(JobQueueFaxJobList parent, String queueNr, String fileName) throws IOException {
        super(parent, queueNr, fileName);
    }
    
    protected JobState calculateJobState() {
        String s = getRawData(QueueFileFormat.state);
        if (s == null || s.length() < 1) {
            return JobState.UNDEFINED;
        } else {
            return JobState.getJobStateFromQueueCode(s.charAt(0) - '0');
        }
    }

    public Map<String,int[]> getDesiredItems() {
        return ((JobQueueFaxJobList)parent).desiredItems;
    }
    
    public void delete() throws IOException, ServerResponseException {
        getDirAccessor().deleteFile("doneq/q" + jobID);
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
    
    @Override
    public FaxDocument getCommunicationsLog() throws IOException {
        String commID = (String)getData(QueueFileFormat.commid);
        if (commID == null || commID.length() == 0) {
            return null;
        } else {
            return new DirectAccessFaxDoc<QueueFileFormat>(this, getLogFileName(commID) , FileFormat.PlainText);
        }
    }
    
    protected String getLogFileName(String commID) {
        return  "log/c" + commID;
    }

    protected static final Map<String,FileFormat> fileEntries = new HashMap<String,FileFormat>();
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
    
    /**
     * Translated the filename from the control file (docq/docXYZ.ps) into the actual file name
     * @param fileName
     * @return
     */
    protected String translateFileName(String fileName) {
        return fileName;
    }
    
    protected void readSpoolFile(HylaDirAccessor hyda) throws IOException {
        BufferedReader qFileReader = new BufferedReader(hyda.getInputReader(fileName));
        Map<String,int[]> desiredItems = getDesiredItems();
        
        String[] result = new String[parent.getColumns().getCompleteView().size()];
        documents.clear();
        
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
                    // lastIndexOf is > -1 since indexOf is > -1
                    String fileName = realLine.substring(realLine.lastIndexOf(':') + 1);
                    documents.add(new DirectAccessFaxDoc<QueueFileFormat>(this, translateFileName(fileName), fileEntries.get(key)));
                } else {
                    if (Utils.debugMode) {
                        log.finest("Ignoring entry for queue file " + jobID + ": " + realLine);
                    }
                }
            } else {
                log.info("Malformed queue file entry for q" + jobID + ": " + realLine);
            }
        }
        qFileReader.close();
        lastModified = hyda.getLastModified(fileName);
        reloadData(result);
    }
}