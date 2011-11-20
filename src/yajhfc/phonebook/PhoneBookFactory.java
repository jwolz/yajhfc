package yajhfc.phonebook;

import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;

import yajhfc.Utils;
import yajhfc.phonebook.csv.CSVPhoneBook;
import yajhfc.phonebook.jdbc.JDBCPhoneBook;
import yajhfc.phonebook.ldap.LDAPPhoneBook;
import yajhfc.phonebook.xml.XMLPhoneBook;
import yajhfc.phonebook.xml.XMLSettings;

/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */

public class PhoneBookFactory {

    public static final ArrayList<PhoneBookType> PhonebookTypes;
    
    static {
        PhonebookTypes = new ArrayList<PhoneBookType>();
        PhonebookTypes.add(new PhoneBookType(XMLPhoneBook.class));
        PhonebookTypes.add(new PhoneBookType(JDBCPhoneBook.class));
        PhonebookTypes.add(new PhoneBookType(LDAPPhoneBook.class));
        PhonebookTypes.add(new PhoneBookType(CSVPhoneBook.class));
    }
    
    public static PhoneBook instanceForDescriptor(String descriptor, Dialog parent) {
        int pos = descriptor.indexOf(':');
        if (pos <= 0) {
            return new XMLPhoneBook(parent); //Compatibility with old versions (-> no prefix)
        } else
            return instanceForPrefix(descriptor.substring(0, pos), parent);
    }
    
    public static PhoneBook instanceForPrefix(String prefix, Dialog parent) {
        for (PhoneBookType pe: PhonebookTypes) {
            if (prefix.equals(pe.getPrefix()))
                return pe.createInstance(parent);
        }
        return null;
    }
    
    public static final String DEFAULT_PHONEBOOK_NAME = Utils._("Personal phone book");
    
    public static File getDefaultPhonebook() {
        return new File(Utils.getConfigDir(), "default.phonebook");
    }
    
    public static String getDefaultPhonebookDescriptor() {
        XMLSettings settings = new XMLSettings();
        settings.fileName = getDefaultPhonebook().getAbsolutePath();
        settings.caption = DEFAULT_PHONEBOOK_NAME;
        return XMLPhoneBook.PB_Prefix + ":" + settings.saveToString();
    }
}
