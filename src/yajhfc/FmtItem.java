/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc;

import java.text.DateFormat;

import yajhfc.filters.FilterKey;

/**
 * @author jonas
 *
 */
public interface FmtItem extends FilterKey {
    /**
     * Returns the format used by HylaFAX
     * @return
     */
    public String getHylaFmt();
    /**
     * Returns the (short) description for display to the user
     * @return
     */
    public String getDescription();
    /**
     * Returns the long description for tool tips
     * @return
     */
    public String getLongDescription();
    
    /**
     * Returns the date format used to parse the value from the HylaFAX server.
     * Is null if getDataType() != Date.class
     * @return
     */
    public DateFormat getHylaDateFormat();
    /**
     * Returns the date format used to format the value displayed to the user.
     * Is null if getDataType() != Date.class
     * @return
     */
    public DateFormat getDisplayDateFormat();
    
}
