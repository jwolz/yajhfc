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
package yajhfc.phonebook.ui;

import static yajhfc.Utils._;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.SortOrder;
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.MultiButtonGroup;
import yajhfc.util.fmtEditor;

/**
 * @author jonas
 *
 */
public class SortOrderDialog extends JDialog {
    private static final String DESCENDING_ACTION_COMMAND = "descending";

    private static final String ASCENDING_ACTION_COMMAND = "ascending";

    private static final int border = 8;
    
    fmtEditor<PBEntryField> fmtEditor;
    Action actOK;
    MultiButtonGroup directionGroup;
    boolean[] descending = new boolean[PBEntryField.FIELD_COUNT];
    List<PBEntryField> selectedList;
    JPanel sortDirectionPanel;
    
    public SortOrder result;

    
    public SortOrderDialog(Dialog owner, SortOrder oldOrder) {
        super(owner, _("Custom sort order"), true);
        initialize(oldOrder);
    }
    
    public SortOrderDialog(Frame owner, SortOrder oldOrder) {
        super(owner, _("Custom sort order"), true);
        initialize(oldOrder);
    }
    
    private void initialize(SortOrder oldOrder) {
        actOK = new ExcDialogAbstractAction(_("OK")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (selectedList.size() > 0) {
                    PBEntryField[] selection = selectedList.toArray(new PBEntryField[selectedList.size()]);
                    boolean[] descStat = new boolean[selection.length];
                    
                    for (int i=0; i<selection.length; i++) {
                        descStat[i] = isDescending(selection[i]);
                    }
                    result = new SortOrder(selection, descStat);
                    
                    dispose();
                }
            }
        };
        
        selectedList = new ArrayList<PBEntryField>();
        if (oldOrder != null) {
            for (int i=0; i<oldOrder.getFieldCount(); i++) {
                PBEntryField field = oldOrder.getFields(i);
                selectedList.add(field);
                setDescending(field, oldOrder.getDescending(i));
            }
            actOK.setEnabled(true);
        } else {
            actOK.setEnabled(false);
        }
        
        fmtEditor = new fmtEditor<PBEntryField>(PBEntryField.values(), selectedList, Collections.<PBEntryField>emptyList(),
                new DefaultListCellRenderer(), null, null,  _("Selected sort order:"), _("Avaiable fields:"));
        fmtEditor.getSelectedList().setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                PBEntryField field = (PBEntryField)value;
                value = field.getDescription() + " (" + (isDescending(field) ? _("descending") : _("ascending")) + ")";
        
                return super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
            }
        });
        fmtEditor.getSelectedList().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadSortDirectionPanel(); 
                }
            }
        });
        fmtEditor.getSelectedListModel().addListDataListener(new ListDataListener() {
            public void intervalRemoved(ListDataEvent e) {
                checkOKEnable();
            }
            
            public void intervalAdded(ListDataEvent e) {
                checkOKEnable();
            }
            
            public void contentsChanged(ListDataEvent e) { }
            
            private void checkOKEnable() {
                actOK.setEnabled(selectedList.size() > 0);
            }
        });
        
        directionGroup = new MultiButtonGroup() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int[] idxs = fmtEditor.getSelectedList().getSelectedIndices();
                if (idxs.length == 0)
                    return;

                final boolean desc = DESCENDING_ACTION_COMMAND.equals(e.getActionCommand());
                for (int idx : idxs) {
                    setDescending(idx, desc);
                    fmtEditor.getSelectedListModel().changeNotify(idx);
                }
            }
        };
        directionGroup.addItem(_("ascending"), ASCENDING_ACTION_COMMAND);
        directionGroup.addItem(_("descending"), DESCENDING_ACTION_COMMAND);
        
        CancelAction cancelAction = new CancelAction(this);
        
        sortDirectionPanel = new JPanel();
        sortDirectionPanel.setLayout(new BoxLayout(sortDirectionPanel, BoxLayout.Y_AXIS));
        sortDirectionPanel.setBorder(BorderFactory.createTitledBorder(_("Sort direction")));
        
        Dimension spacer = new Dimension(border, border);
        sortDirectionPanel.add(Box.createRigidArea(spacer));
        for (JRadioButton btn : directionGroup.createButtons()) {
            sortDirectionPanel.add(btn);
            sortDirectionPanel.add(Box.createRigidArea(spacer));
        }
        loadSortDirectionPanel(); 
        
        Box rightPanel = Box.createVerticalBox();
        rightPanel.add(Box.createRigidArea(spacer));
        rightPanel.add(sortDirectionPanel);
        rightPanel.add(Box.createVerticalGlue());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, border, border));
        buttonPanel.add(new JButton(actOK));
        buttonPanel.add(new JButton(cancelAction));
        
        JPanel contentPane = new JPanel(new BorderLayout(border, border));
        contentPane.add(fmtEditor, BorderLayout.CENTER);
        contentPane.add(rightPanel, BorderLayout.EAST);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        setSize(800, 600);
        Utils.setDefWinPos(this);
    }
    
    void setDescending(int index, boolean value) {
        setDescending(selectedList.get(index), value);
    }
    
    void setDescending(PBEntryField field, boolean value) {
        descending[field.ordinal()] = value;
    }
    
    boolean isDescending(int index) {
        return isDescending(selectedList.get(index));
    }
    
    boolean isDescending(PBEntryField field) {
        return descending[field.ordinal()];
    }
    
    void loadSortDirectionPanel() {
        int idx = fmtEditor.getSelectedList().getSelectedIndex();
                
        Utils.enableChildren(sortDirectionPanel, idx>=0);
        if (idx >= 0) {
            directionGroup.setSelectedActionCommand(isDescending(idx) ? DESCENDING_ACTION_COMMAND : ASCENDING_ACTION_COMMAND);
        }
    }

    public static SortOrder showForSortOrder(Window owner, SortOrder sortOrder) {
        SortOrderDialog sod;
        if (owner instanceof Dialog) {
            sod = new SortOrderDialog((Dialog)owner, sortOrder);
        } else if (owner instanceof Frame) {
            sod = new SortOrderDialog((Frame)owner, sortOrder);
        } else {
            throw new RuntimeException("owner must be a Dialog or Frame");
        }
        sod.setVisible(true);
        return sod.result;
    }
}
