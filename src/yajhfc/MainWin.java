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
import gnu.hylafax.Job;

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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
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
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.JTable.PrintMode;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConverter;
import yajhfc.file.FormattedFile.FileFormat;
import yajhfc.filters.AndFilter;
import yajhfc.filters.Filter;
import yajhfc.filters.FilterCreator;
import yajhfc.filters.OrFilter;
import yajhfc.filters.StringFilter;
import yajhfc.filters.StringFilterOperator;
import yajhfc.filters.ui.CustomFilterDialog;
import yajhfc.launch.Launcher2;
import yajhfc.launch.MainApplicationFrame;
import yajhfc.macosx.MacOSXSupport;
import yajhfc.model.MyTableModel;
import yajhfc.model.RecvYajJob;
import yajhfc.model.SendingYajJob;
import yajhfc.model.SentYajJob;
import yajhfc.model.TooltipJTable;
import yajhfc.model.UnReadMyTableModel;
import yajhfc.model.UnreadItemEvent;
import yajhfc.model.UnreadItemListener;
import yajhfc.model.YajJob;
import yajhfc.model.archive.ArchiveTableModel;
import yajhfc.model.archive.ArchiveYajJob;
import yajhfc.model.archive.FileHylaDirAccessor;
import yajhfc.model.archive.HylaDirAccessor;
import yajhfc.model.archive.QueueFileFormat;
import yajhfc.options.OptionsWin;
import yajhfc.phonebook.NewPhoneBookWin;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;
import yajhfc.readstate.PersistentReadState;
import yajhfc.send.SendController;
import yajhfc.send.SendWinControl;
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

@SuppressWarnings("serial")
public final class MainWin extends JFrame implements MainApplicationFrame {
    
    /**
     * The interval in which the read state is automatically saved
     */
    private static final int READ_PERSIST_INTERVAL = 12345;
    static final Logger log = Logger.getLogger(MainWin.class.getName());
    
    protected JPanel jContentPane = null;
    
    protected JToolBar toolbar;
    protected QuickSearchHelper quickSearchHelper = new QuickSearchHelper();
    
    protected JTabbedPane tabMain = null;
    
    protected JScrollPane scrollRecv = null;
    protected JScrollPane scrollSent = null;
    protected JScrollPane scrollSending = null;
    
    protected TooltipJTable<RecvFormat> tableRecv = null;
    protected TooltipJTable<JobFormat> tableSent = null;
    protected TooltipJTable<JobFormat> tableSending = null;
    
    protected UnReadMyTableModel recvTableModel = null;  
    protected MyTableModel<JobFormat> sentTableModel = null;  
    protected MyTableModel<JobFormat> sendingTableModel = null; 
    
    protected NumberRowViewport recvRowNumbers, sentRowNumbers, sendingRowNumbers;
    
    protected JTextPane textStatus = null;
    protected JSplitPane statusSplitter;
    
    protected JMenuBar jJMenuBar = null;
    
    protected JMenu menuFax = null;
    protected JMenu menuView = null;
    protected JMenu menuExtras = null;
    protected JMenu helpMenu = null;
    
    protected JCheckBoxMenuItem menuMarkError;
    
    protected JRadioButtonMenuItem menuViewAll, menuViewOwn, menuViewCustom;
    protected ButtonGroup viewGroup;
    
    protected FaxOptions myopts = null;
      
    protected java.util.Timer utmrTable;
    protected TableRefresher tableRefresher = null;
    protected StatusRefresher statRefresher = null;
   
    protected MouseListener tblMouseListener;
    //protected KeyListener tblKeyListener;
    protected DefaultTableCellRenderer hylaDateRenderer;
    
    protected JPopupMenu tblPopup;
    
    protected ProgressPanel tablePanel;
    
    protected MenuViewListener menuViewListener;
    
    // Uncomment for archive support.
    protected TooltipJTable<QueueFileFormat> tableArchive;
    protected JScrollPane scrollArchive;
    protected ArchiveTableModel archiveTableModel;
    protected NumberRowViewport archiveRowNumbers;
    
    // Actions:
    protected Action actSend, actShow, actDelete, actOptions, actExit, actAbout, actPhonebook, actReadme, actPoll, actFaxRead, actFaxSave, actForward, actAdminMode;
    protected Action actRefresh, actResend, actPrintTable, actSuspend, actResume, actClipCopy, actShowRowNumbers, actAdjustColumns, actReconnect, actEditToolbar;
    protected Action actSaveAsPDF, actSaveAsTIFF, actUpdateCheck, actAnswerCall, actSearchFax, actViewLog; 
    protected StatusBarResizeAction actAutoSizeStatus;
    protected ActionEnabler actChecker;
    protected Map<String,Action> availableActions = new HashMap<String,Action>();
    protected YajHFCTrayIcon trayIcon = null;
    
    protected HylaClientManager clientManager;
    
    public enum SendReadyState {
        Ready, NeedToWait, NotReady;
    }
    protected SendReadyState sendReady = SendReadyState.NeedToWait;
    boolean hideMenusForMac = false;
    
    // Worker classes:
    private class DeleteWorker extends ProgressWorker {
        private TooltipJTable<? extends FmtItem> selTable;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
            if (hyfc == null) {
                return;
            }
            int[] selRows =  selTable.getSelectedRows();

            for (int i : selRows) {
                YajJob<? extends FmtItem> yj = null;
                try {
                    yj = selTable.getJobForRow(i);
                    updateNote(MessageFormat.format(_("Deleting fax {0}"), yj.getIDValue()));
                    
                    yj.delete(hyfc);
                    
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
            
            clientManager.endServerTransaction();
        }
        @Override
        protected void done() {
            refreshTables();
        }
        
        public DeleteWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selTable = selTable;
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(false);
        }
    }
    private class MultiSaveWorker extends ProgressWorker {
        private TooltipJTable<? extends FmtItem> selTable;
        private File targetDir;
        private int fileCounter;
        private final boolean askForEveryFile;
        private JFileChooser fileChooser;
        
        @Override
        protected int calculateMaxProgress() {
            return 1000*selTable.getSelectedRowCount();
        }

        @Override
        public void doWork() {
            fileCounter = 0;
            HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
            if (hyfc == null) {
                return;
            }
            try {
                int[] selRows =  selTable.getSelectedRows();
                List<String> errorInfo = new ArrayList<String>();
                for (int i : selRows) {
                    YajJob<? extends FmtItem> yj = null;
                    try {
                        yj = selTable.getJobForRow(i);
                        errorInfo.clear();
                        List<HylaServerFile> hsfs = yj.getServerFilenames(hyfc, errorInfo);
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
                            updateNote(MessageFormat.format(_("Saving fax {0}"), yj.getIDValue()));
                            for (HylaServerFile hsf : hsfs) {
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
                                    hsf.download(hyfc, target);
                                    fileCounter++;
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
                clientManager.endServerTransaction();
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
            this.selTable = selTable;
            this.targetDir = targetDir;
            this.progressMonitor = tablePanel;
            this.askForEveryFile = askForEveryFile;
            this.setCloseOnExit(true);
        }
    }
    private class ShowWorker extends ProgressWorker {
        private TooltipJTable<? extends FmtItem> selTable;
        private int sMin, sMax;
        
        @Override
        protected int calculateMaxProgress() {
            return 100 + 1200*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
            if (hyfc == null) {
                return;
            }
            List<FormattedFile> downloadedFiles = new ArrayList<FormattedFile>();
            List<String> errorInfo = new ArrayList<String>();
            int[] selRows =  selTable.getSelectedRows();
            sMin = Integer.MAX_VALUE; sMax = Integer.MIN_VALUE;
            final MessageFormat displayingMsg      = new MessageFormat(_("Displaying fax {0}"));
            final MessageFormat downloadingMessage = new MessageFormat(Utils._("Downloading {0}"));
            for (int i : selRows) {
                YajJob<? extends FmtItem> yj = null;
                try {
                    yj = selTable.getJobForRow(i);
                    updateNote(displayingMsg.format(new Object[] { yj.getIDValue() }));
                    downloadedFiles.clear();
                    errorInfo.clear();
                    
                    //System.out.println("" + i + ": " + yj.getIDValue().toString());
                    List<HylaServerFile> serverFiles = yj.getServerFilenames(hyfc, errorInfo);
                    
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
                        for(HylaServerFile hsf : serverFiles) {
                            updateNote(downloadingMessage.format(new Object[] {hsf.getPath()}));
                            try {
                                downloadedFiles.add(hsf.getPreviewFile(hyfc));
                            } catch (Exception e1) {
                                showExceptionDialog(MessageFormat.format(_("An error occured displaying the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()), e1);
                            }
                            stepProgressBar(step);
                        }
                        updateNote(Utils._("Launching viewer"));
                        if (downloadedFiles.size() > 0) {
                            MultiFileConverter.viewMultipleFiles(downloadedFiles, myopts.paperSize, false);
                        }
                        stepProgressBar(100);
                    }
                    if (yj instanceof RecvYajJob) {
                        ((RecvYajJob)yj).setRead(true);
                        if (i < sMin)
                            sMin = i;
                        if (i > sMax)
                            sMax = i;
                    }
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("An error occured displaying the fax \"{0}\":\n"), yj.getIDValue()) + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    showExceptionDialog(MessageFormat.format(_("An error occured displaying the fax \"{0}\":"), yj.getIDValue()), e1);
                }
            }
            clientManager.endServerTransaction();
        }
        
        @Override
        protected void done() {
            if (sMax >= 0 && selTable == tableRecv) {
                tableRecv.getSorter().fireTableRowsUpdated(sMin, sMax);
                actFaxRead.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, true);
            }
        }
        
        public ShowWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selTable = selTable;
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(true);
        }
    }
    // Worker classes:
    private class SuspendWorker extends ProgressWorker {
        private TooltipJTable<? extends FmtItem> selTable;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
            if (hyfc == null) {
                return;
            }
            int[] selRows =  selTable.getSelectedRows();

            for (int i : selRows) {
                SendingYajJob yj = null;
                try {
                    yj = (SendingYajJob)selTable.getJobForRow(i);
                    updateNote(MessageFormat.format(_("Suspending job {0}"), yj.getIDValue()));
                    
                    char jobstate = yj.getJobState();
                    if (jobstate == SentYajJob.JOBSTATE_RUNNING) {
                        if (showConfirmDialog(MessageFormat.format(_("Suspending the currently running job {0} may block until it is done (or switch to another \"non running state\"). Try to suspend it anyway?") , yj.getIDValue()),
                                _("Suspend fax job"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            yj.suspend(hyfc);
                        }
                    } else {
                        yj.suspend(hyfc);
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
            clientManager.endServerTransaction();
        }
        @Override
        protected void done() {
            refreshTables();
        }
        
        public SuspendWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selTable = selTable;
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(false);
        }
    }
    // Worker classes:
    private class ResumeWorker extends ProgressWorker {
        private TooltipJTable<? extends FmtItem> selTable;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
            if (hyfc == null) {
                return;
            }
            int[] selRows =  selTable.getSelectedRows();

            for (int i : selRows) {
                SendingYajJob yj = null;
                try {
                    yj = (SendingYajJob)selTable.getJobForRow(i);
                    updateNote(MessageFormat.format(_("Resuming job {0}"), yj.getIDValue()));
                    char jobstate = yj.getJobState();
                    if (jobstate != SentYajJob.JOBSTATE_SUSPENDED) {
                        if (showConfirmDialog(MessageFormat.format(_("Job {0} is not in state \"Suspended\" so resuming it probably will not work. Try to resume it anyway?") , yj.getIDValue()),
                                _("Resume fax job"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            yj.resume(hyfc);
                        }
                    } else {
                        yj.resume(hyfc);
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
            clientManager.endServerTransaction();
        }
        @Override
        protected void done() {
            refreshTables();
        }
        
        public ResumeWorker(TooltipJTable<? extends FmtItem> selTable) {
            this.selTable = selTable;
            this.progressMonitor = tablePanel;
            this.setCloseOnExit(false);
        }
    }
    private class SaveToFormatWorker extends ProgressWorker {
        private TooltipJTable<? extends FmtItem> selTable;
        private File targetDir;
        private int fileCounter;
        private JFileChooser fileChooser;
        private boolean askForEveryFile;
        private final FileFormat desiredFormat;
        
        @Override
        protected int calculateMaxProgress() {
            return 1000*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            try {
                fileCounter = 0;
                HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                if (hyfc == null) {
                    return;
                }
                int[] selRows =  selTable.getSelectedRows();
                List<FormattedFile> ffs = new ArrayList<FormattedFile>();

                for (int i : selRows) {
                    YajJob<? extends FmtItem> yj = null;
                    try {
                        yj = selTable.getJobForRow(i);
                        updateNote(MessageFormat.format(_("Saving fax {0}"), yj.getIDValue()));
                        ffs.clear();
                        for(HylaServerFile hsf : yj.getServerFilenames(hyfc)) {
                            try {
                                ffs.add(hsf.getPreviewFile(hyfc));
                            } catch (Exception e1) {
                                //JOptionPane.showMessageDialog(MainWin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
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
                            
                            File target = new File(targetDir, filePrefix + '.' + desiredFormat.getDefaultExtension());
                            if (askForEveryFile) {
                                FileChooserRunnable runner = new FileChooserRunnable(MainWin.this, fileChooser, MessageFormat.format(_("File name to save fax {0}"), yj.getIDValue()), null, target, false);
                                SwingUtilities.invokeAndWait(runner);
                                if (runner.getSelection() == null) {
                                    return;
                                }
                                target = runner.getSelection();
                                targetDir = target.getParentFile();
                            }
                            MultiFileConverter.convertMultipleFilesToSingleFile(ffs, target, desiredFormat, myopts.paperSize);
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
                clientManager.endServerTransaction();
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
                FileFilter pdfFilter = new ExampleFileFilter(desiredFormat.getPossibleExtensions(), desiredFormat.getDescription());
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
        
        public SaveToFormatWorker(TooltipJTable<? extends FmtItem> selTable, File targetDir, boolean askForEveryFile, FileFormat desiredFormat) {
            this.selTable = selTable;
            this.targetDir = targetDir;
            this.progressMonitor = tablePanel;
            this.askForEveryFile = askForEveryFile;
            this.desiredFormat = desiredFormat;
            this.setCloseOnExit(true);
        }
    }
    private class SaveToFormatAction extends ExcDialogAbstractAction {
        private final FileFormat desiredFormat;

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

        public SaveToFormatAction(FileFormat desiredFormat) {
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
                
                List<HylaModem> modems;
                if (clientManager != null) {
                    try {
                        modems = clientManager.getModems();
                        if (modems == null) {
                            modems = HylaModem.defaultModems;
                        }
                    } catch (Exception ex) {
                        modems = HylaModem.defaultModems;
                    }
                } else {
                    modems = HylaModem.defaultModems;
                }
                
                //PROFILE: long time = System.currentTimeMillis();
                OptionsWin ow = new OptionsWin(myopts, MainWin.this, modems);
                //PROFILE: System.out.println("After OptionsWin constructor: " + (-time + (time = System.currentTimeMillis())));
                ow.setModal(true);
                Utils.unsetWaitCursorOnOpen(null, ow);
                ow.setVisible(true);
                if (ow.getModalResult()) {
                    showOrHideTrayIcon();
                    addOrRemoveArchiveTab();
                    reconnectToServer(null);
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
                SendWinControl sw = SendController.createSendWindow(MainWin.this, clientManager, false, false);

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
                SendWinControl sw = SendController.createSendWindow(MainWin.this, clientManager, true, true);
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
                        ((RecvYajJob)tableRecv.getJobForRow(i)).setRead(newState);
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
                
                HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                HylaServerFile file;
                try {
                    file = tableRecv.getJobForRow(tableRecv.getSelectedRow()).getServerFilenames(hyfc).get(0);
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(MainWin.this, _("Couldn't get a filename for the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    ExceptionDialog.showExceptionDialog(MainWin.this, _("Couldn't get a filename for the fax:"), e1);
                    clientManager.endServerTransaction();
                    return;
                }
                
                SendWinControl sw = SendController.createSendWindow(MainWin.this, clientManager, false, true);
                sw.addServerFile(file);
                sw.setVisible(true);
                refreshTables();
                
                clientManager.endServerTransaction();
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
                
                reconnectToServer(null);
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
                if (selTable != tableSent || selTable.getSelectedRow() < 0)
                    return;
                
                Utils.setWaitCursor(null);
                SentYajJob job = (SentYajJob)selTable.getJobForRow(selTable.getSelectedRow());
                
                List<HylaServerFile> files;
                String number, voiceNumber, company, name, location, subject;
                HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                
                try {
                    synchronized (hyfc) {
                        files = job.getServerFilenames(hyfc);
                        
                        Job hyJob = job.getJob(hyfc);
                        number = hyJob.getDialstring();
                        name = hyJob.getProperty("TOUSER");
                        company = hyJob.getProperty("TOCOMPANY");
                        location = hyJob.getProperty("TOLOCATION");
                        voiceNumber = hyJob.getProperty("TOVOICE");
                        subject = hyJob.getProperty("REGARDING");
                    }
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(MainWin.this, _("Couldn't get a filename for the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    Utils.unsetWaitCursor(null);
                    ExceptionDialog.showExceptionDialog(MainWin.this, _("Could not get all of the job information necessary to resend the fax:"), e1);
                    return;
                } finally {
                    clientManager.endServerTransaction();
                }
                
                SendWinControl sw = SendController.createSendWindow(MainWin.this, clientManager, false, true);
                
                for (HylaServerFile hysf : files) {
                    sw.addServerFile(hysf);
                }
                sw.getRecipients().add(new DefaultPBEntryFieldContainer(number, name, company, location, voiceNumber));
                sw.setSubject(subject);
                
                Utils.unsetWaitCursorOnOpen(null, sw.getWindow());
                sw.setVisible(true);
                refreshTables();
            };
        };
        actResend.putValue(Action.NAME, _("Resend fax..."));
        actResend.putValue(Action.SHORT_DESCRIPTION, _("Resend the fax"));
        actResend.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Export"));
        putAvailableAction("Resend", actResend);
        
        actPrintTable = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                try {
                    MessageFormat header = new MessageFormat(tabMain.getToolTipTextAt(tabMain.getSelectedIndex()));
                    Date now = new Date();
                    MessageFormat footer = new MessageFormat("'" + DateFormat.getDateInstance(DateFormat.SHORT, Utils.getLocale()).format(now) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Utils.getLocale()).format(now) + "' - " + Utils._("page {0}"));
                    
                    selTable.print(PrintMode.FIT_WIDTH, header, footer);
                } catch (PrinterException pe) {
                    ExceptionDialog.showExceptionDialog(MainWin.this, Utils._("Error printing the table:"), pe);
                }
            };
        };
        actPrintTable.putValue(Action.NAME, _("Print table..."));
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
                if (clientManager != null) {
                    doLogout();
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
                if (clientManager.isAdminMode()) {
                    List<HylaModem> modems = new ArrayList<HylaModem>();
                    for (HylaModem modem : clientManager.getModems()) {
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
                TooltipJTable<? extends FmtItem> selTable = getSelectedTable();
                
                if (selTable.getSelectedRowCount() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                
                TableType tt = selTable.getRealModel().getTableType();
                switch (tt) {
                case SENT:
                case SENDING:
                    // Supported
                    break;
                default:
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                
                // Collect selected rows:
                List<YajJob<? extends FmtItem>> jobs = new ArrayList<YajJob<? extends FmtItem>>(selTable.getSelectedRowCount());
                for (int idx : selTable.getSelectedRows()) {
                    jobs.add(selTable.getJobForRow(idx));
                }
                
                LogViewWorker worker = new LogViewWorker(jobs, clientManager, tablePanel);
                worker.setCloseOnExit(true);
                worker.startWork(MainWin.this, _("Viewing logs"));
            }
        };
        actViewLog.putValue(Action.NAME, _("View log") + "...");
        actViewLog.putValue(Action.SHORT_DESCRIPTION, _("Displays the communication log of the selected fax"));
        actViewLog.putValue(Action.SMALL_ICON, Utils.loadIcon("general/History"));
        putAvailableAction("ViewLog", actViewLog);
        
        
        actAutoSizeStatus = new StatusBarResizeAction();
        actAutoSizeStatus.putValue(Action.NAME, _("Auto-size status bar"));
        actAutoSizeStatus.putValue(Action.SHORT_DESCRIPTION, _("Automatically resize the status bar"));
        putAvailableAction("AutoSizeStatus", actAutoSizeStatus);
        
        actSaveAsPDF = new SaveToFormatAction(FileFormat.PDF);
        putAvailableAction("SaveAsPDF", actSaveAsPDF);
        
        actSaveAsTIFF = new SaveToFormatAction(FileFormat.TIFF);
        putAvailableAction("SaveAsTIFF", actSaveAsTIFF);
        
        actChecker = new ActionEnabler();
    }
    
    private void putAvailableAction(String key, Action act) {
        if (availableActions.put(key, act) != null) {
            log.severe("Action " + key + " already existed!");
        }
        act.putValue(Action.ACTION_COMMAND_KEY, key);
    }
    
    @SuppressWarnings("unchecked")
    TooltipJTable<? extends FmtItem> getSelectedTable() {
        return (TooltipJTable)((JScrollPane)tabMain.getSelectedComponent()).getViewport().getView();
    }
    @SuppressWarnings("unchecked")
    TooltipJTable<? extends FmtItem> getTableByIndex(int index) {
        return (TooltipJTable)((JScrollPane)tabMain.getComponent(index)).getViewport().getView();
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
        if (tableRefresher == null)
            return;
        
        tablePanel.showIndeterminateProgress(_("Fetching fax list..."));
        
        tableRefresher.hideProgress = true;
        utmrTable.schedule(new TimerTaskWrapper(tableRefresher), 0);
    }
    
    public void refreshStatus() {
        if (statRefresher == null)
            return;
        
        utmrTable.schedule(new TimerTaskWrapper(statRefresher), 0);
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
                        MyTableModel<? extends FmtItem> model = ((TooltipJTable<? extends FmtItem>)table).getRealModel();
                        formatBuffer.setLength(0);
                        value = model.columns.get(realCol).getDisplayDateFormat().format(value, formatBuffer, dummyPos).toString();
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
            menuView.add(new JSeparator());
            menuView.add(menuMarkError);
            menuView.add(new ActionJCheckBoxMenuItem(actShowRowNumbers));
            menuView.add(new ActionJCheckBoxMenuItem(actAdjustColumns));
            menuView.add(new ActionJCheckBoxMenuItem(actAutoSizeStatus));
            menuView.add(new JSeparator());
            menuView.add(new JMenuItem(actRefresh));
            
            getTabMain().addChangeListener(menuViewListener);
        }
        return menuView;
    }
    
    /**
     * This is the default constructor
     */
    public MainWin(boolean adminState) {
        super();
        initialize(adminState);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize(boolean adminState) {
        myopts = Utils.getFaxOptions();
        
        createActions(adminState);
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
                
                doLogout();
                
                saveWindowSettings();
            
                Thread.yield();
                System.exit(0);
            }
        });
        setIconImage(Toolkit.getDefaultToolkit().getImage(MainWin.class.getResource("icon.png")));
        
        utmrTable = new java.util.Timer("RefreshTimer", true);
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
        if (myopts.automaticallyCheckForUpdate) {
            UpdateChecker.startSilentUpdateCheck();
        }
        
        Utils.executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                PersistentReadState.getCurrent().persistReadState();
            }
        }, READ_PERSIST_INTERVAL, READ_PERSIST_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    public void saveWindowSettings() {
        saveTableColumnSettings();
        
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
    			macSup.setApplicationMenuActions(actOptions, actAbout, actExit);
    			hideMenusForMac = myopts.adjustMenusForMacOSX;
    			macSup.setDockIconImage(Toolkit.getDefaultToolkit().getImage(MainWin.class.getResource("logo-large.png")));
    		}
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
    
    void invokeLogoutThreaded() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doLogout();
            } 
        });
    }
    
    void doLogout() {
        doLogout(false);
    }
    
    private void saveTableColumnSettings() {
        if (clientManager != null) { 
            myopts.recvColState = getTableRecv().getColumnCfgString();
            myopts.sentColState = getTableSent().getColumnCfgString();
            myopts.sendingColState = getTableSending().getColumnCfgString();
            if (tableArchive != null)
                myopts.archiveColState = tableArchive.getColumnCfgString();
        }
    }
    
    private void doLogout(boolean immediateReconnect) {
        try {
            log.fine("Logging out...");
            sendReady = immediateReconnect ? SendReadyState.NeedToWait : SendReadyState.NotReady;
            
            tablePanel.showIndeterminateProgress(_("Logging out..."));
            if (tableRefresher != null)
                tableRefresher.cancel();
            if (statRefresher != null)
                statRefresher.cancel();
            
            if (clientManager != null) {                
                saveTableColumnSettings();
                
                recvTableModel.cleanupReadState();
                
                utmrTable.schedule(new AsyncLogoutTask(clientManager), 0);
                clientManager = null;
            }
            //tmrStat.stop();
            //tmrTable.stop();

            
            getRecvTableModel().setData(null);
            getSentTableModel().setData(null);
            getSendingTableModel().setData(null);
            if (myopts.showArchive) {
                getArchiveTableModel().setData((List<ArchiveYajJob>)null);
            }
            
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
            log.fine("Successfully logged out");
        } catch (Exception e) {
            log.log(Level.WARNING, "Error logging out:", e);
            // do nothing
        }
        tablePanel.hideProgress();
    }
    
    void reloadTableColumnSettings() {
        UnReadMyTableModel tm = getRecvTableModel();
        tm.columns = myopts.recvfmt;
        tm.fireTableStructureChanged();
        
        MyTableModel<JobFormat> tm2 = getSentTableModel();
        tm2.columns = myopts.sentfmt;
        tm2.fireTableStructureChanged();

        tm2 = getSendingTableModel();
        tm2.columns = myopts.sendingfmt;
        tm2.fireTableStructureChanged();
        
        // Uncomment for archive support.
        if (myopts.showArchive) {
            ArchiveTableModel tm3 = getArchiveTableModel();
            tm3.columns = myopts.archiveFmt;
            tm3.fireTableStructureChanged();
        }
        
        tableRecv.setColumnCfgString(myopts.recvColState);
        tableSent.setColumnCfgString(myopts.sentColState);
        tableSending.setColumnCfgString(myopts.sendingColState);
        // Uncomment for archive support.
        if (myopts.showArchive && tableArchive != null)
            tableArchive.setColumnCfgString(myopts.archiveColState);
    }
    
    public void reconnectToServer(Runnable loginAction) {        
        doLogout(true);
        
        if (myopts.host.length() == 0) { // Prompt for server if not set
            actOptions.actionPerformed(null);
            return;
        }
        
        this.setEnabled(false);
        tablePanel.showIndeterminateProgress(_("Logging in..."));
        
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
            quickSearchPanel.add(quickSearchHelper.getQuickSearchBar(actSearchFax), BorderLayout.NORTH);
            
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
        // Uncomment for archive support.
        if (myopts.showArchive) {
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
            
            recvTableModel.unreadFont = tableRecv.getFont().deriveFont(Font.BOLD);
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
    
    private UnReadMyTableModel getRecvTableModel() {
        if (recvTableModel == null) {
            recvTableModel = new UnReadMyTableModel(PersistentReadState.getCurrent());
            recvTableModel.addUnreadItemListener(new UnreadItemListener() {
                public void newItemsAvailable(UnreadItemEvent evt) {
                    if (evt.isOldDataNull())
                        return;
                    
                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_TOFRONT) != 0) {
                        bringToFront();
                    }
                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_BEEP) != 0) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    if ((myopts.newFaxAction & FaxOptions.NEWFAX_VIEWER) != 0) {
                        HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                        if (hyfc == null) {
                            return;
                        }
                        for (RecvYajJob j : evt.getItems()) {
                            for (HylaServerFile hsf : j.getServerFilenames(hyfc, null)) {
                                try {
                                    hsf.getPreviewFile(hyfc).view();
                                } catch (Exception e) {
                                    if (Utils.debugMode) {
                                        log.log(Level.WARNING, "Exception while trying to view new faxes:", e);
                                        //e.printStackTrace(Utils.debugOut);
                                    }
                                }
                            }
                            if ((myopts.newFaxAction & FaxOptions.NEWFAX_MARKASREAD) != 0) {
                                j.setRead(true);
                            }
                        }
                        clientManager.endServerTransaction();
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

    private MyTableModel<JobFormat> getSentTableModel() {
        if (sentTableModel == null) {
            sentTableModel = new MyTableModel<JobFormat>() {
                @Override
                protected YajJob<JobFormat> createYajJob(String[] data) {
                    return new SentYajJob(this.columns, data);
                }
                
                @Override
                public TableType getTableType() {
                    return TableType.SENT;
                }
            };
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
    
    private MyTableModel<JobFormat> getSendingTableModel() {
        if (sendingTableModel == null) {
            sendingTableModel = new MyTableModel<JobFormat>() {
                @Override
                protected YajJob<JobFormat> createYajJob(String[] data) {
                    return new SendingYajJob(this.columns, data);
                }
                
                @Override
                public TableType getTableType() {
                    return TableType.SENDING;
                }
            };
        }
        return sendingTableModel;
    }

    // Uncomment for archive support.
    private ArchiveTableModel getArchiveTableModel() {
        if (archiveTableModel == null) {
            archiveTableModel = new ArchiveTableModel();
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

    private JMenu getMenuExtras() {
        if (menuExtras == null) {
            menuExtras = new JMenu(_("Extras"));
            menuExtras.add(actPhonebook);
            menuExtras.addSeparator();
            menuExtras.add(new JMenuItem(actClipCopy));
            menuExtras.add(new JMenuItem(actPrintTable));
            menuExtras.add(new JMenuItem(actSearchFax));
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
    
    public Frame getFrame() {
        return this;
    }
    
    class MenuViewListener implements ActionListener, ChangeListener {
        private JRadioButtonMenuItem[] lastSel = new JRadioButtonMenuItem[TableType.TABLE_COUNT];
        @SuppressWarnings("unchecked")
        private Filter[] currentFilters = new Filter[TableType.TABLE_COUNT];
        
        @SuppressWarnings("unchecked")
        private void setJobFilter(MyTableModel model, Filter filter) {
            currentFilters[model.getTableType().ordinal()] = filter;
            refreshFilter(model);
        }
        
        @SuppressWarnings("unchecked")
        public Filter<YajJob<? extends FmtItem>,? extends FmtItem> getFilterFor(TableType tableType) {
            return currentFilters[tableType.ordinal()];
        }
        
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            try {
                String cmd = e.getActionCommand();
                MyTableModel model = getSelectedTable().getRealModel();
                int selTab = tabMain.getSelectedIndex();

                if (cmd.equals("view_all")) {
                    setJobFilter(model, null);
                    lastSel[selTab] = menuViewAll;
                } else if (cmd.equals("view_own")) {
                    setJobFilter(model, getOwnFilterFor(model));
                    lastSel[selTab] = menuViewOwn;
                } else if (cmd.equals("view_custom")) {
                    CustomFilterDialog cfd = new CustomFilterDialog(MainWin.this, 
                            MessageFormat.format(Utils._("Custom filter for table {0}"), tabMain.getTitleAt(selTab)),
                            Utils._("Only display fax jobs fulfilling:"),
                            Utils._("You have entered no filtering conditions. Do you want to show all faxes instead?"),
                            Utils._("Please enter a valid date/time!\n(Hint: Exactly the same format as in the fax job table is expected)"),
                            model.columns, (lastSel[selTab] == menuViewCustom) ? getFilterFor(model.getTableType()) : null);
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
            MyTableModel<? extends FmtItem> model = getSelectedTable().getRealModel();
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
        
        private Filter<YajJob<? extends FmtItem>,? extends FmtItem> getOwnFilterFor(MyTableModel<? extends FmtItem> model) {
            final String user = (clientManager != null) ? clientManager.getUser() : myopts.user;
            return new StringFilter<YajJob<? extends FmtItem>,FmtItem>(getOwnerColumn(model), StringFilterOperator.EQUAL, user, true);
        }
        
        private boolean canMarkError(MyTableModel<? extends FmtItem> model) {
            List<? extends FmtItem> columns = model.columns.getCompleteView();
            switch (model.getTableType()) {
            case RECEIVED:
                return columns.contains(RecvFormat.e);
            case SENT:
            case SENDING:
                return columns.contains(JobFormat.a) || columns.contains(JobFormat.s);
            case ARCHIVE:
                return columns.contains(QueueFileFormat.state);
            default:
                return false;
            }
        }
        
        private FmtItem getOwnerColumn(MyTableModel<? extends FmtItem> model) {
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
        
        private boolean ownFilterOK(MyTableModel<? extends FmtItem> model) {
            final FmtItem ownerItem = getOwnerColumn(model);
            return (ownerItem != null && model.columns.getCompleteView().contains(ownerItem));
        }
        /**
         * Re-validates the filters on reconnection
         */
        @SuppressWarnings("unchecked")
        public void reConnected() {
            for (int i = 0; i < tabMain.getTabCount(); i++) {
                MyTableModel model = getTableByIndex(i).getRealModel();
                if (lastSel[i] == menuViewOwn) {
                    if (ownFilterOK(model)) 
                        setJobFilter(model, getOwnFilterFor(model));
                    else {
                        lastSel[i] = menuViewAll;
                        setJobFilter(model, null);
                    }
                } else if (lastSel[i] == menuViewCustom) {
                    if (getFilterFor(model.getTableType()) == null || !getFilterFor(model.getTableType()).validate(model.columns)) {
                        lastSel[i] = menuViewAll;
                        setJobFilter(model, null);
                    }
                    
                } else if (lastSel[i] == menuViewAll) 
                    setJobFilter(model, null);
            }
            stateChanged(null);
        }
        

        @SuppressWarnings("unchecked")
        private void loadSaveString(int idx, String data) {
            if ((data == null) || data.equals("A")) {
                lastSel[idx] = menuViewAll;
            } else if (data.equals("O")) {
                lastSel[idx] = menuViewOwn;
            } else if (data.startsWith("C")) {
                MyTableModel model = getTableByIndex(idx).getRealModel();
                Filter<YajJob,FmtItem> yjf = FilterCreator.stringToFilter(data.substring(1), model.columns);
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
                MyTableModel<? extends FmtItem> model = getTableByIndex(idx).getRealModel();
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
                    faxReadSelected = ((RecvYajJob)tableRecv.getJobForRow(tableRecv.getSelectedRow())).isRead();
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
                    resendState = false;
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

    class StatusRefresher extends TimerTask {
        String oldText = "";

        public synchronized void run() {
            try {
                String newText;
                HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                if (hyfc == null) {
                    newText = Utils._("Could not log in");
                    cancel();
                    invokeLogoutThreaded();
                    return;
                } else {
                    try {
                        Vector<?> status;
                        synchronized (hyfc) {
                            log.finest("In hyfc monitor");
                            status = hyfc.getList("status");
                        }
                        log.finest("Out of hyfc monitor");
                        newText = Utils.listToString(status, "\n");
                    } catch (SocketException se) {
                        log.log(Level.WARNING, "Error refreshing the status, logging out.", se);
                        cancel();
                        invokeLogoutThreaded();
                        return;
                    } catch (Exception e) {
                        newText = _("Error refreshing the status:") + " " + e;
                        log.log(Level.WARNING, "Error refreshing the status:", e);
                    }
                }
                if (!newText.equals(oldText)) {
                    oldText = newText;

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            textStatus.setText(oldText);
                        } 
                    });
                }
                if (clientManager != null)
                    clientManager.endServerTransaction();
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Error refreshing the status:", ex);
            }
        }
    };
    
    class TableRefresher extends TimerTask {
        String sentfmt, sendingfmt;
        Vector<?> lastRecvList = null, lastSentList = null, lastSendingList = null;
        // Uncomment for archive support.
        long lastArchiveModification = -1;
        HylaDirAccessor hyda;
        boolean cancelled = false;
        /**
         * Whether to hide the progress panel on the next run
         */
        public boolean hideProgress = false;
        
        @Override
        public boolean cancel() {
            cancelled = true;
            return super.cancel();
        }
        
        public synchronized void run() {
            try {
                log.fine("Begin table refresh");
                
                HylaFAXClient hyfc = clientManager.beginServerTransaction(MainWin.this);
                if (hyfc == null) {
                    return;
                }
                Vector<?> lst;
                try {
                    //System.out.println(System.currentTimeMillis() + ": Getting list...");
                    synchronized (hyfc) {
                        lst = hyfc.getList("recvq");
                    }
                    //System.out.println(System.currentTimeMillis() + ": Got list...");
                    if ((lastRecvList == null) || !lst.equals(lastRecvList)) {
                        String[][] data = new String[lst.size()][];
                        for (int i = 0; i < lst.size(); i++) {
                            //data[i] = ((String)lst.get(i)).split("\\|");
                            data[i] = Utils.fastSplit((String)lst.get(i), '|');
                        }
                        SwingUtilities.invokeLater(new TableDataRunner(recvTableModel, data));
                        lastRecvList = lst;

                        //                    if (!didFirstRun) {
                        //                        // Read the read/unread status *after* the table contents has been set 
                        //                        SwingUtilities.invokeLater(new Runnable() {
                        //                            public void run() {
                        //                                recvTableModel.loadReadState(PersistentReadState.CURRENT);
                        //                                tableRecv.repaint();
                        //                            }
                        //                        });
                        //                        didFirstRun = true;
                        //                    }
                        //System.out.println(System.currentTimeMillis() + ": Did invokeLater()");
                    }
                } catch (SocketException se) {
                    log.log(Level.WARNING, "A socket error occured refreshing the tables, logging out.", se);
                    cancel();
                    invokeLogoutThreaded();
                    return;
                } catch (Exception e) {
                    log.log(Level.WARNING, "An error occured refreshing the tables: ", e);
                    //                if (Utils.debugMode) {
                    //                    Utils.debugOut.println("An error occured refreshing the tables: ");
                    //                    e.printStackTrace(Utils.debugOut);
                    //                }
                }        

                log.fine("recvq complete");
                if (cancelled) {
                    log.fine("Already cancelled");
                    return;
                }
                
                try {
                    synchronized (hyfc) {
                        hyfc.jobfmt(sentfmt);
                        lst = hyfc.getList("doneq");
                    }
                    if ((lastSentList == null) || !lst.equals(lastSentList)) {
                        String[][] data = new String[lst.size()][];
                        for (int i = 0; i < lst.size(); i++) {
                            //data[i] = ((String)lst.get(i)).split("\\|");
                            data[i] = Utils.fastSplit((String)lst.get(i), '|');
                        }
                        SwingUtilities.invokeLater(new TableDataRunner(sentTableModel, data));
                        lastSentList = lst;
                    }
                } catch (SocketException se) {
                    log.log(Level.WARNING, "A socket error occured refreshing the tables, logging out.", se);
                    cancel();
                    invokeLogoutThreaded();
                    return;
                } catch (Exception e) {
                    log.log(Level.WARNING, "An error occured refreshing the tables: ", e);
                    //                if (Utils.debugMode) {
                    //                    Utils.debugOut.println("An error occured refreshing the tables: ");
                    //                    e.printStackTrace(Utils.debugOut);
                    //                }
                }

                log.fine("doneq complete");
                if (cancelled) {
                    log.fine("Already cancelled");
                    return;
                }
                
                try {
                    synchronized (hyfc) {
                        hyfc.jobfmt(sendingfmt);
                        lst = hyfc.getList("sendq");
                    }
                    if ((lastSendingList == null) || !lst.equals(lastSendingList)) {
                        String[][] data = new String[lst.size()][];
                        for (int i = 0; i < lst.size(); i++) {
                            //data[i] = ((String)lst.get(i)).split("\\|");
                            data[i] = Utils.fastSplit((String)lst.get(i), '|');
                        }
                        SwingUtilities.invokeLater(new TableDataRunner(sendingTableModel, data));
                        lastSendingList = lst;
                    }
                } catch (SocketException se) {
                    log.log(Level.WARNING, "A socket error occured refreshing the tables, logging out.", se);
                    cancel();
                    invokeLogoutThreaded();
                    return;
                } catch (Exception e) {
                    log.log(Level.WARNING, "An error occured refreshing the tables: ", e);
                    //                if (Utils.debugMode) {
                    //                    Utils.debugOut.println("An error occured refreshing the tables: ");
                    //                    e.printStackTrace(Utils.debugOut);
                    //                }
                }
                
                log.fine("sendq complete");

                if (cancelled) {
                    log.fine("Already cancelled");
                    return;
                }
                // Uncomment for archive support.
                if (myopts.showArchive) {
                    try {                        
                        long modificationTime = hyda.getLastModified();
                        if (lastArchiveModification == -1 || lastArchiveModification != modificationTime) {
                            final List<ArchiveYajJob> archiveJobs = ArchiveYajJob.getArchiveFiles(hyda, archiveTableModel.columns);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    archiveTableModel.setData(archiveJobs);
                                } 
                            });
                            lastArchiveModification = modificationTime;
                        }
                    } catch (Exception e) {
                        log.log(Level.WARNING, "An error occured refreshing the tables: ", e);
                    }
                    log.fine("archive complete");
                }

                if (hideProgress && tablePanel.isShowingProgress()) {
                    log.fine("Hiding progress...");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (cancelled) {
                                log.fine("Already cancelled");
                                return;
                            }
                            tablePanel.hideProgress();
                            log.fine("Progress hidden");
                        }
                    });
                    hideProgress = false;
                }
                if (clientManager != null)
                    clientManager.endServerTransaction();
                log.fine("Tables refresh complete");
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Error refreshing the tables:", ex);
            }
        }
        
        public TableRefresher(String sentfmt, String sendingfmt) {
            this.sentfmt = sentfmt;
            this.sendingfmt = sendingfmt; 
            
            if (myopts.showArchive) {
                    hyda = new FileHylaDirAccessor(new File(myopts.archiveLocation));
            }
        }
        
        class TableDataRunner implements Runnable {
            private String[][] data = null;
            private MyTableModel<? extends FmtItem> tm;
                    
            public void run() {
                if (cancelled) {
                    log.fine("Already cancelled");
                    return;
                }
                //System.out.println(System.currentTimeMillis() + ": About to set data...");
                tm.setData(data);         
                //System.out.println(System.currentTimeMillis() + ": Set data.");
            }
            
            public TableDataRunner(MyTableModel<? extends FmtItem> tm, String[][] data) {
                this.tm = tm;
                this.data = data;
            }
        }
    }
    
    static class TimerTaskWrapper extends TimerTask {
        protected Runnable wrapped;
        
        @Override
        public void run() {
            wrapped.run();
        }
        
        public TimerTaskWrapper(Runnable wrapped) {
            this.wrapped = wrapped;
        }
    }
    
    class LoginThread implements Runnable {
        protected boolean wantAdmin;
        protected Runnable loginAction;
        
        public void run() {  
            try {
                
                if (Utils.debugMode) {
                    log.info("Begin login (wantAdmin=" + wantAdmin + ")");
                }
                clientManager = new HylaClientManager(myopts);
                clientManager.setAdminMode(wantAdmin);
                if (clientManager.beginServerTransaction(MainWin.this) == null) {
                    doErrorCleanup();
                    return;
                }
                if (Utils.debugMode) {
                    log.info("Login succeeded. -- begin init work.");
                }
                
                PersistentReadState persistentReadState = PersistentReadState.getCurrent();
                recvTableModel.setPersistentReadState(persistentReadState);
                
                // Multi-threaded implementation of the periodic refreshes.
                // I hope I didn't introduce too many race conditions/deadlocks this way
                statRefresher = new StatusRefresher();

                tableRefresher = new TableRefresher(myopts.sentfmt.getFormatString(), myopts.sendingfmt.getFormatString());
                tableRefresher.hideProgress = true;
                
                // Read the read/unread status *after* the table contents has been set 

                persistentReadState.prepareReadStates();
                
                // Final UI updates:
                SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                       MainWin.this.setTitle(clientManager.getUser() + "@" + myopts.host + (clientManager.isAdminMode() ? " (admin)" : "") + " - " +Utils.AppName);
                       if (trayIcon != null) {
                           trayIcon.setConnectedState(true);
                       }
                       
                       actAdminMode.putValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY, clientManager.isAdminMode());
                       if (clientManager.isAdminMode()) {
                           // A reddish gray
                           Color defStatusBackground = getDefStatusBackground();
                           textStatus.setBackground(new Color(Math.min(defStatusBackground.getRed() + 40, 255), defStatusBackground.getGreen(), defStatusBackground.getBlue()));
                       } 
                       
                       reloadTableColumnSettings();
                       
                       menuView.setEnabled(true);
                       // Re-check menu View state:
                       menuViewListener.reConnected();
                       
                       tablePanel.showIndeterminateProgress(_("Fetching fax list..."));
                       
                       utmrTable.schedule(statRefresher, 0, myopts.statusUpdateInterval);
                       utmrTable.schedule(tableRefresher, 0, myopts.tableUpdateInterval);
                       
                       actSend.setEnabled(true);
                       actPoll.setEnabled(true);
                       actAnswerCall.setEnabled(true);
                       
                       setActReconnectState(false);
                       
                       sendReady = SendReadyState.Ready;
                       MainWin.this.setEnabled(true);
                       
                       if (Utils.debugMode) {
                           log.info("Finished init work!");
                       }
                       if (loginAction != null) {
                           if (Utils.debugMode) {
                               log.info("Doing login action: " + loginAction.getClass().getName());
                           }
                           loginAction.run();
                           if (Utils.debugMode) {
                               log.info("Finished login action.");
                           }
                       }
                       clientManager.endServerTransaction();
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
            clientManager = null;
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

    public HylaClientManager getClientManager() {
        return clientManager;
    }
    

    void refreshFilter() {
        refreshFilter(getSelectedTable().getRealModel());
    }
    
    @SuppressWarnings("unchecked")
    void refreshFilter(MyTableModel selectedModel ) {
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
    
    private static class AsyncLogoutTask extends TimerTask {
        private HylaClientManager clientManager;

        @Override
        public void run() {
            try {
                clientManager.forceLogout();
            } catch (Exception e) {
                log.log(Level.WARNING, "Error logging out:", e);
            }
        }

        public AsyncLogoutTask(HylaClientManager clientManager) {
            super();
            this.clientManager = clientManager;
        }
        
    }
    
    class QuickSearchHelper extends AbstractQuickSearchHelper implements ChangeListener {
        
        @SuppressWarnings("unchecked")
        public Filter getFilterFor(TableType tableType) {
            String searchText = textQuickSearch.getText();
            if (searchText == null || searchText.length() == 0) {
                return null;
            }
            
//            FmtItem[] filterCols;
//            switch (tableType) {
//            case RECEIVED:
//                filterCols = new FmtItem[] { RecvFormat.e, RecvFormat.f, RecvFormat.s, RecvFormat.i, RecvFormat.j, RecvFormat.o, RecvFormat.Y, RecvFormat.Z};
//                break;
//            case SENT:
//            case SENDING:
//                filterCols = new FmtItem[] { JobFormat.C, JobFormat.e, JobFormat.j, JobFormat.R, JobFormat.s, JobFormat.v, JobFormat.J, JobFormat.S, JobFormat.o, JobFormat.Y, JobFormat.Z};
//                break;
//            case ARCHIVE:
//                filterCols = new FmtItem[] { QueueFileFormat.company, QueueFileFormat.external, QueueFileFormat.jobid, QueueFileFormat.number, QueueFileFormat.receiver, QueueFileFormat.status, QueueFileFormat.jobtag, QueueFileFormat.sender, QueueFileFormat.owner, QueueFileFormat.tts };
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown table type");
//            }
//            List<? extends FmtItem> availableCols = getColumnsFor(tableType).getCompleteView();
//            OrFilter rv = new OrFilter();
//            
//            for (FmtItem fi : filterCols) {
//                if (availableCols.contains(fi)) {
//                    rv.addChild(new StringFilter(fi, StringFilterOperator.CONTAINS, searchText, false));
//                }
//            }
          
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
                return recvTableModel.columns;
            case SENT:
                return sentTableModel.columns;
            case SENDING:
                return sendingTableModel.columns;
            case ARCHIVE:
                return archiveTableModel.columns;
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
}  


