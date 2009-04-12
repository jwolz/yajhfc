package yajhfc.phonebook.xml;
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
import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import yajhfc.Utils;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookException;
import yajhfc.phonebook.PhonebookEvent;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;

public class XMLPhoneBook extends PhoneBook {

    private static final Logger log = Logger.getLogger(XMLPhoneBook.class.getName());
    
    public static final String PB_Prefix = "XML";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = Utils._("XML phone book"); // A user-readable name for this Phonebook type
    public static final String PB_Description = Utils._("A phone book saving its entries as a XML file."); // A user-readable description of this Phonebook type
    public static final boolean PB_CanExport = true;   // Can the phone book used to export entries?
    
    private ArrayList<XMLPhoneBookEntry> list;
    private XMLSettings settings;
    private boolean isOpened = false;
    private boolean wasChanged = false;
    protected final boolean allowDistLists;
    
    protected static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    protected static TransformerFactory TRANSFORMER_FACTORY;
    
    protected static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        if (DOCUMENT_BUILDER_FACTORY == null) {
            DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        }
        return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
    }
    
    protected static TransformerFactory getTransformerFactory() {
        if (TRANSFORMER_FACTORY == null) {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
        }
        return TRANSFORMER_FACTORY;
    }
    
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
        XMLPhoneBookEntry pb = new SingleXMLPhoneBookEntry(this);
        addEntryGeneral(pb);
        return pb;
    }

    @Override
    public DistributionList addDistributionList() {
        if (!allowDistLists) 
            throw new UnsupportedOperationException("No distribution lists allowed.");
        
        XMLDistributionList pb = new XMLDistributionList(this);
        addEntryGeneral(pb);
        return pb;
    }
    
    private void addEntryGeneral(XMLPhoneBookEntry pb) {
        int pos = getInsertionPos(pb);
        list.add(pos, pb);
        fireEntriesAdded(pos, pb);
        wasChanged = true;
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
    public String browseForPhoneBook(boolean exportMode) {
        XMLSettings newSettings = new PBBrowser(settings, exportMode).browseForPhoneBook(parentDialog);
        if (newSettings == null) {
            return null;
        } else {
            return PB_Prefix + ":" + newSettings.saveToString();
        }
    }

    @Override
    public void close() {
        if (!isOpen())
            return;
        if (wasChanged) {
            try {
                saveToResult(new StreamResult(new FileOutputStream(settings.fileName)));
            } catch (Exception e) {
                ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Error saving the phone book: "), e);
            }
        }
        isOpened = false;
    }

    public void saveToResult(Result result) throws ParserConfigurationException, TransformerException {
        Document doc = createDocumentBuilder().newDocument();

        Element root = doc.createElement("phonebook");
        doc.appendChild(root);
        saveToXML(root, doc);

        root.normalize();
        
        TransformerFactory tFactory = getTransformerFactory();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);

        transformer.transform(source, result);
    }
    
    public void saveToXML(Element el, Document doc) {
        for (XMLPhoneBookEntry entry : list) {
            String entryName;
            if (entry instanceof DistributionList) {
                entryName = "distributionlist";
            } else {
                entryName = "entry";
            }
            Element ent = doc.createElement(entryName);
            entry.saveToXML(ent, doc);
            el.appendChild(ent);
        }
        wasChanged = false;
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
                    SingleXMLPhoneBookEntry entry = new SingleXMLPhoneBookEntry(this);
                    entry.loadFromXML((Element)item);
                    list.add(entry);
                } else if (nodeName.equals("distributionlist")) {
                    XMLDistributionList entry = new XMLDistributionList(this);
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
        wasChanged = false;
    }
    
    @Override
    protected void openInternal(String descriptor) throws PhoneBookException {       
/*        for (int i = 0; i < 20; i++) {
            SingleXMLPhoneBookEntry pb = new SingleXMLPhoneBookEntry();
            
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

    public void loadFromInputSource(InputSource source) throws ParserConfigurationException, SAXException, IOException {
        list.clear();
        
        DocumentBuilder builder = createDocumentBuilder();
        
        Document doc = builder.parse(source);
        
        Element root = doc.getDocumentElement();
        
        loadFromXML(root);
    }
    
    private void reloadEntries() throws PhoneBookException {
        list.clear();
        
        File file = new File(settings.fileName);
        if (!file.exists()) {
            return;
        }
        
        try {
            loadFromInputSource(new InputSource(file.toURI().toString()));
        } catch (Exception e) {
            throw new PhoneBookException(e, false);
        } 
    }
    
    public boolean wasChanged() {
        return wasChanged; 
    }
    
    @Override
    public boolean isOpen() {
        return isOpened;
    }
    
    @Override
    public boolean supportsDistributionLists() {
        return allowDistLists;
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
    
    public XMLPhoneBook(Dialog parent, boolean allowDistLists) {
        super(parent);
        this.allowDistLists = allowDistLists;
        
        list = new ArrayList<XMLPhoneBookEntry>();
        itemsView = Collections.<PhoneBookEntry>unmodifiableList(list);
    }
    
    public XMLPhoneBook(Dialog parent) {
        this(parent, true);
    }

    static class PBBrowser extends SafeJFileChooser {
        
        private JTextField textCaption;
        private JPanel bottomPanel;
        private final boolean exportMode;
        
        public PBBrowser(XMLSettings settings, boolean exportMode) {
            super();
            
            this.exportMode = exportMode;
            
            removeChoosableFileFilter(getAcceptAllFileFilter());
            ExampleFileFilter ff = new ExampleFileFilter("phonebook", Utils._("Phone book files"));
            addChoosableFileFilter(ff);
            addChoosableFileFilter(getAcceptAllFileFilter());
            setFileFilter(ff);
            
            if (!exportMode) {
                bottomPanel = new JPanel(false);
                bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
                bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(Utils._("Phone book name to display:")),
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
        }
        
        @Override
        protected JDialog createDialog(Component parent)
                throws HeadlessException {
            JDialog dialog = super.createDialog(parent);
            if (!exportMode) {
                JPanel newContentPane = new JPanel(new BorderLayout(), false);
                newContentPane.add(dialog.getContentPane(), BorderLayout.CENTER);
                newContentPane.add(bottomPanel, BorderLayout.SOUTH);
                dialog.setContentPane(newContentPane);
            }
            return dialog;
        }
        
        /**
         * Browse for phonebook.
         * Returns setting if the user selected "Open" or null otherwise
         * @return
         */
        public XMLSettings browseForPhoneBook(Component parentDialog) {
            setDialogType(exportMode ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
            if (showDialog(parentDialog, null) == JFileChooser.APPROVE_OPTION) {
                XMLSettings settings = new XMLSettings();
                settings.fileName = getSelectedFile().getAbsolutePath();
                
                if (textCaption != null)
                    settings.caption = textCaption.getText().trim();
                
                return settings;
            } else {
                return null;
            }
        }
    }
}

