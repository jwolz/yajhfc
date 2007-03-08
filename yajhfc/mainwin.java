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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.filters.CustomFilterDialog;
import yajhfc.filters.FilterCreator;
import yajhfc.filters.StringFilter;
import yajhfc.filters.StringFilterOperator;
import yajhfc.phonebook.PhoneBookWin;


public final class mainwin extends JFrame {
    
    private JPanel jContentPane = null;
    
    private JToolBar toolbar;
    
    private JTabbedPane TabMain = null;
    
    private JScrollPane scrollRecv = null;
    private JScrollPane scrollSent = null;
    private JScrollPane scrollSending = null;
    
    private TooltipJTable TableRecv = null;
    private TooltipJTable TableSent = null;
    private TooltipJTable TableSending = null;
    
    private UnReadMyTableModel recvTableModel = null;  
    private MyTableModel sentTableModel = null;  
    private MyTableModel sendingTableModel = null; 
    
    private JTextPane TextStatus = null;
    
    private JMenuBar jJMenuBar = null;
    
    private JMenu menuFax = null;
    private JMenu menuView = null;
    private JMenu menuExtras = null;
    private JMenu helpMenu = null;
    
    private JMenuItem exitMenuItem = null;
    private JMenuItem aboutMenuItem = null;
    private JMenuItem optionsMenuItem = null;
    private JMenuItem ShowMenuItem = null;
    private JMenuItem DeleteMenuItem = null;
    private JMenuItem SendMenuItem = null;    
    
    private JRadioButtonMenuItem menuViewAll, menuViewOwn, menuViewCustom;
    private ButtonGroup viewGroup;
    
    HylaFAXClient hyfc = null;
    private FaxOptions myopts = null;
      
    private java.util.Timer utmrTable;
    private TableRefresher tableRefresher = null;
    private StatusRefresher statRefresher = null;
    
    private MouseListener tblMouseListener;
    private KeyListener tblKeyListener;
    private DefaultTableCellRenderer hylaDateRenderer;
    
    private JPopupMenu tblPopup;
    
    private MenuViewListener menuViewListener;
    
    // Actions:
    private Action actSend, actShow, actDelete, actOptions, actExit, actAbout, actPhonebook, actReadme, actPoll, actFaxRead, actFaxSave, actForward, actAdminMode;
    private Action actRefresh;
    private ActionEnabler actChecker;
    
    
    public enum SendReadyState {
        Ready, NeedToWait, NotReady;
    }
    private SendReadyState sendReady = SendReadyState.NeedToWait;
    
    private static String _(String key) {
        return utils._(key);
    }
    
    // Worker classes:
    private class DeleteWorker extends ProgressWorker {
        private TooltipJTable selTable;
        
        @Override
        protected int calculateMaxProgress() {
            return 20 + 10*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            int[] selRows =  selTable.getSelectedRows();

            for (int i : selRows) {
                YajJob yj = null;
                try {
                    yj = selTable.getJobForRow(i);
                    updateNote(MessageFormat.format("Deleting fax {0}", yj.getIDValue()));
                    
                    yj.delete(hyfc);
                    
                    stepProgressBar(10);
                } catch (Exception e1) {
                    String msgText;
                    if (yj == null)
                        msgText = _("Error deleting a fax:\n");
                    else
                        msgText = MessageFormat.format(_("Error deleting the fax \"{0}\":\n"), yj.getIDValue());
                    //JOptionPane.showMessageDialog(mainwin.this, msgText + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    showExceptionDialog(msgText, e1);
                }
            }
        }
        @Override
        protected void done() {
            refreshTables();
        }
        
        public DeleteWorker(TooltipJTable selTable) {
            this.selTable = selTable;
        }
    }
    private class MultiSaveWorker extends ProgressWorker {
        private TooltipJTable selTable;
        private File targetDir;
        private int fileCounter;
        
        @Override
        protected int calculateMaxProgress() {
            return 1000*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            fileCounter = 0;
            
            int[] selRows =  selTable.getSelectedRows();
            
            for (int i : selRows) {
                YajJob yj = null;
                try {
                    yj = selTable.getJobForRow(i);
                    updateNote(MessageFormat.format(_("Saving fax {0}"), yj.getIDValue()));
                    for(HylaServerFile hsf : yj.getServerFilenames(hyfc)) {
                        try {
                            String filename = hsf.getPath();
                            int seppos = filename.lastIndexOf('/');
                            if (seppos < 0)
                                seppos = filename.lastIndexOf(File.separatorChar);
                            if (seppos >= 0)
                                filename = filename.substring(seppos + 1);
                            
                            File target = new File(targetDir, filename);
                            hsf.download(hyfc, target);
                            fileCounter++;
                        } catch (Exception e1) {
                            //JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
                            showExceptionDialog(MessageFormat.format(_("An error occured saving the file {0} (job {1}):"), hsf.getPath(), yj.getIDValue()), e1);
                        }
                    }
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(mainwin.this, _("An error occured saving the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    showExceptionDialog(_("An error occured saving the fax:"), e1);
                }
                stepProgressBar(1000);
            }
        }
        
        @Override
        protected void pMonClosed() {
            JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("{0} files saved to directory {1}."), fileCounter, targetDir.getPath()), _("Faxes saved"), JOptionPane.INFORMATION_MESSAGE);
        }
        
        public MultiSaveWorker(TooltipJTable selTable, File targetDir) {
            this.selTable = selTable;
            this.targetDir = targetDir;
        }
    }
    private class ShowWorker extends ProgressWorker {
        private TooltipJTable selTable;
        private int sMin, sMax;
        
        @Override
        protected int calculateMaxProgress() {
            return 100 + 1100*selTable.getSelectedRowCount();
        }
        
        @Override
        public void doWork() {
            int[] selRows =  selTable.getSelectedRows();
            sMin = Integer.MAX_VALUE; sMax = Integer.MIN_VALUE;
            for (int i : selRows) {
                YajJob yj = null;
                try {
                    yj = selTable.getJobForRow(i);
                    updateNote(MessageFormat.format(_("Displaying fax {0}"), yj.getIDValue()));
                    
                    //System.out.println("" + i + ": " + yj.getIDValue().toString());
                    List<HylaServerFile> serverFiles = yj.getServerFilenames(hyfc);

                    stepProgressBar(100);
                    if (serverFiles.size() == 0) {
                        showMessageDialog(MessageFormat.format(_("No document files available for the fax \"{0}\"."), yj.getIDValue()), _("Display fax"), JOptionPane.INFORMATION_MESSAGE);

                       stepProgressBar(1000);
                    } else {
                        int step = 1000 / serverFiles.size();
                        for(HylaServerFile hsf : serverFiles) {
                            try {
                                hsf.view(hyfc, myopts);
                                //System.out.println(hsf.getPath());
                            } catch (Exception e1) {
                                //JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("An error occured displaying the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
                                ExceptionDialog.showExceptionDialog(mainwin.this, MessageFormat.format(_("An error occured displaying the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()), e1);
                            }
                            stepProgressBar(step);
                        }
                    }
                    if (yj instanceof RecvYajJob) {
                        ((RecvYajJob)yj).setRead(true);
                        if (i < sMin)
                            sMin = i;
                        if (i > sMax)
                            sMax = i;
                    }
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("An error occured displaying the fax \"{0}\":\n"), yj.getIDValue()) + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    showExceptionDialog(MessageFormat.format(_("An error occured displaying the fax \"{0}\":"), yj.getIDValue()), e1);
                }
            }
        }
        
        @Override
        protected void done() {
            if (sMax >= 0 && selTable == TableRecv) {
                TableRecv.getSorter().fireTableRowsUpdated(sMin, sMax);
                actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, true);
            }
        }
        
        public ShowWorker(TooltipJTable selTable) {
            this.selTable = selTable;
        }
    }
    
    // Creates all actions:
    private void createActions(boolean adminState) {
        actOptions = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SendReadyState oldState = sendReady;
                sendReady = SendReadyState.NeedToWait;
                
                OptionsWin ow = new OptionsWin(myopts, mainwin.this);
                ow.setModal(true);
                ow.setVisible(true);
                if (ow.getModalResult()) 
                    reconnectToServer();
                else
                    sendReady = oldState;
                    
            }
        };
        actOptions.putValue(Action.NAME, _("Options") + "...");
        actOptions.putValue(Action.SHORT_DESCRIPTION, _("Shows the Options dialog"));
        actOptions.putValue(Action.SMALL_ICON, utils.loadIcon("general/Preferences"));
        
        actSend = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SendWin sw = new SendWin(hyfc, mainwin.this);
                sw.setModal(true);
                sw.setVisible(true);
                refreshTables();
            }
        };
        actSend.putValue(Action.NAME, _("Send") + "...");
        actSend.putValue(Action.SHORT_DESCRIPTION, _("Shows the send fax dialog"));
        actSend.putValue(Action.SMALL_ICON, utils.loadIcon("general/SendMail"));
        
        actPoll = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                SendWin sw = new SendWin(hyfc, mainwin.this, true);
                sw.setModal(true);
                sw.setVisible(true);
            }
        };
        actPoll.putValue(Action.NAME, _("Poll") + "...");
        actPoll.putValue(Action.SHORT_DESCRIPTION, _("Shows the poll fax dialog"));
        actPoll.putValue(Action.SMALL_ICON, utils.loadIcon("general/Import"));
        
        actDelete = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TooltipJTable selTable = (TooltipJTable)((JScrollPane)TabMain.getSelectedComponent()).getViewport().getView();
                
                String msgText;
                
                if (selTable == TableSending)
                    msgText = _("Do you really want to cancel the selected fax jobs?");
                else
                    msgText = _("Do you really want to delete the selected faxes?");
                
                if (JOptionPane.showConfirmDialog(mainwin.this, msgText, _("Delete faxes"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    DeleteWorker wrk = new DeleteWorker(selTable);
                    wrk.startWork(mainwin.this, _("Deleting faxes"));
                }
            };
            
        };
        actDelete.putValue(Action.NAME, _("Delete"));
        actDelete.putValue(Action.SHORT_DESCRIPTION, _("Deletes the selected fax"));
        actDelete.putValue(Action.SMALL_ICON, utils.loadIcon("general/Delete"));
        
        actShow = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                TooltipJTable selTable = (TooltipJTable)((JScrollPane)TabMain.getSelectedComponent()).getViewport().getView();
                
                ShowWorker wrk = new ShowWorker(selTable);
                wrk.startWork(mainwin.this, _("Viewing faxes"));
            }
        };
        actShow.putValue(Action.NAME, _("Show") + "...");
        actShow.putValue(Action.SHORT_DESCRIPTION, _("Displays the selected fax"));
        actShow.putValue(Action.SMALL_ICON, utils.loadIcon("general/Zoom"));
        
        actExit = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                //System.exit(0);
            }
        };
        actExit.putValue(Action.NAME, _("Exit"));
        actExit.putValue(Action.SHORT_DESCRIPTION, _("Exits the application"));
        actExit.putValue(Action.SMALL_ICON, utils.loadIcon("general/Stop"));
        
        actAbout = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(aboutMenuItem.getComponent(), utils.AppName + "\n\n" + _("by Jonas Wolz"), _("About"), JOptionPane.INFORMATION_MESSAGE);
                AboutDialog aDlg = new AboutDialog(mainwin.this);
                aDlg.setMode(AboutDialog.Mode.ABOUT);
                aDlg.setVisible(true);
            }
        };
        actAbout.putValue(Action.NAME, _("About") +  "...");
        actAbout.putValue(Action.SHORT_DESCRIPTION, _("Shows the about dialog"));
        actAbout.putValue(Action.SMALL_ICON, utils.loadIcon("general/About"));
        
        actPhonebook = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                PhoneBookWin pbw = new PhoneBookWin(mainwin.this);
                pbw.setModal(true);
                pbw.setVisible(true);
            }
        };
        actPhonebook.putValue(Action.NAME, _("Phone book") +  "...");
        actPhonebook.putValue(Action.SHORT_DESCRIPTION, _("Display/edit the phone book"));
        actPhonebook.putValue(Action.SMALL_ICON, utils.loadIcon("general/Bookmarks"));
        
        actReadme = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                AboutDialog aDlg = new AboutDialog(mainwin.this);
                aDlg.setMode(AboutDialog.Mode.READMES);
                aDlg.setVisible(true);
            }
        };
        actReadme.putValue(Action.NAME, _("Documentation") +  "...");
        actReadme.putValue(Action.SHORT_DESCRIPTION, _("Shows the README files"));
        actReadme.putValue(Action.SMALL_ICON, utils.loadIcon("general/Help"));
        
        actFaxRead = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = true;
                else
                    newState = !state;
                
                if (TabMain.getSelectedComponent() == scrollRecv) { // TableRecv
                    int sMin = Integer.MAX_VALUE, sMax = Integer.MIN_VALUE;
                    for (int i:TableRecv.getSelectedRows()) {
                        ((RecvYajJob)TableRecv.getJobForRow(i)).setRead(newState);
                        if (i < sMin)
                            sMin = i;
                        if (i > sMax)
                            sMax = i;
                    }
                    if (sMax >= 0)
                        TableRecv.getSorter().fireTableRowsUpdated(sMin, sMax);
                    actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, newState);
                }
            };
        };
        actFaxRead.putValue(Action.NAME, _("Marked as read"));
        actFaxRead.putValue(Action.SHORT_DESCRIPTION, _("Marks the selected fax as read/unread"));
        actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, true);
        
        actFaxSave = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TooltipJTable selTable = (TooltipJTable)((JScrollPane)TabMain.getSelectedComponent()).getViewport().getView();
                
                if (selTable.getSelectedRowCount() == 1) {
                    JFileChooser jfc = new JFileChooser();
                    
                    try {
                        YajJob yj = selTable.getJobForRow(selTable.getSelectedRow());
                        List<HylaServerFile> serverFiles = yj.getServerFilenames(hyfc);
                        if (serverFiles.size() == 0) {
                            JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("No document files available for the fax \"{0}\"."), yj.getIDValue()), _("Display fax"), JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            for(HylaServerFile hsf : serverFiles) {
                                try {
                                    String filename = hsf.getPath();
                                    int seppos = filename.lastIndexOf('/');
                                    if (seppos < 0)
                                        seppos = filename.lastIndexOf(File.separatorChar);
                                    if (seppos >= 0)
                                        filename = filename.substring(seppos + 1);
                                    jfc.setDialogTitle(MessageFormat.format(_("Save {0} to"), hsf.getPath()));
                                    jfc.setSelectedFile(new File(filename));
                                    
                                    jfc.resetChoosableFileFilters();
                                    ExampleFileFilter curFilter = new ExampleFileFilter(hsf.getType().getDefaultExtension(), hsf.getType().getDescription());
                                    jfc.addChoosableFileFilter(curFilter);
                                    jfc.setFileFilter(curFilter);
                                    
                                    if (jfc.showSaveDialog(mainwin.this) == JFileChooser.APPROVE_OPTION)
                                        hsf.download(hyfc, jfc.getSelectedFile());
                                } catch (Exception e1) {
                                    //JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
                                    ExceptionDialog.showExceptionDialog(mainwin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):"), hsf.getPath(), yj.getIDValue()), e1);
                                }                               
                            }
                        }
                    } catch (Exception e1) {
                        //JOptionPane.showMessageDialog(mainwin.this, _("An error occured saving the fax:\n")  + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                        ExceptionDialog.showExceptionDialog(mainwin.this, _("An error occured saving the fax:"), e1);
                    }
                } else {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setDialogTitle(_("Select a directory to save the faxes in"));
                    jfc.setApproveButtonText(_("Select"));
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (jfc.showOpenDialog(mainwin.this) == JFileChooser.APPROVE_OPTION) {
                        MultiSaveWorker wrk = new MultiSaveWorker(selTable, jfc.getSelectedFile());
                        wrk.startWork(mainwin.this, _("Saving faxes"));
                    }
                }
            };
        };
        actFaxSave.putValue(Action.NAME, _("Save fax..."));
        actFaxSave.putValue(Action.SHORT_DESCRIPTION, _("Saves the selected fax on disk"));
        actFaxSave.putValue(Action.SMALL_ICON, utils.loadIcon("general/SaveAs"));
        
        actForward = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (TabMain.getSelectedComponent() != scrollRecv || TableRecv.getSelectedRow() < 0)
                    return;
                
                String filename;
                try {
                    filename = TableRecv.getJobForRow(TableRecv.getSelectedRow()).getServerFilenames(hyfc).get(0).getPath();
                } catch (Exception e1) {
                    //JOptionPane.showMessageDialog(mainwin.this, _("Couldn't get a filename for the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                    ExceptionDialog.showExceptionDialog(mainwin.this, _("Couldn't get a filename for the fax:"), e1);
                    return;
                }
                
                SendWin sw = new SendWin(hyfc, mainwin.this);
                sw.setModal(true);
                sw.addServerFile(filename);
                sw.setVisible(true);
                refreshTables();
            }
        };
        actForward.putValue(Action.NAME, _("Forward fax..."));
        actForward.putValue(Action.SHORT_DESCRIPTION, _("Forwards the fax"));
        actForward.putValue(Action.SMALL_ICON, utils.loadIcon("general/Redo"));
        
        actAdminMode = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Boolean state = (Boolean)getValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY);
                boolean newState;
                if (state == null)
                    newState = false;
                else
                    newState = !state;

                actAdminMode.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, newState);
                
                reconnectToServer();
            };
        };
        actAdminMode.putValue(Action.NAME, _("Admin mode"));
        actAdminMode.putValue(Action.SHORT_DESCRIPTION, _("Connect to the server in admin mode (e.g. to delete faxes)"));
        actAdminMode.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, adminState);
        
        actRefresh = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                refreshStatus();
                refreshTables();
            };
        };
        actRefresh.putValue(Action.NAME, _("Refresh"));
        actRefresh.putValue(Action.SHORT_DESCRIPTION, _("Refresh tables and server status"));
        actRefresh.putValue(Action.SMALL_ICON, utils.loadIcon("general/Refresh"));
        actRefresh.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        
        actChecker = new ActionEnabler();
    }
    
    private JPopupMenu getTblPopup() {
        if (tblPopup == null) {
            tblPopup = new JPopupMenu(_("Fax"));
            tblPopup.add(new JMenuItem(actShow));
            tblPopup.add(new JMenuItem(actFaxSave));
            tblPopup.add(new JMenuItem(actDelete));
            tblPopup.addSeparator();
            tblPopup.add(new ActionJCheckBoxMenuItem(actFaxRead));
        }
        return tblPopup;
    }
    
    public void refreshTables() {
        tableRefresher.run();
    }
    
    public void refreshStatus() {
        statRefresher.run();
    }
    
    public SendReadyState getSendReadyState() {
        return sendReady;
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
                            src.setRowSelectionInterval(row, row);
                            getTblPopup().show(src,
                                    e.getX(), e.getY());
                        }
                    }
                }
                
                
            };
        }
        return tblMouseListener;
    }
    
    private KeyListener getTblKeyListener() {
        if (tblKeyListener == null) {
            tblKeyListener = new KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        actShow.actionPerformed(null);
                        break;
                    case KeyEvent.VK_DELETE:
                        actDelete.actionPerformed(null);
                        break;
                    }
                };
            };            
        }
        return tblKeyListener;
    }
    
    private DefaultTableCellRenderer getHylaDateRenderer() {
        if (hylaDateRenderer == null) {
            hylaDateRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value != null) {
                        int realCol = table.getColumnModel().getColumn(column).getModelIndex();
                        MyTableModel model = ((TooltipJTable)table).getRealModel();
                        value = model.columns.get(realCol).dateFormat.fmtOut.format(value);
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

            toolbar.add(actSend);
            toolbar.addSeparator();
            toolbar.add(actShow);
            toolbar.add(actDelete);
            toolbar.addSeparator();
            toolbar.add(actRefresh);
            toolbar.addSeparator();
            toolbar.add(actPhonebook);
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
            
            menuViewOwn = new JRadioButtonMenuItem(_("Only own faxes"));
            menuViewOwn.setActionCommand("view_own");
            menuViewOwn.addActionListener(menuViewListener);
            
            menuViewCustom = new JRadioButtonMenuItem(_("Custom filter..."));
            menuViewCustom.setActionCommand("view_custom");
            menuViewCustom.addActionListener(menuViewListener);
            
            viewGroup = new ButtonGroup();
            viewGroup.add(menuViewAll);
            viewGroup.add(menuViewOwn);
            viewGroup.add(menuViewCustom);
         
            menuView.add(menuViewAll);
            menuView.add(menuViewOwn);
            menuView.add(menuViewCustom);
            menuView.add(new JSeparator());
            menuView.add(new JMenuItem(actRefresh));
            
            getTabMain().addChangeListener(menuViewListener);
        }
        return menuView;
    }
    
    /**
     * This is the default constructor
     */
    public mainwin(boolean adminState) {
        super();
        initialize(adminState);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize(boolean adminState) {
        createActions(adminState);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getJJMenuBar());
        this.setSize(644, 466);
        this.setContentPane(getJContentPane());
        this.setTitle(utils.AppName);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            private boolean saved = false;
            
            public void windowClosing(java.awt.event.WindowEvent e) {
                sendReady = SendReadyState.NotReady;
                
                doLogout();
                menuViewListener.saveToOptions();
                myopts.mainWinBounds = getBounds();
                myopts.mainwinLastTab = getTabMain().getSelectedIndex();
                
                myopts.storeToFile(FaxOptions.getDefaultConfigFileName());
                saved = true;
                Launcher.releaseLock();
                System.exit(0);
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                if (!saved)
                    windowClosing(null);
            }
        });
        setIconImage(Toolkit.getDefaultToolkit().getImage(mainwin.class.getResource("icon.png")));
        
        MyInit();
        
        if (myopts.mainWinBounds != null)
            this.setBounds(myopts.mainWinBounds);
        else
            //this.setLocationByPlatform(true);
            utils.setDefWinPos(this);
        
        TabMain.setSelectedIndex(myopts.mainwinLastTab);
        actChecker.doEnableCheck();
        
    }

    
    private void MyInit() { 
        myopts = utils.getFaxOptions();
        
        utmrTable = new java.util.Timer("RefreshTimer", true);
        reloadTableColumnSettings();
        menuViewListener.loadFromOptions();
    }
    
    private void doLogout() {
        try {
            if (tableRefresher != null)
                tableRefresher.doCancel();
            if (statRefresher != null)
                statRefresher.doCancel();
            
            if (hyfc != null) {                
                myopts.recvColState = getTableRecv().getColumnCfgString();
                myopts.sentColState = getTableSent().getColumnCfgString();
                myopts.sendingColState = getTableSending().getColumnCfgString();
                
                //myopts.recvReadState = recvTableModel.getStateString();
                try {
                    recvTableModel.storeToStream(new FileOutputStream(utils.getConfigDir() + "recvread"));
                } catch (IOException e) {
                    System.err.println("Error storing read state: " + e.getMessage());
                }
                
                hyfc.quit();
            }
            //tmrStat.stop();
            //tmrTable.stop();

            
            getRecvTableModel().setData(null);
            getSentTableModel().setData(null);
            getSendingTableModel().setData(null);
            
            getTextStatus().setBackground(getDefStatusBackground());
            getTextStatus().setText(_("Disconnected."));
            
            menuView.setEnabled(false);
            this.setTitle("Disconnected - " + utils.AppName);
        }
        catch (Exception e) {
            e.printStackTrace();
            // do nothing
        }
    }
    
    private void reloadTableColumnSettings() {
        MyTableModel tm = getRecvTableModel();
        tm.columns = myopts.recvfmt;
        tm.fireTableStructureChanged();
        
        tm = getSentTableModel();
        tm.columns = myopts.sentfmt;
        tm.fireTableStructureChanged();

        tm = getSendingTableModel();
        tm.columns = myopts.sendingfmt;
        tm.fireTableStructureChanged();
        
        TableRecv.setColumnCfgString(myopts.recvColState);
        TableSent.setColumnCfgString(myopts.sentColState);
        TableSending.setColumnCfgString(myopts.sendingColState);
    }
    
    public void reconnectToServer() {
        sendReady = SendReadyState.NeedToWait;
        
        doLogout();
        
        if (myopts.host.length() == 0) { // Prompt for server if not set
            actOptions.actionPerformed(null);
            return;
        }
        
        hyfc = new HylaFAXClient();
        hyfc.setDebug(utils.debugMode);
        try {
            hyfc.open(myopts.host, myopts.port);
            
            while (hyfc.user(myopts.user)) {                
                    if (myopts.askPassword) {
                        
                        String pwd = PasswordDialog.showPasswordDialog(this, _("User password"), MessageFormat.format(_("Please enter the password for user \"{0}\"."), myopts.user));
                        if (pwd == null) { // User cancelled
                            hyfc.quit();
                            hyfc = null;
                            return;
                        } else
                            try {
                                hyfc.pass(pwd);
                                //repeatAsk = false;
                                break;
                            } catch (ServerResponseException e) {
                                ExceptionDialog.showExceptionDialog(this, _("An error occured in response to the password:"), e);
                                //repeatAsk = true;
                            }
                    } else {
                        hyfc.pass(myopts.pass);
                        break;
                    }
            } 
            
            this.setTitle(myopts.user + "@" + myopts.host + " - " + utils.AppName  );
            
            if ((Boolean)actAdminMode.getValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY)) {
                boolean authOK = false;
                if (myopts.askAdminPassword) {
                    do {
                        String pwd = PasswordDialog.showPasswordDialog(this, _("Admin password"), MessageFormat.format(_("Please enter the administrative password for user \"{0}\"."), myopts.user));
                        if (pwd == null) { // User cancelled
                            break; //Continue in "normal" mode
                        } else
                            try {
                                hyfc.admin(pwd);
                                authOK = true;
                            } catch (ServerResponseException e) {
                                ExceptionDialog.showExceptionDialog(this, _("An error occured in response to the password:"), e);
                                authOK = false;
                            }
                    } while (!authOK);
                } else {
                    hyfc.admin(myopts.AdminPassword);
                    authOK = true; // No error => authOK
                }
                if (authOK) {
                    // A reddish gray
                    Color defStatusBackground = getDefStatusBackground();
                    TextStatus.setBackground(new Color(Math.min(defStatusBackground.getRed() + 40, 255), defStatusBackground.getGreen(), defStatusBackground.getBlue()));
                    this.setTitle(myopts.user + "@" + myopts.host + " (admin) - " +utils.AppName);
                } else
                    actAdminMode.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, false);
            }
            
            hyfc.setPassive(myopts.pasv);
            hyfc.tzone(myopts.tzone.type);
            
            reloadTableColumnSettings();
            hyfc.rcvfmt(utils.VectorToString(myopts.recvfmt, "|"));
            
            menuView.setEnabled(true);
            // Re-check menu View state:
            menuViewListener.reConnected();
            
            // Multi-threaded implementation of the periodic refreshes.
            // I hope I didn't introduce too many race conditions/deadlocks this way
            statRefresher = new StatusRefresher();
            utmrTable.schedule(statRefresher, 0, myopts.statusUpdateInterval);
            
            tableRefresher = new TableRefresher(utils.VectorToString(myopts.sentfmt, "|"), utils.VectorToString(myopts.sendingfmt, "|"));
            // Read the read/unread status *after* the table contents has been set 
            tableRefresher.run();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        recvTableModel.readFromStream(new FileInputStream(utils.getConfigDir() + "recvread"));
                    } catch (FileNotFoundException e) { 
                        // No file yet - keep empty
                    } catch (IOException e) {
                        System.err.println("Error reading read status: " + e.getMessage());
                    } 
                }
            });
            utmrTable.schedule(tableRefresher, 0, myopts.tableUpdateInterval);
            
            sendReady = SendReadyState.Ready;
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(this, _("An error occured connecting to the server:") + "\n" + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            hyfc = null;
            ExceptionDialog.showExceptionDialog(this, _("An error occured connecting to the server:"), e);
            //actOptions.actionPerformed(null);
            sendReady = SendReadyState.NotReady;
        }
        
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
            
            Box box = Box.createVerticalBox();
            box.add(getTabMain());
            box.add(getTextStatus());
            
            jContentPane.add(box, BorderLayout.CENTER);
            jContentPane.add(getToolbar(), BorderLayout.NORTH);
        }
        return jContentPane;
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

    /**
     * This method initializes jMenu	
     * 	
     * @return javax.swing.JMenu	
     */
    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setText(_("Help"));
            helpMenu.add(new JMenuItem(actReadme));
            helpMenu.addSeparator();
            helpMenu.add(getAboutMenuItem());
        }
        return helpMenu;
    }

    /**
     * This method initializes jMenuItem	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getExitMenuItem() {
        if (exitMenuItem == null) {
            exitMenuItem = new JMenuItem();
            exitMenuItem.setAction(actExit);
        }
        return exitMenuItem;
    }

    /**
     * This method initializes jMenuItem	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getAboutMenuItem() {
        if (aboutMenuItem == null) {
            aboutMenuItem = new JMenuItem();
            aboutMenuItem.setAction(actAbout);
        }
        return aboutMenuItem;
    }

    /**
     * This method initializes jTabbedPane	
     * 	
     * @return javax.swing.JTabbedPane	
     */
    private JTabbedPane getTabMain() {
        if (TabMain == null) {
            TabMain = new JTabbedPane();
            TabMain.addTab(_("Received"), null, getScrollRecv(), _("Received faxes"));
            TabMain.addTab(_("Sent"), null, getScrollSent(), _("Sent faxes"));
            TabMain.addTab(_("Transmitting"), null, getScrollSending(), _("Faxes in the output queue"));
            
            TabMain.addChangeListener(actChecker);
        }
        return TabMain;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getScrollRecv() {
        if (scrollRecv == null) {
            scrollRecv = new JScrollPane();
            scrollRecv.setViewportView(getTableRecv());
        }
        return scrollRecv;
    }

    /**
     * This method initializes jTable	
     * 	
     * @return javax.swing.JTable	
     */
    private TooltipJTable getTableRecv() {
        if (TableRecv == null) {
            TableRecv = new TooltipJTable(getRecvTableModel());
            TableRecv.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            //TableRecv.setModel(getRecvTableModel());
            TableRecv.setShowGrid(true);
            TableRecv.addMouseListener(getTblMouseListener());
            TableRecv.addKeyListener(getTblKeyListener());
            TableRecv.getSelectionModel().addListSelectionListener(actChecker);
            TableRecv.setDefaultRenderer(Date.class, getHylaDateRenderer());
            
            recvTableModel.unreadFont = TableRecv.getFont().deriveFont(Font.BOLD);
        }
        return TableRecv;
    }

    private Color getDefStatusBackground() {
        Color rv;
        rv = UIManager.getColor("control");
        if (rv == null)
            rv = new Color(230, 230, 230);
        return rv;
    }
    
    /**
     * This method initializes jTextPane	
     * 	
     * @return javax.swing.JTextPane	
     */
    private JTextPane getTextStatus() {
        if (TextStatus == null) {
            TextStatus = new JTextPane() {
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
            TextStatus.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

            TextStatus.setBackground(getDefStatusBackground());
            TextStatus.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
            TextStatus.setEditable(false);
        }
        return TextStatus;
    }

    /**
     * This method initializes MyTableModel	
     * 	
     */
    private UnReadMyTableModel getRecvTableModel() {
        if (recvTableModel == null) {
            recvTableModel = new UnReadMyTableModel();
            recvTableModel.addUnreadItemListener(new UnreadItemListener() {
                public void newItemsAvailable(UnreadItemEvent evt) {
                    if (evt.isOldDataNull())
                        return;
                    
                    if ((myopts.newFaxAction & utils.NEWFAX_TOFRONT) != 0) {
                        int state = getExtendedState();
                        if ((state & mainwin.ICONIFIED) != 0) 
                            setExtendedState(state & (~mainwin.ICONIFIED));
                        toFront();
                    }
                    if ((myopts.newFaxAction & utils.NEWFAX_BEEP) != 0) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                    if ((myopts.newFaxAction & utils.NEWFAX_VIEWER) != 0) {
                        for (RecvYajJob j : evt.getItems()) {
                            for (HylaServerFile hsf : j.getServerFilenames(hyfc)) {
                                try {
                                    hsf.view(hyfc, myopts);
                                } catch (Exception e) {
                                    if (utils.debugMode) {
                                        System.out.println("Exception while trying to view new faxes:");
                                        e.printStackTrace(System.out);
                                    }
                                }
                            }
                            if ((myopts.newFaxAction & utils.NEWFAX_MARKASREAD) != 0) {
                                j.setRead(true);
                            }
                        }
                    }
                };
            });
        }
        return recvTableModel;
    }

    /**
     * This method initializes jScrollPane1	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getScrollSent() {
        if (scrollSent == null) {
            scrollSent = new JScrollPane();
            scrollSent.setViewportView(getTableSent());
        }
        return scrollSent;
    }

    /**
     * This method initializes jTable	
     * 	
     * @return javax.swing.JTable	
     */
    private TooltipJTable getTableSent() {
        if (TableSent == null) {
            TableSent = new TooltipJTable(getSentTableModel());
            TableSent.setShowGrid(true);
            //TableSent.setModel(getSentTableModel());
            TableSent.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            TableSent.getSelectionModel().addListSelectionListener(actChecker);
            TableSent.addMouseListener(getTblMouseListener());
            TableSent.addKeyListener(getTblKeyListener());
            TableSent.setDefaultRenderer(Date.class, getHylaDateRenderer());
        }
        return TableSent;
    }

    /**
     * This method initializes MyTableModel	
     * 	
     */
    private MyTableModel getSentTableModel() {
        if (sentTableModel == null) {
            sentTableModel = new MyTableModel();
        }
        return sentTableModel;
    }

    /**
     * This method initializes jScrollPane2	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getScrollSending() {
        if (scrollSending == null) {
            scrollSending = new JScrollPane();
            scrollSending.setViewportView(getTableSending());
        }
        return scrollSending;
    }

    /**
     * This method initializes jTable	
     * 	
     * @return javax.swing.JTable	
     */
    private TooltipJTable getTableSending() {
        if (TableSending == null) {
            TableSending = new TooltipJTable(getSendingTableModel());
            TableSending.setShowGrid(true);
            //TableSending.setModel(getSendingTableModel());
            TableSending.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            TableSending.getSelectionModel().addListSelectionListener(actChecker);
            TableSending.addMouseListener(getTblMouseListener());
            TableSending.addKeyListener(getTblKeyListener());
            TableSending.setDefaultRenderer(Date.class, getHylaDateRenderer());
        }
        return TableSending;
    }

    /**
     * This method initializes MyTableModel	
     * 	
     */
    private MyTableModel getSendingTableModel() {
        if (sendingTableModel == null) {
            sendingTableModel = new MyTableModel() {
                @Override
                protected YajJob createYajJob(String[] data) {
                    return new SendingYajJob(this.columns, data);
                }
            };
        }
        return sendingTableModel;
    }

    /**
     * This method initializes jMenu	
     * 	
     * @return javax.swing.JMenu	
     */
    private JMenu getMenuFax() {
        if (menuFax == null) {
            menuFax = new JMenu();
            menuFax.setText(_("Fax"));
            menuFax.add(getSendMenuItem());
            menuFax.add(new JMenuItem(actPoll));
            menuFax.add(new JMenuItem(actForward));
            menuFax.addSeparator();
            menuFax.add(getShowMenuItem());
            menuFax.add(new JMenuItem(actFaxSave));
            menuFax.add(getDeleteMenuItem());
            menuFax.addSeparator();
            menuFax.add(new ActionJCheckBoxMenuItem(actFaxRead));
            menuFax.addSeparator();
            menuFax.add(getExitMenuItem());
        }
        return menuFax;
    }

    private JMenu getMenuExtras() {
        if (menuExtras == null) {
            menuExtras = new JMenu(_("Extras"));
            menuExtras.add(actPhonebook);
            menuExtras.addSeparator();
            menuExtras.add(getOptionsMenuItem());
            menuExtras.addSeparator();
            menuExtras.add(new ActionJCheckBoxMenuItem(actAdminMode));
        }
        return menuExtras;
    }
    /**
     * This method initializes jMenuItem	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getShowMenuItem() {
        if (ShowMenuItem == null) {
            ShowMenuItem = new JMenuItem();
            ShowMenuItem.setAction(actShow);
        }
        return ShowMenuItem;
    }

    /**
     * This method initializes jMenuItem1	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getDeleteMenuItem() {
        if (DeleteMenuItem == null) {
            DeleteMenuItem = new JMenuItem();
            DeleteMenuItem.setAction(actDelete);
        }
        return DeleteMenuItem;
    }

    /**
     * This method initializes jMenuItem	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getSendMenuItem() {
        if (SendMenuItem == null) {
            SendMenuItem = new JMenuItem();
            SendMenuItem.setAction(actSend);
        }
        return SendMenuItem;
    }
    
    /**
     * This method initializes jMenuItem1   
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getOptionsMenuItem() {
        if (optionsMenuItem == null) {
            optionsMenuItem = new JMenuItem();
            //optionsMenuItem.setText(_("Options") + "...");
            optionsMenuItem.setAction(actOptions);
        }
        return optionsMenuItem;
    }
    
    class MenuViewListener implements ActionListener, ChangeListener {
        private JRadioButtonMenuItem[] lastSel = new JRadioButtonMenuItem[3];
        
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            MyTableModel model = ((TooltipJTable)((JScrollPane)TabMain.getSelectedComponent()).getViewport().getView()).getRealModel();
            int selTab = TabMain.getSelectedIndex();
            
            if (cmd.equals("view_all")) {
                model.setJobFilter(null);
                lastSel[selTab] = menuViewAll;
            } else if (cmd.equals("view_own")) {
                model.setJobFilter(getOwnFilterFor(model));
                lastSel[selTab] = menuViewOwn;
            } else if (cmd.equals("view_custom")) {
                CustomFilterDialog cfd = new CustomFilterDialog(mainwin.this, TabMain.getTitleAt(selTab), 
                        model.columns, (lastSel[selTab] == menuViewCustom) ? model.getJobFilter() : null);
                cfd.setVisible(true);
                if (cfd.returnValue != null) {
                    model.setJobFilter(cfd.returnValue);
                    lastSel[selTab] = menuViewCustom;
                } else {
                    if (lastSel[selTab] != menuViewCustom)
                            resetLastSel(selTab);
                }
            }
        }
        
        private void resetLastSel(int selTab) {
            if (lastSel[selTab] != null)
                lastSel[selTab].setSelected(true);
            else 
                menuViewAll.setSelected(true);
        }
        
        public void stateChanged(ChangeEvent e) {
            MyTableModel model = ((TooltipJTable)((JScrollPane)TabMain.getSelectedComponent()).getViewport().getView()).getRealModel();
            boolean viewOwnState  = ownFilterOK(model);
            
            resetLastSel(TabMain.getSelectedIndex());
            menuViewOwn.setEnabled(viewOwnState);
            if ((!viewOwnState && menuViewOwn.isSelected())) {
                menuViewAll.setSelected(true);
                model.setJobFilter(null);
            }
        }
        
        private YajJobFilter getOwnFilterFor(MyTableModel model) {
            return new StringFilter((model == recvTableModel ? utils.recvfmt_Owner : utils.jobfmt_Owner), StringFilterOperator.EQUAL, myopts.user);
        }
        
        private boolean ownFilterOK(MyTableModel model) {
            if (model == recvTableModel) { 
                return myopts.recvfmt.contains(utils.recvfmt_Owner);
            } else if (model == sentTableModel || model == sendingTableModel) { 
                return model.columns.contains(utils.jobfmt_Owner);
            } else
                return false;
        }
        /**
         * Re-validates the filters on reconnection
         */
        public void reConnected() {
            for (int i = 0; i < lastSel.length; i++) {
                MyTableModel model = ((TooltipJTable)((JScrollPane)TabMain.getComponent(i)).getViewport().getView()).getRealModel();
                if (lastSel[i] == menuViewOwn) {
                    if (ownFilterOK(model)) 
                        model.setJobFilter(getOwnFilterFor(model));
                    else {
                        lastSel[i] = menuViewAll;
                        model.setJobFilter(null);
                    }
                } else if (lastSel[i] == menuViewCustom) {
                    if (!model.getJobFilter().validate(model.columns)) {
                        lastSel[i] = menuViewAll;
                        model.setJobFilter(null);
                    }
                    
                } else if (lastSel[i] == menuViewAll) 
                    model.setJobFilter(null);
            }
            stateChanged(null);
        }
        

        private void loadSaveString(int idx, String data) {
            if ((data == null) || data.equals("A")) {
                lastSel[idx] = menuViewAll;
            } else if (data.equals("O")) {
                lastSel[idx] = menuViewOwn;
            } else if (data.startsWith("C")) {
                MyTableModel model = ((TooltipJTable)((JScrollPane)TabMain.getComponent(idx)).getViewport().getView()).getRealModel();
                YajJobFilter yjf = FilterCreator.stringToFilter(data.substring(1), model.columns);
                if (yjf == null) {
                    lastSel[idx] = menuViewAll;
                } else {
                    lastSel[idx] = menuViewCustom;
                    model.setJobFilter(yjf);
                }
            }
        }
        
        public void loadFromOptions() {
            loadSaveString(0, myopts.recvFilter);
            loadSaveString(1, myopts.sentFilter);
            loadSaveString(2, myopts.sendingFilter);
            reConnected();
        }
        
        private String getSaveString(int idx) {
            if (lastSel[idx] == null || lastSel[idx] == menuViewAll) {
                return "A";
            } else if (lastSel[idx] == menuViewOwn) {
                return "O";
            } else if (lastSel[idx] == menuViewCustom) {
                MyTableModel model = ((TooltipJTable)((JScrollPane)TabMain.getComponent(idx)).getViewport().getView()).getRealModel();
                return "C" + FilterCreator.filterToString(model.getJobFilter());
            } else
                return null;
        }
        
        public void saveToOptions() {
            myopts.recvFilter = getSaveString(0);
            myopts.sentFilter = getSaveString(1);
            myopts.sendingFilter = getSaveString(2);
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
            
            if (TabMain.getSelectedComponent() == scrollRecv) { // Received Table active
                if (TableRecv.getSelectedRow() >= 0) {
                    showState = true;
                    deleteState = true;
                    faxReadState = true;
                    faxReadSelected = ((RecvYajJob)TableRecv.getJobForRow(TableRecv.getSelectedRow())).isRead();
                }
            } else if (TabMain.getSelectedComponent() == scrollSent) { // Sent Table
                if (TableSent.getSelectedRow() >= 0) {
                    deleteState = true;
                    showState = true;
                }
            } else if (TabMain.getSelectedComponent() == scrollSending) { // Sending Table
                if (TableSending.getSelectedRow() >= 0) {
                    deleteState = true;
                    showState = true;
                }
            } 
            
            actShow.setEnabled(showState);
            actFaxSave.setEnabled(showState);
            actDelete.setEnabled(deleteState);
            actFaxRead.setEnabled(faxReadState);
            actForward.setEnabled(faxReadState);
            actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, faxReadSelected);
        }
    }
    
    class StatusRefresher extends TimerTask {
        String text = "";
        private Runnable statRunner;
        private boolean cancelled = false;
        
        public synchronized boolean doCancel() {
            cancelled = true;
            return cancel();
        }

        public synchronized void run() {
            if (cancelled)
                return;
            try {
                String newText = utils.VectorToString(hyfc.getList("status"), "\n");
                if (!newText.equals(text)) {
                    text = newText;
                    SwingUtilities.invokeLater(statRunner);
                }
            } catch (Exception e) {
                System.err.println("Error refreshing the status: " + e.getMessage());
            }
        }
        
        public StatusRefresher() {
            statRunner = new Runnable() {
                public void run() {
                    TextStatus.setText(text);
                }
            };
        }
    };
    
    class TableRefresher extends TimerTask {
        private String sentfmt, sendingfmt;
        private Vector lastRecvList = null, lastSentList = null, lastSendingList = null;
        private boolean cancelled = false;
        
        public synchronized boolean doCancel() {
            cancelled = true;
            return cancel();
        }
        
        public synchronized void run() {
            if (cancelled)
                return;
            
            Vector lst;
            try {
                lst = hyfc.getList("recvq");
                if ((lastRecvList == null) || !lst.equals(lastRecvList)) {
                    String[][] data = new String[lst.size()][];
                    for (int i = 0; i < lst.size(); i++)
                        data[i] = ((String)lst.get(i)).split("\\|");
                    SwingUtilities.invokeLater(new TableDataRunner(recvTableModel, data));
                    lastRecvList = lst;
                }
            } catch (Exception e) {
                System.err.println("An error occured refreshing the tables: " + e.getMessage());
            }        
            
            try {
                hyfc.jobfmt(sentfmt);
                lst = hyfc.getList("doneq");
                if ((lastSentList == null) || !lst.equals(lastSentList)) {
                    String[][] data = new String[lst.size()][];
                    for (int i = 0; i < lst.size(); i++) 
                        data[i] = ((String)lst.get(i)).split("\\|");
                    SwingUtilities.invokeLater(new TableDataRunner(sentTableModel, data));
                    lastSentList = lst;
                }
            } catch (Exception e) {
                System.err.println("An error occured refreshing the tables: " + e.getMessage());
            }
            
            try {
                hyfc.jobfmt(sendingfmt);
                lst = hyfc.getList("sendq");
                if ((lastSendingList == null) || !lst.equals(lastSendingList)) {
                    String[][] data = new String[lst.size()][];
                    for (int i = 0; i < lst.size(); i++)
                        data[i] = ((String)lst.get(i)).split("\\|");
                    SwingUtilities.invokeLater(new TableDataRunner(sendingTableModel, data));
                    lastSendingList = lst;
                }
            } catch (Exception e) {
                System.err.println("An error occured refreshing the tables: " + e.getMessage());
            }
        }
        
        public TableRefresher(String sentfmt, String sendingfmt) {
            this.sentfmt = sentfmt;
            this.sendingfmt = sendingfmt; 
        }
        
        class TableDataRunner implements Runnable {
            private String[][] data = null;
            private MyTableModel tm;
                    
            public void run() {
                tm.setData(data);         
            }
            
            public TableDataRunner(MyTableModel tm, String[][] data) {
                this.tm = tm;
                this.data = data;
            }
        }
    }
}  

class ActionJCheckBoxMenuItem extends JCheckBoxMenuItem {
    
    public static final String SELECTED_PROPERTY = "selected";
    
    @Override
    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new MyPropertyChangeListener(super.createActionPropertyChangeListener(a));
    }
    
    @Override
    protected void configurePropertiesFromAction(Action a) {
        Boolean selValue = (Boolean)a.getValue(SELECTED_PROPERTY);
        if (selValue != null)
            setSelected(selValue);
        
        super.configurePropertiesFromAction(a);
    }
    
    public ActionJCheckBoxMenuItem() {
        super();
    }
    
    public ActionJCheckBoxMenuItem(Action a) {
        super(a);
    }
    
    // Wrapper to update the Selected property as needed
    private class MyPropertyChangeListener implements PropertyChangeListener {
        private PropertyChangeListener orgPCL = null;
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(SELECTED_PROPERTY)) {
                setSelected((Boolean)evt.getNewValue());
            }
            orgPCL.propertyChange(evt);                
        }
        
        public MyPropertyChangeListener(PropertyChangeListener orgPCL) {
            this.orgPCL = orgPCL;
        }
    }
}
