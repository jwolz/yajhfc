package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YajLanguage  {
//    SYSTEM_DEFAULT(null),
//    CHINESE(Locale.CHINESE),
//    ENGLISH(Locale.ENGLISH),
//    FRENCH(Locale.FRENCH),
//    GERMAN(Locale.GERMAN),
//    GREEK(new Locale("el")),
//    ITALIAN(Locale.ITALIAN),
//    POLISH(new Locale("pl")),
//    RUSSIAN(new Locale("ru")),
//    SPANISH(new Locale("es")),
//    TURKISH(new Locale("tr"))
//    ;
    
    private static final Logger log = Logger.getLogger(YajLanguage.class.getName());
    
    public static final List<YajLanguage> supportedLanguages;
    public static final YajLanguage SYSTEM_DEFAULT;
    static {
        supportedLanguages = new ArrayList<YajLanguage>();
        supportedLanguages.add(SYSTEM_DEFAULT = new YajLanguage(null));
        supportedLanguages.add(new YajLanguage(Locale.CHINESE));
        supportedLanguages.add(new YajLanguage(Locale.ENGLISH));
        supportedLanguages.add(new YajLanguage(Locale.FRENCH));
        supportedLanguages.add(new YajLanguage(Locale.GERMAN));
        supportedLanguages.add(new YajLanguage(new Locale("el")));
        supportedLanguages.add(new YajLanguage(Locale.ITALIAN));
        supportedLanguages.add(new YajLanguage(new Locale("pl")));
        supportedLanguages.add(new YajLanguage(new Locale("ru")));
        supportedLanguages.add(new YajLanguage(new Locale("es")));
        supportedLanguages.add(new YajLanguage(new Locale("tr")));
    }
        
    public static YajLanguage languageFromLangCode(String langCode) {
        for (YajLanguage lang : supportedLanguages) {
            if (lang.getLangCode().equals(langCode)) {
                return lang;
            }
        }
        return SYSTEM_DEFAULT;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected final Locale locale;
    private String description = null;
    
    public String toString() {
        if (description == null) {
            // Cache the description
            description = createDescription();
        }
        return description; 
    }

    protected String createDescription() {
        if (locale == null) {
            return Utils._("(System default)");
        } else {
            if (Utils.getLocale().getLanguage().equals(locale.getLanguage())) 
                return locale.getDisplayLanguage(Utils.getLocale());
            else 
                return locale.getDisplayLanguage(locale) + " (" + locale.getDisplayLanguage(Utils.getLocale()) + ")";
        }
    }
    
    public Locale getLocale() {
        if (locale == null) {
            return Utils.DEFAULT_LOCALE;
        } else {
            return locale;
        }
    }
    
    public String getLangCode() {
        if (locale == null) {
            return "auto";
        } else {
            return locale.getLanguage();
        }
    }
    
    /**
     * Returns the class loader to load resources for this language
     * @return
     */
    protected ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
    
    /**
     * Loads the specified resource
     * @param path
     * @return
     */
    protected URL getResource(String path) {
        return getClass().getResource(path);
    }
    
    
    /**
     * Returns the messages resource bundle for this language
     * @return the resource bundle or null
     */
    public ResourceBundle getMessagesResourceBundle() {
        // Use special handling for english locale as we don't use
        // a ResourceBundle for it
        final Locale myLocale = getLocale();
        if (myLocale.equals(Locale.ENGLISH)) {
            if (Utils.debugMode) {
                log.fine("Not loading messages for language " + myLocale);
            }
            return null;
        } else {
            try {
                if (Utils.debugMode) {
                    log.fine("Trying to load messages for language " + myLocale);
                }
                return ResourceBundle.getBundle("yajhfc.i18n.Messages", myLocale, getClassLoader());
            } catch (Exception e) {
                log.log(Level.WARNING, "Error loading messages for " + myLocale, e);
                return null;
            }
        }
    }
    
    
    /**
     * Loads a localized version of the specified file
     * @param path the path to the resource
     * @param intlVersionValid if the English version is OK, too
     * @return the URL to the resource or null if no localized version could be found
     */
    public URL getLocalizedFile(String path, boolean intlVersionValid) {
        String prefix;
        String suffix;
        Locale loc = getLocale();
        int pos = path.lastIndexOf('.');
        
        if (pos < 0) {
            prefix = path;
            suffix = "";
        } else {
            prefix = path.substring(0, pos);
            suffix = path.substring(pos);
        }
        
        String[] tryList = {
                prefix + "_" + loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant() + suffix,
                prefix + "_" + loc.getLanguage() + "_" + loc.getCountry() + suffix,
                prefix + "_" + loc.getLanguage() + suffix
        };
        URL lURL = null;
        for (int i = 0; i < tryList.length; i++) {
            if (Utils.debugMode) {
                log.fine("Trying to find " + tryList[i] + " for language " + loc);
            }
            lURL = getResource(tryList[i]);
            if (lURL != null) {
                if (Utils.debugMode) {
                    log.fine("Found " + lURL);
                }
                return lURL;
            }
        }
        if (intlVersionValid) {
            return getResource(path);
        } else {
            return null;
        }
    }
    
    public YajLanguage(Locale locale) {
        this.locale = locale;
    }
}
