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
package yajhfc.options;

import static yajhfc.Utils._;
import static yajhfc.options.OptionsWin.border;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.Utils;
import yajhfc.options.OptionsPageWrapper.Callback;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ListListModel;

/**
 * @author jonas
 *
 */
public abstract class MultiEditPanel<T> extends JPanel implements Callback<T> {
    PanelTreeNode settingsNode;
    JList list;
    Action actAdd, actRemove, actEdit, actDuplicate, actUp, actDown, actSort;
    
    ListListModel<T> itemsListModel;
    
    public static final Comparator<Object> LEXICAL_CASE_INSENSITIVE_COMPARATOR = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
        
    };
    
    public MultiEditPanel() {
        this(new ListListModel<T>(new ArrayList<T>()));
    }

    public MultiEditPanel(ListListModel<T> servers) {
        this.itemsListModel = servers;
        initialize();
    }
    
    public ListListModel<T> getItemsListModel() {
        return itemsListModel;
    }
    
    public PanelTreeNode getSettingsNode() {
        return settingsNode;
    }
    
    public void setSettingsNode(PanelTreeNode settingsNode) {
        this.settingsNode = settingsNode;
        settingsNode.initializeChildren();
    }
    
    protected abstract String getDeletePrompt(T selectedItem);
    
    protected abstract String getDeletePrompt(int[] selectedItems);
    
    protected abstract T createNewItem();
    
    protected abstract T duplicateItem(T toDuplicate);
    
    protected abstract PanelTreeNode createChildNode(T forItem);
    
    protected abstract void updateChildNode(PanelTreeNode node, T forItem);
    
    private void initialize() {
        double[][] tablelay = {
                {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border,TableLayout.PREFERRED, border,TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border }
        };
        setLayout(new TableLayout(tablelay));
        
        list = new JList(itemsListModel);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    checkEnable();
                }
            }
        });
        
        actAdd = new ExcDialogAbstractAction(_("Add"), Utils.loadIcon("general/Add")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                itemsListModel.add(createNewItem());
                list.setSelectedIndex(itemsListModel.getSize() - 1);
            }
        };
        
        actDuplicate = new ExcDialogAbstractAction(_("Duplicate"), Utils.loadIcon("general/Copy")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                T selected = getSelectedItem();
                if (selected != null) {
                    itemsListModel.add(duplicateItem(selected));
                    list.setSelectedIndex(itemsListModel.getSize() - 1);
                }
            }
        };
        
        actRemove = new ExcDialogAbstractAction(_("Remove"), Utils.loadIcon("general/Delete")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int[] selIndices = list.getSelectedIndices();
                if (selIndices.length==0 || itemsListModel.getSize() <= 1)
                    return;
                
                String prompt;
                if (selIndices.length==1) {
                    prompt = getDeletePrompt(itemsListModel.getList().get(selIndices[0]));
                } else {
                    prompt = getDeletePrompt(selIndices);
                }
                if (JOptionPane.showConfirmDialog(MultiEditPanel.this, prompt, _("Remove"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    itemsListModel.removeAll(selIndices);
                }
            }
        };
        
        actEdit = new ExcDialogAbstractAction(_("Edit") + "...", Utils.loadIcon("general/Edit")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int selIdx = list.getSelectedIndex();
                OptionsWin ow = (OptionsWin)SwingUtilities.getWindowAncestor(MultiEditPanel.this);
                if (selIdx < 0 || ow == null)
                    return;
                
                ow.selectNode(settingsNode.getChildren().get(selIdx));
            }
        };
        
        actDown = new ExcDialogAbstractAction(_("Down"), Utils.loadIcon("navigation/Down")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int selIdx = list.getSelectedIndex();
                if (selIdx < 0 || selIdx > itemsListModel.getSize()-2)
                    return;
                
                itemsListModel.moveDown(new int[] { selIdx });
            }
        };
        
        actUp = new ExcDialogAbstractAction(_("Up"), Utils.loadIcon("navigation/Up")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int selIdx = list.getSelectedIndex();
                if (selIdx < 1)
                    return;
                
                itemsListModel.moveUp(new int[] { selIdx });
            }
        };
        
        actSort = new ExcDialogAbstractAction(_("Sort"), Utils.loadIcon("text/AlignJustify")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                itemsListModel.sort(LEXICAL_CASE_INSENSITIVE_COMPARATOR);
            }
        };
        
        itemsListModel.addListDataListener(new ListDataListener() {
            
            public void intervalRemoved(ListDataEvent e) {
                List<PanelTreeNode> range = settingsNode.getChildren().subList(e.getIndex0(), e.getIndex1()+1);
                int[] indices = new int[range.size()];
                for (int i=0; i < indices.length; i++) {
                    indices[i] = i + e.getIndex0();
                }
                Object[] childs = range.toArray();
                
                range.clear();
                
                settingsNode.getTreeModel().nodesWereRemoved(settingsNode, indices, childs);
            }
            
            public void intervalAdded(ListDataEvent e) {
                final int itemCount = e.getIndex1()-e.getIndex0()+1;
                ArrayList<PanelTreeNode> childs = new ArrayList<PanelTreeNode>(itemCount);
                int[] indices = new int[itemCount];
              
                for (int i=0; i < itemCount; i++) {
                    int modelIndex = i + e.getIndex0();
                    
                    indices[i] = modelIndex;
                    
                    childs.add(createChildNode(itemsListModel.getList().get(modelIndex)));
                }

                settingsNode.getChildren().addAll(e.getIndex0(), childs);
               
                settingsNode.getTreeModel().nodesWereInserted(settingsNode, indices);
                
            }
            
            public void contentsChanged(ListDataEvent e) {
                final int itemCount = e.getIndex1()-e.getIndex0()+1;
                int[] indices = new int[itemCount];
                List<PanelTreeNode> children = settingsNode.getChildren();
                for (int i=0; i < itemCount; i++) {
                    int modelIndex = i + e.getIndex0();
                    
                    indices[i] = modelIndex;
                    updateChildNode(children.get(modelIndex), itemsListModel.get(modelIndex));
                }
                
                settingsNode.getTreeModel().nodesChanged(settingsNode, indices);
            }
        });
        
        this.add(new JScrollPane(list), "1,1,1,14,f,f");
        this.add(new JButton(actAdd), "3,1");
        this.add(new JButton(actDuplicate), "3,3");
        this.add(new JButton(actEdit), "3,5");
        this.add(new JButton(actUp), "3,7");
        this.add(new JButton(actDown), "3,9");
        this.add(new JButton(actSort), "3,11");
        this.add(new JButton(actRemove), "3,13");
        
        checkEnable();
    }
    
    protected void checkEnable() {
        int selIdx = list.getSelectedIndex();
        int size = itemsListModel.getSize();
        boolean haveSelection = (selIdx >= 0);
        boolean mayDelete = haveSelection && (size > 1);
        
        actDuplicate.setEnabled(haveSelection);
        actRemove.setEnabled(mayDelete);
        actEdit.setEnabled(haveSelection);
        actDown.setEnabled(haveSelection && (selIdx < size - 1));
        actUp.setEnabled(selIdx > 0);
    }

    public void elementSaved(OptionsPageWrapper<T> source) {
        itemsListModel.changeNotify(source.getOptions());
    }
    
    @SuppressWarnings("unchecked")
    public T getSelectedItem() {
        return (T)list.getSelectedValue();
    }
}
