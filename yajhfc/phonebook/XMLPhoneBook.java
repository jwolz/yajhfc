package yajhfc.phonebook;
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

import java.awt.Dialog;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFileChooser;
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

import yajhfc.ExampleFileFilter;
import yajhfc.ExceptionDialog;
import yajhfc.utils;

public class XMLPhoneBook extends PhoneBook {

    public static final String PB_Prefix = "XML";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = utils._("XML Phonebook"); // A user-readable name for this Phonebook type
    public static final String PB_Description = utils._("A Phonebook saving its entries as a XML file."); // A user-readable description of this Phonebook type
    
    private ArrayList<XMLPhoneBookEntry> list;
    private String fileName;
    private boolean isOpened = false;
    
    public void resort() {
        Collections.sort(list, DefaultPhoneBookEntryComparator.globalInstance);
    }
    
    private int getInsertionPos(PhoneBookEntry pbe) {
        int res = Collections.binarySearch(list, pbe, DefaultPhoneBookEntryComparator.globalInstance);
        if (res >= 0) // Element found?
            return res + 1;
        else
            return -(res + 1);
    }
    
    @Override
    public PhoneBookEntry addNewEntry() {
        XMLPhoneBookEntry pb = new XMLPhoneBookEntry(this);
        int pos = getInsertionPos(pb);
        list.add(pos, pb);
        fireIntervalAdded(this, pos, pos);
        return pb;
    }

    void deleteEntry(PhoneBookEntry entry) {
        int index = list.indexOf(entry);
        list.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    void writeEntry(PhoneBookEntry entry) {
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
        JFileChooser jfc = new yajhfc.util.SafeJFileChooser(fileName);
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        ExampleFileFilter ff = new ExampleFileFilter("phonebook", utils._("Phonebook files"));
        jfc.addChoosableFileFilter(ff);
        jfc.addChoosableFileFilter(jfc.getAcceptAllFileFilter());
        jfc.setFileFilter(ff);
        
        if (jfc.showOpenDialog(parentDialog) == JFileChooser.APPROVE_OPTION)
            return PB_Prefix + ":" + jfc.getSelectedFile().getPath();
        else
            return null;
    }

    @Override
    public void close() {
        if (!isOpen())
            return;
        
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
            StreamResult result = new StreamResult(new FileOutputStream(fileName));
            transformer.transform(source, result);
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, utils._("Error saving the phone book: "), e);
        } 
        isOpened = false;
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
                XMLPhoneBookEntry entry = new XMLPhoneBookEntry(this);
                entry.loadFromXML((Element)item);
                list.add(entry);
            }
        }
        
        resort();
        fireContentsChanged(this, 0, list.size() - 1);
    }
    
    @Override
    protected void openInternal(String descriptor) throws PhoneBookException {       
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
        if (!file.exists()) {
            isOpened = true;
            return;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(file);
            
            Element root = doc.getDocumentElement();
            
            loadFromXML(root);
        } catch (Exception e) {
            throw new PhoneBookException(e, false);
        } 
        
        isOpened = true;
    }

    @Override
    public boolean isOpen() {
        return isOpened;
    }
    
    @Override
    public String getDisplayCaption() {
        String rv = PB_Prefix + ":";
        int desiredLen = CAPTION_LENGTH - rv.length();
        
        if (fileName.length() > desiredLen)
            rv += "..." + fileName.substring(fileName.length() - desiredLen + 3);
        else
            rv += fileName;
        
        return rv;
    }
    
    public XMLPhoneBook(Dialog parent) {
        super(parent);
        
        list = new ArrayList<XMLPhoneBookEntry>();
    }

}

