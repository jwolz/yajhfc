/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import yajhfc.FileTextField;
import yajhfc.IDAndNameOptions;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.PBEntryField;
import yajhfc.util.ClipboardPopup;

/**
 * @author jonas
 *
 */
public class IdentityPanel extends AbstractOptionsPanel<SenderIdentity> {
    Map<PBEntryField,JTextComponent> entryFields = new EnumMap<PBEntryField, JTextComponent>(PBEntryField.class);
    
    JTextField textName;
    JTextField textID;
    
    FileTextField ftfCustomDefCover;
    JCheckBox checkUseCustomDefCover;
    
    final CoverPanel parent;
    SenderIdentity idEdit;
    
    public IdentityPanel(CoverPanel parent) {
        super(new BorderLayout());
        this.parent = parent;
    }
    
    private JTextField createEntryTextField(PBEntryField field) {
        JTextField res = new JTextField();
        res.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        entryFields.put(field, res);
        return res;
    }

    
    private JPanel createTopPanel() {
        double[][] dLay = {
                {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED}
        };
        JPanel topPanel = new JPanel(new TableLayout(dLay));
        
        textName = new JTextField();
        
        textID = new JTextField("#####");
        textID.setEditable(false);

        addWithLabel(topPanel, textName, _("Name for this identity:"), "1,2");
        addWithLabel(topPanel, textID, _("ID:"), "3,2");
        
        topPanel.add(new JSeparator(), "0,4,4,4");
        return topPanel;
    }
    
    @Override
    protected void createOptionsUI() { 
        add(createTopPanel(), BorderLayout.NORTH);
        add(createDataPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createDataPanel() {
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
        final int rowCount = 5 + 3 * (longFields - 1 + (shortFields+1)/2);
        double[][] dLay = {
                {OptionsWin.border, 0.5, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                new double[rowCount]
        };

        for (int i=0; i<rowCount-1; i++) {
            if (i%3 == 0) {
                dLay[1][i] = border;
            } else {
                dLay[1][i] = TableLayout.PREFERRED;
            }
        }
        dLay[1][rowCount - 1] = TableLayout.FILL;
        
        JPanel dataPanel = new JPanel(new TableLayout(dLay), false);
        //setBorder(BorderFactory.createTitledBorder(_("Sender data")));
        
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
                        row += 3;
                        col  = 1;
                    }
                } else {
                    layout = new TableLayoutConstraints(1, row, 3, row, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER);
                    col  = 1;
                    row += 3;
                }
                Utils.addWithLabel(dataPanel, textField, field.getDescription()+":", layout);
            }
        }
        
        checkUseCustomDefCover = new JCheckBox(_("Use a custom default cover page:"));
        checkUseCustomDefCover.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ftfCustomDefCover.setEnabled(checkUseCustomDefCover.isSelected());
             } 
         });
        
        ftfCustomDefCover = new FileTextField();
        ftfCustomDefCover.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        ftfCustomDefCover.setFileFilters(Faxcover.getAcceptedFileFilters());
        ftfCustomDefCover.setEnabled(false);
        
        dataPanel.add(checkUseCustomDefCover, new TableLayoutConstraints(1, row-1, 3, row-1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
        dataPanel.add(ftfCustomDefCover, new TableLayoutConstraints(1, row, 3, row, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
        return dataPanel;
    }

    public void loadSettings(SenderIdentity foEdit) {
        this.idEdit = foEdit;
        textName.setText(foEdit.name);
        textID.setText(String.valueOf(foEdit.id));
        ftfCustomDefCover.setText(foEdit.defaultCover);
        checkUseCustomDefCover.setSelected(foEdit.useCustomDefaultCover);
        
        for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
            entry.getValue().setText(foEdit.getField(entry.getKey()));
        }
    }

    public void saveSettings(SenderIdentity foEdit) {
        foEdit.name = textName.getText();
        foEdit.defaultCover = ftfCustomDefCover.getText();
        foEdit.useCustomDefaultCover = checkUseCustomDefCover.isSelected();
        
        for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
            foEdit.setField(entry.getKey(), entry.getValue().getText());
        }
    }
    
    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        SenderIdentity si = IDAndNameOptions.getItemByName(parent.getListModel().getList(), textName.getText());
        if (si != null && si.id != idEdit.id) {
            textName.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("There already exists another identity with this name!"), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
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
