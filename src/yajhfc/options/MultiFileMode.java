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
package yajhfc.options;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public enum MultiFileMode {
    NONE(Utils._("Multiple files, no conversion")),
    EXCEPT_COVER(Utils._("Single file except for cover (needs GhostScript+tiff2pdf)")),
    FULL_FAX(Utils._("Complete fax as single file (needs GhostScript+tiff2pdf)"))
    ;
    
    private final String desc;

    @Override
    public String toString() {
        return desc;
    }
    
    private MultiFileMode(String desc) {
        this.desc = desc;
    }
    
}
