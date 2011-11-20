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
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
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
    
    public DateFormat getFormat() {
        return getInstanceFromKind(this);
    }
    
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
                throw new UnsupportedOperationException("Unknown kind!");
            }
            lastInstances.put(kind, format);
        }
        return format;
    }
}
