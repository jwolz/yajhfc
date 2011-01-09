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
