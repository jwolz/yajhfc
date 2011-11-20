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
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author jonas
 *
 */
public class DateStyle {

    private final String displayName;
    private final String saveString;
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public String getSaveString() {
        return saveString;
    }
    
    public DateStyle(String displayName, String saveString) {
        super();
        this.displayName = displayName;
        this.saveString = saveString;
    }

    public DateStyle(String format) {
        super();
        this.saveString = format;
        this.displayName = format; //new SimpleDateFormat(format, Utils.getLocale()).toLocalizedPattern();
    }


    public static final String FROM_LOCALE = "$LOCALE$";
    public static final String FROM_SYSTEM = "$SYSTEM$";
    
    private static DateStyle[] availableDateStyles;
    private static DateStyle[] availableTimeStyles;
    
    static {
        DateStyle localeStyle = new DateStyle(Utils._("(Language default)"), FROM_LOCALE);
        DateStyle systemStyle = new DateStyle(Utils._("(System default)"), FROM_SYSTEM);
        availableDateStyles = new DateStyle[] {
                localeStyle,
                systemStyle,
                new DateStyle("dd/MM/yyyy"),
                new DateStyle("MM/dd/yyyy"),
                new DateStyle("dd.MM.yyyy"),
                new DateStyle("yyyy-MM-dd")
        };
        availableTimeStyles = new DateStyle[] {
                localeStyle,
                systemStyle,
                new DateStyle("HH:mm:ss"),
                new DateStyle("HH.mm.ss"),
                new DateStyle("hh:mm:ss aa")
        };
    }
    
    public static DateStyle[] getAvailableDateStyles() {
        return availableDateStyles;
    }
    
    public static DateStyle[] getAvailableTimeStyles() {
        return availableTimeStyles;
    }
    
    public static DateFormat getDateFormatFromString(String dateFormat) {
        if (dateFormat.equals(FROM_LOCALE)) {
            return new SimpleDateFormat(Utils._("dd/MM/yyyy"));
        } else if (dateFormat.equals(FROM_SYSTEM)) {
            return DateFormat.getDateInstance(DateFormat.SHORT);
        } else {
            return new SimpleDateFormat(dateFormat);
        }
    }
    
    public static DateFormat getTimeFormatFromString(String timeFormat) {
        if (timeFormat.equals(FROM_LOCALE)) {
            return new SimpleDateFormat(Utils._("HH:mm:ss"));
        } else if (timeFormat.equals(FROM_SYSTEM)) {
            return DateFormat.getTimeInstance(DateFormat.MEDIUM);
        } else {
            return new SimpleDateFormat(timeFormat);
        }
    }

    public static DateFormat getDateTimeFormatFromString(String dateFormat, String timeFormat) {
        final DateFormat dFormat = getDateFormatFromString(dateFormat);
        final DateFormat tFormat = getTimeFormatFromString(timeFormat);
        if (dFormat instanceof SimpleDateFormat && tFormat instanceof SimpleDateFormat) {
            return new SimpleDateFormat(((SimpleDateFormat)dFormat).toPattern() + ' ' + ((SimpleDateFormat)tFormat).toPattern());
        } else {
            return new DateFormat() {
                @Override
                public StringBuffer format(Date date, StringBuffer toAppendTo,
                        FieldPosition fieldPosition) {
                    dFormat.format(date, toAppendTo, fieldPosition);
                    toAppendTo.append(' ');
                    return tFormat.format(date, toAppendTo, fieldPosition);
                }

                @Override
                public Date parse(String source, ParsePosition pos) {
                    Date date = dFormat.parse(source, pos);
                    if (date != null) {
                        int parseIdx = pos.getIndex();
                        while (parseIdx < source.length() && source.charAt(parseIdx) == ' ') {
                            parseIdx++;
                        }
                        pos.setIndex(parseIdx);

                        Date time = tFormat.parse(source, pos);
                        long timeMillis;
                        if (time != null) {
                            timeMillis = time.getTime();
                            timeMillis += TimeZone.getDefault().getOffset(timeMillis);
                        } else {
                            timeMillis = 0;
                        }
                        return new Date(date.getTime() + timeMillis);
                    } else {
                        return null;
                    }
                }     
            };
        }
    }
}
