/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2014 Jonas Wolz <info@yajhfc.de>
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
package yajhfc;

import static yajhfc.Utils._;
import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.HylafaxWorker;
import yajhfc.model.servconn.JobState;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.send.TimeToSendEntry;
import yajhfc.server.ServerOptions;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ProgressWorker;

/**
 * @author jonas
 *
 */
public class EditJobWorker extends ProgressWorker {
    static final Logger log = Logger.getLogger(EditJobWorker.class.getName());
    
    private FaxJob<? extends FmtItem>[] selJobs;
    private MainWin mw;
    
    
    @Override
    protected int calculateMaxProgress() {
        return 20 + 10*selJobs.length;
    }
    
    @Override
    public void doWork() {
        try {
            MessageFormat infoFormat = new MessageFormat(_("Modifying job {0}"));
            MessageFormat suspendFormat = new MessageFormat(_("Suspending job {0}"));
            MessageFormat resumeFormat = new MessageFormat(_("Resuming job {0}"));
            for (FaxJob<? extends FmtItem> job : selJobs) {
                try {
                    String editNote = infoFormat.format(new Object[]{job.getIDValue().toString()});
                    updateNote(editNote);

                    JobState jobstate = job.getCurrentJobState();
                    if (Utils.debugMode)
                        log.fine("Job state is " + jobstate);
                    if (jobstate == JobState.RUNNING) {
                        showMessageDialog(MessageFormat.format(_("Job {0} is currently running, cannot modify this job."), job.getIDValue().toString()), _("Modify job parameters"), JOptionPane.WARNING_MESSAGE);
                        continue;
                    } else if (jobstate != JobState.SUSPENDED) {
                        updateNote(suspendFormat.format(new Object[]{job.getIDValue().toString()}));
                        job.suspend();
                        updateNote(editNote);
                    }
                    Map<String,String> props = job.getJobProperties(EditJobDialog.SUPPORTED_PROPERTIES);
                    if (Utils.debugMode)
                        log.fine("Retrieved properties " + props);

                    EditJobDialogRunnable runner = new EditJobDialogRunnable(mw, job, props);
                    SwingUtilities.invokeAndWait(runner);
                    final HylafaxWorker res = runner.getResult();
                    if (res == null) { // User selected Cancel
                        if (jobstate != JobState.SUSPENDED) { // Job was not suspended before
                            updateNote(resumeFormat.format(new Object[]{job.getIDValue().toString()}));
                            job.resume();
                        }
                        break;
                    }

                    // Set properties. N.b.: This will resume the job
                    job.doHylafaxWork(res);

                    if (jobstate == JobState.SUSPENDED) { // Suspend job again if it was suspended when we began
                        updateNote(suspendFormat.format(new Object[]{job.getIDValue().toString()}));
                        Thread.sleep(1000);
                        job.suspend();
                    }

                    stepProgressBar(10);
                } catch (Exception e1) {
                    String msgText;
                    if (job == null)
                        msgText = _("Error modifying a fax job") + ":\n";
                    else
                        msgText = MessageFormat.format(_("Error modifying the fax job \"{0}\"") + ":\n", job.getIDValue());
                    showExceptionDialog(msgText, e1);
                }
            }
        } catch (Exception ex) {
            showExceptionDialog(_("Error modifying a fax job"), ex);
        }
    }
    @Override
    protected void done() {
        mw.refreshTables();
    }
    
    public static int parseKilltime(String killtime) throws NumberFormatException {
        int kt = Integer.parseInt(killtime);
        // Format is ddhhmm
        return   ( (kt/10000)*24          // days 
                + ((kt/100)%100) ) * 60   // hours                        
                + kt%100;                 // minutes 
    }
    
    public static Date parseSendtime(String sendtime) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        //df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.parse(sendtime);
    }
    
    public EditJobWorker(MainWin mw, TooltipJTable<? extends FmtItem> selTable) {
        this.mw = mw;
        this.selJobs = selTable.getSelectedJobs();
        this.progressMonitor = mw.getTablePanel();
        this.setCloseOnExit(false);
    }

    static class EditHylafaxWorker implements HylafaxWorker {
        public final Date sendTimeDate;
        public final String sendTimeString;
        public final int killTime;
        public final String number;
        public final Map<String,String> customProperties;
        
        public Object work(HylaFAXClient hyfc, Job job) throws IOException,
                ServerResponseException {
            if (sendTimeDate != null)
                job.setSendTime(sendTimeDate);
            if (sendTimeString != null)
                job.setSendTime(sendTimeString);
            if (killTime > 0) 
                job.setKilltime(Utils.minutesToHylaTime(killTime));
            if (number != null) {
                job.setDialstring(number);
                job.setProperty("EXTERNAL", number);
            }
            if (customProperties != null && customProperties.size() > 0) {
                for (Map.Entry<String,String> prop : customProperties.entrySet()) {
                    job.setProperty(prop.getKey(), prop.getValue());
                }
            }
            
            hyfc.jsubm();
            return null;
        }

        public EditHylafaxWorker(Date sendTimeDate, String sendTimeString,
                int killTime, String number,
                Map<String, String> customProperties) {
            super();
            this.sendTimeDate = sendTimeDate;
            this.sendTimeString = sendTimeString;
            this.killTime = killTime;
            this.number = number;
            this.customProperties = customProperties;
        }
        
        
    }
    
    static class EditJobDialog extends JDialog {
        public static String[] SUPPORTED_PROPERTIES = {"EXTERNAL", "DIALSTRING", "LASTTIME", "SENDTIME", "MAXTRIES"/*, "TOTTRIES", "MAXDIALS", "TOTDIALS"*/};
        private static int border = 6;
        
        FaxJob<? extends FmtItem> job;
        Action okAction;
        
        protected JSpinner spinKillTime;
        protected JSpinner spinMaxTries;
        protected TimeToSendEntry ttsEntry;
        protected JTextField textNumber;
        
        String origNumber;
        int origKillTime;
        int origMaxTries;
        Date origTTS;
        
        boolean modalResult = false;
        
        private static String getTitle(FaxJob<? extends FmtItem> job) {
            return MessageFormat.format(_("Modify parameters of job {0}"), job.getIDValue().toString());
        }
        
        public EditJobDialog(Dialog owner, FaxJob<? extends FmtItem> job, Map<String,String> properties) {
            super(owner, getTitle(job), true);
            initialize(job);
            loadValues(properties);
        }

        public EditJobDialog(Frame owner, FaxJob<? extends FmtItem> job, Map<String,String> properties) {
            super(owner, getTitle(job), true);
            initialize(job);
            loadValues(properties);
        }
        
        private void initialize(FaxJob<? extends FmtItem> job) {
            this.job = job;            
            
            okAction = new ExcDialogAbstractAction() {
                @Override
                protected void actualActionPerformed(ActionEvent e) {
                    modalResult = true;
                    dispose();
                }
            };
            okAction.putValue(Action.NAME, Utils._("OK"));
            
            CancelAction cancelAction = new CancelAction(this);
            
            double[][] lay = {
                    {border, TableLayout.PREFERRED, border/2, TableLayout.FILL, border},
                    {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED, border}
            };
            
            JPanel contentPane = new JPanel(new TableLayout(lay));
            
            spinKillTime = new JSpinner(new SpinnerNumberModel(180, 0, ServerOptions.MAX_KILLTIME, 15));

            spinMaxTries = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
            
            ttsEntry = new TimeToSendEntry();
            
            textNumber = new JTextField();
            ClipboardPopup.DEFAULT_POPUP.addToComponent(textNumber);
            
            Box buttonBox = Box.createHorizontalBox();
            Dimension spacer = new Dimension(border,border);
            buttonBox.add(Box.createHorizontalGlue());
            buttonBox.add(new JButton(okAction));
            buttonBox.add(Box.createRigidArea(spacer));
            buttonBox.add(cancelAction.createCancelButton());
            buttonBox.add(Box.createHorizontalGlue());
            
            Utils.addWithLabelHorz(contentPane, textNumber, Utils._("Fax number")+":", "3,1,F,C");
            Utils.addWithLabelHorz(contentPane, spinKillTime, Utils._("Cancel job after (minutes):"), "3,3,F,C");
            Utils.addWithLabelHorz(contentPane, spinMaxTries, Utils._("Maximum tries:"), "3,5,F,C");
            Utils.addWithLabelHorz(contentPane, ttsEntry, Utils._("Time to send:"), "3,7,f,c");
            
            contentPane.add(buttonBox, "0,11,4,11,f,f");
            
            setContentPane(contentPane);
            pack();
            if (getWidth() < 600)
                setSize(600, getHeight());
            Utils.setDefWinPos(this);
        }
        
        public void loadValues(Map<String,String> properties) {
            origNumber = Utils.firstDefined(properties.get("DIALSTRING"), properties.get("EXTERNAL"), "");
            textNumber.setText(origNumber);
            
            try {
                origKillTime = parseKilltime(properties.get("LASTTIME"));
                spinKillTime.setValue(origKillTime);
            } catch (NumberFormatException e) {
                log.log(Level.WARNING, "Invalid kill time " + properties.get("LASTTIME"), e);
                origKillTime = (Integer)spinKillTime.getValue();
            }
            
            try {
                origMaxTries = Integer.parseInt(properties.get("MAXTRIES"));
                spinMaxTries.setValue(origMaxTries);
            } catch (NumberFormatException e) {
                log.log(Level.WARNING, "Invalid max tries " + properties.get("MAXTRIES"), e);
                origMaxTries = (Integer)spinMaxTries.getValue();
            }
            
            try {
                origTTS = parseSendtime(properties.get("SENDTIME"));
                ttsEntry.setSelection(origTTS);
            } catch (ParseException e) {
                log.log(Level.WARNING, "Invalid send time " + properties.get("SENDTIME"), e);
                origTTS = null;
            }

        }
        
        /**
         * Returns a worker to change the properties. Returns null if the user selected cancel
         * @return
         */
        public HylafaxWorker getResult() {
            if (!modalResult)
                return null;
            
            Map<String,String> props = new HashMap<String,String>();
            String number = textNumber.getText();
            int killTime = (Integer)spinKillTime.getValue();
            int maxTries = (Integer)spinMaxTries.getValue();
            Date tts = ttsEntry.getSelection();
            
            if (number.equals(origNumber)) // If unchanged, don't set property
                number = null;
            
            if (tts == null) {
                tts = new Date(System.currentTimeMillis() + 5000); // NOW = In 5 seconds (to allow us to suspend the job again)
            } else if (tts.equals(origTTS))
                tts = null;
            
            if (killTime == origKillTime && tts==null) // Set kill time if it has changed *or* if the time to send has changed (because HylaFAX will reset the kill time if you change the TTS)
                killTime = -1;
            
            if (maxTries != origMaxTries)
                props.put("MAXTRIES", String.valueOf(maxTries));
            
            
            return new EditHylafaxWorker(tts, null, killTime, number, props);
        }
    }
    
    static class EditJobDialogRunnable implements Runnable {
        FaxJob<? extends FmtItem> job;
        Map<String,String> properties;
        Window owner;
        
        HylafaxWorker result;
        
        public EditJobDialogRunnable(Window owner, FaxJob<? extends FmtItem> job, Map<String,String> properties) {
            this.owner = owner;
            this.job = job;
            this.properties = properties;
        }
        
        public void run() {
            EditJobDialog editDialog;
            if (owner instanceof Frame) {
                editDialog = new EditJobDialog((Frame)owner, job, properties);
            } else if (owner instanceof Dialog) {
                editDialog = new EditJobDialog((Dialog)owner, job, properties);
            } else {
                throw new RuntimeException();
            }

            editDialog.setVisible(true);
            result = editDialog.getResult();
        }
        
        /**
         * Returns a worker to change the properties. Returns null if the user selected cancel
         * @return
         */
        public HylafaxWorker getResult() {
            return result;
        }
    }
}
