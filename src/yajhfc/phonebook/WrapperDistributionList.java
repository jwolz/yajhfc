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
package yajhfc.phonebook;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import yajhfc.Utils;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.xml.XMLPhoneBook;

/**
 * Implementation of a distribution list by wrapping another entry and
 * storing the content as XML in the "Comment" field.
 * 
 * @author jonas
 *
 */
public class WrapperDistributionList extends DefaultPhoneBookEntry implements
        DistributionList {
    private static final Logger log = Logger.getLogger(WrapperDistributionList.class.getName());
    
    protected PhoneBookEntry wrapped;
    protected XMLPhoneBook itemsList;
    
    public static final PBEntryField SIGNATURE_FIELD = PBEntryField.Company;
    public static final String SIGNATURE = "@!$DistributionList$!@";
    public static final int MIN_COMMENT_LEN = 5000;
    
    public WrapperDistributionList(PhoneBookEntry wrapped) {
        this.wrapped = wrapped;
    }

    protected XMLPhoneBook getItemsList() {
        if (itemsList == null) {
            itemsList = new XMLPhoneBook(getParent().parentDialog);
            String comments = wrapped.getField(PBEntryField.Comment);
            if (comments != null && comments.length() > 0) {
                try {
                    itemsList.loadFromInputSource(new InputSource(new StringReader(comments)));
                } catch (Exception e) {
                    log.log(Level.WARNING, "Invalid distribution list:", e);
                }
            }
        }
        return itemsList;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#commit()
     */
    @Override
    public void commit() {
        if (itemsList != null && itemsList.wasChanged()) {
            try {
                StringWriter xmlWriter = new StringWriter();
                itemsList.saveToResult(new StreamResult(xmlWriter));
                wrapped.setField(PBEntryField.Comment, xmlWriter.toString());
                wrapped.setField(SIGNATURE_FIELD, SIGNATURE);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error saving distribution list:", e);
            } 
        }
        wrapped.commit();
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#delete()
     */
    @Override
    public void delete() {
        wrapped.delete();
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#getField(yajhfc.phonebook.PBEntryField)
     */
    @Override
    public String getField(PBEntryField field) {
        if (field == PBEntryField.Name) {
            return wrapped.getField(field);
        } else {
            return "";
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#getParent()
     */
    @Override
    public PhoneBook getParent() {
        return wrapped.getParent();
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#setField(yajhfc.phonebook.PBEntryField, java.lang.String)
     */
    @Override
    public void setField(PBEntryField field, String value) {
        if (field == PBEntryField.Name) {
            wrapped.setField(field, value);
        } else {
            // NOP
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#addEntries(java.util.Collection)
     */
    public void addEntries(Collection<? extends PBEntryFieldContainer> items) {
        getItemsList().addEntries(items);
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#addNewEntry()
     */
    public PhoneBookEntry addNewEntry() {
        return getItemsList().addNewEntry();
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#addNewEntry(yajhfc.phonebook.convrules.PBEntryFieldContainer)
     */
    public PhoneBookEntry addNewEntry(PBEntryFieldContainer item) {
        return getItemsList().addNewEntry(item);
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#addPhonebookEventListener(yajhfc.phonebook.PhonebookEventListener)
     */
    public void addPhonebookEventListener(PhonebookEventListener pel) {
        getItemsList().addPhonebookEventListener(pel);
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#getEntries()
     */
    public List<PhoneBookEntry> getEntries() {
        return getItemsList().getEntries();
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#isReadOnly()
     */
    public boolean isReadOnly() {
        return getParent().isReadOnly();
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBookEntryList#removePhonebookEventListener(yajhfc.phonebook.PhonebookEventListener)
     */
    public void removePhonebookEventListener(PhonebookEventListener pel) {
        getItemsList().removePhonebookEventListener(pel);
    }

    @Override
    public String toString() {
        String name = wrapped.getField(PBEntryField.Name);
        return (name == null || name.length() == 0) ? Utils._("<no name>") : name ;
    }
    
    @Override
    public void updateDisplay() {
        wrapped.updateDisplay();
    }
    
    public PhoneBookEntry getWrappedEntry() {
        return wrapped;
    }
    
    /**
     * Checks if the given phone book supports distribution lists created using this
     * wrapper class.
     * @param pb
     * @return
     */
    public static boolean areDistributionListsSupported(PhoneBook pb) {
        int commentLen = pb.getMaxLength(PBEntryField.Comment);
        int signatureLen = pb.getMaxLength(SIGNATURE_FIELD);
        return (pb.isFieldAvailable(PBEntryField.Name) && pb.isFieldAvailable(PBEntryField.Comment) && pb.isFieldAvailable(SIGNATURE_FIELD)) &&
            (( commentLen == 0 || commentLen >= MIN_COMMENT_LEN ) && ( signatureLen == 0 || signatureLen >= SIGNATURE.length() ));
    }
    
    /**
     * Checks if the given PhoneBookEntry contains a distribution list created
     * with this class.
     * @param pbe
     * @return
     */
    public static boolean isDistributionList(PhoneBookEntry pbe) {
        return SIGNATURE.equals(pbe.getField(SIGNATURE_FIELD));
    }
}
