/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

import java.util.Collection;
import java.util.Set;

/**
 * This class supports loading and saving the (un)read state of faxes
 * @author jonas
 *
 */
public abstract class PersistentReadState {
    
    /**
     * Saves the list of faxes in the given set as read
     * @param readFaxes
     */
    public abstract void persistReadState(Collection<String> readFaxes);
    
    /**
     * Returns a set of faxes that should be considered read.
     * @return
     */
    public abstract Set<String> loadReadFaxes();
    
    /**
     * Contains a default instance of the class
     */
    public static final PersistentReadState DEFAULT = new LocalPersistentReadState(utils.getConfigDir() + "recvread");
    /**
     * Contains the currently used PersistentReadState class
     */
    public static PersistentReadState CURRENT = DEFAULT;
}
