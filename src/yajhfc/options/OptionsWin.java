package yajhfc.options;
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
import static yajhfc.Utils.addWithLabel;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.OverlayLayout;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import yajhfc.DateStyle;
import yajhfc.FaxNotification;
import yajhfc.FaxOptions;
import yajhfc.FaxResolution;
import yajhfc.FaxTimezone;
import yajhfc.FmtItemDescComparator;
import yajhfc.FmtItemRenderer;
import yajhfc.HylaModem;
import yajhfc.IconMap;
import yajhfc.JobFormat;
import yajhfc.PaperSize;
import yajhfc.RecvFormat;
import yajhfc.Utils;
import yajhfc.YajLanguage;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginTableModel;
import yajhfc.plugin.PluginType;
import yajhfc.plugin.PluginUI;
import yajhfc.readstate.AvailablePersistenceMethod;
import yajhfc.readstate.PersistentReadState;
import yajhfc.send.SendWinStyle;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.IntVerifier;
import yajhfc.util.JTableTABAction;
import yajhfc.util.SpinnerDateOffsetEditor;
import yajhfc.util.fmtEditor;

public class OptionsWin extends JDialog {
    static final Logger log = Logger.getLogger(OptionsWin.class.getName());
    
    
    JPanel jContentPane = null;
    
    //JTabbedPane TabMain = null;
    JPanel PanelCommon = null;
    JPanel panelServerSettings;
    fmtEditor<RecvFormat> PanelRecvFmt = null;
    fmtEditor<JobFormat> PanelSentFmt = null, PanelSendingFmt = null;
        
    JPanel PanelButtons;
    JButton ButtonOK, ButtonCancel;
    
    JTextField textNotifyAddress, textHost, textUser, /*textViewer,*/ textPort;
    JPasswordField textPassword, textAdminPassword;
    JComboBox comboTZone, comboNotify, comboPaperSize, comboResolution; //, comboNewFaxAction;
    JComboBox comboLang, comboLookAndFeel, comboModem, comboSendWinStyle;
    JCheckBox checkPasv, checkPCLBug, checkAskPassword, checkAskAdminPassword, checkAskUsername;
    JCheckBox checkArchiveSentFaxes;
    JSpinner spinMaxTry, spinMaxDial, spinOffset, spinKillTime, spinSocketTimeout;
    JComboBox comboDateFormat, comboTimeFormat;
    
    JPanel panelServer, panelSend, panelPaths, panelUI, panelDateFormat;
    
    JPanel panelServerRetrieval, panelNewFaxAction;
    JCheckBox checkNewFax_Beep, checkNewFax_ToFront, checkNewFax_Open, checkNewFax_MarkAsRead, checkNewFax_BlinkTrayIcon;
    JSpinner spinStatusInterval, spinTableInterval;
    
    //JCheckBox checkPreferTIFF;
    JCheckBox checkUseDisconnected, checkShowTrayIcon, checkMinimizeToTray, checkMinimizeToTrayOnMainWinClose, checkAutoCheckForUpdate;
    
    JPanel panelPersistence;
    JComboBox comboPersistenceMethods;
    JButton buttonConfigPersistence;
    Map<String,String> persistenceConfigs = new HashMap<String,String>();
    
    JPanel panelPlugins, panelUpdates;
    JTable tablePlugins;
    PluginTableModel pluginTableModel;
    JButton buttonAddJDBC, buttonAddPlugin, buttonRemovePlugin;
    
    JTree mainTree;
    PanelTreeNode rootNode, serverSettingsNode;
    JLabel treeSelLabel;
    JPanel tabPanel;

    FaxOptions foEdit = null;
    List<RecvFormat> recvfmt;
    List<JobFormat> sentfmt, sendingfmt;
    Vector<LF_Entry> lookAndFeels;
    
    List<HylaModem> availableModems;
    List<OptionsPage> optionsPages = new ArrayList<OptionsPage>();
    
    boolean modalResult;
    boolean changedLF;
    public static final int border = 5;
    
    // true if OK, false otherwise
    public boolean getModalResult() {
        return modalResult;
    }
    
    private void initialize() {
        //PROFILE: long time = System.currentTimeMillis();
        //this.setSize(630, 460);
        this.setResizable(true);
        this.setTitle(_("Options"));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(getJContentPane());
        //PROFILE: System.out.println("  After getJContentPane: " + (-time + (time = System.currentTimeMillis())));
        modalResult = false;

        // Load values
        textNotifyAddress.setText(foEdit.notifyAddress);
        textHost.setText(foEdit.host);
        textPort.setText(String.valueOf(foEdit.port));
        textUser.setText(foEdit.user);
        textPassword.setText(foEdit.pass.getPassword());
        textAdminPassword.setText(foEdit.AdminPassword.getPassword());

        comboNotify.setSelectedItem(foEdit.notifyWhen);
        comboPaperSize.setSelectedItem(foEdit.paperSize);
        comboResolution.setSelectedItem(foEdit.resolution);
        comboTZone.setSelectedItem(foEdit.tzone);
        //comboNewFaxAction.setSelectedItem(foEdit.newFaxAction);
        comboLang.setSelectedItem(foEdit.locale);
        comboSendWinStyle.setSelectedItem(foEdit.sendWinStyle);

        int pos = 0; 
        for (int i=0; i<lookAndFeels.size(); i++) {
            if (lookAndFeels.get(i).className.equals(foEdit.lookAndFeel)) {
                pos = i;
                break;
            }
        }
        comboLookAndFeel.setSelectedIndex(pos);
        //changedLF = false;

        persistenceConfigs.put(foEdit.persistenceMethod, foEdit.persistenceConfig);
        pos = 0; 
        for (int i=0; i<PersistentReadState.persistenceMethods.size(); i++) {
            if (PersistentReadState.persistenceMethods.get(i).getKey().equals(foEdit.persistenceMethod)) {
                pos = i;
                break;
            }
        }
        comboPersistenceMethods.setSelectedIndex(pos);

        Object selModem = foEdit.defaultModem;
        for (HylaModem modem : availableModems) {
            if (modem.getInternalName().equals(selModem)) {
                selModem = modem;
                break;
            }
        }
        comboModem.setSelectedItem(selModem);

        
        setDateStyle(comboDateFormat, foEdit.dateStyle, DateStyle.getAvailableDateStyles());
        setDateStyle(comboTimeFormat, foEdit.timeStyle, DateStyle.getAvailableTimeStyles());

        checkPasv.setSelected(foEdit.pasv);
        checkPCLBug.setSelected(foEdit.pclBug);
        checkAskPassword.setSelected(foEdit.askPassword);
        checkAskAdminPassword.setSelected(foEdit.askAdminPassword);
        checkAskUsername.setSelected(foEdit.askUsername);
        //checkPreferTIFF.setSelected(foEdit.preferRenderedTIFF);
        checkUseDisconnected.setSelected(foEdit.useDisconnectedMode);
        checkShowTrayIcon.setSelected(foEdit.showTrayIcon);
        checkMinimizeToTray.setSelected(foEdit.minimizeToTray);
        checkMinimizeToTrayOnMainWinClose.setSelected(foEdit.minimizeToTrayOnMainWinClose);
        checkAutoCheckForUpdate.setSelected(foEdit.automaticallyCheckForUpdate);
        checkArchiveSentFaxes.setSelected(foEdit.archiveSentFaxes);

        checkNewFax_Beep.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_BEEP) != 0);
        checkNewFax_ToFront.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_TOFRONT) != 0);
        checkNewFax_Open.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_VIEWER) != 0);
        checkNewFax_MarkAsRead.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_MARKASREAD) != 0);
        checkNewFax_BlinkTrayIcon.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_BLINKTRAYICON) != 0);

        spinMaxDial.setValue(Integer.valueOf(foEdit.maxDial));
        spinMaxTry.setValue(Integer.valueOf(foEdit.maxTry));
        spinOffset.setValue(foEdit.dateOffsetSecs);
        spinTableInterval.setValue(foEdit.tableUpdateInterval / 1000.0);
        spinStatusInterval.setValue(foEdit.statusUpdateInterval / 1000.0);
        spinKillTime.setValue(foEdit.killTime);
        spinSocketTimeout.setValue(foEdit.socketTimeout / 1000.0);

        pluginTableModel.addAllItems(PluginManager.getKnownPlugins());

        for (OptionsPage page : optionsPages) {
            page.loadSettings(foEdit);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                foEdit.optWinBounds = getBounds();

                if (changedLF && !modalResult) {
                    Utils.setLookAndFeel(foEdit.lookAndFeel);
                }
            }
        });

        
        if (foEdit.optWinBounds != null) {
            this.setBounds(foEdit.optWinBounds);
        } else {
            //this.setLocationByPlatform(true);
            this.pack();
            Utils.setDefWinPos(this);
        }

        // Small special handling for new users
        if (foEdit.host.length() == 0) {
            //TabMain.setSelectedIndex(1);
            mainTree.setSelectionPath(new TreePath(new Object[] { rootNode, serverSettingsNode }));
        } else {
            mainTree.setSelectionRow(0);
        }
        //PROFILE: System.out.println("  After load settings: " + (-time + (time = System.currentTimeMillis())));
        //PROFILE: System.out.println("  After pack: " + (-time + (time = System.currentTimeMillis())));
    }
    
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
            JComponent comp = getMainPanel();
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            jContentPane.add(comp);
            comp = new JSeparator();
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            jContentPane.add(comp);
            jContentPane.add(Box.createRigidArea(new Dimension(0, border)));
            comp = getPanelButtons();
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            jContentPane.add(comp);
        }
        return jContentPane;
    }
    
    private JPanel getPanelButtons() {
        if (PanelButtons == null) {
            PanelButtons = new JPanel(false);
            PanelButtons.setLayout(new BoxLayout(PanelButtons, BoxLayout.LINE_AXIS));
            
            Dimension buttonSize = new Dimension(120, 30);
            
            PanelButtons.add(Box.createHorizontalGlue());
            
            ButtonOK = new JButton(_("OK"));
            ButtonOK.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   if (saveSettings(foEdit)) {
                       modalResult = true;
                       dispose();
                   }
                } 
            });
            ButtonOK.setPreferredSize(buttonSize);
            PanelButtons.add(ButtonOK);
            
            PanelButtons.add(Box.createRigidArea(new Dimension(border, border)));
            
            CancelAction actCancel = new CancelAction(this);
            ButtonCancel = actCancel.createCancelButton();
            ButtonCancel.setPreferredSize(buttonSize);
            PanelButtons.add(ButtonCancel);
            
            PanelButtons.add(Box.createRigidArea(new Dimension(border, border)));
        }
        return PanelButtons;
    }
    
    private JPanel getPanelServerSettings() {
        if (panelServerSettings == null) {
            double[][] tablelay = {
                    {border, 0.4, border, TableLayout.FILL, border},
                    { border, TableLayout.FILL, border, TableLayout.PREFERRED, border }
            };
            panelServerSettings = new JPanel(new TableLayout(tablelay), false);
            
            panelServerSettings.add(getPanelServerRetrieval(), "1,1,1,3");
            panelServerSettings.add(getPanelServer(), "3,1");
            panelServerSettings.add(getPanelPersistence(), "3,3");
        }
        return panelServerSettings;
    }
    
//    private JTabbedPane getTabMain() {
//        if (TabMain == null) {
//            TabMain = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.WRAP_TAB_LAYOUT);
//            
//            TabMain.addTab(_("Common"), getPanelCommon());
//            TabMain.addTab(_("Server"), getPanelServerSettings());
//            TabMain.addTab(_("Server"), getPanelSendSettings());
//            TabMain.addTab(_("Plugins & JDBC"), getPanelPlugins());
//            TabMain.addTab(MessageFormat.format(_("Table \"{0}\""), _("Received")), getPanelRecvFmt());
//            TabMain.addTab(MessageFormat.format(_("Table \"{0}\""), _("Sent")), getPanelSentFmt());
//            TabMain.addTab(MessageFormat.format(_("Table \"{0}\""), _("Transmitting")), getPanelSendingFmt());
//        }
//        return TabMain;
//    }
    
    /**
     * Creates the tree node structure
     */
    private void createRootNode() {
        //PROFILE: long time = System.currentTimeMillis();
        rootNode = new PanelTreeNode(null, null, "root", null);
        List<PanelTreeNode> rootChilds = new ArrayList<PanelTreeNode>();

        rootChilds.add(new PanelTreeNode(rootNode, getPanelCommon(), _("General"), Utils.loadIcon("general/Preferences")));
        //PROFILE: System.out.println("    After panel common: " + (-time + (time = System.currentTimeMillis())));
        
        rootChilds.add(new PanelTreeNode(rootNode, new PathAndViewPanel(), _("Paths and viewers"), Utils.loadIcon("development/Host")));
        //PROFILE: System.out.println("    After path and view panel: " + (-time + (time = System.currentTimeMillis())));
        
        rootChilds.add(serverSettingsNode = new PanelTreeNode(rootNode, getPanelServerSettings(), _("Server"), Utils.loadIcon("development/Server")));
        //PROFILE: System.out.println("    After server settings panel: " + (-time + (time = System.currentTimeMillis())));
        
        PanelTreeNode deliveryNode = new PanelTreeNode(rootNode, getPanelSend(), _("Delivery"), Utils.loadIcon("general/SendMail"));
        //PROFILE: System.out.println("    After send panel: " + (-time + (time = System.currentTimeMillis())));
        deliveryNode.setChildren(new PanelTreeNode[] {
                new PanelTreeNode(deliveryNode, new CoverPanel(), _("Cover page"), Utils.loadIcon("general/ComposeMail"))
        });
        rootChilds.add(deliveryNode);
        //PROFILE: System.out.println("    After cover panel: " + (-time + (time = System.currentTimeMillis())));
        
        rootChilds.add(new PanelTreeNode(rootNode, getPanelPlugins(), _("Plugins & JDBC"), Utils.loadIcon("development/Jar")));
        //PROFILE: System.out.println("    After plugins panel: " + (-time + (time = System.currentTimeMillis())));
        
        JLabel tableLabel = new JLabel(_("Please select on the left which table you want to edit."));
        tableLabel.setHorizontalAlignment(JLabel.CENTER);
        tableLabel.setVerticalAlignment(JLabel.CENTER);
        tableLabel.setHorizontalTextPosition(JLabel.CENTER);
        tableLabel.setVerticalTextPosition(JLabel.CENTER);
        tableLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        PanelTreeNode tables = new PanelTreeNode(rootNode, tableLabel, _("Tables"), null);
        MessageFormat tableFormat = new MessageFormat(_("Table \"{0}\""));
        tables.setChildren(new PanelTreeNode[] {
                new PanelTreeNode(tables, getPanelRecvFmt(), _("Received"), Utils.loadCustomIcon("received.gif"), tableFormat.format(new Object[] {_("Received")})),
                new PanelTreeNode(tables, getPanelSentFmt(), _("Sent"), Utils.loadCustomIcon("sent.gif"), tableFormat.format(new Object[] {_("Sent")})),
                new PanelTreeNode(tables, getPanelSendingFmt(), _("Transmitting"), Utils.loadCustomIcon("sending.gif"), tableFormat.format(new Object[] {_("Transmitting")})),
                new PanelTreeNode(tables, new ArchivePanel(), _("Archive"), Utils.loadCustomIcon("archive.gif"), tableFormat.format(new Object[] {_("Archive")}))
        });
        rootChilds.add(tables);

        for (PluginUI puc : PluginManager.pluginUIs) {
            PanelTreeNode node = puc.createOptionsPanel(rootNode);
            if (node != null) {
                rootChilds.add(node);
            }
        }
        
        rootNode.setChildren(rootChilds);
        //PROFILE: System.out.println("    After table panels: " + (-time + (time = System.currentTimeMillis())));
    }
    
    private JComponent getMainPanel() {
        //PROFILE: long time = System.currentTimeMillis();
        createRootNode();
        //PROFILE: System.out.println("   After createRootNode: " + (-time + (time = System.currentTimeMillis())));
        mainTree = new JTree(new DefaultTreeModel(rootNode));
        mainTree.setRootVisible(false);
        mainTree.addTreeSelectionListener(new TreeSelectionListener() {

            private PanelTreeNode lastSel = null;

            public void valueChanged(TreeSelectionEvent e) {
                PanelTreeNode selNode = (PanelTreeNode)e.getPath().getLastPathComponent();
                if (selNode != null && selNode != lastSel) {
                    if (lastSel != null) {
                        lastSel.getPanel().setVisible(false);
                    }
                    selNode.getPanel().setVisible(true);
                    treeSelLabel.setText(selNode.getLongLabel());
                    lastSel = selNode;
                }
            }
        });
        mainTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                
                PanelTreeNode node = (PanelTreeNode)value;
                if (node.getIcon() != null) {
                    setIcon(node.getIcon());
                }
                return this;
            } 
        });
        mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        mainTree.setRowHeight(20);
        mainTree.setAlignmentX(Component.LEFT_ALIGNMENT);
        //PROFILE: System.out.println("   After create main tree: " + (-time + (time = System.currentTimeMillis())));
        
        tabPanel = new JPanel(false);
        tabPanel.setLayout(new OverlayLayout(tabPanel));
        addTreeToPanel(tabPanel, rootNode, null);
        tabPanel.setMinimumSize(new Dimension(0,0));
        //PROFILE: System.out.println("   After create tab panel: " + (-time + (time = System.currentTimeMillis())));
        
        treeSelLabel = new JLabel("Current selection");
        treeSelLabel.setFont(treeSelLabel.getFont().deriveFont(Font.BOLD, 16));
        treeSelLabel.setHorizontalAlignment(JLabel.LEFT);
        treeSelLabel.setHorizontalTextPosition(JLabel.LEFT);     
        treeSelLabel.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        
        treeSelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Box rightPanel = Box.createVerticalBox();
        rightPanel.add(treeSelLabel);
        rightPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        rightPanel.add(tabPanel);
        
        JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mainTree), rightPanel);
        mainPanel.setDividerLocation(130);
        //PROFILE: System.out.println("   After create main panel: " + (-time + (time = System.currentTimeMillis())));
        return mainPanel;
    }

    /**
     * Adds the node and it children to the tabPanel and to optionsPages (if instanceof OptionsPage)
     * @param tabPanel
     * @param node
     * @param pathToParent
     */
    private void addTreeToPanel(JPanel tabPanel, PanelTreeNode node, TreePath pathToParent) {
        JComponent comp = node.getPanel();
        if (comp != null) {
            tabPanel.add(comp);
            comp.setVisible(false);
            comp.setOpaque(true);
            if (comp instanceof OptionsPage) {
                optionsPages.add((OptionsPage)comp);
            }
        }
        if (node.getChildren() != null) {
            TreePath pathToMe = (pathToParent == null) ? new TreePath(node) :
                pathToParent.pathByAddingChild(node);
            
            for (PanelTreeNode child : node.getChildren()) {
                addTreeToPanel(tabPanel, child, pathToMe);
            }
            mainTree.expandPath(pathToMe);
        }
    }
    
    private JPanel getPanelCommon() {        
        if (PanelCommon == null) {
            double[][] tablelay = {
                    {border, 0.4, border, TableLayout.FILL, border},
                    {border, 0.75, border, TableLayout.PREFERRED, TableLayout.FILL, border }
            };
            PanelCommon = new JPanel(new TableLayout(tablelay), false);
            

            
            //PanelCommon.add(getPanelServer(), "1,1");
            PanelCommon.add(getPanelUI(), "1,1");
            PanelCommon.add(getPanelNewFaxAction(), "3,1");
            PanelCommon.add(getPanelDateFormat(), "1,3");
            PanelCommon.add(getPanelUpdates(), "3,3");
        }
        return PanelCommon;
    }

    private JPanel getPanelUpdates() {
        if (panelUpdates == null) {
            panelUpdates = new JPanel(new BorderLayout(), false);
            panelUpdates.setBorder(BorderFactory.createTitledBorder(_("Update check")));
            
            checkAutoCheckForUpdate = new JCheckBox(_("Automatically check for updates"));
            
            panelUpdates.add(checkAutoCheckForUpdate, BorderLayout.CENTER);
        }
        return panelUpdates;
    }
    
    private JPanel getPanelServer() {
        if (panelServer == null) {
            double[][] tablelay = {
                    {border, 0.22, border, 0.22, border, 0.22, border, TableLayout.FILL, border},
                    new double[11]
            };
            double rowh = 1 / (double)(tablelay[1].length - 2);
            tablelay[1][0] = 0;
            tablelay[1][tablelay[1].length - 1] = border;
            Arrays.fill(tablelay[1], 1, tablelay[1].length - 2, rowh);
            tablelay[1][tablelay[1].length - 2] = TableLayout.FILL;
            
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
            
            addWithLabel(panelServer, textHost, _("Host name:"), "1, 2, 5, 2, f, c");
            addWithLabel(panelServer, textPort, _("Port:"), "7, 2, f, c");
            addWithLabel(panelServer, textUser, _("Username:"), "1, 4, 5, 4, f, c");
            panelServer.add(checkAskUsername, "6,4,7,4,f,c");
            //addWithLabel(panelServer, textPassword, _("Password:"), "5, 4, 7, 4, f, c");
            addWithLabel(panelServer, textPassword, _("Password:"), "1, 6, 5, 6, f, c");
            panelServer.add(checkAskPassword, "6, 6, 7, 6, f, c");
            addWithLabel(panelServer, textAdminPassword, _("Admin Password:"), "1, 8, 5, 8, f, c");
            panelServer.add(checkAskAdminPassword, "6, 8, 7, 8, f, c");
            
            panelServer.add(checkPasv, "1, 9, 7, 9");
        }
        return panelServer;
    }
    
    private JPanel getPanelUI() {
        if (panelUI == null) {
            final int rowCount = 11;
            final double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            final double rowh = 2 / (double)(rowCount + 1);
            //tablelay[1][0] = border;
            tablelay[1][rowCount - 1] = border;
            for (int i = 0; i < rowCount-4; i++) {
                if (i%2 == 0) {
                    tablelay[1][i] = TableLayout.PREFERRED;
                } else {
                    tablelay[1][i] = rowh;
                }
            }
            tablelay[1][rowCount - 3] = tablelay[1][rowCount - 4] = rowh;
            tablelay[1][rowCount - 2] =  TableLayout.FILL;
            
            panelUI = new JPanel(new TableLayout(tablelay), false);
            panelUI.setBorder(BorderFactory.createTitledBorder(_("User interface")));
            
            //comboNewFaxAction = new JComboBox(Utils.newFaxActions);
            
            comboLang = new JComboBox(YajLanguage.values());
            
            lookAndFeels = LF_Entry.getLookAndFeelList();
            comboLookAndFeel = new JComboBox(lookAndFeels);
            comboLookAndFeel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LF_Entry sel = (LF_Entry)comboLookAndFeel.getSelectedItem();
                    if (changedLF || !sel.className.equals(foEdit.lookAndFeel)) {
                        Utils.setLookAndFeel(sel.className);
                        
                        SwingUtilities.updateComponentTreeUI(OptionsWin.this);
                        changedLF = true;
                    }
                }
            });
            
            comboSendWinStyle = new JComboBox(SendWinStyle.values());
            
            checkShowTrayIcon = new JCheckBox(_("Show tray icon"));
            checkShowTrayIcon.setToolTipText(_("Show a system tray icon (works only with Java 6 or higher)"));
            checkShowTrayIcon.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    final boolean selected = checkShowTrayIcon.isSelected();
                    checkMinimizeToTray.setEnabled(selected);
                    checkMinimizeToTrayOnMainWinClose.setEnabled(selected);
                } 
            });
            
            checkMinimizeToTray = new JCheckBox(_("Minimize to tray"));
            checkMinimizeToTray.setEnabled(false);
            //checkMinimizeToTray.setToolTipText(_("Minimize to system tray (works only with Java 6 or higher)"));

            checkMinimizeToTrayOnMainWinClose = new JCheckBox("<html>" + _("Minimize to tray when main window is closed") + "</html>");
            checkMinimizeToTrayOnMainWinClose.setEnabled(false);
            
            addWithLabel(panelUI, comboLang, _("Language:"), "1, 1, 1, 1, f, c");
            addWithLabel(panelUI, comboLookAndFeel, _("Look and Feel:"), "1, 3, 1, 3, f, c");
            addWithLabel(panelUI, comboSendWinStyle, _("Style of send dialog:"), "1, 5, 1, 5, f, c");
            //addWithLabel(panelUI, comboNewFaxAction, "<html>" + _("When a new fax is received:") + "</html>", "1, 3, 1, 3, f, c");
            
            panelUI.add(checkShowTrayIcon, "1,7,1,7,f,c");
            panelUI.add(checkMinimizeToTray, "1,8,1,8,f,c");
            panelUI.add(checkMinimizeToTrayOnMainWinClose, "1,9,1,9,f,c");
        }
        return panelUI;
    }
    
    private JPanel getPanelNewFaxAction() {
        if (panelNewFaxAction == null) {
            final int rowCount = 7;
            double[][] tablelay = {
                    {border, 4*border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            double rowh = 1 / (double)(rowCount - 2);
            tablelay[1][0] = border;
            tablelay[1][rowCount - 1] = border;
            Arrays.fill(tablelay[1], 1, rowCount - 1, rowh);
            tablelay[1][rowCount - 2] = TableLayout.FILL;
            
            panelNewFaxAction = new JPanel(new TableLayout(tablelay), false);
            panelNewFaxAction.setBorder(BorderFactory.createTitledBorder(_("Actions after receiving a new fax:")));
            
            checkNewFax_Beep = new JCheckBox(_("Beep"));
            checkNewFax_ToFront = new JCheckBox(_("Bring to front"));
            checkNewFax_Open = new JCheckBox(_("Open in viewer"));
            checkNewFax_Open.addChangeListener(new ChangeListener() {
               public void stateChanged(ChangeEvent e) {
                   checkNewFax_MarkAsRead.setEnabled(checkNewFax_Open.isSelected());
               }
            });
            checkNewFax_MarkAsRead = new JCheckBox(_("And mark as read"));
            checkNewFax_MarkAsRead.setEnabled(false);
            checkNewFax_BlinkTrayIcon = new JCheckBox(_("Show flashing tray icon"));
            
            panelNewFaxAction.add(checkNewFax_Beep, "1,1,2,1");
            panelNewFaxAction.add(checkNewFax_ToFront, "1,2,2,2");
            panelNewFaxAction.add(checkNewFax_Open, "1,3,2,3");
            panelNewFaxAction.add(checkNewFax_MarkAsRead, "2,4");
            panelNewFaxAction.add(checkNewFax_BlinkTrayIcon, "1,5,2,5");
        }
        return panelNewFaxAction;
    }
    
    private JPanel getPanelDateFormat() {
        if (panelDateFormat == null) {
            double[][] tablelay = {
                    {border, 0.5, border, TableLayout.FILL, border},
                    {border, TableLayout.FILL, TableLayout.PREFERRED, border}
            };
            panelDateFormat = new JPanel(new TableLayout(tablelay),false);
            panelDateFormat.setBorder(BorderFactory.createTitledBorder(_("Date and Time Format:")));
            
            comboDateFormat = new JComboBox(DateStyle.getAvailableDateStyles());
            comboDateFormat.setEditable(true);
            comboTimeFormat = new JComboBox(DateStyle.getAvailableTimeStyles());
            comboTimeFormat.setEditable(true);
            
            addWithLabel(panelDateFormat, comboDateFormat, _("Date format:"), "1,2");
            addWithLabel(panelDateFormat, comboTimeFormat, _("Time format:"), "3,2");
        }
        return panelDateFormat;
    }
    
    private JPanel getPanelServerRetrieval() {
        if (panelServerRetrieval == null) {
            final int rowCount = 13;
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            double rowh = 1 / (double)(rowCount - 1);
            //tablelay[1][0] = border;
            tablelay[1][rowCount - 1] = border;
            Arrays.fill(tablelay[1], 0, rowCount - 1, rowh);
            tablelay[1][3] = tablelay[1][5]  = rowh*0.5;
            tablelay[1][8] = tablelay[1][10] = rowh*1.3333333333;
            tablelay[1][rowCount - 2] = TableLayout.FILL;
            
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
            
            addWithLabel(panelServerRetrieval, spinOffset, _("Date/Time offset:"), "1, 1, 1, 1, f, c");
            spinOffset.setToolTipText(_("Offset to be added to dates received from the HylaFAX server before displaying them."));
            panelServerRetrieval.add(checkPCLBug, "1, 2, 1, 3");
            panelServerRetrieval.add(checkUseDisconnected, "1, 4, 1, 5");
            //panelServerRetrieval.add(checkPreferTIFF, "1, 6, 1, 7");
            
            addWithLabel(panelServerRetrieval, spinTableInterval, "<html>" + _("Table refresh interval (secs.):") + "</html>", "1, 7, 1, 7, f, c");
            addWithLabel(panelServerRetrieval, spinStatusInterval, "<html>" + _("Server status refresh interval (secs.):") + "</html>", "1, 9, 1, 9, f, c");
            addWithLabel(panelServerRetrieval, spinSocketTimeout, "<html>" + _("Server socket timeout (secs):") + "</html>", "1, 11, 1, 11, f, c"); 
        }
        return panelServerRetrieval;
    }
    
    private JPanel getPanelSend() {
        if (panelSend == null) {
            final int rowCount = 13;
            double[][] tablelay = {
                    {border,  0.5, border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            double rowh = 1 / (double)(rowCount - 2);
            tablelay[1][0] = border;
            tablelay[1][rowCount - 1] = border;
            Arrays.fill(tablelay[1], 1, rowCount - 2, rowh);
            tablelay[1][rowCount - 2] = TableLayout.FILL;
            
            panelSend = new JPanel(new TableLayout(tablelay), false);
            panelSend.setBorder(BorderFactory.createTitledBorder(_("Delivery settings")));
           
            textNotifyAddress = new JTextField();
            textNotifyAddress.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            
            comboTZone = new JComboBox(FaxTimezone.values());
            comboNotify = new JComboBox(FaxNotification.values());
            comboNotify.setRenderer(new IconMap.ListCellRenderer());
            comboPaperSize = new JComboBox(PaperSize.values());
            comboResolution = new JComboBox(FaxResolution.values());
            
            //availableModems = HylaModem.defaultModems;
            comboModem = new JComboBox(availableModems.toArray());
            comboModem.setEditable(true);
            
            spinMaxDial = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
            spinMaxTry = new JSpinner(new SpinnerNumberModel(6, 1, 100, 1));
            spinKillTime= new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));
            
            checkArchiveSentFaxes = new JCheckBox(_("Archive sent fax jobs"));
            
            addWithLabel(panelSend, textNotifyAddress, _("E-mail address for notifications:"), "1, 2, 3, 2, f, c");
            addWithLabel(panelSend, comboNotify, _("Notify when:"), "1, 4, 1, 4, f, c");
            addWithLabel(panelSend, comboModem, _("Modem:"), "3, 4, 3, 4, f, c");
            addWithLabel(panelSend, comboTZone, _("Time zone:"), "1, 6, f, c");
            addWithLabel(panelSend, comboResolution, _("Resolution:"), "3, 6, f, c");
            addWithLabel(panelSend, comboPaperSize, _("Paper size:"), "1, 8, f, c" );
            addWithLabel(panelSend, spinKillTime, _("Cancel job after (minutes):"), "3, 8, f, c");
            addWithLabel(panelSend, spinMaxDial, _("Maximum dials:"), "1, 10, f, c");
            addWithLabel(panelSend, spinMaxTry, _("Maximum tries:"), "3, 10, f, c");    
            panelSend.add(checkArchiveSentFaxes, "1,11,f,c");
        }
        return panelSend;
    }
    
    private fmtEditor<RecvFormat> getPanelRecvFmt() {
        if (PanelRecvFmt == null) {
            PanelRecvFmt = new fmtEditor<RecvFormat>(RecvFormat.values(), recvfmt, Collections.<RecvFormat>emptyList(), new FmtItemRenderer(), FmtItemDescComparator.<RecvFormat>getInstance(), null, _("Selected columns:"), _("Available columns:")); //Arrays.asList(Utils.requiredRecvFmts));
        }
        return PanelRecvFmt;
    }
    
    private fmtEditor<JobFormat> getPanelSendingFmt() {
        if (PanelSendingFmt == null) {
            PanelSendingFmt = new fmtEditor<JobFormat>(JobFormat.values(), sendingfmt, Collections.<JobFormat>emptyList(), new FmtItemRenderer(), FmtItemDescComparator.<JobFormat>getInstance(), null, _("Selected columns:"), _("Available columns:")); // Arrays.asList(Utils.requiredSendingFmts));
        }
        return PanelSendingFmt;
    }
    
    private fmtEditor<JobFormat>  getPanelSentFmt() {
        if (PanelSentFmt == null) {
            PanelSentFmt = new fmtEditor<JobFormat>(JobFormat.values(), sentfmt, Collections.<JobFormat>emptyList(), new FmtItemRenderer(), FmtItemDescComparator.<JobFormat>getInstance(), null, _("Selected columns:"), _("Available columns:")); //Arrays.asList(Utils.requiredSentFmts));
        }
        return PanelSentFmt;
    }
    
    public OptionsWin(FaxOptions foEdit, Frame owner, List<HylaModem> availableModems) {
        super(owner);
        this.foEdit = foEdit;
        this.availableModems = availableModems;
        recvfmt = new ArrayList<RecvFormat>(foEdit.recvfmt);
        sentfmt = new ArrayList<JobFormat>(foEdit.sentfmt);
        sendingfmt = new ArrayList<JobFormat>(foEdit.sendingfmt);
        
        initialize();
    }
    
    static class LF_Entry {
        public String name;
        public String className;
        
        public LF_Entry(String name, String className) {
            this.name = name;
            this.className = className;
        }
        
        public LF_Entry(UIManager.LookAndFeelInfo lfi) {
            this(lfi.getName(), lfi.getClassName());
        }
        
        public String toString() {
            return name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof LF_Entry) {
                return ((LF_Entry)obj).className.equals(className);
            } else if (obj instanceof String) {
                return obj.equals(className);
            }
            return false;
        }
        
        public static Vector<LF_Entry> getLookAndFeelList() {
            UIManager.LookAndFeelInfo[] lfiList = UIManager.getInstalledLookAndFeels();
            Vector<LF_Entry> entries = new Vector<LF_Entry>(lfiList.length + 2);
            entries.add(new LF_Entry(Utils._("(System native)"), FaxOptions.LOOKANDFEEL_SYSTEM));
            entries.add(new LF_Entry(Utils._("(Crossplatform)"), FaxOptions.LOOKANDFEEL_CROSSPLATFORM));
            for (UIManager.LookAndFeelInfo lfi : lfiList) {
                entries.add(new LF_Entry(lfi));
            }
            return entries;
        }
    }
    
   
    // Does not work correctly :-(
    /*
     * Refreshes the Look&Feel for the complete application
     */
    static void refreshLF() {
        for (Frame f: Frame.getFrames()) {
            //refreshLF(f);
            SwingUtilities.updateComponentTreeUI(f);
        }
    }

    private String getModem() {
        Object sel = comboModem.getSelectedItem();
        if (Utils.debugMode) {
            log.info("Selected modem (" + sel.getClass().getCanonicalName() + "): " + sel);
        }
        if (sel instanceof HylaModem) {
            return ((HylaModem)sel).getInternalName();
        } else {
            String str = sel.toString();
            int pos = str.indexOf(' '); // Use part up to the first space
            if (pos == -1)
                return str;
            else
                return str.substring(0, pos);
        }
    }
    
    private void setDateStyle(JComboBox combo, String optsField, DateStyle[] availableVals) {
        Object selStyle = optsField;
        for (DateStyle style : availableVals) {
            if (style.getSaveString().equals(selStyle)) {
                selStyle = style;
                break;
            }
        }
//        if (selStyle instanceof String) {
//            selStyle = new SimpleDateFormat((String)selStyle, Utils.getLocale()).toLocalizedPattern();
//        }
        combo.setSelectedItem(selStyle);
    }
    
    private String getDateStyle(JComboBox combo) {
        Object sel = combo.getSelectedItem();

        if (sel instanceof DateStyle) {
            return ((DateStyle)sel).getSaveString();
        } else {
            //SimpleDateFormat fmt = new SimpleDateFormat("", Utils.getLocale());
            //fmt.applyLocalizedPattern(sel.toString());
            //return fmt.toPattern();
            return sel.toString();
        }
    }
    
    
    /**
     * Saves the settings. Returns true if they were saved successfully
     * @param foEdit
     * @return
     */
    public boolean saveSettings(FaxOptions foEdit) {
        
        if (!validateInput()) {
            return false;
        }
        
        try {
            foEdit.port = Integer.parseInt(textPort.getText());
            
            foEdit.maxDial = ((Integer)spinMaxDial.getValue()).intValue();
            foEdit.maxTry = ((Integer)spinMaxTry.getValue()).intValue();
            foEdit.dateOffsetSecs = (Integer)spinOffset.getValue();
            foEdit.tableUpdateInterval = (int)(((Double)spinTableInterval.getValue()).doubleValue() * 1000);
            foEdit.statusUpdateInterval = (int)(((Double)spinStatusInterval.getValue()).doubleValue() * 1000);
            foEdit.socketTimeout = (int)(((Double)spinSocketTimeout.getValue()).doubleValue() * 1000);
            foEdit.killTime = (Integer)spinKillTime.getValue();
            
            foEdit.notifyAddress = textNotifyAddress.getText();
            foEdit.host = textHost.getText();
            foEdit.user = textUser.getText();
            foEdit.pass.setPassword(new String(textPassword.getPassword()));
            foEdit.AdminPassword.setPassword(new String(textAdminPassword.getPassword()));
            
            foEdit.notifyWhen = (FaxNotification)comboNotify.getSelectedItem();
            foEdit.paperSize = (PaperSize)comboPaperSize.getSelectedItem();
            foEdit.resolution = (FaxResolution)comboResolution.getSelectedItem();
            foEdit.tzone = (FaxTimezone)comboTZone.getSelectedItem();
            //foEdit.newFaxAction = (FaxIntProperty)comboNewFaxAction.getSelectedItem();
            foEdit.sendWinStyle = (SendWinStyle)comboSendWinStyle.getSelectedItem();
            
            String newLF = ((LF_Entry)comboLookAndFeel.getSelectedItem()).className;
            if (!newLF.equals(foEdit.lookAndFeel)) {
                foEdit.lookAndFeel = newLF;
                //JOptionPane.showMessageDialog(OptionsWin.this, _("You must restart the program for a change of the look&feel to take effect."), _("Options"), JOptionPane.INFORMATION_MESSAGE);
                
                Utils.setLookAndFeel(newLF);
                refreshLF();
            }
            
            YajLanguage newLang = (YajLanguage)comboLang.getSelectedItem();
            if (!newLang.equals(foEdit.locale)) {
                foEdit.locale = newLang;
                JOptionPane.showMessageDialog(OptionsWin.this, _("You must restart the program for the change of the language to take effect."), _("Options"), JOptionPane.INFORMATION_MESSAGE);
            }
            
            foEdit.pasv = checkPasv.isSelected();
            foEdit.pclBug = checkPCLBug.isSelected();
            foEdit.askPassword = checkAskPassword.isSelected();
            foEdit.askAdminPassword = checkAskAdminPassword.isSelected();
            foEdit.askUsername = checkAskUsername.isSelected();
            //foEdit.preferRenderedTIFF = checkPreferTIFF.isSelected();
            foEdit.useDisconnectedMode = checkUseDisconnected.isSelected();
            foEdit.showTrayIcon = checkShowTrayIcon.isSelected();
            foEdit.minimizeToTray = checkMinimizeToTray.isSelected();
            foEdit.minimizeToTrayOnMainWinClose = checkMinimizeToTrayOnMainWinClose.isSelected();
            foEdit.automaticallyCheckForUpdate = checkAutoCheckForUpdate.isSelected();
            foEdit.archiveSentFaxes = checkArchiveSentFaxes.isSelected();
            
            int val = 0;
            if (checkNewFax_Beep.isSelected())
                val |= FaxOptions.NEWFAX_BEEP;
            if (checkNewFax_ToFront.isSelected())
                val |= FaxOptions.NEWFAX_TOFRONT;
            if (checkNewFax_Open.isSelected())
                val |= FaxOptions.NEWFAX_VIEWER;
            if (checkNewFax_MarkAsRead.isSelected())
                val |= FaxOptions.NEWFAX_MARKASREAD;
            if (checkNewFax_BlinkTrayIcon.isSelected())
                val |= FaxOptions.NEWFAX_BLINKTRAYICON;
            foEdit.newFaxAction = val;
            
            foEdit.recvfmt.clear();
            foEdit.recvfmt.addAll(recvfmt);
            foEdit.sentfmt.clear();
            foEdit.sentfmt.addAll(sentfmt);
            foEdit.sendingfmt.clear();
            foEdit.sendingfmt.addAll(sendingfmt);
            
            foEdit.defaultModem = getModem();
            foEdit.dateStyle = getDateStyle(comboDateFormat);
            foEdit.timeStyle = getDateStyle(comboTimeFormat);
            
            for (OptionsPage page : optionsPages) {
                page.saveSettings(foEdit);
            }
            
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
            
            if (PluginManager.updatePluginList(pluginTableModel.getEntries())) {
                JOptionPane.showMessageDialog(OptionsWin.this, Utils._("You will need to restart the program for the changes to the list of plugins and JDBC drivers to take full effect."), Utils._("Plugins & JDBC"), JOptionPane.INFORMATION_MESSAGE);
            }
            
            
        } catch (Exception e1) {
            ExceptionDialog.showExceptionDialog(OptionsWin.this, Utils._("Error saving the settings:"), e1);
            return false;
        }
        return true;
    }
    
    /**
     * Validate input. Return true if input is valid.
     * @return
     */
    private boolean validateInput() {
        
        if (textHost.getText().length() == 0) {
            focusComponent(textHost);
            JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter a host name."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (textUser.getText().length() == 0) {
            focusComponent(textUser);
            JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter a user name."), _("Error"), JOptionPane.ERROR_MESSAGE);
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
            focusComponent(textPort);
            JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter a valid port number."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            DateFormat fmt = DateStyle.getDateFormatFromString(getDateStyle(comboDateFormat));
            fmt.format(new Date());
        } catch (Exception e) {
            focusComponent(comboDateFormat);
            JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter a valid date format:") + '\n' + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            DateFormat fmt = DateStyle.getTimeFormatFromString(getDateStyle(comboTimeFormat));
            fmt.format(new Date());
        } catch (Exception e) {
            focusComponent(comboTimeFormat);
            JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter a valid time format.") + '\n' + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        
        for (OptionsPage page : optionsPages) {
            if (!page.validateSettings(OptionsWin.this)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void focusComponent(Component comp) {
        focusTab(comp);
        comp.requestFocusInWindow();
    }
    
    private void focusTab(Component comp) {
        Component parent = comp.getParent();
        if (parent == null || parent instanceof Window) {
            return;
        } else if (parent == tabPanel) {
            selectTreeNodeForPanel(rootNode, comp, null);
        } else {
            focusTab(parent);
        }
    }
    
    private void selectTreeNodeForPanel(PanelTreeNode node, Component panel, TreePath pathToParent) {
        TreePath pathToMe = (pathToParent == null) ? new TreePath(node) :
            pathToParent.pathByAddingChild(node);
        if (node.getPanel() == panel) {
            mainTree.setSelectionPath(pathToMe);
        } else if (node.getChildren() != null) {            
            for (PanelTreeNode child : node.getChildren()) {
                selectTreeNodeForPanel(child, panel, pathToMe);
            }
        }
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
                    String res = sel.showConfigDialog(OptionsWin.this, persistenceConfigs.get(sel.getKey()));
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

    private JPanel getPanelPlugins() {
        if (panelPlugins == null) {
            double[][] dLay = {
                    {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                    {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border}
            };
            panelPlugins = new JPanel(new TableLayout(dLay), false);
            
            pluginTableModel = new PluginTableModel();
            tablePlugins = new JTable(pluginTableModel); /* {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer,
                        int row, int column) {
                    Component comp = super.prepareRenderer(renderer, row, column);
                    PluginTableModel.Entry entry = ((PluginTableModel)this.dataModel).getEntry(row);
                    if (getSelectedRow() != row) {
                        if (!entry.persistent) {
                            comp.setBackground(UIManager.getColor("TextField.inactiveBackground"));
                            //comp.setForeground(UIManager.getColor("TextField.inactiveForeground"));
                        } else {
                            comp.setBackground(getBackground());
                            //comp.setForeground(getForeground());
                        }
                    }
                    return comp;
                }  
            };*/
            tablePlugins.setDefaultRenderer(IconMap.class, new IconMap.TableCellRenderer());
            tablePlugins.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selRow = tablePlugins.getSelectedRow();
                        buttonRemovePlugin.setEnabled(selRow >= 0) ; // && pluginTableModel.getEntry(selRow).persistent);
                    }
                }
                
            });
            tablePlugins.getColumnModel().getColumn(0).setPreferredWidth(300);
            JTableTABAction.replaceTABWithNextRow(tablePlugins);
            
            JScrollPane scrollTable = new JScrollPane(tablePlugins);
            
            ActionListener actionListener = new ActionListener() {
                JFileChooser fileChooser;
                
                private File chooseFile(String title) {
                    if (fileChooser == null) {
                        fileChooser = new yajhfc.util.SafeJFileChooser();
                        fileChooser.setAcceptAllFileFilterUsed(false);
                        fileChooser.addChoosableFileFilter(new ExampleFileFilter("jar", Utils._("JAR files")));
                    }
                    fileChooser.setDialogTitle(title);
                    if (fileChooser.showOpenDialog(OptionsWin.this) == JFileChooser.APPROVE_OPTION) {
                        return fileChooser.getSelectedFile();
                    } else {
                        return null;
                    }
                }
                
                public void actionPerformed(ActionEvent e) {
                    String actCmd = e.getActionCommand();
                    if (actCmd.equals("addJDBC")) {
                        File jar = chooseFile(Utils._("Add JDBC driver"));
                        if (jar == null)
                            return;
                        
                        pluginTableModel.addItem(jar, PluginType.JDBCDRIVER);
                    } else if (actCmd.equals("addPlugin")) {
                        File jar = chooseFile(Utils._("Add plugin"));
                        if (jar == null)
                            return;
                        
                        if (!PluginManager.isValidPlugin(jar)) {
                            JOptionPane.showMessageDialog(OptionsWin.this, MessageFormat.format(Utils._("The file {0} is not a valid YajHFC plugin!"), jar), Utils._("Add plugin"), JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        pluginTableModel.addItem(jar, PluginType.PLUGIN);
                    } else if (actCmd.equals("remove")) {
                        int idx = tablePlugins.getSelectedRow();
                        if (idx >= 0) {
                            pluginTableModel.removeItemAt(idx);
                        }
                    } else 
                        assert(false);
                }
            };
            buttonAddJDBC = new JButton(_("Add JDBC driver") + "...", Utils.loadIcon("development/JarAdd"));
            buttonAddJDBC.addActionListener(actionListener);
            buttonAddJDBC.setActionCommand("addJDBC");
            buttonAddPlugin = new JButton(_("Add plugin") +  "...", Utils.loadIcon("development/J2EEApplicationClientAdd"));
            buttonAddPlugin.addActionListener(actionListener);
            buttonAddPlugin.setActionCommand("addPlugin");
            buttonRemovePlugin = new JButton(_("Remove item"), Utils.loadIcon("general/Remove"));
            buttonRemovePlugin.addActionListener(actionListener);
            buttonRemovePlugin.setActionCommand("remove");
            buttonRemovePlugin.setEnabled(false);
            
            panelPlugins.add(scrollTable, "1,1,1,7,f,f");
            panelPlugins.add(buttonAddPlugin, "3,1");
            panelPlugins.add(buttonAddJDBC, "3,3");
            panelPlugins.add(buttonRemovePlugin, "3,5");
        }
        return panelPlugins;
    }
}
