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
import java.util.Vector;

import yajhfc.FmtItem;
import yajhfc.YajJob;
import yajhfc.YajJobFilter;


public class ComparableFilter implements YajJobFilter {

    protected Comparable compareValue;
    protected ComparableFilterOperator operator;
    protected FmtItem column = null;
    
    protected int colIdx = -1;
    
    public ComparableFilter(FmtItem col, ComparableFilterOperator op, Comparable compareValue) {
        super();
        this.column = col;
        this.operator = op;
        this.compareValue = compareValue;
    }

    public boolean jobIsVisible(YajJob job) {
        if (column == null || compareValue == null || operator == null || colIdx < 0)
            return true;
        int compResult = compareValue.compareTo(job.getData(colIdx));
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

    public void initFilter(Vector<FmtItem> columns) {
        colIdx = columns.indexOf(column);
    }

    public FmtItem getColumn() {
        return column;
    }
    
    public Object getCompareValue() {
        return compareValue;
    }
    
    public ComparableFilterOperator getOperator() {
        return operator;
    }
    
    public boolean validate(Vector<FmtItem> columns) {
        return columns.contains(column);
    }
}
