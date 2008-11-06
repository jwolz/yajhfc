package yajhfc.filters;


/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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

public class StringFilter<V extends FilterableObject, K extends FilterKey> extends AbstractStringFilter<V, K> implements Filter<V, K>  {
    protected K column = null;
    
    protected Object colIdx = null;
    
    public StringFilter(K col, StringFilterOperator operator, String compareValue, boolean caseSensitive) {
        super(operator, compareValue, caseSensitive);
        this.column = col;
    }

    public boolean matchesFilter(V filterObj) {
        if (column == null || colIdx == null)
            return false;
        Object v = filterObj.getFilterData(colIdx);
        String value;
        if (v == null) {
            value = "";
        } else {
            value = v.toString();
        }
        return doActualMatch(value);
    }

    public void initFilter(FilterKeyList<K> columns) {
        colIdx = columns.translateKey(column);
    }
    
    public K getColumn() {
        return column;
    }
    
    public boolean validate(FilterKeyList<K> columns) {
        return columns.containsKey(column);
    }
}
