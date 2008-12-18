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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jonas
 *
 */
public class RegExDateFormat extends DateFormat {
    private static final Logger log = Logger.getLogger(RegExDateFormat.class.getName());
    
    protected Pattern pattern;
    protected int[] calendarFields;
    protected Calendar calendar = Calendar.getInstance(Locale.US);
    
    /**
     * Constructs a new RegExDateFormat.
     * @param
     * pattern The regular expression used to parse the Date. Every date field should be assigned a match group in the pattern
     * @param
     * calendarFields The mapping of regular expression match groups to calendar fields usable for Calendar.set(). The n-th match group maps to calendarFields[n-1]. 
     * Special handling for Calendar.MONTH (decrement value by 1) is provided.
     */
    public RegExDateFormat(String pattern, int... calendarFields) {
        super();
        this.pattern = Pattern.compile(pattern);
        this.calendarFields = calendarFields;
    }

    /* (non-Javadoc)
     * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo,
            FieldPosition fieldPosition) {
        throw new UnsupportedOperationException("A RegExDateFormat cannot format.");
    }

    /* (non-Javadoc)
     * @see java.text.DateFormat#parse(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Date parse(String source, ParsePosition pos) {
        if (source == null || source.length() == 0)
            return null;
        
        Matcher m = pattern.matcher(source);
        if (m.find(pos.getIndex())) {
            pos.setIndex(m.end());
            
            try {
                calendar.clear();
                for (int i = 1; i <= m.groupCount(); i++) {
                    int calendarField = calendarFields[i-1];
                    String sValue = m.group(i);
                    
                    if (sValue != null && sValue.length() > 0) {
                        int value = Integer.parseInt(sValue);
                        if (calendarField == Calendar.MONTH) {
                            value--; // Java months are zero based
                        } 
                        if (value >= 0) {
                            calendar.set(calendarField, value);
                        }
                    }
                }
                return calendar.getTime();
            } catch (Exception ex) {
                log.log(Level.INFO, "Error parsing the date \"" + source + "\"", ex);
                return null;
            }
        } else {
            return null;
        }
    }
}
