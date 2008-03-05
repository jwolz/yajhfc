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

import java.util.Arrays;
import java.util.List;

/**
 * Description of a modem
 * @author jonas
 *
 */
public class HylaModem {
    protected String internalName;
    protected String number;
    protected String description;
    
    public static final List<HylaModem> defaultModems = Arrays.asList(new HylaModem[] {
            new HylaModem("any", null, utils._("Any modem"))
    });
    
    public HylaModem(String internalName, String number, String description) {
        super();
        this.internalName = internalName;
        this.number = number;
        this.description = description;
    }

    public HylaModem(String internalName, String number) {
        this(internalName, number, internalName + " (" + number + ")");
    }

    public String getInternalName() {
        return internalName;
    }


    public String getNumber() {
        return number;
    }


    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
