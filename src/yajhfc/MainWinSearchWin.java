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
package yajhfc;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import yajhfc.filters.Filter;
import yajhfc.filters.ui.SearchWin;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.ui.FmtItemRenderer;
import yajhfc.model.ui.TooltipJTable;

/**
 * @author jonas
 *
 */
public class MainWinSearchWin extends
        SearchWin<FaxJob<FmtItem>, FmtItem> implements ChangeListener {

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
            return parent.getSelectedTable().getRealModel().getColumns().getCompleteView().toArray(nullArray);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.filters.ui.SearchWin#performSearch(yajhfc.filters.Filter, boolean, boolean)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void performSearch(Filter<FaxJob<FmtItem>, FmtItem> selectedFilter,
            boolean searchBackwards, boolean wrapAroundSearch) {
       
        TooltipJTable selTable = parent.getSelectedTable();
        TableModel model = selTable.getModel();
        
        int selectedIndex = selTable.getSelectedRow();
        if (searchBackwards && selectedIndex == -1) {
            selectedIndex = model.getRowCount();
        }
        selectedFilter.initFilter(selTable.getRealModel().getColumns());
        
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
           
            FaxJob job = selTable.getJobForRow(selectedIndex);
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
