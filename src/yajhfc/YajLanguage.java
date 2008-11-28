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

public enum YajLanguage  {
    SYSTEM_DEFAULT(null),
    ENGLISH(Locale.ENGLISH),
    FRENCH(Locale.FRENCH),
    GERMAN(Locale.GERMAN),
    ITALIAN(Locale.ITALIAN),
    SPANISH(new Locale("es")),
    RUSSIAN(new Locale("ru")),
    TURKISH(new Locale("tr"))
    ;
    private final Locale locale;
    
    public String toString() {
        if (locale == null) {
            return Utils._("(System default)");
        } else {
                if (Utils.getLocale().getLanguage().equals(locale.getLanguage()))
                    return locale.getDisplayLanguage(Utils.getLocale());
                else 
                    return locale.getDisplayLanguage(locale) + " (" + locale.getDisplayLanguage(Utils.getLocale()) + ")";
            }
        }
    
    public Locale getLocale() {
        if (locale == null) {
            return Locale.getDefault();
        } else {
            return locale;
        }
    }
    
    public String getLangCode() {
        if (locale == null) {
            return "auto";
        } else {
            return locale.getLanguage();
        }
    }

    private YajLanguage(Locale locale) {
        this.locale = locale;
    }
    
    public static YajLanguage languageFromLangCode(String langCode) {
        for (YajLanguage lang : values()) {
            if (lang.getLangCode().equals(langCode)) {
                return lang;
            }
        }
        return SYSTEM_DEFAULT;
    }
}
