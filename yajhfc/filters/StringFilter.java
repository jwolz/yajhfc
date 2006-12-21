package yajhfc.filters;

import java.util.Vector;
import java.util.regex.Pattern;

import yajhfc.FmtItem;
import yajhfc.YajJob;
import yajhfc.YajJobFilter;

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

public class StringFilter implements YajJobFilter {

    protected Object compareValue = null;
    protected FmtItem column = null;
    protected StringFilterOperator operator;
    
    protected int colIdx = -1;
    
    public StringFilter(FmtItem col, StringFilterOperator operator, String compareValue) {
        super();
        this.column = col;
        this.operator = operator;
        if (operator == StringFilterOperator.MATCHES) {
            this.compareValue = Pattern.compile(compareValue);
        } else {
            this.compareValue = compareValue;
        }
    }

    public boolean jobIsVisible(YajJob job) {
        if (column == null || compareValue == null || operator == null || colIdx < 0)
            return true;
        String value = job.getStringData(colIdx);
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

    public void initFilter(Vector<FmtItem> columns) {
        colIdx = columns.indexOf(column);
    }
    
    public FmtItem getColumn() {
        return column;
    }
    
    public Object getCompareValue() {
        return compareValue;
    }
    
    public StringFilterOperator getOperator() {
        return operator;
    }
    
    public boolean validate(Vector<FmtItem> columns) {
        return columns.contains(column);
    }
}
