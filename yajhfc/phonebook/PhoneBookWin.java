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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.ClipboardPopup;
import yajhfc.ExceptionDialog;
import yajhfc.utils;

public final class PhoneBookWin extends JDialog 
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
    
    private ClipboardPopup defClPop;
    
    private JMenuItem mnuAdd, mnuDel;
    
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
            textSurname.setText(pb.getName());
            textGivenname.setText(pb.getGivenName());
            textTitle.setText(pb.getTitle());
            textCompany.setText(pb.getCompany());
            textLocation.setText(pb.getLocation());
            textVoicenumber.setText(pb.getVoiceNumber());
            textFaxnumber.setText(pb.getFaxNumber());
            textComment.setText(pb.getComment());
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
    
    private void closePhoneBook() {
        if (phoneBook == null) 
            return;
        
        oldEntry = null;
        phoneBook.close();
    }
    
    private void openPhoneBook(String descriptor) {
        closePhoneBook();
        
        //String descriptor = textDescriptor.getText();
        textDescriptor.setText(descriptor);
        
        if (descriptor != null && descriptor.length() > 0) {
            phoneBook = PhoneBookFactory.instanceForDescriptor(descriptor, this);
            if (phoneBook == null) {
                JOptionPane.showMessageDialog(this, utils._("Unknown Phonebook type selected, using default!"), utils._("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                phoneBook.open(descriptor);
            }
        }
        if (phoneBook == null) {
            phoneBook = PhoneBookFactory.createDefault(this);
            phoneBook.openDefault();
        }
        
        oldEntry = null;
        
        listEntries.setModel(phoneBook);
        
        if (phoneBook.getSize() > 0)
            listEntries.setSelectedIndex(0);
        else {
            buttonDel.setEnabled(false);
            mnuDel.setEnabled(false);
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
                
                boolean enable = listEntries.getSelectedIndex() >= 0;
                buttonDel.setEnabled(enable);
                mnuDel.setEnabled(enable);
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
                    pb.delete();
                    writeToTextFields(null);
                }
        } else if (e.getActionCommand().equals("close")) {
            usedSelectButton = false;
            dispose();
        } else if (e.getActionCommand().equals("browse")) {
            String newPB = phoneBook.browseForPhoneBook();
            if (newPB != null) {
                //textDescriptor.setText(newPB);
                openPhoneBook(newPB);
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
    
    private JPanel getJContentPane() {
        if (jContentPane == null) {

            double [][] dLay = {
                    {border, TableLayout.FILL, border, TableLayout.PREFERRED, border},
                    {0, TableLayout.PREFERRED, border, 25, border, TableLayout.FILL}
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
    
    private JMenu getPhonebookMenu() {
        JMenu importMenu = new JMenu(utils._("Import"));
        importMenu.setIcon(utils.loadIcon("general/Import"));
        JMenu openMenu = new JMenu(utils._("Open"));
        openMenu.setIcon(utils.loadIcon("general/Open"));
        /*JMenu saveAsMenu = new JMenu(utils._("Save as"));
        saveAsMenu.setIcon(utils.loadIcon("general/SaveAs"));*/
        
        for (PhoneBookType pbt : PhoneBookFactory.PhonebookTypes) {
            String menuText = pbt.getDisplayName();
            PhonebookMenuActionListener pbmal = new PhonebookMenuActionListener(pbt);
            
            JMenuItem mi;
            mi = new JMenuItem(menuText);
            mi.setActionCommand(PhonebookMenuActionListener.IMPORT_COMMAND);
            mi.addActionListener(pbmal);
            importMenu.add(mi);
            
            mi = new JMenuItem(menuText);
            mi.setActionCommand(PhonebookMenuActionListener.OPEN_COMMAND);
            mi.addActionListener(pbmal);
            openMenu.add(mi);
            
            /*mi = new JMenuItem(menuText);
            mi.setActionCommand(PhonebookMenuActionListener.SAVEAS_COMMAND);
            mi.addActionListener(pbmal);
            saveAsMenu.add(mi);*/
        }
        
        JMenuItem closeMenu = new JMenuItem(utils._("Close"));
        closeMenu.setActionCommand("close");
        closeMenu.addActionListener(this);
        
        JMenu pbMenu = new JMenu(utils._("Phonebook"));
        pbMenu.add(openMenu);
        pbMenu.add(importMenu);
        //pbMenu.add(saveAsMenu);
        pbMenu.add(new JSeparator());
        pbMenu.add(closeMenu);
        return pbMenu;
    }
    
    private JMenu getEntryMenu() {
        JMenu entryMenu = new JMenu(utils._("Entry"));
        
        mnuAdd = new JMenuItem(utils._("Add"), utils.loadIcon("general/Add"));
        mnuAdd.setToolTipText(_("Add new entry"));
        mnuAdd.setActionCommand("add");
        mnuAdd.addActionListener(this);
        mnuDel = new JMenuItem(utils._("Delete"), utils.loadIcon("general/Delete"));
        mnuDel.setToolTipText(_("Delete selected entry"));
        mnuDel.setEnabled(false);
        mnuDel.setActionCommand("del");
        mnuDel.addActionListener(this);
        
        entryMenu.add(mnuAdd);
        entryMenu.add(mnuDel);
        return entryMenu;
    }
    
    private JMenuBar getMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getPhonebookMenu());
        menuBar.add(getEntryMenu());
        return menuBar;
    }
    
    public void initialize() {
        setContentPane(getJContentPane());
        setJMenuBar(getMenu());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(_("Phone book"));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closePhoneBook();
                
                utils.getFaxOptions().phoneWinBounds = getBounds();
                utils.getFaxOptions().lastPhonebook = phoneBook.getDescriptor();
            }
        });
        
        
        if (utils.getFaxOptions().phoneWinBounds != null)
            this.setBounds(utils.getFaxOptions().phoneWinBounds);
        else {
            this.setSize(640, 480);
            //this.setLocationByPlatform(true);
            utils.setDefWinPos(this);
        }
        
        openPhoneBook(utils.getFaxOptions().lastPhonebook);
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
    
    private ClipboardPopup getDefClPop() {
        if (defClPop == null) {
            defClPop = new ClipboardPopup();
        }
        return defClPop;
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
    
    private class PhonebookMenuActionListener implements ActionListener{
        
        public static final String IMPORT_COMMAND = "pb_import";
        public static final String SAVEAS_COMMAND = "pb_saveas";
        public static final String OPEN_COMMAND = "pb_open";
        
        public PhoneBookType pbType;
        
        public void actionPerformed(ActionEvent e) {
            try {
                String cmd = e.getActionCommand();

                if (cmd.equals(OPEN_COMMAND)) 
                    doOpen();
                /*else if (cmd.equals(SAVEAS_COMMAND))
                    doSaveAs();*/
                else if (cmd.equals(IMPORT_COMMAND)) 
                    doImport();
                else
                    System.err.println("Unknown Action command: " + cmd);
            } catch (Exception ex) {
                ExceptionDialog.showExceptionDialog(PhoneBookWin.this, utils._("Error executing the desired action:"), ex);
            }
        }
        
        private void doImport() {
            PhoneBook pb;
            String descriptor;
            
            pb = pbType.createInstance(PhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook();
            if (descriptor != null) {
                pb.open(descriptor); 
                
                for (int i=0; i < pb.getSize(); i++) {
                    PhoneBookEntry pbe = phoneBook.addNewEntry();
                    pbe.copyFrom(pb.readEntry(i));
                }
                pb.close();
                phoneBook.resort();
            }
        }
        
        /*private void doSaveAs() {
            
        }*/
        
        private void doOpen() {
            PhoneBook pb;
            String descriptor;
            
            if (pbType.targetClass.isInstance(phoneBook))
                pb = phoneBook;
            else
                pb = pbType.createInstance(PhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook();
            if (descriptor != null) {
                openPhoneBook(descriptor);                
            }
        }
        
        public PhonebookMenuActionListener(PhoneBookType pbType) {
            this.pbType = pbType;
        }
    }
}



