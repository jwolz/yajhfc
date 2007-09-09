package yajhfc.phonebook;

import java.util.Comparator;

/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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


public class DefaultPhoneBookEntryComparator implements Comparator<PhoneBookEntry> {
    public static final DefaultPhoneBookEntryComparator globalInstance = new DefaultPhoneBookEntryComparator();
    
    public int compare(PhoneBookEntry o1, PhoneBookEntry o2) {
        int res;
        String s, s2;
        
        s = o1.getName();
        s2 = o2.getName();
        
        if (s == null) {
            if (s2 == null)
                res = 0;
            else
                res = Integer.MAX_VALUE;
        } else {
            if (s2 == null) 
                res = Integer.MIN_VALUE;
            else
                res = s.compareToIgnoreCase(s2);
        }
        
        if (res == 0) {
            s = o1.getGivenName();
            s2 = o2.getGivenName();
            
            if (s == null) {
                if (s2 == null)
                    return 0;
                else
                    return Integer.MAX_VALUE;
            } else {
                if (s2 == null) 
                    return Integer.MIN_VALUE;
                else
                    return s.compareToIgnoreCase(s2);
            }
        } else
            return res;
        //return o1.toString().compareToIgnoreCase(o2.toString());
    }
}
