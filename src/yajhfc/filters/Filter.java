package yajhfc.filters;



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

public interface Filter<V extends FilterableObject,K extends FilterKey> {
    /**
     * Should return true if filterObj matches the filter, false otherwise. 
     * @param filterObj
     * @return
     */
    public boolean matchesFilter(V filterObj);
    
    /**
     * Initialize filter. It must be guaranteed that the columns or filter properties
     * are not changed during the subsequent matchesFilter() calls.
     * @param columns
     */
    public void initFilter(FilterKeyList<K> columns);
    
    /**
     * Validates this filter against a new set of columns.
     * Returns true if this filter still applies to them, false if it should be removed.
     * @param columns
     * @return
     */
    public boolean validate(FilterKeyList<K> columns);
    
    /**
     * Converts this Filter to a human readable String
     * @return
     */
    public String toString();
    
    /**
     * Appends a human readable String representation of this filter to the given StringBuilder
     * @return
     */
    public void toString(StringBuilder appendTo);
}
