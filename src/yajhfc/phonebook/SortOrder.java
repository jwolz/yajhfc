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
package yajhfc.phonebook;

import java.util.Comparator;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class SortOrder implements Comparator<PhoneBookEntry> {
    protected final PBEntryField[] fields;
    protected final boolean[] descending;

    public int getFieldCount() {
        return fields.length;
    }
    
    public PBEntryField getFields(int index) {
        return fields[index];
    }
    
    public boolean getDescending(int index) {
        return descending[index];
    }
    
    public int compare(PhoneBookEntry o1, PhoneBookEntry o2) {
        for (int i=0; i<fields.length; i++) {
            String val1 = o1.getField(fields[i]);
            String val2 = o2.getField(fields[i]);
            if (val1 == null) {
                if (val2 == null) {
                    continue;
                } else {
                    return -1;
                }
            } else { // val1 != null
                if (val2 == null) {
                    return 1;
                } else {
                    int cmp = val1.compareToIgnoreCase(val2);
                    if (cmp != 0) {
                        return (descending[i] ? -cmp : cmp);
                    }
                }
            }
        }
        return 0;
    }

    public String serialize() {
        StringBuilder res = new StringBuilder();
        for (int i=0; i<fields.length; i++) {
            res.append(descending[i] ? '-' : '+').append(fields[i].name()).append(',');
        }
        return res.toString();
    }
    
    public static SortOrder deserialize(String s) {
        String[] split = Utils.fastSplit(s, ',');
        PBEntryField[] fields = new PBEntryField[split.length];
        boolean[] descending = new boolean[split.length];
        
        for (int i=0; i<split.length; i++) {
            descending[i] = (split[i].charAt(0) == '-');
            fields[i] = Enum.valueOf(PBEntryField.class, split[i].substring(1));
        }
        
        return new SortOrder(fields, descending);
    }
    
    public SortOrder(PBEntryField[] fields, boolean[] descending) {
        super();
        this.fields = fields;
        this.descending = descending;
    }
}
