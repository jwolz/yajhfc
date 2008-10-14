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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import yajhfc.FaxOptions;
import yajhfc.FormattedFile;
import yajhfc.HylaClientManager;
import yajhfc.HylaModem;
import yajhfc.PaperSize;
import yajhfc.ProgressWorker;
import yajhfc.utils;
import yajhfc.FormattedFile.FileFormat;
import yajhfc.faxcover.Faxcover;
import yajhfc.faxcover.Faxcover.InvalidCoverFormatException;

/**
 * @author jonas
 *
 */
public class SendController {
    
    // These properties are set in the constructor:
    protected HylaClientManager clientManager;
    protected Dialog parent;
    protected boolean pollMode;
    
    // These properties have default values and may be set using getters and setters
    protected boolean useCover = false;
    protected List<HylaTFLItem> files = new ArrayList<HylaTFLItem>();
    protected List<NumberTFLItem> numbers = new ArrayList<NumberTFLItem>();
    protected String subject = "", comments = "";
    protected PaperSize paperSize = utils.getFaxOptions().paperSize; 
    protected File customCover = null;
    
    protected int maxTries = utils.getFaxOptions().maxTry;
    protected String notificationType = utils.getFaxOptions().notifyWhen.type;
    protected int resolution = utils.getFaxOptions().resolution.type;
    protected int killTime = utils.getFaxOptions().killTime;
    
    /**
     * The selected modem. Either a HylaModem or a String containing the modem's name.
     */
    protected Object selectedModem;

    private void setPaperSizes() {
        //PaperSize desiredSize = (PaperSize)comboPaperSize.getSelectedItem();
        for (HylaTFLItem item : files) {
            item.setDesiredPaperSize(paperSize);
        }
    }

    protected Faxcover initFaxCover() throws IOException, FileNotFoundException, InvalidCoverFormatException {
        FaxOptions fo = utils.getFaxOptions();   
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
                JOptionPane.showMessageDialog(parent, MessageFormat.format(utils._("Can not read file \"{0}\"!"), customCover.toString()), utils._("Error"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } else if (fo.useCustomDefaultCover) {
            if (!(new File(fo.defaultCover).canRead())) {
                JOptionPane.showMessageDialog(parent, MessageFormat.format(utils._("Can not read default cover page file \"{0}\"!"), fo.defaultCover), utils._("Error"), JOptionPane.WARNING_MESSAGE);
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

        cov.fromCompany = fo.FromCompany;
        cov.fromFaxNumber = fo.FromFaxNumber;
        cov.fromLocation = fo.FromLocation;
        cov.fromVoiceNumber = fo.FromVoiceNumber;
        cov.fromMailAddress = fo.FromEMail;
        cov.sender = fo.FromName;

        cov.comments = comments;
        cov.regarding = subject;

        cov.pageSize = paperSize;

        return cov;
    }
    protected File makeCoverFile(Faxcover cov, NumberTFLItem to) throws IOException, FileNotFoundException {
        File coverFile;

        if (to != null) {
            cov.toCompany = to.company;
            cov.toFaxNumber = to.faxNumber;
            cov.toLocation = to.location;
            cov.toName = to.name;
            cov.toVoiceNumber = to.voiceNumber;
        } else {
            //TODO: Change if necessary?
            cov.toCompany = ""; //textToCompany.getText();
            cov.toFaxNumber = ""; //textNumber.getText();
            cov.toLocation = ""; //textToLocation.getText();
            cov.toName = ""; //textToName.getText();
            cov.toVoiceNumber = ""; //textToVoiceNumber.getText();
        }

        // Create cover:
        coverFile = File.createTempFile("cover", ".tmp");
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
        }
        
        protected int calculateMaxProgress() {
            return 10000;
        }

        @Override
        public void doWork() {
            try {
                int step;
                setPaperSizes();

                if (useCover) {
                    step = 10000 / (files.size() + 1);
                    updateNote(utils._("Creating cover page"));

                    File coverFile = makeCoverFile(initFaxCover(), selectedNumber);
                    FormattedFile.viewFile(coverFile.getPath(), FileFormat.PostScript);
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
                        updateNote(MessageFormat.format(utils._("Formatting {0}"), item.getText()));
                        item.preview(SendController.this.parent, hyfc);
                        stepProgressBar(step);
                    }
                } finally {
                    clientManager.endServerTransaction();                    
                }
            } catch (Exception e1) {
                showExceptionDialog(utils._("Error previewing the documents:"), e1);
            } 
        } 
    }
    class SendWorker extends ProgressWorker {
        private final Logger log = Logger.getLogger(SendWorker.class.getName());    
        
        private void setIfNotEmpty(Job j, String prop, String val) {
            try {
                if (val.length() >  0)
                    j.setProperty(prop, utils.sanitizeInput(val));
            } catch (Exception e) {
                log.log(Level.WARNING, "Couldn't set additional job info " + prop + ": ", e);
            }
        }

        private String getModem() {
            Object sel = selectedModem;
            if (utils.debugMode) {
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
            maxProgress = 20 * files.size() + 20 * numbers.size() + 10;
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
                FaxOptions fo = utils.getFaxOptions();                    

                synchronized (hyfc) {
                    if (!pollMode) {
                        setPaperSizes();

                        if (useCover) {
                            cover = initFaxCover();
                            stepProgressBar(20);
                        }

                        // Upload documents:
                        //TEST synchronized (hyfc) {
                        hyfc.type(HylaFAXClient.TYPE_IMAGE);

                        for (HylaTFLItem item : files) {
                            updateNote(MessageFormat.format(utils._("Uploading {0}"), item.getText()));
                            item.upload(hyfc);

                            stepProgressBar(20);
                        }
                        //TEST }
                    }            

                    String modem = utils.sanitizeInput(getModem());
                    if (utils.debugMode) {
                        log.fine("Use modem: " + modem);
                        //utils.debugOut.println(modem);
                    }
                    for (NumberTFLItem numItem : numbers) {
                        updateNote(MessageFormat.format(utils._("Creating job to {0}"), numItem.getText()));

                        try {
                            String coverName = null;
                            if (cover != null) {
                                File coverFile = makeCoverFile(cover, numItem);

                                FileInputStream fi = new FileInputStream(coverFile);
                                coverName = hyfc.putTemporary(fi);
                                fi.close();

                                coverFile.delete();
                            }
                            stepProgressBar(5);

                            //TEST synchronized (hyfc) {
                            Job j = hyfc.createJob();

                            stepProgressBar(5);

                            j.setFromUser(utils.sanitizeInput(fo.user));
                            j.setNotifyAddress(utils.sanitizeInput(fo.notifyAddress));
                            j.setMaximumDials(fo.maxDial);

                            if (!pollMode) {
                                // Set general job information...
                                setIfNotEmpty(j, "TOUSER", numItem.name);
                                setIfNotEmpty(j, "TOCOMPANY", numItem.company);
                                setIfNotEmpty(j, "TOLOCATION", numItem.location);
                                setIfNotEmpty(j, "TOVOICE", numItem.voiceNumber);
                                setIfNotEmpty(j, "REGARDING", subject);
                                setIfNotEmpty(j, "COMMENTS", comments);
                                setIfNotEmpty(j, "FROMCOMPANY", fo.FromCompany);
                                setIfNotEmpty(j, "FROMLOCATION", fo.FromLocation);
                                setIfNotEmpty(j, "FROMVOICE", fo.FromVoiceNumber);

                                if (fo.regardingAsUsrKey) {
                                    setIfNotEmpty(j, "USRKEY", subject);
                                }
                            }
                            
                            String faxNumber = utils.sanitizeInput(numItem.faxNumber);
                            j.setDialstring(faxNumber);
                            j.setProperty("EXTERNAL", faxNumber); // needed to fix an error while sending multiple jobs
                            j.setMaximumTries(maxTries);
                            j.setNotifyType(notificationType);
                            j.setPageDimension(paperSize.size);
                            j.setVerticalResolution(resolution);
                            j.setSendTime("NOW"); // bug fix 
                            j.setKilltime(utils.minutesToHylaTime(killTime));  

                            j.setProperty("MODEM", modem);

                            if (pollMode) 
                                j.setProperty("POLL", "\"\" \"\"");
                            else {               
                                if (coverName != null)
                                    j.setProperty("COVER", coverName);

                                for (HylaTFLItem item : files) {
                                    j.addDocument(item.getServerName());                        
                                }

                                fo.useCover = useCover;
                                fo.useCustomCover = (customCover != null);
                                fo.CustomCover =  (customCover != null) ? customCover.getAbsolutePath() : null;
                            }

                            stepProgressBar(5);

                            hyfc.submit(j);
                            //TEST }

                            stepProgressBar(5);
                        } catch (Exception e1) {
                            showExceptionDialog(MessageFormat.format(utils._("An error occured while submitting the fax job for phone number \"{0}\" (will try to submit the fax to the other numbers anyway): "), numItem.getText()) , e1);
                        }
                    }
                }

                updateNote(utils._("Cleaning up"));
                for (HylaTFLItem item  : files) {
                    item.cleanup();
                }

            } catch (Exception e1) {
                //JOptionPane.showMessageDialog(ButtonSend, _("An error occured while submitting the fax: ") + "\n" + e1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                showExceptionDialog(utils._("An error occured while submitting the fax: "), e1);
            } 
            clientManager.endServerTransaction();
        }

        @Override
        protected void done() {
            SendController.this.parent.dispose();
        }
    }
    
    
    public SendController(HylaClientManager clientManager, Dialog parent, boolean pollMode) {
        this.clientManager = clientManager;
        this.parent = parent;
        this.pollMode = pollMode;
    }
    
    public void sendFax() {
        SendWorker wrk = new SendWorker();
        wrk.startWork(parent, utils._("Sending fax"));
    }
    
    public void previewFax(NumberTFLItem selectedNumber) {
        PreviewWorker wrk = new PreviewWorker(selectedNumber);
        wrk.startWork(parent, utils._("Previewing fax"));
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
    
    public static SendWinControl createSendWindow(Frame owner, HylaClientManager manager, boolean pollMode, boolean initiallyHideFiles) {
        SendWinControl result;
    
        if (pollMode || utils.getFaxOptions().sendWinStyle == SendWinStyle.TRADITIONAL) {
            result = new SendWin(manager, owner, pollMode);
        } else {
            result = new SimplifiedSendDialog(manager, owner, initiallyHideFiles);
        } 
        return result;
    }
}
