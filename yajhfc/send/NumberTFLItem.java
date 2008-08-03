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
        faxNumber = pbe.getFaxNumber();
        
        company = pbe.getCompany();
        location = pbe.getLocation();
        voiceNumber = pbe.getVoiceNumber();
        
        StringBuilder nameBuilder = new StringBuilder();
        if (pbe.getTitle().length() > 0)
            nameBuilder.append(pbe.getTitle()).append(' ');
        if (pbe.getGivenName().length() > 0)
            nameBuilder.append(pbe.getGivenName()).append(' ');
        nameBuilder.append(pbe.getName());
        name = nameBuilder.toString();
    }
    
    public NumberTFLItem(String number) {
        this.faxNumber = number;
    }
    
    public NumberTFLItem(PhoneBookEntry pbe) {
        loadFromPBE(pbe);
    }
}