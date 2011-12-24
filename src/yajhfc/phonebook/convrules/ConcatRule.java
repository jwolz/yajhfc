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
package yajhfc.phonebook.convrules;

import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public class ConcatRule extends DefaultEntryToStringRule {

    protected Object[] children;
    
    /* (non-Javadoc)
     * @see yajhfc.phonebook.convrules.DefaultEntryToStringRule#applyRule(yajhfc.phonebook.PhoneBookEntry, java.lang.StringBuilder)
     */
    @Override
    public int applyRule(PBEntryFieldContainer entry, StringBuilder appendTo) {
        int oldLen = appendTo.length();
        Object lastItem = null;
        //boolean ignoreNext = false;
        boolean foundField = false;
        
        // Concatenate the children.
        // A non-PBEntryField and non-EntryToStringRule child is only appended
        // if *both* its predecessor and successor (if these exist) are of non-zero length
        // PBEntryField and EntryToStringRule entries are always appended.
        for (Object child : children) {         
            if (child instanceof PBEntryField) {
                String val = entry.getField((PBEntryField)child);
                if (val != null && val.length() > 0) {
                    if (lastItem != null) {
                        appendTo.append(lastItem);
                    }
                    appendTo.append(val);
                    foundField = true;
                } else {
                    //ignoreNext = true;
                }
                lastItem = null;
            } else if (child instanceof EntryToStringRule) {
                int insertOffset = appendTo.length();
                if (((EntryToStringRule)child).applyRule(entry, appendTo) > 0) {
                    if (lastItem != null) {
                        appendTo.insert(insertOffset, lastItem);
                    }
                    foundField = true;
                } else {
                   // ignoreNext = true;
                }
                lastItem = null;
            } else {
//                if (lastItem != null) {
//                    appendTo.append(lastItem);
//                }
                if (foundField) { //!ignoreNext) {
                    lastItem = child;
                } else {
                    //ignoreNext = false;
                }
            }
        }
        if (lastItem != null) {
            appendTo.append(lastItem);
        }
        return appendTo.length() - oldLen;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        objectArrayToString(res, children);
        return res.toString();
    }
    
    /**
     * Converts an Object array as used by a ConcatRule into a String representation.
     * @param res
     * @param children
     */
    public static void objectArrayToString(StringBuilder res, Object[] children) {
        for (Object child : children) {
            if (child instanceof PBEntryField) {
                res.append('[').append(child).append(']');
            } else {
                res.append(child);
            }
        }
    }
    
    /**
     * Concatenates the children.
     * If the child is a EntryToStringRule or a PBEntryField, the corresponding
     * value is appended. Else the toString() method is called.
     * @param children
     */
    public ConcatRule(Object... children) {
        super();
        this.children = children;
    }

    /**
     * @return the children
     */
    public Object[] getChildren() {
        return children;
    }

}
