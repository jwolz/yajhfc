package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLPhoneBook extends PhoneBook {

    private ArrayList<XMLPhoneBookEntry> list;
    private String fileName;
    private DefaultPhoneBookEntryComparator pbeComparator;
    
    public void reSort() {
        Collections.sort(list, pbeComparator);
    }
    
    private int getInsertionPos(PhoneBookEntry pbe) {
        int res = Collections.binarySearch(list, pbe, pbeComparator);
        if (res >= 0) // Element found?
            return res + 1;
        else
            return -(res + 1);
    }
    
    @Override
    public PhoneBookEntry addNewEntry() {
        XMLPhoneBookEntry pb = new XMLPhoneBookEntry();
        int pos = getInsertionPos(pb);
        list.add(pos, pb);
        fireIntervalAdded(this, pos, pos);
        return pb;
    }

    @Override
    public void deleteEntry(PhoneBookEntry entry) {
        int index = list.indexOf(entry);
        list.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public void writeEntry(PhoneBookEntry entry) {
        int oldpos = list.indexOf(entry);
        list.remove(oldpos);
        int pos = getInsertionPos(entry);
        list.add(pos, (XMLPhoneBookEntry)entry);
        fireContentsChanged(this, oldpos, pos);
    }

    public Object getElementAt(int index) {
        return list.get(index);
    }

    public int getSize() {
        return list.size();
    }

    @Override
    public String browseForPhoneBook() {
        JFileChooser jfc = new JFileChooser(fileName);
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        ExampleFileFilter ff = new ExampleFileFilter("phonebook", utils._("Phonebook files"));
        jfc.addChoosableFileFilter(ff);
        jfc.addChoosableFileFilter(jfc.getAcceptAllFileFilter());
        jfc.setFileFilter(ff);
        
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            return jfc.getSelectedFile().getPath();
        else
            return null;
    }

    @Override
    public void close() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.newDocument();
            
            Element root = doc.createElement("phonebook");
            doc.appendChild(root);
            saveToXML(root, doc);
            
            root.normalize();
            TransformerFactory tFactory =
                TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, utils._("Error saving the phone book: ") + e.getLocalizedMessage());
        }
    }

    public void saveToXML(Element el, Document doc) {
        for (XMLPhoneBookEntry entry : list) {
            Element ent = doc.createElement("entry");
            entry.saveToXML(ent, doc);
            el.appendChild(ent);
        }
    }
    
    public void loadFromXML(Element el) {
        NodeList nl = el.getChildNodes();
        list.clear();
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item.getNodeType() == Node.ELEMENT_NODE) && (item.getNodeName().equals("entry"))) {
                XMLPhoneBookEntry entry = new XMLPhoneBookEntry();
                entry.loadFromXML((Element)item);
                list.add(entry);
            }
        }
        
        reSort();
        fireContentsChanged(this, 0, list.size() - 1);
    }
    
    @Override
    public void open(String descriptor) {       
/*        for (int i = 0; i < 20; i++) {
            XMLPhoneBookEntry pb = new XMLPhoneBookEntry();
            
            pb.surname = "Müller";
            pb.faxnumber = "01234/56789";
            pb.company = "foobar AG";
            pb.givenname = "Number " + i; 
            list.add(pb);
        } */

        fileName = descriptor;
        list.clear();
        
        File file = new File(fileName);
        if (!file.exists())
            return;
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(file);
            
            Element root = doc.getDocumentElement();
            
            loadFromXML(root);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, utils._("Error loading the phone book: ") + e.getLocalizedMessage());
        }
        
    }

    public XMLPhoneBook() {
        list = new ArrayList<XMLPhoneBookEntry>();
        pbeComparator = new DefaultPhoneBookEntryComparator();
    }

}

class XMLPhoneBookEntry extends PhoneBookEntry {
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
                System.err.println("Error writing element " + f[i].getName() + ": " + e.toString());
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
                     System.err.println("Error reading element " + item.getNodeName() + ": " + e.toString());
                }
            }
        }
    }
}
