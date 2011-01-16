package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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

import static yajhfc.Utils._;
import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.export.ExportAction;
import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.MultiFileConverter;
import yajhfc.filters.AndFilter;
import yajhfc.filters.Filter;
import yajhfc.filters.FilterCreator;
import yajhfc.filters.OrFilter;
import yajhfc.filters.StringFilter;
import yajhfc.filters.StringFilterOperator;
import yajhfc.filters.ui.CustomFilterDialog;
import yajhfc.launch.Launcher2;
import yajhfc.launch.MainApplicationFrame;
import yajhfc.logconsole.LogConsole;
import yajhfc.macosx.MacOSXSupport;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.IconMap;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.TableType;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.ConnectionState;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.FaxListConnectionListener;
import yajhfc.model.servconn.FaxListConnectionType;
import yajhfc.model.servconn.JobState;
import yajhfc.model.servconn.defimpl.SwingFaxListConnectionListener;
import yajhfc.model.table.FaxListTableModel;
import yajhfc.model.table.ReadStateFaxListTableModel;
import yajhfc.model.table.UnreadItemEvent;
import yajhfc.model.table.UnreadItemListener;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.options.OptionsWin;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.NewPhoneBookWin;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;
import yajhfc.print.FaxTablePrinter;
import yajhfc.readstate.PersistentReadState;
import yajhfc.send.SendController;
import yajhfc.send.SendWinControl;
import yajhfc.server.Server;
import yajhfc.server.ServerManager;
import yajhfc.server.ServerOptions;
import yajhfc.tray.TrayFactory;
import yajhfc.tray.YajHFCTrayIcon;
import yajhfc.util.AbstractQuickSearchHelper;
import yajhfc.util.ActionJCheckBoxMenuItem;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.FileChooserRunnable;
import yajhfc.util.JTableTABAction;
import yajhfc.util.NumberRowViewport;
import yajhfc.util.ProgressPanel;
import yajhfc.util.ProgressWorker;
import yajhfc.util.SafeJFileChooser;
import yajhfc.util.SelectedActionPropertyChangeListener;
import yajhfc.util.ToolbarEditorDialog;
import yajhfc.util.ProgressWorker.ProgressUI;

@SuppressWarnings("serial")
public final class MainWin extends JFrame implements MainApplicationFrame {
    

    static final Logger log = Logger.getLogger(MainWin.class.getName());
    
    protected JPanel jContentPane = null;
    
    protected JToolBar toolbar, quickSearchbar;
    protected QuickSearchHelper quickSearchHelper = new QuickSearchHelper();
    
    protected JTabbedPane tabMain = null;
    
    protected JScrollPane scrollRecv = null;
    protected JScrollPane scrollSent = null;
    protected JScrollPane scrollSending = null;
    
    protected TooltipJTable<RecvFormat> tableRecv = null;
    protected TooltipJTable<JobFormat> tableSent = null;
    protected TooltipJTable<JobFormat> tableSending = null;
    
    protected ReadStateFaxListTableModel<RecvFormat> recvTableModel = null;  
    protected FaxListTableModel<JobFormat> sentTableModel = null;  
    protected FaxListTableModel<JobFormat> sendingTableModel = null; 
    
    protected NumberRowViewport recvRowNumbers, sentRowNumbers, sendingRowNumbers;
    
    protected JTextPane textStatus = null;
    protected JSplitPane statusSplitter;
    
    protected JMenuBar jJMenuBar = null;
    
    protected JMenu menuFax = null;
    protected JMenu menuView = null;
    protected JMenu menuExtras = null;
    protected JMenu helpMenu = null;
    protected JMenu menuTable;
    
    protected JCheckBoxMenuItem menuMarkError;
    
    protected JRadioButtonMenuItem menuViewAll, menuViewOwn, menuViewCustom;
    protected ButtonGroup viewGroup;
    
    protected FaxOptions myopts = null;

    protected MouseListener tblMouseListener;
    //protected KeyListener tblKeyListener;
    protected DefaultTableCellRenderer hylaDateRenderer;
    
    protected JPopupMenu tblPopup;
    
    protected ProgressPanel tablePanel;
    
    protected MenuViewListener menuViewListener;
    
    // Uncomment for archive support.
    protected TooltipJTable<QueueFileFormat> tableArchive;
    protected JScrollPane scrollArchive;
    protected FaxListTableModel<QueueFileFormat> archiveTableModel;
    protected NumberRowViewport archiveRowNumbers;
    
    // Actions:
    protected Action actSend, actShow, actDelete, actOptions, actExit, actAbout, actPhonebook, actReadme, actPoll, actFaxRead, actFaxSave, actForward, actAdminMode;
    protected Action actRefresh, actResend, actPrintTable, actSuspend, actResume, actClipCopy, actShowRowNumbers, actAdjustColumns, actReconnect, actEditToolbar;
    protected Action actSaveAsPDF, actSaveAsTIFF, actUpdateCheck, actAnswerCall, actSearchFax, actViewLog, actLogConsole, actExport;
    protected Action actShowToolbar, actShowQuickSearchBar;
    protected StatusBarResizeAction actAutoSizeStatus;
    protected ActionEnabler actChecker;
    protected Map<String,Action> availableActions = new HashMap<String,Action>();
    protected YajHFCTrayIcon trayIcon = null;
    protected ServerMenu serverMenu;
    
    protected Server currentServer;
    protected FaxListConnection connection;
    
    public enum SendReadyState {
        Ready, NeedToWait, NotReady;
    }
    protected SendReadyState sendReady = SendReadyState.NeedToWait;
    boolean hideMenusForMac = false;
    
    protected boolean userInitiatedLogout = false;
    
    // Worker classes:
    private class DeleteWorker extends ProgressWorker {
        //private TooltipJTable<? extends FmtItem> selTable;
        private FaxJob<? extends FmtItem>[] selJobs;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selJobs.length;
        }

        @Override
        public void doWork() {
            try {
                connection.beginMultiOperation();
                try {

                    MessageFormat infoFormat = new MessageFormat(_("Deleting fax {0}"));
                    for (FaxJob<? extends FmtItem> yj : selJobs) {
                        try {
                            updateNote(infoFormat.format(new Object[] {yj.getIDValue()}));

                            yj.delete();

                            stepProgressBar(10);
                        } catch (Exception e1) {
                            String msgText;
                            if (yj == null)
                                msgText = _("Error deleting a fax:\n");
                            else
                                msgText = MessageFormat.format(_("Error deleting the fax \"{0}\":\n"), yj.getIDValue());
                            //JOptionPane.showMessageDialog(MainWin.this, msgText + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                            showExceptionDialog(msgText, e1);
                        }
                    }

                } finally {
                    connection.endMultiOperation();
                }
            } catch (Exception ex) {
                showExceptionDialog(_("Error deleting faxes:"), ex);
            }
        }
        
        @Override
        protected void done() {
            refreshTables();
        }
        
        public DeleteWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selJobs = selTable.getSelectedJobs();
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(false);
        }
    }
    private class MultiSaveWorker extends ProgressWorker {
        private FaxJob<? extends FmtItem>[] selJobs;
        private File targetDir;
        private int fileCounter;
        private final boolean askForEveryFile;
        private JFileChooser fileChooser;
        
        @Override
        protected int calculateMaxProgress() {
            return 1000*selJobs.length;
        }

        @Override
        public void doWork() {
            fileCounter = 0;
            try {
                connection.beginMultiOperation();
                try {
                    List<String> errorInfo = new ArrayList<String>();
                    MessageFormat infoFormat = new MessageFormat(_("Saving fax {0}"));
                    for (FaxJob<? extends FmtItem> yj : selJobs) {
                        try {
                            errorInfo.clear();
                            Collection<FaxDocument> hsfs = yj.getDocuments(errorInfo);
                            if (hsfs.size() == 0) {
                                if (askForEveryFile) {
                                    StringBuffer res = new StringBuffer();
                                    new MessageFormat(_("No accessible document files are available for the fax \"{0}\".")).format(new Object[] {yj.getIDValue()}, res, null);
                                    if (errorInfo.size() > 0) {
                                        res.append("\n\n");
                                        res.append(_("The following files were inaccessible:"));
                                        res.append('\n');
                                        for (String info : errorInfo) {
                                            res.append(info).append('\n');
                                        }
                                    }
                                    showMessageDialog(res.toString(), _("Save fax"), JOptionPane.INFORMATION_MESSAGE);
                                } 
                            } else {
                                updateNote(infoFormat.format(new Object[] {yj.getIDValue()}));
                                for (FaxDocument hsf : hsfs) {
                                    try {
                                        String filename = hsf.getPath();
                                        int seppos = filename.lastIndexOf('/');
                                        if (seppos < 0)
                                            seppos = filename.lastIndexOf(File.separatorChar);
                                        if (seppos >= 0)
                                            filename = filename.substring(seppos + 1);

                                        File target = new File(targetDir, filename);
                                        if (askForEveryFile) {
                                            FileFilter[] ffs = { new ExampleFileFilter(hsf.getType().getDefaultExtension(), hsf.getType().getDescription()) };
                                            FileChooserRunnable runner = new FileChooserRunnable(MainWin.this, fileChooser, MessageFormat.format(_("Save {0} to"), hsf.getPath()), ffs, target, false);
                                            SwingUtilities.invokeAndWait(runner);
                                            if (runner.getSelection() == null) {
                                                return;
                                            }
                                            target = runner.getSelection();
                                            targetDir = target.getParentFile();
                                        }
                                        FileOutputStream outStream = new FileOutputStream(target);
                                        hsf.downloadToStream(outStream);
                                        outStream.close();
                                        fileCounter++;
                                    } catch (ServerResponseException sre) {
                                        showExceptionDialog(MessageFormat.format(_("While downloading the file {0} (job {1}), the server gave back an error code:"), hsf.getPath(), yj.getIDValue()), sre);
                                    } catch (FileNotFoundException fnfe) { 
                                        showExceptionDialog(MessageFormat.format(_("The file {0} (job {1}) could not be opened for reading (probably you lack the necessary access permissions):"), hsf.getPath(), yj.getIDValue()), fnfe);
                                    } catch (Exception e1) {
                                        //JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
                                        showExceptionDialog(MessageFormat.format(_("An error occured saving the file {0} (job {1}):"), hsf.getPath(), yj.getIDValue()), e1);
                                    }
                                }
                            }
                            if (targetDir != null)
                                Utils.getFaxOptions().lastSavePath = targetDir.getAbsolutePath();
                        } catch (Exception e1) {
                            //JOptionPane.showMessageDialog(MainWin.this, _("An error occured saving the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                            showExceptionDialog(_("An error occured saving the fax:"), e1);
                        }
                        stepProgressBar(1000);
                    }

                } finally {
                    connection.endMultiOperation();
                }
            } catch (Exception ex) {
                showExceptionDialog(_("Error saving faxes:"), ex);
            }
        }
        
        @Override
        protected void initialize() {
            if (askForEveryFile) {
                if (targetDir == null && Utils.getFaxOptions().lastSavePath.length() > 0) {
                    targetDir = new File(Utils.getFaxOptions().lastSavePath);
                }
                fileChooser = new SafeJFileChooser();
            } 
        }
        
        @Override
        protected void pMonClosed() {
            if (!askForEveryFile) {
                JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("{0} files saved to directory {1}."), fileCounter, targetDir.getPath()), _("Faxes saved"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        public MultiSaveWorker(TooltipJTable<? extends FmtItem> selTable, File targetDir, boolean askForEveryFile) {
            this.selJobs = selTable.getSelectedJobs();
            this.targetDir = targetDir;
            this.progressMonitor = tablePanel;
            this.askForEveryFile = askForEveryFile;
            this.setCloseOnExit(true);
        }
    }
    private class ShowWorker extends ProgressWorker {
        private FaxJob<? extends FmtItem>[] selJobs;
        private boolean updateReadState;
        private int sMin, sMax;
        
        @Override
        protected int calculateMaxProgress() {
            return 100 + 1200*selJobs.length;
        }
        
        @Override
        public void doWork() {
            try {
                connection.beginMultiOperation();
                try {
                    List<FormattedFile> downloadedFiles = new ArrayList<FormattedFile>();
                    List<String> errorInfo = new ArrayList<String>();
                    final MessageFormat displayingMsg      = new MessageFormat(_("Displaying fax {0}"));
                    final MessageFormat downloadingMessage = new MessageFormat(Utils._("Downloading {0}"));
                    for (FaxJob<? extends FmtItem> yj : selJobs) {
                        try {
                            updateNote(displayingMsg.format(new Object[] { yj.getIDValue() }));
                            downloadedFiles.clear();
                            errorInfo.clear();

                            //System.out.println("" + i + ": " + yj.getIDValue().toString());
                            Collection<FaxDocument> serverFiles = yj.getDocuments(errorInfo);

                            stepProgressBar(100);
                            if (serverFiles.size() == 0) {
                                StringBuffer res = new StringBuffer();
                                new MessageFormat(_("No accessible document files are available for the fax \"{0}\".")).format(new Object[] {yj.getIDValue()}, res, null);
                                if (errorInfo.size() > 0) {
                                    res.append("\n\n");
                                    res.append(_("The following files were inaccessible:"));
                                    res.append('\n');
                                    for (String info : errorInfo) {
                                        res.append(info).append('\n');
                                    }
                                }
                                showMessageDialog(res.toString(), _("Display fax"), JOptionPane.INFORMATION_MESSAGE);

                                stepProgressBar(1000);
                            } else {
                                int step = 1000 / serverFiles.size();
                                downloadedFiles.clear();
                                for(FaxDocument hsf : serverFiles) {
                                    updateNote(downloadingMessage.format(new Object[] {hsf.getPath()}));
                                    try {
                                        downloadedFiles.add(hsf.getDocument());
                                    } catch (ServerResponseException sre) {
                                        showExceptionDialog(MessageFormat.format(_("While downloading the file {0} (job {1}), the server gave back an error code:"), hsf.getPath(), yj.getIDValue()), sre);
                                    } catch (FileNotFoundException fnfe) { 
                                        showExceptionDialog(MessageFormat.format(_("The file {0} (job {1}) could not be opened for reading (probably you lack the necessary access permissions):"), hsf.getPath(), yj.getIDValue()), fnfe);
                                    } catch (Exception e1) {
                                        showExceptionDialog(MessageFormat.format(_("An error occured displaying the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()), e1);
                                    }
                                    stepProgressBar(step);
                                }
                                updateNote(Utils._("Launching viewer"));
                                if (downloadedFiles.size() > 0) {
                                    MultiFileConverter.viewMultipleFiles(downloadedFiles, currentServer.getOptions().paperSize, false);
                                }
                                stepProgressBar(100);
                            }
                            yj.setRead(true);
                        } catch (Exception e1) {
                            //JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("An error occured displaying the fax \"{0}\":\n"), yj.getIDValue()) + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                            showExceptionDialog(MessageFormat.format(_("An error occured displaying the fax \"{0}\":"), yj.getIDValue()), e1);
                        }
                    }
                } finally {
                    connection.endMultiOperation();
                }
            } catch (Exception ex) {
                showExceptionDialog(_("Error displaying faxes:"), ex);
            }
        }
        
        @Override
        protected void done() {
            if (sMax >= 0 && updateReadState) {
                tableRecv.getSorter().fireTableRowsUpdated(sMin, sMax);
                actFaxRead.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, true);
            }
        }
        
        public ShowWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.updateReadState = (selTable == tableRecv);
            this.selJobs = selTable.getSelectedJobs();
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(true);
            
            if (updateReadState) {
                sMin = Integer.MAX_VALUE; sMax = Integer.MIN_VALUE;
                for (int i : selTable.getSelectedRows()) {
                    // Calculate the minimum and maximum selected row
                    if (i < sMin)
                        sMin = i;
                    if (i > sMax)
                        sMax = i;
                }
            }
        }
    }
    // Worker classes:
    private class SuspendWorker extends ProgressWorker {
        private FaxJob<? extends FmtItem>[] selJobs;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selJobs.length;
        }
        
        @Override
        public void doWork() {
            try {
                connection.beginMultiOperation();
                try {
                    MessageFormat infoFormat = new MessageFormat(_("Suspending job {0}"));
                    for (FaxJob<? extends FmtItem> yj : selJobs) {
                        try {
                            updateNote(infoFormat.format(new Object[]{yj.getIDValue()}));

                            JobState jobstate = yj.getJobState();
                            if (jobstate == JobState.RUNNING) {
                                if (showConfirmDialog(MessageFormat.format(_("Suspending the currently running job {0} may block until it is done (or switch to another \"non running state\"). Try to suspend it anyway?") , yj.getIDValue()),
                                        _("Suspend fax job"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                                    yj.suspend();
                                }
                            } else {
                                yj.suspend();
                            }

                            stepProgressBar(10);
                        } catch (Exception e1) {
                            String msgText;
                            if (yj == null)
                                msgText = _("Error suspending a fax job:\n");
                            else
                                msgText = MessageFormat.format(_("Error suspending the fax job \"{0}\":\n"), yj.getIDValue());
                            showExceptionDialog(msgText, e1);
                        }
                    }
                } finally {
                    connection.endMultiOperation();
                }
            } catch (Exception ex) {
                showExceptionDialog(_("Error suspending faxes:"), ex);
            }
        }
        @Override
        protected void done() {
            refreshTables();
        }
        
        public SuspendWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selJobs = selTable.getSelectedJobs();
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(false);
        }
    }
    // Worker classes:
    private class ResumeWorker extends ProgressWorker {
        private FaxJob<? extends FmtItem>[] selJobs;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selJobs.length;
        }
        
        @Override
        public void doWork() {
            try {
                connection.beginMultiOperation();
                try {
                    MessageFormat infoFormat = new MessageFormat(_("Resuming job {0}"));
                    
                    for (FaxJob<? extends FmtItem> yj : selJobs) {
                        try {
                            updateNote(infoFormat.format(new Object[] { yj.getIDValue() }));
                            JobState jobstate = yj.getJobState();
                            if (jobstate != JobState.SUSPENDED) {
                                if (showConfirmDialog(MessageFormat.format(_("Job {0} is not in state \"Suspended\" so resuming it probably will not work. Try to resume it anyway?") , yj.getIDValue()),
                                        _("Resume fax job"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                                    yj.resume();
                                }
                            } else {
                                yj.resume();
                            }

                            stepProgressBar(10);
                        } catch (Exception e1) {
                            String msgText;
                            if (yj == null)
                                msgText = _("Error resuming a fax job:\n");
                            else
                                msgText = MessageFormat.format(_("Error resuming the fax job \"{0}\":\n"), yj.getIDValue());
                            showExceptionDialog(msgText, e1);
                        }
                    }
                } finally {
                    connection.endMultiOperation();
                }
            } catch (Exception ex) {
                showExceptionDialog(_("Error resuming faxes:"), ex);
            }
        }
        @Override
        protected void done() {
            refreshTables();
        }
        
        public ResumeWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selJobs = selTable.getSelectedJobs();
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(false);
        }
    }
    private class SaveToFormatWorker extends ProgressWorker {
        private FaxJob<? extends FmtItem>[] selJobs;
        private File targetDir;
        private int fileCounter;
        private JFileChooser fileChooser;
        private boolean askForEveryFile;
        private final MultiFileConvFormat desiredFormat;
        
        @Override
        protected int calculateMaxProgress() {
            return 1000*selJobs.length;
        }
        
        @Override
        public void doWork() {
            try {
                fileCounter = 0;
                connection.beginMultiOperation();
                try {
                    List<FormattedFile> ffs = new ArrayList<FormattedFile>();
                    MessageFormat infoFormat = new MessageFormat(_("Saving fax {0}"));
                    for (FaxJob<? extends FmtItem> yj : selJobs) {
                        try {
                            updateNote(infoFormat.format(new Object[]{yj.getIDValue()}));
                            ffs.clear();
                            for(FaxDocument hsf : yj.getDocuments()) {
                                try {
                                    ffs.add(hsf.getDocument());
                                } catch (ServerResponseException sre) {
                                    showExceptionDialog(MessageFormat.format(_("While downloading the file {0} (job {1}), the server gave back an error code:"), hsf.getPath(), yj.getIDValue()), sre);
                                } catch (FileNotFoundException fnfe) { 
                                    showExceptionDialog(MessageFormat.format(_("The file {0} (job {1}) could not be opened for reading (probably you lack the necessary access permissions):"), hsf.getPath(), yj.getIDValue()), fnfe);
                                } catch (Exception e1) {
                                    showExceptionDialog(MessageFormat.format(_("An error occured saving the file {0} (job {1}):"), hsf.getPath(), yj.getIDValue()), e1);
                                }
                            }
                            if (ffs.size() > 0) {
                                Object idVal = yj.getIDValue();
                                String filePrefix;
                                if (idVal instanceof Integer) {
                                    filePrefix = "fax" + ((Integer)idVal).intValue();
                                } else {
                                    filePrefix = idVal.toString();
                                    int pos = filePrefix.lastIndexOf('.');
                                    if (pos >= 0)
                                        filePrefix = filePrefix.substring(0, pos);
                                }

                                File target = new File(targetDir, filePrefix + '.' + desiredFormat.getFileFormat().getDefaultExtension());
                                if (askForEveryFile) {
                                    FileChooserRunnable runner = new FileChooserRunnable(MainWin.this, fileChooser, MessageFormat.format(_("File name to save fax {0}"), yj.getIDValue()), null, target, false);
                                    SwingUtilities.invokeAndWait(runner);
                                    if (runner.getSelection() == null) {
                                        return;
                                    }
                                    target = runner.getSelection();
                                    targetDir = target.getParentFile();
                                }
                                MultiFileConverter.convertMultipleFilesToSingleFile(ffs, target, desiredFormat, currentServer.getOptions().paperSize);
                                fileCounter++;
                            }
                        } catch (Exception e1) {
                            //JOptionPane.showMessageDialog(MainWin.this, _("An error occured saving the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                            showExceptionDialog(_("An error occured saving the fax:"), e1);
                        }
                        stepProgressBar(1000);
                    }
                    if (targetDir != null)
                        Utils.getFaxOptions().lastSavePath = targetDir.getAbsolutePath();
                } finally {
                    connection.endMultiOperation();
                }
            } catch (Exception ex) {
                showExceptionDialog(_("Error saving faxes:"), ex);
            }
        }
        
        @Override
        protected void initialize() {
            if (askForEveryFile) {
                if (targetDir == null && Utils.getFaxOptions().lastSavePath.length() > 0) {
                    targetDir = new File(Utils.getFaxOptions().lastSavePath);
                }
                fileChooser = new SafeJFileChooser();
                fileChooser.resetChoosableFileFilters();
                FileFilter pdfFilter = new ExampleFileFilter(desiredFormat.getFileFormat().getPossibleExtensions(), desiredFormat.getFileFormat().getDescription());
                fileChooser.addChoosableFileFilter(pdfFilter);
                fileChooser.setFileFilter(pdfFilter);
            } 
        }
        
        @Override
        protected void pMonClosed() {
            if (!askForEveryFile) {
                JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("{0} files saved to directory {1}."), fileCounter, targetDir.getPath()), _("Faxes saved"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        public SaveToFormatWorker(TooltipJTable<? extends FmtItem> selTable, File targetDir, boolean askForEveryFile, MultiFileConvFormat desiredFormat) {
            this.selJobs = selTable.getSelectedJobs();
            this.targetDir = targetDir;
            this.progressMonitor = tablePanel;
            this.askForEveryFile = askForEveryFile;
            this.desiredFormat = desiredFormat;
            this.setCloseOnExit(true);
        }
    }
    private class SaveToFormatAction extends ExcDialogAbstractAction {
        private final MultiFileConvFormat desiredFormat;

        public void actualActionPerformed(ActionEvent e) {
            TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
            if (selTable.getSelectedRowCount() == 0) {
                return;
            }
            if ((Utils.searchExecutableInPath(myopts.ghostScriptLocation) == null) ||
                    (Utils.searchExecutableInPath(myopts.tiff2PDFLocation) == null)) {
                JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("Save to {0} needs GhostScript and tiff2pdf.\nPlease specify the location of these tools in the options dialog (see the FAQ for download locations)."), desiredFormat.name()), _("Error"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (selTable.getSelectedRowCount() > 1) {
                JFileChooser jfc = new yajhfc.util.SafeJFileChooser();
                jfc.setDialogTitle(_("Select a directory to save the faxes in"));
                jfc.setApproveButtonText(_("Select"));
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (Utils.getFaxOptions().lastSavePath.length() > 0) {
                    jfc.setSelectedFile(new File(Utils.getFaxOptions().lastSavePath));
                }
                if (jfc.showOpenDialog(MainWin.this) == JFileChooser.APPROVE_OPTION) {
                    SaveToFormatWorker wrk = new SaveToFormatWorker(selTable, jfc.getSelectedFile(), false, desiredFormat);
                    wrk.startWork(MainWin.this, _("Saving faxes"));
                    Utils.getFaxOptions().lastSavePath = jfc.getSelectedFile().getPath();
                }
            } else {
                SaveToFormatWorker wrk = new SaveToFormatWorker(selTable, null, true, desiredFormat);
                wrk.startWork(MainWin.this, _("Saving faxes"));
            }
        }

        public SaveToFormatAction(MultiFileConvFormat desiredFormat) {
            super();
            this.desiredFormat = desiredFormat;
            
            putValue(Action.NAME, MessageFormat.format(_("Save fax as {0}..."), desiredFormat));
            putValue(Action.SHORT_DESCRIPTION, MessageFormat.format(_("Saves the selected fax(es) as single {0} file"), desiredFormat));
            putValue(Action.SMALL_ICON, Utils.loadCustomIcon("saveAs" + desiredFormat.name() + ".png"));
        }
    }
    
    
    // Creates all actions:
    private void createActions(boolean adminState) {
        actOptions = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                Utils.setWaitCursor(null);
                
                SendReadyState oldState = sendReady;
                sendReady = SendReadyState.NeedToWait;
                                
                //PROFILE: long time = System.currentTimeMillis();
                OptionsWin ow = new OptionsWin(myopts, MainWin.this);
                //PROFILE: System.out.println("After OptionsWin constructor: " + (-time + (time = System.currentTimeMillis())));
                ow.setModal(true);
                Utils.unsetWaitCursorOnOpen(null, ow);
                ow.setVisible(true);
                if (ow.getModalResult()) {
                    optionsChanged();
                    Utils.storeOptionsToFile();
                } else {
                    sendReady = oldState;
                }
                    
            }
        };
        actOptions.putValue(Action.NAME, _("Options") + "...");
        actOptions.putValue(Action.SHORT_DESCRIPTION, _("Shows the Options dialog"));
        actOptions.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Preferences"));
        putAvailableAction("Options", actOptions);
        
        actSend = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                Utils.setWaitCursor(null);
                SendWinControl sw = SendController.createSendWindow(MainWin.this, currentServer, false, false);

                Utils.unsetWaitCursorOnOpen(null, sw.getWindow());
                sw.setVisible(true);
                if (sw.getModalResult()) {
                    refreshTables();
                }
            }
        };
        actSend.putValue(Action.NAME, _("Send") + "...");
        actSend.putValue(Action.SHORT_DESCRIPTION, _("Shows the send fax dialog"));
        actSend.putValue(Action.SMALL_ICON, Utils.loadIcon("general/SendMail"));
        putAvailableAction("Send", actSend);
        
        actPoll = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                Utils.setWaitCursor(null);
                SendWinControl sw = SendController.createSendWindow(MainWin.this, currentServer, true, true);
                Utils.unsetWaitCursorOnOpen(null, sw.getWindow());
                sw.setVisible(true);
            }
        };
        actPoll.putValue(Action.NAME, _("Poll") + "...");
        actPoll.putValue(Action.SHORT_DESCRIPTION, _("Shows the poll fax dialog"));
        actPoll.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Import"));
        putAvailableAction("Poll", actPoll);
        
        actDelete = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                
                String msgText;
                
                if (selTable == tableSending)
                    msgText = _("Do you really want to cancel the selected fax jobs?");
                else
                    msgText = _("Do you really want to delete the selected faxes?");
                
                if (JOptionPane.showConfirmDialog(MainWin.this, msgText, _("Delete faxes"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    DeleteWorker wrk = new DeleteWorker(selTable);
                    wrk.startWork(MainWin.this, _("Deleting faxes"));
                }
            };
            
        };
        actDelete.putValue(Action.NAME, _("Delete"));
        actDelete.putValue(Action.SHORT_DESCRIPTION, _("Deletes the selected fax"));
        actDelete.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Delete"));
        putAvailableAction("Delete", actDelete);
        
        actShow = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                
                ShowWorker wrk = new ShowWorker(selTable);
                wrk.startWork(MainWin.this, _("Viewing faxes"));
            }
        };
        actShow.putValue(Action.NAME, _("Show") + "...");
        actShow.putValue(Action.SHORT_DESCRIPTION, _("Displays the selected fax"));
        actShow.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Zoom"));
        putAvailableAction("Show", actShow);
        
        actExit = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                dispose();
                //System.exit(0);
            }
        };
        actExit.putValue(Action.NAME, _("Exit"));
        actExit.putValue(Action.SHORT_DESCRIPTION, _("Exits the application"));
        actExit.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Stop"));
        putAvailableAction("Exit", actExit);
        
        actAbout = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(null);
                //JOptionPane.showMessageDialog(aboutMenuItem.getComponent(), Utils.AppName + "\n\n" + _("by Jonas Wolz"), _("About"), JOptionPane.INFORMATION_MESSAGE);
                AboutDialog aDlg = new AboutDialog(MainWin.this);
                aDlg.setMode(AboutDialog.Mode.ABOUT);
                Utils.unsetWaitCursorOnOpen(null, aDlg);
                aDlg.setVisible(true);
            }
        };
        actAbout.putValue(Action.NAME, _("About") +  "...");
        actAbout.putValue(Action.SHORT_DESCRIPTION, _("Shows the about dialog"));
        actAbout.putValue(Action.SMALL_ICON, Utils.loadIcon("general/About"));
        putAvailableAction("About", actAbout);
        
        actPhonebook = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(null);
                NewPhoneBookWin pbw = new NewPhoneBookWin(MainWin.this);
                pbw.setModal(true);
                Utils.unsetWaitCursorOnOpen(null, pbw);
                pbw.setVisible(true);
            }
        };
        actPhonebook.putValue(Action.NAME, _("Phone book") +  "...");
        actPhonebook.putValue(Action.SHORT_DESCRIPTION, _("Display/edit the phone book"));
        actPhonebook.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Bookmarks"));
        putAvailableAction("Phonebook", actPhonebook);
        
        actReadme = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(null);
                AboutDialog aDlg = new AboutDialog(MainWin.this);
                aDlg.setMode(AboutDialog.Mode.READMES);
                Utils.unsetWaitCursorOnOpen(null, aDlg);
                aDlg.setVisible(true);
            }
        };
        actReadme.putValue(Action.NAME, _("Documentation") +  "...");
        actReadme.putValue(Action.SHORT_DESCRIPTION, _("Shows the README files"));
        actReadme.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Help"));
        putAvailableAction("Readme", actReadme);
        
        actFaxRead = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = true;
                else
                    newState = !state;
                
                if (tabMain.getSelectedComponent() == scrollRecv) { // TableRecv
                    int sMin = Integer.MAX_VALUE, sMax = Integer.MIN_VALUE;
                    for (int i:tableRecv.getSelectedRows()) {
                        tableRecv.getJobForRow(i).setRead(newState);
                        if (i < sMin)
                            sMin = i;
                        if (i > sMax)
                            sMax = i;
                    }
                    if (sMax >= 0)
                        tableRecv.getSorter().fireTableRowsUpdated(sMin, sMax);
                    putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, newState);
                }
            };
        };
        actFaxRead.putValue(Action.NAME, _("Marked as read"));
        actFaxRead.putValue(Action.SHORT_DESCRIPTION, _("Marks the selected fax as read/unread"));
        actFaxRead.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, true);
        putAvailableAction("FaxRead", actFaxRead);
        
        actFaxSave = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();                
                
                if (selTable.getSelectedRowCount() == 0) {
                    return;
                }
                  
                if (selTable.getSelectedRowCount() > 1) {
                    JFileChooser jfc = new yajhfc.util.SafeJFileChooser();
                    jfc.setDialogTitle(_("Select a directory to save the faxes in"));
                    jfc.setApproveButtonText(_("Select"));
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (Utils.getFaxOptions().lastSavePath.length() > 0) {
                        jfc.setSelectedFile(new File(Utils.getFaxOptions().lastSavePath));
                    }
                    if (jfc.showOpenDialog(MainWin.this) == JFileChooser.APPROVE_OPTION) {
                        MultiSaveWorker wrk = new MultiSaveWorker(selTable, jfc.getSelectedFile(), false);
                        wrk.startWork(MainWin.this, _("Saving faxes"));
                        Utils.getFaxOptions().lastSavePath = jfc.getSelectedFile().getPath();
                    }
                } else {
                    MultiSaveWorker wrk = new MultiSaveWorker(selTable, null, true);
                    wrk.startWork(MainWin.this, _("Saving faxes"));
                }
            };
        };
        actFaxSave.putValue(Action.NAME, _("Save fax..."));
        actFaxSave.putValue(Action.SHORT_DESCRIPTION, _("Saves the selected fax on disk"));
        actFaxSave.putValue(Action.SMALL_ICON, Utils.loadIcon("general/SaveAs"));
        putAvailableAction("FaxSave", actFaxSave);
        
        actForward = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (tabMain.getSelectedComponent() != scrollRecv || tableRecv.getSelectedRow() < 0)
                    return;
                
                List<FaxDocument> files = new ArrayList<FaxDocument>();
                try {
                    for (int row : tableRecv.getSelectedRows()) {
                        files.addAll(tableRecv.getJobForRow(row).getDocuments());
                    }
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(MainWin.this, _("Couldn't get a filename for the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    ExceptionDialog.showExceptionDialog(MainWin.this, _("Couldn't get a filename for the fax:"), e1);
                    return;
                }
                
                SendWinControl sw = SendController.createSendWindow(MainWin.this, currentServer, false, true);
                for (FaxDocument doc : files) {
                    sw.addServerFile(doc);
                }
                sw.setVisible(true);
                refreshTables();
                
            }
        };
        actForward.putValue(Action.NAME, _("Forward fax..."));
        actForward.putValue(Action.SHORT_DESCRIPTION, _("Forwards the fax"));
        actForward.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Redo"));
        putAvailableAction("Forward", actForward);
        
        actAdminMode = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(null);
                Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = false;
                else
                    newState = !state;

                putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, newState);
                
                doLogout(true);
                Utils.unsetWaitCursor(null);
            };
        };
        actAdminMode.putValue(Action.NAME, _("Admin mode"));
        actAdminMode.putValue(Action.SHORT_DESCRIPTION, _("Connect to the server in admin mode (e.g. to delete faxes)"));
        actAdminMode.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, adminState);
        putAvailableAction("AdminMode", actAdminMode);
        
        actRefresh = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(null);
                refreshStatus();
                refreshTables();
                Utils.unsetWaitCursor(null);
            };
        };
        actRefresh.putValue(Action.NAME, _("Refresh"));
        actRefresh.putValue(Action.SHORT_DESCRIPTION, _("Refresh tables and server status"));
        actRefresh.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Refresh"));
        actRefresh.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        putAvailableAction("Refresh", actRefresh);

        actResend = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {                
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                if (selTable.getSelectedRow() < 0)
                    return;

                Utils.setWaitCursor(null);
                
                final FaxJob<? extends FmtItem>[] selectedJobs = selTable.getSelectedJobs(); 
                
                ProgressWorker resendWorker = new ProgressWorker() {
                    Set<FaxDocument> files = new HashSet<FaxDocument>();
                    String subject = null;
                    List<PBEntryFieldContainer> recipients = new ArrayList<PBEntryFieldContainer>();
                    boolean success = false;
                    
                    @Override
                    public void doWork() {
                        try {
                            MessageFormat infoFormat = new MessageFormat(_("Getting information for job {0}..."));
                            connection.beginMultiOperation();
                            try {
                                for (FaxJob<? extends FmtItem> job : selectedJobs) {
                                    String number, voiceNumber, company, name, location;
                                    Collection<FaxDocument> jobFiles;

                                    updateNote(infoFormat.format(new Object[] {job.getIDValue()}));
                                    
                                    jobFiles = job.getDocuments();
                                    
                                    Map<String,String> props = job.getJobProperties("EXTERNAL", "DIALSTRING", "TOUSER", "TOCOMPANY", "TOLOCATION", "TOVOICE", "REGARDING");
                                    if (props != null && props.size() > 0) {
                                        number = props.get("DIALSTRING");
                                        if (number == null) {
                                            number = props.get("EXTERNAL");
                                        }
                                        name = props.get("TOUSER");
                                        company = props.get("TOCOMPANY");
                                        location = props.get("TOLOCATION");
                                        voiceNumber = props.get("TOVOICE");
                                        if (subject == null || subject.length() == 0) {
                                            // Simply take the first non-empty subject
                                            subject = props.get("REGARDING").trim();
                                        }
                                        recipients.add(new DefaultPBEntryFieldContainer(number, name, company, location, voiceNumber));
                                    }
                                    for (FaxDocument hysf : jobFiles) {
                                            files.add(hysf);
                                    }
                                }
                                updateNote(_("Opening send dialog..."));
                                success = true;
                            } catch (Exception e1) {
                                Utils.unsetWaitCursor(null);
                                ExceptionDialog.showExceptionDialog(MainWin.this, _("Could not get all of the job information necessary to resend the fax:"), e1);
                                return;
                            } finally {
                                connection.endMultiOperation();
                            }
                        } catch (Exception ex) {
                            ExceptionDialog.showExceptionDialog(MainWin.this, _("Error resending faxes:"), ex);
                        }
                    }
                    
                    @Override
                    protected void done() {
                        if (success) {
                            SendWinControl sw = SendController.createSendWindow(MainWin.this, currentServer, false, true);

                            sw.getRecipients().addAll(recipients);
                            for (FaxDocument doc : files) {
                                sw.addServerFile(doc);
                            }

                            if (subject != null)
                                sw.setSubject(subject);

                            Utils.unsetWaitCursorOnOpen(null, sw.getWindow());
                            sw.setVisible(true);
                        } else {
                            Utils.unsetWaitCursor(null);
                        }
                    }
                };
                resendWorker.setProgressMonitor(tablePanel);
                resendWorker.setCloseOnExit(true);
                
                resendWorker.startWork(MainWin.this, _("Resending fax..."));
            }
        };
        actResend.putValue(Action.NAME, _("Resend fax..."));
        actResend.putValue(Action.SHORT_DESCRIPTION, _("Resend the fax"));
        actResend.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Export"));
        putAvailableAction("Resend", actResend);
        
        actPrintTable = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                FaxTablePrinter.printFaxTable(MainWin.this, selTable, tabMain.getToolTipTextAt(tabMain.getSelectedIndex()));
            };
        };
        actPrintTable.putValue(Action.NAME, _("Print") + "...");
        actPrintTable.putValue(Action.SHORT_DESCRIPTION, _("Prints the currently displayed table"));
        actPrintTable.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Print"));
        putAvailableAction("PrintTable", actPrintTable);
        
        actSuspend = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                SuspendWorker wrk = new SuspendWorker(selTable);
                wrk.startWork(MainWin.this, _("Suspending jobs"));
            }
        };
        actSuspend.putValue(Action.NAME, _("Suspend"));
        actSuspend.putValue(Action.SHORT_DESCRIPTION, _("Suspends the transfer of the selected fax"));
        actSuspend.putValue(Action.SMALL_ICON, Utils.loadIcon("media/Pause"));
        putAvailableAction("Suspend", actSuspend);
        
        actResume = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                ResumeWorker wrk = new ResumeWorker(selTable);
                wrk.startWork(MainWin.this, _("Resuming jobs"));
            }
        };
        actResume.putValue(Action.NAME, _("Resume"));
        actResume.putValue(Action.SHORT_DESCRIPTION, _("Resumes the transfer of the selected fax"));
        actResume.putValue(Action.SMALL_ICON, Utils.loadIcon("media/Play"));
        putAvailableAction("Resume", actResume);
        
        actClipCopy = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                selTable.getTransferHandler().exportToClipboard(selTable, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.COPY);
            }
        };
        actClipCopy.putValue(Action.NAME, _("Copy"));
        actClipCopy.putValue(Action.SHORT_DESCRIPTION, _("Copies the selected table items to the clipboard"));
        actClipCopy.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Copy"));
        putAvailableAction("ClipCopy", actClipCopy);
        
        actShowRowNumbers = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = false;
                else
                    newState = !state; 

                recvRowNumbers.setVisible(newState);
                sentRowNumbers.setVisible(newState);
                sendingRowNumbers.setVisible(newState);
                
                putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, newState);
            };
        };
        actShowRowNumbers.putValue(Action.NAME, _("Show row numbers"));
        actShowRowNumbers.putValue(Action.SHORT_DESCRIPTION, _("Show row numbers"));
        actShowRowNumbers.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, myopts.showRowNumbers);
        putAvailableAction("ShowRowNumbers", actShowRowNumbers);
        
        actAdjustColumns = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = false;
                else
                    newState = !state; 
                
                int newMode = newState ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS : JTable.AUTO_RESIZE_OFF;
                tableRecv.setAutoResizeMode(newMode);
                tableSent.setAutoResizeMode(newMode);
                tableSending.setAutoResizeMode(newMode);
                // Uncomment for archive support.
                if (tableArchive != null)
                    tableArchive.setAutoResizeMode(newMode);
                
                putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, newState);
            };
        };
        actAdjustColumns.putValue(Action.NAME, _("Adjust column widths"));
        actAdjustColumns.putValue(Action.SHORT_DESCRIPTION, _("Adjust column widths to fit the window size"));
        actAdjustColumns.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, myopts.adjustColumnWidths);
        putAvailableAction("AdjustColumns", actAdjustColumns);
        
        actReconnect = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(null);
                if (connection.getConnectionState() != ConnectionState.DISCONNECTED) {
                    doLogout(false);
                } else {
                    reconnectToServer(null);
                }
                Utils.unsetWaitCursor(null);
            }
        };
        actReconnect.putValue(Action.SHORT_DESCRIPTION, _("Connect or disconnect to the HylaFAX server"));
        putAvailableAction("Reconnect", actReconnect);
        setActReconnectState(true);
        
        actEditToolbar = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                ToolbarEditorDialog ted = new ToolbarEditorDialog(MainWin.this, availableActions, toolbar, FaxOptions.DEF_TOOLBAR_CONFIG);
                ted.setVisible(true);
            }
        };
        actEditToolbar.putValue(Action.NAME, _("Customize toolbar") + "...");
        actEditToolbar.putValue(Action.SHORT_DESCRIPTION, _("Customize the toolbar"));
        //actEditToolbar.putValue(Action.SMALL_ICON, Utils.loadIcon("media/Play"));
        putAvailableAction("EditToolbar", actEditToolbar);
        
        actUpdateCheck = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                UpdateChecker.doGUIUpdateCheck(MainWin.this, MainWin.this.tablePanel);
            }
        };
        actUpdateCheck.putValue(Action.NAME, _("Check for update") + "...");
        actUpdateCheck.putValue(Action.SHORT_DESCRIPTION, _("Checks if there is a newer version of YajHFC available"));
        //actEditToolbar.putValue(Action.SMALL_ICON, Utils.loadIcon("media/Play"));
        putAvailableAction("UpdateCheck", actUpdateCheck);
        
        actAnswerCall = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                HylaClientManager clientManager = connection.getClientManager();
                if (clientManager == null) {
                    JOptionPane.showMessageDialog(MainWin.this, _("Answering a phone call requires a direct connection to the HylaFAX server which is not available."), _("Answer call"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if (clientManager.isAdminMode()) {
                    List<HylaModem> modems = new ArrayList<HylaModem>();
                    for (HylaModem modem : clientManager.getRealModems()) {
                        if (!modem.getInternalName().equals("any")) {
                            modems.add(modem);
                        }
                    }
                    
                    Object modem;
                    if (modems.size() == 0) {
                        JOptionPane.showMessageDialog(MainWin.this, _("No valid modem found."), _("Answer call"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    } /*else if (modems.size() == 1) {
                        modem = modems.get(0);
                    } */else {
                        modem = JOptionPane.showInputDialog(MainWin.this, _("Please select which modem shall answer a phone call:"), _("Answer call"), JOptionPane.QUESTION_MESSAGE, null, modems.toArray(), modems.get(0));
                    }
                    if (modem == null)
                        return;
                    
                    HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                    if (hyfc == null)
                        return;
                    
                    try {
                        hyfc.answer(((HylaModem)modem).getInternalName() + " fax");
                    } catch (Exception e1) {
                        ExceptionDialog.showExceptionDialog(MainWin.this, _("Error answering the phone call:"), e1);
                    } finally {
                        clientManager.endServerTransaction();
                    }
                } else {
                    JOptionPane.showMessageDialog(MainWin.this, _("Answering a phone call requires administrative privileges.\nPlease enable admin mode first."), _("Answer call"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        actAnswerCall.putValue(Action.NAME, _("Answer call") + "...");
        actAnswerCall.putValue(Action.SHORT_DESCRIPTION, _("Manually answer a phone call with a modem"));
        //actEditToolbar.putValue(Action.SMALL_ICON, Utils.loadIcon("media/Play"));
        putAvailableAction("AnswerCall", actAnswerCall);
        
        actSearchFax = new ExcDialogAbstractAction() {
            private MainWinSearchWin searchWin = null;
            
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                if (searchWin == null) {
                    searchWin = new MainWinSearchWin(MainWin.this);
                }
                searchWin.setVisible(true);
            }
        };
        actSearchFax.putValue(Action.NAME, _("Search fax") + "...");
        actSearchFax.putValue(Action.SHORT_DESCRIPTION, _("Searches for a fax"));
        actSearchFax.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Find"));
        putAvailableAction("SearchFax", actSearchFax);
        
        actViewLog = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                Utils.setWaitCursor(null);
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                
                if (selTable.getSelectedRowCount() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                
                TableType tt = selTable.getRealModel().getTableType();
                switch (tt) {
                case SENT:
                case SENDING:
                case ARCHIVE:
                    // Supported
                    break;
                default:
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                
                // Collect selected rows:
                List<FaxJob<? extends FmtItem>> jobs = new ArrayList<FaxJob<? extends FmtItem>>(selTable.getSelectedRowCount());
                for (int idx : selTable.getSelectedRows()) {
                    jobs.add(selTable.getJobForRow(idx));
                }
                
                LogViewWorker worker = new LogViewWorker(connection, jobs, tablePanel);
                worker.setCloseOnExit(true);
                worker.startWork(MainWin.this, _("Viewing logs"));
                Utils.unsetWaitCursor(null);
            }
        };
        actViewLog.putValue(Action.NAME, _("View log") + "...");
        actViewLog.putValue(Action.SHORT_DESCRIPTION, _("Displays the communication log of the selected fax"));
        actViewLog.putValue(Action.SMALL_ICON, Utils.loadIcon("general/History"));
        putAvailableAction("ViewLog", actViewLog);
        
        actLogConsole = new ExcDialogAbstractAction() {
            LogConsole logCons;
            
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                if (logCons == null) {
                    Utils.setWaitCursor(null);
                    logCons = new LogConsole();
                    logCons.addWindowListener(new WindowAdapter() {
                        public void windowClosed(WindowEvent e) {
                            logCons = null;
                        };
                    });
                    Utils.unsetWaitCursorOnOpen(null, logCons);
                    logCons.setVisible(true);
                } else {
                    logCons.toFront();
                }
            }
        };
        actLogConsole.putValue(Action.NAME, _("Log console") + "...");
        actLogConsole.putValue(Action.SHORT_DESCRIPTION, _("Displays the YajHFC log in real time"));
        putAvailableAction("LogConsole", actLogConsole);
        
        actShowToolbar = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = false;
                else
                    newState = !state; 
                
                toolbar.setVisible(newState);
                myopts.showToolbar = newState;
                
                putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, newState);
            };
        };
        actShowToolbar.putValue(Action.NAME, _("Show Toolbar"));
        actShowToolbar.putValue(Action.SHORT_DESCRIPTION, _("Shows or hides the toolbar."));
        actShowToolbar.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, myopts.showToolbar);
        putAvailableAction("ShowToolbar", actShowToolbar);
        
        actShowQuickSearchBar = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = false;
                else
                    newState = !state; 
                
                quickSearchbar.setVisible(newState);
                myopts.showQuickSearchbar = newState;
                
                putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, newState);
            };
        };
        actShowQuickSearchBar.putValue(Action.NAME, _("Show Quick Search bar"));
        actShowQuickSearchBar.putValue(Action.SHORT_DESCRIPTION, _("Show or hides the Quick Search toolbar."));
        actShowQuickSearchBar.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, myopts.showQuickSearchbar);
        putAvailableAction("ShowQuickSearchBar",actShowQuickSearchBar);
        
        actAutoSizeStatus = new StatusBarResizeAction();
        actAutoSizeStatus.putValue(Action.NAME, _("Auto-size status bar"));
        actAutoSizeStatus.putValue(Action.SHORT_DESCRIPTION, _("Automatically resize the status bar"));
        putAvailableAction("AutoSizeStatus", actAutoSizeStatus);
        
        actSaveAsPDF = new SaveToFormatAction(MultiFileConvFormat.PDF);
        putAvailableAction("SaveAsPDF", actSaveAsPDF);
        
        actSaveAsTIFF = new SaveToFormatAction(MultiFileConvFormat.TIFF);
        putAvailableAction("SaveAsTIFF", actSaveAsTIFF);
        
        actExport = new ExportAction(this);
        putAvailableAction("ExportTable", actExport);
        
        
        actChecker = new ActionEnabler();
    }
    
    private void putAvailableAction(String key, Action act) {
        if (availableActions.put(key, act) != null) {
            log.severe("Action " + key + " already existed!");
        }
        act.putValue(Action.ACTION_COMMAND_KEY, key);
    }
    
    /**
     * Returns the currently selected (i.e. visible) table
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TooltipJTable<? extends FmtItem> getSelectedTable() {
        return (TooltipJTable)((JScrollPane)tabMain.getSelectedComponent()).getViewport().getView();
    }
    
    /**
     * Returns the table having the specified index
     * @param index
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TooltipJTable<? extends FmtItem> getTableByIndex(int index) {
        return (TooltipJTable)((JScrollPane)tabMain.getComponent(index)).getViewport().getView();
    }
    
    /**
     * Returns the long description of the currently selected tab
     * @return
     */
    public String getSelectedTableDescription() {
    	return tabMain.getToolTipTextAt(tabMain.getSelectedIndex());
    }
    
    void setActReconnectState(boolean showConnect) {
        actReconnect.putValue(Action.NAME, showConnect ? _("Connect") : _("Disconnect"));
        actReconnect.putValue(Action.SMALL_ICON, Utils.loadCustomIcon(showConnect ? "disconnected.png" : "connected.png"));
    }
    
    
    JPopupMenu getTblPopup() {
        if (tblPopup == null) {
            tblPopup = new JPopupMenu(_("Fax"));
            tblPopup.add(new JMenuItem(actShow));
            tblPopup.add(new JMenuItem(actFaxSave));
            tblPopup.add(new JMenuItem(actSaveAsPDF));
            tblPopup.add(new JMenuItem(actSaveAsTIFF));
            tblPopup.add(new JMenuItem(actViewLog));
            tblPopup.addSeparator();
            tblPopup.add(new JMenuItem(actClipCopy));
            tblPopup.addSeparator();
            tblPopup.add(new JMenuItem(actForward));
            tblPopup.add(new JMenuItem(actResend));
            tblPopup.addSeparator();
            tblPopup.add(new JMenuItem(actDelete));
            tblPopup.addSeparator();
            tblPopup.add(new ActionJCheckBoxMenuItem(actFaxRead));
        }
        return tblPopup;
    }
    
    public void refreshTables() {
        tablePanel.showIndeterminateProgress(_("Fetching fax list..."));
        
        connection.addFaxListConnectionListener(new RefreshCompleteHider(tablePanel, connection));
        //tableRefresher.hideProgress = true;
        Utils.executorService.submit(new Runnable() {
            public void run() {
                connection.refreshFaxLists();
             } 
         });
    }
    
    public void refreshStatus() {
        Utils.executorService.submit(new Runnable() {
           public void run() {
               connection.refreshStatus();
            } 
        });
    }
    
    public SendReadyState getSendReadyState() {
        return sendReady;
    }
    
    public void setSelectedTab(int index)
    {
        if (index >= 0 && index < getTabMain().getTabCount()) {
            getTabMain().setSelectedIndex(index);
        }
    }
    
    private MouseListener getTblMouseListener() {
        if (tblMouseListener == null) {
            tblMouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                        JTable src = (JTable)e.getComponent();
                        int row = src.rowAtPoint(e.getPoint());
                        if ((row >= 0) && (src.getSelectedRow() == row))
                            actShow.actionPerformed(null);
                    }
                }
                
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }
                
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }
                
                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        JTable src = (JTable)e.getComponent();
                        int row = src.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            if (!src.isRowSelected(row)) {
                                src.setRowSelectionInterval(row, row);
                            }
                            getTblPopup().show(src, e.getX(), e.getY());
                        }
                    }
                }
                
                
            };
        }
        return tblMouseListener;
    }
    
//    private KeyListener getTblKeyListener() {
//        if (tblKeyListener == null) {
//            tblKeyListener = new KeyAdapter() {
//                public void keyPressed(java.awt.event.KeyEvent e) {
//                    switch (e.getKeyCode()) {
//                    case KeyEvent.VK_ENTER:
//                        actShow.actionPerformed(null);
//                        break;
//                    case KeyEvent.VK_DELETE:
//                        actDelete.actionPerformed(null);
//                        break;
//                    }
//                };
//            };            
//        }
//        return tblKeyListener;
//    }
//    
    private DefaultTableCellRenderer getHylaDateRenderer() {
        if (hylaDateRenderer == null) {
            hylaDateRenderer = new DefaultTableCellRenderer() {
                private final StringBuffer formatBuffer = new StringBuffer();
                private final FieldPosition dummyPos = new FieldPosition(0);
                
                @SuppressWarnings("unchecked")
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value != null) {
                        int realCol = table.getColumnModel().getColumn(column).getModelIndex();
                        FaxListTableModel<? extends FmtItem> model = ((TooltipJTable<? extends FmtItem>)table).getRealModel();
                        formatBuffer.setLength(0);
                        value = model.getColumns().get(realCol).getDisplayDateFormat().format(value, formatBuffer, dummyPos).toString();
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                            row, column);
                }
            };
        }
        return hylaDateRenderer;
    }
    
    private JToolBar getToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setVisible(myopts.showToolbar);
            
            ToolbarEditorDialog.loadConfigFromString(toolbar, myopts.toolbarConfig, availableActions);
//            toolbar.add(actSend);
//            toolbar.addSeparator();
//            toolbar.add(actShow);
//            toolbar.add(actDelete);
//            toolbar.addSeparator();
//            toolbar.add(actRefresh);
//            toolbar.addSeparator();
//            toolbar.add(actPhonebook);
//            toolbar.addSeparator();
//            toolbar.add(actResume);
//            toolbar.add(actSuspend);
        }
        return toolbar;
    }
    
    private JMenu getMenuView() {
        if (menuView == null) {
            menuView = new JMenu(_("View"));
            
            menuViewListener = new MenuViewListener();
            
            menuViewAll = new JRadioButtonMenuItem(_("All faxes"));
            menuViewAll.setActionCommand("view_all");
            menuViewAll.setSelected(true);
            menuViewAll.addActionListener(menuViewListener);
            menuViewAll.setEnabled(myopts.allowChangeFilter);
            
            menuViewOwn = new JRadioButtonMenuItem(_("Only own faxes"));
            menuViewOwn.setActionCommand("view_own");
            menuViewOwn.addActionListener(menuViewListener);
            menuViewOwn.setEnabled(myopts.allowChangeFilter);
            
            menuViewCustom = new JRadioButtonMenuItem(_("Custom filter..."));
            menuViewCustom.setActionCommand("view_custom");
            menuViewCustom.addActionListener(menuViewListener);
            menuViewCustom.setEnabled(myopts.allowChangeFilter);
            
            menuMarkError = new JCheckBoxMenuItem(_("Mark failed jobs"));
            menuMarkError.setActionCommand("mark_failed");
            menuMarkError.addActionListener(menuViewListener);
            menuMarkError.setSelected(myopts.markFailedJobs);
            
            viewGroup = new ButtonGroup();
            viewGroup.add(menuViewAll);
            viewGroup.add(menuViewOwn);
            viewGroup.add(menuViewCustom);
         
            menuView.add(menuViewAll);
            menuView.add(menuViewOwn);
            menuView.add(menuViewCustom);
            menuView.addSeparator();
            menuView.add(menuMarkError);
            menuView.add(new ActionJCheckBoxMenuItem(actShowRowNumbers));
            menuView.add(new ActionJCheckBoxMenuItem(actAdjustColumns));
            menuView.addSeparator();
            menuView.add(new ActionJCheckBoxMenuItem(actAutoSizeStatus));
            menuView.add(new ActionJCheckBoxMenuItem(actShowToolbar));
            menuView.add(new ActionJCheckBoxMenuItem(actShowQuickSearchBar));
            menuView.addSeparator();
            menuView.add(new JMenuItem(actRefresh));
            
            getTabMain().addChangeListener(menuViewListener);
        }
        return menuView;
    }
    
    /**
     * This is the default constructor
     */
    public MainWin() {
        super();
        //initialize(adminState);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    public void initialize(boolean adminState, String serverToUse) {
        myopts = Utils.getFaxOptions();
        
        createActions(adminState);
        
        if (serverToUse != null) {
            ServerOptions sOpt = IDAndNameOptions.getItemFromCommandLineCoding(myopts.servers, serverToUse);
            if (sOpt != null) {
                setCurrentServerByID(sOpt.id);
            } else {
                setCurrentServerByID(myopts.lastServerID);
            }
        } else {
            setCurrentServerByID(myopts.lastServerID);
        }
        setFaxListConnectionFromServer();
        
        initializePlatformSpecifics();
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setJMenuBar(getJJMenuBar());
        this.setSize(644, 466);
        this.setContentPane(getJContentPane());
        this.setTitle(Utils.AppName);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            //private boolean saved = false;
            
            public void windowClosed(java.awt.event.WindowEvent e) {
                sendReady = SendReadyState.NotReady;
                
                saveWindowSettings();
                
                if (connection.getConnectionState() == ConnectionState.CONNECTED) {
                    doLogout(false, new Runnable() {
                        public void run() {
                            Thread.yield();
                            System.exit(0);
                        } 
                    });
                } else {
                    Thread.yield();
                    System.exit(0);
                }
            }
        });
        setIconImage(Toolkit.getDefaultToolkit().getImage(MainWin.class.getResource("icon.png")));
        
        reloadTableColumnSettings();
        menuViewListener.loadFromOptions(myopts);
        
        if (myopts.mainWinBounds != null)
            this.setBounds(myopts.mainWinBounds);
        else
            //this.setLocationByPlatform(true);
            Utils.setDefWinPos(this);
        
        tabMain.setSelectedIndex(myopts.mainwinLastTab);
        actChecker.doEnableCheck();
        
        showOrHideTrayIcon();
        setDisconnectedUI();
        if (myopts.automaticallyCheckForUpdate) {
            UpdateChecker.startSilentUpdateCheck();
        }
        
    }
    
    public void saveWindowSettings() {
        saveTableColumnSettings();
        myopts.lastServerID = currentServer.getID();
        
        menuViewListener.saveToOptions(myopts);
        myopts.mainwinLastTab = getTabMain().getSelectedIndex();
        myopts.mainWinBounds = getBounds();
        
        Boolean selVal = (Boolean)actShowRowNumbers.getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
        myopts.showRowNumbers = (selVal != null && selVal.booleanValue());
        selVal = (Boolean)actAdjustColumns.getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
        myopts.adjustColumnWidths = (selVal != null && selVal.booleanValue());
        myopts.toolbarConfig = ToolbarEditorDialog.saveConfigToString(toolbar);
        
        if (actAutoSizeStatus.isSelected()) {
            myopts.statusBarSize = -1;
        } else {
            myopts.statusBarSize = statusSplitter.getHeight() - statusSplitter.getDividerLocation();
        }
    }
    
    void initializePlatformSpecifics() {
    	if (Utils.IS_MACOSX) {
    		MacOSXSupport macSup = MacOSXSupport.getInstance();
    		if (macSup != null) {
    			macSup.setApplicationMenuActions(this, actOptions, actAbout, actExit);
    			hideMenusForMac = myopts.adjustMenusForMacOSX;
    			macSup.setDockIconImage(Toolkit.getDefaultToolkit().getImage(MainWin.class.getResource("logo-large.png")));
    		}
    		getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
    	}
    }
    
    void showOrHideTrayIcon() {
        if (myopts.showTrayIcon) {
            if (trayIcon == null && TrayFactory.trayIsAvailable()) {
                trayIcon = new YajHFCTrayIcon(this, recvTableModel, actSend, actReconnect, null, actExit, null, actAbout);
            }
            if (trayIcon != null) {
                setDefaultCloseOperation(myopts.minimizeToTrayOnMainWinClose ? JFrame.HIDE_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
                trayIcon.setMinimizeToTray(myopts.minimizeToTray);
            }
        } else {
            if (trayIcon != null) {
                trayIcon.dispose();
                trayIcon = null;
            }
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }
    
    public boolean hasTrayIcon() {
        return (trayIcon != null);
    }
    
    
    private void saveTableColumnSettings() {
        if (connection.getConnectionState() == ConnectionState.CONNECTED) { 
            myopts.recvColState = getTableRecv().getColumnCfgString();
            myopts.sentColState = getTableSent().getColumnCfgString();
            myopts.sendingColState = getTableSending().getColumnCfgString();
            if (tableArchive != null)
                myopts.archiveColState = tableArchive.getColumnCfgString();
        }
    }
    
    /**
     * Disconnects from the server asynchronously. If immediateReconnect is true,
     * calls reconnectToServer after the disconnect has completed.
     */
    void doLogout(final boolean immediateReconnect) {
        doLogout(immediateReconnect, null);
    }
    /**
     * Disconnects from the server asnynchronously. If immediateReconnect is true,
     * calls reconnectToServer after the disconnect has completed.
     * If intermediateAction != null, the Runnable is run in the EDT after the disconnect completed,
     * (but before the reconnect is started when requested).
     */
    void doLogout(final boolean immediateReconnect, final Runnable intermediateAction) {
        try {
            log.fine("Logging out...");
            stopReconnectTimer();
            
            sendReady = immediateReconnect ? SendReadyState.NeedToWait : SendReadyState.NotReady;
            
            if (Utils.debugMode)
                log.fine("Logout ConnectionState is: " + connection.getConnectionState());
            if (connection.getConnectionState() == ConnectionState.CONNECTED) {                
                saveTableColumnSettings();
                
                recvTableModel.cleanupReadState();
                
                tablePanel.showIndeterminateProgress(_("Logging out..."));
                
                userInitiatedLogout = true;
                Utils.executorService.submit(new Runnable() {
                   public void run() {
                       connection.disconnect();
                       SwingUtilities.invokeLater(new Runnable() {
                           public void run() {
                               if (intermediateAction != null)
                                   intermediateAction.run();
                               if (immediateReconnect) {
                                   reconnectToServer(null);
                               } else {
                                   tablePanel.hideProgress();
                               }
                           }
                       });
                    } 
                });
            } else {
                setDisconnectedUI();
                if (immediateReconnect) {
                    reconnectToServer(null);
                }
            }
            
            log.fine("Successfully initiated log out");
        } catch (Exception e) {
            log.log(Level.WARNING, "Error logging out:", e);
            // do nothing
        }
    }
    
    protected void setDisconnectedUI() {
        getTextStatus().setBackground(getDefStatusBackground());
        getTextStatus().setText(_("Disconnected."));
        
        actSend.setEnabled(false);
        actPoll.setEnabled(false);
        menuView.setEnabled(false);
        actAnswerCall.setEnabled(false);
        
        setActReconnectState(true);
        this.setTitle("Disconnected - " + Utils.AppName);
        if (trayIcon != null) {
            trayIcon.setConnectedState(false);
        }
        this.setEnabled(true);
    }
    
    void reloadTableColumnSettings() {
//        getRecvTableModel().;
//        tm.columns = myopts.recvfmt;
//        tm.fireTableStructureChanged();
//        
//        MyTableModel<JobFormat> tm2 = getSentTableModel();
//        tm2.columns = myopts.sentfmt;
//        tm2.fireTableStructureChanged();
//
//        tm2 = getSendingTableModel();
//        tm2.columns = myopts.sendingfmt;
//        tm2.fireTableStructureChanged();
//        
//        // Uncomment for archive support.
//        if (myopts.showArchive) {
//            ArchiveTableModel tm3 = getArchiveTableModel();
//            tm3.columns = myopts.archiveFmt;
//            tm3.fireTableStructureChanged();
//        }
        
        tableRecv.setColumnCfgString(myopts.recvColState);
        tableSent.setColumnCfgString(myopts.sentColState);
        tableSending.setColumnCfgString(myopts.sendingColState);
        if (currentServer.getOptions().showArchive && tableArchive != null)
            tableArchive.setColumnCfgString(myopts.archiveColState);
    }
    
    public void reconnectToServer(Runnable loginAction) {        
        stopReconnectTimer();
        
        if (currentServer.getOptions().host == null || currentServer.getOptions().host.length() == 0) { // Prompt for server if not set
            actOptions.actionPerformed(null);
            return;
        }
        
        this.setEnabled(false);
        tablePanel.showIndeterminateProgress(_("Logging in..."));
        
        userInitiatedLogout = false;
        Utils.executorService.submit(new LoginThread((Boolean)actAdminMode.getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY), loginAction));
        
    }
    
    
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            
            tablePanel = new ProgressPanel();
            
//            Box box = Box.createVerticalBox();
//            box.add(getTabMain());
//            box.add(getTextStatus());
//            tablePanel.setContentComponent(box);
//            

            tablePanel.setContentComponent(getStatusSplitter());
            
            JPanel quickSearchPanel = new JPanel(new BorderLayout());
            quickSearchPanel.add(tablePanel, BorderLayout.CENTER);
            quickSearchPanel.add(quickSearchbar = quickSearchHelper.getQuickSearchBar(actSearchFax), BorderLayout.NORTH);
            quickSearchbar.setVisible(myopts.showQuickSearchbar);
            
            jContentPane.add(quickSearchPanel, BorderLayout.CENTER);
            jContentPane.add(getToolbar(), BorderLayout.NORTH);
        }
        return jContentPane;
    }
    
    private JSplitPane getStatusSplitter() {
        if (statusSplitter == null) {
            statusSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getTabMain(),
                    new JScrollPane(getTextStatus(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
            statusSplitter.setOneTouchExpandable(true);
            statusSplitter.setResizeWeight(1); // Table gets all available space
            statusSplitter.setBorder(null);

            textStatus.getDocument().addDocumentListener(actAutoSizeStatus);
            statusSplitter.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, actAutoSizeStatus);
            MainWin.this.addWindowListener(actAutoSizeStatus);

        }
        return statusSplitter;
    }
    
    /**
     * This method initializes jJMenuBar	
     * 	
     * @return javax.swing.JMenuBar	
     */
    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getMenuFax());
            jJMenuBar.add(getMenuTable());
            jJMenuBar.add(getServerMenu().getMenu());
            jJMenuBar.add(getMenuView());
            jJMenuBar.add(getMenuExtras());
            jJMenuBar.add(getHelpMenu());
        }
        return jJMenuBar;
    }

    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setText(_("Help"));
            helpMenu.add(new JMenuItem(actReadme));
            helpMenu.addSeparator();
            helpMenu.add(new JMenuItem(actUpdateCheck));
            helpMenu.add(new JMenuItem(actLogConsole));
            if (!hideMenusForMac) {
            	helpMenu.add(new JMenuItem(actAbout));
            }
        }
        return helpMenu;
    }

    JTabbedPane getTabMain() {
        if (tabMain == null) {
            tabMain = new JTabbedPane();
            tabMain.addTab(_("Received"), Utils.loadCustomIcon("received.gif"), getScrollRecv(), _("Received faxes"));
            tabMain.addTab(_("Sent"), Utils.loadCustomIcon("sent.gif"), getScrollSent(), _("Sent faxes"));
            tabMain.addTab(_("Transmitting"), Utils.loadCustomIcon("sending.gif"), getScrollSending(), _("Faxes in the output queue"));
            // Uncomment for archive support.
            addOrRemoveArchiveTab();
            
            tabMain.addChangeListener(actChecker);
            tabMain.addChangeListener(quickSearchHelper);
        }
        return tabMain;
    }

    void addOrRemoveArchiveTab() {
        if (currentServer.getOptions().showArchive) {
            if ((scrollArchive == null || tabMain.indexOfComponent(scrollArchive) < 0 )) {
                tabMain.addTab(_("Archive"), Utils.loadCustomIcon("archive.gif"), getScrollArchive());
            }
        } else {
            if (scrollArchive != null) {
                tabMain.remove(scrollArchive);
            }
        }
    }
    
    private JScrollPane getScrollRecv() {
        if (scrollRecv == null) {
            scrollRecv = new JScrollPane();
            scrollRecv.setViewportView(getTableRecv());
            
            recvRowNumbers = new NumberRowViewport(tableRecv, scrollRecv);
            recvRowNumbers.setVisible(myopts.showRowNumbers);
        }
        return scrollRecv;
    }

    private TooltipJTable<RecvFormat> getTableRecv() {
        if (tableRecv == null) {
            tableRecv = new TooltipJTable<RecvFormat>(getRecvTableModel());
            doCommonTableSetup(tableRecv);
            
            recvTableModel.setUnreadFont(tableRecv.getFont().deriveFont(Font.BOLD));
        }
        return tableRecv;
    }

    Color getDefStatusBackground() {
        Color rv;
        rv = UIManager.getColor("control");
        if (rv == null)
            rv = new Color(230, 230, 230);
        return rv;
    }
    
    private JTextPane getTextStatus() {
        if (textStatus == null) {
            textStatus = new JTextPane() {
                @Override
                public Dimension getMinimumSize() {
                    return super.getPreferredSize();
                }
                
                @Override
                public Dimension getMaximumSize() {
                    Dimension d = super.getPreferredSize();
                    d.width = Integer.MAX_VALUE;
                    return d;
                }
            };
            //textStatus.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

            textStatus.setBackground(getDefStatusBackground());
            textStatus.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
            textStatus.setEditable(false);
        }
        return textStatus;
    }

    public void bringToFront() {
        if (!isVisible()) {
            setVisible(true);
        }
        
        int state = getExtendedState();
        if ((state & MainWin.ICONIFIED) != 0) 
            setExtendedState(state & (~MainWin.ICONIFIED));
        toFront();
    }
    
    ReadStateFaxListTableModel<RecvFormat> getRecvTableModel() {
        if (recvTableModel == null) {
            recvTableModel = new ReadStateFaxListTableModel<RecvFormat>(connection.getReceivedJobs(), currentServer.getPersistence());
            recvTableModel.addUnreadItemListener(new UnreadItemListener<RecvFormat>() {
                public void newItemsAvailable(UnreadItemEvent<RecvFormat> evt) {
                    if (evt.isOldDataNull())
                        return;
                    
                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_TOFRONT) != 0) {
                        bringToFront();
                    }
                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_BEEP) != 0) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_VIEWER) != 0) {
                        try {
                            connection.beginMultiOperation();
                            try {
                                for (FaxJob<RecvFormat> j : evt.getItems()) {
                                    for (FaxDocument hsf : j.getDocuments()) {
                                        try {
                                            hsf.getDocument().view();
                                        } catch (Exception e) {
                                            log.log(Level.WARNING, "Exception while trying to view new faxes:", e);
                                        }
                                    }
                                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_MARKASREAD) != 0) {
                                        j.setRead(true);
                                    }
                                }
                            } finally {
                                connection.endMultiOperation();
                            }
                        } catch (Exception ex) {
                            ExceptionDialog.showExceptionDialog(MainWin.this, "Exception while trying to view new faxes:", ex);
                        }
                    }
                }
                
                public void readStateChanged() {
                    // NOP
                }
            });
        }
        return recvTableModel;
    }


    private JScrollPane getScrollSent() {
        if (scrollSent == null) {
            scrollSent = new JScrollPane();
            scrollSent.setViewportView(getTableSent());
            
            sentRowNumbers = new NumberRowViewport(tableSent, scrollSent);
            sentRowNumbers.setVisible(myopts.showRowNumbers);
        }
        return scrollSent;
    }

    private TooltipJTable<JobFormat> getTableSent() {
        if (tableSent == null) {
            tableSent = new TooltipJTable<JobFormat>(getSentTableModel());
            doCommonTableSetup(tableSent);
        }
        return tableSent;
    }

    FaxListTableModel<JobFormat> getSentTableModel() {
        if (sentTableModel == null) {
            sentTableModel = new FaxListTableModel<JobFormat>(connection.getSentJobs());
        }
        return sentTableModel;
    }

    private JScrollPane getScrollSending() {
        if (scrollSending == null) {
            scrollSending = new JScrollPane();
            scrollSending.setViewportView(getTableSending());
            
            sendingRowNumbers = new NumberRowViewport(tableSending, scrollSending);
            sendingRowNumbers.setVisible(myopts.showRowNumbers);
        }
        return scrollSending;
    }

    private TooltipJTable<JobFormat> getTableSending() {
        if (tableSending == null) {
            tableSending = new TooltipJTable<JobFormat>(getSendingTableModel());
            doCommonTableSetup(tableSending);
        }
        return tableSending;
    }

    private void doCommonTableSetup(TooltipJTable<? extends FmtItem> table) {
        table.setShowGrid(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(actChecker);
        table.addMouseListener(getTblMouseListener());
        //table.addKeyListener(getTblKeyListener());
        
        table.getActionMap().put("yajhfc-show", actShow);
        table.getActionMap().put("yajhfc-delete", actDelete);
        final InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "yajhfc-show");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "yajhfc-delete");
        
        table.setDefaultRenderer(Date.class, getHylaDateRenderer());
        table.setDefaultRenderer(IconMap.class, new IconMap.TableCellRenderer());
        
        JTableTABAction.replaceTABWithNextRow(table);
    }
    
    FaxListTableModel<JobFormat> getSendingTableModel() {
        if (sendingTableModel == null) {
            sendingTableModel = new FaxListTableModel<JobFormat>(connection.getSendingJobs());
        }
        return sendingTableModel;
    }

    FaxListTableModel<QueueFileFormat> getArchiveTableModel() {
        if (archiveTableModel == null) {
            archiveTableModel = new FaxListTableModel<QueueFileFormat>(connection.getArchivedJobs());
        }
        return archiveTableModel;
    }
    
    JScrollPane getScrollArchive() {
        if (scrollArchive == null) {
            tableArchive = new TooltipJTable<QueueFileFormat>(getArchiveTableModel());
            doCommonTableSetup(tableArchive);
            scrollArchive = new JScrollPane(tableArchive);
            archiveRowNumbers = new NumberRowViewport(tableArchive, scrollArchive);
            archiveRowNumbers.setVisible(myopts.showRowNumbers);
        }
        return scrollArchive;
    }
    
    private JMenu getMenuFax() {
        if (menuFax == null) {
            menuFax = new JMenu();
            menuFax.setText(_("Fax"));
            menuFax.add(new JMenuItem(actSend));
            menuFax.add(new JMenuItem(actPoll));
            menuFax.add(new JMenuItem(actForward));
            menuFax.add(new JMenuItem(actResend));
            menuFax.addSeparator();
            menuFax.add(new JMenuItem(actShow));
            menuFax.add(new JMenuItem(actFaxSave));
            menuFax.add(new JMenuItem(actSaveAsPDF));
            menuFax.add(new JMenuItem(actSaveAsTIFF));
            menuFax.add(new JMenuItem(actViewLog));
            menuFax.add(new JMenuItem(actDelete));
            menuFax.addSeparator();
            menuFax.add(new JMenuItem(actResume));
            menuFax.add(new JMenuItem(actSuspend));
            menuFax.addSeparator();
            menuFax.add(new ActionJCheckBoxMenuItem(actFaxRead));
            if (!hideMenusForMac) {
                menuFax.addSeparator();
                menuFax.add(new JMenuItem(actExit));
            }
        }
        return menuFax;
    }
    
    private JMenu getMenuTable() {
        if (menuTable == null) {
            menuTable = new JMenu(_("Table"));
            menuTable.add(new JMenuItem(actSearchFax));
            menuTable.addSeparator();
            menuTable.add(new JMenuItem(actClipCopy));
            menuTable.add(new JMenuItem(actPrintTable));
            menuTable.add(new JMenuItem(actExport));
        }
        return menuTable;
    }

    private JMenu getMenuExtras() {
        if (menuExtras == null) {
            menuExtras = new JMenu(_("Extras"));
            menuExtras.add(actPhonebook);
            menuExtras.addSeparator();
            if (!hideMenusForMac) {
            	menuExtras.add(new JMenuItem(actOptions));
            }
            menuExtras.add(new JMenuItem(actEditToolbar));
            menuExtras.addSeparator();
            menuExtras.add(new JMenuItem(actReconnect));
            menuExtras.add(new ActionJCheckBoxMenuItem(actAdminMode));
            menuExtras.addSeparator();
            menuExtras.add(new JMenuItem(actAnswerCall));
            if (PluginManager.pluginUIs.size() > 0) {
                boolean createdSeparator = false;
                for (PluginUI pmc : PluginManager.pluginUIs) {
                    final JMenuItem[] menuItems = pmc.createMenuItems();
                    if (menuItems != null) {
                        for (JMenuItem item : menuItems) {
                            if (!createdSeparator) { // Only create a separator if the plugin really adds a menu item 
                                menuExtras.addSeparator();
                                createdSeparator = true;
                            }
                            menuExtras.add(item);
                        }
                    }
                }
            }
        }
        return menuExtras;
    }
    
    protected ServerMenu getServerMenu() {
        if (serverMenu == null) {
            serverMenu = new ServerMenu();
            if (currentServer != null)
                serverMenu.setSelectionByID(currentServer.getID());
        }
        return serverMenu;
    }
    
    public Frame getFrame() {
        return this;
    }
    
    class MenuViewListener implements ActionListener, ChangeListener {
        private JRadioButtonMenuItem[] lastSel = new JRadioButtonMenuItem[TableType.TABLE_COUNT];
        @SuppressWarnings({ "rawtypes" })
        private Filter[] currentFilters = new Filter[TableType.TABLE_COUNT];
        
        private void setJobFilter(@SuppressWarnings("rawtypes") FaxListTableModel model, @SuppressWarnings("rawtypes") Filter filter) {
            currentFilters[model.getTableType().ordinal()] = filter;
            refreshFilter(model);
        }
        
        @SuppressWarnings("unchecked")
        public Filter<FaxJob<? extends FmtItem>,? extends FmtItem> getFilterFor(TableType tableType) {
            return currentFilters[tableType.ordinal()];
        }
        
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            try {
                String cmd = e.getActionCommand();
                @SuppressWarnings("rawtypes")
                FaxListTableModel model = getSelectedTable().getRealModel();
                int selTab = tabMain.getSelectedIndex();

                if (cmd.equals("view_all")) {
                    setJobFilter(model, null);
                    lastSel[selTab] = menuViewAll;
                } else if (cmd.equals("view_own")) {
                    setJobFilter(model, getOwnFilterFor(model));
                    lastSel[selTab] = menuViewOwn;
                } else if (cmd.equals("view_custom")) {
                    @SuppressWarnings("rawtypes")
                    CustomFilterDialog cfd = new CustomFilterDialog(MainWin.this, 
                            MessageFormat.format(Utils._("Custom filter for table {0}"), tabMain.getTitleAt(selTab)),
                            Utils._("Only display fax jobs fulfilling:"),
                            Utils._("You have entered no filtering conditions. Do you want to show all faxes instead?"),
                            Utils._("Please enter a valid date/time!\n(Hint: Exactly the same format as in the fax job table is expected)"),
                            model.getColumns(), (lastSel[selTab] == menuViewCustom) ? getFilterFor(model.getTableType()) : null);
                    cfd.setVisible(true);
                    if (cfd.okClicked) {
                        if (cfd.returnValue == null) {
                            menuViewAll.doClick();
                            return;
                        } else {
                            setJobFilter(model, cfd.returnValue);
                            lastSel[selTab] = menuViewCustom;
                        }
                    } else {
                        if (lastSel[selTab] != menuViewCustom)
                            resetLastSel(selTab);
                    }
                } else if (cmd.equals("mark_failed")) {
                    myopts.markFailedJobs = menuMarkError.isSelected();

                    getSelectedTable().repaint();
                }
            } catch (Exception ex) {
                Object src = null;
                if (e != null) {
                    src = e.getSource();
                }
                if (src == null || !(src instanceof Component)) {
                    src = Launcher2.application;
                }

                ExceptionDialog.showExceptionDialog((Component)src, Utils._("An Error occurred executing the desired action:"), ex);
            }
        }
        
        private void resetLastSel(int selTab) {
            if (lastSel[selTab] != null)
                lastSel[selTab].setSelected(true);
            else 
                menuViewAll.setSelected(true);
        }
        
        public void stateChanged(ChangeEvent e) {
            FaxListTableModel<? extends FmtItem> model = getSelectedTable().getRealModel();
            boolean viewOwnState  = ownFilterOK(model);
            boolean markErrorState = canMarkError(model);
            
            resetLastSel(tabMain.getSelectedIndex());
            menuViewOwn.setEnabled(myopts.allowChangeFilter && viewOwnState);
            menuMarkError.setEnabled(markErrorState);
            if ((!viewOwnState && menuViewOwn.isSelected())) {
                menuViewAll.setSelected(true);
                setJobFilter(model, null);
            }
        }
        
        private Filter<FaxJob<? extends FmtItem>,? extends FmtItem> getOwnFilterFor(FaxListTableModel<? extends FmtItem> model) {
            final String user = currentServer.isConnected() ? connection.getClientManager().getUser() : currentServer.getOptions().user;
            return new StringFilter<FaxJob<? extends FmtItem>,FmtItem>(getOwnerColumn(model), StringFilterOperator.EQUAL, user, true);
        }
        
        private boolean canMarkError(FaxListTableModel<? extends FmtItem> model) {
            return model.getJobs().isShowingErrorsSupported();
        }
        
        private FmtItem getOwnerColumn(FaxListTableModel<? extends FmtItem> model) {
            switch (model.getTableType()) {
            case RECEIVED:
                return RecvFormat.o;
            case SENT:
            case SENDING:
                return JobFormat.o;
            case ARCHIVE:
                return QueueFileFormat.owner;
            default:
                return null;
            }
        }
        
        private boolean ownFilterOK(FaxListTableModel<? extends FmtItem> model) {
            final FmtItem ownerItem = getOwnerColumn(model);
            return (ownerItem != null && model.getColumns().getCompleteView().contains(ownerItem));
        }
        /**
         * Re-validates the filters on reconnection
         */
        @SuppressWarnings("unchecked")
        public void reConnected() {
            for (int i = 0; i < tabMain.getTabCount(); i++) {
                @SuppressWarnings("rawtypes")
                FaxListTableModel model = getTableByIndex(i).getRealModel();
                if (lastSel[i] == menuViewOwn) {
                    if (ownFilterOK(model)) 
                        setJobFilter(model, getOwnFilterFor(model));
                    else {
                        lastSel[i] = menuViewAll;
                        setJobFilter(model, null);
                    }
                } else if (lastSel[i] == menuViewCustom) {
                    if (getFilterFor(model.getTableType()) == null || !getFilterFor(model.getTableType()).validate(model.getColumns())) {
                        lastSel[i] = menuViewAll;
                        setJobFilter(model, null);
                    }
                    
                } else if (lastSel[i] == menuViewAll) 
                    setJobFilter(model, null);
            }
            stateChanged(null);
        }
        

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void loadSaveString(int idx, String data) {
            if ((data == null) || data.equals("A")) {
                lastSel[idx] = menuViewAll;
            } else if (data.equals("O")) {
                lastSel[idx] = menuViewOwn;
            } else if (data.startsWith("C")) {
                FaxListTableModel model = getTableByIndex(idx).getRealModel();
                Filter<FaxJob,FmtItem> yjf = FilterCreator.stringToFilter(data.substring(1), model.getColumns());
                if (yjf == null) {
                    lastSel[idx] = menuViewAll;
                } else {
                    lastSel[idx] = menuViewCustom;
                    setJobFilter(model, yjf);
                }
            } else {
                // Fall back to view all faxes
                log.warning("Unknown filter for index " + idx + ":" + data);
                lastSel[idx] = menuViewAll;
            }
        }
        
        public void loadFromOptions(FaxOptions opts) {
            loadSaveString(0, opts.recvFilter);
            loadSaveString(1, opts.sentFilter);
            loadSaveString(2, opts.sendingFilter);
            loadSaveString(3, opts.archiveFilter);
            reConnected();
        }
        
        private String getSaveString(int idx) {
            if (lastSel[idx] == null || lastSel[idx] == menuViewAll) {
                return "A";
            } else if (lastSel[idx] == menuViewOwn) {
                return "O";
            } else if (lastSel[idx] == menuViewCustom) {
                FaxListTableModel<? extends FmtItem> model = getTableByIndex(idx).getRealModel();
                return "C" + FilterCreator.filterToString(getFilterFor(model.getTableType()));
            } else
                return null;
        }
        
        public void saveToOptions(FaxOptions opts) {
            opts.recvFilter = getSaveString(0);
            opts.sentFilter = getSaveString(1);
            opts.sendingFilter = getSaveString(2);
            opts.archiveFilter = getSaveString(3);
        }
    }

    class ActionEnabler implements ListSelectionListener, ChangeListener {
        public void stateChanged(ChangeEvent e) {
            doEnableCheck();
        }
        
        public void valueChanged(ListSelectionEvent e) {
            doEnableCheck();
        }
        
        public void doEnableCheck() {
            boolean showState = false;
            boolean deleteState = false;
            boolean faxReadState = false, faxReadSelected = false;
            boolean resendState = false;
            boolean suspResumeState = false;
            boolean viewLogState = false;
            
            final Component selectedComponent = tabMain.getSelectedComponent();
            if (selectedComponent == scrollRecv) { // Received Table active
                if (tableRecv.getSelectedRow() >= 0) {
                    showState = true;
                    deleteState = true;
                    faxReadState = true;
                    faxReadSelected = tableRecv.getJobForRow(tableRecv.getSelectedRow()).isRead();
                }
            } else if (selectedComponent == scrollSent) { // Sent Table
                if (tableSent.getSelectedRow() >= 0) {
                    deleteState = true;
                    showState = true;
                    resendState = true;
                    viewLogState = true;
                }
            } else if (selectedComponent == scrollSending) { // Sending Table
                if (tableSending.getSelectedRow() >= 0) {
                    deleteState = true;
                    showState = true;
                    resendState = true;
                    suspResumeState = true;
                    viewLogState = true;
                }
                // Uncomment for archive support.
            } if (selectedComponent == scrollArchive) { // Archive Table
                if (tableArchive.getSelectedRow() >= 0) {
                    deleteState = true;
                    showState = true;
                    resendState = true;
                    viewLogState = true;
                }
            } 
            
            actShow.setEnabled(showState);
            actFaxSave.setEnabled(showState);
            actSaveAsPDF.setEnabled(showState);
            actSaveAsTIFF.setEnabled(showState);
            actDelete.setEnabled(deleteState);
            actFaxRead.setEnabled(faxReadState);
            actForward.setEnabled(faxReadState);
            actResend.setEnabled(resendState);
            actSuspend.setEnabled(suspResumeState);
            actResume.setEnabled(suspResumeState);
            actClipCopy.setEnabled(showState);
            actViewLog.setEnabled(viewLogState);

            actFaxRead.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, faxReadSelected);
        }
    }
    
    static class RefreshCompleteHider extends SwingFaxListConnectionListener {
        final ProgressUI progressUI;
        final FaxListConnection parent;
        final Runnable refreshCompleteAction;
        
        @Override
        protected void refreshCompleteSwing(RefreshKind refreshKind, boolean success) {
            if (refreshKind != RefreshKind.STATUS) {
                progressUI.close();
                parent.removeFaxListConnectionListener(this);
                if (refreshCompleteAction != null) {
                	refreshCompleteAction.run();
                }
            }
        }
        
        public RefreshCompleteHider(ProgressUI progressUI, FaxListConnection parent) {
        	this(progressUI, parent, null);
        }
        
        public RefreshCompleteHider(ProgressUI progressUI, FaxListConnection parent, Runnable refreshCompleteAction) {
            super(false,false,true);
            this.progressUI = progressUI;
            this.parent = parent;
            this.refreshCompleteAction = refreshCompleteAction;
        }
    }
    
    class LoginThread implements Runnable {
        protected boolean wantAdmin;
        protected Runnable loginAction;
        protected boolean refreshComplete = false;
        
        
        public void run() {  
            try {
                
                PersistentReadState persistentReadState = currentServer.getPersistence();
                recvTableModel.setPersistentReadState(persistentReadState);
                
                // Read the read/unread status *after* the table contents has been set 

                persistentReadState.prepareReadStates();
                
                if (Utils.debugMode) {
                    log.info("Begin login (wantAdmin=" + wantAdmin + ")");
                }
                
                // UI updates after we have a list of faxes:
                Runnable refreshCompleteAction = new Runnable() {
                    public void run() {

                        reloadTableColumnSettings();

                        menuView.setEnabled(true);
                        // Re-check menu View state:
                        menuViewListener.reConnected();

                        actSend.setEnabled(true);
                        actPoll.setEnabled(true);
                        actAnswerCall.setEnabled(true);

                        setActReconnectState(false);

                        sendReady = SendReadyState.Ready;
                        MainWin.this.setEnabled(true);

                        log.info("Finished init work after refresh complete!");
                        if (loginAction != null) {
                            if (Utils.debugMode) {
                                log.info("Doing login action: " + loginAction.getClass().getName());
                            }
                            loginAction.run();
                            if (Utils.debugMode) {
                                log.info("Finished login action.");
                            }
                        }
                    } 
                };
                
                connection.addFaxListConnectionListener(new RefreshCompleteHider(tablePanel, connection, refreshCompleteAction));
                if (!connection.connect(wantAdmin)) {
                    log.info("Login failed, bailing out");
                    doErrorCleanup();
                    return;
                }
                
                if (Utils.debugMode) {
                    log.info("Login succeeded. -- begin init work.");
                }
                
                // Final UI updates after we have the connection established:
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (tablePanel.isShowingProgress())
                            tablePanel.showIndeterminateProgress(_("Fetching fax list..."));
                        
                        final HylaClientManager clientManager = connection.getClientManager();
                        MainWin.this.setTitle(clientManager.getUser() + "@" + currentServer.getOptions().host + (clientManager.isAdminMode() ? " (admin)" : "") + " - " + Utils.AppName);
                        if (trayIcon != null) {
                            trayIcon.setConnectedState(true);
                        }

                        actAdminMode.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, clientManager.isAdminMode());
                        if (clientManager.isAdminMode()) {
                            // A reddish gray
                            Color defStatusBackground = getDefStatusBackground();
                            textStatus.setBackground(new Color(Math.min(defStatusBackground.getRed() + 40, 255), defStatusBackground.getGreen(), defStatusBackground.getBlue()));
                        } 
                        log.fine("Finished init work after connect!");
                    } 
                });
            } catch (Exception e) {
                ExceptionDialog.showExceptionDialog(MainWin.this, _("An error occured connecting to the server:"), e);
                doErrorCleanup();
            }
        }
        
        private void doErrorCleanup() {
            if (Utils.debugMode) {
                log.info("Login failed! -- doing cleanup.");
            }
            connection.disconnect();
            sendReady = SendReadyState.NotReady;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tablePanel.hideProgress();
                    MainWin.this.setEnabled(true);
                 } 
             });
        }
        
        public LoginThread(boolean wantAdmin, Runnable loginAction) {
            this.wantAdmin = wantAdmin;
            this.loginAction = loginAction;
        }
    }    

    void refreshFilter() {
        refreshFilter(getSelectedTable().getRealModel());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void refreshFilter(FaxListTableModel selectedModel ) {
        TableType selectedTableType = selectedModel.getTableType();
        Filter viewFilter = menuViewListener.getFilterFor(selectedTableType);
        Filter quickSearchFilter = quickSearchHelper.getFilterFor(selectedTableType);
        
        Filter modelFilter;
        if (viewFilter == null) {
            if (quickSearchFilter == null) {
                modelFilter = null;
            } else {
                modelFilter = quickSearchFilter;
            }
        } else {
            if (quickSearchFilter == null) {
                modelFilter = viewFilter;
            } else {
                modelFilter = new AndFilter(viewFilter, quickSearchFilter); 
            }
        }
        selectedModel.setJobFilter(modelFilter);
    }
    
    public HylaClientManager getClientManager() {
        return connection.getClientManager();
    }
    
    /**
     * Sets the currentServer to the server with the specified ID
     * Do not call this method if there is a server already set; use switchServer in that case instead!
     * @param newID
     */
    protected void setCurrentServerByID(final int newID) {
        final ServerManager serverMan = ServerManager.getDefault();
        if (!serverMan.setCurrentByID(newID)) {
            if (serverMan.getServers().size() > 0)
                serverMan.setCurrentByIndex(0);
        }
        currentServer = serverMan.getCurrent();
    }
    
    /**
     * Switches to the specified server.
     * @param newID
     */
    protected void switchServer(final int newID) {
        doLogout(true, new Runnable() {
            public void run() {
                setCurrentServerByID(newID);
                
                addOrRemoveArchiveTab();
                if (currentServer.getConnection() == connection) {
                    getRecvTableModel().fireTableStructureChanged();
                    getSentTableModel().fireTableStructureChanged();
                    getSendingTableModel().fireTableStructureChanged();
                    if (currentServer.getOptions().showArchive) {
                        getArchiveTableModel().fireTableStructureChanged();
                    }
                } else {
                    setFaxListConnectionFromServer();
                    getRecvTableModel().setJobs(connection.getReceivedJobs());
                    getSentTableModel().setJobs(connection.getSentJobs());
                    getSendingTableModel().setJobs(connection.getSendingJobs());
                    if (currentServer.getOptions().showArchive) {
                        getArchiveTableModel().setJobs(connection.getArchivedJobs());
                    }
                }
                serverMenu.setSelectionByID(newID);
            }
        });
    }
    
    protected void optionsChanged() {
        showOrHideTrayIcon();
        ServerManager.getDefault().optionsChanged();
        serverMenu.refreshMenuItems();
        switchServer(currentServer.getID());
    }

    private final FaxListConnectionListener connListener = new SwingFaxListConnectionListener(true, true, false) {        
        @Override
        protected void serverStatusChangedSwing(String statusText) {
            textStatus.setText(statusText);
        }
        
        @Override
        protected void connectionStateChangeSwing(ConnectionState oldState,
                ConnectionState newState) {
            if (newState == ConnectionState.DISCONNECTED) {
                setDisconnectedUI();
                if (tablePanel.isShowingProgress())
                    tablePanel.hideProgress();
                if (!userInitiatedLogout && myopts.autoReconnect) {
                    setupReconnectTimer();
                }
                userInitiatedLogout = false;
            }
        }
    };
    void setFaxListConnectionFromServer()  {
        if (currentServer == null)
            return;
        
        try {
            if (connection != null)
                connection.removeFaxListConnectionListener(connListener);
            connection = currentServer.getConnection();
            connection.addFaxListConnectionListener(connListener);
        } catch (Exception e) {
            if (currentServer.getOptions().faxListConnectionType == FaxListConnectionType.HYLAFAX) {
                ExceptionDialog.showExceptionDialog(MainWin.this, "Error creating FaxListConnection, exiting YajHFC.", e);
                System.exit(1);
            } else {
                ExceptionDialog.showExceptionDialog(MainWin.this, "Error creating FaxListConnection, fallling back to default.", e);
                currentServer.getOptions().faxListConnectionType = FaxListConnectionType.HYLAFAX;
                setFaxListConnectionFromServer();
            }
        }
    }
    
    private static final int RECONNECT_DELAY = 30;
    javax.swing.Timer reconnectTimer;
    protected void setupReconnectTimer() {
        if (reconnectTimer == null) {
            reconnectTimer = new javax.swing.Timer(1000, null);
            reconnectTimer.setInitialDelay(0);
            reconnectTimer.addActionListener(new ActionListener() {
                private int counter = RECONNECT_DELAY;
                MessageFormat reconnectFmt = new MessageFormat(_("Disconnected, will try to reconnect in {0} seconds..."));

                public void actionPerformed(ActionEvent e) {
                    textStatus.setText(reconnectFmt.format(new Object[] {counter}));
                    if (counter > 0) {
                        counter--;
                    } else {
                        reconnectTimer.stop();
                        reconnectTimer = null;
                        reconnectToServer(null);
                    }
                }
            });
            reconnectTimer.start();
        }
    }
    
    private void stopReconnectTimer() {
        if (reconnectTimer != null && reconnectTimer.isRunning()) {
            reconnectTimer.stop();
            reconnectTimer = null;
        }
    }
   

    class QuickSearchHelper extends AbstractQuickSearchHelper implements ChangeListener {
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Filter getFilterFor(TableType tableType) {
            String searchText = textQuickSearch.getText();
            if (searchText == null || searchText.length() == 0) {
                return null;
            }
          
            // Search on all visible columns
            List<? extends FmtItem> availableCols = getColumnsFor(tableType);
            OrFilter rv = new OrFilter();

            for (FmtItem col : availableCols) {
                rv.addChild(new StringFilter(col, StringFilterOperator.CONTAINS, searchText, false));
            }
            return rv;
        }
        
        private FmtItemList<? extends FmtItem> getColumnsFor(TableType tableType) {
            switch (tableType) {
            case RECEIVED:
                return recvTableModel.getColumns();
            case SENT:
                return sentTableModel.getColumns();
            case SENDING:
                return sendingTableModel.getColumns();
            case ARCHIVE:
                return archiveTableModel.getColumns();
            default:
                throw new IllegalArgumentException("Unknown table type");
            }
        }
        
        public void stateChanged(ChangeEvent e) {
            refreshFilter();
        }

        protected void performActualQuickSearch() {
            refreshFilter();
        }
        
        @Override
        protected Component getFocusComponent() {
            return getSelectedTable();
        }
        
        public JToolBar getQuickSearchBar(Action searchAction) {
            return getQuickSearchBar(searchAction, 
                    _("Type here parts of the sender, recipient or file name in order to search for a fax."),
                    _("Reset quick search and show all faxes."));
        }
    }
    
    class StatusBarResizeAction extends ExcDialogAbstractAction implements DocumentListener, PropertyChangeListener, WindowListener {
        /**
         * The maximum value the status bar size may differ from getAutoDividerLocation() to keep it at auto size
         */
        private static final int RESET_THRESHOLD = 2;
        
        private boolean firstDisplay = true;
        
        public void actualActionPerformed(ActionEvent e) {
            setSelected(!isSelected());
        };

        public boolean isSelected() {
            Boolean state = (Boolean)getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY);
            return (state != null) ? state.booleanValue() : false;
        }
        
        public void setSelected(boolean selected) {
            putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, selected);
            if (selected) {
                makePreferredSize();
            }
        }

        private void makePreferredSize() {
            //statusSplitter.resetToPreferredSizes();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    statusSplitter.setDividerLocation(getAutoDividerLocation());
                    //System.out.println("makePreferredSize: " + getAutoDividerLocation());
                }
            });
        }
        
        int getAutoDividerLocation() {
            return statusSplitter.getHeight() - statusSplitter.getBottomComponent().getPreferredSize().height - statusSplitter.getDividerSize() - 1;
        }
        
        public void changedUpdate(DocumentEvent e) {
            // NOP
        }

        public void insertUpdate(DocumentEvent e) {
            if (isSelected()) {
                makePreferredSize();
            }
        }

        public void removeUpdate(DocumentEvent e) {
            if (isSelected()) {
                makePreferredSize();
            }
        }
        
        private boolean initialAdjustments = true;
        public void propertyChange(PropertyChangeEvent evt) {
            if (!MainWin.this.isVisible())
                return;
            
            if (isSelected() && 
                    Math.abs((Integer)evt.getNewValue() - getAutoDividerLocation()) > RESET_THRESHOLD) {
                if (!initialAdjustments) {
                    setSelected(false);
                }
            } else {
                initialAdjustments = false;
            }
            //System.out.println("Reset: " + ((Integer)evt.getNewValue() - getAutoDividerLocation()));
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
            if (firstDisplay) {
                setSelected(myopts.statusBarSize < 0);
                if (myopts.statusBarSize >= 0) {
                    statusSplitter.setDividerLocation(statusSplitter.getHeight() - myopts.statusBarSize);
                }
                firstDisplay = false;
            } else if (isSelected()) {
                makePreferredSize();
            }
        }
    }
    
    class ServerMenu implements ActionListener {
        //private static final String PROP_SERVER = "ServerMenu->Server";
        
        protected JMenu serverMenu;
        protected final List<JRadioButtonMenuItem> serverMenuItems = new ArrayList<JRadioButtonMenuItem>();
        protected ButtonGroup serverGroup;
        
        public ServerMenu() {
            serverMenu = new JMenu(_("Server"));
            serverGroup = new ButtonGroup();
            refreshMenuItems();
        }
        
        public JMenu getMenu() {
            return serverMenu;
        }
        
        public void refreshMenuItems() {
            Map<String,JRadioButtonMenuItem> oldMenus;
            if (serverMenuItems.size() > 0) {
                oldMenus = new HashMap<String, JRadioButtonMenuItem>();
                for (JRadioButtonMenuItem item : serverMenuItems) {
                    oldMenus.put(item.getActionCommand(), item);
                }
            } else {
                oldMenus = Collections.emptyMap();
            }
            
            serverMenuItems.clear();
            serverMenu.removeAll();
         
            for (Server server : ServerManager.getDefault().getServers()) {
                String actionCommand = String.valueOf(server.getID());
                
                JRadioButtonMenuItem newItem = oldMenus.get(actionCommand);
                if (newItem == null) {
                    newItem = new JRadioButtonMenuItem(server.toString());
                    newItem.addActionListener(this);
                    newItem.setActionCommand(actionCommand);
                    serverGroup.add(newItem);
                } else {
                    newItem.setText(server.toString());
                    oldMenus.remove(actionCommand);
                }
                //newItem.putClientProperty(PROP_SERVER, server);
                serverMenuItems.add(newItem);
                serverMenu.add(newItem);
                
                if (server == currentServer) {
                    newItem.setSelected(true);
                }
            }
            
            // Items left in the map correspond to removed itemsListModel
            for (JRadioButtonMenuItem item : oldMenus.values()) {
                serverGroup.remove(item);
            }
        }
        
        public void setSelectionByIndex(int index) {
            serverMenuItems.get(index).setSelected(true);
        }
        
        public void setSelectionByID(int id) {
            String sID = String.valueOf(id);
            for (JRadioButtonMenuItem item : serverMenuItems) {
                if (sID.equals(item.getActionCommand())) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            //System.out.println("actionPerformed: " + e);
            JRadioButtonMenuItem item = (JRadioButtonMenuItem)e.getSource();
            int newID = Integer.parseInt(item.getActionCommand());
            if (newID != currentServer.getID()) {
                switchServer(newID);
            }
        }
        
    }
}  


