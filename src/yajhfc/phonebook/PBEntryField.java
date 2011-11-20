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
package yajhfc.phonebook;

import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import yajhfc.Utils;
import yajhfc.filters.FilterKey;
import yajhfc.filters.FilterKeyList;

public enum PBEntryField implements FilterKey {
    GivenName(Utils._("First name"), "givenname", true),
    Name(Utils._("Last Name#(please translate as short form of surname)#", "Last Name"), "surname", true),
    Title(Utils._("Title"), "title", true),
    Company(Utils._("Company"), "company", true),
    Position(Utils._("Position"), "position", true),
    Department(Utils._("Department"), "department", true),
    Street(Utils._("Street"), "street", false),
    Location(Utils._("City"), "location", true),
    Country(Utils._("Country"), "country", true),
    ZIPCode(Utils._("ZIP code"), "zipcode", true),
    State(Utils._("State/Region"), "state", true),
    EMailAddress(Utils._("e-mail address"), "email", true),
    WebSite(Utils._("Website"), "website", true),
    FaxNumber(Utils._("Fax number"), "faxnumber", true),
    VoiceNumber(Utils._("Voice number"), "voicenumber", true),
    Comment(Utils._("Comments"), "comment", false)
    ;
    // Classes to adapt when a field is added/removed:
    // - MarkupFaxcover
    // - SenderIdentity
    // - LDAPSettings
    
    private final String desc;
    private final String key;
    private final boolean shortLength;
    
    private PBEntryField(String desc, String key, boolean shortLength) {
        this.desc = desc;
        this.key = key;
        this.shortLength = shortLength;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public String toString() {
        return desc;
    }

    public String getKey() {
        return key;
    }
    
    /**
     * Returns if the field's value has a short length (for automatic GUI creation)
     * @return
     */
    public boolean isShortLength() {
        return shortLength;
    }
    
    public Class<?> getDataType() {
        return String.class;
    }
    
    public Format getFormat() {
        return null;
    }
    
    /**
     * The number of fields. Equals values().length, but is more efficient
     */
    public static final int FIELD_COUNT = values().length;
    
    public static final FilterKeyList<PBEntryField> filterKeyList = new FilterKeyList<PBEntryField>() {

        public boolean containsKey(PBEntryField key) {
            return true;
        }

        public PBEntryField[] getAvailableKeys() {
            return values();
        }

        public Object translateKey(PBEntryField key) {
            return key;
        }

        public PBEntryField getKeyForName(String name) {
            try {
                return Enum.valueOf(PBEntryField.class, name);
            } catch (Exception e) {
                return null;
            }
        }
        
    };

    private static Map<String,PBEntryField> keyToFieldMap;
    public static Map<String,PBEntryField> getKeyToFieldMap() {
        if (keyToFieldMap == null) {
            keyToFieldMap = new HashMap<String,PBEntryField>();
            for (PBEntryField field : PBEntryField.values()) {
                PBEntryField.keyToFieldMap.put(field.getKey(), field);
            }
        }
        return keyToFieldMap;
    }
    
}
