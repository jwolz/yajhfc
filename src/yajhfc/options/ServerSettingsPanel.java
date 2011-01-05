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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import yajhfc.FaxOptions;
import yajhfc.HylaClientManager;
import yajhfc.Utils;
import yajhfc.readstate.AvailablePersistenceMethod;
import yajhfc.readstate.PersistentReadState;
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
public class ServerSettingsPanel extends AbstractOptionsPanel {

    protected final static class TestConnectionWorker extends ProgressWorker implements ActionListener {
        private final Dialog ow;
        private final FaxOptions tempFO;
        private HylaClientManager tempHCM;
        private volatile boolean cancelled = false;

        protected TestConnectionWorker(Dialog ow, FaxOptions tempFO) {
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
    JCheckBox checkPCLBug;
    //JCheckBox checkPreferTIFF;
    JCheckBox checkUseDisconnected;
    JSpinner spinOffset;
    JSpinner spinSocketTimeout;
    JSpinner spinStatusInterval;
    JSpinner spinTableInterval;
    JComboBox comboPersistenceMethods;
    JButton buttonConfigPersistence;
    Action actTestConnection;
    JComboBox comboCharset;
    
    
    public ServerSettingsPanel() {
        super(false);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
        double[][] tablelay = {
                {border, 0.4, border, TableLayout.FILL, border},
                { border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border }
        };
        this.setLayout(new TableLayout(tablelay));
        
        this.add(getPanelServerRetrieval(), "1,1,1,3,f,f");
        this.add(getPanelServer(), "3,1,f,t");
        this.add(getPanelPersistence(), "3,3,f,b");
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        textHost.setText(foEdit.host);
        textPort.setText(String.valueOf(foEdit.port));
        textUser.setText(foEdit.user);
        textPassword.setText(foEdit.pass.getPassword());
        textAdminPassword.setText(foEdit.AdminPassword.getPassword());
        
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
        
        checkPasv.setSelected(foEdit.pasv);
        checkPCLBug.setSelected(foEdit.pclBug);
        checkAskPassword.setSelected(foEdit.askPassword);
        checkAskAdminPassword.setSelected(foEdit.askAdminPassword);
        checkAskUsername.setSelected(foEdit.askUsername);
        checkUseDisconnected.setSelected(foEdit.useDisconnectedMode);
        
        
        spinOffset.setValue(foEdit.dateOffsetSecs);
        spinTableInterval.setValue(foEdit.tableUpdateInterval / 1000.0);
        spinStatusInterval.setValue(foEdit.statusUpdateInterval / 1000.0);
        spinSocketTimeout.setValue(foEdit.socketTimeout / 1000.0);
        

    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        saveSettingsWithOutPersistence(foEdit);
        
        // Save persistence settings:
        String persistenceMethod = ((AvailablePersistenceMethod)comboPersistenceMethods.getSelectedItem()).getKey();
        String config = persistenceConfigs.get(persistenceMethod);
        if (config == null) config = "";
        if (!(persistenceMethod.equals(foEdit.persistenceMethod) && 
                ((foEdit.persistenceConfig == null) || config.equals(foEdit.persistenceConfig)))) {
            PersistentReadState.getCurrent().persistReadState();
            PersistentReadState.resetCurrent();
        }
        foEdit.persistenceMethod = persistenceMethod;
        foEdit.persistenceConfig = config;
    }

    protected void saveSettingsWithOutPersistence(FaxOptions foEdit) {
        foEdit.port = Integer.parseInt(textPort.getText());
        foEdit.dateOffsetSecs = (Integer)spinOffset.getValue();
        foEdit.tableUpdateInterval = (int)(((Double)spinTableInterval.getValue()).doubleValue() * 1000);
        foEdit.statusUpdateInterval = (int)(((Double)spinStatusInterval.getValue()).doubleValue() * 1000);
        foEdit.socketTimeout = (int)(((Double)spinSocketTimeout.getValue()).doubleValue() * 1000);
        
        foEdit.host = textHost.getText();
        foEdit.user = textUser.getText();
        foEdit.pass.setPassword(new String(textPassword.getPassword()));
        foEdit.AdminPassword.setPassword(new String(textAdminPassword.getPassword()));
        
        foEdit.pasv = checkPasv.isSelected();
        foEdit.pclBug = checkPCLBug.isSelected();
        foEdit.askPassword = checkAskPassword.isSelected();
        foEdit.askAdminPassword = checkAskAdminPassword.isSelected();
        foEdit.askUsername = checkAskUsername.isSelected();
        foEdit.useDisconnectedMode = checkUseDisconnected.isSelected();
        
        foEdit.hylaFAXCharacterEncoding = ((Charset)comboCharset.getSelectedItem()).name();
    }
    
    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        if (textHost.getText().length() == 0) {
            optionsWin.focusComponent(textHost);
            JOptionPane.showMessageDialog(this, _("Please enter a host name."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (textUser.getText().length() == 0) {
            optionsWin.focusComponent(textUser);
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
            optionsWin.focusComponent(textPort);
            JOptionPane.showMessageDialog(this, _("Please enter a valid port number."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
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
                    final OptionsWin ow = (OptionsWin)SwingUtilities.getWindowAncestor(ServerSettingsPanel.this);
                    if (!validateSettings(ow)) {
                        return;
                    }
                    final FaxOptions tempFO = Utils.getFaxOptions().clone();
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
            final int rowCount = 22;
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
            
            checkPCLBug = new JCheckBox("<html>" + _("Use PCL file type bugfix") + "</html>");
            spinOffset = SpinnerDateOffsetEditor.createJSpinner();
            
            spinStatusInterval = new JSpinner(new SpinnerNumberModel(1, 0.5, 86400, 1));
            spinTableInterval = new JSpinner(new SpinnerNumberModel(3, 0.5, 86400, 1));
            spinSocketTimeout = new JSpinner(new SpinnerNumberModel((double)90, 0, 86400, 1));
            spinSocketTimeout.setToolTipText(_("The maximum time to wait for a interaction with the server to complete. Values below 5 are not recommended; 0 disables this timeout."));
            
            //checkPreferTIFF = new JCheckBox("<html>" + _("Prefer rendered TIFF (experimental)") + "</html>");
            //checkPreferTIFF.setToolTipText(_("Try to fetch the rendered TIFF from the HylaFAX server instead of the source file."));
            
            checkUseDisconnected = new JCheckBox("<html>" + _("Create new session for every action") + "</html>");
            checkUseDisconnected.setToolTipText(_("Connect to the server and log in for every action (e.g. view a fax, update tables, ...) and disconnect afterwards. This impairs performance but might work around some bugs."));
            
            Vector<Charset> charsets = new Vector<Charset>(Charset.availableCharsets().values());
            Collections.sort(charsets);
            comboCharset = new JComboBox(charsets);
            
            addWithLabel(panelServerRetrieval, spinOffset, _("Date/Time offset:"), "1, 2, 1, 2, f, c");
            spinOffset.setToolTipText(_("Offset to be added to dates received from the HylaFAX server before displaying them."));
            panelServerRetrieval.add(checkPCLBug, "1, 5");
            panelServerRetrieval.add(checkUseDisconnected, "1, 8");
            //panelServerRetrieval.add(checkPreferTIFF, "1, 6, 1, 7");
            
            addWithLabel(panelServerRetrieval, spinTableInterval, "<html>" + _("Table refresh interval (secs.):") + "</html>", "1, 11, 1, 11, f, c");
            addWithLabel(panelServerRetrieval, spinStatusInterval, "<html>" + _("Server status refresh interval (secs.):") + "</html>", "1, 14, 1, 14, f, c");
            addWithLabel(panelServerRetrieval, spinSocketTimeout, "<html>" + _("Server socket timeout (secs):") + "</html>", "1, 17, 1, 17, f, c");
            addWithLabel(panelServerRetrieval, comboCharset, _("Character set"), "1,20,1,20,f,c");
        }
        return panelServerRetrieval;
    }

    private JPanel getPanelPersistence() {
        if (panelPersistence == null) {
            double[][] tablelay = {
                    {border, TableLayout.FILL, border/2, TableLayout.PREFERRED, border},
                    {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border}
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
                    String res = sel.showConfigDialog(SwingUtilities.getWindowAncestor(ServerSettingsPanel.this), persistenceConfigs.get(sel.getKey()));
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
            panelPersistence.add(buttonConfigPersistence, "3,2");
        }
        return panelPersistence;
    }

}
