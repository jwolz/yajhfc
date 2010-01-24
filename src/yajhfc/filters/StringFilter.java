package yajhfc.filters;

import java.text.FieldPosition;
import java.text.Format;


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
    
    private StringBuffer formatBuffer;
    private FieldPosition dummyFieldPos;
    
    public StringFilter(K col, StringFilterOperator operator, String compareValue, boolean caseSensitive) {
        super(operator, compareValue, caseSensitive);
        this.column = col;
    }

    // Cache the StringBuffer and the FieldPosition as matchesFilter is called often
    protected StringBuffer getFormatBuffer() {
        if (formatBuffer == null) {
            formatBuffer = new StringBuffer();
        }
        return formatBuffer;
    }
    protected FieldPosition getDummyFieldPos() {
        if (dummyFieldPos == null) {
            dummyFieldPos = new FieldPosition(0);
        }
        return dummyFieldPos;
    }
    
    public boolean matchesFilter(V filterObj) {
        if (column == null || colIdx == null)
            return false;
        Object v = filterObj.getFilterData(colIdx);
        String value;
        if (v == null) {
            value = "";
        } else {
            Format colFormat = column.getFormat();
            if (colFormat == null) {
                value = v.toString();
            } else {
                StringBuffer buf = getFormatBuffer();
                buf.setLength(0);
                value = colFormat.format(v, buf, getDummyFieldPos()).toString();
            }
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
    
    @Override
    protected void fieldToString(StringBuilder appendTo) {
        appendTo.append(column);
    }
}
