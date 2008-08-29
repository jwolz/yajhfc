package yajhfc.phonebook;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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


public class GeneralConnectionSettings extends AbstractConnectionSettings {    
    public String name = "";
    public String givenName = "";
    public String title = "";
    public String location = "";
    public String company = "";
    public String faxNumber = "";
    public String voiceNumber = "";
    public String comment = "";
     
    public static final int SURNAME_FIELD = 0;
    public static final int GIVENNAME_FIELD = 1;
    public static final int LOCATION_FIELD = 2;
    public static final int TITLE_FIELD = 3;
    public static final int COMPANY_FIELD = 4;
    public static final int VOICENUMBER_FIELD = 5;
    public static final int FAXNUMBER_FIELD = 6;
    public static final int COMMENT_FIELD = 7;
    
    public static final int FIELD_COUNT = 8;
    
    public String getMappingFor(int fieldIdx) {
        switch (fieldIdx) {
        case SURNAME_FIELD:
            return name;
        case GIVENNAME_FIELD:
            return givenName;
        case LOCATION_FIELD:
            return location;
        case TITLE_FIELD:
            return title;
        case COMPANY_FIELD:
            return company;
        case VOICENUMBER_FIELD:
            return voiceNumber;
        case FAXNUMBER_FIELD:
            return faxNumber;
        case COMMENT_FIELD:
            return comment;
        default:
            throw new IllegalArgumentException("Unknown field " + fieldIdx);
        }
    }

}
