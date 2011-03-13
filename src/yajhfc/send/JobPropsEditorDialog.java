/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.send;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
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
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.JTableTABAction;
import yajhfc.util.MapTableModel;

/**
 * @author jonas
 *
 */
public class JobPropsEditorDialog extends JDialog {
    protected Map<String,String> mapToEdit;
    protected MapTableModel model;
    protected JTable mapTable;
    
    protected Action okAction, deleteAction;
    
    /**
     * Available job properties as listed by the "JPARM" command
     */
    static final String[] availableProperties = {
        "BEGBR",
        "BEGST",
        "CHOPTHRESHOLD",
        "CLIENT",
        "COMMENTS",
        "COMMID",
        "DATAFORMAT",
        "DIALSTRING",
        "DONEOP",
        "EXTERNAL",
        "FAXNUMBER",
        "FROMCOMPANY",
        "FROMLOCATION",
        "FROMUSER",
        "FROMVOICE",
        "GROUPID",
        "JOBID",
        "JOBINFO",
        "JOBTYPE",
        "LASTTIME",
        "MAXDIALS",
        "MAXTRIES",
        "MINBR",
        "MODEM",
        "NDIALS",
        "NOTIFYADDR",
        "NOTIFY",
        "NPAGES",
        "NTRIES",
        "OWNER",
        "PAGECHOP",
        "PAGELENGTH",
        "PAGEWIDTH",
        "PASSWD",
        "REGARDING",
        "RETRYTIME",
        "SCHEDPRI",
        "SENDTIME",
        "STATE",
        "STATUS",
        "STATUSCODE",
        "SUBADDR",
        "TAGLINE",
        "TOCOMPANY",
        "TOLOCATION",
        "TOTDIALS",
        "TOTPAGES",
        "TOTTRIES",
        "TOUSER",
        "TOVOICE",
        "TSI",
        "USECONTCOVER",
        "USEECM",
        "USETAGLINE",
        "USEXVRES",
        "USRKEY",
        "VRES",
    };
    
    public JobPropsEditorDialog(Dialog owner, Map<String,String> mapToEdit) {
        super(owner, Utils._("Job properties"), true);
        this.mapToEdit = mapToEdit;
        initialize();
    }
 
    private void initialize() {
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
        JComboBox keyCombo = new JComboBox(availableProperties);
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
        
        contentPane.add(new JLabel("<html>" + Utils._("This dialog allows you to set HylaFAX job properties directly. Use it only if you know what you are doing!") + "</html>"), BorderLayout.NORTH);
        contentPane.add(new JScrollPane(mapTable), BorderLayout.CENTER);
        contentPane.add(buttonBox, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        pack();
        if (getWidth() > 640) {
            setSize(640, getHeight());
        }
        Utils.setDefWinPos(this);
    }
}
