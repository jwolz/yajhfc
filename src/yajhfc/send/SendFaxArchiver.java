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
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.MultiFileConverter;
import yajhfc.file.UnknownFormatException;
import yajhfc.send.email.YajMailer;
import yajhfc.shutdown.ShutdownManager;
import yajhfc.ui.YajOptionPane;

/**
 * @author jonas
 *
 */
public class SendFaxArchiver implements SendControllerListener {
	private static final Logger log = Logger.getLogger(SendFaxArchiver.class.getName());
	
    protected final SendController sendController;
    protected final YajOptionPane dialogs;
    protected final String errorDir;
    protected final String successDir;
    protected final String errorMail;

    /**
     * An Object we can call toString() on to get a log
     */
    protected final Object logger;

    public SendFaxArchiver(SendController sendController, YajOptionPane dialogs, String successDir, String errorDir, String errorMail, Object logger) {
        super();
        this.sendController = sendController;
        this.dialogs = dialogs;
        this.successDir = successDir;
        this.errorDir = errorDir;
        this.logger = logger;
        this.errorMail = errorMail;

        sendController.addSendControllerListener(this);
    }

    protected String getPdfBaseName() {
        return new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
    }
    
    public File saveFaxAsPDF(String logText, String outDir) throws IOException, UnknownFormatException, ConversionException {
        String baseName = getPdfBaseName();
        File outFile = new File(outDir, baseName + ".pdf");
        int num = 0;
        while (!outFile.createNewFile()) {
            outFile = new File(outDir, baseName + "-" + (++num) + ".pdf");
        }
        log.fine("Saving fax as " + outFile);
        MultiFileConverter.convertTFLItemsToSingleFile(sendController.getFiles(), outFile, MultiFileConvFormat.PDF, sendController.getPaperSize());

        // Write the log
        if (logText != null && logText.length() > 0) {
            String logName = outFile.getName();
            int pos = logName.lastIndexOf('.');
            if (pos < 0)
                pos = logName.length();
            logName = logName.substring(0,pos) + ".log";
            File outLog = new File(outDir, logName);
            FileWriter fw = new FileWriter(outLog);
            fw.write(logText);
            fw.close();
        }

        return outFile;
    }

    public void saveFaxAsSuccess() {
    	log.info("Fax has been sent successfully...");
        if (successDir != null) {
            try {
                saveFaxAsPDF((logger!=null) ? logger.toString() : null, successDir);
            } catch (Exception e) {
                dialogs.showExceptionDialog("Error saving fax as PDF", e);
            } 
        }
    }

    public void saveFaxAsError() {
    	log.info("Fax has not been sent successfully...");
        File pdf = null;
        if (errorDir != null) {
            try {
                pdf = saveFaxAsPDF((logger!=null) ? logger.toString() : null, errorDir);
            } catch (Exception e) {
                dialogs.showExceptionDialog("Error saving fax as PDF", e);
            } 
        }
        if (errorMail != null) {
            if (!YajMailer.isAvailable()) {
                dialogs.showMessageDialog("Mail plugin not installed, cannot send error mail.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    boolean tempPDF = (pdf == null);
                    if (tempPDF) {
                        pdf = File.createTempFile("attachment", ".pdf");
                        ShutdownManager.deleteOnExit(pdf);
                        MultiFileConverter.convertTFLItemsToSingleFile(sendController.getFiles(), pdf, MultiFileConvFormat.PDF, sendController.getPaperSize());
                    }
                    String body = Utils._("The attached fax could not be submitted successfully to HylaFAX.");
                    if (logger != null) {
                        body = body + "\n\n" + 
                                Utils._("Send log:") + "\n" +
                                "-------------------------------------------------\n" + 
                                logger;
                    }
                    
                    YajMailer mailer = YajMailer.getInstance();
                    mailer.setSubject("YajHFC: " + Utils._("Fax failed to send"));
                    mailer.setBody(body);
                    mailer.setToAddresses(errorMail);
                    mailer.addAttachment(pdf, getPdfBaseName() + ".pdf");
                    mailer.setFromIdentity(sendController.getIdentity());
                    mailer.sendMail();

                    if (tempPDF)
                        pdf.delete();
                } catch (Exception e) {
                    dialogs.showExceptionDialog("Error sending mail for failed fax", e);
                }     
            }
        }
    }



    /* (non-Javadoc)
     * @see yajhfc.send.SendControllerListener#sendOperationComplete(boolean)
     */
    public void sendOperationComplete(boolean success) {
        if (success) {
            saveFaxAsSuccess();
        } else {
            saveFaxAsError();
        }
    }

}
