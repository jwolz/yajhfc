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

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import yajhfc.PasswordDialog;
import yajhfc.utils;
import yajhfc.phonebook.DefaultPhoneBookEntryComparator;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookException;

public class LDAPPhoneBook extends PhoneBook {

    public static final String PB_Prefix = "LDAP";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = utils._("LDAP Phonebook (read only)"); // A user-readable name for this Phonebook type
    public static final String PB_Description = utils._("A Phonebook reading its entries from an LDAP directory."); // A user-readable description of this Phonebook type
    
    LDAPSettings settings;
    private DirContext ctx;
    
    private ArrayList<LDAPPhoneBookEntry> entries = new ArrayList<LDAPPhoneBookEntry>();
    
    @Override
    public boolean isReadOnly() {
        return true;
    }
    
    @Override
    public PhoneBookEntry addNewEntry() {
        return null;
    }

    @Override
    public String browseForPhoneBook() {
        ConnectionDialog cDlg = new ConnectionDialog(parentDialog);
        LDAPSettings cs = new LDAPSettings(settings);
        if (cDlg.browseForPhonebook(cs))
            return PB_Prefix + ":" + cs.saveToString();
        else
            return null;
    }

    @Override
    public void close() {
        entries.clear();
        
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                //NOP
            }
            ctx = null;
        }
    }

    @Override
    public boolean isOpen() {
        return (ctx != null);
    }

    @Override
    protected void openInternal(String descriptorWithoutPrefix)
            throws PhoneBookException {
        try {
            settings = new LDAPSettings(descriptorWithoutPrefix);
            
            Hashtable<String,String> env = new Hashtable<String,String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://" + settings.serverName  + ":" + settings.port +  "/" + settings.baseDN);
            
            if (settings.useAuth) {
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, settings.bindDN);
                
                String password;
                if (settings.askForCredential) {
                    password = PasswordDialog.showPasswordDialog(parentDialog, utils._("LDAP password"), MessageFormat.format(utils._("Please enter the LDAP password for user {0}."), settings.bindDN));
                    if (password == null)
                        return;
                } else {
                    password = settings.credential;
                }
                env.put(Context.SECURITY_CREDENTIALS, password);
            } else {
                env.put(Context.SECURITY_AUTHENTICATION, "none");
            }
            
            ctx = new InitialDirContext(env);
            
            SearchControls sctl = new SearchControls();
            if (settings.searchSubTree) 
                sctl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            else
                sctl.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            
            sctl.setReturningAttributes(settings.getAttributes());
            
            String filter;
            if (settings.objectFilter == null || settings.objectFilter.length() == 0)
                filter = "(objectClass=*)";
            else
                filter = settings.objectFilter;
            
            NamingEnumeration<SearchResult> res = ctx.search("", filter, sctl);
            while (res.hasMore()) {
                entries.add(new LDAPPhoneBookEntry(this, res.next()));
            }
            res.close();
            
            resort();
        } catch (NamingException e) {
            ctx = null;
            throw new PhoneBookException(e, false);
        }
    }

    @Override
    public String getDisplayCaption() {
        String rv = PB_Prefix + ":" + settings.serverName + "/" + settings.baseDN;

        
        if (rv.length() > 30)
            return rv.substring(0, 27) + "...";
        else
            return rv;
    }
    
    @Override
    public void resort() {
        Collections.sort(entries, DefaultPhoneBookEntryComparator.globalInstance);
    }

    public Object getElementAt(int index) {
        return entries.get(index);
    }

    public int getSize() {
        return entries.size();
    }

    public LDAPPhoneBook(Dialog parent) {
        super(parent);
       
    }
}
