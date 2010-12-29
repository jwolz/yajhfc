/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.readstate;

import java.util.Set;

/**
 * Interface for listeners that are notified when the read state
 * of faxes changed.
 * 
 * @author jonas
 *
 */
public interface ReadStateChangedListener {
    /**
     * Notifies the receiver that the read state of the faxes identified by the ID
     * values in the given Collection has changed. <br>
     * NOTE: This might be called from outside the event dispatching thread!
     * @param changedFaxes
     */
    public void readStateChanged(PersistentReadState sender, Set<String> changedFaxes);
}
