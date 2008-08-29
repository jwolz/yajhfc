package yajhfc;
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

import static yajhfc.utils._;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class fmtEditor extends JPanel 
    implements ActionListener {
    
    private List<FmtItem> selectedFmts;
    private List<FmtItem> deselectedFmts;
    private List<FmtItem> dontDeleteFmts;
    
    private JPanel buttonPane, availPane, selectedPane;
    private JButton buttonAdd, buttonDelete, buttonUp, buttonDown;
    private JList listAvail, listSelected;
    private JScrollPane scrollAvail, scrollSelected;
    
    private boolean canDelete(Object[] items) {
        if (items.length == 0 || selectedFmts.size() <= 1) // Ensure at least one item is present
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
    
    
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        add(getAvailPane());
        add(Box.createHorizontalStrut(5));
        add(getButtonPane());
        add(Box.createHorizontalStrut(5));
        add(getSelectedPane());
        
        if (deselectedFmts.size() > 0)
            listAvail.setSelectedIndex(0);
        if (selectedFmts.size() > 0)
            listSelected.setSelectedIndex(0);
    }
    
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));            
            
            buttonPane.add(Box.createVerticalGlue());
            
            buttonUp = new JButton();
            initMoveButton(buttonUp, "Up", _("Up"));
            buttonUp.setToolTipText(_("Moves the selected Column up"));
            buttonPane.add(buttonUp);
            
            buttonPane.add(Box.createVerticalStrut(10));
            
            buttonAdd = new JButton();
            initMoveButton(buttonAdd, "Forward", ">>");
            buttonAdd.setToolTipText(_("Adds the selected Column"));
            buttonPane.add(buttonAdd);
            
            buttonPane.add(Box.createVerticalStrut(10));
            
            buttonDelete = new JButton();
            initMoveButton(buttonDelete, "Back", "<<");
            buttonDelete.setToolTipText(_("Removes the selected Column"));
            buttonPane.add(buttonDelete);
            
            buttonPane.add(Box.createVerticalStrut(10));
            
            buttonDown = new JButton();
            initMoveButton(buttonDown, "Down", _("Down"));
            buttonDown.setToolTipText(_("Moves the selected Column down"));
            buttonPane.add(buttonDown);  
            
            buttonPane.add(Box.createVerticalGlue());
        }
        return buttonPane;
    }
    
    private JPanel getAvailPane() {
        if (availPane == null) {
            availPane = new JPanel(new BorderLayout());
            availPane.add(new JLabel(_("Available Columns:")), BorderLayout.NORTH);
            
            listAvail = new JList(new SortedListModel<FmtItem>(deselectedFmts, FmtItemDescComparator.globalInstance));
            listAvail.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listAvail.setCellRenderer(new FmtItemRenderer());
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
    
    private JPanel getSelectedPane() {
        if (selectedPane == null) {
            selectedPane = new JPanel(new BorderLayout());
            selectedPane.add(new JLabel(_("Selected Columns:")), BorderLayout.NORTH);
            
            listSelected = new JList(new ListListModel<FmtItem>(selectedFmts));
            listSelected.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listSelected.setCellRenderer(new FmtItemRenderer());
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
        ImageIcon ico = utils.loadIcon("navigation/" + name);
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
        ListListModel<FmtItem> mSel = (ListListModel<FmtItem>)listSelected.getModel();
        SortedListModel<FmtItem> mAvail = (SortedListModel<FmtItem>)listAvail.getModel();

        if (e.getActionCommand().equals("Forward")) { //>>
            int[] selIndexes = listAvail.getSelectedIndices();
            if (selIndexes.length == 0) {
                return;
            }
            for (int i : selIndexes) {
                mSel.add(mAvail.get(i));
            }
            mAvail.removeAll(selIndexes);
        } else {
            int[] selIndexes = listSelected.getSelectedIndices();
            if (selIndexes.length == 0) {
                return;
            }
            if (e.getActionCommand().equals("Back")) { //<<
                Object[] sel = listSelected.getSelectedValues();
                if (canDelete(sel)) {
                    for (Object o : sel) {
                        mAvail.add((FmtItem)o);
                    }
                    mSel.removeAll(selIndexes);
                }
            } else if (e.getActionCommand().equals("Up")) {
                mSel.moveUp(selIndexes);
                for (int i = 0; i < selIndexes.length; i++) {
                    selIndexes[i]--;
                }
                listSelected.setSelectedIndices(selIndexes);
            } else if (e.getActionCommand().equals("Down")) { 
                mSel.moveDown(selIndexes);
                for (int i = 0; i < selIndexes.length; i++) {
                    selIndexes[i]++;
                }
                listSelected.setSelectedIndices(selIndexes);
            }     
        }
    }
    
    public fmtEditor(FmtItem[] avail, List<FmtItem> selected, List<FmtItem> dontDelete) {
        super();
        selectedFmts = selected;
        deselectedFmts = new ArrayList<FmtItem>(Arrays.asList(avail));
        deselectedFmts.removeAll(selectedFmts);
        dontDeleteFmts = dontDelete;
        
        initialize();
    }
    
    static class FmtItemDescComparator implements Comparator<FmtItem> {
        public int compare(FmtItem o1, FmtItem o2) {
            return o1.desc.compareTo(o2.desc);
        }        
        
        public static final FmtItemDescComparator globalInstance = new FmtItemDescComparator();
    }
}
