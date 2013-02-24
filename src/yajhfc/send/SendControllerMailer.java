/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2013 Jonas Wolz <info@yajhfc.de>
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

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.regex.Pattern;

import yajhfc.SenderIdentity;

/**
 * A stub to support sending mails instead of faxes
 * 
 * @author jonas
 *
 */
public abstract class SendControllerMailer {

    /**
     * The SendControllerMailer implementation instance or null if no such implementation is available
     */
    protected static SendControllerMailer INSTANCE = null;
    
    /**
     * Determines if an instance is available
     * @return
     */
    public static boolean isAvailable() {
        return INSTANCE != null;
    }

	public static SendControllerMailer getInstance() {
		return INSTANCE;
	}

	public static Pattern getDefaultMailPattern() {
        return Pattern.compile("@@\\s*mail(?:recipient)?\\s*:?(.+?)@@", Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Mails the documents from the specified SendController as PDF attachment to the specified addresses
     * @param controller
     * @param mailAdresses
     * @return true if the message has been sent successfully
     */
    public abstract boolean mailToRecipients(String subject, String body, Collection<String> mailAdresses, File attachment, String attachmentName, SenderIdentity fromIdentity) throws MailException;
    
    /**
     * Mails the documents from the specified SendController as PDF attachment to the specified addresses
     * @param controller
     * @param mailAdresses
     * @return true if the message has been sent successfully
     */
    public abstract boolean mailToRecipients(SendController controller, String subject, String body, Collection<String> mailAdresses) throws MailException;
    
    /**
     * Mails the documents from the specified SendController as PDF attachment to the specified addresses
     * @param controller
     * @param mailAdresses
     * @return true if the message has been sent successfully
     */
    public abstract boolean mailToRecipients(SendController controller, Collection<String> mailAdresses) throws MailException;
    
    /**
     * Returns a message format to automatically generate an attachment PDF file name from the send time.
     * @return
     */
    public abstract MessageFormat getAttachmentNameFormat();
    
    /**
     * An exception sending the mail
     * @author jonas
     *
     */
    public static class MailException extends Exception {

        public MailException() {
        }

        public MailException(String message) {
            super(message);
        }

        public MailException(Throwable cause) {
            super(cause);
        }

        public MailException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
