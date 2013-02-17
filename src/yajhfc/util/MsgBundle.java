/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2011 Jonas Wolz
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
package yajhfc.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * Class holding the translations
 * 
 * @author jonas
 *
 */
public class MsgBundle {
    private static final Logger log = Logger.getLogger(MsgBundle.class.getName());
    
    protected final String resourceBundleName;
    protected final ClassLoader classLoader;
    protected ResourceBundle msgs = null;
    protected boolean triedMsgLoad = false;
    
    /**
     * Returns the translation of key. If no translation is found, the
     * key is returned.
     * @param key
     * @return
     */
    public String _(String key) {
        return _(key, key);
    }
    
    /**
     * Returns the translation of key. If no translation is found, the
     * defaultValue is returned.
     * @param key
     * @param defaultValue
     * @return
     */
    public String _(String key, String defaultValue) {
        ResourceBundle messages = getMessagesResourceBundle();
        if (messages == null) {
            return defaultValue;              
        } else {
            try {
                return messages.getString(key);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
    
    public ResourceBundle getMessagesResourceBundle() {
        if (!triedMsgLoad) {
            triedMsgLoad = true;
            
            // Use special handling for english locale as we don't use
            // a ResourceBundle for it
            final Locale myLocale = getLocale();
            if (myLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                if (Utils.debugMode) {
                    log.fine("Not loading messages for language " + myLocale);
                }
                msgs = null;
            } else {
                try {
                    if (Utils.debugMode) {
                        log.fine("Trying to load messages for language " + myLocale);
                    }
                    msgs = ResourceBundle.getBundle(resourceBundleName, myLocale, getClassLoader());
                } catch (Exception e) {
                    log.log(Level.INFO, "Error loading messages for " + myLocale, e);
                    msgs = null;
                }
            }
        }
        return msgs;
    }
    
    /**
     * Returns the locale used by this MsgBundle
     * @return
     */
    public Locale getLocale() {
        return Utils.getLocale();
    }
    
    /**
     * Returns the class loader to load resources for this language
     * @return
     */
    protected ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public MsgBundle(String resourceBundleName, ClassLoader classLoader) {
        super();
        this.resourceBundleName = resourceBundleName;
        this.classLoader = classLoader;
    }
}
