package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;


public class mainwin extends JFrame {
    
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
    private JMenu menuExtras = null;
    private JMenu helpMenu = null;
    
    private JMenuItem exitMenuItem = null;
    private JMenuItem aboutMenuItem = null;
    private JMenuItem optionsMenuItem = null;
    private JMenuItem ShowMenuItem = null;
    private JMenuItem DeleteMenuItem = null;
    private JMenuItem SendMenuItem = null;    
    
    HylaFAXClient hyfc = null;
    private FaxOptions myopts = null;
    
    //private javax.swing.Timer tmrStat;    
    private java.util.Timer utmrTable;
    private TableRefresher tableRefresher = null;
    private StatusRefresher statRefresher = null;
    
    private MouseListener tblMouseListener;
    private DefaultTableCellRenderer hylaDateRenderer;
    
    private JPopupMenu tblPopup;
    
    // Actions:
    private Action actSend, actShow, actDelete, actOptions, actExit, actAbout, actPhonebook, actReadme, actPoll, actFaxRead, actFaxSave, actForward;
    private ActionEnabler actChecker;
    
    private static String _(String key) {
        return utils._(key);
    }
    
    // Creates all actions:
    private void createActions() {
        actOptions = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                OptionsWin ow = new OptionsWin(myopts, mainwin.this);
                ow.setModal(true);
                ow.setVisible(true);
                if (ow.getModalResult()) 
                    ReloadSettings();
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
                    for (int i : selTable.getSelectedRows()) {
                        YajJob yj = null;
                        try {
                            yj = selTable.getJobForRow(i);
                            yj.delete(hyfc);
                        } catch (Exception e1) {
                            if (yj == null)
                                msgText = _("Error deleting a fax:\n");
                            else
                                msgText = MessageFormat.format(_("Error deleting the fax \"{0}\":\n"), yj.getIDValue());
                            //JOptionPane.showMessageDialog(mainwin.this, msgText + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                            ExceptionDialog.showExceptionDialog(mainwin.this, msgText, e1);
                        }
                    }
                }
            };
        };
        actDelete.putValue(Action.NAME, _("Delete"));
        actDelete.putValue(Action.SHORT_DESCRIPTION, _("Deletes the selected fax"));
        actDelete.putValue(Action.SMALL_ICON, utils.loadIcon("general/Delete"));
        
        actShow = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                TooltipJTable selTable = (TooltipJTable)((JScrollPane)TabMain.getSelectedComponent()).getViewport().getView();
                
                int sMin = Integer.MAX_VALUE, sMax = Integer.MIN_VALUE;
                for (int i : selTable.getSelectedRows()) {
                    YajJob yj = null;
                    try {
                        yj = selTable.getJobForRow(i);
                        List<HylaServerFile> serverFiles = yj.getServerFilenames(hyfc);
                        if (serverFiles.size() == 0) {
                            JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("No document files available for the fax \"{0}\"."), yj.getIDValue()), _("Display fax"), JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            for(HylaServerFile hsf : serverFiles) {
                                try {
                                    hsf.view(hyfc, myopts);
                                } catch (Exception e1) {
                                    //JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("An error occured displaying the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
                                    ExceptionDialog.showExceptionDialog(mainwin.this, MessageFormat.format(_("An error occured displaying the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()), e1);
                                }
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
                        ExceptionDialog.showExceptionDialog(mainwin.this, MessageFormat.format(_("An error occured displaying the fax \"{0}\":"), yj.getIDValue()), e1);
                    }
                }
                if (sMax >= 0 && selTable == TableRecv) {
                    TableRecv.getSorter().fireTableRowsUpdated(sMin, sMax);
                    actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, true);
                }
                
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
                                    ExampleFileFilter curFilter = new ExampleFileFilter(hsf.getType(), hsf.getType() + _(" files"));
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
                        int fileCounter = 0;
                        
                        for (int i : selTable.getSelectedRows()) {
                            YajJob yj = null;
                            try {
                                yj = selTable.getJobForRow(i);
                                for(HylaServerFile hsf : yj.getServerFilenames(hyfc)) {
                                    try {
                                        String filename = hsf.getPath();
                                        int seppos = filename.lastIndexOf('/');
                                        if (seppos < 0)
                                            seppos = filename.lastIndexOf(File.separatorChar);
                                        if (seppos >= 0)
                                            filename = filename.substring(seppos + 1);
                                        
                                        File target = new File(jfc.getSelectedFile(), filename);
                                        hsf.download(hyfc, target);
                                        fileCounter++;
                                    } catch (Exception e1) {
                                        //JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):\n"), hsf.getPath(), yj.getIDValue()) + e1.getMessage() , _("Error"), JOptionPane.ERROR_MESSAGE);
                                        ExceptionDialog.showExceptionDialog(mainwin.this, MessageFormat.format(_("An error occured saving the file {0} (job {1}):"), hsf.getPath(), yj.getIDValue()), e1);
                                    }
                                }
                                
                            } catch (Exception e1) {
                                //JOptionPane.showMessageDialog(mainwin.this, _("An error occured saving the fax:\n") + e1.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                                ExceptionDialog.showExceptionDialog(mainwin.this, _("An error occured saving the fax:"), e1);
                            }
                        }
                        
                        JOptionPane.showMessageDialog(mainwin.this, MessageFormat.format(_("{0} files saved to directory {1}."), fileCounter, jfc.getSelectedFile().getPath()), _("Faxes saved"), JOptionPane.INFORMATION_MESSAGE);
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
            }
        };
        actForward.putValue(Action.NAME, _("Forward fax..."));
        actForward.putValue(Action.SHORT_DESCRIPTION, _("Forwards the fax"));
        actForward.putValue(Action.SMALL_ICON, utils.loadIcon("general/Redo"));
        
        
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
            toolbar.add(actPhonebook);
        }
        return toolbar;
    }
    
    /**
     * This is the default constructor
     */
    public mainwin() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        createActions();
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getJJMenuBar());
        this.setSize(644, 466);
        this.setContentPane(getJContentPane());
        this.setTitle(utils.AppName);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            private boolean saved = false;
            
            public void windowClosing(java.awt.event.WindowEvent e) {
                doLogout();
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
            this.setLocationByPlatform(true);
        
        TabMain.setSelectedIndex(myopts.mainwinLastTab);
        actChecker.doEnableCheck();
        
    }

    
    /*private void ReloadTables() throws FileNotFoundException, IOException, ServerResponseException  {
        MyTableModel tm = getRecvTableModel();
        Vector lst = hyfc.getList("recvq");
     
        if ((lastRecvList == null) || !lst.equals(lastRecvList)) {
            String[][] data = new String[lst.size()][];
            for (int i = 0; i < lst.size(); i++)
                data[i] = ((String)lst.get(i)).split("\\|");
            tm.setData(data);
            lastRecvList = lst;
        }        
        
        tm = getSentTableModel();
        String fmt = utils.VectorToString(myopts.sentfmt, "|");
        hyfc.jobfmt(fmt);
        lst = hyfc.getList("doneq");
        if ((lastSentList == null) || !lst.equals(lastSentList)) {
            String[][] data = new String[lst.size()][];
            for (int i = 0; i < lst.size(); i++) 
                data[i] = ((String)lst.get(i)).split("\\|");
            tm.setData(data);
            lastSentList = lst;
        }
        
        tm = getSendingTableModel();
        fmt = utils.VectorToString(myopts.sendingfmt, "|");
        hyfc.jobfmt(fmt);
        lst = hyfc.getList("sendq");
        if ((lastSendingList == null) || !lst.equals(lastSendingList)) {
            String[][] data = new String[lst.size()][];
            for (int i = 0; i < lst.size(); i++)
                data[i] = ((String)lst.get(i)).split("\\|");
            tm.setData(data);  
            lastSendingList = lst;
        }
    }*/
    
    private void MyInit() { 
        myopts = utils.getFaxOptions();
        
        /*tmrStat = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getStatus();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            };
        });
        tmrStat.stop();*/
        
        /*tmrTable = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ReloadTables();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            };
        }); 
        tmrTable.stop();*/
        
        utmrTable = new java.util.Timer("RefreshTimer");
        ReloadSettings();
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
            
            getTextStatus().setText(_("Disconnected."));
        }
        catch (Exception e) {
            // do nothing
        }
    }
    
    private void ReloadSettings() {
        doLogout();
        
        if (myopts.host.length() == 0) { // Prompt for server if not set
            actOptions.actionPerformed(null);
            return;
        }
        
        hyfc = new HylaFAXClient();
        try {
            hyfc.open(myopts.host, myopts.port);
            
            if (hyfc.user(myopts.user)) 
                hyfc.pass(myopts.pass);
            
            hyfc.setPassive(myopts.pasv);
            hyfc.tzone(myopts.tzone.type);
            
            //getStatus();
            
            MyTableModel tm = getRecvTableModel();
            tm.columns = myopts.recvfmt;
            tm.fireTableStructureChanged();
            hyfc.rcvfmt(utils.VectorToString(myopts.recvfmt, "|"));
            
            tm = getSentTableModel();
            tm.columns = myopts.sentfmt;
            tm.fireTableStructureChanged();

            tm = getSendingTableModel();
            tm.columns = myopts.sendingfmt;
            tm.fireTableStructureChanged();
            
            TableRecv.setColumnCfgString(myopts.recvColState);
            TableSent.setColumnCfgString(myopts.sentColState);
            TableSending.setColumnCfgString(myopts.sendingColState);
            
            // Multi-threaded implementation of the periodic refreshes.
            // I hope I didn't introduce too much race conditions/deadlocks this way
            statRefresher = new StatusRefresher();
            utmrTable.schedule(statRefresher, 0, myopts.statusUpdateInterval);
            
            tableRefresher = new TableRefresher(utils.VectorToString(myopts.sentfmt, "|"), utils.VectorToString(myopts.sendingfmt, "|"));
            // Read the read/unread status after the table contents has been set 
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
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(this, _("An error occured connecting to the server:") + "\n" + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            ExceptionDialog.showExceptionDialog(this, _("An error occured connecting to the server:"), e);
            actOptions.actionPerformed(null);
        }
        
    }
    
    
    /*private void getStatus() throws FileNotFoundException, IOException, ServerResponseException {
        Vector stat = hyfc.getList("status");
        TextStatus.setText(utils.VectorToString(stat, "\n"));
    }*/
/*    
    private void showRecvFile(int row) {
        try {
            File tmptif = File.createTempFile("fax", ".tif");
            tmptif.deleteOnExit();
            
            FileOutputStream tmpout = new FileOutputStream(tmptif);
            
            hyfc.type(gnu.inet.ftp.FtpClientProtocol.TYPE_IMAGE);
            hyfc.get("recvq/" + getRecvFilename(row), tmpout);
            tmpout.close();
            
            
            String execCmd = myopts.faxViewer;
            
            if (execCmd.indexOf("%s") >= 0)
                execCmd = execCmd.replace("%s", tmptif.getPath());
            else
                execCmd += " " + tmptif.getPath();
            
            Runtime.getRuntime().exec(execCmd);
            
            recvSetRead(row, true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, _("Couldn't open the fax:") + "\n" + e.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE); 
        }
        
    }
    
    private void showJobFile(Job job) {
        try {
            
            String[] files = job.getDocumentName().split("\n");
            ArrayList<FaxStringProperty> availFiles = new ArrayList<FaxStringProperty>();
            
            // The last entry is "End of Documents"!
            for (int i = 0; i < files.length - 1; i++) {
                String[] fields = files[i].split("\\s");
                if (fields[0].equalsIgnoreCase("PS") || fields[0].equalsIgnoreCase("PDF")) {
                    try {
                        hyfc.stat(fields[1]); // will throw FileNotFoundException if file doesn't exist
                        availFiles.add(new FaxStringProperty(fields[1], fields[0])); // "abuse" FaxStringProperty to hold fileName <-> type "map"
                    } catch (FileNotFoundException e) {
                        // do nothing
                        //System.err.println(e.toString());
                    }
                } else
                    System.err.println(_("Unknown file format for file: ") + files[i]);
            }
            
            if (availFiles.size() == 0) {
                JOptionPane.showMessageDialog(this, _("No document files available for this job."), _("Show fax"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
                
            FaxStringProperty selFile = null;
            
            if (availFiles.size() > 1) { 
                
                selFile = (FaxStringProperty)JOptionPane.showInputDialog(this, _("Please select the file to display."), _("Show fax"), JOptionPane.INFORMATION_MESSAGE, null, availFiles.toArray(), availFiles.get(0));
            
                if (selFile == null)
                    return;
            } else
                selFile = availFiles.get(0);
            
            File tmpFile = File.createTempFile("fax", "." + selFile.type.toLowerCase());
            tmpFile.deleteOnExit();
            
            FileOutputStream tmpout = new FileOutputStream(tmpFile);
            
            hyfc.type(gnu.inet.ftp.FtpClientProtocol.TYPE_IMAGE);
            hyfc.get(selFile.desc, tmpout);
            tmpout.close();
            
            String execCmd = myopts.psViewer;
            
            if (execCmd.indexOf("%s") >= 0)
                execCmd = execCmd.replace("%s", tmpFile.getPath());
            else
                execCmd += " " + tmpFile.getPath();
            
            Runtime.getRuntime().exec(execCmd);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, _("Couldn't open the fax:") + "\n" + e.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE); 
        }
        
    }*/
    /*
    private void recvSetRead(int row, boolean state) {
        ((RecvYajJob)TableRecv.getRealModel().getJob(TableRecv.getSorter().modelIndex(row))).setRead(state);
        TableRecv.getSorter().fireTableRowsUpdated(row, row); // HACK to repaint table (calling it in the TableModel would remove the current selection)
        actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, state);
    }*/
    
   /* private String getRecvFilename(int row) {
        //return recvTableModel.getValueAt(row, myopts.recvfmt.indexOf(utils.recvfmt_FileName)).toString();
        return TableRecv.getModel().getValueAt(row, myopts.recvfmt.indexOf(utils.recvfmt_FileName)).toString().trim();
    }
    
    private Job getJob(int row, TooltipJTable table) {
        int col = table.getRealModel().columns.indexOf(utils.jobfmt_JobID);
        if (col < 0)
            return null;
        
        try {
            return hyfc.getJob((Integer)table.getModel().getValueAt(row, col));
        } catch (Exception e) {
            return null;
        }
    }*/
    
    ///////////
    
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
            TableRecv.getSelectionModel().addListSelectionListener(actChecker);
            TableRecv.setDefaultRenderer(Date.class, getHylaDateRenderer());
            
            recvTableModel.unreadFont = TableRecv.getFont().deriveFont(Font.BOLD);
        }
        return TableRecv;
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
            TextStatus.setBackground(UIManager.getColor("Label.backgroundColor"));
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
                    
                    if ((myopts.newFaxAction.type & utils.NEWFAX_TOFRONT) != 0) {
                        int state = getExtendedState();
                        if ((state & mainwin.ICONIFIED) != 0) 
                            setExtendedState(state & (~mainwin.ICONIFIED));
                        toFront();
                    }
                    if ((myopts.newFaxAction.type & utils.NEWFAX_BEEP) != 0) {
                        Toolkit.getDefaultToolkit().beep();
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
