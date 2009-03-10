/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.CompanyRule;
import yajhfc.phonebook.convrules.LocationRule;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.ZIPCodeRule;
import yajhfc.util.ClipboardPopup;

/**
 * @author jonas
 *
 */
public class CoverPanel extends JPanel implements OptionsPage {

    Map<PBEntryField,JTextComponent> entryFields = new EnumMap<PBEntryField, JTextComponent>(PBEntryField.class);
    
    FileTextField ftfCustomDefCover;
    JCheckBox checkUseCustomDefCover;
    JPanel panelCover;
    JComboBox comboNameRule, comboCompanyRule, comboLocationRule, comboZIPCodeRule;
    
    
    public CoverPanel() {
        super(new BorderLayout(), false);
        add(getBottomPanel(), BorderLayout.WEST);
        add(getPanelCover(), BorderLayout.CENTER);
    }
    
    private JPanel getBottomPanel() {
        JPanel bottomPanel = new JPanel(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(_("General")),
                BorderFactory.createEmptyBorder(OptionsWin.border, OptionsWin.border, OptionsWin.border, OptionsWin.border)));
        
        checkUseCustomDefCover = new JCheckBox(_("Use a custom default cover page:"));
        checkUseCustomDefCover.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ftfCustomDefCover.setEnabled(checkUseCustomDefCover.isSelected());
             } 
         });
        checkUseCustomDefCover.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        ftfCustomDefCover = new FileTextField();
        ftfCustomDefCover.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        ftfCustomDefCover.setFileFilters(Faxcover.getAcceptedFileFilters());
        ftfCustomDefCover.setEnabled(false);
        ftfCustomDefCover.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        comboNameRule = new JComboBox(NameRule.values());

        comboCompanyRule = new JComboBox(CompanyRule.values());
        
        comboLocationRule = new JComboBox(LocationRule.values());
        
        comboZIPCodeRule = new JComboBox(ZIPCodeRule.values());
        
        Dimension spacer = new Dimension(OptionsWin.border, OptionsWin.border);
        bottomPanel.add(Box.createRigidArea(spacer));
        addComboToBox(bottomPanel, comboNameRule, Utils._("Name format:"));
        bottomPanel.add(Box.createVerticalGlue());
        addComboToBox(bottomPanel, comboCompanyRule, Utils._("Company format:"));
        bottomPanel.add(Box.createVerticalGlue());
        addComboToBox(bottomPanel, comboLocationRule, Utils._("Location format:"));
        bottomPanel.add(Box.createVerticalGlue());
        addComboToBox(bottomPanel, comboZIPCodeRule, Utils._("ZIP code format:"));
        bottomPanel.add(Box.createVerticalGlue());
        bottomPanel.add(checkUseCustomDefCover);
        bottomPanel.add(ftfCustomDefCover);
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
    
    private JTextField createEntryTextField(PBEntryField field) {
        JTextField res = new JTextField();
        res.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        entryFields.put(field, res);
        return res;
    }
    
    private JPanel getPanelCover() {
        if (panelCover == null) {
            int longFields = 0;
            int shortFields = 0;
            final PBEntryField[] values = PBEntryField.values();
            for (PBEntryField field : values) {
                if (field.isShortLength()) {
                    shortFields++;
                } else {
                    longFields++;
                }
            }
            final int rowCount = 2 + 2 * (longFields - 1 + (shortFields+1)/2);
            double[][] dLay = {
                    {OptionsWin.border, 0.5, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                    new double[rowCount]
            };
            final double rowH = 1.0 / (double)(rowCount - 2);
            Arrays.fill(dLay[1], 1, rowCount - 2, rowH);
            dLay[1][0] = dLay[1][rowCount - 1] = OptionsWin.border;
            dLay[1][rowCount - 2] = TableLayout.FILL;
            
            panelCover = new JPanel(new TableLayout(dLay), false);
            panelCover.setBorder(BorderFactory.createTitledBorder(_("Sender data")));
            
            int row = 2;
            int col = 1;
            for (PBEntryField field : values) {
                if (field != PBEntryField.Comment) {
                    JTextField textField = createEntryTextField(field);
                    TableLayoutConstraints layout;
                    if (field.isShortLength()) {
                        layout = new TableLayoutConstraints(col, row, col, row, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER);
                        if (col == 1) {
                            col = 3;
                        } else {
                            row += 2;
                            col  = 1;
                        }
                    } else {
                        layout = new TableLayoutConstraints(1, row, 3, row, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER);
                        col  = 1;
                        row += 2;
                    }
                    Utils.addWithLabel(panelCover, textField, field.getDescription()+":", layout);
                }
            }
            
        }
        return panelCover;
    }

    public void loadSettings(FaxOptions foEdit) {
        for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
            entry.getValue().setText(foEdit.getCoverFrom().getField(entry.getKey()));
        }
        
        ftfCustomDefCover.setText(foEdit.defaultCover);
        checkUseCustomDefCover.setSelected(foEdit.useCustomDefaultCover);
        
        comboNameRule.setSelectedItem(foEdit.coverNameRule);
        comboCompanyRule.setSelectedItem(foEdit.coverCompanyRule);
        comboLocationRule.setSelectedItem(foEdit.coverLocationRule);
        comboZIPCodeRule.setSelectedItem(foEdit.coverZIPCodeRule);
    }

    public void saveSettings(FaxOptions foEdit) {
        for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
            foEdit.getCoverFrom().setField(entry.getKey(), entry.getValue().getText());
        }
        
        foEdit.defaultCover = ftfCustomDefCover.getText();
        foEdit.useCustomDefaultCover = checkUseCustomDefCover.isSelected();
        
        foEdit.coverNameRule = (NameRule)comboNameRule.getSelectedItem();
        foEdit.coverCompanyRule = (CompanyRule)comboCompanyRule.getSelectedItem();
        foEdit.coverLocationRule = (LocationRule)comboLocationRule.getSelectedItem();
        foEdit.coverZIPCodeRule = (ZIPCodeRule)comboZIPCodeRule.getSelectedItem();
    }

    public boolean validateSettings(OptionsWin optionsWin) {
        if (checkUseCustomDefCover.isSelected()) {
            if (!(new File(ftfCustomDefCover.getText()).canRead())) {
                optionsWin.focusComponent(ftfCustomDefCover.getJTextField());
                JOptionPane.showMessageDialog(optionsWin, _("The selected default cover page can not be read."), _("Error"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
}
