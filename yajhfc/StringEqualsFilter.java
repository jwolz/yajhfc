package yajhfc;

import java.util.Vector;

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

/**
 * A filter that displays only jobs where the value in the specified column is
 * equal to the compareValue.
 */
public class StringEqualsFilter implements YajJobFilter {

    public String compareValue = null;
    public FmtItem column = null;
    
    protected int colIdx = -1;
    
    public StringEqualsFilter(FmtItem col, String compareValue) {
        super();
        this.column = col;
        this.compareValue = compareValue;
    }

    public boolean jobIsVisible(YajJob job) {
        if (column == null || compareValue == null || colIdx < 0)
            return true;
        return job.getStringData(colIdx).equals(compareValue);
    }

    public void initFilter(Vector<FmtItem> columns) {
        colIdx = columns.indexOf(column);
    }
}
