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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author jonas
 *
 */
public class AcceleratorKeyDialog extends JDialog {
    ActionToKeyStrokeTableModel tableModel;
    JTable table;
    KeyStrokeTextField textAccelerator;
    Action actSetAccelerator, actClearAccelerator, actOK, actReset, actUseDefault;
    
    public AcceleratorKeyDialog(Dialog owner, Collection<Action> actions) {
        super(owner, _("Edit keyboard accelerators"), true);
        initialize(actions);
    }
    
    public AcceleratorKeyDialog(Frame owner, Collection<Action> actions) {
        super(owner, _("Edit keyboard accelerators"), true);
        initialize(actions);
    }
    
    private void initialize(Collection<Action> actions) {
    	actSetAccelerator = new ExcDialogAbstractAction(_("Set Key")) {
			@Override
			protected void actualActionPerformed(ActionEvent e) {
				tableModel.setKeystroke(table.getSelectedRow(), textAccelerator.getKeyStroke());
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
					int row = table.getSelectedRow();
					
				    boolean enableEditing = (row >= 0);
			    	actSetAccelerator.setEnabled(enableEditing);
			    	actUseDefault.setEnabled(enableEditing);
			    	actClearAccelerator.setEnabled(enableEditing);
			    	textAccelerator.setEnabled(enableEditing);
			    	
			    	if (enableEditing) {
			    		textAccelerator.setKeyStroke(tableModel.getKeystroke(row));
			    	} else {
			    		textAccelerator.setKeyStroke(null);
			    	}
				}
			}
		});
        
        textAccelerator = new KeyStrokeTextField();
        
        CancelAction cancelAct = new CancelAction(this);
        
        JPanel editPanel = new JPanel(null);
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.Y_AXIS));
        JLabel accLabel = new JLabel(_("Keyboard Accelerator:"));
        accLabel.setAlignmentX(LEFT_ALIGNMENT);
        editPanel.add(accLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(new JButton(actOK));
        buttonPanel.add(new JButton(actReset));
        buttonPanel.add(cancelAct.createCancelButton());
        
        
        setContentPane(contentPane);
    }
    
    static class ActionToKeyStrokeTableModel extends AbstractTableModel {
        private final static String[] cols = {
            _("Action"),
            _("Accelerator Key")
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
        
        public ActionToKeyStrokeTableModel(Collection<Action> actionColl) {
            actions = actionColl.toArray(new Action[actionColl.size()]);
            Arrays.sort(actions, ToolbarEditorDialog.actionComparator);
            for (int i = 0; i < actions.length; i++) {
                keyStrokes[i] = (KeyStroke)actions[i].getValue(Action.ACCELERATOR_KEY);
            }
        }
    }
}
