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
package yajhfc.phonebook;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public abstract class AbstractConnectionSettings {
    static final Logger log = Logger.getLogger(AbstractConnectionSettings.class.getName());
    
    private static final String separator = ";";
    private static final char escapeChar = '~';
    
    public static final String noField = "<none>";
    public static final String noField_translated = Utils._("<none>");
    
    public static boolean isNoField(String fieldName) {
        return (fieldName == null || fieldName.length() == 0 || fieldName.equals(noField));
    }
    

    protected final Map<String,SettingField> availableFields = new TreeMap<String,SettingField>();
    
    public AbstractConnectionSettings() {
        readAvailableFields();
    }

    /**
     * Reads all available *public* fields using reflection.
     */
    protected void readAvailableFields() {
        availableFields.clear();
        
        for (Field f : getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            if (Modifier.isFinal(f.getModifiers()))
                continue;
            
            availableFields.put(f.getName(), new ReflectionField(f));
        }     
    }

    
    public Set<String> getAvailableFields() {
        return availableFields.keySet();
    }
    
    public void setField(String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException {
        SettingField f = availableFields.get(fieldName);
        if (f == null) {
            throw new IllegalArgumentException("Field " + fieldName + " not found.");
        }
        f.set(this, value);
    }
    
    public Object getField(String fieldName) throws IllegalArgumentException, IllegalAccessException {
        SettingField f = availableFields.get(fieldName);
        if (f == null) {
            throw new IllegalArgumentException("Field " + fieldName + " not found.");
        }
        return f.get(this);
    }
    
    public void copyFrom(AbstractConnectionSettings other) {
        if (other == null)
            return;
        

        for (SettingField f : availableFields.values()) {
            try {
                f.set(this, f.get(other));
            } catch (Exception e) {
                //NOP
            }
        }
    }
    
    public String saveToString() {
        StringBuilder builder = new StringBuilder();
        for (SettingField f : availableFields.values()) {            
            try {
                String val;
                val = f.get(this).toString();
                val = Utils.escapeChars(val, separator, escapeChar) ;
                builder.append(f.getName());
                builder.append('=');
                builder.append(val);
                builder.append(separator);
            } catch (Exception e) {
                //NOP
            }
        }
        
        return builder.toString();
    }
    
    public void loadFromString(String input) {
        if (input == null)
            return;
        
        String[] tokens = Utils.fastSplit(input, separator.charAt(0));
        for (String line : tokens) {
            int pos = line.indexOf('=');
            if (pos < 0)
                continue;
            
            String fieldName = line.substring(0, pos);
            String value = Utils.unEscapeChars(line.substring(pos+1), separator, escapeChar);
            try {
                SettingField f = availableFields.get(fieldName);
                if (f == null) {
                    log.warning("Field not found: " + fieldName);
                    continue;
                }
                Class<?> f_class = f.getType();
                if (f_class == String.class) {
                    f.set(this, value);
                } else if (f_class == Boolean.TYPE || f_class == Boolean.class) {
                    f.set(this, Boolean.valueOf(value));
                } else if (f_class == Integer.TYPE || f_class == Integer.class) {
                    f.set(this, Integer.valueOf(value));
                } else {
                    log.log(Level.WARNING, "Unsupported field type " + f_class.getName() + " of field " + fieldName);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Exception loading fields:", e);
            }
        }
    }
    
    public interface SettingField {
        public abstract String getName();
        public abstract Object get(AbstractConnectionSettings instance);
        public abstract void set(AbstractConnectionSettings instance, Object value);
        public abstract Class<?> getType();
    }
    
    protected static class ReflectionField implements SettingField {
        protected final Field field;
        
        public String getName() {
            return field.getName();
        }

        public Class<?> getType() {
            return field.getType();
        }

        public Object get(AbstractConnectionSettings instance) {
            try {
                return field.get(instance);
            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, "Could not read value:", e);
                return null;
            } catch (IllegalAccessException e) {
                log.log(Level.WARNING, "Could not read value:", e);
                return null;
            }
        }

        public void set(AbstractConnectionSettings instance, Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, "Could not write value:", e);
            } catch (IllegalAccessException e) {
                log.log(Level.WARNING, "Could not write value:", e);
            }
        }

        public ReflectionField(Field field) {
            super();
            this.field = field;
        }

    }
}
