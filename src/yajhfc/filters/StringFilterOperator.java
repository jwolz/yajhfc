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
 */
import yajhfc.Utils;


public enum StringFilterOperator {
    EQUAL(Utils._("equals")),
    NOTEQUAL(Utils._("not equals")),
    CONTAINS(Utils._("contains")),
    CONTAINS_NOT(Utils._("contains not")),
    STARTSWITH(Utils._("starts with")),
    ENDSWITH(Utils._("ends with")),
    MATCHES(Utils._("matches"));
    
    private final String description;
    
    StringFilterOperator(String description) {
        this.description = description;
    }
    
    public String description() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}

