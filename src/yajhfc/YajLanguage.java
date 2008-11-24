package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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

import java.util.Locale;

public class YajLanguage extends MyManualMapObject {
    
    protected Locale locale;
    
    public String toString() {
        if (Utils.getLocale().getLanguage().equals(locale.getLanguage()))
            return locale.getDisplayLanguage(Utils.getLocale());
        else 
            return locale.getDisplayLanguage(locale) + " (" + locale.getDisplayLanguage(Utils.getLocale()) + ")";
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    @Override
    public Object getKey() {
        return locale.getLanguage();
    }

    public YajLanguage(Locale locale) {
        this.locale = locale;
    }
}
