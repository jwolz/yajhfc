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
        java.lang.reflect.Field[] f = XMLPhoneBookEntry.class.getFields();
        
        for (int i = 0; i < f.length; i++) {
            try {
                Object val = f[i].get(this);
                if (val == null)
                    continue;
                
                Element dataEl = doc.createElement(f[i].getName());
                dataEl.setTextContent(val.toString());
                el.appendChild(dataEl);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error writing element " + f[i].getName() + ": ", e);
            }
        }
    }
    
    public void loadFromXML(Element el) {
        NodeList nl = el.getChildNodes();
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item.getNodeType() == Node.ELEMENT_NODE)) {
                try {
                    java.lang.reflect.Field f = XMLPhoneBookEntry.class.getField(item.getNodeName());
                    f.set(this, item.getTextContent());
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error reading element " + item.getNodeName() + ": ", e);
                }
            }
        }
        dirty = false;
    }
}