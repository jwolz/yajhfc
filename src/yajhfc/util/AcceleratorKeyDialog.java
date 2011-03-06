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
import java.awt.Dialog;
import java.awt.Frame;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

/**
 * @author jonas
 *
 */
public class AcceleratorKeyDialog extends JDialog {
    ActionToKeyStrokeTableModel tableModel;
    JTable table;
    KeyStrokeTextField textAccelerator;
    Action actSetAccelerator, actClearAccelerator;
    
    public AcceleratorKeyDialog(Dialog owner, Collection<Action> actions) {
        super(owner, _("Edit keyboard accelerators"), true);
        initialize(actions);
    }
    
    public AcceleratorKeyDialog(Frame owner, Collection<Action> actions) {
        super(owner, _("Edit keyboard accelerators"), true);
        initialize(actions);
    }
    
    private void initialize(Collection<Action> actions) {
        JPanel contentPane = new JPanel(new BorderLayout());
        
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
                KeyStroke key = keyStrokes[rowIndex];
                return (key == null) ? "" : key.toString();
            default:
                return null;
            }
        }
        
        public void setKeystroke(int rowIndex, KeyStroke key) {
            keyStrokes[rowIndex] = key;
            fireTableCellUpdated(rowIndex, 1);
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
