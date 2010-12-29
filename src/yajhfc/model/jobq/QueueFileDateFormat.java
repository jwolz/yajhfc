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
package yajhfc.model.jobq;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;

import yajhfc.FaxTimezone;
import yajhfc.Utils;

public class QueueFileDateFormat extends DateFormat {
    
    protected final boolean applyTimezoneCorrection;
    
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo,
            FieldPosition fieldPosition) {
        long millisecs = date.getTime();
        if (applyTimezoneCorrection && Utils.getFaxOptions().tzone == FaxTimezone.LOCAL) {
            millisecs -= TimeZone.getDefault().getOffset(millisecs);
        }
        
        fieldPosition.setBeginIndex(toAppendTo.length());
        toAppendTo.append(millisecs/1000);
        fieldPosition.setEndIndex(toAppendTo.length());
        return toAppendTo;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        long millisecs = Long.parseLong(source.substring(pos.getIndex())) * 1000;
        if (applyTimezoneCorrection && Utils.getFaxOptions().tzone == FaxTimezone.LOCAL) {
            millisecs += TimeZone.getDefault().getOffset(millisecs);
        }
        pos.setIndex(source.length());
        return new Date(millisecs);
    }

    public QueueFileDateFormat(boolean applyTimezoneCorrection) {
        super();
        this.applyTimezoneCorrection = applyTimezoneCorrection;
    }
    
}