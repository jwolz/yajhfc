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
package yajhfc;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import yajhfc.filters.Filter;
import yajhfc.filters.ui.SearchWin;
import yajhfc.model.TooltipJTable;
import yajhfc.model.YajJob;

/**
 * @author jonas
 *
 */
public class MainWinSearchWin extends
        SearchWin<YajJob<FmtItem>, FmtItem> implements ChangeListener {

    private MainWin parent;
    private static final FmtItem[] nullArray = new FmtItem[0];
    
    
    /* (non-Javadoc)
     * @see yajhfc.filters.ui.SearchWin#getAvailableFields()
     */
    @Override
    protected FmtItem[] getAvailableFields() {
        if (parent == null) {
            return nullArray;
        } else {
            return parent.getSelectedTable().getRealModel().columns.getCompleteView().toArray(nullArray);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.filters.ui.SearchWin#performSearch(yajhfc.filters.Filter, boolean, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void performSearch(Filter<YajJob<FmtItem>, FmtItem> selectedFilter,
            boolean searchBackwards, boolean wrapAroundSearch) {
       
        TooltipJTable selTable = parent.getSelectedTable();
        TableModel model = selTable.getModel();
        
        int selectedIndex = selTable.getSelectedRow();
        if (searchBackwards && selectedIndex == -1) {
            selectedIndex = model.getRowCount();
        }
        selectedFilter.initFilter(selTable.getRealModel().columns);
        
        boolean didWrapAround = false;
        boolean found = false;
        while (true) {
            if (searchBackwards) {
                selectedIndex--;
                if (selectedIndex < 0) {
                    if (wrapAroundSearch && !didWrapAround) {
                        selectedIndex = model.getRowCount() - 1;
                        didWrapAround = true;
                    } else {
                        break;
                    }
                }
            } else {
                selectedIndex++;
                if (selectedIndex >=  model.getRowCount()) {
                    if (wrapAroundSearch && !didWrapAround) {
                        selectedIndex = 0;
                        didWrapAround = true;
                    } else {
                        break;
                    }
                }
            }
           
            YajJob job = selTable.getJobForRow(selectedIndex);
            if (selectedFilter.matchesFilter(job)) {
                found = true;
                break;
            }
        } 
        if (found) {
            selTable.setRowSelectionInterval(selectedIndex, selectedIndex);
            selTable.scrollRectToVisible(selTable.getCellRect(selectedIndex, 0, true));
        } else {
            JOptionPane.showMessageDialog(this, Utils._("No matching fax found."), Utils._("Search fax"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void stateChanged(ChangeEvent e) {
        refreshFieldList();
    }

    @Override
    public void dispose() {
        if (parent != null) {
            if (parent.tabMain != null) {
                parent.tabMain.removeChangeListener(this);
            }
        }
        
        super.dispose();
    }
    
    public MainWinSearchWin(MainWin parent) {
        super(parent,Utils._("Search fax"));
        
        this.parent = parent;
        
        parent.getTabMain().addChangeListener(this);
        
        comboFields.setRenderer(new FmtItemRenderer());
        refreshFieldList();
    }
}
