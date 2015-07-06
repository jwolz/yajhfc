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
package yajhfc.send.email;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.SenderIdentity;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.MultiFileConverter;
import yajhfc.send.HylaTFLItem;
import yajhfc.send.SendController;
import yajhfc.shutdown.ShutdownManager;

/**
 * A stub to support sending mails instead of faxes
 * 
 * @author jonas
 *
 */
public abstract class YajMailer {

    /**
     * The SendControllerMailer implementation instance or null if no such implementation is available
     */
    protected static Class<? extends YajMailer> IMPLEMENTATION = null;
    
    /**
     * Determines if an instance is available
     * @return
     */
    public static boolean isAvailable() {
        return (IMPLEMENTATION != null);
    }

	public static YajMailer getInstance() {
		try {
            return IMPLEMENTATION.newInstance();
        } catch (Exception e) {
            Logger.getLogger(YajMailer.class.getName()).log(Level.SEVERE, "Invalid implementation: " + IMPLEMENTATION, e);
            IMPLEMENTATION = null;
            return null;
        } 
	}

	protected SenderIdentity fromIdentity = null;
	protected String subject = null;
	protected String body = null;
	protected Collection<String> toAddresses = null;
	protected Collection<String> ccAddresses = null;
	protected Collection<String> bccAddresses = null;
	protected List<Attachment> attachments = new ArrayList<Attachment>();
	protected MessageFormat attachmentNameFormat = null;
	
    /**
     * Returns a message format to automatically generate an attachment PDF file name from the send time.
     * @return
     */
    public MessageFormat getAttachmentNameFormat() {
        if (attachmentNameFormat == null) {
            // Create a default format
            attachmentNameFormat = new MessageFormat("doc_{0,date,yyyy-MM-dd_HH-mm-ss}.pdf");
        }
        return attachmentNameFormat;
    }
    
    public void setAttachmentNameFormat(MessageFormat attachmentNameFormat) {
        this.attachmentNameFormat = attachmentNameFormat;
    }
    
    /**
     * Convenience method to send mail to the specified recipients
     * @param controller
     * @param recipients
     */
    public boolean mailToRecipients(SendController controller, Collection<String> recipients) throws MailException {
        initializeFromSendController(controller);
        setToAddresses(recipients);
        return sendMail();
    }
	
	/**
	 * Initializes the sender settings using the specified SendController object
	 * @param controller
	 */
	public void initializeFromSendController(SendController controller) throws MailException {
	    setSubject(controller.getSubject());
	    setBody(controller.getComment());
	    setFromIdentity(controller.getIdentity());
	    addPDFAttachmentForTFLItems(controller.getFiles(), controller.getPaperSize());
	}
	
	/**
	 * Adds a PDF attachment for the specified TFLItems
	 * @param items
	 */
	public void addPDFAttachmentForTFLItems(List<HylaTFLItem> items, PaperSize paperSize) throws MailException {
        MessageFormat fromFormat = getAttachmentNameFormat();
        final Date sendDate = new Date();
        
        File tempFile;
        try {
            tempFile = File.createTempFile("attachment", ".pdf");
            ShutdownManager.deleteOnExit(tempFile);
            MultiFileConverter.convertTFLItemsToSingleFile(items, tempFile, MultiFileConvFormat.PDF, paperSize);
            
            addAttachment(tempFile, fromFormat.format(new Object[] { sendDate }));
        } catch (Exception e) {
            throw new MailException("Error creating PDF for fax", e);
        } 
	}
	
	/**
	 * Adds an attachment
	 * @param file
	 * @return
	 */
	public void addAttachment(File file) {
	    addAttachment(file, null);
	}
	
	/**
     * Adds an attachment
     * @param file
     * @return
     */
    public void addAttachment(File file, String fileName) {
        attachments.add(new Attachment(fileName, file, null));
    }
    
    /**
     * Adds an attachment
     * @param saveFile
     * @return
     */
    public void addAttachment(String stringContent, String fileName) {
        attachments.add(new Attachment(fileName, null, stringContent));
    }
	
	public SenderIdentity getFromIdentity() {
        return fromIdentity;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Collection<String> getToAddresses() {
        return toAddresses;
    }

    public Collection<String> getCcAddresses() {
        return ccAddresses;
    }

    public Collection<String> getBccAddresses() {
        return bccAddresses;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setFromIdentity(SenderIdentity fromIdentity) {
        this.fromIdentity = fromIdentity;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setToAddresses(Collection<String> toAddresses) {
        this.toAddresses = toAddresses;
    }

    public void setToAddresses(String... toAddresses) {
        this.toAddresses = Arrays.asList(toAddresses);
    }
    
    public void setCcAddresses(Collection<String> ccAddresses) {
        this.ccAddresses = ccAddresses;
    }
    
    public void setCcAddresses(String... ccAddresses) {
        this.ccAddresses = Arrays.asList(ccAddresses);
    }

    public void setBccAddresses(Collection<String> bccAddresses) {
        this.bccAddresses = bccAddresses;
    }
    
    public void setBccAddresses(String... bccAddresses) {
        this.bccAddresses = Arrays.asList(bccAddresses);
    }

    
    /**
	 * Sends mail using the specified settings
	 * @return true if successful
	 */
	public abstract boolean sendMail() throws MailException;
 
	/**
	 * Returns the last sent mail's timestamp
	 * @return
	 */
	public abstract Date getLastSendTime();
	
	public static final class Attachment {
	    public final String fileName;
	    public final File file;
	    public final String textContent;
	    
        protected Attachment(String fileName, File file, String textContent) {
            super();
            this.fileName = fileName;
            this.file = file;
            this.textContent = textContent;
        }
	}
}
