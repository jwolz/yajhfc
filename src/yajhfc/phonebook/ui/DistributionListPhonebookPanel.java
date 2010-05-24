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
package yajhfc.phonebook.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import yajhfc.Utils;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.JTableTABAction;
import yajhfc.util.LimitedPlainDocument;


/**
 * @author jonas
 *
 */
public class DistributionListPhonebookPanel extends PhonebookPanel {
    private static final Logger log = Logger.getLogger(DistributionListPhonebookPanel.class.getName());

    protected PhoneBookTableModel tableModel;
    protected JTable entryTable;
    protected JTextField nameField;
    protected JLabel tableLabel;
    protected Action actAddNewItem, actRemoveItem, actAddExistingItem;
    protected JButton[] distListButtons;
    protected JButton createDistListButton;
    
    protected DistributionList listToEdit = null;
    protected final List<PhoneBookEntry> selectedRows = new ArrayList<PhoneBookEntry>();
    protected ExistingItemDialog existingItemDialog;
    
    /**
     * @param parent
     * @param layout
     * @param isDoubleBuffered
     */
    public DistributionListPhonebookPanel(NewPhoneBookWin parent) {
        super(parent, null, false);
        createActions();
        
        double[][] dLay = {
                { TableLayout.FILL, NewPhoneBookWin.border, (1.0/3.0), NewPhoneBookWin.border, (1.0/3.0) },
                { TableLayout.PREFERRED, TableLayout.PREFERRED, NewPhoneBookWin.border, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED }
        };
        setLayout(new TableLayout(dLay));
        
        nameField = createEntryTextField(PBEntryField.Name);
        
        tableModel = new PhoneBookTableModel(null);
        entryTable = new JTable(tableModel);
        entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        entryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableTABAction.wrapDefTabAction(entryTable);
        entryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (listToEdit != null) {
                        int[] selIndices = entryTable.getSelectedRows();
                        selectedRows.clear();
                        for (int idx : selIndices) {
                            selectedRows.add(listToEdit.getEntries().get(idx));
                        }
                    }
                    checkEnable();
                }
            }
            
        });
        
        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(actAddNewItem);
        tablePopup.add(actAddExistingItem);
        tablePopup.addSeparator();
        tablePopup.add(actRemoveItem);
        
        entryTable.setComponentPopupMenu(tablePopup);
        
        createDistListButton = new JButton(parent.addDistListAction);
        createDistListButton.setText(Utils._("Create new distribution list from selection"));
        createDistListButton.setVisible(false);
        
        Utils.addWithLabel(this, nameField, Utils._("Name") + ":", "0,1,4,1");
        tableLabel = Utils.addWithLabel(this, new JScrollPane(entryTable), "Dummy", "0,4,4,4");
        
        distListButtons = new JButton[3];
        add(distListButtons[0] = new JButton(actAddNewItem), "0,5");
        add(distListButtons[1] = new JButton(actAddExistingItem), "2,5");
        add(distListButtons[2] = new JButton(actRemoveItem), "4,5");
        
        add(createDistListButton, "0,5,4,5");
        
        checkEnable();
    }

    private void createActions() {
        actAddNewItem = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                PhoneBook currentPhonebook = getCurrentPhoneBook();
                if (listToEdit == null || currentPhonebook == null || currentPhonebook.isReadOnly()) 
                    return;
                
                int row = listToEdit.getEntries().indexOf(listToEdit.addNewEntry());
                entryTable.getSelectionModel().setSelectionInterval(row, row);
            }
        };
        actAddNewItem.putValue(Action.NAME, Utils._("Add new item"));
        actAddNewItem.putValue(Action.SMALL_ICON, Utils.loadIcon("general/New"));
        actAddNewItem.putValue(Action.SHORT_DESCRIPTION, Utils._("Add a new entry to the distribution list"));
        actAddNewItem.setEnabled(false);
        
        actAddExistingItem = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                PhoneBook currentPhonebook = getCurrentPhoneBook();
                if (listToEdit == null || currentPhonebook == null || currentPhonebook.isReadOnly()) 
                    return;
                
                if (existingItemDialog == null || !existingItemDialog.isVisible()) {
                    existingItemDialog = new ExistingItemDialog();
                    existingItemDialog.setVisible(true);
                } else {
                    existingItemDialog.toFront();
                }
            }
        };
        actAddExistingItem.putValue(Action.NAME, Utils._("Add existing item"));
        actAddExistingItem.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Add"));
        actAddExistingItem.putValue(Action.SHORT_DESCRIPTION, Utils._("Add an existing phone book entry to the distribution list"));
        actAddExistingItem.setEnabled(false);

        actRemoveItem = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (selectedRows.size() == 0)
                    return;
                
                if (JOptionPane.showConfirmDialog(parent, Utils._("Do you want to delete the selected entries?"), Utils._("Delete entries"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    for (PhoneBookEntry entry : selectedRows.toArray(new PhoneBookEntry[selectedRows.size()])) {
                        entry.delete();
                    }
                }
            }
        };
        actRemoveItem.putValue(Action.NAME, Utils._("Delete"));
        actRemoveItem.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Delete"));
        actRemoveItem.putValue(Action.SHORT_DESCRIPTION, Utils._("Delete selected entry"));
        actRemoveItem.setEnabled(false);
    }
    
    void checkEnable() {
        PhoneBook currentPhonebook = getCurrentPhoneBook();
        boolean canWrite = (listToEdit != null) && (currentPhonebook != null) && !currentPhonebook.isReadOnly();
        boolean haveSelection = (selectedRows.size() > 0);
        boolean canDelete = canWrite && haveSelection;
        
        actAddExistingItem.setEnabled(canWrite);
        actAddNewItem.setEnabled(canWrite);
        actRemoveItem.setEnabled(canDelete);
    }
    
    PhoneBook getCurrentPhoneBook() {
        return listToEdit == null ? null : listToEdit.getParent();
    }
    
    @Override
    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if (!flag && existingItemDialog != null) {
            existingItemDialog.dispose();
            existingItemDialog = null;
            tableModel.setPhoneBook(null);
        }
    }
    
    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookPanel#readFromTextFields(yajhfc.phonebook.PhoneBookEntry, boolean)
     */
    @Override
    public void readFromTextFields(PhoneBookEntry pb, boolean updateOnly) {
        if (pb != listToEdit) {
            log.warning("pb != listToEdit");
        }
        if (pb instanceof DistributionList) {
            if (entryTable.isEditing()) {
                entryTable.getCellEditor().stopCellEditing();
            }
            pb.setField(PBEntryField.Name, nameField.getText());
            pb.commit();
        } else {
            log.severe("No distribution list specified in readFromTextFields!");
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhonebookPanel#writeToTextFields(yajhfc.phonebook.PhoneBook, yajhfc.phonebook.PhoneBookEntry)
     */
    @Override
    public void writeToTextFields(PhoneBook phoneBook, PhoneBookEntry pb) {
        if (pb instanceof DistributionList) {
            if (existingItemDialog != null) {
                existingItemDialog.dispose();
                existingItemDialog = null;
            }
            listToEdit = (DistributionList)pb;
            boolean editable = !phoneBook.isReadOnly();
            
            tableModel.setPhoneBook(listToEdit);
            tableModel.setEditable(editable);
            entryTable.setModel(tableModel);
            
            nameField.setText(pb.getField(PBEntryField.Name));
            nameField.setEditable(editable);
            ((LimitedPlainDocument)nameField.getDocument()).setLimit(phoneBook.getMaxLength(PBEntryField.Name));
            
            configureUI(true);
            checkEnable();
        } else {
            log.severe("No distribution list specified in writeToTextFields!");
        }
    }

    private void configureUI(boolean showDistList) {
        tableLabel.setText(showDistList ? 
                Utils._("Items in the distribution list:") :
                Utils._("Items in the current selection:"));
        
        if (createDistListButton.isVisible() == showDistList) {
            for (JButton button : distListButtons) {
                button.setVisible(showDistList);
            }
            createDistListButton.setVisible(!showDistList);
        }
    }
    
    public void showMultiSelection(PhoneBook phoneBook, List<PhoneBookEntry> pbs) {
        if (existingItemDialog != null) {
            existingItemDialog.dispose();
            existingItemDialog = null;
        }
        listToEdit = null;
        
        PBEntryFieldTableModel model = new PBEntryFieldTableModel(Collections.<PBEntryFieldContainer>unmodifiableList(NewPhoneBookWin.resolveDistributionLists(pbs)));
        model.setEditable(false);
        entryTable.setModel(model);
        
        nameField.setEditable(false);
        nameField.setText("<" + Utils._("Current selection") + ">");

        configureUI(false);
        
        checkEnable();
    }
    
    protected class ExistingItemDialog extends JDialog implements TreeSelectionListener {
        private Action addAction;
        List<PhoneBookEntry> selItems = new ArrayList<PhoneBookEntry>();
        private JTree itemsTree;
        
        public ExistingItemDialog() {
            super(parent, Utils._("Items to add"));
            double[][] dLay = {
                    { TableLayout.FILL },
                    { TableLayout.PREFERRED, TableLayout.FILL, NewPhoneBookWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED }
            };
            JPanel contentPane = new JPanel(new TableLayout(dLay));

            itemsTree = new JTree(parent.treeModel);
            itemsTree.setEditable(false);
            itemsTree.setRootVisible(true);
            itemsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            itemsTree.setCellRenderer(parent.phoneBookRenderer);
            itemsTree.addTreeSelectionListener(this);
            
            addAction = new ExcDialogAbstractAction() {
                public void actualActionPerformed(ActionEvent e) {
                    if (listToEdit != null) {
                        listToEdit.addEntries(NewPhoneBookWin.resolveDistributionLists(selItems));
                    }
                }
            };
            addAction.putValue(Action.NAME, Utils._("Add"));
            addAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Add"));
            addAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Add the selected entry to the distribution list"));
            addAction.setEnabled(false);
            
            CancelAction cancelAct = new CancelAction(this, Utils._("Close"));
            
            JPopupMenu treePopup = new JPopupMenu();
            treePopup.add(addAction);
            itemsTree.setComponentPopupMenu(treePopup);
            
            contentPane.add(new JLabel("<html>" + Utils._("Please select the items to add to the distribution list:") + "</html>"), "0,0");
            contentPane.add(new JScrollPane(itemsTree), "0,1");
            contentPane.add(new JButton(addAction), "0,3");
            contentPane.add(cancelAct.createCancelButton(), "0,4");
            
            setContentPane(contentPane);
            setResizable(true);
            pack();
            Rectangle screenSize = getGraphicsConfiguration().getBounds();
            setLocation(screenSize.x + screenSize.width - getWidth(), screenSize.y + (screenSize.height - getHeight()) / 2);
        }

        public void valueChanged(TreeSelectionEvent e) {
            TreePath[] paths = itemsTree.getSelectionPaths();

            // Read selection:
            selItems.clear();
            if (paths != null && paths.length > 0) {
                for (TreePath tp : paths) {
                    if (tp.getPathCount() == 3) {
                        selItems.add((PhoneBookEntry)tp.getPathComponent(2));
                    }
                }
            }
            
            addAction.setEnabled(selItems.size() > 0);
        }

    }
}
