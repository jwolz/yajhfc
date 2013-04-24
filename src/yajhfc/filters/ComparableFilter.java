package yajhfc.filters;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */


public class ComparableFilter<V extends FilterableObject, K extends FilterKey> implements Filter<V,K> {

    @SuppressWarnings("rawtypes")
    protected Comparable compareValue;
    protected ComparableFilterOperator operator;
    protected K column = null;
    
    protected Object colIdx = null;
    
    public ComparableFilter(K col, ComparableFilterOperator op, @SuppressWarnings("rawtypes") Comparable compareValue) {
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
