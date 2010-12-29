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
package yajhfc.model.servconn;

/**
 * @author jonas
 *
 */
public interface FaxListConnectionListener {
    public enum RefreshKind {
        STATUS,
        FAXLISTS,
        FAXLISTS_FROM_CACHE
    }
    
    /**
     * Called when the connection state changes
     */
    public void connectionStateChange(ConnectionState oldState, ConnectionState newState);
    
    /**
     * Called when the status of the server has changed.
     */
    public void serverStatusChanged(String statusText);
    
    /**
     * Called when all tables or the status has been refreshed (regardless if any changes have been detected)
     * @param refreshKind what has been refreshed?
     * @param success true if the refresh was successful, false otherwise
     */
    public void refreshComplete(RefreshKind refreshKind, boolean success);
}
