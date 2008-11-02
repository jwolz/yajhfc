package yajhfc.filters;

import java.util.regex.Pattern;

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

public class StringFilter<V extends FilterableObject, K extends FilterKey>  implements Filter<V, K>  {

    protected Object compareValue = null;
    protected K column = null;
    protected StringFilterOperator operator;
    protected boolean caseSensitive;
    
    protected Object colIdx = null;
    
    public StringFilter(K col, StringFilterOperator operator, String compareValue, boolean caseSensitive) {
        super();
        this.column = col;
        this.operator = operator;
        if (operator == StringFilterOperator.MATCHES) {
            this.compareValue = Pattern.compile(compareValue, caseSensitive ? 0 : (Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE));
        } else {
            this.compareValue = caseSensitive ? compareValue : compareValue.toLowerCase();
        }
        this.caseSensitive = caseSensitive;
    }

    public boolean matchesFilter(V filterObj) {
        if (column == null || compareValue == null || operator == null || colIdx == null)
            return true;
        Object v = filterObj.getFilterData(colIdx);
        String value;
        if (v == null) {
            value = "";
        } else {
            value = v.toString();
        }
        if (!caseSensitive && operator != StringFilterOperator.MATCHES) {
            value = value.toLowerCase();
        }
        switch (operator) {
        case EQUAL:
            return value.equals((String)compareValue);
        case NOTEQUAL:
            return !value.equals((String)compareValue);
        case CONTAINS:
            return value.contains((String)compareValue);
        case STARTSWITH:
            return value.startsWith((String)compareValue);
        case ENDSWITH:
            return value.endsWith((String)compareValue);
        case MATCHES:
            return ((Pattern)compareValue).matcher(value).matches();
        default:
            return true;
        }
    }

    public void initFilter(FilterKeyList<K> columns) {
        colIdx = columns.translateKey(column);
    }
    
    public K getColumn() {
        return column;
    }
    
    public Object getCompareValue() {
        return compareValue;
    }
    
    public StringFilterOperator getOperator() {
        return operator;
    }
    
    public boolean validate(FilterKeyList<K> columns) {
        return columns.containsKey(column);
    }
}
