/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.launch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.send.SendController;
import yajhfc.send.SendWinControl;
import yajhfc.send.StreamTFLItem;

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
    protected boolean closeAfterSubmit = false;
    
    protected final List<String> recipients = new ArrayList<String>();
    protected final List<String> files = new ArrayList<String>();
    protected InputStream inStream;
    protected String streamDesc = null;
    protected StreamTFLItem tflInStream;
    
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
    
    /**
     * Prepares the submit
     */
    public void prepareSubmit() throws IOException {
        if (preparedSubmit)
            return;

        if (inStream != null)
            tflInStream = new StreamTFLItem(inStream, streamDesc);
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
        SendWinControl sw = SendController.getSendWindow(Launcher2.application.getFrame(), Launcher2.application.getClientManager(), false, true);

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
