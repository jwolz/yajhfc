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
package yajhfc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class AsyncComboBoxOrListModel<T> extends AbstractListModel
    implements ListModel, ComboBoxModel {

    private Object selectedItem;
    protected final List<T> elements;
    protected Future<List<T>> future;
    protected boolean finished = false;
    private Callable<List<T>> listCallable;
    
    /**
     * Creates a new AsyncComboBoxOrListModel
     * @param initialItems the items initially in the list.
     * @param callable the Callable asynchronously returning the "final" list items 
     * @param replace true to replace the elements with the "final" list items, false to add them.
     * @param runWhenFinished a Runnable to run when the list has the final elements or null
     */
    public AsyncComboBoxOrListModel(List<T> initialItems, final Callable<List<T>> callable, final boolean replace, final Runnable runWhenFinished) {
        super();
        this.elements = new ArrayList<T>(initialItems);
        
        listCallable = new Callable<List<T>>() {
            public List<T> call() throws Exception {
                final List<T> newElements = callable.call();
                final int oldSize = elements.size();
                
                if (replace) {
                    elements.clear();
                }
                elements.addAll(newElements);
                
                SwingUtilities.invokeLater(new Runnable() {
                    @SuppressWarnings("synthetic-access")
                    public void run() {
                        if (replace) {
                            fireIntervalRemoved(AsyncComboBoxOrListModel.this, 0, oldSize-1);
                            fireIntervalAdded(AsyncComboBoxOrListModel.this, 0, elements.size()-1);
                        } else {
                            fireIntervalAdded(AsyncComboBoxOrListModel.this, oldSize, elements.size()-1);
                        }
                        
                        if (runWhenFinished != null) {
                            runWhenFinished.run();
                        }
                        finished = true;
                    } 
                });
                return newElements;
            }
        };
        refreshListAsync();
    }

    /**
     * Refreshes the list asynchronously
     */
    public void refreshListAsync() {
        future = Utils.executorService.submit(listCallable);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return elements.get(index);
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return elements.size();
    }

    public Object getSelectedItem() {
        return selectedItem;
    }
    
    public boolean hasFinished() {
        return finished;
    }

    public void setSelectedItem(Object selectedItem) {
        if (this.selectedItem != selectedItem) {
            this.selectedItem = selectedItem;
            fireContentsChanged(this, -1, -1);
        }
    }
    
    public Future<List<T>> getFuture() {
        return future;
    }
}
