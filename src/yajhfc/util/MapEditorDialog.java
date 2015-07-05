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
package yajhfc.util;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.Utils;

public abstract class MapEditorDialog extends JDialog {

    protected Map<String,String> mapToEdit;
    protected MapTableModel model;
    protected JTable mapTable;
    protected Action okAction;
    protected Action deleteAction;

    public MapEditorDialog(Frame owner, String title, Map<String,String> mapToEdit) throws HeadlessException {
        super(owner, title, true);
        this.mapToEdit = mapToEdit;
        initialize();
    }

    public MapEditorDialog(Dialog owner, String title, Map<String,String> mapToEdit) throws HeadlessException {
        super(owner, title, true);
        this.mapToEdit = mapToEdit;
        initialize();
    }

    protected void initialize() {
        deleteAction = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int row = mapTable.getSelectedRow();
                if (row >= 0 && model.rowIsDeletable(row)) {
                    model.deleteRow(row);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };
        deleteAction.putValue(Action.NAME, Utils._("Remove row"));
        
        okAction = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (mapTable.isEditing()) {
                    mapTable.getCellEditor().stopCellEditing();
                 }
                
                mapToEdit.clear();
                mapToEdit.putAll(model.getMapToEdit());
                dispose();
            }
        };
        okAction.putValue(Action.NAME, Utils._("OK"));
        
        CancelAction cancelAction = new CancelAction(this);
        
        model = new MapTableModel(new TreeMap<String,String>(mapToEdit));
        mapTable = new JTable(model);
        mapTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                deleteAction.setEnabled(e.getFirstIndex() >= 0 && model.rowIsDeletable(e.getFirstIndex()));
            }
        });
        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(deleteAction);
        mapTable.setComponentPopupMenu(tablePopup);
        JComboBox keyCombo = new JComboBox(getAvailableProperties());
        keyCombo.setEditable(true);
        mapTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(keyCombo));
        mapTable.getActionMap().put(deleteAction.getClass().getName(), deleteAction);
        mapTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteAction.getClass().getName());
        JTableTABAction.wrapDefTabAction(mapTable);
        
        JPanel contentPane = new JPanel(new BorderLayout(6,6));
        
        Box buttonBox = Box.createHorizontalBox();
        Dimension spacer = new Dimension(6,6);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(new JButton(okAction));
        buttonBox.add(Box.createRigidArea(spacer));
        buttonBox.add(cancelAction.createCancelButton());
        buttonBox.add(Box.createHorizontalGlue());
        
        contentPane.add(new JLabel("<html>" + getCaption() + "</html>"), BorderLayout.NORTH);
        contentPane.add(new JScrollPane(mapTable), BorderLayout.CENTER);
        contentPane.add(buttonBox, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        pack();
        if (getWidth() > 640) {
            setSize(640, getHeight());
        }
        Utils.setDefWinPos(this);
    }

    /**
     * Returns the text to be used as caption on top of the dialog
     * @return
     */
    protected abstract String getCaption();

    /**
     * Returns the list of properties that should be selectable from the combo box in the first table column
     * @return
     */
    protected abstract String[] getAvailableProperties();

}