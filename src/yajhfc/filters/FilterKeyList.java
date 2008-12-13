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
package yajhfc.filters;

/**
 * @author jonas
 *
 */
public interface FilterKeyList<K extends FilterKey> {
    
    /**
     * Translates the key into an intermediate representation used by
     * {@link FilterableObject#getFilterData(Object)}. 
     * This can be used if an indexOf-Operation must be performed, for example.
     * @param key
     * @return
     */
    public Object translateKey(K key);
    
    /**
     * Returns an array of all available key values
     * @return
     */
    public K[] getAvailableKeys();
    
    /**
     * Checks if the given key value is (still) present in the list of keys
     * @param key
     * @return
     */
    public boolean containsKey(K key);
    
    /**
     * Returns the filter key matching the given name or null if no such key
     * can be found.
     * @param name
     * @return
     */
    public K getKeyForName(String name);
}
