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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import yajhfc.Utils;
import yajhfc.filters.AndFilter;
import yajhfc.filters.ConcatStringFilter;
import yajhfc.filters.Filter;
import yajhfc.filters.OrFilter;
import yajhfc.filters.StringFilter;
import yajhfc.filters.StringFilterOperator;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookException;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.PasswordDialog;

public class LDAPPhoneBook extends PhoneBook {

    public static final String PB_Prefix = "LDAP";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = Utils._("LDAP Phonebook (read only)"); // A user-readable name for this Phonebook type
    public static final String PB_Description = Utils._("A Phonebook reading its entries from an LDAP directory."); // A user-readable description of this Phonebook type

    private static final Logger log = Logger.getLogger(LDAPPhoneBook.class.getName());
    
    LDAPSettings settings;
    private DirContext ctx;
    
    private final ArrayList<LDAPPhoneBookEntry> entries = new ArrayList<LDAPPhoneBookEntry>();
    
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
            env.put(Context.PROVIDER_URL, "ldap://" + settings.serverName  + ":" + settings.port +  "/" + LDAPSettings.sanitizeDN(settings.baseDN));
            
            if (settings.useAuth) {
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, LDAPSettings.sanitizeDN(settings.bindDN));
                
                String password;
                if (settings.askForCredential) {
                    String[] pwd = PasswordDialog.showPasswordDialog(parentDialog, Utils._("LDAP password"), Utils._("Please enter the LDAP password:"), settings.bindDN, false);
                    if (pwd == null)
                        return;
                    else
                        password = pwd[1];
                } else {
                    password = settings.credential;
                }
                env.put(Context.SECURITY_CREDENTIALS, password);
            } else {
                env.put(Context.SECURITY_AUTHENTICATION, "none");
            }
            
            ctx = new InitialDirContext(env);
            
            if (settings.initiallyLoadAll) {
                loadEntries(entries, null, settings.countLimit);
            }
        } catch (NamingException e) {
            ctx = null;
            throw new PhoneBookException(e, false);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends PhoneBookEntry> void loadEntries(List<T> targetList, Filter<PhoneBookEntry,PBEntryField> pbFilter, int countLimit) throws NamingException {
        
        SearchControls sctl = new SearchControls();
        if (settings.searchSubTree) 
            sctl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        else
            sctl.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        
        sctl.setCountLimit(countLimit);
        sctl.setReturningAttributes(settings.getAttributes());
        
        String filter = getLDAPFilter(pbFilter);
        if (Utils.debugMode) {
            log.fine("LDAP filter: " + filter);
        }
        //System.out.println(filter);
        
        NamingEnumeration<SearchResult> res = ctx.search("", filter, sctl);
        while (res.hasMore()) {
            targetList.add((T)new LDAPPhoneBookEntry(this, res.next()));
        }
        res.close();
        
        Collections.sort(targetList);
    }

    @Override
    public List<PhoneBookEntry> applyFilter(
            Filter<PhoneBookEntry, PBEntryField> filter) {
        if (settings.initiallyLoadAll || filter == null) {
            return super.applyFilter(filter);
        } else {
            List<PhoneBookEntry> resultList = new ArrayList<PhoneBookEntry>();
            try {
                loadEntries(resultList, filter, settings.countLimit);
            } catch (NamingException e) {
                ExceptionDialog.showExceptionDialog(this.parentDialog, Utils._("Error executing the search:"), e);
            }
            lastFilterResult = resultList;
            return resultList;
        }
    }
    
    protected String getLDAPFilter(Filter<PhoneBookEntry,PBEntryField> pbFilter) {
        String initialFilter;
        if (settings.objectFilter == null || settings.objectFilter.length() == 0) {
            initialFilter = "(objectClass=*)";
        } else {
            initialFilter = settings.objectFilter;
        }
        
        if (pbFilter == null) {
            return initialFilter;
        } else {
            StringBuilder res = new StringBuilder();
            res.append("(&");
            if (initialFilter.charAt(0) == '(' && initialFilter.charAt(initialFilter.length() - 1) == ')') {
                res.append(initialFilter);
            } else {
                res.append('(').append(initialFilter).append(')');
            }
            appendLDAPSearchForFilter(res, pbFilter);
            res.append(')');
            return res.toString();
        }
    }
    
    protected static final Pattern tokenSplitter = Pattern.compile("(\\s|[,;])+");
    protected void appendLDAPSearchForFilter(StringBuilder appendTo, Filter<PhoneBookEntry,PBEntryField> filter) {
        if (filter instanceof AndFilter) { 
            AndFilter<PhoneBookEntry, PBEntryField> andFilter = (AndFilter<PhoneBookEntry, PBEntryField>)filter;
            appendTo.append('(');
            if (filter instanceof OrFilter) {
                appendTo.append('|');
            } else { // "plain" AndFilter
                appendTo.append('&');
            }
            for (Filter<PhoneBookEntry, PBEntryField> child : andFilter.getChildList()) {
                appendLDAPSearchForFilter(appendTo, child);
            }
            appendTo.append(')');
        } else if (filter instanceof StringFilter) {
            StringFilter<PhoneBookEntry, PBEntryField> strFilter = (StringFilter<PhoneBookEntry, PBEntryField>)filter;
            appendStringFilter(appendTo, strFilter.getColumn(), strFilter.getOperator(), strFilter.getCompareValue().toString());
        } else if (filter instanceof ConcatStringFilter) {
            ConcatStringFilter<PhoneBookEntry, PBEntryField> strFilter = (ConcatStringFilter<PhoneBookEntry, PBEntryField>)filter;
            String[] tokens = tokenSplitter.split(strFilter.getCompareValue().toString());
            if (tokens.length > 1) {
                appendTo.append("(&");
            }
            for (String token : tokens) {
                appendTo.append("(|");
                for (Object o : strFilter.getConcatVals()) {
                    if (o instanceof PBEntryField) {
                        appendStringFilter(appendTo, (PBEntryField)o, strFilter.getOperator(), token);
                    }
                }
                appendTo.append(')');
            }
            if (tokens.length > 1) {
                appendTo.append(')');
            }
        } else {
            log.severe("Unsupported filter type: " + filter.getClass());
            appendTo.append("(objectClass=*)");
        }
    }
    
    protected void appendStringFilter(StringBuilder appendTo, PBEntryField field, StringFilterOperator operator, String compVal) {
        String mapping = settings.getMappingFor(field);
        if (LDAPSettings.isNoField(mapping)) {
            appendTo.append("(objectClass=*)");
        } else {
            appendTo.append('(');
            switch (operator) {
            case CONTAINS:
                appendTo.append(mapping);
                appendTo.append("=*");
                appendEscaped(appendTo, compVal);
                appendTo.append('*');
                break;
            case ENDSWITH:
                appendTo.append(mapping);
                appendTo.append("=*");
                appendEscaped(appendTo, compVal);
                break;
            case STARTSWITH:
                appendTo.append(mapping);
                appendTo.append('=');
                appendEscaped(appendTo, compVal);
                appendTo.append('*');
                break;
            default:
                log.warning("Unsupported String operator " + operator.name()); 
                // Fall through intended!
            case EQUAL:
                appendTo.append(mapping);
                appendTo.append('=');
                appendEscaped(appendTo, compVal);
                break;
            case NOTEQUAL:
                appendTo.append("!(");
                appendTo.append(mapping);
                appendTo.append('=');
                appendEscaped(appendTo, compVal);
                appendTo.append(')');
                break;
            }
            appendTo.append(')');
        }
    }
    
    public static void appendEscaped(StringBuilder appendTo, String toAppend) {
        for (int i=0; i<toAppend.length(); i++) {
            char c = toAppend.charAt(i);
            switch (c) {
            case 0:
            case '*':
            case '(':
            case ')':
            case '\\':
                appendTo.append('\\').
                    append(Character.forDigit((c & 0xf), 16)).
                    append(Character.forDigit((c >> 4) & 0xf, 16));
                break;
            default:
                appendTo.append(c);
                break;
            }
        }
    }
    
    
    @Override
    public String getDisplayCaption() {
        if (settings.displayCaption != null && settings.displayCaption.length() > 0) {
            return settings.displayCaption;
        } else {
            String rv = PB_Prefix + ":" + settings.serverName + "/" + settings.baseDN;

            if (rv.length() > CAPTION_LENGTH)
                return rv.substring(0, CAPTION_LENGTH-3) + "...";
            else
                return rv;
        }
    }
    
    @Override
    public void resort() {
        Collections.sort(entries);
    }

    private List<PhoneBookEntry> itemsView = Collections.<PhoneBookEntry>unmodifiableList(entries);
    @Override
    public List<PhoneBookEntry> getEntries() {
        return itemsView;
    }


    public LDAPPhoneBook(Dialog parent) {
        super(parent);
       
    }
}
