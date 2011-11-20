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
package yajhfc.model.servconn.directaccess.recvq;

import gnu.inet.ftp.ServerResponseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.JobState;
import yajhfc.model.servconn.directaccess.DirectAccessFaxDoc;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.tiff.RecvTIFFReader;

/**
 * @author jonas
 *
 */
public class RecvQFaxJob extends DirectAccessFaxJob<RecvFormat> {
    private static final long serialVersionUID = 1;
    static final Logger log = Logger.getLogger(RecvQFaxJob.class.getName());
    
    protected RecvQFaxJob(RecvQFaxJobList parent,
            String queueNr, String fileName) throws IOException {
        super(parent, queueNr, fileName);
        documents.clear();
        documents.add(new DirectAccessFaxDoc<RecvFormat>(this, fileName, FileFormat.TIFF));
    }

    @Override
    protected void readSpoolFile(HylaDirAccessor hyda) throws IOException {        
        RecvTIFFReader r = new RecvTIFFReader();
        FileInputStream inStream = new FileInputStream(hyda.getFile(fileName));
        r.read(inStream);
        inStream.close();
        
        List<RecvFormat> desiredCols = parent.getColumns().getCompleteView();
        Object[] resultData = new Object[desiredCols.size()];
        for (int i = 0; i < desiredCols.size(); i++) {
            switch (desiredCols.get(i)) {
            case a: 
                resultData[i] = r.getSubAddress();
                break;
            case b:
                resultData[i] = Integer.valueOf(r.getBitRate());
                break;
            case d:
                resultData[i] = r.getDataFormatName();
                break;
            case e:
                resultData[i] = r.getReason();
                break;
            case f:
                resultData[i] = jobID;
                break;
            case h:
                resultData[i] = new Date(1000L * r.getRecvTime());
                break;
            case i:
                resultData[i] = r.getCallIDName();
                break;
            case j:
                resultData[i] = r.getCallIDNumber();
                break;
            case l:
                resultData[i] = Integer.valueOf(r.getPageLength());
                break;
            case m:
            case q:
                resultData[i] = getProtection(hyda.getProtection(fileName));
                break;
            case n:
                resultData[i] = Integer.valueOf((int)hyda.getSize(fileName));
                break;
            case o:
                resultData[i] = "<unknown>";
                break;
            case p:
                resultData[i] = Integer.valueOf(r.getNumberOfPages());
                break;
            case r:
                resultData[i] = Integer.valueOf(r.getVerticalRes());
                break;
            case s:
                resultData[i] = r.getSender();
                break;
            case t:
                resultData[i] = DateFormat.getDateTimeInstance().format(r.getDate());
                break;
            case w:
                resultData[i] = Integer.valueOf(r.getPageWidth());
                break;
            case z:
                resultData[i] = Boolean.valueOf(r.isInProgress());
                break;
            case Y:
            case Z:
                resultData[i] = r.getDate();
                break;
            }
        }
        data = resultData;
        if (Utils.debugMode) {
            log.finest(jobID + " data after reading: " + Arrays.toString(resultData));
        }

        // Calculate status
        String errorDesc = r.getReason();
        if ((errorDesc != null) && (errorDesc.length() > 0)) {
            state = JobState.FAILED;
        } else {
            if (r.isInProgress()) { // If in progress...
                state = JobState.RUNNING;
            } else {
                state = JobState.DONE;
            }
        }
    }

    protected String getProtection(int mode) {
        char[] p = new char[7];
        p[0] = '-';
        p[1] = ((mode & 040) != 0) ? 'r' : '-';
        p[2] = ((mode & 020) != 0) ? 'w' : '-';
        p[3] = ((mode & 010) != 0) ? 'x' : '-';
        p[4] = ((mode & 004) != 0) ? 'r' : '-';
        p[5] = ((mode & 002) != 0) ? 'w' : '-';
        p[6] = ((mode & 001) != 0) ? 'x' : '-';
        return new String(p);
    }
    
    @Override
    protected JobState calculateJobState() {
        return JobState.DONE;
    }

    public void delete() throws IOException, ServerResponseException {
        getDirAccessor().deleteFile(fileName);
    }

}
