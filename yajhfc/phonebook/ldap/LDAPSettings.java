package yajhfc.phonebook.ldap;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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

import java.util.logging.Logger;

import yajhfc.utils;
import yajhfc.phonebook.GeneralConnectionSettings;


public class LDAPSettings extends GeneralConnectionSettings {
    public String serverName = ""; //"suse101-vmw";
    public int port = 389;
    public String baseDN = ""; //"o=Scalix";
    public boolean useAuth = false;
    public String bindDN = "";
    public String credential = "";
    public boolean askForCredential = false;
    
    public String objectFilter = "(facsimileTelephoneNumber=*)";
    public boolean searchSubTree = true;
    
    public String displayCaption = "";
    
    public String[] getAttributes() {
        int count = 0;
        for (int i=0; i < FIELD_COUNT; i++) 
            if (!isNoField(getMappingFor(i)))
                count++;
        
        String[] rv = new String[count];
        int j = 0;
        for (int i=0; i < FIELD_COUNT; i++) {
            String mapping = getMappingFor(i);
            if (!isNoField(mapping)) 
                rv[j++] = mapping;
        }
        
        return rv;
    }
    
    public LDAPSettings() {
        super();
        name = "sn";
        givenName = "givenName";
        location = "l";
        title = "title";
        company = "o";
        voiceNumber = "telephoneNumber";
        faxNumber = "facsimileTelephoneNumber";
        comment = "description";
    }
    
    public LDAPSettings(String serialized) {
        this();
        loadFromString(serialized);
    }
    public LDAPSettings(LDAPSettings src) {
        this();
        copyFrom(src);
    }


    // Parser states:
    private static final int STATE_NORMAL = 0;
    private static final int STATE_INQUOTE = 1;
    private static final int STATE_CURCHARESCAPED = 2;
    private static final int STATE_STRIPSPACES = 3;
    /**
     * Strips spaces after , and ; used as separator to wor around
     * server bugs.
     * @param dn
     * @return
     */
    public static final String sanitizeDN(String dn) {
        StringBuilder rv = new StringBuilder(dn.length());
        int state = STATE_NORMAL;

        // Simple "parser" to detect commas inside quotes
        // and escaped commas (i.e. \, and \;)
        for (int i=0; i < dn.length(); i++) {
            char c = dn.charAt(i);

            switch (state) {
            case STATE_NORMAL:
            default:
                rv.append(c);

                switch (c) {
                case '\"':
                    state = STATE_INQUOTE;
                    break;
                case '\\':
                    state = STATE_CURCHARESCAPED;
                    break;
                case ',':
                case ';':
                    state = STATE_STRIPSPACES;
                    break;
                default:
                    break;
                }
                break;
            case STATE_INQUOTE:
                rv.append(c);

                if (c=='\"') {
                    state = STATE_NORMAL;
                }
                break;
            case STATE_CURCHARESCAPED:
                rv.append(c);

                state = STATE_NORMAL;
                break;
            case STATE_STRIPSPACES:
                if (!Character.isWhitespace(c)) {
                    rv.append(c);
                    state = STATE_NORMAL;
                }
                break;
            }
        }

        if (utils.debugMode) {
            Logger.getLogger(LDAPSettings.class.getName()).fine("Changed DN from \"" + dn +  "\" to \"" + rv + "\"");
        }
        return rv.toString();
    }

    // Test code:
//    public static void main(String[] args) {
//        System.out.println(sanitizeDN("ou=test,dc=test,dc=de"));
//        System.out.println(sanitizeDN("ou=test, dc=test,   dc=de"));
//        System.out.println(sanitizeDN("ou=\"test\", dc=test, dc=de"));
//        System.out.println(sanitizeDN("ou=\"test, foo\", dc=test, dc=de"));
//        System.out.println(sanitizeDN("ou=test\\,  bar, dc=test; dc=de"));
//    }
}
