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
            new HylaModem("any", null, Utils._("Any modem"))
    });
    
    public HylaModem(String internalName, String number, String description) {
        super();
        this.internalName = internalName;
        this.number = number;
        this.description = description;
    }

    public HylaModem(String internalName, String number) {
        this(internalName, number, generateDescription(internalName, number));
    }
    
    public HylaModem(String serializedForm) {
        super();
        loadFromString(serializedForm);
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
    
    public String saveToString() {
        return Utils.escapeChars(internalName==null?"":internalName, "|", '~') + "|" + 
            Utils.escapeChars(number==null?"":number, "|", '~') + "|" + 
            Utils.escapeChars(description==null?"":description, "|", '~');
    }
    
    protected void loadFromString(String input) {
        String[] splitted = Utils.fastSplit(input, '|');
        if (splitted.length >= 1) {
            internalName = Utils.unEscapeChars(splitted[0], "|", '~');
        }
        if (splitted.length >= 2) {
            number = Utils.unEscapeChars(splitted[1], "|", '~');
        }
        if (splitted.length >= 3) {
            description = Utils.unEscapeChars(splitted[2], "|", '~');
        }
    }
    
    protected static String generateDescription(String internalName,
            String number) {
        return internalName + " (" + number + ")";
    }
}
