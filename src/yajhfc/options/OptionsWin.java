package yajhfc.options;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */

import static yajhfc.Utils._;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.OverlayLayout;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;
import yajhfc.util.CancelAction;
import yajhfc.util.ExceptionDialog;

public class OptionsWin extends JDialog {
    //static final Logger log = Logger.getLogger(OptionsWin.class.getName());
    
    JPanel jContentPane = null;
    
    CommonPanel panelCommon;
        
    JPanel panelButtons;
    JButton buttonOK, buttonCancel;
    
    JPanel panelPaths;
    
    JTree mainTree;
    PanelTreeNode rootNode, serverSettingsNode;
    JLabel treeSelLabel;
    JPanel tabPanel;

    FaxOptions foToEdit = null;
    
    PanelTreeNode selectedNode = null;
    
    boolean modalResult;
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
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                foToEdit.optWinBounds = getBounds();

                if (panelCommon.changedLF && !modalResult) {
                    Utils.setLookAndFeel(foToEdit.lookAndFeel);
                }
            }
        });

        
        if (foToEdit.optWinBounds != null) {
            this.setBounds(foToEdit.optWinBounds);
        } else {
            //this.setLocationByPlatform(true);
            this.setSize(800,600);
//            this.pack();
//            Dimension size = this.getSize();
//            if (size.width > 900) {
//                size.width = 900;
//                setSize(size);
//            }
            Utils.setDefWinPos(this);
        }

        // Small special handling for new users
        String server0Host = foToEdit.servers.get(0).host;
        if (server0Host == null || server0Host.length() == 0) {
            //TabMain.setSelectedIndex(1);
            if (serverSettingsNode.isLeaf()) {
                mainTree.setSelectionPath(new TreePath(new Object[] { rootNode, serverSettingsNode }));
            } else {
                mainTree.setSelectionPath(new TreePath(new Object[] { rootNode, serverSettingsNode, serverSettingsNode.getChildAt(0) }));
            }
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
        if (panelButtons == null) {
            panelButtons = new JPanel(false);
            panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
            
            Dimension buttonSize = new Dimension(120, 30);
            
            panelButtons.add(Box.createHorizontalGlue());
            
            buttonOK = new JButton(_("OK"));
            buttonOK.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   if (saveSettings(foToEdit)) {
                       modalResult = true;
                       dispose();
                   }
                } 
            });
            buttonOK.setPreferredSize(buttonSize);
            panelButtons.add(buttonOK);
            
            panelButtons.add(Box.createRigidArea(new Dimension(border, border)));
            
            CancelAction actCancel = new CancelAction(this);
            buttonCancel = actCancel.createCancelButton();
            buttonCancel.setPreferredSize(buttonSize);
            panelButtons.add(buttonCancel);
            
            panelButtons.add(Box.createRigidArea(new Dimension(border, border)));
        }
        return panelButtons;
    }
    
    
    /**
     * Creates the tree node structure
     */
    private void createRootNode() {
        rootNode = new PanelTreeNode(null, null, "root", null) {
            @Override
            public DefaultTreeModel getTreeModel() {
                return (DefaultTreeModel)mainTree.getModel();
            }  
        };
        
        PanelTreeNode generalNode, pathsNode, coverNode, pluginsNode;
        
        rootNode.addChild(generalNode = new PanelTreeNode(rootNode, panelCommon = new CommonPanel(this), _("General"), Utils.loadIcon("general/Preferences")));
        
        PathAndViewPanel pathAndViewPanel;
        rootNode.addChild(pathsNode = new PanelTreeNode(rootNode, pathAndViewPanel = new PathAndViewPanel(), _("Paths and viewers"), Utils.loadIcon("development/Host")));
        pathsNode.addChild(new PanelTreeNode(pathsNode, new TextExtractorPanel(pathAndViewPanel), _("Recipient extraction"), Utils.loadCustomIcon("extract_recipient.gif"))); 
        
        ServerSettingsPanel serverPanel;
        rootNode.addChild(serverSettingsNode = new PanelTreeNode(rootNode, serverPanel = new ServerSettingsPanel(), _("Servers"), Utils.loadCustomIcon("servers.png")));

        CoverPanel coverPanel;
        rootNode.addChild(coverNode = new PanelTreeNode(rootNode, coverPanel = new CoverPanel() , _("Cover page & Identities"), Utils.loadCustomIcon("envelopes.png")));
        serverPanel.setIdentitiesModel(coverPanel.getListModel());
        
        rootNode.addChild(pluginsNode = new PanelTreeNode(rootNode, new PluginPanel(), _("Plugins & JDBC"), Utils.loadIcon("development/Jar")));
        
        PanelTreeNode tablesNode = new PanelTreeNode(rootNode, 
                new LabelOptionsPage(_("Please select on the left which table you want to edit.")),
                _("Tables"),null);
        MessageFormat tableFormat = new MessageFormat(_("Table \"{0}\""));
        tablesNode.addChild(new PanelTreeNode(tablesNode, new FmtEditorPanel<RecvFormat>(RecvFormat.values(), "recvfmt"), _("Received"), Utils.loadCustomIcon("received.gif"), tableFormat.format(new Object[] {_("Received")})));
        tablesNode.addChild(new PanelTreeNode(tablesNode, new FmtEditorPanel<JobFormat>(JobFormat.values(), "sentfmt"), _("Sent"), Utils.loadCustomIcon("sent.gif"), tableFormat.format(new Object[] {_("Sent")})));
        tablesNode.addChild(new PanelTreeNode(tablesNode, new FmtEditorPanel<JobFormat>(JobFormat.values(), "sendingfmt"), _("Transmitting"), Utils.loadCustomIcon("sending.gif"), tableFormat.format(new Object[] {_("Transmitting")})));
        tablesNode.addChild(new PanelTreeNode(tablesNode, new FmtEditorPanel<QueueFileFormat>(QueueFileFormat.values(), "archiveFmt"), _("Archive"), Utils.loadCustomIcon("archive.gif"), tableFormat.format(new Object[] {_("Archive")})));
        rootNode.addChild(tablesNode);

        PanelTreeNode advancedNode = new PanelTreeNode(rootNode, 
                new LabelOptionsPage(_("These settings normally need not to be changed.")),
                _("Advanced settings"), null);
        advancedNode.addChild(new PanelTreeNode(advancedNode, new AdminSettingsPage(this), _("Administrative settings"), Utils.loadCustomIcon("adminsettings.gif")));
        advancedNode.addChild(new PanelTreeNode(advancedNode, new ConvertersPage(), _("File converters"), Utils.loadCustomIcon("customfilters.png")));
        
        for (PluginUI puc : PluginManager.pluginUIs) {
            try {
                PanelTreeNode parent;
                switch (puc.getOptionsPanelParent()) {
                case PluginUI.OPTION_PANEL_ADVANCED:
                    parent = advancedNode;
                    break;
                case PluginUI.OPTION_PANEL_COVER:
                    parent = coverNode;
                    break;
                case PluginUI.OPTION_PANEL_GENERAL:
                    parent = generalNode;
                    break;
                case PluginUI.OPTION_PANEL_PATHS_VIEWERS:
                    parent = pathsNode;
                    break;
                case PluginUI.OPTION_PANEL_PLUGINS:
                    parent = pluginsNode;
                    break;
                case PluginUI.OPTION_PANEL_SERVER:
                    parent = serverSettingsNode;
                    break;
                case PluginUI.OPTION_PANEL_TABLES:
                    parent = tablesNode;
                    break;
                case PluginUI.OPTION_PANEL_ROOT:
                default:
                    parent = rootNode;
                    break;                    
                }

                PanelTreeNode node = puc.createOptionsPanel(parent);
                if (node != null) {
                    parent.addChild(node);
                }
            } catch (Exception e) {
                Logger.getLogger(OptionsWin.class.getName()).log(Level.SEVERE, "Error creating options panel " + puc, e);
            }
        }
        
        // Add the "advanced settings" node at the bottom of the list
        if (advancedNode.getChildCount() > 0) {
            rootNode.addChild(advancedNode);
        }
        
    }
    
    private JComponent getMainPanel() {
        //PROFILE: long time = System.currentTimeMillis();
        createRootNode();
        //PROFILE: System.out.println("   After createRootNode: " + (-time + (time = System.currentTimeMillis())));
        mainTree = new JTree(new DefaultTreeModel(rootNode));
        mainTree.setRootVisible(false);
        mainTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                PanelTreeNode selNode = (PanelTreeNode)e.getPath().getLastPathComponent();
                if (selNode != null && selNode != selectedNode) {
                    if (selectedNode != null) {
                        if (!selectedNode.getOptionsPage().pageIsHidden(OptionsWin.this)) {
                            mainTree.setSelectionPath(e.getOldLeadSelectionPath());
                            return;
                        }
                        selectedNode.getOptionsPage().getPanel().setVisible(false);
                    }
                    if (!selNode.settingsAndUILoaded) {
                        JComponent comp = selNode.getOptionsPage().getPanel();
                        tabPanel.add(comp);
                        comp.setVisible(true);
                        comp.setOpaque(true);
                        selNode.getOptionsPage().loadSettings(foToEdit);
                        selNode.settingsAndUILoaded = true;
                    } else {
                        selNode.getOptionsPage().getPanel().setVisible(true);
                    }
                    selNode.getOptionsPage().pageIsShown(OptionsWin.this);
                    
                    treeSelLabel.setText(selNode.getLongLabel());
                    selectedNode = selNode;
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
        mainTree.addMouseListener(new MouseAdapter() {
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath path = mainTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        PanelTreeNode ptn = (PanelTreeNode)path.getLastPathComponent();
                        if (ptn.getPopupMenu() != null) {
                            mainTree.setSelectionPath(path);
                            ptn.getPopupMenu().show((Component)e.getSource(), e.getX(), e.getY());
                        }
                    }
                }
            }
            
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
        });
        mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        mainTree.setRowHeight(20);
        mainTree.setAlignmentX(Component.LEFT_ALIGNMENT);
        //PROFILE: System.out.println("   After create main tree: " + (-time + (time = System.currentTimeMillis())));
        
        tabPanel = new JPanel(false);
        tabPanel.setLayout(new OverlayLayout(tabPanel));
        initializeTree(rootNode, null);
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
        mainPanel.setDividerLocation(mainTree.getPreferredSize().width + 8);
        //PROFILE: System.out.println("   After create main panel: " + (-time + (time = System.currentTimeMillis())));
        return mainPanel;
    }

    /**
     * Initializes the tree node and its children
     * @param tabPanel
     * @param node
     * @param pathToParent
     */
    private void initializeTree(PanelTreeNode node, TreePath pathToParent) {
        if (node.getOptionsPage() != null)
            node.getOptionsPage().initializeTreeNode(node, foToEdit);
        if (node.getChildren() != null) {
            TreePath pathToMe = (pathToParent == null) ? new TreePath(node) :
                pathToParent.pathByAddingChild(node);
            
            for (PanelTreeNode child : node.getChildren()) {
                initializeTree(child, pathToMe);
            }
            mainTree.expandPath(pathToMe);
        }
    }
    
    
    public OptionsWin(FaxOptions foEdit, Frame owner) {
        super(owner);
        this.foToEdit = foEdit;
        
        initialize();
    }
    
    public void reloadSettings(FaxOptions foEdit) {
        reloadSettings(rootNode, foEdit);
    }
    
    private void reloadSettings(PanelTreeNode node, FaxOptions foEdit) {
        if (node.settingsAndUILoaded) {
            node.getOptionsPage().loadSettings(foEdit);
        }
        if (node.getChildren() != null) {
            for (PanelTreeNode cn : node.getChildren()) {
                reloadSettings(cn, foEdit);
            }
        }
    }
    
    /**
     * Saves the settings. Returns true if they were saved successfully
     * @param foEdit
     * @return
     */
    public boolean saveSettings(FaxOptions foEdit) {
        if (selectedNode != null) {
            if (!selectedNode.getOptionsPage().pageIsHidden(this)) {
                return false;
            }
        }
        
        if (!validateInput(rootNode)) {
            return false;
        }
        
        try {
            saveSettings(foEdit, rootNode);            
        } catch (Exception e1) {
            ExceptionDialog.showExceptionDialog(OptionsWin.this, Utils._("Error saving the settings:"), e1);
            return false;
        }
        return true;
    }
    
    private void saveSettings(FaxOptions foEdit, PanelTreeNode node) {
        if (node.settingsAndUILoaded) {
            node.getOptionsPage().saveSettings(foEdit);
        }
        if (node.getChildren() != null) {
            for (PanelTreeNode cn : node.getChildren()) {
                saveSettings(foEdit, cn);
            }
        }
    }
    
    /**
     * Validate input. Return true if input is valid.
     * @return
     */
    private boolean validateInput(PanelTreeNode node) {
        if (node.settingsAndUILoaded) {
            if (!node.getOptionsPage().validateSettings(OptionsWin.this)) {
                return false;
            }
        }
        if (node.getChildren() != null) {
            for (PanelTreeNode cn : node.getChildren()) {
                if (!validateInput(cn)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private TreePath getPathToNode(TreeNode node) {
        if (node == rootNode) {
            return new TreePath(node);
        } else {
            return getPathToNode(node.getParent()).pathByAddingChild(node);
        }
    }
    
    public void selectNode(TreeNode node) {
        mainTree.setSelectionPath(getPathToNode(node));
    }
    
    public TreePath getSelectedPath() {
        return mainTree.getSelectionPath();
    }
    
    /**
     * Focuses the selected component and switches to the appropriate panel
     * @param comp
     */
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
        if (node.settingsAndUILoaded && node.getOptionsPage().getPanel() == panel) {
            mainTree.setSelectionPath(pathToMe);
        } else if (node.getChildren() != null) {            
            for (PanelTreeNode child : node.getChildren()) {
                selectTreeNodeForPanel(child, panel, pathToMe);
            }
        }
    }
    
    public PanelTreeNode getRootNode() {
        return rootNode;
    }
}
