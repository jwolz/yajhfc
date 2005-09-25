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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PhoneBookWin extends JDialog 
    implements ListSelectionListener, ActionListener {

    private JPanel jContentPane;
    private JSplitPane bottomPane;

    private JTextField textDescriptor;
    private JButton buttonBrowse, buttonClose, buttonSelect;
    
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
    
    private boolean usedSelectButton;
    
    private final double border = 5;
    
    private String _(String key) {
        return utils._(key);
    }
    
    private void writeToTextFields(PhoneBookEntry pb) {
        boolean enable;
        
        if (pb == null) {
            textSurname.setText("");
            textGivenname.setText("");
            textTitle.setText("");
            textCompany.setText("");
            textLocation.setText("");
            textVoicenumber.setText("");
            textFaxnumber.setText("");
            textComment.setText("");
            enable = false;
        } else {
            textSurname.setText(pb.surname);
            textGivenname.setText(pb.givenname);
            textTitle.setText(pb.title);
            textCompany.setText(pb.company);
            textLocation.setText(pb.location);
            textVoicenumber.setText(pb.voicenumber);
            textFaxnumber.setText(pb.faxnumber);
            textComment.setText(pb.comment);
            enable = true;
        }
        
        textSurname.setEnabled(enable);
        textGivenname.setEnabled(enable);
        textTitle.setEnabled(enable);
        textCompany.setEnabled(enable);
        textLocation.setEnabled(enable);
        textVoicenumber.setEnabled(enable);
        textFaxnumber.setEnabled(enable);
        textComment.setEnabled(enable);
    }
    
    private void readFromTextFields(PhoneBookEntry pb, boolean updateOnly) {
        if (pb == null)
           return; 
        
        pb.surname = textSurname.getText();
        pb.givenname = textGivenname.getText();
        pb.title = textTitle.getText();
        pb.company = textCompany.getText();
        pb.location = textLocation.getText();
        pb.voicenumber = textVoicenumber.getText();
        pb.faxnumber = textFaxnumber.getText();
        pb.comment = textComment.getText();
        
        if (updateOnly)
            phoneBook.updateEntryInList(pb);
        else
            phoneBook.writeEntry(pb);
    }
    
    private void closePhoneBook() {
        if (phoneBook == null) 
            return;
        
        oldEntry = null;
        phoneBook.close();
    }
    
    private void openPhoneBook() {
        closePhoneBook();
            
        phoneBook = new XMLPhoneBook();
        oldEntry = null;
        
        String descriptor = textDescriptor.getText();
        if (descriptor == null || descriptor.equals("")) {
            descriptor = utils.getConfigDir() + "default.phonebook";
            textDescriptor.setText(descriptor);
        }
        phoneBook.open(descriptor);
        
        listEntries.setModel(phoneBook);
        
        if (phoneBook.getSize() > 0)
            listEntries.setSelectedIndex(0);
        else {
            buttonDel.setEnabled(false);
            writeToTextFields(null); // disable text fields
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            PhoneBookEntry newEntry = (PhoneBookEntry)listEntries.getSelectedValue();
            if (oldEntry != newEntry) {
                readFromTextFields(oldEntry, false);
                oldEntry = newEntry;
                writeToTextFields(oldEntry);
                buttonDel.setEnabled(listEntries.getSelectedIndex() >= 0);
            }
        }        
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("add")) {
            PhoneBookEntry pb = phoneBook.addNewEntry();
            listEntries.setSelectedValue(pb, true);
        } else if (e.getActionCommand().equals("del")) {
            PhoneBookEntry pb = (PhoneBookEntry)listEntries.getSelectedValue();
            if (pb != null)
                if (JOptionPane.showConfirmDialog(this, MessageFormat.format(_("Do you want to delete the entry for \"{0}\"?"), pb), "Delete entry", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    oldEntry = null;
                    phoneBook.deleteEntry(pb);
                    writeToTextFields(null);
                }
        } else if (e.getActionCommand().equals("close")) {
            usedSelectButton = false;
            dispose();
        } else if (e.getActionCommand().equals("browse")) {
            String newPB = phoneBook.browseForPhoneBook();
            if (newPB != null) {
                textDescriptor.setText(newPB);
                openPhoneBook();
            }
        } else if (e.getActionCommand().equals("select")) {
            usedSelectButton = true;
            setVisible(false);
        } else
            assert(false);
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
    
    private JPanel getJContentPane() {
        if (jContentPane == null) {

            double [][] dLay = {
                    {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                    {border, 25, border, 25, border, TableLayout.FILL}
            };
            
            jContentPane = new JPanel(new TableLayout(dLay));
            
            textDescriptor = new JTextField();
            textDescriptor.setEditable(false);
            textDescriptor.setBackground(UIManager.getColor("Label.backgroundColor"));
            
            buttonBrowse = new JButton(utils.loadIcon("general/Open"));
            buttonBrowse.setActionCommand("browse");
            buttonBrowse.addActionListener(this);
                        
            buttonClose = new JButton(_("Close"));
            buttonClose.setActionCommand("close");
            buttonClose.addActionListener(this);
            
            buttonSelect = new JButton(_("Select"));
            buttonSelect.setIcon(utils.loadIcon("general/Undo"));
            buttonSelect.setActionCommand("select");
            buttonSelect.addActionListener(this);
            buttonSelect.setVisible(false);
            
            Box box = Box.createHorizontalBox();
            box.add(textDescriptor);
            box.add(buttonBrowse);
            
            jContentPane.add(new JLabel(_("Phone book:")), "1, 1, L B");
            jContentPane.add(box, "1, 3");
            
            jContentPane.add(buttonSelect, "3, 1");
            jContentPane.add(buttonClose, "3, 3");
            
            jContentPane.add(getBottomPane(), "0, 5, 4, 5");
        }
        return jContentPane;
    }
    
    public void initialize() {
        setContentPane(getJContentPane());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(_("Phone book"));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closePhoneBook();
                
                utils.getFaxOptions().phoneWinBounds = getBounds();
                utils.getFaxOptions().lastPhonebook = textDescriptor.getText();
            }
        });
        
        
        if (utils.getFaxOptions().phoneWinBounds != null)
            this.setBounds(utils.getFaxOptions().phoneWinBounds);
        else {
            this.setSize(640, 480);
            this.setLocationByPlatform(true);
        }
        
        textDescriptor.setText(utils.getFaxOptions().lastPhonebook);
        openPhoneBook();
    }
    
    public PhoneBookWin(Dialog owner) {
        super(owner);
        initialize();
    }
    
    public PhoneBookWin(Frame owner) {
        super(owner);
        initialize();
    }
    
    public PhoneBookEntry selectNumber() {
        usedSelectButton = false;
        setModal(true);
        buttonSelect.setVisible(true);
        
        setVisible(true);
        
        if (usedSelectButton) {
            PhoneBookEntry res = (PhoneBookEntry)listEntries.getSelectedValue();
            dispose();
            return res;
        } else
            return null;
    }
    
    class EntryTextFieldListener implements ActionListener, FocusListener {

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
}

// Phone book entry
// Phone book implementations can override this in order to
// save status information.
// New PhoneBookEntry classes are only to be created by PhoneBook implementations
abstract class PhoneBookEntry {
    public String surname = "";
    public String givenname = "";
    public String title = "";
    public String company = "";
    public String location = "";
    public String voicenumber = "";
    public String faxnumber = "";
    public String comment = "";
    
    public String toString() {
        if (surname.length() > 0) {
            if (givenname.length() > 0)
                //return surname + ", " + givenname;
                return MessageFormat.format(utils._("{0} {1}"), givenname, surname);
            else
                return surname;
        } else {
            if (givenname.length() > 0)
                return givenname;
            else
                return utils._("<no name>");
        }
    }
    
}

class DefaultPhoneBookEntryComparator implements Comparator<PhoneBookEntry> {
    public int compare(PhoneBookEntry o1, PhoneBookEntry o2) {
        int res;
        res = o1.surname.compareToIgnoreCase(o2.surname);
        if (res == 0) {
            return o1.givenname.compareToIgnoreCase(o2.givenname);
        } else
            return res;
        //return o1.toString().compareToIgnoreCase(o2.toString());
    }
}

// Abstract class describing a phone book
// Should be sorted
abstract class PhoneBook extends AbstractListModel {

    public abstract PhoneBookEntry addNewEntry();
    
    // read entry
    public PhoneBookEntry readEntry(int index) {
        return (PhoneBookEntry)getElementAt(index);
    }
    
    // Write the entry to the "database"
    public abstract void writeEntry(PhoneBookEntry entry);
    
    // Just update the display in the JList
    public void updateEntryInList(PhoneBookEntry entry) {
        writeEntry(entry);
    }
    
    public abstract void deleteEntry(PhoneBookEntry entry);
    
    // Show dialog to select a new Phonebook.
    // Return null if user selects cancel
    public abstract String browseForPhoneBook();
    
    public abstract void open(String descriptor);
    
    public abstract void close();
}
