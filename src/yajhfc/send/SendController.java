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

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;

import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import yajhfc.FaxOptions;
import yajhfc.HylaClientManager;
import yajhfc.HylaModem;
import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.faxcover.Faxcover.InvalidCoverFormatException;
import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConverter;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.util.ProgressWorker;
import yajhfc.util.ProgressWorker.ProgressUI;

/**
 * @author jonas
 *
 */
public class SendController {
    
    // These properties are set in the constructor:
    protected HylaClientManager clientManager;
    protected Window parent;
    protected boolean pollMode;
    
    // These properties have default values and may be set using getters and setters
    protected boolean useCover = false;
    protected List<HylaTFLItem> files = new ArrayList<HylaTFLItem>();
    protected List<NumberTFLItem> numbers = new ArrayList<NumberTFLItem>();
    protected String subject = "", comments = "";
    protected PaperSize paperSize = Utils.getFaxOptions().paperSize; 
    protected File customCover = null;
    
    protected int maxTries = Utils.getFaxOptions().maxTry;
    protected String notificationType = Utils.getFaxOptions().notifyWhen.getType();
    protected int resolution = Utils.getFaxOptions().resolution.getResolution();
    protected int killTime = Utils.getFaxOptions().killTime;
    // null = "NOW"
    protected Date sendTime = null;
    
    protected ProgressUI progressMonitor = null;
    
    /**
     * The selected modem. Either a HylaModem or a String containing the modem's name.
     */
    protected Object selectedModem = Utils.getFaxOptions().defaultModem;
    
    private static final int FILE_DISPLAY_LEN = 30;
    
    public ProgressUI getProgressMonitor() {
        return progressMonitor;
    }

    public void setProgressMonitor(ProgressUI progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    private void setPaperSizes() {
        //PaperSize desiredSize = (PaperSize)comboPaperSize.getSelectedItem();
        for (HylaTFLItem item : files) {
            item.setDesiredPaperSize(paperSize);
        }
    }

    protected Faxcover initFaxCover() throws IOException, FileNotFoundException, InvalidCoverFormatException {
        FaxOptions fo = Utils.getFaxOptions();   
        Faxcover cov;

        File coverTemplate = null;
        if (customCover != null) {
            coverTemplate = customCover;
        } else if (fo.useCustomDefaultCover) {
            coverTemplate = new File(fo.defaultCover);
        }
        cov = Faxcover.createInstanceForTemplate(coverTemplate);
        
        
        cov.pageCount = 0;

        if (customCover != null) {
            if (!(customCover.canRead())) {
                JOptionPane.showMessageDialog(parent, MessageFormat.format(Utils._("Can not read file \"{0}\"!"), customCover.toString()), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } else if (fo.useCustomDefaultCover) {
            if (!(new File(fo.defaultCover).canRead())) {
                JOptionPane.showMessageDialog(parent, MessageFormat.format(Utils._("Can not read default cover page file \"{0}\"!"), fo.defaultCover), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        for (HylaTFLItem item : files) {

            InputStream strIn = item.getInputStream();
            if (strIn != null) {
                // Try to get page count 
                cov.estimatePostscriptPages(strIn);
                strIn.close();
            }
        }
        
        cov.fromData = fo.getCoverFrom();
        cov.nameRule = fo.coverNameRule;
        cov.locationRule = fo.coverLocationRule.generateRule(fo.coverZIPCodeRule);
        cov.companyRule = fo.coverCompanyRule;
        
        cov.comments = comments;
        cov.regarding = subject;

        cov.pageSize = paperSize;

        return cov;
    }
    protected File makeCoverFile(Faxcover cov, NumberTFLItem to) throws IOException, FileNotFoundException {
        File coverFile;

        if (to != null) {
            cov.toData = to.fields;
        } else {
            cov.toData = new DefaultPBEntryFieldContainer("");
        }

        // Create cover:
        coverFile = File.createTempFile("cover", ".ps");
        coverFile.deleteOnExit();
        FileOutputStream fout = new FileOutputStream(coverFile);
        cov.makeCoverSheet(fout);
        fout.close();                       

        return coverFile;
    }

    class PreviewWorker extends ProgressWorker {
        protected NumberTFLItem selectedNumber;
        
        public PreviewWorker(NumberTFLItem selectedNumber) {
            this.selectedNumber = selectedNumber;
            this.progressMonitor = SendController.this.progressMonitor;
        }
        
        protected int calculateMaxProgress() {
            return 15000;
        }

        @Override
        public void doWork() {
            try {
                int step;
                setPaperSizes();
                List<FormattedFile> viewFiles = new ArrayList<FormattedFile>(files.size() + 1);
                
                if (useCover) {
                    step = 10000 / (files.size() + 1);
                    updateNote(Utils._("Creating cover page"));

                    File coverFile = makeCoverFile(initFaxCover(), selectedNumber);
                    //FormattedFile.viewFile(coverFile.getPath(), FileFormat.PostScript);
                    viewFiles.add(new FormattedFile(coverFile));
                    setProgress(step);
                } else {
                    if (files.size() > 0)
                        step = 10000 / files.size();
                    else
                        step = 0;
                }
                HylaFAXClient hyfc = clientManager.beginServerTransaction(SendController.this.parent);
                if (hyfc == null) {
                    return;
                }
                try {
                    for (HylaTFLItem item : files) {
                        updateNote(MessageFormat.format(Utils._("Formatting {0}"), Utils.shortenFileNameForDisplay(item.getText(), FILE_DISPLAY_LEN)));
                        //item.preview(SendController.this.parent, hyfc);
                        viewFiles.add(item.getPreviewFilename(hyfc));
                        stepProgressBar(step);
                    }
                } finally {
                    clientManager.endServerTransaction();                    
                }
                updateNote(Utils._("Launching viewer"));
                MultiFileConverter.viewMultipleFiles(viewFiles, paperSize, true);
            } catch (Exception e1) {
                showExceptionDialog(Utils._("Error previewing the documents:"), e1);
            } 
        } 
        
    }
    class SendWorker extends ProgressWorker {
        private final Logger log = Logger.getLogger(SendWorker.class.getName());    
        private final SendFileManager fileManager;
        
        private void setIfNotEmpty(Job j, String prop, String val) {
            try {
                if (val.length() >  0)
                    j.setProperty(prop, Utils.sanitizeInput(val));
            } catch (Exception e) {
                log.log(Level.WARNING, "Couldn't set additional job info " + prop + ": ", e);
            }
        }

        private String getModem() {
            Object sel = selectedModem;
            if (Utils.debugMode) {
                log.fine("Selected modem (" + sel.getClass().getCanonicalName() + "): " + sel);
            }
            if (sel instanceof HylaModem) {
                return ((HylaModem)sel).getInternalName();
            } else {
                String str = sel.toString();
                int pos = str.indexOf(' '); // Use part up to the first space
                if (pos == -1)
                    return str;
                else
                    return str.substring(0, pos);
            }
        }

        @Override
        protected int calculateMaxProgress() {
            int maxProgress;
            maxProgress = 20 * numbers.size() + 10 + fileManager.calcMaxProgress();
            if (useCover) {
                maxProgress += 20;
            }
            return maxProgress;
        }

        @Override
        public void doWork() {
            try {
                HylaFAXClient hyfc = clientManager.beginServerTransaction(SendController.this.parent);
                if (hyfc == null) {
                    return;
                }

                //File coverFile = null;
                Faxcover cover = null;
                FaxOptions fo = Utils.getFaxOptions();                    

                if (!pollMode) {
                    setPaperSizes();

                    if (useCover) {
                        cover = initFaxCover();
                        stepProgressBar(20);
                    }

                    fileManager.uploadFiles(hyfc, this);
                }            

                String modem = Utils.sanitizeInput(getModem());
                if (Utils.debugMode) {
                    log.fine("Use modem: " + modem);
                    //Utils.debugOut.println(modem);
                }
                for (NumberTFLItem numItem : numbers) {
                    updateNote(MessageFormat.format(Utils._("Creating job to {0}"), numItem.getText()));

                    try {
                        if (cover != null) {
                            fileManager.setCoverFile(makeCoverFile(cover, numItem), hyfc);
                        }
                        stepProgressBar(5);

                        synchronized (hyfc) {
                            log.finest("In hyfc monitor");
                            Job j = hyfc.createJob();

                            stepProgressBar(5);

                            j.setFromUser(Utils.sanitizeInput(fo.user));
                            String notifyAddr = Utils.sanitizeInput(fo.notifyAddress);
                            if (notifyAddr != null && notifyAddr.length() > 0) {
                                j.setNotifyAddress(notifyAddr);
                            }
                            j.setMaximumDials(fo.maxDial);

                            if (!pollMode) {
                                // Set general job information...
                                setIfNotEmpty(j, "TOUSER", fo.coverNameRule.applyRule(numItem.fields));
                                setIfNotEmpty(j, "TOCOMPANY", fo.coverCompanyRule.applyRule(numItem.fields));
                                setIfNotEmpty(j, "TOLOCATION", fo.coverLocationRule.generateRule(fo.coverZIPCodeRule).applyRule(numItem.fields));
                                setIfNotEmpty(j, "TOVOICE", numItem.fields.get(PBEntryField.VoiceNumber));
                                setIfNotEmpty(j, "REGARDING", subject);
                                setIfNotEmpty(j, "COMMENTS", comments);
                                setIfNotEmpty(j, "FROMCOMPANY", fo.FromCompany);
                                setIfNotEmpty(j, "FROMLOCATION", fo.FromLocation);
                                setIfNotEmpty(j, "FROMVOICE", fo.FromVoiceNumber);

                                if (fo.regardingAsUsrKey) {
                                    setIfNotEmpty(j, "USRKEY", subject);
                                }
                            }

                            String faxNumber = Utils.sanitizeInput(numItem.fields.get(PBEntryField.FaxNumber));
                            j.setDialstring(faxNumber);
                            //j.setProperty("EXTERNAL", faxNumber); // needed to fix an error while sending multiple jobs
                            j.setMaximumTries(maxTries);
                            j.setNotifyType(notificationType);
                            j.setPageDimension(paperSize.getSize());
                            j.setVerticalResolution(resolution);
                            if (sendTime == null) {
                                j.setSendTime("NOW"); // bug fix
                            } else {
                                j.setSendTime(sendTime);
                            }
                            j.setKilltime(Utils.minutesToHylaTime(killTime));  

                            j.setProperty("MODEM", modem);

                            if (pollMode) 
                                j.setProperty("POLL", "\"\" \"\"");
                            else {               
                                fileManager.attachDocuments(hyfc, j, this);

                                fo.useCover = useCover;
                                fo.useCustomCover = (customCover != null);
                                fo.CustomCover =  (customCover != null) ? customCover.getAbsolutePath() : null;
                            }

                            stepProgressBar(5);

                            hyfc.submit(j);
                        }
                        log.finest("Out of hyfc monitor");
                        stepProgressBar(5);
                    } catch (Exception e1) {
                        showExceptionDialog(MessageFormat.format(Utils._("An error occured while submitting the fax job for phone number \"{0}\" (will try to submit the fax to the other numbers anyway): "), numItem.getText()) , e1);
                    }
                }

                updateNote(Utils._("Cleaning up"));
                fileManager.cleanup();
            } catch (Exception e1) {
                //JOptionPane.showMessageDialog(ButtonSend, _("An error occured while submitting the fax: ") + "\n" + e1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                showExceptionDialog(Utils._("An error occured while submitting the fax: "), e1);
            } 
            clientManager.endServerTransaction();
        }

        @Override
        protected void done() {
            SendController.this.parent.dispose();
        }
        
        public SendWorker() {
            this.progressMonitor = SendController.this.progressMonitor;
            this.fileManager = new SendFileManager(paperSize, files);
        }
    }
    
    public SendController(HylaClientManager clientManager, Window parent, boolean pollMode) {
        this(clientManager, parent, pollMode, null);
    }
    
    public SendController(HylaClientManager clientManager, Window parent, boolean pollMode, ProgressUI progressMonitor) {
        this.clientManager = clientManager;
        this.parent = parent;
        this.pollMode = pollMode;
        this.progressMonitor = progressMonitor;
    }
    
    /**
     * Validates the settings for correctness and shows problems to the user.
     * @return true if entries are valid
     */
    public boolean validateEntries() {    
        
        if (numbers.size() == 0) {
            JOptionPane.showMessageDialog(parent, Utils._("To send a fax you have to enter at least one phone number!"), Utils._("Warning"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        for (int i=0; i < numbers.size(); i++) {
            NumberTFLItem number = numbers.get(i);
            String faxNumber = number.fields.get(PBEntryField.FaxNumber);
            if (faxNumber == null || faxNumber.length() == 0) {
                JOptionPane.showMessageDialog(parent, MessageFormat.format(Utils._("For recipient {0} (\"{1}\") no fax number has been entered."), i+1, NameRule.GIVENNAME_NAME.applyRule(number.fields)), Utils._("Warning"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }
        
        if (files.size() == 0) {
            if (useCover) {
                if (JOptionPane.showConfirmDialog(parent, Utils._("You haven't selected a file to transmit, so your fax will ONLY contain the cover page.\nContinue anyway?"), Utils._("Continue?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
                    return false;
            } else {
                JOptionPane.showMessageDialog(parent, Utils._("To send a fax you must select at least one file!"), Utils._("Warning"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }   
        
        return true;
    }
    
    public void sendFax() {
        SendWorker wrk = new SendWorker();
        wrk.startWork(parent, Utils._("Sending fax"));
    }
    
    public void previewFax(NumberTFLItem selectedNumber) {
        PreviewWorker wrk = new PreviewWorker(selectedNumber);
        wrk.startWork(parent, Utils._("Previewing fax"));
    }

    
    // Getters/Setters:
    public boolean isUseCover() {
        return useCover;
    }

    public void setUseCover(boolean useCover) {
        this.useCover = useCover;
    }

    public List<NumberTFLItem> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<NumberTFLItem> numbers) {
        this.numbers = numbers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        if (subject == null) {
            throw new IllegalArgumentException("subject may not be null!");
        }
        this.subject = subject;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        if (comments == null) {
            throw new IllegalArgumentException("comments may not be null!");
        }
        this.comments = comments;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(PaperSize paperSize) {
        if (paperSize == null) {
            throw new IllegalArgumentException("paperSize may not be null!");
        }
        this.paperSize = paperSize;
    }

    public File getCustomCover() {
        return customCover;
    }

    public void setCustomCover(File customCover) {
        this.customCover = customCover;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getKillTime() {
        return killTime;
    }

    public void setKillTime(int killTime) {
        this.killTime = killTime;
    }

    public Object getSelectedModem() {
        return selectedModem;
    }

    public void setSelectedModem(Object selectedModem) {
        if (selectedModem == null) {
            throw new IllegalArgumentException("selectedModem may not be null!");
        }
        this.selectedModem = selectedModem;
    }

    public HylaClientManager getClientManager() {
        return clientManager;
    }

    public Window getParent() {
        return parent;
    }

    public boolean isPollMode() {
        return pollMode;
    }

    public List<HylaTFLItem> getFiles() {
        return files;
    }
    
    public Date getSendTime() {
        return sendTime;
    }

    /**
     * Sets the time to send. null means "now"
     * @param sendTime
     */
    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
    
    public static SendWinControl createSendWindow(Frame owner, HylaClientManager manager, boolean pollMode, boolean initiallyHideFiles) {
        SendWinControl result;
    
        if (pollMode || Utils.getFaxOptions().sendWinStyle == SendWinStyle.TRADITIONAL) {
            result = new SendWin(manager, owner, pollMode);
        } else {
            result = new SimplifiedSendDialog(manager, owner, initiallyHideFiles);
        } 
        return result;
    }
}
