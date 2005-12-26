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

public class FmtItem extends MyManualMapObject {
    public String fmt;
    public String longdesc;
    public String desc;
    public Class<?> dataClass;
    public HylaDateField dateFormat = null;
    
    public String toString() {
        return "%" + fmt;
    }
     
    public FmtItem(String fmt, String desc) {
        this(fmt, desc, desc);
    }
    
    public FmtItem(String fmt, String desc, Class<?> dataClass) {
        this(fmt, desc, desc, dataClass);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, HylaDateField dateFormat) {
        this(fmt, desc, longdesc, Date.class);
        this.dateFormat = dateFormat;
    }
    
    
    public FmtItem(String fmt, String desc, String longdesc) {
        this(fmt, desc, longdesc, String.class);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, Class<?> dataClass) {
        this.fmt = fmt;
        this.desc = desc;
        this.longdesc = longdesc;
        this.dataClass = dataClass;
    }
    
    @Override
    public Object getKey() {
        return fmt;
    }
}
