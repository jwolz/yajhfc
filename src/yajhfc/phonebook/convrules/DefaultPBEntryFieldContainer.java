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
package yajhfc.phonebook.convrules;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.phonebook.PBEntryField;

/**
 * @author jonas
 *
 */
public class DefaultPBEntryFieldContainer extends EnumMap<PBEntryField, String>
        implements PBEntryFieldContainer {

    private static final Logger log = Logger.getLogger(DefaultPBEntryFieldContainer.class.getName());
    
    /* (non-Javadoc)
     * @see yajhfc.phonebook.convrules.PBEntryFieldContainer#getField(yajhfc.phonebook.PBEntryField)
     */
    public String getField(PBEntryField field) {
        return get(field);
    }
    
    public void setField(PBEntryField field, String value) {
        put(field, value);
    }
    
    public void copyFrom(PBEntryFieldContainer other) {
        for (PBEntryField field : PBEntryField.values()) {
            put(field, other.getField(field));
        }
    }    
    
    /**
     * Parses a string in the format field1:value1;field2:value2;... into this instance.
     * Fields not specified in the String are set to "".
     * 
     * @param numberOrFullFields
     */
    public DefaultPBEntryFieldContainer parseFromString(final String numberOrFullFields) {
        setAllFieldsTo("");
        parseStringToPBEntryFieldContainer(this, numberOrFullFields);
        return this;
    }
    
    /**
     * Parses a string in the format field1:value1;field2:value2;... into the given PBEntryFieldContainer
     * Fields not specified in the String keep their current values.
     * 
     * @param container
     * @param numberOrFullFields
     */
    @SuppressWarnings("fallthrough")
    public static void parseStringToPBEntryFieldContainer(final PBEntryFieldContainer container, final String numberOrFullFields) {
        // If it contains no : or ;, assume it's a fax number
        if (numberOrFullFields.indexOf(':') < 0 || numberOrFullFields.indexOf(';') < 0) {
            container.setField(PBEntryField.FaxNumber, numberOrFullFields);
        } else {
            int pos;
            int oldPos = 0;
            StringBuilder value = new StringBuilder();
            while ((pos = numberOrFullFields.indexOf(':', oldPos)) >= 0) {
                String key = numberOrFullFields.substring(oldPos, pos).trim().toLowerCase();
                value.setLength(0);

                boolean afterBackSlash = false;
                parseLoop: while (pos < numberOrFullFields.length()-1) {
                    final char c = numberOrFullFields.charAt(++pos);
                    if (afterBackSlash) {
                        switch (c) {
                        default:
                            value.append('\\'); // Fall through intended
                        case ';':
                        case '\\':
                            value.append(c);
                        }
                        afterBackSlash = false;
                    } else {
                        switch (c) {
                        case '\\':
                            afterBackSlash = true;
                            break;
                        case ';':
                            break parseLoop;
                        default:
                            value.append(c);
                        break;
                        }
                    }
                }

                PBEntryField pbField = PBEntryField.getKeyToFieldMap().get(key);
                if (pbField != null) {
                    container.setField(pbField, value.toString().trim());
                } else {
                    log.info("Unknown field:value \"" + key + ':' + value + '"');
                }
                oldPos = pos+1;
            }
        }
    }
    
    public void setAllFieldsTo(String value) {
        for (PBEntryField field : PBEntryField.values()) {
            put(field, value);
        }
    }
    
    public DefaultPBEntryFieldContainer() {
        super(PBEntryField.class);
    }

    public DefaultPBEntryFieldContainer(String defValue) {
        this();
        setAllFieldsTo(defValue);
    }
    
    public DefaultPBEntryFieldContainer(PBEntryFieldContainer other) {
        this();
        copyFrom(other);
    }
    
    public DefaultPBEntryFieldContainer(String faxNumber, String name, String company, String location, String voiceNumber) {
        this("");
        setField(PBEntryField.FaxNumber, faxNumber);
        setField(PBEntryField.Name, name);
        setField(PBEntryField.Company, company);
        setField(PBEntryField.Location, location);
        setField(PBEntryField.VoiceNumber, voiceNumber);
    }
        
    /**
     * Parses a list of recipients as given on the command line and adds them to the target list
     * @param cmdLineRecipients
     * @param targetList
     */
    public static void parseCmdLineStrings(Collection<PBEntryFieldContainer> targetList, Collection<String> cmdLineRecipients) {
        for (String number : cmdLineRecipients) {
            if (number.startsWith("@")) {
                try {
                    readListFile(targetList, number.substring(1));
                } catch (IOException e) {
                    log.log(Level.WARNING, "Error reading the recipients from the file specified by " + number, e);
                }
            } else {
                targetList.add(new DefaultPBEntryFieldContainer().parseFromString(number));
            }
        }
    }
    
    /**
     * Parses a file containing recipients as given on the command line and adds them to the target list
     * @param cmdLineRecipients
     * @param targetList
     * @throws IOException 
     */
    public static void readListFile(Collection<PBEntryFieldContainer> targetList, String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0) {
                targetList.add(new DefaultPBEntryFieldContainer().parseFromString(line));
            }
        }
        reader.close();
    }
}
