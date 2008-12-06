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
package yajhfc;

import gnu.getopt.LongOpt;

/**
 * A long option with description (to auto-generate a help text)
 * @author jonas
 *
 */
public class ExtLongOpt extends LongOpt {

    private final String description;
    
    /**
     * @param name
     * @param has_arg
     * @param flag
     * @param val
     * @throws IllegalArgumentException
     */
    public ExtLongOpt(String name, int has_arg, StringBuffer flag, int val, String description)
            throws IllegalArgumentException {
        super(name, has_arg, flag, val);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
