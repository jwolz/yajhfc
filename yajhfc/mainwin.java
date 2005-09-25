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
import gnu.hylafax.Job;

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


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
    private Action actSend, actShow, actDelete, actOptions, actExit, actAbout, actPhonebook, actReadme, actPoll, actFaxRead;
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
                Component selComp = TabMain.getSelectedComponent();
                
                if ((selComp == scrollRecv) && (TableRecv.getSelectedRow() >= 0)) {
                    String fileName = getRecvFilename(TableRecv.getSelectedRow());
                    if (JOptionPane.showConfirmDialog(mainwin.this, MessageFormat.format(_("Do you want to delete the fax \"{0}\"?"), fileName), _("Delete fax"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            hyfc.dele("recvq/" + fileName);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(mainwin.this, _("Couldn't delete the fax: ") + "\n" + e1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if ((selComp == scrollSent) || (selComp == scrollSending)) {
                    TooltipJTable tbl = (selComp == scrollSent) ? TableSent : TableSending;
                    
                    try {
                        if (tbl.getSelectedRow() >= 0) {
                            Job job = getJob(tbl.getSelectedRow(), tbl);
                            if (job == null) {
                                JOptionPane.showMessageDialog(mainwin.this, _("Selected job could not be found!"), _("Error"), JOptionPane.WARNING_MESSAGE);
                                return;
                            }   
                            
                            if (JOptionPane.showConfirmDialog(mainwin.this, MessageFormat.format(_("Do you want to delete the fax job No. \"{0,number,integer}\" (fax number: {1})?"), Long.valueOf(job.getId()), job.getDialstring()), _("Delete job"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                try {
                                    if (tbl == TableSending)
                                        hyfc.kill(job);
                                    else
                                        hyfc.delete(job);
                                } catch (Exception e1) {
                                    JOptionPane.showMessageDialog(mainwin.this, _("Couldn't delete the job: ") + "\n" + e1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(mainwin.this, _("Couldn't access the selected job!\n Reason: ")  + e1.getLocalizedMessage(), _("Error"), JOptionPane.WARNING_MESSAGE);;                       
                    }
                }
            };
        };
        actDelete.putValue(Action.NAME, _("Delete"));
        actDelete.putValue(Action.SHORT_DESCRIPTION, _("Deletes the selected fax"));
        actDelete.putValue(Action.SMALL_ICON, utils.loadIcon("general/Delete"));
        
        actShow = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Component selComp = TabMain.getSelectedComponent();
                if ((selComp == scrollRecv) && (TableRecv.getSelectedRow() >= 0))
                    showRecvFile(TableRecv.getSelectedRow());
                else if ((selComp == scrollSent) || (selComp == scrollSending)) {
                    TooltipJTable tbl = (selComp == scrollSent) ? TableSent : TableSending;
                    
                    try {
                        if (tbl.getSelectedRow() >= 0) {
                            Job job = getJob(tbl.getSelectedRow(), tbl);
                            if (job == null) {
                                JOptionPane.showMessageDialog(mainwin.this, _("Selected job could not be found!"), _("Error"), JOptionPane.WARNING_MESSAGE);
                                return;
                            }   
                            
                            showJobFile(job);
                        }
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(mainwin.this, _("Couldn't access the selected job!\n Reason: ")  + e1.getLocalizedMessage(), _("Error"), JOptionPane.WARNING_MESSAGE);;                       
                    }
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
                    if (TableRecv.getSelectedRow() >= 0) {
                        recvSetRead(TableRecv.getSelectedRow(), newState);
                    }
                }
            };
        };
        actFaxRead.putValue(Action.NAME, _("Marked as read"));
        actFaxRead.putValue(Action.SHORT_DESCRIPTION, _("Marks the selected fax as read/unread"));
        actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, true);
        
        actChecker = new ActionEnabler();
    }
    
    private JPopupMenu getTblPopup() {
        if (tblPopup == null) {
            tblPopup = new JPopupMenu(_("Fax"));
            tblPopup.add(new JMenuItem(actShow));
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
            
            //ReloadTables();
            
            //recvTableModel.setStateString(myopts.recvReadState);
            try {
                recvTableModel.readFromStream(new FileInputStream(utils.getConfigDir() + "recvread"));
            } catch (FileNotFoundException e) { 
                // No file yet - keep empty
            } catch (IOException e) {
                System.err.println("Error reading read status: " + e.getMessage());
            }
            TableRecv.repaint();
            
            //tmrStat.start();
            //tmrTable.start();
            
            // Multi-threaded implementation of the periodic refreshes.
            // I hope I didn't introduce too much race conditions/deadlocks this way
            statRefresher = new StatusRefresher();
            utmrTable.schedule(statRefresher, 0, myopts.statusUpdateInterval);
            
            tableRefresher = new TableRefresher(utils.VectorToString(myopts.sentfmt, "|"), utils.VectorToString(myopts.sendingfmt, "|"));
            utmrTable.schedule(tableRefresher, 0, myopts.tableUpdateInterval);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, _("An error occured connecting to the server:") + "\n" + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            actOptions.actionPerformed(null);
        }
        
    }
    
    
    /*private void getStatus() throws FileNotFoundException, IOException, ServerResponseException {
        Vector stat = hyfc.getList("status");
        TextStatus.setText(utils.VectorToString(stat, "\n"));
    }*/
    
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
        
    }
    
    private void recvSetRead(int row, boolean state) {
        recvTableModel.setRead(getRecvFilename(row), state);
        TableRecv.getSorter().fireTableRowsUpdated(row, row); // HACK to repaint table (calling it in the TableModel would remove the current selection)
        actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, state);
    }
    
    private String getRecvFilename(int row) {
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
    }
    
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
            TableRecv.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            //TableRecv.setModel(getRecvTableModel());
            TableRecv.setShowGrid(true);
            
            /* TableRecv.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                        int row = TableRecv.rowAtPoint(e.getPoint());
                        if (row >= 0)
                            showRecvFile(row);
                    }
                }
            }); */
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
            recvTableModel = new UnReadMyTableModel(utils.recvfmt_FileName);
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
            TableSent.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
            TableSending.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
            sendingTableModel = new MyTableModel();
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
            menuFax.addSeparator();
            menuFax.add(getShowMenuItem());
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
    
    class MyTableModel extends AbstractTableModel {
        
        protected String[][] data;
        public Vector<FmtItem> columns;
        private HashMap<Long,Object> dataCache; // cache to avoid too much parsing
        
        /**
         * Returns a custom font for the table cell.
         * A return value of null means "use default font"
         * @param row
         * @param col
         * @return
         */
        public Font getCellFont(int row, int col) {
            return null;
        }
        
        public void flushCache() {
            dataCache.clear();
        }
        
        private Long getCacheKey(int row, int col) {
            return ((long)row << 32) | (long)col;
        }
        
        public void setData(String[][] newData) {
            if (!Arrays.deepEquals(data, newData)) {
                data = newData;
                flushCache();
                fireTableDataChanged();
            }
        }
        public int getColumnCount() {
            if (columns != null)
                return columns.size();
            else
                return 0;
        }
        
        public int getRowCount() {
            if (data == null)
                return 0;
            else
                return data.length;
        }
        
        public String getStringAt(int rowIndex, int columnIndex) {
            try {
                return data[rowIndex][columnIndex];
            }
            catch (ArrayIndexOutOfBoundsException a) {
                return " ";
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            Class <?>dataClass = getColumnClass(columnIndex);
            //System.out.println("getValue: " + rowIndex + "," + columnIndex);
            
            if (dataClass == String.class) { // shortcut for String -> non-"cached"
                /* String res = getStringAt(rowIndex, columnIndex).trim();
                if (res.length() == 0)
                    return null;
                else
                    return res; */
                return getStringAt(rowIndex, columnIndex);
            } else {                
                Long key = getCacheKey(rowIndex, columnIndex);
                
                Object result = dataCache.get(key);
                if ((result != null) || dataCache.containsKey(key))
                    return result; // value is cached -> return it
                
                String res = getStringAt(rowIndex, columnIndex).trim();
                if (res.length() > 0) {
                    //System.out.println("Parse: " + res);
                    try {
                        if (dataClass == Integer.class)
                            result = Integer.valueOf(res);
                        else if (dataClass == Float.class)
                            result = Float.valueOf(res);
                        else if (dataClass == Double.class)
                            result = Double.valueOf(res);
                        else if (dataClass == Boolean.class) // "*" if true, " " otherwise
                            result = (res.trim().length() > 0);
                        else if (dataClass == Date.class)
                            result = columns.get(columnIndex).dateFormat.fmtIn.parse(res);
                        else
                            result = res;
                    } catch (NumberFormatException e) {
                        // If not parseable, return NaN
                        System.err.println("Not a number: " + res);
                        //result = Float.NaN;
                        result = null;
                    } catch (ParseException e) {
                        System.err.println("Not a parseable date: " + res);
                        result = null;
                    }    
                } else
                    result = null;
                dataCache.put(key, result);
                return result;
            }
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        
        public String getColumnName(int column) {
            return columns.get(column).desc;
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columns.get(columnIndex).dataClass;
        }
        
        public MyTableModel() {
            dataCache = new HashMap<Long,Object>();
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
                    faxReadSelected = recvTableModel.getRead(getRecvFilename(TableRecv.getSelectedRow()));
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
            actDelete.setEnabled(deleteState);
            actFaxRead.setEnabled(faxReadState);
            actFaxRead.putValue(ActionJCheckBoxMenuItem.SELECTED_PROPERTY, faxReadSelected);
        }
    }
    
    // Tablemodel with read/unread state
    class UnReadMyTableModel extends MyTableModel {
        public Font readFont = null;
        public Font unreadFont = null;    
        public boolean defaultState = false;
        private FmtItem hashCol = null;
        private int hashColIdx = -3;
        
        protected HashMap<String,Boolean> readMap = null;
        
        public void setHashCol(FmtItem hashCol) {
            this.hashCol = hashCol;
            refreshHashColIdx();
        }
        
        public FmtItem getHashCol() {
            return hashCol;
        }
        
        public void setRead(String key, boolean state) {
            readMap.put(key, state);
            
            //fireTableRowsUpdated(row, row);
        }
        
        public boolean getRead(String key) {
            Boolean val = readMap.get(key);
            if (val == null)
                return defaultState;
            else
                return val;
        }
        
        private void refreshHashColIdx() {
            if (columns != null)
                hashColIdx = columns.indexOf(hashCol);
            else
                hashColIdx = -2;
        }
        
        // Delete entries removed from the data
        public void shrinkReadState() {                        
            if (hashColIdx < 0)
                return;
            
            int rowC = getRowCount();
            ArrayList<String> dataKeys = new ArrayList<String>(rowC);           
            for (int i=0; i < rowC; i++) {
                dataKeys.add(data[i][hashColIdx]);
            }
            readMap.keySet().retainAll(dataKeys);
        }
        
        /*@Override
        public void fireTableDataChanged() {
            shrinkReadState();
            super.fireTableDataChanged();
        }*/
        
        @Override
        public void fireTableStructureChanged() {
            refreshHashColIdx();
            super.fireTableStructureChanged();
        }
        
        @Override
        public Font getCellFont(int row, int col) {
            if (hashColIdx < 0)
                return null;
            
            if (getRead(data[row][hashColIdx]))
                return readFont;
            else
                return unreadFont;
        }
        
        public void readFromStream(InputStream fin) throws IOException {
            BufferedReader bIn = new BufferedReader(new InputStreamReader(fin));
            
            readMap.clear();
            String line = null;
            while ((line = bIn.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                        readMap.put(line, !defaultState);
                }
            }
            bIn.close();
        }
        
        public void storeToStream(OutputStream fOut) throws IOException {
            BufferedWriter bOut = new BufferedWriter(new OutputStreamWriter(fOut));
            
            shrinkReadState();
            bOut.write("# " + utils.AppShortName + " " + utils.AppVersion + " configuration file\n");
            bOut.write("# This file contains a list of faxes considered read\n\n");
            
            for ( String key : readMap.keySet() ) {
                if (readMap.get(key).booleanValue() != defaultState)
                    bOut.write(key + "\n");
            }
            bOut.close();
        }
        
        /*public String getStateString() {
            StringBuilder res = new StringBuilder();
            
            shrinkReadState();
            for ( String key : readMap.keySet() ) {
                if (readMap.get(key).booleanValue() != defaultState)
                    res.append(key).append('|');
            }
            return res.toString();
        }
        
        public void setStateString(String str) {
            readMap.clear();
            if (str.length() == 0)
                return;
            
            String[] selKeys = str.split("\\|");
            for (int i=0; i < selKeys.length; i++)
                readMap.put(selKeys[i], !defaultState);
        }*/
        
        public UnReadMyTableModel(FmtItem hashCol) {
            super();
            this.hashCol = hashCol;
            readMap = new HashMap<String,Boolean>();
        }
    }
    
    
    // JTable with tooltips and other extensions
    // Create a new table with: new TooltipJTable(realTableModel)
    class TooltipJTable extends JTable {
        
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                public String getToolTipText(MouseEvent event) {
                    int index = columnModel.getColumnIndexAtX(event.getPoint().x);
                    int realIndex = 
                            columnModel.getColumn(index).getModelIndex();
                    return getRealModel().columns.get(realIndex).longdesc;
                }
            };
        }
        
        public MyTableModel getRealModel() {
            return (MyTableModel)((TableSorter)dataModel).tableModel;
        }  
        
        public TableSorter getSorter() {
            return (TableSorter)dataModel;
        }
        
        public TooltipJTable(MyTableModel model) {
            super(new TableSorter(model));
            getSorter().setTableHeader(getTableHeader());
            getTableHeader().setReorderingAllowed(false);
            setRowHeight(getFontMetrics(getFont()).getHeight() + 4);
        }
        
        public String getColumnCfgString() {
            StringBuilder res = new StringBuilder();
            
            int recvCol = 0;
            for (int i = 0; i < getColumnCount(); i++) {
                recvCol = (i + 1) * getSorter().getSortingStatus(i); // HACK: getSortingStatus returns 1, -1, 0 in the "right way" for this
                if (recvCol != 0)
                    break;
            }
            
            res.append(recvCol).append('|');
            
            Enumeration<TableColumn> colEnum = getColumnModel().getColumns();
            while (colEnum.hasMoreElements()) {
                TableColumn col = colEnum.nextElement();
                res.append(col.getIdentifier()).append(':').append(col.getWidth()).append('|');
            }
            
            return res.toString();
        }
        
        public void setColumnCfgString(String newCfg) {
            if ((newCfg == null) || (newCfg.length() == 0))
                return;
            
            String[] cfg = newCfg.split("\\|");            
            if (cfg.length < 1)
                return;
            
            try {
                int sort = Integer.parseInt(cfg[0]);
                if ((sort != 0) && (Math.abs(sort) <= getColumnCount()))
                    getSorter().setSortingStatus(Math.abs(sort) - 1, (sort > 0) ? TableSorter.ASCENDING : TableSorter.DESCENDING);
            } catch (NumberFormatException e1) {
                System.err.println("Couldn't parse value: " + cfg[0]);
            }
                        
            for (int i = 1; i < cfg.length; i++) {
                try {
                    int pos = cfg[i].indexOf(':');
                    if (pos >= 0) {
                        String id = cfg[i].substring(0, pos);
                        int val = Integer.parseInt(cfg[i].substring(pos + 1));
                        
                        Enumeration<TableColumn> colEnum = getColumnModel().getColumns();
                        while (colEnum.hasMoreElements()) {
                            TableColumn col = colEnum.nextElement();
                            if (col.getIdentifier().equals(id)) {
                                col.setPreferredWidth(val);
                                break;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Couldn't parse value: " + cfg[i]);
                }
            } 
        }
        
        @Override
        public void columnAdded(TableColumnModelEvent e) {
            // Set identifier 
            getColumnModel().getColumn(e.getToIndex()).setIdentifier(getRealModel().columns.get(e.getToIndex()).fmt);
            super.columnAdded(e);
        }
        
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component comp = super.prepareRenderer(renderer, row, column);
            Font customFnt = getRealModel().getCellFont(getSorter().modelIndex(row), getColumnModel().getColumn(column).getModelIndex());
            if (customFnt != null)
                comp.setFont(customFnt);
            return comp;
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
}  //  @jve:decl-index=0:visual-constraint="10,10"
