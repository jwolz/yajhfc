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
import java.util.Collection;

import javax.naming.OperationNotSupportedException;

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
    
    void setSubject(String subject) throws IOException;
    
    void setComments(String comments) throws IOException; 
   
    void setModem(String modem) throws IOException;
    
    void setCloseAfterSubmit(boolean closeAfterSumbit) throws IOException, OperationNotSupportedException;
    
    /**
     * Shows the send dialog, configures it as specified and optionally waits for the user to close it.
     * After calling this method, the other methods may not be used any more!
     * @return if wait == true the IDs of the successfully submitted faxes, else the result is undefined (probably null)
     */
    long[] submit(boolean wait) throws IOException;

}
