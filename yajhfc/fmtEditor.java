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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
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
    
    //private FmtItem[] availFmts;
    private Vector<FmtItem> selectedFmts;
    private Vector<FmtItem> deselectedFmts;
    private List<FmtItem> dontDeleteFmts;
    
    private JPanel buttonPane, availPane, selectedPane;
    private JButton buttonAdd, buttonDelete, buttonUp, buttonDown;
    private JList listAvail, listSelected;
    private JScrollPane scrollAvail, scrollSelected;
    
    private String _(String key) {
        return utils._(key);
    }
    
    private boolean canDelete(FmtItem item) {
        if (selectedFmts.size() <= 1) // Ensure at least one item is present
            return false;
        
        if (dontDeleteFmts == null)
            return true;
        else
            return !dontDeleteFmts.contains(item);
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
            
            listAvail = new JList(new VectorListModel(deselectedFmts));
            listAvail.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
            
            listSelected = new JList(new VectorListModel(selectedFmts));
            listSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listSelected.setCellRenderer(new FmtItemRenderer());
            listSelected.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selidx = listSelected.getSelectedIndex();
                        buttonUp.setEnabled(selidx > 0);
                        buttonDown.setEnabled((selidx >=0) && (selidx < (listSelected.getModel().getSize() - 1)));
                        buttonDelete.setEnabled((selidx >= 0) && canDelete((FmtItem)listSelected.getSelectedValue()));
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
    
    
    public void actionPerformed(ActionEvent e) {
        VectorListModel mSel = (VectorListModel)listSelected.getModel();
        VectorListModel mAvail = (VectorListModel)listAvail.getModel();
        int selidx = listSelected.getSelectedIndex();
        
        if (e.getActionCommand().equals("Forward")) { //>>
            Object sel = listAvail.getSelectedValue();
            if (sel != null) {
                mSel.addElement(sel);
                mAvail.removeElement(sel);
            }
        } else if (e.getActionCommand().equals("Back")) { //<<
            Object sel = listSelected.getSelectedValue();
            if ((sel != null) && canDelete((FmtItem)sel)) {
                int insidx = Collections.binarySearch(deselectedFmts, (FmtItem)sel, new FmtItemDescComparator());
                assert (insidx < 0);
                insidx = -(insidx + 1);
                mAvail.addElement(insidx, sel);
                mSel.removeElement(sel);
            }
        } else if (e.getActionCommand().equals("Up")) {
            if (selidx > 0) {
                mSel.moveElement(selidx, selidx - 1);
                listSelected.setSelectedIndex(selidx - 1);
            }
        } else if (e.getActionCommand().equals("Down")) { 
            if (selidx < mSel.getSize() - 1) {
                mSel.moveElement(selidx, selidx + 1);
                listSelected.setSelectedIndex(selidx + 1);
            }
        }        
    }
    
    public fmtEditor(FmtItem[] avail, Vector<FmtItem> selected, List<FmtItem> dontDelete) {
        super();
        //availFmts = avail;
        selectedFmts = selected;
        deselectedFmts = new Vector<FmtItem>(Arrays.asList(avail));
        deselectedFmts.removeAll(selectedFmts);
        Collections.sort(deselectedFmts, new FmtItemDescComparator());
        dontDeleteFmts = dontDelete;
        
        initialize();
    }
    
    class FmtItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, ((FmtItem)value).desc, index, isSelected,
                    cellHasFocus);
        }
    }
    
    class VectorListModel extends AbstractListModel {
        private Vector data;
        
        public Object getElementAt(int index) {
            return data.elementAt(index);
        }
        
        public int getSize() {
            return data.size();
        }
        
        public void removeElement(Object obj) {
            this.remove(data.indexOf(obj));
        }
        
        public void remove(int index) {
            data.remove(index);
            fireIntervalRemoved(this, index, index);
        }
        
        public void addElement(int index, Object obj) {
            data.add(index, obj);
            fireIntervalAdded(this, index, index);
        }
        
        public void addElement(Object obj) {
            data.add(obj);
            fireIntervalAdded(this, data.size() - 1, data.size() - 1);
        }
        
        // Moves Element from oldindex to newindex
        public void moveElement(int oldindex, int newindex) {
            Object obj = data.get(oldindex);
            data.remove(oldindex);
            data.add(newindex, obj);
            fireContentsChanged(this, oldindex, newindex);
        }
        
        public VectorListModel(Vector data) {
            super();
            this.data = data;
        }
    }
    
    class FmtItemDescComparator implements Comparator<FmtItem> {
        public int compare(FmtItem o1, FmtItem o2) {
            return o1.desc.compareTo(o2.desc);
        }        
    }
}
