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
package yajhfc.options;

import static yajhfc.Utils._;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import yajhfc.FaxOptions;
import yajhfc.IDAndNameOptions;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.phonebook.convrules.CompanyRule;
import yajhfc.phonebook.convrules.LocationRule;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.ZIPCodeRule;
import yajhfc.util.ListComboModel;
import yajhfc.util.ListListModel;

/**
 * @author jonas
 *
 */
public class CoverPanel extends AbstractOptionsPanel<FaxOptions> {
    
    JPanel panelCover;
    JComboBox comboNameRule, comboCompanyRule, comboLocationRule, comboZIPCodeRule;
    
    MultiEditPanel<SenderIdentity> coverPanel;
    ListListModel<SenderIdentity> listModel = new ListComboModel<SenderIdentity>(new ArrayList<SenderIdentity>());;
    
    public CoverPanel() {
        super(new BorderLayout(), false);
    }
    
    public ListListModel<SenderIdentity> getListModel() {
        return listModel;
    }
    
    @Override
    protected void createOptionsUI() {
        add(getBottomPanel(), BorderLayout.WEST);
        add(getPanelCover(), BorderLayout.CENTER);
    }
    
    private MultiEditPanel<SenderIdentity> getPanelCover() {
        if (coverPanel == null) {
            coverPanel = new MultiEditPanel<SenderIdentity>(listModel) {
                IdentityPanel idPanel = new IdentityPanel(CoverPanel.this);

                @Override
                protected String getDeletePrompt(SenderIdentity selectedItem) {
                    return MessageFormat.format(_("Do you really want to remove the identity \"{0}\"?"), selectedItem);
                }
                
                @Override
                protected String getDeletePrompt(int[] selectedItems) {
                    return MessageFormat.format(_("Do you really want to remove the selected {0} identities?"), selectedItems.length);
                }
                
                @Override
                protected SenderIdentity duplicateItem(SenderIdentity toDuplicate) {
                    SenderIdentity newID = new SenderIdentity(toDuplicate);
                    newID.generateNewID();
                    newID.name = MessageFormat.format(_("Copy of {0}"), newID.name);
                    return newID;
                }
                
                @Override
                protected SenderIdentity createNewItem() {
                    return new SenderIdentity(Utils.getFaxOptions());
                }
                
                MessageFormat identityFormat = new MessageFormat(_("Identity {0}"));
                
                @Override
                protected PanelTreeNode createChildNode(SenderIdentity forItem) {
                    String label = forItem.toString();
                    PanelTreeNode newChild = new PanelTreeNode(settingsNode, 
                            new OptionsPageWrapper<SenderIdentity>(idPanel, forItem, this),
                            label, Utils.loadIcon("general/ComposeMail"), identityFormat.format(new Object[] {label}));
                    return newChild;
                }
                
                @Override
                protected void updateChildNode(PanelTreeNode node,
                        SenderIdentity forItem) {
                    String label = forItem.toString();
                    node.setLabel(label);
                    node.setLongLabel(identityFormat.format(new Object[] {label}));
                }
                
                private boolean listSaved = false;
                public void saveSettingsCalled(OptionsPageWrapper<SenderIdentity> source,
                        FaxOptions foEdit) {
                    if (listSaved)
                        return;
                    
                    ListListModel<SenderIdentity> senders = itemsListModel;
                    foEdit.identities.clear();
                    foEdit.identities.addAll(senders.getList());
                    listSaved = true;
                }
                
                public boolean validateSettingsCalled(
                        OptionsPageWrapper<SenderIdentity> source, OptionsWin optionsWin) {
                    List<SenderIdentity> identites = itemsListModel.getList();
                    if (identites.size() == 0) {
                        // Should never happen...
                        JOptionPane.showMessageDialog(optionsWin, "Need at least one identity!");
                        return false;
                    }
                    if (IDAndNameOptions.checkForDuplicates(identites)) {
                        // Should never happen either...
                        JOptionPane.showMessageDialog(optionsWin, "Duplicate IDs found, please cancel this dialog (should never happen)!");
                        return false;
                    }
                    return true;
                }
            };
            coverPanel.setBorder(BorderFactory.createTitledBorder(_("Identities")));
        }
        return coverPanel;
    }
    
    private JPanel getBottomPanel() {
        JPanel bottomPanel = new JPanel(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(_("General")),
                BorderFactory.createEmptyBorder(OptionsWin.border, OptionsWin.border, OptionsWin.border, OptionsWin.border)));
        
        comboNameRule = new JComboBox(NameRule.values());

        comboCompanyRule = new JComboBox(CompanyRule.values());
        
        comboLocationRule = new JComboBox(LocationRule.values());
        
        comboZIPCodeRule = new JComboBox(ZIPCodeRule.values());
        
        Dimension spacer = new Dimension(OptionsWin.border, OptionsWin.border);
        bottomPanel.add(Box.createRigidArea(spacer));
        addComboToBox(bottomPanel, comboNameRule, Utils._("Name format:"));
        bottomPanel.add(Box.createRigidArea(spacer));
        addComboToBox(bottomPanel, comboCompanyRule, Utils._("Company format:"));
        bottomPanel.add(Box.createRigidArea(spacer));
        addComboToBox(bottomPanel, comboLocationRule, Utils._("Location format:"));
        bottomPanel.add(Box.createRigidArea(spacer));
        addComboToBox(bottomPanel, comboZIPCodeRule, Utils._("ZIP code format:"));
        bottomPanel.add(Box.createVerticalGlue());
        bottomPanel.add(Box.createRigidArea(spacer));
        return bottomPanel;
    }
    
    private void addComboToBox(JPanel box, JComboBox combo, String label) {
        Dimension prefSize = combo.getPreferredSize();
        prefSize.width = Integer.MAX_VALUE;
        combo.setMaximumSize(prefSize);
        combo.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        JLabel jLabel = new JLabel(label);
        jLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        jLabel.setLabelFor(combo);
        
        box.add(jLabel);
        box.add(combo);
    }
    


    public void loadSettings(FaxOptions foEdit) {
        
        comboNameRule.setSelectedItem(foEdit.coverNameRule);
        comboCompanyRule.setSelectedItem(foEdit.coverCompanyRule);
        comboLocationRule.setSelectedItem(foEdit.coverLocationRule);
        comboZIPCodeRule.setSelectedItem(foEdit.coverZIPCodeRule);
    }

    @Override
    public void initializeTreeNode(PanelTreeNode node, FaxOptions foEdit) {
        getPanelCover().setSettingsNode(node);
        
        ListListModel<SenderIdentity> senders = getPanelCover().itemsListModel;
        senders.clear();
        for (SenderIdentity opt : foEdit.identities) {
            senders.add(new SenderIdentity(opt));
        }
        if (senders.getSize() == 0){
            SenderIdentity so = new SenderIdentity(foEdit);
            so.name = _("Default");
            senders.add(so);
        }
    }
    
    public void saveSettings(FaxOptions foEdit) {        
        foEdit.coverNameRule = (NameRule)comboNameRule.getSelectedItem();
        foEdit.coverCompanyRule = (CompanyRule)comboCompanyRule.getSelectedItem();
        foEdit.coverLocationRule = (LocationRule)comboLocationRule.getSelectedItem();
        foEdit.coverZIPCodeRule = (ZIPCodeRule)comboZIPCodeRule.getSelectedItem();
        
        coverPanel.saveSettingsCalled(null, foEdit);
    }

}
