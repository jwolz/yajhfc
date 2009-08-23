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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
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
                    List lst = (List)val;
                    int idx = 0;
                    for (Object o : lst) {
                        p.setProperty(propertyName + '.' + (++idx), (String)o);
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
                    final List<String> list = (List<String>)f.get(this);
                    list.clear();

                    int i = 1;
                    String val;
                    while ((val = p.getProperty(propertyName + '.' + i)) != null) {
                        list.add(val);
                        i++;
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
}
