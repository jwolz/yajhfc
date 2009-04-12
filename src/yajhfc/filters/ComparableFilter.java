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


public class ComparableFilter<V extends FilterableObject, K extends FilterKey> implements Filter<V,K> {

    @SuppressWarnings("unchecked")
    protected Comparable compareValue;
    protected ComparableFilterOperator operator;
    protected K column = null;
    
    protected Object colIdx = null;
    
    @SuppressWarnings("unchecked")
    public ComparableFilter(K col, ComparableFilterOperator op, Comparable compareValue) {
        super();
        this.column = col;
        this.operator = op;
        this.compareValue = compareValue;
    }

    @SuppressWarnings("unchecked")
    public boolean matchesFilter(V filterObj) {
        if (column == null || compareValue == null || operator == null || colIdx == null)
            return true;
        int compResult = compareValue.compareTo(filterObj.getFilterData(colIdx));
        switch (operator) {
        case EQUAL:
            return (compResult == 0);
        case NOTEQUAL:
            return (compResult != 0);
        case LESS:
            return (compResult > 0); // > 0 instead of < 0 because LESS means column value less than compareValue
        case LESSEQUAL:
            return (compResult >= 0);
        case GREATER:
            return (compResult < 0);
        case GREATEREQUAL:
            return (compResult <= 0);
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
    
    public ComparableFilterOperator getOperator() {
        return operator;
    }
    
    public boolean validate(FilterKeyList<K> columns) {
        return columns.containsKey(column);
    }
    
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        this.toString(res);
        return res.toString();
    }
    
    public void toString(StringBuilder appendTo) {
        appendTo.append(column).append(' ').append(operator.getShortSymbol()).append(' ').append(compareValue);
    }
}
