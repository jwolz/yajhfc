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

import java.awt.event.ActionEvent;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import yajhfc.FaxOptions;
import yajhfc.IDAndNameOptions;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.cache.Cache;
import yajhfc.server.ServerOptions;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ListListModel;

/**
 * @author jonas
 *
 */
public class ServerSettingsPanel extends AbstractOptionsPanel<FaxOptions> {

    JCheckBox checkPCLBug;
    JSpinner spinSocketTimeout;
    JSpinner spinStatusInterval;
    JSpinner spinTableInterval;
    JPanel panelGlobal;
    private MultiEditPanel<ServerOptions> panelServers;
    
    JCheckBox checkUseCache;
    Action actClearCache;
    JLabel labelCacheSize;
    
    SingleServerSettingsPanel singleServerPanel = new SingleServerSettingsPanel(this);
    ModemsPanel modemPanel = new ModemsPanel();
    SendPanel sendPanel = new SendPanel();
    
    public ServerSettingsPanel() {
        super(false);
    }
    
    public ListListModel<ServerOptions> getServers() {
        return getPanelServers().itemsListModel;
    }
    
    void updateCacheSize() {
        long cacheSize = 0;
        for (ServerOptions srv : getServers()) {
            cacheSize += Cache.getCacheLocation(srv.id).length();
        }
        // Round the size to full KBs
        labelCacheSize.setText(MessageFormat.format(_("{0} KB"), ((cacheSize + 512)/1024)));
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
        double[][] tablelay = {
                {border, TableLayout.PREFERRED, border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        this.setLayout(new TableLayout(tablelay));
        
        this.add(getPanelGlobal(), "1,1,1,1,f,f");
        this.add(createPanelCache(), "1,3,1,3,f,f");
        this.add(getPanelServers(), "3,1,3,4,f,f");
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        checkPCLBug.setSelected(foEdit.pclBug);
 
        spinTableInterval.setValue(foEdit.tableUpdateInterval / 1000.0);
        spinStatusInterval.setValue(foEdit.statusUpdateInterval / 1000.0);
        spinSocketTimeout.setValue(foEdit.socketTimeout / 1000.0);
        
        checkUseCache.setSelected(foEdit.useFaxListCache);
        updateCacheSize();
        
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.tableUpdateInterval = (int)(((Double)spinTableInterval.getValue()).doubleValue() * 1000);
        foEdit.statusUpdateInterval = (int)(((Double)spinStatusInterval.getValue()).doubleValue() * 1000);
        foEdit.socketTimeout = (int)(((Double)spinSocketTimeout.getValue()).doubleValue() * 1000);
        
        foEdit.pclBug = checkPCLBug.isSelected();
        
        foEdit.useFaxListCache = checkUseCache.isSelected();
    }
    
    @Override
    public void initializeTreeNode(PanelTreeNode node, FaxOptions foEdit) {
        getPanelServers().setSettingsNode(node);
        
        ListListModel<ServerOptions> servers = getServers();
        servers.clear();
        for (ServerOptions opt : foEdit.servers) {
            servers.add(new ServerOptions(opt));
        }
        if (servers.getSize() == 0){
            ServerOptions so = new ServerOptions(foEdit);
            so.name = _("Default");
            servers.add(so);
        }
    }
    
    public void setIdentitiesModel(
            ListListModel<SenderIdentity> identitiesModel) {
        sendPanel.setIdentitiesModel(identitiesModel);
    }
    
    private MultiEditPanel<ServerOptions> getPanelServers() {
        if (panelServers == null) {
            panelServers = new MultiEditPanel<ServerOptions>() {
                
                MessageFormat serverFormat = new MessageFormat(_("Server {0}"));
                MessageFormat modemFormat = new MessageFormat(_("Modems for server {0}"));
                MessageFormat sendFormat = new MessageFormat(_("Delivery settings for server {0}"));
                
                @Override
                protected PanelTreeNode createChildNode(ServerOptions forItem) {
                    String label = forItem.toString();
                    PanelTreeNode newChild = new PanelTreeNode(settingsNode, 
                            new OptionsPageWrapper<ServerOptions>(singleServerPanel, forItem, this),
                            label, Utils.loadIcon("development/Server"), serverFormat.format(new Object[] { label }));
                    newChild.setChildren(new PanelTreeNode[] {
                            new PanelTreeNode(newChild, new OptionsPageWrapper<ServerOptions>(sendPanel, forItem, this), 
                                    _("Delivery"), Utils.loadIcon("general/SendMail"), sendFormat.format(new Object[] { label })),
                            new PanelTreeNode(newChild, new OptionsPageWrapper<ServerOptions>(modemPanel, forItem, this),
                                    _("Modems"), Utils.loadCustomIcon("modem.png"), modemFormat.format(new Object[] { label }))
                    });
                    return newChild;
                }

                @Override
                protected void updateChildNode(PanelTreeNode node,
                        ServerOptions forItem) {
                    String label = forItem.toString();
                    node.setLabel(label);
                    
                    Object args = new Object[] { label };
                    FieldPosition pos = new FieldPosition(0);
                    StringBuffer buf = new StringBuffer();
                    node.setLongLabel(serverFormat.format(args, buf, pos).toString());
                    buf.setLength(0);
                    node.getChildren().get(0).setLongLabel(sendFormat.format(args, buf, pos).toString());
                    buf.setLength(0);
                    node.getChildren().get(1).setLongLabel(modemFormat.format(args, buf, pos).toString());
                }
                
                @Override
                protected ServerOptions createNewItem() {
                    return new ServerOptions(Utils.getFaxOptions());
                }

                @Override
                protected ServerOptions duplicateItem(ServerOptions toDuplicate) {
                    ServerOptions newSrv = new ServerOptions(toDuplicate);
                    newSrv.generateNewID();
                    newSrv.name = MessageFormat.format(_("Copy of {0}"), newSrv.name);
                    return newSrv;
                }

                @Override
                protected String getDeletePrompt(ServerOptions selectedItem) {
                    return MessageFormat.format(_("Do you really want to remove the server \"{0}\"?"), selectedItem);
                }
                
                private boolean listSaved = false;
                public void saveSettingsCalled(OptionsPageWrapper<ServerOptions> source,
                        FaxOptions foEdit) {
                    if (listSaved)
                        return;
                    
                    ListListModel<ServerOptions> servers = itemsListModel;
                    foEdit.servers.clear();
                    foEdit.servers.addAll(servers.getList());
                    listSaved = true;
                }
                
                public boolean validateSettingsCalled(OptionsPageWrapper<ServerOptions> source, OptionsWin optionsWin) {
                    List<ServerOptions> servers = itemsListModel.getList();
                    if (servers.size() == 0) {
                        // Should never happen...
                        JOptionPane.showMessageDialog(optionsWin, "Need at least one server!");
                        return false;
                    }
                    if (IDAndNameOptions.checkForDuplicates(servers)) {
                        // Should never happen either...
                        JOptionPane.showMessageDialog(optionsWin, "Duplicate IDs found, please cancel this dialog (should never happen)!");
                        return false;
                    }
                    return true;
                }
            };
            panelServers.setBorder(BorderFactory.createTitledBorder(_("Servers")));
        }
        return panelServers;
    }
    
    private JPanel createPanelCache() {
        actClearCache = new ExcDialogAbstractAction(_("Clear cache")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                for (ServerOptions srv : getServers()) {
                    Cache.getCacheLocation(srv.id).delete();
                }
                Cache.useForNextLogin = false;
                updateCacheSize();
            }
        };
        
        checkUseCache = new JCheckBox(_("Locally cache fax lists"));
        labelCacheSize = new JLabel("XXX");
        
        double[][] dLay = {
                {border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        JPanel cachePanel = new JPanel(new TableLayout(dLay), false);
        cachePanel.setBorder(BorderFactory.createTitledBorder(_("Cache settings")));
        cachePanel.add(checkUseCache, "1,1");
        cachePanel.add(new JLabel(_("Current Cache size:")), "1,3");
        cachePanel.add(labelCacheSize, "1,4");
        cachePanel.add(new JButton(actClearCache), "1,6");
        
        return cachePanel;
    }
    
    private JPanel getPanelGlobal() {
        if (panelGlobal == null) {
            final int rowCount = 22;
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            
            tablelay[1][rowCount-1] = TableLayout.FILL;
            for (int i=0; i<rowCount-1; i++) {
                if (i%3 == 0) {
                    tablelay[1][i] = border;
                } else {
                    tablelay[1][i] = TableLayout.PREFERRED;
                }
            }
            
            panelGlobal = new JPanel(new TableLayout(tablelay), false);
            panelGlobal.setBorder(BorderFactory.createTitledBorder(_("Global settings")));
            
            checkPCLBug = new JCheckBox("<html>" + _("Use PCL file type bugfix") + "</html>");

            
            spinStatusInterval = new JSpinner(new SpinnerNumberModel(1, 0.5, 86400, 1));
            spinTableInterval = new JSpinner(new SpinnerNumberModel(3, 0.5, 86400, 1));
            spinSocketTimeout = new JSpinner(new SpinnerNumberModel((double)90, 0, 86400, 1));
            spinSocketTimeout.setToolTipText(_("The maximum time to wait for a interaction with the server to complete. Values below 5 are not recommended; 0 disables this timeout."));

            
            panelGlobal.add(checkPCLBug, "1, 2");
            addWithLabel(panelGlobal, spinTableInterval, "<html>" + _("Table refresh interval (secs.):") + "</html>", "1, 5, 1, 5 f, c");
            addWithLabel(panelGlobal, spinStatusInterval, "<html>" + _("Server status refresh interval (secs.):") + "</html>", "1, 8, 1, 8, f, c");
            addWithLabel(panelGlobal, spinSocketTimeout, "<html>" + _("Server socket timeout (secs):") + "</html>", "1, 11, 1, 11, f, c");
        }
        return panelGlobal;
    }
}
