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
package yajhfc;

import java.awt.Cursor;
import java.awt.HeadlessException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginManager.PluginInfo;
import yajhfc.send.LocalFileTFLItem;
import yajhfc.send.SendController;
import yajhfc.send.SendControllerListener;
import yajhfc.send.StreamTFLItem;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressWorker.ProgressUI;

/**
 * @author jonas
 *
 */
public class NoGUISender extends JFrame implements ProgressUI {

    JLabel progressLabel, noteLabel;
    JProgressBar progressBar;
    
    /**
     * @throws HeadlessException
     */
    public NoGUISender() throws HeadlessException {
        super(Utils.AppShortName);
        
        JPanel progressPanel = new JPanel(null);
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        
        progressLabel = new JLabel("Logging in...");
        noteLabel = new JLabel();
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
                
        progressPanel.add(progressLabel);
        progressPanel.add(noteLabel);
        progressPanel.add(Box.createVerticalStrut(5));
        progressPanel.add(progressBar);
        
        setContentPane(progressPanel);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }


    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#close()
     */
    public void close() {
        dispose();
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#setNote(java.lang.String)
     */
    public void setNote(String note) {
        noteLabel.setText(note);
        pack();
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#setProgress(int)
     */
    public void setProgress(int progress) {
        if (progressBar.isIndeterminate())
            progressBar.setIndeterminate(false);
        progressBar.setValue(progress);
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#showDeterminateProgress(java.lang.String, java.lang.String, int, int)
     */
    public void showDeterminateProgress(String message, String initialNote,
            int min, int max) {
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setValue(min);
        commonProgressSetup(message, initialNote);
    }

    public boolean supportsIndeterminateProgress() {
        return true;
    }
    
    public void showIndeterminateProgress(String message, String initialNote) {
        progressBar.setIndeterminate(true);
        commonProgressSetup(message, initialNote);
    }
    
    protected void commonProgressSetup(String progressText, String noteText) {
        progressLabel.setText(progressText);
        setNote(noteText);
    }
    
    public boolean isShowingIndeterminate() {
        return progressBar.isIndeterminate();
    }
    
    public void setMaximum(int progress) {
        progressBar.setMaximum(progress);
    }
    
    public static void startUpWithoutUI(List<String> recipients, List<String> files, List<PluginInfo> plugins, boolean noPlugins, boolean useStdIn, boolean useCover, String subject, String comment) {
        if (recipients.size() == 0) {
            System.err.println("In no GUI mode you have to specify at least one recipient.");
            System.exit(1);
        }
        if (files.size() == 0 && !useStdIn) {
            System.err.println("In no GUI mode you have to specify at least one file to send or --stdin.");
            System.exit(1);
        }
        
        // Load plugins:
        if (!noPlugins) {
            PluginManager.readPluginList();
        }
        PluginManager.addPlugins(plugins);
        PluginManager.loadAllKnownPlugins();
        
        NoGUISender progressFrame = new NoGUISender();
        progressFrame.showIndeterminateProgress(Utils._("Logging in..."), null);
        progressFrame.setVisible(true);
        
        try {
            HylaClientManager clientManager = new HylaClientManager(Utils.getFaxOptions());
            clientManager.forceLogin(progressFrame);
            
            SendController sendController = new SendController(clientManager, progressFrame, false, progressFrame);
            sendController.addSendControllerListener(new SendControllerListener() {
               public void sendOperationComplete(boolean success) {
                   System.exit(success ? 0 : 1);
               } 
            });
            
            for (String number : recipients) {
                sendController.getNumbers().add(new DefaultPBEntryFieldContainer().parseFromString(number));
            }
            sendController.setUseCover(useCover);
            if (subject != null)
                sendController.setSubject(subject);
            if (comment != null)
                sendController.setComments(comment);
            if (useStdIn) {
                sendController.getFiles().add(new StreamTFLItem(System.in));
            }
            for (String file : files) {
                sendController.getFiles().add(new LocalFileTFLItem(file));
            }
            
            if (sendController.validateEntries()) {
                sendController.sendFax();
            }
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(progressFrame, Utils._("Error sending the fax:"), ex);
            System.exit(2);
        }
    }
}
