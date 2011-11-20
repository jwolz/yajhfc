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
