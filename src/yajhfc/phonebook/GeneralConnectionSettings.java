package yajhfc.phonebook;

import java.util.Arrays;
import java.util.Map;

/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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


public class GeneralConnectionSettings extends AbstractConnectionSettings {    
//    public String name = "";
//    public String givenName = "";
//    public String title = "";
//    public String location = "";
//    public String company = "";
//    public String faxNumber = "";
//    public String voiceNumber = "";
//    public String comment = "";
    public static final PBEntrySettingsField[] entryFields;
    static {
        final PBEntryField[] fields = PBEntryField.values();
        entryFields = new PBEntrySettingsField[fields.length];
        for (int i = 0; i < entryFields.length; i++) {
            PBEntryField field = fields[i];
            String name = field.name();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            entryFields[i] = new PBEntrySettingsField(field, name);
        }
    }
    
    private String[] pbFields = new String[entryFields.length];
    
    public String getMappingFor(PBEntryField field) {
        return pbFields[field.ordinal()];
    }

    public void setMappingFor(PBEntryField field, String mapping) {
        pbFields[field.ordinal()] = mapping;
    }
    
    @Override
    protected void readAvailableFields(Map<String, SettingField> availableFieldsMap) {
        super.readAvailableFields(availableFieldsMap);
        for (SettingField field : entryFields) {
			availableFieldsMap.put(field.getName(), field);
        }
    }

    public GeneralConnectionSettings() {
        this(AbstractConnectionSettings.noField);
    }
    
    public GeneralConnectionSettings(String defaultMapping) {
        Arrays.fill(pbFields, defaultMapping);
    }
    
    public static class PBEntrySettingsField implements SettingField {
        private final PBEntryField field;
        private final String name;
        
        public Object get(AbstractConnectionSettings instance) {
            return ((GeneralConnectionSettings)instance).getMappingFor(field);
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return String.class;
        }

        public void set(AbstractConnectionSettings instance, Object value) {
            ((GeneralConnectionSettings)instance).setMappingFor(field, (String)value);
        }

        public PBEntryField getField() {
            return field;
        }
        
        public boolean isFieldSaved() {
            return true;
        }
        
        PBEntrySettingsField(PBEntryField field, String name) {
            super();
            this.field = field;
            this.name = name;
        }
    }
}
