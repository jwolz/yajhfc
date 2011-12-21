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
package yajhfc;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import yajhfc.TextViewPanel.Text;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.util.ProgressWorker;

/**
 * @author jonas
 *
 */
public class LogViewWorker extends ProgressWorker {
    protected List<FaxJob<? extends FmtItem>> jobs;
    protected List<Text> logList;
    protected FaxListConnection connection;
    
    public LogViewWorker(FaxListConnection connection, List<FaxJob<? extends FmtItem>> jobs, ProgressUI progressMonitor) {
        this.jobs = jobs;
        this.progressMonitor = progressMonitor;
        this.connection = connection;
    }

    @Override
    protected int calculateMaxProgress() {
        return jobs.size() * 100;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker#doWork()
     */
    @Override
    public void doWork() {
        if (jobs.size() == 0)
            return;

        MessageFormat processingX = new MessageFormat(Utils._("Getting log for {0}"));
        MessageFormat jobX = new MessageFormat(Utils._("Fax job {0} ({1})"));

        logList = new ArrayList<Text>(jobs.size());

        try {
            connection.beginMultiOperation();
            try {
                ByteArrayOutputStream logStream = new ByteArrayOutputStream(4000);
                for (FaxJob<?> job : jobs) {
                    updateNote(processingX.format(new Object[] { job.getIDValue() } ));

                    FaxDocument hsf = job.getCommunicationsLog();
                    if (hsf == null) {
                        logList.add(new Text(jobX.format(new Object[] { job.getIDValue(), Utils._("<none>")} ), Utils._("There is no log file available for this fax job.")));
                    } else {
                        String caption = jobX.format(new Object[] { job.getIDValue(), hsf.getPath() } );
                        try {
                            logStream.reset();
                            hsf.downloadToStream(logStream);
                            String logText = logStream.toString(connection.getOptions().hylaFAXCharacterEncoding);
                            logList.add(new Text(caption, logText));
                        } catch (Exception e) {
                            logList.add(new Text(caption, 
                                    Utils._("Error retrieving the log:") + '\n' + e.toString()));
                        }
                    }
                    stepProgressBar(100);
                }
            } finally {
                connection.endMultiOperation();
            }
        } catch (Exception e) {
            showExceptionDialog(Utils._("Error retrieving the log:"), e);
        } 
    }
    
    @Override
    protected void done() {       
        TextViewPanel.displayFrame(Utils._("View log"), logList, false);
    }
}
