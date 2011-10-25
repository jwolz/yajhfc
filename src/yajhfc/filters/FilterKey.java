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
package yajhfc.filters;

import java.text.Format;

/**
 * @author jonas
 *
 */
public interface FilterKey {
    /**
     * Returns the data type of columns using this filter key
     * @return
     */
    public Class<?> getDataType();
    
    /**
     * Returns a java.text.Format instance that is used to format this column's data 
     * when the filter needs String data (to make sure a StringFilter filters on
     * the same data a user would see).
     * If the return value is null, each data value's toString() method is used instead.
     * @return
     */
    public Format getFormat();
    
    /**
     * A unique name for this filter key which can be used for serializing it to a String
     * @return
     */
    public String name();
}
