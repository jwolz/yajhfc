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

import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public enum CompanyRule implements EntryToStringRule {
    COMPANY(new ConcatRule(PBEntryField.Company)),
    DEPARTMENT_COMPANY(new ConcatRule(PBEntryField.Department, ", ", PBEntryField.Company)),
    COMPANY_DEPARTMENT(new ConcatRule(PBEntryField.Company, " (", PBEntryField.Department, ")"))
    ;
    
    private final  String displayName;
    private final EntryToStringRule rule;
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }

    public String applyRule(PBEntryFieldContainer entry) {
        return rule.applyRule(entry);
    }

    public int applyRule(PBEntryFieldContainer entry, StringBuilder appendTo) {
        return rule.applyRule(entry, appendTo);
    }

    private CompanyRule(EntryToStringRule rule) {
        this.rule = rule;
        displayName = rule.toString();
    }
}
