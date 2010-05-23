package yajhfc.util;
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

import static yajhfc.Utils._;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import yajhfc.Utils;

public class fmtEditor<T> extends JPanel 
    implements ActionListener {
    
    private final ListListModel<T> selectedFmtModel;
    private final SortedListModel<T> deselectedFmtModel;
    private final Collection<T> dontDeleteFmts;
    private final Collection<T> availableFmts;
    
    private final ListCellRenderer listRenderer;
    private final Comparator<T> listComparator;
    
    /**
     * Separator, can be null
     */
    final T separator;
    
    JPanel buttonPane, availPane, selectedPane;
    JButton buttonAdd, buttonDelete, buttonUp, buttonDown;
    JList listAvail, listSelected;
    JScrollPane scrollAvail, scrollSelected;
    
    boolean canDelete(Object[] items) {
        if (items.length == 0 || selectedFmtModel.getSize() <= 1) // Ensure at least one item is present
            return false;
        
        if (dontDeleteFmts == null)
            return true;
        else {
            for (Object o : items) {
                if (dontDeleteFmts.contains(o)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    
    private void initialize(String selCaption, String deselCaption) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        Dimension spacer = new Dimension(5,0);
        add(getAvailPane(deselCaption));
        add(Box.createRigidArea(spacer));
        add(getButtonPane());
        add(Box.createRigidArea(spacer));
        add(getSelectedPane(selCaption));
        
        if (deselectedFmtModel.getSize() > 0)
            listAvail.setSelectedIndex(0);
        if (selectedFmtModel.getSize() > 0)
            listSelected.setSelectedIndex(0);
    }
    
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));            
            
            buttonPane.add(Box.createVerticalGlue());
            
            buttonUp = new JButton();
            initMoveButton(buttonUp, "Up", _("Up"));
            buttonUp.setToolTipText(_("Moves the selected item up"));
            buttonPane.add(buttonUp);
            
            buttonPane.add(Box.createVerticalStrut(10));
            
            buttonAdd = new JButton();
            initMoveButton(buttonAdd, "Forward", ">>");
            buttonAdd.setToolTipText(_("Adds the selected item"));
            buttonPane.add(buttonAdd);
            
            buttonPane.add(Box.createVerticalStrut(10));
            
            buttonDelete = new JButton();
            initMoveButton(buttonDelete, "Back", "<<");
            buttonDelete.setToolTipText(_("Removes the selected item"));
            buttonPane.add(buttonDelete);
            
            buttonPane.add(Box.createVerticalStrut(10));
            
            buttonDown = new JButton();
            initMoveButton(buttonDown, "Down", _("Down"));
            buttonDown.setToolTipText(_("Moves the selected item down"));
            buttonPane.add(buttonDown);  
            
            buttonPane.add(Box.createVerticalGlue());
        }
        return buttonPane;
    }
    
    private JPanel getAvailPane(String deselCaption) {
        if (availPane == null) {
            availPane = new JPanel(new BorderLayout());
            availPane.add(new JLabel(deselCaption), BorderLayout.NORTH);
            
            listAvail = new JList(deselectedFmtModel);
            listAvail.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listAvail.setCellRenderer(listRenderer);
            listAvail.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        buttonAdd.setEnabled(listAvail.getSelectedIndex() >= 0);
                    }
                };
            });
                        
            scrollAvail = new JScrollPane(listAvail);
            scrollAvail.setPreferredSize(new Dimension(100, 100));
            
            availPane.add(scrollAvail, BorderLayout.CENTER);
        }
        return availPane;
    }
    
    private JPanel getSelectedPane(String selCaption) {
        if (selectedPane == null) {
            selectedPane = new JPanel(new BorderLayout());
            selectedPane.add(new JLabel(selCaption), BorderLayout.NORTH);
            
            listSelected = new JList(selectedFmtModel);
            listSelected.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listSelected.setCellRenderer(listRenderer);
            listSelected.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int minselidx = listSelected.getMinSelectionIndex();
                        int maxselidx = listSelected.getMaxSelectionIndex();
                        buttonUp.setEnabled(minselidx > 0);
                        buttonDown.setEnabled((minselidx >=0) && (maxselidx < (listSelected.getModel().getSize() - 1)));
                        buttonDelete.setEnabled((minselidx >= 0) && canDelete(listSelected.getSelectedValues()));
                    }
                };
            });
            
            scrollSelected = new JScrollPane(listSelected);
            scrollSelected.setPreferredSize(new Dimension(100, 100));
            
            selectedPane.add(scrollSelected, BorderLayout.CENTER);
        }
        return selectedPane;
    }
    
    private void initMoveButton(JButton button, String name, String alt) {
        ImageIcon ico = Utils.loadIcon("navigation/" + name);
        if (ico != null)
            button.setIcon(ico);
        else
            button.setText(alt);
        button.setActionCommand(name);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 100));
        button.addActionListener(this);
        button.setEnabled(false);
    }
    

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("Forward")) { //>>
            int[] selIndexes = listAvail.getSelectedIndices();
            if (selIndexes.length == 0) {
                return;
            }
            for (int i = selIndexes.length - 1; i >=0; i--) {
                int index = selIndexes[i];
                T selObj = deselectedFmtModel.get(index);
                
                selectedFmtModel.add(selObj);
                if (selObj != separator) {
                    deselectedFmtModel.remove(index);
                }
            }
        } else {
            int[] selIndexes = listSelected.getSelectedIndices();
            if (selIndexes.length == 0) {
                return;
            }
            if (e.getActionCommand().equals("Back")) { //<<
                Object[] sel = listSelected.getSelectedValues();
                if (canDelete(sel)) {
                    for (Object o : sel) {
                        if (o != separator) {
                            deselectedFmtModel.add((T)o);
                        }
                    }
                    selectedFmtModel.removeAll(selIndexes);
                }
            } else if (e.getActionCommand().equals("Up")) {
                selectedFmtModel.moveUp(selIndexes);
                for (int i = 0; i < selIndexes.length; i++) {
                    selIndexes[i]--;
                }
                listSelected.setSelectedIndices(selIndexes);
            } else if (e.getActionCommand().equals("Down")) { 
                selectedFmtModel.moveDown(selIndexes);
                for (int i = 0; i < selIndexes.length; i++) {
                    selIndexes[i]++;
                }
                listSelected.setSelectedIndices(selIndexes);
            }     
        }
    }
    
    public void setNewSelection(Collection<T> newSelected) {
        List<T> deselected = new ArrayList<T>(availableFmts);
        deselected.removeAll(newSelected);
        if (separator != null) {
            deselected.add(separator);
        }
        
        deselectedFmtModel.clear();
        deselectedFmtModel.addAll(deselected);
        
        selectedFmtModel.clear();
        selectedFmtModel.addAll(newSelected);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        Utils.enableChildren(this, enabled);
        super.setEnabled(enabled);
    }
    

    
    /**
     * Creates a new fmtEditor
     * @param avail the available elements, should not contain the separator
     * @param selected the selected elements; this list is edited in place by this editor
     * @param dontDelete a list of selected elements that may not be deleted
     * @param listRenderer the list cell renderer used to render the items
     * @param comparator a comparator used to compare items in order to sort the lists.
     * @param separator a special "separator" available item that does not get deleted from the available list and may show up multiple times in the selected list. 
     * May be null if no such element is necessary.
     * @param selCaption the caption for the "selected" list
     * @param deselCaption the caption for the "deselected" list
     */
    public fmtEditor(T[] avail, List<T> selected, Collection<T> dontDelete, ListCellRenderer listRenderer, Comparator<T> comparator, T separator, String selCaption, String deselCaption) {
        this(Arrays.asList(avail), selected, dontDelete, listRenderer, comparator, separator, selCaption, deselCaption);
    }
    /**
     * Creates a new fmtEditor
     * @param avail the available elements, should not contain the separator
     * @param selected the selected elements; this list is edited in place by this editor
     * @param dontDelete a list of selected elements that may not be deleted
     * @param listRenderer the list cell renderer used to render the items
     * @param comparator a comparator used to compare items in order to sort the lists.
     * @param separator a special "separator" available item that does not get deleted from the available list and may show up multiple times in the selected list. 
     * May be null if no such element is necessary.
     * @param selCaption the caption for the "selected" list
     * @param deselCaption the caption for the "deselected" list
     */
    public fmtEditor(Collection<T> avail, List<T> selected, Collection<T> dontDelete, ListCellRenderer listRenderer, Comparator<T> comparator, T separator, String selCaption, String deselCaption) {
        super(false);
        List<T> deselectedFmts = new ArrayList<T>(avail);
        if (selected.size() > 0)
            deselectedFmts.removeAll(selected);
        if (separator != null) {
            deselectedFmts.add(separator);
        }
        
        dontDeleteFmts = dontDelete;
        this.listRenderer = listRenderer;
        this.listComparator = comparator;
        this.separator = separator;
        this.availableFmts = avail;
        
        this.deselectedFmtModel = new SortedListModel<T>(deselectedFmts, listComparator);
        this.selectedFmtModel = new ListListModel<T>(selected);
        
        initialize(selCaption, deselCaption);
    }
}
