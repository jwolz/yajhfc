/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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

import gnu.hylafax.HylaFAXClient;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import yajhfc.file.FormattedFile.FileFormat;
import yajhfc.model.YajJob;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressWorker;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class LogViewWorker extends ProgressWorker {
    protected List<YajJob<? extends FmtItem>> jobs;
    protected HylaClientManager clientManager;
    protected List<Log> logList;
    
    public LogViewWorker(List<YajJob<? extends FmtItem>> jobs, HylaClientManager clientManager, ProgressUI progressMonitor) {
        this.jobs = jobs;
        this.progressMonitor = progressMonitor;
        this.clientManager = clientManager;
    }

    @Override
    protected int calculateMaxProgress() {
        return jobs.size() * 100;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker#doWork()
     */
    @Override
    public void doWork() {
        if (jobs.size() == 0)
            return;
        
        MessageFormat processingX = new MessageFormat(Utils._("Getting log for {0}"));
        MessageFormat jobX = new MessageFormat(Utils._("Fax job {0} ({1})"));
        
        logList = new ArrayList<Log>(jobs.size());
        
        HylaFAXClient hyfc = clientManager.beginServerTransaction(SwingUtilities.windowForComponent(parent));
        try {
            synchronized (hyfc) {
                ByteArrayOutputStream logStream = new ByteArrayOutputStream(4000);
                for (YajJob<?> job : jobs) {
                    updateNote(processingX.format(new Object[] { job.getIDValue() } ));

                    HylaServerFile hsf = job.getCommunicationsLog();
                    if (hsf == null) {
                        logList.add(new Log(jobX.format(new Object[] { job.getIDValue(), Utils._("<none>")} ), Utils._("There is no log file available for this fax job.")));
                    } else {
                        String caption = jobX.format(new Object[] { job.getIDValue(), hsf.getPath() } );
                        try {
                            logStream.reset();
                            hsf.downloadToStream(hyfc, logStream);
                            String logText = logStream.toString(Utils.HYLAFAX_CHARACTER_ENCODING);
                            logList.add(new Log(caption, logText));
                        } catch (Exception e) {
                            logList.add(new Log(caption, 
                                    Utils._("Error retrieving the log:") + '\n' + e.toString()));
                        }
                    }
                    stepProgressBar(100);
                }
            }
        } catch (Exception e) {
            showExceptionDialog(Utils._("Error retrieving the log:"), e);
        } finally {
            clientManager.endServerTransaction();
        }
    }
    
    @Override
    protected void done() {
        LogViewDialog lvd = new LogViewDialog(logList);
        lvd.setVisible(true);
    }

    public static class Log {
        public String caption;
        public String log;
        
        public Log(String caption, String log) {
            super();
            this.caption = caption;
            this.log = log;
        }        
    }
    
    public static class LogViewDialog extends JFrame {
        private static final int border = 8;
        
        Action actSave, actCopy;
        JTabbedPane tabs;
        
        public LogViewDialog(List<Log> toDisplay) throws HeadlessException {
            super();
            initialize(toDisplay);
        }

        private void initialize(List<Log> toDisplay) {
            createActions();
            CancelAction cancelAct = new CancelAction(this, Utils._("Close"));
            
            JPanel contentPane = new JPanel(new BorderLayout());
            tabs = new JTabbedPane(JTabbedPane.BOTTOM);
            
            for (Log log : toDisplay) {
                addLog(log);
            }
            
            JPanel buttonsPane = new JPanel(new GridLayout(1, 3, border, border));
            buttonsPane.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            buttonsPane.add(new JButton(actCopy));
            buttonsPane.add(new JButton(actSave));
            buttonsPane.add(cancelAct.createCancelButton());
            
            contentPane.add(tabs, BorderLayout.CENTER);
            contentPane.add(buttonsPane, BorderLayout.SOUTH);
            
            setContentPane(contentPane);
            setTitle(Utils._("View log"));
            setIconImage(Utils.loadIcon("general/History").getImage());
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            Utils.setDefWinPos(this);
            pack();
        }
        
        private void addLog(Log log) {
            JTextArea textDisplay = new JTextArea(log.log);
            textDisplay.setEditable(false);
            textDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ClipboardPopup.DEFAULT_POPUP.addToComponent(textDisplay);

            tabs.addTab(log.caption, 
                    new JScrollPane(textDisplay, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        }
        
        private void createActions() {
            actSave = new ExcDialogAbstractAction() {
                @Override
                protected void actualActionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new SafeJFileChooser();
                    if (Utils.getFaxOptions().lastSavePath.length() > 0) {
                        fileChooser.setCurrentDirectory(new File(Utils.getFaxOptions().lastSavePath));
                    }
                    fileChooser.resetChoosableFileFilters();
                    FileFilter txtFilter = new ExampleFileFilter(FileFormat.PlainText.getPossibleExtensions(), FileFormat.PlainText.getDescription());
                    FileFilter logFilter = new ExampleFileFilter("log", Utils._("Log files"));
                    fileChooser.addChoosableFileFilter(txtFilter);
                    fileChooser.addChoosableFileFilter(logFilter);
                    fileChooser.setFileFilter(txtFilter);
                    
                    if (fileChooser.showSaveDialog(LogViewDialog.this) == JFileChooser.APPROVE_OPTION) {
                        Utils.getFaxOptions().lastSavePath = fileChooser.getCurrentDirectory().getAbsolutePath();
                        
                        File selectedFile = Utils.getSelectedFileFromSaveChooser(fileChooser);
                        JTextArea selectedText = (JTextArea)((JScrollPane)tabs.getSelectedComponent()).getViewport().getView();
                        
                        try {
                            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(selectedFile));
                            writer.write(selectedText.getText());
                            writer.close();
                        } catch (Exception e1) {
                            ExceptionDialog.showExceptionDialog(LogViewDialog.this, Utils._("Error saving the log file"), e1);
                        }
                    }
                }
            };
            actSave.putValue(Action.NAME, Utils._("Save") + "...");
            actSave.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
            
            actCopy = new ExcDialogAbstractAction() {
                @Override
                protected void actualActionPerformed(ActionEvent e) {
                    JTextArea selectedText = (JTextArea)((JScrollPane)tabs.getSelectedComponent()).getViewport().getView();
                    StringSelection contents = new StringSelection(selectedText.getText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
                }
            };
            actCopy.putValue(Action.NAME, Utils._("Copy") );
            actCopy.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Copy"));
        }
    }
}
