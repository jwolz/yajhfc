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
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author jonas
 *
 */
public enum DateKind {
    TIME_ONLY,
    DATE_ONLY,
    DATE_AND_TIME,
    DURATION;
    
    private static final Map<DateKind,DateFormat> lastInstances = new EnumMap<DateKind,DateFormat>(DateKind.class);
    private static String lastDateStyle;
    private static String lastTimeStyle;
    
    public static DateFormat getInstanceFromKind(DateKind kind) {
        FaxOptions opts = Utils.getFaxOptions();
        DateFormat format;
        if ((lastDateStyle != opts.dateStyle && !opts.dateStyle.equals(lastDateStyle)) 
                || (lastTimeStyle != opts.timeStyle && !opts.timeStyle.equals(lastTimeStyle))) {
            lastInstances.clear();
            format = null;
            lastDateStyle = opts.dateStyle;
            lastTimeStyle = opts.timeStyle;
        } else {
            format = lastInstances.get(kind);
        }
        if (format == null) {
            switch (kind) {
            case DATE_ONLY:
                format = DateStyle.getDateFormatFromString(opts.dateStyle);
                break;
            case TIME_ONLY:
                format = DateStyle.getTimeFormatFromString(opts.timeStyle);
                break;
            case DATE_AND_TIME:
                format = DateStyle.getDateTimeFormatFromString(opts.dateStyle, opts.timeStyle);
                break;
            case DURATION:
                format = new SimpleDateFormat("HH:mm:ss");
                break;
            default:
                throw new UnsupportedOperationException("Unkown kind!");
            }
            lastInstances.put(kind, format);
        }
        return format;
    }
}
