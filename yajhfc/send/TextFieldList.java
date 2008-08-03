package yajhfc.send;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.ListListModel;
import yajhfc.utils;


public abstract class TextFieldList<T extends TFLItem> extends JPanel implements ListSelectionListener, KeyListener, MouseListener, DocumentListener, FocusListener {

    protected JTextField textField;
    protected JList list;
    protected JScrollPane scrollPane;
    protected ListListModel<T> model;
    protected Action addAction, removeAction, modifyAction, upAction, downAction;
    protected JPopupMenu popup;
    protected boolean useUpDown;
    protected ArrayList<JComponent> localComponents;
    protected TFLItem lastSelection;
    protected JButton btnRemove;
    
    public Action getUpAction() {
        if (upAction == null) {
            upAction = new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!this.isEnabled()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    int selIdx = list.getSelectedIndex();
                    if (selIdx >= 1) {
//                        Object old = model.get(selIdx);
//                        model.set(selIdx, model.get(selIdx - 1));
//                        model.set(selIdx - 1, old);
                        model.moveUp(new int[] { selIdx });
                    }
                };
            };
            upAction.putValue(Action.NAME, utils._("Up"));
            upAction.putValue(Action.SHORT_DESCRIPTION, utils._("Moves the item upwards"));
            upAction.putValue(Action.SMALL_ICON, utils.loadIcon("navigation/Up"));
            upAction.setEnabled(false);
        }
        return upAction;
    }
    
    public Action getDownAction() {
        if (downAction == null) {
            downAction = new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!this.isEnabled()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    int selIdx = list.getSelectedIndex();
                    if (selIdx >= 0 && selIdx < model.getSize() - 1) {
//                        Object old = model.get(selIdx);
//                        model.set(selIdx, model.get(selIdx + 1));
//                        model.set(selIdx + 1, old);
                        model.moveDown(new int[] { selIdx });
                    }
                };
            };
            downAction.putValue(Action.NAME, utils._("Down"));
            downAction.putValue(Action.SHORT_DESCRIPTION, utils._("Moves the item downwards"));
            downAction.putValue(Action.SMALL_ICON, utils.loadIcon("navigation/Down"));
            downAction.setEnabled(false);
        }
        return downAction;
    }
    
    public Action getAddAction() {
        if (addAction == null) {
            addAction = new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!this.isEnabled()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    addListItem(textField.getText());                    
                };
            };
            addAction.putValue(Action.NAME, utils._("Add"));
            addAction.putValue(Action.SHORT_DESCRIPTION, utils._("Adds the text to the list"));
            addAction.putValue(Action.SMALL_ICON, utils.loadIcon("general/Add"));
        }
        return addAction;
    }
    
    public Action getRemoveAction() {
        if (removeAction == null) {
            removeAction = new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!this.isEnabled()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    if (list.getSelectedIndex() >= 0) {
                        lastSelection = null;
                        model.remove(list.getSelectedIndex());
                        //textField.setText("");
                    }
                };
            };
            removeAction.putValue(Action.NAME, utils._("Remove"));
            removeAction.putValue(Action.SHORT_DESCRIPTION, utils._("Removes the selected item from the list"));
            removeAction.putValue(Action.SMALL_ICON, utils.loadIcon("general/Delete"));
            removeAction.setEnabled(false);
        }
        return removeAction;
    }
    
    public Action getModifyAction() {
        if (modifyAction == null) {
            modifyAction = new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!this.isEnabled()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    TFLItem sel = (TFLItem)list.getSelectedValue();
                    modifyListItem(sel);
                };
            };
            modifyAction.putValue(Action.NAME, utils._("Update"));
            modifyAction.putValue(Action.SHORT_DESCRIPTION, utils._("Updates the selected list item"));
            modifyAction.putValue(Action.SMALL_ICON, utils.loadIcon("general/Refresh"));
            modifyAction.setEnabled(false);
        }
        return modifyAction;
    }
    
    protected void commitChanges(TFLItem sel) {
        sel.setText(textField.getText());
    }
    
    protected abstract T createListItem(String text);
//    {
//        return new DefTFLItem(text);
//    }
    
    protected void modifyListItem(TFLItem sel) {
        if (sel != null && sel.isMutable() && textField.getDocument().getLength() > 0) {
            commitChanges(sel);
            // Hack to redraw the list:
            //model.setElementAt(sel, list.getSelectedIndex());
            list.repaint();
        }
    }
    
    public void addListItem(T newItem) {
        if (!model.getList().contains(newItem)) {// Elements should be unique
            lastSelection = null;
            model.add(newItem);
            list.setSelectedIndex(model.getSize() - 1);
        }
    }
    
    public void addListItem(String text) {
        if (text.length() > 0) {
            addListItem(createListItem(text));
        }
    }
    
//    public DefaultListModel getModel() {
//        if (model == null) {
//            model = new DefaultListModel();
//        }
//        return model;
//    }
    
    public JList getList() {
        if (list == null) {
            list = new JList(model);
            list.setVisibleRowCount(3);
        }
        return list;
    }

    protected JPopupMenu getPopup() {
        if (popup == null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getModifyAction()));
            popup.addSeparator();
            popup.add(new JMenuItem(getAddAction()));
            popup.add(new JMenuItem(getRemoveAction()));
            if (useUpDown) {
                popup.addSeparator();
                popup.add(new JMenuItem(getUpAction()));
                popup.add(new JMenuItem(getDownAction()));
            }
        }        
        return popup;
    }
    
    public TextFieldList(JTextField textField, boolean haveUpDown) {
        this(textField, haveUpDown, new ArrayList<T>());
    }
    public TextFieldList(JTextField textField, boolean haveUpDown, List<T> internalList) {
        super(null, false);
        this.useUpDown = haveUpDown;
        this.textField = textField;
        
        double [][] dLay = {
                { TableLayout.FILL, TableLayout.PREFERRED },
                { TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}
        };
        this.setLayout(new TableLayout(dLay));
        
        localComponents = new ArrayList<JComponent>();
        model = new ListListModel<T>(internalList);
        
        scrollPane = new JScrollPane(getList(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        localComponents.add(scrollPane);
        add(scrollPane, "0, 0, 0, 2, F, F");
        
        JButton btnAdd = new JButton(getAddAction());
        btnAdd.setText("");
        localComponents.add(btnAdd);
        btnRemove = new JButton(getRemoveAction());
        btnRemove.setText("");
        localComponents.add(btnRemove);
        add(btnAdd, "1, 0");
        add(btnRemove, "1, 1");
        
        textField.addKeyListener(this);
        textField.getDocument().addDocumentListener(this);
        localComponents.add(textField);
        
        list.addKeyListener(this);
        list.addListSelectionListener(this);
        list.addMouseListener(this);
        localComponents.add(list);
        
        for (JComponent comp : localComponents) {
            comp.addFocusListener(this);
        }
    }

    /**
     * Adds a component that should be considered "local" in regard to focus
     * (i.e. belonging to this TextFieldList).
     * @param comp
     */
    public void addLocalComponent(JComponent comp) {
        localComponents.add(comp);
        comp.addFocusListener(this);
    }
    
    public void removeLocalComponent(JComponent comp) {
        if (localComponents.remove(comp)) {
            comp.removeFocusListener(this);
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == list) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                getRemoveAction().actionPerformed(null);
            }
        } else if (e.getSource() == textField) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (e.isControlDown() || (list.getSelectedIndex() < 0))
                    getAddAction().actionPerformed(null);
                else 
                    getModifyAction().actionPerformed(null);

            }
        }
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
           getPopup().show(this, e.getX(), e.getY());
        }
    }
    
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int selIdx = list.getSelectedIndex();
        TFLItem sel = (TFLItem)list.getSelectedValue();
        
        if (!e.getValueIsAdjusting() && lastSelection != null) {
            modifyListItem(lastSelection);
        }
        
        getModifyAction().setEnabled(selIdx >= 0 && sel.isMutable());
        getRemoveAction().setEnabled(selIdx >= 0 && sel.isMutable());
        if (useUpDown) {
            getUpAction().setEnabled(selIdx >= 1);
            getDownAction().setEnabled(selIdx >= 0 && selIdx < model.getSize() - 1);
        }
        
        if (sel != null && !e.getValueIsAdjusting()) {
            displayItem(sel);
            lastSelection = sel;
        }    
    }
    
    protected void displayItem(TFLItem sel) {
        if (sel.isMutable())
            textField.setText(sel.getText());
        else
            textField.setText("");
    }
    
    public void changedUpdate(DocumentEvent e) {
        getAddAction().setEnabled(textField.getDocument().getLength() > 0);        
    }
    
    public void commit() {
        if (model.getSize() == 0 && getAddAction().isEnabled()) {
            getAddAction().actionPerformed(null);
        } else if (getModifyAction().isEnabled()) {
            getModifyAction().actionPerformed(null);
        }
    }
    
    public void focusLost(FocusEvent e) {
        // Our group of components lost focus...
        if (e.getComponent().isEnabled() && !localComponents.contains(e.getOppositeComponent())) {
            commit();
        }
    }
    

    public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
            int selItem = list.getSelectedIndex();
            if (selItem >= 0 && list.getCellBounds(selItem, selItem).contains(e.getPoint()))
                getRemoveAction().actionPerformed(null);
        }
    }
    
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component comp : getComponents()) {
            comp.setEnabled(enabled);
        }
        list.setEnabled(enabled);
    } 
    
    public void keyReleased(KeyEvent e) {
        // method stub 
    }

    public void keyTyped(KeyEvent e) {
        // method stub
    }

    public void mouseEntered(MouseEvent e) {
        // method stub
    }

    public void mouseExited(MouseEvent e) {
        // method stub
    }

    public void insertUpdate(DocumentEvent e) {
        // method stub
    }

    public void removeUpdate(DocumentEvent e) {
        // method stub
    }
    
    public void focusGained(FocusEvent e) {
        // method stub
    }
    
}
