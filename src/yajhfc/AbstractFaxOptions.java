/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.model.FmtItemList;

/**
 * Class useable for options.
 * All public non-static fields are loaded/saved by loadFromProperties/saveToProperties.
 * 
 * Supported data types:
 * - String
 * - boolean
 * - int
 * - long
 * - YajLanguage
 * - FmtItemList
 * - Rectangle
 * - Point 
 * - List<String>
 * - Enum
 * - Password
 * - Map<String,String>
 * - Serializable
 * 
 * @author jonas
 *
 */
public abstract class AbstractFaxOptions {
    static final Logger log = Logger.getLogger(AbstractFaxOptions.class.getName());
    
    protected final String prefix;

    /**
     * Constructs a abstract fax options class with the specified property name prefix
     * @param prefix
     */
    public AbstractFaxOptions(String prefix) {
        super();
        this.prefix = prefix;
    }
    
    
    protected String getPropertyName(Field field) {
        if (prefix == null || prefix.length() == 0)
            return field.getName();
        else
            return prefix + '-' + field.getName();
    }
    
    private static final char sep = '|';
    
    public void storeToProperties(Properties p) {
        storeToProperties(p, getClass().getFields());
    }
    
    /**
     * Stores the fields given by f into p
     * @param p
     * @param f
     */
    @SuppressWarnings("unchecked")
    public void storeToProperties(Properties p, java.lang.reflect.Field[] f) {
        
        for (int i = 0; i < f.length; i++) {
            try {
                if (Modifier.isStatic(f[i].getModifiers()))
                    continue;
                
                Object val = f[i].get(this);
                if (val == null)
                    continue;
                
                final String propertyName = getPropertyName(f[i]);
                if ((val instanceof String) || (val instanceof Integer) || (val instanceof Boolean) || (val instanceof Long))
                    p.setProperty(propertyName, val.toString());
                else if (val instanceof YajLanguage) {
                    p.setProperty(propertyName, ((YajLanguage)val).getLangCode());
                } else if (val instanceof FmtItemList) {
                    p.setProperty(propertyName, ((FmtItemList)val).saveToString());
                } else if (val instanceof Rectangle) {
                    Rectangle rval = (Rectangle)val;
                    p.setProperty(propertyName, "" + rval.x + sep + rval.y + sep + rval.width + sep + rval.height);
                } else if (val instanceof Point) {
                    Point pval = (Point)val;
                    p.setProperty(propertyName, "" + pval.x + sep + pval.y);
                } else if (val instanceof List) {
                    Class<?>[] typeParams = resolveTypeParameters(List.class, f[i]);
                    if (typeParams == null || typeParams.length != 1) {
                        log.warning("Could not resolve type params for " + f[i]);
                        continue;
                    }
                    if (typeParams[0] == String.class) {
                        List lst = (List)val;
                        int idx = 0;
                        for (Object o : lst) {
                            p.setProperty(propertyName + '.' + (++idx), (String)o);
                        }
                    } else if (Enum.class.isAssignableFrom(typeParams[0])){
                        StringBuilder res = new StringBuilder();
                        List<? extends Enum> lst = (List<? extends Enum>)val;
                        for (Enum e : lst) {
                            res.append(e.name()).append(sep);
                        }
                        p.setProperty(propertyName, res.toString());
                    } else {
                        log.warning("Invalid list content type " + typeParams[0] + " for field " + f[i]);
                    }
                } else if (val instanceof Enum) {
                    p.setProperty(propertyName, ((Enum)val).name());
                } else if (val instanceof Password) {
                    p.setProperty(propertyName + "-obfuscated", ((Password)val).getObfuscatedPassword());
                } else if (val instanceof Map)  {
                    StringBuilder res = new StringBuilder();
                    Iterator it = ((Map)val).entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry)it.next();
                        String key = Utils.escapeChars((String)entry.getKey(), "=;", '~');
                        String value = Utils.escapeChars((String)entry.getValue(), "=;", '~');
                        
                        res.append(key).append('=').append(value).append(';');
                    }
                    p.setProperty(propertyName, res.toString());
                } else if (val instanceof Serializable) {
                    // As a "last resort" use Java object serialization
                    String sVal = serializeObjectToString(val);
                    p.setProperty(propertyName, sVal);
                } else {
                    log.log(Level.WARNING, "Unknown field type " + val.getClass().getName());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Exception reading field: ", e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadFromProperties(Properties p) {

        if (p.size() == 0) {
            log.info("No settings to load found.");
            return;
        }

        for (Field f : getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            
            try {
                final Class<?> fcls = f.getType();
                final String propertyName = getPropertyName(f);
                
                if (List.class.isAssignableFrom(fcls) 
                        && (!FmtItemList.class.isAssignableFrom(fcls))) {
                    Class<?>[] typeParams = resolveTypeParameters(List.class, f);
                    if (typeParams == null || typeParams.length != 1) {
                        log.warning("Could not resolve type params for " + f);
                        continue;
                    }
                    if (typeParams[0] == String.class) {
                        final List<String> list = (List<String>)f.get(this);
                        list.clear();

                        int i = 1;
                        String val;
                        while ((val = p.getProperty(propertyName + '.' + i)) != null) {
                            list.add(val);
                            i++;
                        }
                    } else if (Enum.class.isAssignableFrom(typeParams[0])){
                        String val = p.getProperty(propertyName);
                        if (val != null) {
                            List<Enum> lst = (List<Enum>)f.get(this);
                            lst.clear();
                            if (val.length() > 0) {
                                String[] items = Utils.fastSplit(val, sep);

                                for (String item : items) {
                                    lst.add(Enum.valueOf((Class<? extends Enum>)typeParams[0], item));
                                }
                            }
                        }
                    } else {
                        log.warning("Invalid list content type " + typeParams[0] + " for field " + f);
                    }
                } else if (Password.class.isAssignableFrom(fcls)) {
                    Password pwd = (Password)f.get(this);
                    String val = p.getProperty(propertyName);
                    if (val != null) {
                        pwd.setPassword(val);
                    } else {
                        val = p.getProperty(propertyName + "-obfuscated");
                        if (val != null) {
                            pwd.setObfuscatedPassword(val);
                        }
                    }
                } else {
                    String val = p.getProperty(propertyName);
                    if (val != null) {
                        if (String.class.isAssignableFrom(fcls))
                            f.set(this, val);
                        else if (Integer.TYPE.isAssignableFrom(fcls))
                            f.setInt(this, Integer.parseInt(val));
                        else if (Long.TYPE.isAssignableFrom(fcls))
                            f.setLong(this, Long.parseLong(val));
                        else if (Boolean.TYPE.isAssignableFrom(fcls))
                            f.setBoolean(this, Boolean.parseBoolean(val));
                        else if (YajLanguage.class.isAssignableFrom(fcls)) {
                            f.set(this, YajLanguage.languageFromLangCode(val));
                        } else if (FmtItemList.class.isAssignableFrom(fcls)) {
                            FmtItemList fim = (FmtItemList)f.get(this);
                            fim.loadFromString(val);
                        } else  if (Rectangle.class.isAssignableFrom(fcls)) {
                            String [] v =  Utils.fastSplit(val, sep);
                            f.set(this, new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3])));
                        } else if (Point.class.isAssignableFrom(fcls)) {
                            String [] v =  Utils.fastSplit(val, sep);
                            f.set(this, new Point(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                        } else if (Enum.class.isAssignableFrom(fcls)) {
                            f.set(this, Enum.valueOf((Class<? extends Enum>)fcls, val));
                        } else if (Map.class.isAssignableFrom(fcls)) {
                            Map map = (Map)f.get(this);
                            map.clear();
                            
                            String[] entries = Utils.fastSplit(val, ';');
                            for (String entry : entries) {
                                int pos = entry.indexOf('=');
                                if (pos > 0) {
                                    String key = Utils.unEscapeChars(entry.substring(0,pos), "=;", '~');
                                    String value = Utils.unEscapeChars(entry.substring(pos+1), "=;", '~');
                                    map.put(key,value);
                                } else {
                                    log.warning("Unknown map entry in " + propertyName + ": " + entry);
                                }
                            }
                        } else if (Serializable.class.isAssignableFrom(fcls)) {
                            // As a "last resort" use Java object serialization
                            Object obj = deserializeObjectFromString(val);
                            if (obj != null) {
                                f.set(this, obj);
                            }
                        } else {
                            log.log(Level.WARNING, "Unknown field type " + fcls);
                        }
                    }
                }
            } catch (Exception e1) {
                log.log(Level.WARNING, "Couldn't load setting for " + f + ": ", e1);
            }
        }
    }
    
    protected String serializeObjectToString(Object obj) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1000);
            ObjectOutputStream oos = new ObjectOutputStream(byteStream);
            oos.writeObject(obj);
            oos.close();
            
            // Use a primitive "bin-hex" encoding of the data
            byte[] data = byteStream.toByteArray();
            char[] stringData = new char[data.length*2];
            for (int i=0; i < data.length; i++) {
                byte d = data[i];
                stringData[2*i  ] = Character.forDigit((d >> 4) & 0xf, 16);
                stringData[2*i+1] = Character.forDigit( d       & 0xf, 16);
            }
            return new String(stringData);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error serializing " + obj, e);
            return null;
        }
    }
    
    protected Object deserializeObjectFromString(String serializedForm) {
        try {
            char[] stringData = serializedForm.toCharArray();
            if (stringData.length % 2 != 0)
                throw new IllegalArgumentException("String data length is not even!");
            byte[] byteData = new byte[stringData.length/2];
            for (int i=0; i < byteData.length; i++) {
                byteData[i] = (byte)((Character.digit(stringData[2*i  ], 16) << 4) |
                                      Character.digit(stringData[2*i+1], 16));
            }
            
            ByteArrayInputStream byteStream = new ByteArrayInputStream(byteData);
            ObjectInputStream ooi = new ObjectInputStream(byteStream);
            Object rv = ooi.readObject();
            ooi.close();
            return rv;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error deserializing", e);
            return null;
        } 
    }
    
    /**
     * Resolves the type parameters of the given field (e.g. for List<String>).
     * Does not handle stuff like "class MyList extends ArrayList<Field>"
     * Returns null if the resolving fails (e.g. for a List<List<String>>)
     * @param f
     * @return
     */
    protected Class<?>[] resolveTypeParameters(Class<?> iface, Field f) {
        Type genType = f.getGenericType();
        if (genType instanceof Class<?>) {
            for (Type t : ((Class<?>)genType).getGenericInterfaces()) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getRawType().equals(iface)) {
                        genType = pt;
                        break;
                    }
                }
            }
        }
        
        if (genType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genType;
            Type[] typeArgs = pt.getActualTypeArguments();
            Class<?>[] result = new Class[typeArgs.length];
            for (int i=0; i<typeArgs.length; i++) {
                if (typeArgs[i] instanceof Class<?>) {
                    result[i] = (Class<?>)typeArgs[i];
                } else {
                    log.fine("Need more levels of resolving for type " + typeArgs[i] + " for field " + f );
                    return null;
                }
            }
            return result;
        } else {
            log.fine("Not a parameterized type: " + genType + " for field " + f);
            return null;
        }
    }
}
