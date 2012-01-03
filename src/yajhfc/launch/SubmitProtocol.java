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
import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import yajhfc.file.textextract.RecipientExtractionMode;

/**
 * @author jonas
 *
 */
public interface SubmitProtocol {
    /**
     * Adds the documents attached to the fax
     * @param fileNames
     */
    void addFiles(Collection<String> fileNames) throws IOException;
    /**
     * Sets the input stream for the fax
     * @param stream
     * @param sourceText a textual description of the stream for user display (may be null)
     */
    void setInputStream(InputStream stream, String sourceText) throws IOException;
    
    /**
     * Adds recipients to use
     * @param recipients
     */
    void addRecipients(Collection<String> recipients) throws IOException;
    
    /**
     * Sets if a cover page should be used
     * @param useCover
     */
    void setCover(boolean useCover) throws IOException; 
    
    /**
     * Sets if a recipients should be extracted from the specified documents
     * @param useCover
     */
    void setExtractRecipients(RecipientExtractionMode extractRecipients) throws IOException; 
    
    /**
     * Sets the subject for the fax
     * @param subject
     * @throws IOException
     */
    void setSubject(String subject) throws IOException;
    /**
     * Sets the comment for the new fax
     * @param comments
     * @throws IOException
     */
    void setComments(String comments) throws IOException; 
   
    /**
     * Sets the modem to use for the new fax
     * @param modem
     * @throws IOException
     */
    void setModem(String modem) throws IOException;
    
    /**
     * Sets if the application should close after submitting the fax
     * @param closeAfterSumbit
     * @throws IOException
     * @throws OperationNotSupportedException
     */
    void setCloseAfterSubmit(boolean closeAfterSumbit) throws IOException, OperationNotSupportedException;
    
    /**
     * Sets the server to use for the new fax
     * @param serverToUse
     * @throws IOException
     */
    void setServer(String serverToUse) throws IOException;
    /**
     * Sets the identity to use for the new fax
     * @param identityToUse
     * @throws IOException
     */
    void setIdentity(String identityToUse) throws IOException;
    
    /**
     * Shows the send dialog, configures it as specified and optionally waits for the user to close it.
     * After calling this method, the other methods may not be used any more!
     * @param wait Wait for the user interaction to complete
     * @return if wait == true the IDs of the successfully submitted faxes, else the result is undefined (probably null)
     */
    long[] submit(boolean wait) throws IOException;

}
