/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import yajhfc.FaxOptions;
import yajhfc.HylaClientManager;
import yajhfc.HylaModem;
import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.JTableTABAction;

/**
 * @author jonas
 *
 */
public class ModemsPanel extends JPanel implements OptionsPage {

    ButtonGroup customOrAllGroup;
    Action actAdd, actRemove, actUp, actDown, actReset;
    JTable modemsTable;
    JRadioButton radAuto, radManual;
    HylaModemTableModel tableModel;
    
    private static final int border = 6;
    
    public ModemsPanel() {
        super(false);
        initialize();
    }
    
    private void createActions() {
        actAdd = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                tableModel.addRow();
            }
        };
        actAdd.putValue(Action.NAME, _("Add"));
        actAdd.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Add"));
        
        actRemove = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                int row = modemsTable.getSelectedRow();
                if (row >= 0) {
                    tableModel.deleteRow(row);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            };
        };
        actRemove.putValue(Action.NAME, Utils._("Remove"));
        actRemove.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Delete"));
        actRemove.setEnabled(false);
        
        
        actUp = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                int row = modemsTable.getSelectedRow();
                if (row >= 1) {
                    tableModel.moveUp(row);
                    modemsTable.getSelectionModel().setSelectionInterval(row-1, row-1);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            };
        };
        actUp.putValue(Action.NAME, Utils._("Up"));
        actUp.putValue(Action.SMALL_ICON, Utils.loadIcon("navigation/Up"));
        actUp.setEnabled(false);

        actDown = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                int row = modemsTable.getSelectedRow();
                if (row >= 0 && row < tableModel.getRowCount()-1) {
                    tableModel.moveDown(row);
                    modemsTable.getSelectionModel().setSelectionInterval(row+1, row+1);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            };
        };
        actDown.putValue(Action.NAME, Utils._("Down"));
        actDown.putValue(Action.SMALL_ICON, Utils.loadIcon("navigation/Down"));
        actDown.setEnabled(false);
        
        actReset = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                if (JOptionPane.showConfirmDialog(ModemsPanel.this, _("Reset the list of modems?"), _("Reset"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    resetModemsList();
                }
            };
        };
        actReset.putValue(Action.NAME, Utils._("Reset"));
        actReset.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Refresh"));
    }
    
    private void initialize() {
        createActions();
        
        final int rowCount = 7;
        double[][] dLay = {
                {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                new double[rowCount*2+2]
        };
        for (int i=0; i<rowCount; i++) {
            dLay[1][i*2] = border;
            dLay[1][i*2+1] = TableLayout.PREFERRED;
        }
        dLay[1][rowCount*2]   = TableLayout.FILL;
        dLay[1][rowCount*2+1] = border;
        
        setLayout(new TableLayout(dLay));
        
        ItemListener radioListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableCheck();
            }
        };
        radAuto = new JRadioButton(_("Automatically get the list of modems"));
        radAuto.addItemListener(radioListener);
        
        radManual = new JRadioButton(_("Use the following list of modems:"));
        radManual.addItemListener(radioListener);
        
        customOrAllGroup = new ButtonGroup();
        customOrAllGroup.add(radAuto);
        customOrAllGroup.add(radManual);
        
        tableModel = new HylaModemTableModel();
        
        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(new JMenuItem(actAdd));
        tablePopup.add(new JMenuItem(actRemove));
        tablePopup.addSeparator();
        tablePopup.add(new JMenuItem(actUp));
        tablePopup.add(new JMenuItem(actDown));
        
        modemsTable = new JTable(tableModel);
        modemsTable.setShowGrid(true);
        modemsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        modemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    enableCheck();
                }
            }
        });
        modemsTable.getActionMap().put("yajhfc-delete", actRemove);
        final InputMap im = modemsTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "yajhfc-delete");
        JTableTABAction.wrapDefTabAction(modemsTable);
        modemsTable.setComponentPopupMenu(tablePopup);
        
        add(radAuto, new TableLayoutConstraints(1, 1, 3, 1, TableLayout.LEFT, TableLayout.CENTER));
        add(radManual, new TableLayoutConstraints(1, 3, 4, 3, TableLayout.LEFT, TableLayout.CENTER));
        add(new JScrollPane(modemsTable), new TableLayoutConstraints(1, 5, 1, rowCount*2, TableLayout.FULL, TableLayout.FULL));
        add(new JButton(actAdd), new TableLayoutConstraints(3, 5));
        add(new JButton(actRemove), new TableLayoutConstraints(3, 7));
        add(new JButton(actUp), new TableLayoutConstraints(3, 9));
        add(new JButton(actDown), new TableLayoutConstraints(3, 11));
        add(new JButton(actReset), new TableLayoutConstraints(3, 13));
    }
    
    void enableCheck() {
        boolean itemSelected = (modemsTable.getSelectedRow() >= 0);
        
        actUp.setEnabled(itemSelected);
        actDown.setEnabled(itemSelected);
        actRemove.setEnabled(itemSelected);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        tableModel.loadFromStringList(foEdit.customModems);
        if (foEdit.useCustomModems) {
            radManual.setSelected(true);
        } else {
            radAuto.setSelected(true);
            if (foEdit.customModems.size() == 0) {
                resetModemsList();
            }
        }
        
        enableCheck();
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.useCustomModems = radManual.isSelected();
        tableModel.saveToStringList(foEdit.customModems);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        if (modemsTable.isEditing()) {
            modemsTable.getCellEditor().stopCellEditing();
         }
        return true;
    }

    void resetModemsList() {
        tableModel.clear();
        tableModel.addAll(HylaModem.defaultModems);
        HylaClientManager clientManager = Launcher2.application.getClientManager();
        if (clientManager != null)
            tableModel.addAll(clientManager.getRealModems());

    }

    static class MutableHylaModem extends HylaModem {
        public MutableHylaModem(HylaModem toCopy) {
            this(toCopy.getInternalName(), toCopy.getNumber(), toCopy.getDescription());
        }
        
        public MutableHylaModem(String internalName, String number,
                String description) {
            super(internalName, number, description);
        }
        
        public MutableHylaModem(String serializedForm) {
            super(serializedForm);
        }
        
        public void setInternalName(String newName) {
            checkUpdateDescription(newName, number);
            this.internalName = newName;
        }
        
        public void setNumber(String newNumber) {
            checkUpdateDescription(internalName, newNumber);
            this.number = newNumber;
        }
        
        public void setDescription(String newDescription) {
            if (newDescription == null || newDescription.length() == 0) {
                description = generateDescription(internalName, number);
            } else { 
                this.description = newDescription;
            }
        }
        
        protected void checkUpdateDescription(String newInternalName, String newNumber) {
            if (description == null || description.length() == 0 
                    || description.equals(generateDescription(internalName, number))) {
                description = generateDescription(newInternalName, newNumber);
            }
        }
    }
    
    static class HylaModemTableModel extends AbstractTableModel {
        protected static final String[] columns = {
            _("HylaFAX name"),
            _("Number/Description"),
            _("Full Description"),
        };
        
        protected List<MutableHylaModem> modems = new ArrayList<MutableHylaModem>();
        
        public int getColumnCount() {
            return columns.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getRowCount() {
            return modems.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            MutableHylaModem modem = modems.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return modem.getInternalName();
            case 1:
                return modem.getNumber();
            case 2:
                return modem.getDescription();
            default:
                return null;
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            MutableHylaModem modem = modems.get(rowIndex);
            switch (columnIndex) {
            case 0:
                modem.setInternalName(aValue.toString());
                break;
            case 1:
                modem.setNumber(aValue.toString());
                break;
            case 2:
                modem.setDescription(aValue.toString());
                break;
            default:
                // Do nothing
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        
        
        public void addRow() {
            addRow(new MutableHylaModem("", "", ""));
        }
        
        public void addRow(MutableHylaModem modem) {
            modems.add(modem);
            fireTableRowsInserted(modems.size()-1, modems.size()-1);
        }
        
        public HylaModem getRow(int rowIndex) {
            return modems.get(rowIndex);
        }
        
        public void clear() {
            modems.clear();
            fireTableDataChanged();
        }
       
        public void addAll(Collection<? extends HylaModem> modemColl) {
            for (HylaModem modem : modemColl) {
                modems.add(new MutableHylaModem(modem));
            }
            fireTableDataChanged();
        }
        
        public void deleteRow(int rowIndex) {
            modems.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
        
        public void moveUp(int rowIndex) {
            if (rowIndex <= 0)
                throw new ArrayIndexOutOfBoundsException("Cannot move up further");
            MutableHylaModem temp = modems.get(rowIndex);
            modems.set(rowIndex, modems.get(rowIndex-1));
            modems.set(rowIndex-1, temp);
            fireTableRowsUpdated(rowIndex-1, rowIndex);
        }
        
        public void moveDown(int rowIndex) {
            if (rowIndex >= modems.size()-1)
                throw new ArrayIndexOutOfBoundsException("Cannot move down further");
            MutableHylaModem temp = modems.get(rowIndex);
            modems.set(rowIndex, modems.get(rowIndex+1));
            modems.set(rowIndex+1, temp);
            fireTableRowsUpdated(rowIndex, rowIndex+1);
        }
        
        public void saveToStringList(List<String> stringList) {
            stringList.clear();
            for (MutableHylaModem modem : modems) {
                stringList.add(modem.saveToString());
            }
        }
        
        public void loadFromStringList(List<String> stringList) {
            modems.clear();
            for (String s : stringList) {
                modems.add(new MutableHylaModem(s));
            }
            fireTableDataChanged();
        }
    }
}
