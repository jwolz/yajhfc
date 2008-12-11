package yajhfc.phonebook;
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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLPhoneBookEntry extends SimplePhoneBookEntry {
    private static final Logger log = Logger.getLogger(XMLPhoneBookEntry.class.getName());
        
    private XMLPhoneBook parent;
    
    public XMLPhoneBookEntry(XMLPhoneBook parent) {
        this.parent = parent;
    }
    
    @Override
    public void commit() {
        if (dirty) {
            parent.writeEntry(this);
            dirty = false;
        }
    }
    
    @Override
    public void delete() {
        parent.deleteEntry(this);
    }
    
    public void saveToXML(Element el, Document doc) {
        for (PBEntryField field : PBEntryField.values()) {
            try {
                String val = getField(field);
                if (val == null)
                    continue;
                
                Element dataEl = doc.createElement(field.getKey());
                dataEl.setTextContent(val);
                el.appendChild(dataEl);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error writing element " + field + ": ", e);
            }
        }
    }
    
    public void loadFromXML(Element el) {
        NodeList nl = el.getChildNodes();
        Map<String,PBEntryField> keyToFieldMap = PBEntryField.getKeyToFieldMap();
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item.getNodeType() == Node.ELEMENT_NODE)) {
                try {
                    PBEntryField field = keyToFieldMap.get(item.getNodeName());
                    if (field == null) {
                        log.warning("Unknown field: " + item.getNodeName());
                    } else {
                        setFieldUndirty(field, item.getTextContent());
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
}