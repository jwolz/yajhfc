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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.ClipboardPopup;
import yajhfc.ExceptionDialog;
import yajhfc.utils;

public class PhoneBookPanel extends JPanel 
    implements ListSelectionListener, ActionListener {

        private JSplitPane bottomPane;

        private JTextField textDescriptor;
        private JButton buttonBrowse, /*buttonClose,*/ buttonSelect;
        
        private JPanel leftPane;
        private JPanel rightPane;
        
        private JScrollPane scrollEntries;
        private JList listEntries;
        private JButton buttonAdd, buttonDel;
        
        private JTextField textSurname, textGivenname, textTitle, textCompany, textLocation;
        private JTextField textVoicenumber, textFaxnumber;
        private JScrollPane scrollComment;
        private JTextArea textComment;
        
        private PhoneBook phoneBook;
        private PhoneBookEntry oldEntry = null;
        private EntryTextFieldListener entryListener;
        
        private ClipboardPopup defClPop;
        
        private static final double border = 5;
        
        private boolean deleteOK = false, writeOK = false, browseOK = false;
        
        private EventListenerList enableList = new EventListenerList();
        
        public void openPhoneBook(String descriptor) {
            closePhoneBook();
            
            textDescriptor.setText(descriptor);
            
            try {
                if (descriptor != null && descriptor.length() > 0) {
                    phoneBook = PhoneBookFactory.instanceForDescriptor(descriptor, findParentDlg());
                    if (phoneBook == null) {
                        JOptionPane.showMessageDialog(this, utils._("Unknown Phonebook type selected."), utils._("Error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        phoneBook.open(descriptor);
                    }
                }
            } catch (PhoneBookException e) {
                if (!e.messageAlreadyDisplayed())
                    ExceptionDialog.showExceptionDialog(findParentDlg(), utils._("Error loading the phone book: "), e);
                return; // do nothing...
            }

            
            textDescriptor.setText(phoneBook.getDescriptor());
            textDescriptor.setCaretPosition(0);
            
            oldEntry = null;
            
            listEntries.setModel(phoneBook);
            
            enabledChange(!phoneBook.isReadOnly(), true);
            
            if (phoneBook.getSize() > 0) {
                listEntries.setSelectedIndex(0);
            } else {
                deleteEnableChanged(false);
                
                writeToTextFields(null); // disable text fields
            }
        }
        
        public void closePhoneBook() {
            
            oldEntry = null;
            
            if (phoneBook != null) 
                phoneBook.close();
            
            writeToTextFields(null);
            enabledChange(false, false);
        }
        
        public void importPhoneBook(PhoneBook pb, String descriptor) {
            try {
                if (pb == null) {
                    pb = PhoneBookFactory.instanceForDescriptor(descriptor, findParentDlg());
                }
                if (pb == null) {
                    JOptionPane.showMessageDialog(this, utils._("Unsupported phone book format."), utils._("Error"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (descriptor != null) 
                    pb.open(descriptor); 
                
                for (int i=0; i < pb.getSize(); i++) {
                    PhoneBookEntry pbe = phoneBook.addNewEntry();
                    pbe.copyFrom(pb.readEntry(i));
                }
                
                if (descriptor != null) // Phone book has been opened above...
                    pb.close();
                
                phoneBook.resort();
            } catch (PhoneBookException e) {
                if (!e.messageAlreadyDisplayed())
                    ExceptionDialog.showExceptionDialog(findParentDlg(), utils._("Error loading the phone book: "), e);
            }
        }
        
        public void addEnableListener(PBPanelListener el) {
            enableList.add(PBPanelListener.class, el);
        }
        
        public void removeEnableListener(PBPanelListener el) {
            enableList.remove(PBPanelListener.class, el);
        }
        
        public PhoneBook getPhoneBook() {
            return phoneBook;
        }
        
        public void selectPhoneBookEntry(int index) {
            listEntries.setSelectedIndex(index);
        }
        
        public int getSelectedPBEntryIndex() {
            return listEntries.getSelectedIndex();
        }
        
        public PhoneBookEntry getSelectedPBEntry() {
            return (PhoneBookEntry)listEntries.getSelectedValue();
        }
        
        public void addEntry() {
            if (!writeOK || phoneBook.isReadOnly()) 
                return;
            
            PhoneBookEntry pb = phoneBook.addNewEntry();
            listEntries.setSelectedValue(pb, true);
        }
        
        public void deleteCurrentEntry() {
            if (!deleteOK || phoneBook.isReadOnly()) 
                return;
            
            PhoneBookEntry pb = (PhoneBookEntry)listEntries.getSelectedValue();
            if (pb != null)
                if (JOptionPane.showConfirmDialog(this, MessageFormat.format(_("Do you want to delete the entry for \"{0}\"?"), pb), "Delete entry", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    oldEntry = null;
                    pb.delete();
                    writeToTextFields(null);
                }
        }
        
        public void browseForPhonebook() {
            String newPB = phoneBook.browseForPhoneBook();
            if (newPB != null) {
                //textDescriptor.setText(newPB);
                openPhoneBook(newPB);
            }
        }
        
        public boolean isDeleteOK() {
            return deleteOK;
        }
        public boolean isWriteOK() {
            return writeOK;
        }
        public boolean isBrowseOK() {
            return browseOK;
        }
        
        /**
         * Should return a "sensible" title for this phonebook's
         * tab. It should be shortened to 30 characters or less (in a "sensible way").
         * @return
         */
        public String getSensibleTitle() {
            PhoneBook pb = getPhoneBook();
            if (pb != null)
                return pb.getDisplayCaption();
            else
                return utils._("No phone book");
        }
        
        public void setSelectButtonVisible(boolean state) {
            buttonSelect.setVisible(state);
        }
        
        private void deleteEnableChanged(boolean deleteOK) {
            this.deleteOK = deleteOK;
            buttonDel.setEnabled(deleteOK);
            
            EnableEventObj eeo = new EnableEventObj(this, deleteOK, writeOK, browseOK);
            for (PBPanelListener el : enableList.getListeners(PBPanelListener.class)) {
                el.deleteOKChanged(eeo);
            }
        }
        
        private void enabledChange(boolean writeOK, boolean browseOK) {
            //entryMenu.setEnabled(false); 
            //importMenu.setEnabled(false); 
            
            this.deleteOK = deleteOK && writeOK; 
            this.writeOK = writeOK;
            this.browseOK = browseOK;
            
            buttonDel.setEnabled(deleteOK);
            
            buttonAdd.setEnabled(writeOK);
            
            buttonBrowse.setEnabled(browseOK);
            buttonSelect.setEnabled(browseOK);
            
            EnableEventObj eeo = new EnableEventObj(this, deleteOK, writeOK, browseOK);
            for (PBPanelListener el : enableList.getListeners(PBPanelListener.class)) {
                el.phoneBookStateChanged(eeo);
            }
        }
        
        private void fireItemSelected() {
            EventObject eo = new EventObject(this);
            for (PBPanelListener el : enableList.getListeners(PBPanelListener.class)) {
                el.currentItemSelected(eo);
            }
        }
        
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                PhoneBookEntry newEntry = (PhoneBookEntry)listEntries.getSelectedValue();
                if (oldEntry != newEntry) {
                    readFromTextFields(oldEntry, false);
                    oldEntry = newEntry;
                    writeToTextFields(oldEntry);
                    
                    deleteEnableChanged((listEntries.getSelectedIndex() >= 0) && (!phoneBook.isReadOnly()));
                }
            }        
        }
        
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals("add")) {
                addEntry();
            } else if (cmd.equals("del")) {
                deleteCurrentEntry();
            } else if (cmd.equals("browse")) {
                browseForPhonebook();
            } else if (cmd.equals("select")) {
                fireItemSelected();
            } else
                assert(false);
        }
        
        private String _(String key) {
            return utils._(key);
        }
        
        private Dialog findParentDlg() {
            return (Dialog)SwingUtilities.getWindowAncestor(this);
        }
        
        private void writeToTextFields(PhoneBookEntry pb) {
            
            if (pb == null) {
                textSurname.setText("");
                textGivenname.setText("");
                textTitle.setText("");
                textCompany.setText("");
                textLocation.setText("");
                textVoicenumber.setText("");
                textFaxnumber.setText("");
                textComment.setText("");
                
                textSurname.setEnabled(false);
                textGivenname.setEnabled(false);
                textTitle.setEnabled(false);
                textCompany.setEnabled(false);
                textLocation.setEnabled(false);
                textVoicenumber.setEnabled(false);
                textFaxnumber.setEnabled(false);
                
                scrollComment.setEnabled(false);
                textComment.setEnabled(false);
            } else {
                textSurname.setText(pb.getName());
                textGivenname.setText(pb.getGivenName());
                textTitle.setText(pb.getTitle());
                textCompany.setText(pb.getCompany());
                textLocation.setText(pb.getLocation());
                textVoicenumber.setText(pb.getVoiceNumber());
                textFaxnumber.setText(pb.getFaxNumber());
                textComment.setText(pb.getComment());
                
                textSurname.setEnabled(phoneBook.isFieldNameAvailable());
                textGivenname.setEnabled(phoneBook.isFieldGivenNameAvailable());
                textTitle.setEnabled(phoneBook.isFieldTitleAvailable());
                textCompany.setEnabled(phoneBook.isFieldCompanyAvailable());
                textLocation.setEnabled(phoneBook.isFieldLocationAvailable());
                textVoicenumber.setEnabled(phoneBook.isFieldVoiceNumberAvailable());
                textFaxnumber.setEnabled(phoneBook.isFieldFaxNumberAvailable());
                scrollComment.setEnabled(phoneBook.isFieldCommentAvailable());
                textComment.setEnabled(phoneBook.isFieldCommentAvailable());
                
                boolean editable = !phoneBook.isReadOnly();
                textSurname.setEditable(editable);
                textGivenname.setEditable(editable);
                textTitle.setEditable(editable);
                textCompany.setEditable(editable);
                textLocation.setEditable(editable);
                textVoicenumber.setEditable(editable);
                textFaxnumber.setEditable(editable);
                textComment.setEditable(editable);
            }
        }
        
        private void readFromTextFields(PhoneBookEntry pb, boolean updateOnly) {
            if (pb == null)
               return; 
            
            pb.setName(textSurname.getText());
            pb.setGivenName(textGivenname.getText());
            pb.setTitle(textTitle.getText());
            pb.setCompany(textCompany.getText());
            pb.setLocation(textLocation.getText());
            pb.setVoiceNumber(textVoicenumber.getText());
            pb.setFaxNumber(textFaxnumber.getText());
            pb.setComment(textComment.getText());
            
            if (updateOnly)
                pb.updateDisplay();
            else
                pb.commit();
        }
        
        private void addWithLabel(JPanel pane, JComponent comp, String text, String layout) {
            TableLayoutConstraints c = new TableLayoutConstraints(layout);
            
            pane.add(comp, c);
            
            JLabel lbl = new JLabel(text);
            lbl.setLabelFor(comp);
            c.row1 = c.row2 = c.row1 - 1;
            c.vAlign = TableLayoutConstraints.BOTTOM;
            c.hAlign = TableLayoutConstraints.LEFT;
            pane.add(lbl, c);
        }
        
        private JSplitPane getBottomPane() {
            if (bottomPane == null) {
                bottomPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLeftPane(), getRightPane());
                bottomPane.setDividerLocation(140);
            }
            return bottomPane;
        }
        
        private JPanel getLeftPane() {
            if (leftPane == null) {
                
                double [][] dLay = {
                        {0.5, TableLayout.FILL},
                        {25, TableLayout.FILL}
                };
                leftPane = new JPanel(new TableLayout(dLay));
                
                listEntries = new JList();
                listEntries.addListSelectionListener(this);
                scrollEntries = new JScrollPane(listEntries);
                
                buttonAdd = new JButton(utils.loadIcon("general/Add"));
                buttonAdd.setToolTipText(_("Add new entry"));
                buttonAdd.setActionCommand("add");
                buttonAdd.addActionListener(this);
                buttonDel = new JButton(utils.loadIcon("general/Delete"));
                buttonDel.setToolTipText(_("Delete selected entry"));
                buttonDel.setEnabled(false);
                buttonDel.setActionCommand("del");
                buttonDel.addActionListener(this);
                
                leftPane.add(buttonAdd, "0, 0");
                leftPane.add(buttonDel, "1, 0");
                leftPane.add(scrollEntries, "0, 1, 1, 1");            
            }
            return leftPane;
        }
        
        private JTextField createEntryTextField() {
            JTextField res = new JTextField();
            res.addFocusListener(entryListener);
            res.addActionListener(entryListener);
            res.addMouseListener(getDefClPop());
            return res;
        }
        
        private JPanel getRightPane() {
            if (rightPane == null) {
                
                double[][] dLay = {
                        {border, 0.5, border, TableLayout.FILL, border},
                        new double[14]
                };
                final double rowH = 1.0 / 17.0;
                Arrays.fill(dLay[1], 1, dLay[1].length - 2, rowH);
                dLay[1][0] = dLay[1][dLay[1].length - 1] = border;
                dLay[1][dLay[1].length - 2] = TableLayout.FILL;
                
                rightPane = new JPanel(new TableLayout(dLay));
                
                entryListener = new EntryTextFieldListener();
                
                textSurname = createEntryTextField();
                textGivenname = createEntryTextField();
                textCompany = createEntryTextField(); 
                textLocation = createEntryTextField();
                textVoicenumber = createEntryTextField();
                textFaxnumber = createEntryTextField();
                textTitle = createEntryTextField();
                
                textComment = new JTextArea();
                textComment.setWrapStyleWord(true);
                textComment.setLineWrap(true);
                textComment.addFocusListener(entryListener);
                textComment.addMouseListener(getDefClPop());
                scrollComment = new JScrollPane(textComment, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                
                addWithLabel(rightPane, textGivenname, _("Given name:"), "1, 2, F, C");
                addWithLabel(rightPane, textSurname, _("Surname:"), "3, 2, F, C");
                addWithLabel(rightPane, textCompany, _("Company:"), "1, 4, F, C");
                addWithLabel(rightPane, textTitle, _("Title:"), "3, 4, F, C");
                addWithLabel(rightPane, textLocation, _("Location:"), "1, 6, 3, 6, F, C");
                addWithLabel(rightPane, textVoicenumber, _("Voice number:"),  "1, 8, 3, 8, F, C");
                addWithLabel(rightPane, textFaxnumber, _("Fax number:"), "1, 10, 3, 10, F, C");
                addWithLabel(rightPane, scrollComment, _("Comments:"), "1, 12, 3, 12");
                
            }
            return rightPane;
        }
        
        private void initialize() {

                double [][] dLay = {
                        {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                        {0, TableLayout.PREFERRED, border, 25, border, TableLayout.FILL}
                };
                
                this.setLayout(new TableLayout(dLay));
                
                textDescriptor = new JTextField();
                textDescriptor.setEditable(false);
                textDescriptor.setBackground(UIManager.getColor("Label.backgroundColor"));
                textDescriptor.addMouseListener(getDefClPop());
                
                buttonBrowse = new JButton(utils.loadIcon("general/Open"));
                buttonBrowse.setActionCommand("browse");
                buttonBrowse.addActionListener(this);
                            
                /*buttonClose = new JButton(_("Close"));
                buttonClose.setActionCommand("close");
                buttonClose.addActionListener(this);
                */
                
                buttonSelect = new JButton(_("Select"));
                buttonSelect.setIcon(utils.loadIcon("general/Undo"));
                buttonSelect.setActionCommand("select");
                buttonSelect.addActionListener(this);
                buttonSelect.setVisible(false);
                
                Box box = Box.createHorizontalBox();
                box.add(textDescriptor);
                box.add(buttonBrowse);
                
                this.add(new JLabel(_("Phone book:")), "1, 1, L B");
                this.add(box, "1, 3, 3, 3");
                
                this.add(buttonSelect, "3, 1");
                //this.add(buttonClose, "3, 3");
                
                this.add(getBottomPane(), "0, 5, 4, 5");

        }
        
                
        private ClipboardPopup getDefClPop() {
            if (defClPop == null) {
                defClPop = new ClipboardPopup();
            }
            return defClPop;
        }
        
        public PhoneBookPanel() {
            super(null, false);
            initialize();
        }
        
        private class EntryTextFieldListener implements ActionListener, FocusListener {

            public void actionPerformed(ActionEvent e) {
                updateListEntries();
            }

            public void focusGained(FocusEvent e) {
                // do nothing
            }

            public void focusLost(FocusEvent e) {
                updateListEntries();
            }
            
            private void updateListEntries() {
                readFromTextFields(oldEntry, true);
                listEntries.setSelectedValue(oldEntry, true);
            }
        }
        
        public interface PBPanelListener extends EventListener {
            public void deleteOKChanged(EnableEventObj evt);
            public void phoneBookStateChanged(EnableEventObj evt);
            public void currentItemSelected(EventObject evt);
        }
        
        public static class EnableEventObj extends EventObject {
            private boolean deleteOK, writeOK, browseOK;
            
            public boolean isDeleteOK() {
                return deleteOK;
            }
            public boolean isWriteOK() {
                return writeOK;
            }
            public boolean isBrowseOK() {
                return browseOK;
            }
            
            EnableEventObj(Object source, boolean deleteOK, boolean writeOK, boolean browseOK) {
                super(source);
                this.deleteOK = deleteOK;
                this.writeOK = writeOK;
                this.browseOK = browseOK;
            }
        }
}
