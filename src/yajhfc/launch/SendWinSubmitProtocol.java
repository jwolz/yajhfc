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
package yajhfc.launch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import yajhfc.Utils;
import yajhfc.file.textextract.FaxnumberExtractor;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.send.SendController;
import yajhfc.send.SendWinControl;
import yajhfc.send.StreamTFLItem;
import yajhfc.server.ServerManager;

/**
 * @author jonas
 *
 */
public class SendWinSubmitProtocol implements SubmitProtocol, Runnable {
    private static final Logger log = Logger.getLogger(SendWinSubmitProtocol.class.getName());
    
    protected String subject;
    protected String comments;
    protected String modem;
    protected Boolean useCover;
    protected Boolean extractRecipients;
    protected boolean closeAfterSubmit = false;
    
    protected final List<String> recipients = new ArrayList<String>();
    protected final List<String> files = new ArrayList<String>();
    protected InputStream inStream;
    protected String streamDesc = null;
    protected StreamTFLItem tflInStream;
    
    protected String server, identity;
    
    protected boolean preparedSubmit = false;
    
    protected long[] submittedIDs = null;
    
    public SendWinSubmitProtocol() {
    }
    
    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#setComments(java.lang.String)
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#setCover(boolean)
     */
    public void setCover(boolean useCover) {
        this.useCover = useCover;
    }
    
    public void setExtractRecipients(boolean extractRecipients)
            throws IOException {
        this.extractRecipients = extractRecipients;
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#setFiles(java.util.List)
     */
    public void addFiles(Collection<String> fileNames) {
        this.files.addAll(fileNames);
    }

    
    public void setInputStream(InputStream stream, String sourceText) {
        this.inStream = stream;
        this.streamDesc = sourceText;
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#setRecipients(java.util.List)
     */
    public void addRecipients(Collection<String> recipients) {
        this.recipients.addAll(recipients);
    }

    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#setSubject(java.lang.String)
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public void setModem(String modem) throws IOException {
        this.modem = modem;
    }

    public void setCloseAfterSubmit(boolean closeAfterSumbit) {
        this.closeAfterSubmit = closeAfterSumbit;
    }
    
    public void setIdentity(String identityToUse) throws IOException {
        this.identity = identityToUse;
    }
    
    public void setServer(String serverToUse) throws IOException {
        this.server = serverToUse;
    }
    
    /**
     * Prepares the submit
     */
    public void prepareSubmit() throws IOException {
        if (preparedSubmit)
            return;

        if (inStream != null)
            tflInStream = new StreamTFLItem(inStream, streamDesc);
        
        if (Utils.debugMode)
            log.fine("Check for extracting recipients: extractRecipients=" + extractRecipients + "; Utils.getFaxOptions().extractRecipients=" + Utils.getFaxOptions().extractRecipients);
        if ((extractRecipients != null && extractRecipients.booleanValue())
         || (extractRecipients == null && Utils.getFaxOptions().extractRecipients)) {
            try {
                if (inStream != null) {
                    log.fine("Extracting recipients from stdin");
                    FaxnumberExtractor extractor = new FaxnumberExtractor();
                    extractor.extractFromMultipleFiles(Collections.singletonList(tflInStream.getPreviewFilename()), recipients);
                } else if (files.size() > 0) {
                    log.fine("Extracting recipients from input files");
                    FaxnumberExtractor extractor = new FaxnumberExtractor();
                    extractor.extractFromMultipleFileNames(files, recipients);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error extracting recipients", e);
            }
        }
        
        preparedSubmit = true;
    }


    /* (non-Javadoc)
     * @see yajhfc.launch.SubmitProtocol#submitNoWait()
     */
    public long[] submit(boolean wait) throws IOException {
        prepareSubmit();
        if (wait) {
            if (SwingUtilities.isEventDispatchThread()) {
                run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(this);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error submitting the fax:", e);
                }
            }
            return submittedIDs;
        } else {
            SwingUtilities.invokeLater(this);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
//        log.fine("Running...");
//        Launcher2.application.bringToFront();

        log.fine("Initializing SendWin");
        SendWinControl sw = SendController.getSendWindow(Launcher2.application.getFrame(), ServerManager.getDefault().getCurrent(), false, true);

        if (server != null) {
            sw.setServer(server);
        }
        if (identity != null) {
            sw.setIdentity(identity);
        }
        if (inStream != null) {                
            sw.addInputStream(tflInStream);
        } else {
            for (String fileName : files)
                sw.addLocalFile(fileName);
        }
        if (recipients != null && recipients.size() > 0) {
            DefaultPBEntryFieldContainer.parseCmdLineStrings(sw.getRecipients(), recipients);
        }
        if (useCover != null) {
            sw.setUseCover(useCover);
        }
        if (subject != null) {
            sw.setSubject(subject);
        }
        if (comments != null) {
            sw.setComment(comments);
        }
        if (modem != null) {
            sw.setModem(modem);
        }
        if (Launcher2.application.getFrame().isVisible()) {
            Launcher2.application.getFrame().toFront();
        }
        log.fine("Showing SendWin");
        sw.setVisible(true);
        log.fine("SendWin closed");
        
        if (sw.getModalResult()) {
            List<Long> idList = sw.getSubmittedJobIDs();
            long[] ids = new long[idList.size()];
            for (int i=0; i<ids.length; i++) {
                ids[i] = idList.get(i).longValue();
            }
            submittedIDs = ids;
        } else {
            submittedIDs = null;
        }
        
        if (closeAfterSubmit)
            Launcher2.application.dispose();
    }

}
