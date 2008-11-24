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


import java.util.Date;

import yajhfc.filters.FilterKey;

public class FmtItem extends MyManualMapObject implements FilterKey {
    public final String fmt;
    public final String longdesc;
    public final String desc;
    public final Class<?> dataClass;
    public final HylaDateField dateFormat;
    
    public String toString() {
        return "%" + fmt;
    }
     
    public FmtItem(String fmt, String desc) {
        this(fmt, desc, desc);
    }
    
    public FmtItem(String fmt, String desc, Class<?> dataClass) {
        this(fmt, desc, desc, dataClass, null);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, HylaDateField dateFormat) {
        this(fmt, desc, longdesc, Date.class, dateFormat);
    }
    
    
    public FmtItem(String fmt, String desc, String longdesc) {
        this(fmt, desc, longdesc, String.class, null);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, Class<?> dataClass) {
        this(fmt,desc,longdesc,dataClass,null);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, Class<?> dataClass, HylaDateField dateFormat) {
        this.fmt = fmt;
        this.desc = desc;
        this.longdesc = longdesc;
        this.dataClass = dataClass;
        this.dateFormat = dateFormat;
    }
    
    @Override
    public Object getKey() {
        return fmt;
    }

    public Class<?> getDataType() {
        return dataClass;
    }
}
