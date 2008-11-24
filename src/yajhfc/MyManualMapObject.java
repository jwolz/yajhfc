package yajhfc;
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

public abstract class MyManualMapObject {
    public abstract Object getKey();
    
    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MyManualMapObject)
            return getKey().equals(((MyManualMapObject)obj).getKey());
        else if (obj.getClass().isInstance(getKey()))
            return getKey().equals(obj);
        else if (obj instanceof String) 
            return getKey().equals(stringToKey((String)obj));
        else
            return false;
    }
    
    /**
     * Returns strKey converted into the data format of strKey. 
     * Must be overridden if the Object returned by getKey() is not a String!
     * 
     * @param strKey
     * @return
     */
    public Object stringToKey(String strKey) {
        return strKey;
    }
}
