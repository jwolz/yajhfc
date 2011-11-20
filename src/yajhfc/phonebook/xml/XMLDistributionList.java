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
package yajhfc.phonebook.xml;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import yajhfc.Utils;
import yajhfc.phonebook.DefaultPhoneBookEntry;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhonebookEventListener;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * @author jonas
 *
 */
public class XMLDistributionList extends DefaultPhoneBookEntry implements
        XMLPhoneBookEntry, DistributionList {
    private static final Logger log = Logger.getLogger(XMLDistributionList.class.getName());
    
    private XMLPhoneBook parent;
    private String name = "";
    private XMLPhoneBook items;
    private boolean dirty = false;
    
    public XMLDistributionList(XMLPhoneBook parent) {
        this.parent = parent;
        items = new XMLPhoneBook(parent.parentDialog);
    }
    
    @Override
    public void commit() {
        for (PhoneBookEntry pbe : items.getEntries().toArray(new PhoneBookEntry[items.getEntries().size()])) {
            pbe.commit();
        }
        if (dirty || items.wasChanged()) {
            parent.writeEntry(this);
            dirty = false;
        }
    }
    
    @Override
    public void delete() {
        parent.deleteEntry(this);
    }
    
    public void saveToXML(Element el, Document doc) {
        try {
            Element dataEl = doc.createElement("name");
            dataEl.setTextContent(name);
            el.appendChild(dataEl);
            
            dataEl = doc.createElement("items");
            items.saveToXML(dataEl, doc);
            el.appendChild(dataEl);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error saving distribution list: ", e);
        }
        dirty = false;
    }
    
    public void loadFromXML(Element el) {
        NodeList nl = el.getChildNodes();
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item.getNodeType() == Node.ELEMENT_NODE)) {
                try {
                    String nodeName = item.getNodeName();
                    if ("name".equals(nodeName)) {
                        name = item.getTextContent();
                    } else if ("items".equals(nodeName)) {
                        items.loadFromXML((Element)item);
                    } else {
                        log.warning("Unknown element: " + nodeName);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error reading element " + item.getNodeName() + ": ", e);
                }
            }
        }
        dirty = false;
    }

    @Override
    public PhoneBook getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#getField(yajhfc.phonebook.PBEntryField)
     */
    @Override
    public String getField(PBEntryField field) {
        if (field == PBEntryField.Name) {
            return name;
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DefaultPhoneBookEntry#setField(yajhfc.phonebook.PBEntryField, java.lang.String)
     */
    @Override
    public void setField(PBEntryField field, String value) {
        if (field == PBEntryField.Name) {
            if (!name.equals(value)) {
                name = value;
                dirty = true;
            }
        } else {
            // NOP
        }
    }
    
    @Override
    public String toString() {
        return (name == null || name.length() == 0) ? Utils._("<no name>") : name ;
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.DistributionList#getItems()
     */
    public List<PhoneBookEntry> getEntries() {
        return items.getEntries();
    }

    public PhoneBookEntry addNewEntry(PBEntryFieldContainer item) {
        return items.addNewEntry(item);
    }

    public void addEntries(Collection<? extends PBEntryFieldContainer> items) {
        this.items.addEntries(items);
    }

    public PhoneBookEntry addNewEntry() {
        return items.addNewEntry();
    }

    public void addPhonebookEventListener(PhonebookEventListener pel) {
        items.addPhonebookEventListener(pel);
    }

    public void removePhonebookEventListener(PhonebookEventListener pel) {
        items.removePhonebookEventListener(pel);
    }
    
    public boolean isReadOnly() {
        return parent.isReadOnly();
    }
}
