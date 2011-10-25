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
 */
package yajhfc.phonebook.convrules;


/**
 * A rule that appends the first child rule containing an empty String.
 * @author jonas
 *
 */
public class ChoiceRule extends DefaultEntryToStringRule {

    protected EntryToStringRule[] children;
    
    @Override
    public int applyRule(PBEntryFieldContainer entry, StringBuilder appendTo) {
        for (EntryToStringRule rule : children) {
            int res = rule.applyRule(entry, appendTo);
            if (res != 0)
                return res;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (EntryToStringRule rule : children) {
            res.append(rule.toString()).append(" / ");
        }
        res.delete(res.length()-4, res.length()-1);
        return res.toString();
    }
    
    public ChoiceRule(EntryToStringRule... children) {
        super();
        this.children = children;
    }

}
