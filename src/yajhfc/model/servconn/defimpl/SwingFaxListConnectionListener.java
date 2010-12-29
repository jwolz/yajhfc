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
package yajhfc.model.servconn.defimpl;

import javax.swing.SwingUtilities;

import yajhfc.model.servconn.ConnectionState;
import yajhfc.model.servconn.FaxListConnectionListener;

/**
 * FaxListConnectionListener that makes sure the event processing is done
 * in the event dispatching thread.
 * 
 * @author jonas
 *
 */
public abstract class SwingFaxListConnectionListener implements
        FaxListConnectionListener {
    public boolean enableConnectionStateChange;
    public boolean enableServerStatusChange;
    public boolean enableRefreshComplete;
    
    
    protected void connectionStateChangeSwing(ConnectionState oldState, ConnectionState newState) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see yajhfc.model.servconn.FaxListConnectionListener#connectionStateChange(yajhfc.model.servconn.ConnectionState, yajhfc.model.servconn.ConnectionState)
     */
    public final void connectionStateChange(final ConnectionState oldState,
            final ConnectionState newState) {
        if (!enableConnectionStateChange)
            return;
        
        if (SwingUtilities.isEventDispatchThread()) {
            connectionStateChangeSwing(oldState, newState);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   connectionStateChangeSwing(oldState, newState);
                } 
            });
        }
    }
    
    protected void serverStatusChangedSwing(String statusText) {
        // Do nothing
    }
    
    /* (non-Javadoc)
     * @see yajhfc.model.servconn.FaxListConnectionListener#serverStatusChanged(java.lang.String)
     */
    public final void serverStatusChanged(final String statusText) {
        if (!enableServerStatusChange)
            return;
        
        if (SwingUtilities.isEventDispatchThread()) {
            serverStatusChangedSwing(statusText);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   serverStatusChangedSwing(statusText);
                } 
            });
        }
    }
    
    public final void refreshComplete(final RefreshKind refreshKind, final boolean success) {
        if (!enableRefreshComplete)
            return;
        
        if (SwingUtilities.isEventDispatchThread()) {
            refreshCompleteSwing(refreshKind, success);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   refreshCompleteSwing(refreshKind, success);
                } 
            });
        }
    }
    
    protected void refreshCompleteSwing(RefreshKind refreshKind, boolean success) {
        // NOP
    }

    public SwingFaxListConnectionListener() {
        this(true,true,true);
    }

    public SwingFaxListConnectionListener(boolean enableConnectionStateChange,
            boolean enableServerStatusChange, boolean enableRefreshComplete) {
        super();
        this.enableConnectionStateChange = enableConnectionStateChange;
        this.enableServerStatusChange = enableServerStatusChange;
        this.enableRefreshComplete = enableRefreshComplete;
    }


    
}
