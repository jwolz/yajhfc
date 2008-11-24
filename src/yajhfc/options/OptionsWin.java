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
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import yajhfc.FaxIntProperty;
import yajhfc.FaxOptions;
import yajhfc.FaxStringProperty;
import yajhfc.FileTextField;
import yajhfc.FmtItem;
import yajhfc.FmtItemDescComparator;
import yajhfc.FmtItemRenderer;
import yajhfc.HylaModem;
import yajhfc.IconMap;
import yajhfc.PaperSize;
import yajhfc.PluginManager;
import yajhfc.PluginTableModel;
import yajhfc.YajLanguage;
import yajhfc.Utils;
import yajhfc.PluginManager.PluginType;
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
    fmtEditor<FmtItem> PanelRecvFmt = null, PanelSentFmt = null, PanelSendingFmt = null;
        
    JPanel PanelButtons;
    JButton ButtonOK, ButtonCancel;
    
    JTextField textNotifyAddress, textHost, textUser, /*textViewer,*/ textPort;
    JPasswordField textPassword, textAdminPassword;
    JComboBox comboTZone, comboNotify, comboPaperSize, comboResolution; //, comboNewFaxAction;
    JComboBox comboLang, comboLookAndFeel, comboModem, comboSendWinStyle;
    JCheckBox checkPasv, checkPCLBug, checkAskPassword, checkAskAdminPassword;
    JSpinner spinMaxTry, spinMaxDial, spinOffset, spinKillTime, spinSocketTimeout;
    //JButton buttonBrowseViewer;
    FileTextField ftfFaxViewer, ftfPSViewer;
    
    ClipboardPopup clpDef;
    JPanel panelServer, panelSend, panelPaths, panelUI;
    
    JPanel panelServerRetrieval, panelNewFaxAction;
    JCheckBox checkNewFax_Beep, checkNewFax_ToFront, checkNewFax_Open, checkNewFax_MarkAsRead;
    JSpinner spinStatusInterval, spinTableInterval;
    
    JCheckBox checkPreferTIFF, checkUseDisconnected, checkMinimizeToTray;
    
    JPanel panelPersistence;
    JComboBox comboPersistenceMethods;
    JButton buttonConfigPersistence;
    Map<String,String> persistenceConfigs = new HashMap<String,String>();
    
    JPanel panelPlugins;
    JTable tablePlugins;
    PluginTableModel pluginTableModel;
    JButton buttonAddJDBC, buttonAddPlugin, buttonRemovePlugin;
    
    JTree mainTree;
    PanelTreeNode rootNode;
    JLabel treeSelLabel;
    JPanel tabPanel;

    FaxOptions foEdit = null;
    List<FmtItem> recvfmt, sentfmt, sendingfmt;
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
        //this.setSize(630, 460);
        this.setResizable(true);
        this.setTitle(_("Options"));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        clpDef = new ClipboardPopup();
        this.setContentPane(getJContentPane());
        
        modalResult = false;
        
        // Load values
        textNotifyAddress.setText(foEdit.notifyAddress);
        textHost.setText(foEdit.host);
        textPort.setText(String.valueOf(foEdit.port));
        textUser.setText(foEdit.user);
        textPassword.setText(foEdit.pass);
        textAdminPassword.setText(foEdit.AdminPassword);
        ftfFaxViewer.setText(foEdit.faxViewer);
        ftfPSViewer.setText(foEdit.psViewer);
        
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
            if (modem.getInternalName().equals(foEdit.defaultModem)) {
                selModem = modem;
                break;
            }
        }
        comboModem.setSelectedItem(selModem);
        
        checkPasv.setSelected(foEdit.pasv);
        checkPCLBug.setSelected(foEdit.pclBug);
        checkAskPassword.setSelected(foEdit.askPassword);
        checkAskAdminPassword.setSelected(foEdit.askAdminPassword);
        checkPreferTIFF.setSelected(foEdit.preferRenderedTIFF);
        checkUseDisconnected.setSelected(foEdit.useDisconnectedMode);
        checkMinimizeToTray.setSelected(foEdit.minimizeToTray);
        
        checkNewFax_Beep.setSelected((foEdit.newFaxAction & Utils.NEWFAX_BEEP) != 0);
        checkNewFax_ToFront.setSelected((foEdit.newFaxAction & Utils.NEWFAX_TOFRONT) != 0);
        checkNewFax_Open.setSelected((foEdit.newFaxAction & Utils.NEWFAX_VIEWER) != 0);
        checkNewFax_MarkAsRead.setSelected((foEdit.newFaxAction & Utils.NEWFAX_MARKASREAD) != 0);
        
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
               foEdit.optWinPos = getLocation();
               
               if (changedLF && !modalResult) {
                   Utils.setLookAndFeel(foEdit.lookAndFeel);
               }
            }
        });
        
        if (foEdit.optWinPos != null)
            this.setLocation(foEdit.optWinPos);
        else
            //this.setLocationByPlatform(true);
            Utils.setDefWinPos(this);
        
        // Small special handling for new users
        if (foEdit.host.length() == 0) {
            //TODO
            //TabMain.setSelectedIndex(1);
            mainTree.setSelectionPath(new TreePath(new Object[] { rootNode, rootNode.getChildAt(1) }));
        } else {
            mainTree.setSelectionRow(0);
        }
        
        this.pack();
    }
    
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel(new BorderLayout());
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
            ButtonOK.addActionListener(new ButtonOKActionListener());
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
    
    private JComponent getMainPanel() {
        
        rootNode = new PanelTreeNode(null, null, "root", null);
        List<PanelTreeNode> rootChilds = new ArrayList<PanelTreeNode>();

        rootChilds.add(new PanelTreeNode(rootNode, getPanelCommon(), _("Common"), Utils.loadIcon("general/Preferences")));
        rootChilds.add(new PanelTreeNode(rootNode, getPanelServerSettings(), _("Server"), Utils.loadIcon("development/Server")));
        PanelTreeNode deliveryNode = new PanelTreeNode(rootNode, getPanelSend(), _("Delivery"), Utils.loadIcon("general/SendMail"));
        CoverPanel coverPanel = new CoverPanel();
        optionsPages.add(coverPanel);
        deliveryNode.setChildren(new PanelTreeNode[] {
                new PanelTreeNode(deliveryNode, coverPanel, _("Cover page"), Utils.loadIcon("general/ComposeMail"))
        });
        rootChilds.add(deliveryNode);
        rootChilds.add(new PanelTreeNode(rootNode, getPanelPlugins(), _("Plugins & JDBC"), Utils.loadIcon("development/Jar")));

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
                new PanelTreeNode(tables,  getPanelRecvFmt(), _("Received"), Utils.loadCustomIcon("received.gif"), tableFormat.format(new Object[] {_("Received")})),
                new PanelTreeNode(tables, getPanelSentFmt(), _("Sent"), Utils.loadCustomIcon("sent.gif"), tableFormat.format(new Object[] {_("Sent")})),
                new PanelTreeNode(tables, getPanelSendingFmt(), _("Transmitting"), Utils.loadCustomIcon("sending.gif"), tableFormat.format(new Object[] {_("Transmitting")})),
        });
        rootChilds.add(tables);

        rootNode.setChildren(rootChilds);

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
        
        tabPanel = new JPanel(false);
        tabPanel.setLayout(new OverlayLayout(tabPanel));
        addTreeToPanel(tabPanel, rootNode, null);
        tabPanel.setMinimumSize(new Dimension(0,0));
        
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
        return mainPanel;
    }

    private void addTreeToPanel(JPanel tabPanel, PanelTreeNode node, TreePath pathToParent) {
        JComponent comp = node.getPanel();
        if (comp != null) {
            tabPanel.add(comp);
            comp.setVisible(false);
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
                    { border, 0.55, border, TableLayout.FILL, border }
            };
            PanelCommon = new JPanel(new TableLayout(tablelay), false);
            
            //PanelCommon.add(getPanelServer(), "1,1");
            PanelCommon.add(getPanelUI(), "1,1");
            PanelCommon.add(getPanelNewFaxAction(), "3,1");
            PanelCommon.add(getPanelPaths(), "1,3,3,3");
        }
        return PanelCommon;
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
            textHost.addMouseListener(clpDef);
            textPort = new JTextField();
            textPort.addMouseListener(clpDef);
            textPort.setInputVerifier(new IntVerifier(1, 65536));
            textUser = new JTextField();
            textUser.addMouseListener(clpDef);
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
            
            checkPasv = new JCheckBox(_("Use passive mode to fetch faxes"));
            
            addWithLabel(panelServer, textHost, _("Host name:"), "1, 2, 5, 2, f, c");
            addWithLabel(panelServer, textPort, _("Port:"), "7, 2, f, c");
            addWithLabel(panelServer, textUser, _("Username:"), "1, 4, 5, 4, f, c");
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
            final int rowCount = 8;
            final double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            final double rowh = 2 / (double)(rowCount - 1);
            //tablelay[1][0] = border;
            tablelay[1][rowCount - 1] = border;
            for (int i = 0; i < rowCount-2; i++) {
                if (i%2 == 0) {
                    tablelay[1][i] = TableLayout.PREFERRED;
                } else {
                    tablelay[1][i] = rowh;
                }
            }
            tablelay[1][rowCount - 2] = TableLayout.FILL;
            
            panelUI = new JPanel(new TableLayout(tablelay), false);
            panelUI.setBorder(BorderFactory.createTitledBorder(_("User interface")));
            
            //comboNewFaxAction = new JComboBox(Utils.newFaxActions);
            
            comboLang = new JComboBox(Utils.AvailableLocales);
            
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
            
            checkMinimizeToTray = new JCheckBox(_("Minimize to tray"));
            checkMinimizeToTray.setToolTipText(_("Minimize to system tray (works only with Java 6 or higher)"));

            addWithLabel(panelUI, comboLang, _("Language:"), "1, 1, 1, 1, f, c");
            addWithLabel(panelUI, comboLookAndFeel, _("Look and Feel:"), "1, 3, 1, 3, f, c");
            addWithLabel(panelUI, comboSendWinStyle, _("Style of send dialog:"), "1, 5, 1, 5, f, c");
            //addWithLabel(panelUI, comboNewFaxAction, "<html>" + _("When a new fax is received:") + "</html>", "1, 3, 1, 3, f, c");
            
            panelUI.add(checkMinimizeToTray, "1,6,1,6,f,c");
        }
        return panelUI;
    }
    
    private JPanel getPanelNewFaxAction() {
        if (panelNewFaxAction == null) {
            double[][] tablelay = {
                    {border, 4*border, TableLayout.FILL, border},
                    new double[6]
            };
            double rowh = 1 / (double)(tablelay[1].length - 2);
            tablelay[1][0] = border;
            tablelay[1][tablelay[1].length - 1] = border;
            Arrays.fill(tablelay[1], 1, tablelay[1].length - 1, rowh);
            tablelay[1][tablelay[1].length - 2] = TableLayout.FILL;
            
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
            
            panelNewFaxAction.add(checkNewFax_Beep, "1,1,2,1");
            panelNewFaxAction.add(checkNewFax_ToFront, "1,2,2,2");
            panelNewFaxAction.add(checkNewFax_Open, "1,3,2,3");
            panelNewFaxAction.add(checkNewFax_MarkAsRead, "2,4");
        }
        return panelNewFaxAction;
    }
    
    private JPanel getPanelServerRetrieval() {
        if (panelServerRetrieval == null) {
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[15]
            };
            double rowh = 1 / (double)(tablelay[1].length - 1);
            //tablelay[1][0] = border;
            tablelay[1][tablelay[1].length - 1] = border;
            Arrays.fill(tablelay[1], 0, tablelay[1].length - 1, rowh);
            tablelay[1][3] = tablelay[1][5] = tablelay[1][7] = rowh*0.5;
            tablelay[1][8] = tablelay[1][10] = tablelay[1][12] = rowh*1.5;
            tablelay[1][tablelay[1].length - 2] = TableLayout.FILL;
            
            panelServerRetrieval = new JPanel(new TableLayout(tablelay), false);
            panelServerRetrieval.setBorder(BorderFactory.createTitledBorder(_("General settings:")));
            
            checkPCLBug = new JCheckBox("<html>" + _("Use PCL file type bugfix") + "</html>");
            spinOffset = SpinnerDateOffsetEditor.createJSpinner();
            
            spinStatusInterval = new JSpinner(new SpinnerNumberModel(1, 0.5, 86400, 1));
            spinTableInterval = new JSpinner(new SpinnerNumberModel(3, 0.5, 86400, 1));
            spinSocketTimeout = new JSpinner(new SpinnerNumberModel((double)90, 0, 86400, 1));
            spinSocketTimeout.setToolTipText(_("The maximum time to wait for a interaction with the server to complete. Values below 5 are not recommended; 0 disables this timeout."));
            
            checkPreferTIFF = new JCheckBox("<html>" + _("Prefer rendered TIFF (experimental)") + "</html>");
            checkPreferTIFF.setToolTipText(_("Try to fetch the rendered TIFF from the HylaFAX server instead of the source file."));
            
            checkUseDisconnected = new JCheckBox("<html>" + _("Create new session for every action") + "</html>");
            checkUseDisconnected.setToolTipText(_("Connect to the server and log in for every action (e.g. view a fax, update tables, ...) and disconnect afterwards. This impairs performance but might work around some bugs."));
            
            addWithLabel(panelServerRetrieval, spinOffset, _("Date/Time offset:"), "1, 1, 1, 1, f, c");
            spinOffset.setToolTipText(_("Offset to be added to dates received from the HylaFAX server before displaying them."));
            panelServerRetrieval.add(checkPCLBug, "1, 2, 1, 3");
            panelServerRetrieval.add(checkUseDisconnected, "1, 4, 1, 5");
            panelServerRetrieval.add(checkPreferTIFF, "1, 6, 1, 7");
            
            addWithLabel(panelServerRetrieval, spinTableInterval, "<html>" + _("Table refresh interval (secs.):") + "</html>", "1, 9, 1, 9, f, c");
            addWithLabel(panelServerRetrieval, spinStatusInterval, "<html>" + _("Server status refresh interval (secs.):") + "</html>", "1, 11, 1, 11, f, c");
            addWithLabel(panelServerRetrieval, spinSocketTimeout, "<html>" + _("Server socket timeout (secs):") + "</html>", "1, 13, 1, 13, f, c"); 
        }
        return panelServerRetrieval;
    }
    
    private JPanel getPanelSend() {
        if (panelSend == null) {
            final int rowCount = 12;
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
            textNotifyAddress.addMouseListener(clpDef);
            
            comboTZone = new JComboBox(Utils.timezones);
            comboNotify = new JComboBox(Utils.notifications);
            comboNotify.setRenderer(new IconMap.ListCellRenderer());
            comboPaperSize = new JComboBox(Utils.papersizes);
            comboResolution = new JComboBox(Utils.resolutions);
            
            //availableModems = HylaModem.defaultModems;
            comboModem = new JComboBox(availableModems.toArray());
            comboModem.setEditable(true);
            
            spinMaxDial = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
            spinMaxTry = new JSpinner(new SpinnerNumberModel(6, 1, 100, 1));
            spinKillTime= new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));
            
            
            addWithLabel(panelSend, textNotifyAddress, _("E-mail address for notifications:"), "1, 2, 3, 2, f, c");
            addWithLabel(panelSend, comboNotify, _("Notify when:"), "1, 4, 1, 4, f, c");
            addWithLabel(panelSend, comboModem, _("Modem:"), "3, 4, 3, 4, f, c");
            addWithLabel(panelSend, comboTZone, _("Time zone:"), "1, 6, f, c");
            addWithLabel(panelSend, comboResolution, _("Resolution:"), "3, 6, f, c");
            addWithLabel(panelSend, comboPaperSize, _("Paper size:"), "1, 8, f, c" );
            addWithLabel(panelSend, spinKillTime, _("Cancel job after (minutes):"), "3, 8, f, c");
            addWithLabel(panelSend, spinMaxDial, _("Maximum dials:"), "1, 10, f, c");
            addWithLabel(panelSend, spinMaxTry, _("Maximum tries:"), "3, 10, f, c");    
            
        }
        return panelSend;
    }
    

    
    private JPanel getPanelPaths() {
        if (panelPaths == null) {         
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[6]
            };
            double rowh = 1 / (double)(tablelay[1].length - 2);
            tablelay[1][0] = 0;
            tablelay[1][tablelay[1].length - 1] = border;
            Arrays.fill(tablelay[1], 1, tablelay[1].length - 2, rowh);
            tablelay[1][tablelay[1].length - 2] = TableLayout.FILL;
            
            panelPaths = new JPanel(new TableLayout(tablelay), false);
            panelPaths.setBorder(BorderFactory.createTitledBorder(_("Path settings")));
            
            ftfFaxViewer = new ExeFileTextField();
            ftfFaxViewer.getJTextField().addMouseListener(clpDef);
            ftfPSViewer = new ExeFileTextField();
            ftfPSViewer.getJTextField().addMouseListener(clpDef);
            
            panelPaths.add(new JLabel(_("Command line for fax viewer: (insert %s as a placeholder for the filename)")), "1, 1 f b");
            panelPaths.add(ftfFaxViewer, "1, 2, f, c");
            panelPaths.add(new JLabel(_("Command line for Postscript viewer: (insert %s as a placeholder for the filename)")), "1, 3 f b");
            panelPaths.add(ftfPSViewer, "1, 4, f, c");
        }
        return panelPaths;
    }
    
    private JLabel addWithLabel(JPanel pane, JComponent comp, String text, String layout) {
        TableLayoutConstraints c = new TableLayoutConstraints(layout);
        
        pane.add(comp, c);
        
        JLabel lbl = new JLabel(text);
        lbl.setLabelFor(comp);
        c.row1 = c.row2 = c.row1 - 1;
        c.vAlign = TableLayoutConstraints.BOTTOM;
        c.hAlign = TableLayoutConstraints.LEFT;
        pane.add(lbl, c); 
        
        return lbl;
    }
    
    @SuppressWarnings("unchecked")
    private fmtEditor getPanelRecvFmt() {
        if (PanelRecvFmt == null) {
            PanelRecvFmt = new fmtEditor(Utils.recvfmts, recvfmt, Collections.EMPTY_LIST, new FmtItemRenderer(), FmtItemDescComparator.globalInstance, null, _("Selected columns:"), _("Available columns:")); //Arrays.asList(Utils.requiredRecvFmts));
        }
        return PanelRecvFmt;
    }
    
    @SuppressWarnings("unchecked")
    private fmtEditor getPanelSendingFmt() {
        if (PanelSendingFmt == null) {
            PanelSendingFmt = new fmtEditor(Utils.jobfmts, sendingfmt, Collections.EMPTY_LIST, new FmtItemRenderer(), FmtItemDescComparator.globalInstance, null, _("Selected columns:"), _("Available columns:")); // Arrays.asList(Utils.requiredSendingFmts));
        }
        return PanelSendingFmt;
    }
    
    @SuppressWarnings("unchecked")
    private fmtEditor getPanelSentFmt() {
        if (PanelSentFmt == null) {
            PanelSentFmt = new fmtEditor(Utils.jobfmts, sentfmt, Collections.EMPTY_LIST, new FmtItemRenderer(), FmtItemDescComparator.globalInstance, null, _("Selected columns:"), _("Available columns:")); //Arrays.asList(Utils.requiredSentFmts));
        }
        return PanelSentFmt;
    }
    
    public OptionsWin(FaxOptions foEdit, Frame owner, List<HylaModem> availableModems) {
        super(owner);
        this.foEdit = foEdit;
        this.availableModems = availableModems;
        recvfmt = new ArrayList<FmtItem>(foEdit.recvfmt);
        sentfmt = new ArrayList<FmtItem>(foEdit.sentfmt);
        sendingfmt = new ArrayList<FmtItem>(foEdit.sendingfmt);
        
        initialize();
    }
    
    static class ExeFileTextField extends FileTextField {
        protected String readTextFieldFileName() {
            return this.getText().replaceAll("\"|%s", "").trim();
        };
        
        @Override
        protected void writeTextFieldFileName(String fName) {
            if (fName.contains(" ")) 
                this.setText("\"" + fName + "\" %s");
            else
                this.setText(fName + " %s");
        }
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
        Frame[] frames = Frame.getFrames();
        for (Frame f: frames) {
            //refreshLF(f);
            SwingUtilities.updateComponentTreeUI(f);
        }
    }

    
    class ButtonOKActionListener implements ActionListener {
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
        
        public void actionPerformed(ActionEvent e) {
            
            if (!validateInput()) {
                return;
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
                foEdit.pass = new String(textPassword.getPassword());
                foEdit.faxViewer = ftfFaxViewer.getText();
                foEdit.psViewer = ftfPSViewer.getText();
                foEdit.AdminPassword = new String(textAdminPassword.getPassword());
                
                foEdit.notifyWhen = (FaxStringProperty)comboNotify.getSelectedItem();
                foEdit.paperSize = (PaperSize)comboPaperSize.getSelectedItem();
                foEdit.resolution = (FaxIntProperty)comboResolution.getSelectedItem();
                foEdit.tzone = (FaxStringProperty)comboTZone.getSelectedItem();
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
                foEdit.preferRenderedTIFF = checkPreferTIFF.isSelected();
                foEdit.useDisconnectedMode = checkUseDisconnected.isSelected();
                foEdit.minimizeToTray = checkMinimizeToTray.isSelected();
                
                int val = 0;
                if (checkNewFax_Beep.isSelected())
                    val |= Utils.NEWFAX_BEEP;
                if (checkNewFax_ToFront.isSelected())
                    val |= Utils.NEWFAX_TOFRONT;
                if (checkNewFax_Open.isSelected())
                    val |= Utils.NEWFAX_VIEWER;
                if (checkNewFax_MarkAsRead.isSelected())
                    val |= Utils.NEWFAX_MARKASREAD;
                foEdit.newFaxAction = val;
                
                foEdit.recvfmt.clear();
                foEdit.recvfmt.addAll(recvfmt);
                foEdit.sentfmt.clear();
                foEdit.sentfmt.addAll(sentfmt);
                foEdit.sendingfmt.clear();
                foEdit.sendingfmt.addAll(sendingfmt);
                
                foEdit.defaultModem = getModem();
                
                // Save persistence settings:
                String persistenceMethod = ((AvailablePersistenceMethod)comboPersistenceMethods.getSelectedItem()).getKey();
                String config = persistenceConfigs.get(persistenceMethod);
                if (config == null) config = "";
                if (!(persistenceMethod.equals(foEdit.persistenceMethod) && config.equals(foEdit.persistenceConfig))) {
                    PersistentReadState.getCurrent().persistReadState();
                    PersistentReadState.resetCurrent();
                }
                foEdit.persistenceMethod = persistenceMethod;
                foEdit.persistenceConfig = config;
                
                if (PluginManager.updatePluginList(pluginTableModel.getEntries())) {
                    JOptionPane.showMessageDialog(OptionsWin.this, Utils._("You will need to restart the program for the changes to the list of plugins and JDBC drivers to take full effect."), Utils._("Plugins & JDBC"), JOptionPane.INFORMATION_MESSAGE);
                }
                
                for (OptionsPage page : optionsPages) {
                    page.saveSettings(foEdit);
                }
                
            } catch (Exception e1) {
                ExceptionDialog.showExceptionDialog(OptionsWin.this, Utils._("Error saving the settings:"), e1);
                return;
            }
            
            modalResult = true;
            dispose();
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
            
            if (ftfFaxViewer.getText().length() == 0) {
                focusComponent(ftfFaxViewer.getJTextField());
                JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter the command line for the fax viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (ftfPSViewer.getText().length() == 0) {
                focusComponent(ftfPSViewer.getJTextField());
                JOptionPane.showMessageDialog(OptionsWin.this, _("Please enter the command line for the PostScript viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
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
            
            for (OptionsPage page : optionsPages) {
                if (!page.validateSettings(OptionsWin.this)) {
                    return false;
                }
            }
            
            return true;
        }
        

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
    
    private static class PanelTreeNode implements TreeNode {

        private List<PanelTreeNode> children;
        private TreeNode parent;
        private JComponent panel;
        private String label;
        private String longLabel;
        private Icon icon;
        
        public Enumeration<PanelTreeNode> children() {
            if (children == null)
                return null;
            
            return Collections.enumeration(children);
        }

        public boolean getAllowsChildren() {
            return !isLeaf();
        }

        public TreeNode getChildAt(int childIndex) {
            if (children == null)
                return null;
            
            return children.get(childIndex);
        }

        public int getChildCount() {
            if (children == null)
                return 0;
            
            return children.size();
        }

        public int getIndex(TreeNode node) {
            if (children == null)
                return -1;
            
            return children.indexOf(node);
        }

        public TreeNode getParent() {
            return parent;
        }

        public boolean isLeaf() {
            return (children == null);
        }

        public List<PanelTreeNode> getChildren() {
            return children;
        }

        public void setChildren(List<PanelTreeNode> children) {
            this.children = children;
        }

        public void setChildren(PanelTreeNode[] children) {
            this.children = Arrays.asList(children);
        }

        public JComponent getPanel() {
            return panel;
        }
        
        public Icon getIcon() {
            return icon;
        }

        public String getLongLabel() {
            return longLabel;
        }
        
        @Override
        public String toString() {
            return label;
        }
        
        PanelTreeNode(TreeNode parent, JComponent panel, String label, Icon icon) {
            this(parent,panel,label,icon,label);
        }
        
        PanelTreeNode(TreeNode parent, JComponent panel, String label, Icon icon, String longLabel) {
            super();
            this.label = label;
            this.panel = panel;
            this.parent = parent;
            this.icon = icon;
            this.longLabel = longLabel;
        }
    }
}
