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
package yajhfc.options;

import static yajhfc.Utils._;
import static yajhfc.Utils.addWithLabel;
import static yajhfc.options.OptionsWin.border;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import yajhfc.FaxTimezone;
import yajhfc.FileTextField;
import yajhfc.HylaClientManager;
import yajhfc.IDAndNameOptions;
import yajhfc.Utils;
import yajhfc.model.servconn.FaxListConnectionType;
import yajhfc.readstate.AvailablePersistenceMethod;
import yajhfc.readstate.PersistentReadState;
import yajhfc.server.ServerOptions;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.IntVerifier;
import yajhfc.util.ProgressDialog;
import yajhfc.util.ProgressWorker;
import yajhfc.util.SpinnerDateOffsetEditor;

/**
 * @author jonas
 *
 */
public class SingleServerSettingsPanel extends AbstractOptionsPanel<ServerOptions> {

    protected final static class TestConnectionWorker extends ProgressWorker implements ActionListener {
        private final Dialog ow;
        private final ServerOptions tempFO;
        private HylaClientManager tempHCM;
        private volatile boolean cancelled = false;

        protected TestConnectionWorker(Dialog ow, ServerOptions tempFO) {
            this.ow = ow;
            this.tempFO = tempFO;
            
            setProgressMonitor(new ProgressDialog(ow, _("Test connection"), this).progressPanel);
        }
        
        public void startWork() {
            startWork(ow, MessageFormat.format(_("Testing connection to {0}"), tempFO.host));
        }

        @Override
        public void doWork() {
            updateNote(_("Logging in..."));
            tempHCM = new HylaClientManager(tempFO);
            if (!cancelled && tempHCM.forceLogin(ow) != null) {
                if (!cancelled)
                    updateNote(_("Login successful, logging out."));
                tempHCM.forceLogout();
                if (!cancelled) {
                    progressMonitor.close();
                    this.showMessageDialog(_("Connection to the HylaFAX server successful."), _("Test connection"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            cancelled = true;
            if (tempHCM != null) {
                tempHCM.setShowErrorsUsingGUI(false);
            }
            interrupt();
            getProgressMonitor().close();
        }
    }

    JPanel panelPersistence;
    JPanel panelServer;
    JPanel panelServerRetrieval;
    Map<String,String> persistenceConfigs = new HashMap<String,String>();
    JPasswordField textAdminPassword;
    JTextField textHost;
    JPasswordField textPassword;
    JTextField textPort;
    JTextField textUser;
    JCheckBox checkAskAdminPassword;
    JCheckBox checkAskPassword;
    JCheckBox checkAskUsername;
    JCheckBox checkPasv;
    JCheckBox checkUseDisconnected;
    JSpinner spinOffset;
    JComboBox comboPersistenceMethods;
    JButton buttonConfigPersistence;
    Action actTestConnection;
    JComboBox comboCharset;
    JComboBox comboTZone;
    
    JCheckBox checkUseArchive;
    FileTextField ftfArchiveLocation;
    JLabel archiveLabel;
    
    FileTextField ftfSpoolLocation;
    JComboBox comboConnectionType;
    
    JLabel labelSpoolLocation;
    
    JTextField textName;
    JTextField textID;
    
    final ServerSettingsPanel parent;
    ServerOptions soEdit;
    
    public SingleServerSettingsPanel(ServerSettingsPanel parent) {
        super(false);
        this.parent = parent;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
        double[][] tablelay = {
                {border, 0.4, border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                { border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border }
        };
        this.setLayout(new TableLayout(tablelay));
        
        textName = new JTextField();
        
        textID = new JTextField("#####");
        textID.setEditable(false);
        
        addWithLabel(this, textName, _("Name for this server:"), "1,2,3,2,f,c");
        addWithLabel(this, textID, _("ID:"), "5,2,f,c");
        
        this.add(getPanelServerRetrieval(), "1,4,1,4,f,t");
        this.add(getPanelServer(), "3,4,5,4,f,t");
        this.add(createPanelConnectionType(), "3,6,5,6,f,t");
        this.add(getPanelPersistence(), "1,6,1,6,f,t");
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(ServerOptions foEdit) {
        this.soEdit = foEdit;
        
        textName.setText(foEdit.name);
        textID.setText(String.valueOf(foEdit.id));
        textHost.setText(foEdit.host);
        textPort.setText(String.valueOf(foEdit.port));
        textUser.setText(foEdit.user);
        textPassword.setText(foEdit.pass.getPassword());
        textAdminPassword.setText(foEdit.AdminPassword.getPassword());
        
        persistenceConfigs.clear();
        persistenceConfigs.put(foEdit.persistenceMethod, foEdit.persistenceConfig);
        int pos = 0; 
        for (int i=0; i<PersistentReadState.persistenceMethods.size(); i++) {
            if (PersistentReadState.persistenceMethods.get(i).getKey().equals(foEdit.persistenceMethod)) {
                pos = i;
                break;
            }
        }
        comboPersistenceMethods.setSelectedIndex(pos);
        
        comboCharset.setSelectedItem(Charset.forName(foEdit.hylaFAXCharacterEncoding));
        comboTZone.setSelectedItem(foEdit.tzone);
        
        checkPasv.setSelected(foEdit.pasv);
        checkAskPassword.setSelected(foEdit.askPassword);
        checkAskAdminPassword.setSelected(foEdit.askAdminPassword);
        checkAskUsername.setSelected(foEdit.askUsername);
        checkUseDisconnected.setSelected(foEdit.useDisconnectedMode);
        
        
        spinOffset.setValue(foEdit.dateOffsetSecs);
        checkUseArchive.setSelected(foEdit.showArchive);
        ftfArchiveLocation.setText(foEdit.archiveLocation);

        comboConnectionType.setSelectedItem(foEdit.faxListConnectionType);
        ftfSpoolLocation.setText(foEdit.directAccessSpoolPath);
        
        checkEnable();
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(ServerOptions foEdit) {
        saveSettingsWithOutPersistence(foEdit);
        
        // Save persistence settings:
        String persistenceMethod = ((AvailablePersistenceMethod)comboPersistenceMethods.getSelectedItem()).getKey();
        String config = persistenceConfigs.get(persistenceMethod);
        if (config == null) config = "";
        foEdit.persistenceMethod = persistenceMethod;
        foEdit.persistenceConfig = config;
        
        foEdit.showArchive = checkUseArchive.isSelected();
        foEdit.archiveLocation = ftfArchiveLocation.getText();
        
        foEdit.faxListConnectionType = (FaxListConnectionType)comboConnectionType.getSelectedItem();
        foEdit.directAccessSpoolPath = ftfSpoolLocation.getText();
    }

    protected void saveSettingsWithOutPersistence(ServerOptions foEdit) {
        foEdit.port = Integer.parseInt(textPort.getText());
        foEdit.dateOffsetSecs = (Integer)spinOffset.getValue();
        
        foEdit.name = textName.getText();
        foEdit.host = textHost.getText();
        foEdit.user = textUser.getText();
        foEdit.pass.setPassword(new String(textPassword.getPassword()));
        foEdit.AdminPassword.setPassword(new String(textAdminPassword.getPassword()));
        
        foEdit.pasv = checkPasv.isSelected();
        foEdit.askPassword = checkAskPassword.isSelected();
        foEdit.askAdminPassword = checkAskAdminPassword.isSelected();
        foEdit.askUsername = checkAskUsername.isSelected();
        foEdit.useDisconnectedMode = checkUseDisconnected.isSelected();
        
        foEdit.hylaFAXCharacterEncoding = ((Charset)comboCharset.getSelectedItem()).name();
        foEdit.tzone = (FaxTimezone)comboTZone.getSelectedItem();
    }
    
    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        ServerOptions so = IDAndNameOptions.getItemByName(parent.getServers().getList(), textName.getText());
        if (so != null && so.id != soEdit.id) {
            textName.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("There already exists another server with this name!"), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (textHost.getText().length() == 0) {
            //optionsWin.focusComponent(textHost);
            textHost.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("Please enter a host name."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (textUser.getText().length() == 0) {
            //optionsWin.focusComponent(textUser);
            textUser.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("Please enter a user name."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String port = textPort.getText();
        boolean valid = true;
        if (port.length() == 0) {
            valid = false;
        } else {
            try {
                int iPort = Integer.parseInt(port);
                valid = (iPort > 0 && iPort < 65536);
            } catch (NumberFormatException e) {
                valid = false;
            }
        }
        if (!valid) {
            //optionsWin.focusComponent(textPort);
            textPort.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("Please enter a valid port number."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (checkUseArchive.isSelected()) {
            File archiveDir = new File(ftfArchiveLocation.getText());
            if (!archiveDir.exists()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Utils._("Directory {0} does not exist!"), archiveDir), Utils._("Archive directory"), JOptionPane.ERROR_MESSAGE);
                //optionsWin.focusComponent(ftfArchiveLocation.getJTextField());
                ftfArchiveLocation.getJTextField().requestFocusInWindow();
                return false;
            }
        }
        
        if (comboConnectionType.getSelectedItem() == FaxListConnectionType.DIRECTACCESS) {
            File spoolDir = new File(ftfSpoolLocation.getText());
            File[] checkFiles = {
                    spoolDir, 
                    new File(spoolDir, "recvq"),
                    new File(spoolDir, "doneq"),
                    new File(spoolDir, "docq")
            };
            for (File dir : checkFiles) {
                if (!dir.exists()) {
                    JOptionPane.showMessageDialog(this, MessageFormat.format(Utils._("Directory {0} does not exist!"), dir), Utils._("Invalid spool directory"), JOptionPane.ERROR_MESSAGE);
                    //optionsWin.focusComponent(ftfSpoolLocation.getJTextField());
                    ftfSpoolLocation.getJTextField().requestFocusInWindow();
                    return false;
                }   
            }
        }
        
        return true;
    }

    private JPanel createPanelConnectionType() {
        ftfSpoolLocation = new FileTextField();
        ftfSpoolLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ftfSpoolLocation.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        comboConnectionType = new JComboBox(FaxListConnectionType.values());
        comboConnectionType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selection = comboConnectionType.getSelectedItem();
                boolean enableDirectAccess = (selection == FaxListConnectionType.DIRECTACCESS);

                ftfSpoolLocation.setEnabled(enableDirectAccess);
                labelSpoolLocation.setEnabled(enableDirectAccess);
            }
        });
        
        checkUseArchive = new JCheckBox(Utils._("Show archive table"));
        checkUseArchive.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkEnable();
            }
        });
        ftfArchiveLocation = new FileTextField();
        ftfArchiveLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ftfArchiveLocation.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
                
        double[][] dLay = {
                {border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        JPanel connTypePanel = new JPanel(new TableLayout(dLay), false);
        connTypePanel.setBorder(BorderFactory.createTitledBorder(_("Fax lists")));
        
        Utils.addWithLabel(connTypePanel, comboConnectionType, _("Access method for the fax lists"), "1,2");
        labelSpoolLocation = Utils.addWithLabel(connTypePanel, ftfSpoolLocation, _("Location of spool directory for direct access (must contain recvq, doneq and docq)"), "1,5");
        
        connTypePanel.add(checkUseArchive, "1,7");
        archiveLabel = Utils.addWithLabel(connTypePanel, ftfArchiveLocation, Utils._("Location of archive directory:"), "1,10,1,10,f,f");
        
        return connTypePanel;
    }
    
    private JPanel getPanelServer() {
        if (panelServer == null) {
            final int rowCount = 20;
            double[][] tablelay = {
                    {border, 0.22, border, 0.22, border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                    new double[rowCount]
            };
            for (int i=0; i<rowCount-1; i++) {
                if (i%3 == 0) {
                    tablelay[1][i] = border;
                } else {
                    tablelay[1][i] = TableLayout.PREFERRED;
                }
            }
            tablelay[1][13] = 0; // checkPasv: no label
            tablelay[1][16] = 0; // buttonTestConnection: no label
            tablelay[1][rowCount - 1] = TableLayout.FILL;
            
            panelServer = new JPanel(new TableLayout(tablelay), false);
            panelServer.setBorder(BorderFactory.createTitledBorder(_("Connection settings:")));
                        
            textHost = new JTextField();
            textHost.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            textPort = new JTextField();
            textPort.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            textPort.setInputVerifier(new IntVerifier(1, 65536));
            textUser = new JTextField();
            textUser.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            textPassword = new JPasswordField();
            textAdminPassword = new JPasswordField();
            checkAskPassword = new JCheckBox(_("Always ask"));
            checkAskPassword.addItemListener(new ItemListener() {
               public void itemStateChanged(ItemEvent e) {
                   textPassword.setEnabled(!checkAskPassword.isSelected());
                } 
            });
            checkAskAdminPassword = new JCheckBox(_("Always ask"));
            checkAskAdminPassword.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    textAdminPassword.setEnabled(!checkAskAdminPassword.isSelected());
                 } 
             });
            checkAskUsername = new JCheckBox(_("Always ask"));
            checkAskUsername.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (checkAskUsername.isSelected()) {
                        checkAskPassword.setSelected(true);
                        checkAskAdminPassword.setSelected(true);
                        checkAskPassword.setEnabled(false);
                        checkAskAdminPassword.setEnabled(false);
                    } else {
                        checkAskPassword.setEnabled(true);
                        checkAskAdminPassword.setEnabled(true);
                    }
                }
            });
            
            checkPasv = new JCheckBox(_("Use passive mode to fetch faxes"));
            
            actTestConnection = new ExcDialogAbstractAction(_("Test connection")) {
                @Override
                protected void actualActionPerformed(ActionEvent e) {
                    final OptionsWin ow = (OptionsWin)SwingUtilities.getWindowAncestor(SingleServerSettingsPanel.this);
                    if (!validateSettings(ow)) {
                        return;
                    }
                    final ServerOptions tempFO = new ServerOptions(Utils.getFaxOptions());
                    saveSettings(tempFO);
                    
                    new TestConnectionWorker(ow, tempFO).startWork();
                }
            };
            
            addWithLabel(panelServer, textHost, _("Host name:"), "1, 2, 5, 2, f, c");
            addWithLabel(panelServer, textPort, _("Port:"), "7, 2, f, c");
            addWithLabel(panelServer, textUser, _("Username:"), "1, 5, 5, 5, f, c");
            panelServer.add(checkAskUsername, "6,5,7,5,f,c");
            addWithLabel(panelServer, textPassword, _("Password:"), "1, 8, 5, 8, f, c");
            panelServer.add(checkAskPassword, "6, 8, 7, 8, f, c");
            addWithLabel(panelServer, textAdminPassword, _("Admin Password:"), "1, 11, 5, 11, f, c");
            panelServer.add(checkAskAdminPassword, "6, 11, 7, 11, f, c");
            
            panelServer.add(checkPasv, "1, 14, 7, 14");
            panelServer.add(new JButton(actTestConnection), "1,17,7,17,f,f");
        }
        return panelServer;
    }

    private JPanel getPanelServerRetrieval() {
        if (panelServerRetrieval == null) {
            final int rowCount = 14;
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[rowCount]
            };
//            double rowh = 1 / (double)(rowCount - 1);
//            //tablelay[1][0] = border;
//            tablelay[1][rowCount - 1] = border;
//            Arrays.fill(tablelay[1], 0, rowCount - 1, rowh);
//            tablelay[1][3] = tablelay[1][5]  = rowh*0.5;
//            tablelay[1][8] = tablelay[1][10] = rowh*1.3333333333;
//            tablelay[1][rowCount - 2] = TableLayout.FILL;
            
            tablelay[1][rowCount-1] = TableLayout.FILL;
            for (int i=0; i<rowCount-1; i++) {
                if (i%3 == 0) {
                    tablelay[1][i] = border;
                } else {
                    tablelay[1][i] = TableLayout.PREFERRED;
                }
            }
            
            panelServerRetrieval = new JPanel(new TableLayout(tablelay), false);
            panelServerRetrieval.setBorder(BorderFactory.createTitledBorder(_("General settings:")));
            
            spinOffset = SpinnerDateOffsetEditor.createJSpinner();
            
            
            //checkPreferTIFF = new JCheckBox("<html>" + _("Prefer rendered TIFF (experimental)") + "</html>");
            //checkPreferTIFF.setToolTipText(_("Try to fetch the rendered TIFF from the HylaFAX server instead of the source file."));
            
            checkUseDisconnected = new JCheckBox("<html>" + _("Create new session for every action") + "</html>");
            checkUseDisconnected.setToolTipText(_("Connect to the server and log in for every action (e.g. view a fax, update tables, ...) and disconnect afterwards. This impairs performance but might work around some bugs."));
            
            Vector<Charset> charsets = new Vector<Charset>(Charset.availableCharsets().values());
            Collections.sort(charsets);
            comboCharset = new JComboBox(charsets);
            
            comboTZone = new JComboBox(FaxTimezone.values());
            
            addWithLabel(panelServerRetrieval, spinOffset, _("Date/Time offset:"), "1,2,1,2,f,c");
            spinOffset.setToolTipText(_("Offset to be added to dates received from the HylaFAX server before displaying them."));
            panelServerRetrieval.add(checkUseDisconnected, "1,5");
            addWithLabel(panelServerRetrieval, comboCharset, _("Character set:"), "1,8,1,8,f,c");
            addWithLabel(panelServerRetrieval, comboTZone, _("Time zone:"), "1,11,f,c");
        }
        return panelServerRetrieval;
    }
    
    
    void checkEnable() {
        boolean enable = checkUseArchive.isSelected();
        ftfArchiveLocation.setEnabled(enable);
        archiveLabel.setEnabled(enable);
    }
    
    
    private JPanel getPanelPersistence() {
        if (panelPersistence == null) {
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border}
            };
            panelPersistence = new JPanel(new TableLayout(tablelay), false);
            panelPersistence.setBorder(BorderFactory.createTitledBorder(_("Read/Unread state of faxes")));
            
            ActionListener persistenceListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    AvailablePersistenceMethod sel = (AvailablePersistenceMethod)comboPersistenceMethods.getSelectedItem();
                    if ("combo".equals(e.getActionCommand())) {
                        boolean canConfig = sel != null && sel.canConfigure();
                        buttonConfigPersistence.setEnabled(canConfig);
                        if (canConfig) {
                            String config = persistenceConfigs.get(sel.getKey());
                            if (config == null) {
                                doConfigure(sel);
                            }
                        }
                    } else if ("config".equals(e.getActionCommand())) {
                        if (sel != null) {
                            doConfigure(sel);
                        }
                    } else {
                        assert(false);
                    }
                }
                
                private void doConfigure(AvailablePersistenceMethod sel) {
                    String res = sel.showConfigDialog(SwingUtilities.getWindowAncestor(SingleServerSettingsPanel.this), persistenceConfigs.get(sel.getKey()));
                    if (res != null) {
                        persistenceConfigs.put(sel.getKey(), res);
                    }
                }
            };
            comboPersistenceMethods = new JComboBox(PersistentReadState.persistenceMethods.toArray());
            comboPersistenceMethods.addActionListener(persistenceListener);
            comboPersistenceMethods.setActionCommand("combo");
            
            buttonConfigPersistence = new JButton(_("Configure..."));
            buttonConfigPersistence.addActionListener(persistenceListener);
            buttonConfigPersistence.setActionCommand("config");
            
            addWithLabel(panelPersistence, comboPersistenceMethods, _("Save location:"), "1,2");
            panelPersistence.add(buttonConfigPersistence, "1,4");
        }
        return panelPersistence;
    }

}
