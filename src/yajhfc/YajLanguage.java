package yajhfc;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import yajhfc.util.MsgBundle;

public class YajLanguage extends MsgBundle {
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
        supportedLanguages.add(new YajLanguage(Locale.TRADITIONAL_CHINESE));
        supportedLanguages.add(new YajLanguage(Locale.SIMPLIFIED_CHINESE));
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
            if (lang.matchesLangCode(langCode)) {
                return lang;
            }
        }
        return SYSTEM_DEFAULT;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected final Locale locale;
    private String description = null;
    private final String langCode;
    
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
                return locale.getDisplayName(Utils.getLocale());
            else 
                return locale.getDisplayName(locale) + " (" + locale.getDisplayName(Utils.getLocale()) + ")";
        }
    }
    
    public Locale getLocale() {
        if (locale == null) {
            return Utils.DEFAULT_LOCALE;
        } else {
            return locale;
        }
    }
    
    /**
     * Returns the language code for this language (e.g. de or zh_TW)
     * @return
     */
    public String getLangCode() {
        return langCode;
    }
    
    /**
     * Returns if the language is at least as specific as the given language code.
     * For example: 
     *  - zh_CN will match "zh" or "zh_CN" 
     *  - de will match "de", "de_DE" or "de_AT"
     * @param langCode
     * @return
     */
    public boolean matchesLangCode(String langCode) {
        if (this.langCode.length() >= langCode.length())
            return this.langCode.startsWith(langCode);
        else
            return langCode.startsWith(this.langCode);
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
        super("yajhfc.i18n.Messages", YajLanguage.class.getClassLoader());
        this.locale = locale;
        
        if (locale == null) {
            langCode = "auto";
        } else {
            langCode = locale.toString();
        }
    }
}
