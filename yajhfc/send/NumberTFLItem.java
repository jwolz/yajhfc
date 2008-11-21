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
package yajhfc.send;

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBookEntry;

class NumberTFLItem extends TFLItem {
    public String faxNumber;
    public String name, company, location, voiceNumber;
    
    @Override
    public String getText() {
        return faxNumber;
    }
    
    @Override
    public void setText(String newText) {
        faxNumber = newText;
    }
    
    public void loadFromPBE(PhoneBookEntry pbe) {
        faxNumber = pbe.getField(PBEntryField.FaxNumber);
        
        company = pbe.getField(PBEntryField.Company);
        location = pbe.getField(PBEntryField.Location);
        voiceNumber = pbe.getField(PBEntryField.VoiceNumber);
        
        StringBuilder nameBuilder = new StringBuilder();
        if (pbe.getField(PBEntryField.Title).length() > 0)
            nameBuilder.append(pbe.getField(PBEntryField.Title)).append(' ');
        if (pbe.getField(PBEntryField.GivenName).length() > 0)
            nameBuilder.append(pbe.getField(PBEntryField.GivenName)).append(' ');
        nameBuilder.append(pbe.getField(PBEntryField.Name));
        name = nameBuilder.toString();
    }
    
    public NumberTFLItem(String number) {
        this.faxNumber = number;
        name = company = location = voiceNumber = "";
    }
    
    public NumberTFLItem(PhoneBookEntry pbe) {
        loadFromPBE(pbe);
    }
}