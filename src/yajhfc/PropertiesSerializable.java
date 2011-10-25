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
package yajhfc;

import java.util.Properties;

/**
 * A class that can be "serialized" to a Properties object.
 * 
 * Classes implementing this interface (and "serializable" by AbstractFaxOptions) must have a constructor with one parameter representing the "parent" class
 * that can be used to create an instance of this class with the attributes set
 * to default values.
 * 
 * @author jonas
 *
 */
public interface PropertiesSerializable {
    
    /**
     * Store this class's attributes in the specified Properties list.
     * The specified prefix must be appended to this list's properties
     * @param p
     * @param prefix
     */
    public void storeToProperties(Properties p, String prefix);
    
    /**
     * Load this class's attributes from the specified Properties list.
     * The specified prefix must be appended to this list's properties in order to find them.
     * @param p
     * @param prefix
     */
    public void loadFromProperties(Properties p, String prefix);
}
