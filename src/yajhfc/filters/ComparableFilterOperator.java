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
import yajhfc.Utils;


public enum ComparableFilterOperator {
    EQUAL(Utils._("equals"), "="),
    NOTEQUAL(Utils._("not equals"), "≠"),
    LESS(Utils._("less than"), "<"),
    GREATER(Utils._("greater than"), ">"),
    LESSEQUAL(Utils._("less or equal"), "≤"),
    GREATEREQUAL(Utils._("greater or equal"), "≥");
    
    private final String description;
    private final String shortSymbol;
    
    ComparableFilterOperator(String description, String shortSymbol) {
        this.description = description;
        this.shortSymbol = shortSymbol;
    }
    
    public String description() {
        return description;
    }
    
    public String getShortSymbol() {
        return shortSymbol;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
