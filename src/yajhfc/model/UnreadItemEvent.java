package yajhfc.model;
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

import java.util.Collection;
import java.util.EventObject;

public class UnreadItemEvent extends EventObject {
    private Collection<RecvYajJob> items = null;
    private boolean oldDataNull;
    
    public Collection<RecvYajJob> getItems() {
        return items;
    }
    
    public boolean isOldDataNull() {
        return oldDataNull;
    }
    
    public UnReadMyTableModel getModel() {
        return (UnReadMyTableModel)source;
    }
    
    public UnreadItemEvent(UnReadMyTableModel source, Collection<RecvYajJob> items, boolean oldDataNull) {
        super(source);
        this.items = items;
        this.oldDataNull = oldDataNull;
    }
}