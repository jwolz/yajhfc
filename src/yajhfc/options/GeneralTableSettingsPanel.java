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
import static yajhfc.options.OptionsWin.border;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.Utils;
import yajhfc.cache.Cache;
import yajhfc.model.servconn.FaxListConnectionType;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;

/**
 * @author jonas
 *
 */
public class GeneralTableSettingsPanel extends AbstractOptionsPanel {

    FileTextField ftfSpoolLocation;
    JComboBox comboConnectionType;
    JCheckBox checkUseCache;
    Action actClearCache;
    
    JLabel labelSpoolLocation;
    JLabel labelCacheSize;
    
    /**
     * @param layout
     */
    public GeneralTableSettingsPanel() {
        super(false);
    }
    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
        double[][] dLay = {
                {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                {border, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        setLayout(new TableLayout(dLay));
        
        add(createPanelConnectionType(), "1,1");
        add(createPanelCache(), "3,1");
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
        
        double[][] dLay = {
                {border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border}
        };
        JPanel connTypePanel = new JPanel(new TableLayout(dLay), false);
        connTypePanel.setBorder(BorderFactory.createTitledBorder(_("Fax list connection")));
        
        Utils.addWithLabel(connTypePanel, comboConnectionType, _("Access method for the fax lists"), "1,2");
        labelSpoolLocation = Utils.addWithLabel(connTypePanel, ftfSpoolLocation, _("Location of spool area for direct access"), "1,5");
        return connTypePanel;
    }
    
    void updateCacheSize() {
        // Round the size to full KBs
        labelCacheSize.setText(MessageFormat.format(_("{0} KB"), ((Cache.getDefaultCacheLocation().length() + 512)/1024)));
    }
    
    private JPanel createPanelCache() {
        actClearCache = new ExcDialogAbstractAction(_("Clear cache")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                Cache.getDefaultCacheLocation().delete();
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

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        comboConnectionType.setSelectedItem(foEdit.faxListConnectionType);
        
        checkUseCache.setSelected(foEdit.useFaxListCache);
        
        ftfSpoolLocation.setText(foEdit.directAccessSpoolPath);
        
        updateCacheSize();
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.faxListConnectionType = (FaxListConnectionType)comboConnectionType.getSelectedItem();
        
        foEdit.useFaxListCache = checkUseCache.isSelected();
        
        foEdit.directAccessSpoolPath = ftfSpoolLocation.getText();
    }

        @Override
        public boolean validateSettings(OptionsWin optionsWin) {
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
                        optionsWin.focusComponent(ftfSpoolLocation.getJTextField());
                        return false;
                    }   
                }
            }
            return true;
        }
}
