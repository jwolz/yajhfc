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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

import yajhfc.Utils;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;

public class XMLPhoneBook extends PhoneBook {

    private static final Logger log = Logger.getLogger(XMLPhoneBook.class.getName());
    
    public static final String PB_Prefix = "XML";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = Utils._("XML Phonebook"); // A user-readable name for this Phonebook type
    public static final String PB_Description = Utils._("A Phonebook saving its entries as a XML file."); // A user-readable description of this Phonebook type
    
    private ArrayList<XMLPhoneBookEntry> list;
    private XMLSettings settings;
    private boolean isOpened = false;
    private boolean wasChanged = false;
    
    protected final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    public void resort() {
        Collections.sort(list);
    }
    
    private int getInsertionPos(PhoneBookEntry pbe) {
        int res = Collections.binarySearch(list, pbe);
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
        fireEntriesAdded(pos, pb);
        wasChanged = true;
        return pb;
    }

    void deleteEntry(PhoneBookEntry entry) {
        int index = Utils.identityIndexOf(list, entry);
        if (index >= 0) {
            list.remove(index);
            fireEntriesRemoved(index, entry);
            wasChanged = true;
        }
    }

    void writeEntry(PhoneBookEntry entry) {
        int oldpos = Utils.identityIndexOf(list, entry);
        list.remove(oldpos);
        int pos = getInsertionPos(entry);
        list.add(pos, (XMLPhoneBookEntry)entry);
        fireEntriesChanged(eventObjectForInterval(oldpos, pos));
        wasChanged = true;
    }

    private List<PhoneBookEntry> itemsView;
    @Override
    public List<PhoneBookEntry> getEntries() {
        return itemsView;
    }

    @Override
    public String browseForPhoneBook() {
        return new PBBrowser(settings).browseForPhoneBook(parentDialog).saveToString();
    }

    @Override
    public void close() {
        if (!isOpen())
            return;
        if (wasChanged) {
            try {
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
                StreamResult result = new StreamResult(new FileOutputStream(settings.fileName));
                transformer.transform(source, result);
                wasChanged = false;
            } catch (Exception e) {
                ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Error saving the phone book: "), e);
            }
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
        if (list.size() > 0) {
            PhonebookEvent pbe = eventObjectForInterval(0, list.size() - 1);
            list.clear();
            fireEntriesRemoved(pbe);
        }
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item.getNodeType() == Node.ELEMENT_NODE)) {
                String nodeName = item.getNodeName();
                if (nodeName.equals("entry")) {
                    XMLPhoneBookEntry entry = new XMLPhoneBookEntry(this);
                    entry.loadFromXML((Element)item);
                    list.add(entry);
                } else {
                    log.warning("Unknown node: " + nodeName);
                }
            }
        }
        
        resort();
        if (list.size() > 0) {
            fireEntriesAdded(eventObjectForInterval(0, list.size() - 1));
        }
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

        settings = new XMLSettings();
        settings.loadFromString(descriptor);
        
        reloadEntries();
        isOpened = true;
    }

    private void reloadEntries() throws PhoneBookException {
        list.clear();
        
        File file = new File(settings.fileName);
        if (!file.exists()) {
            isOpened = true;
            return;
        }
        
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(file);
            
            Element root = doc.getDocumentElement();
            
            loadFromXML(root);
            
            wasChanged = false;
        } catch (Exception e) {
            throw new PhoneBookException(e, false);
        } 
    }
    
    @Override
    public boolean isOpen() {
        return isOpened;
    }
    
    @Override
    public String getDisplayCaption() {
        String caption = settings.caption;
        if (caption != null && caption.length() > 0) {
            return caption;
        } else {
            return Utils.shortenFileNameForDisplay(settings.fileName, CAPTION_LENGTH);
        }
    }
    
    public XMLPhoneBook(Dialog parent) {
        super(parent);
        
        list = new ArrayList<XMLPhoneBookEntry>();
        itemsView = Collections.<PhoneBookEntry>unmodifiableList(list);
    }

    static class PBBrowser extends SafeJFileChooser {
        
        private JTextField textCaption;
        private JPanel bottomPanel;
        
        public PBBrowser(XMLSettings settings) {
            super();
            
            removeChoosableFileFilter(getAcceptAllFileFilter());
            ExampleFileFilter ff = new ExampleFileFilter("phonebook", Utils._("Phonebook files"));
            addChoosableFileFilter(ff);
            addChoosableFileFilter(getAcceptAllFileFilter());
            setFileFilter(ff);
            
            
            bottomPanel = new JPanel(false);
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(Utils._("Phonebook name to display:")),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            
//            JLabel captionLabel = new JLabel(Utils._("Phonebook name to display:"));
//            captionLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            
            textCaption = new JTextField();
            
            //bottomPanel.add(captionLabel);
            bottomPanel.add(textCaption);
            
            if (settings != null) {
                setSelectedFile(new File(settings.fileName));
                textCaption.setText(settings.caption);
            }
        }
        
        @Override
        protected JDialog createDialog(Component parent)
                throws HeadlessException {
            JDialog dialog = super.createDialog(parent);
            JPanel newContentPane = new JPanel(new BorderLayout(), false);
            newContentPane.add(dialog.getContentPane(), BorderLayout.CENTER);
            newContentPane.add(bottomPanel, BorderLayout.SOUTH);
            dialog.setContentPane(newContentPane);
            return dialog;
        }
        
        /**
         * Browse for phonebook.
         * Returns true if 
         * @return
         */
        public XMLSettings browseForPhoneBook(Component parentDialog) {
            if (showOpenDialog(parentDialog) == JFileChooser.APPROVE_OPTION) {
                XMLSettings settings = new XMLSettings();
                settings.fileName = getSelectedFile().getAbsolutePath();
                
                settings.caption = textCaption.getText().trim();
                
                return settings;
            } else {
                return null;
            }
        }
    }
}

