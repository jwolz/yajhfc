/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

import javax.swing.Icon;

/**
 * A fax string property with an attached icon.
 * @author jonas
 *
 */
public class FaxStringAndIconProperty extends FaxStringProperty implements
        IconMap {

    protected Icon displayIcon;

    public FaxStringAndIconProperty(String desc, String type, Icon displayIcon) {
        super(desc, type);
        this.displayIcon = displayIcon;
    }

    /* (non-Javadoc)
     * @see yajhfc.IconMap#getDisplayIcon()
     */
    public Icon getDisplayIcon() {
        return displayIcon;
    }

    /* (non-Javadoc)
     * @see yajhfc.IconMap#getText()
     */
    public String getText() {
        return desc;
    }

}
