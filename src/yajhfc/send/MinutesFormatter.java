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
package yajhfc.send;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JFormattedTextField.AbstractFormatter;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class MinutesFormatter extends AbstractFormatter {

    protected NumberFormat format;
    protected String suffix;
    protected double multiplicator;
    protected long baseDate;
    
    /* (non-Javadoc)
     * @see javax.swing.JFormattedTextField.AbstractFormatter#stringToValue(java.lang.String)
     */
    @Override
    public Object stringToValue(String text) throws ParseException {
        return new Date(baseDate + (long)(format.parse(text).doubleValue() * multiplicator));
    }

    /* (non-Javadoc)
     * @see javax.swing.JFormattedTextField.AbstractFormatter#valueToString(java.lang.Object)
     */
    @Override
    public String valueToString(Object value) throws ParseException {
        StringBuffer res = new StringBuffer();
        format.format((((Date)value).getTime() - baseDate) / multiplicator, res, new FieldPosition(0));
        res.append(suffix);
        return res.toString();
    }
    
    public MinutesFormatter(Date baseDate) {
        this(baseDate.getTime(), new DecimalFormat("####0.##"), 60000, " " + Utils._("minutes"));
    }

    public MinutesFormatter(long baseDate, NumberFormat format,
            double multiplicator, String suffix) {
        super();
        this.baseDate = baseDate;
        this.format = format;
        this.multiplicator = multiplicator;
        this.suffix = suffix;
    }





    
}
