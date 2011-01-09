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
package yajhfc.send;

import java.awt.Window;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import yajhfc.model.servconn.FaxDocument;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * Control methods for the send dialog
 * @author jonas
 *
 */
public interface SendWinControl {
    /**
     * Shows/hides the dialog
     * @param visible
     */
    public void setVisible(boolean visible);
    /**
     * Returns true if the user clicked "send fax"
     * @return
     */
    public boolean getModalResult();
    
    /**
     * Returns a list of the job IDs of the submitted jobs if getModalResult() == true
     * If getModalResult() == false, the result is undefined
     * @return
     */
    public List<Long> getSubmittedJobIDs();
    
    /**
     * Returns true if this dialog is in "poll fax" style
     * @return
     */
    public boolean isPollMode();
    /**
     * Returns the window used for the actual dialog
     * @return
     */
    public Window getWindow();
    
    /**
     * Adds a file on the fax server to the list of files to send
     * @param serverFile
     */
    public void addServerFile(FaxDocument serverFile);
//    /**
//     * Adds the specified recipient to list of recipients
//     * @param recipient
//     */
//    public void addRecipient(PBEntryFieldContainer recipient);
    
    //public void addRecipient(String faxNumber, String name, String company, String location, String voiceNumber);
    
    /**
     * Returns the collection of recipients. It is partly modifiable: At least the add() method is implemented.
     */
    public Collection<PBEntryFieldContainer> getRecipients();
    
    /**
     * Sets the subject of the new fax
     */
    public void setSubject(String subject);
    /**
     * Adds a document with data from the specified input stream to the list of documents to send
     * @param inStream
     */
    public void addInputStream(StreamTFLItem inStream);
    /**
     * Adds a local file to the list of documents to send
     * @param fileName
     */
    public void addLocalFile(String fileName);
    
    /**
     * Specifies if a cover page should be used
     * @param useCover
     */
    public void setUseCover(boolean useCover);
    /**
     * Specifies the comment for the new fax
     * @param comment
     */
    public void setComment(String comment);
    
    /**
     * Sets the modem to send
     * @param modem
     */
    public void setModem(String modem);
    /**
     * Sets the server to use for the new fax
     * @param serverToUse
     * @throws IOException
     */
    void setServer(String serverToUse);
    /**
     * Sets the identity to use for the new fax
     * @param identityToUse
     * @throws IOException
     */
    void setIdentity(String identityToUse);
}
