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
package yajhfc.util;


import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.Utils;
import yajhfc.util.KeyStrokeTextField.KeyStrokeTextFieldListener;

/**
 * @author jonas
 *
 */
public class AcceleratorKeyDialog extends JDialog {
    private static final int border = 6;
    
    ActionToKeyStrokeTableModel tableModel;
    JTable table;
    KeyStrokeTextField textAccelerator;
    Action actClearAccelerator, actOK, actReset, actUseDefault;
    
    Map<String,String> defaults;
    
    /**
     * true if the user clicked "OK"
     */
    public boolean modalResult = false;
    
    public AcceleratorKeyDialog(Dialog owner, Collection<Action> actions, Map<String,String> defaults) {
        super(owner, _("Edit keyboard shortcuts"), true);
        this.defaults = defaults;
        initialize(actions);
    }
    
    public AcceleratorKeyDialog(Frame owner, Collection<Action> actions, Map<String,String> defaults) {
        super(owner, _("Edit keyboard shortcuts"), true);
        this.defaults = defaults;
        initialize(actions);
    }
    
    KeyStroke getDefaultForAction(Action act) {
        return KeyStroke.getKeyStroke(defaults.get(act.getValue(Action.ACTION_COMMAND_KEY)));
    }
    
    private void initialize(Collection<Action> actions) {
        actClearAccelerator = new ExcDialogAbstractAction(_("Clear")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    tableModel.setKeystroke(row, null);
                    textAccelerator.setKeyStroke(null);
                }
            }
        };

        actUseDefault = new ExcDialogAbstractAction(_("Use default")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    Action act = tableModel.getAction(row);
                    KeyStroke ks = getDefaultForAction(act);
                    tableModel.setKeystroke(row, ks);
                    textAccelerator.setKeyStroke(ks);
                }
            }
        };

        actReset = new ExcDialogAbstractAction(_("Reset")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                for (int i=0; i<tableModel.getRowCount(); i++) {
                    Action act = tableModel.getAction(i);
                    KeyStroke ks = getDefaultForAction(act);
                    tableModel.setKeystroke(i, ks);
                }
                table.getSelectionModel().clearSelection();
            }
        };
        
        actOK = new ExcDialogAbstractAction(_("OK")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                Object[] dup = tableModel.searchDuplicates();
                if (dup != null) {
                    JOptionPane.showMessageDialog(AcceleratorKeyDialog.this, MessageFormat.format(_("Duplicate shortcut {0} found for actions \"{1}\" and \"{2}\"!"), dup), _("Error"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                tableModel.commitChanges();
                modalResult = true;
                dispose();
            }
        };
    	
        JPanel contentPane = new JPanel(new BorderLayout());
        tableModel = new ActionToKeyStrokeTableModel(actions);
        table = new JTable(tableModel);
        table.setDefaultRenderer(Action.class, new DefaultTableCellRenderer() {
        	@Override
        	public Component getTableCellRendererComponent(JTable table,
        			Object value, boolean isSelected, boolean hasFocus,
        			int row, int column) {
        		
                Action data = (Action)value;
                String text;
                Icon icon;
                String tooltip;
                if (data == null) {
                    text = "";
                    icon = null;
                    tooltip = null;
                } else {
                    text = (String)data.getValue(Action.NAME);
                    icon = (Icon)data.getValue(Action.SMALL_ICON);
                    tooltip = (String)data.getValue(Action.SHORT_DESCRIPTION);
                }
                
                JLabel renderer = (JLabel)super.getTableCellRendererComponent(table, text, isSelected, hasFocus,
        				row, column);
                renderer.setIcon(icon);
                renderer.setToolTipText(tooltip);
                return renderer;
        	}
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateSelection();
				}
			}
		});
        
        textAccelerator = new KeyStrokeTextField();
        textAccelerator.addKeyStrokeTextFieldListener(new KeyStrokeTextFieldListener() {
            public void userTypedShortcut(KeyStroke newShortcut) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    tableModel.setKeystroke(row, newShortcut);
                }
            }
        });
        
        CancelAction cancelAct = new CancelAction(this);
        
        double[][] dLay = {
                {border, textAccelerator.getPreferredSize().width, border},
                {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL}
        };
        JPanel editPanel = new JPanel(new TableLayout(dLay));
        Utils.addWithLabel(editPanel, textAccelerator, _("Accelerator key") + ':', "1,2");
        JLabel lblUsage = new JLabel("<html>" + _("Click on the text field above and press the key (combination) to be used as the selected action's keyboard shortcut.") + "</html>");
        editPanel.add(lblUsage, "1,4");
        editPanel.add(new JButton(actClearAccelerator), "1,6");
        editPanel.add(new JButton(actUseDefault), "1,8");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(new JButton(actOK));
        buttonPanel.add(new JButton(actReset));
        buttonPanel.add(cancelAct.createCancelButton());
        
        contentPane.add(new JScrollPane(table), BorderLayout.CENTER);
        contentPane.add(editPanel, BorderLayout.EAST);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        pack();
        Utils.setDefWinPos(this);
        
        updateSelection();
    }
    
    void updateSelection() {
        int row = table.getSelectedRow();
        
        boolean enableEditing = (row >= 0);
        actUseDefault.setEnabled(enableEditing);
        actClearAccelerator.setEnabled(enableEditing);
        textAccelerator.setEnabled(enableEditing);
        
        if (enableEditing) {
        	textAccelerator.setKeyStroke(tableModel.getKeystroke(row));
        } else {
        	textAccelerator.setKeyStroke(null);
        }
    }

    static class ActionToKeyStrokeTableModel extends AbstractTableModel {
        private final static String[] cols = {
            _("Action"),
            _("Accelerator key")
        };
        
        protected Action[] actions;
        protected KeyStroke[] keyStrokes;
        
        public int getRowCount() {
            return actions.length;
        }

        public int getColumnCount() {
            return cols.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return cols[column];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return Action.class;
            case 1:
                return String.class;
            default:
                return null;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return actions[rowIndex];
            case 1:
                return KeyStrokeTextField.keyStrokeToUserString(keyStrokes[rowIndex]);
            default:
                return null;
            }
        }
        
        public void setKeystroke(int rowIndex, KeyStroke key) {
            keyStrokes[rowIndex] = key;
            fireTableCellUpdated(rowIndex, 1);
        }
        
        public KeyStroke getKeystroke(int rowIndex) {
            return keyStrokes[rowIndex];
        }
        
        public Action getAction(int rowIndex) {
            return actions[rowIndex];
        }
        
        /**
         * Searches for duplicates and returns the first duplicate found in the form { KeyStroke, firstAction, secondAction }.
         * If no duplicates are found, returns null
         * @return
         */
        public Object[] searchDuplicates() {
            for (int i=0; i<keyStrokes.length-1; i++) {
                KeyStroke ks = keyStrokes[i];
                if (ks != null) {
                    for (int j=i+1; j<keyStrokes.length; j++) {
                        KeyStroke ks2 = keyStrokes[j];
                        if (ks2 != null && ks.equals(ks2)) {
                            return new Object[] { ks, actions[i], actions[j] };
                        }
                    }
                }
            }
            return null;
        }
        
        public void commitChanges() {
            for (int i=0; i<actions.length; i++) {
                Action act = actions[i];
                KeyStroke ks = keyStrokes[i];
                act.putValue(Action.ACCELERATOR_KEY, ks);
            }
        }
        
        public ActionToKeyStrokeTableModel(Collection<Action> actionColl) {
            actions = actionColl.toArray(new Action[actionColl.size()]);
            keyStrokes = new KeyStroke[actions.length];
            
            Arrays.sort(actions, ToolbarEditorDialog.actionComparator);
            for (int i = 0; i < actions.length; i++) {
                keyStrokes[i] = (KeyStroke)actions[i].getValue(Action.ACCELERATOR_KEY);
            }
        }
    }
}
