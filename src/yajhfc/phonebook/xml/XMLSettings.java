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
package yajhfc.phonebook.xml;

import java.io.File;

import yajhfc.phonebook.AbstractConnectionSettings;
import yajhfc.phonebook.PhoneBookFactory;


/**
 * @author jonas
 *
 */
public class XMLSettings extends AbstractConnectionSettings {
    public String fileName;
    public String caption;
    
    @Override
    public void loadFromString(String input) {
        if (!input.contains("fileName=")) { //Compatibility
            fileName = input;
            if (PhoneBookFactory.getDefaultPhonebook().equals(new File(fileName))) {
                caption = PhoneBookFactory.DEFAULT_PHONEBOOK_NAME;
            } else {
                caption = "";
            }
        } else {
            super.loadFromString(input);
        }
    }
    
}
