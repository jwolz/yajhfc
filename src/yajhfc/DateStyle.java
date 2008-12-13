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
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        this(format, format);
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
                new DateStyle("HH.mm.ss")
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
                    while (source.charAt(parseIdx) == ' ') {
                        parseIdx++;
                    }
                    pos.setIndex(parseIdx);
                } else {
                    return null;
                }
                Date time = tFormat.parse(source, pos);
                if (time != null) {
                    return new Date(date.getTime() + time.getTime());
                } else {
                    return null;
                }
            }
            
        };
    }
}
