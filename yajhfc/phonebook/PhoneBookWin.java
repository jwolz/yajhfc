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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import yajhfc.ExceptionDialog;
import yajhfc.FaxOptions;
import yajhfc.utils;
import yajhfc.phonebook.PhoneBookPanel.EnableEventObj;

public final class PhoneBookWin extends JDialog 
    implements ActionListener, PhoneBookPanel.PBPanelListener {

    JPanel jContentPane;
    JTabbedPane tabPhonebooks;
        
    JMenu pbMenu, importMenu, openMenu, entryMenu;
    
    JMenuItem mnuAdd, mnuDel, mnuSearch;
    
    Action listRemoveAction;
    
    SearchWin searchWin;
    
    boolean usedSelectButton = false, selBtnVisible = false;
    //private final double border = 5;
    
    private void setSelBtnVisible(boolean b) {
        selBtnVisible = b;
        for (int i = 0; i < tabPhonebooks.getTabCount(); i++) {
            PhoneBookPanel pbp = (PhoneBookPanel)tabPhonebooks.getComponent(i);
            pbp.setSelectButtonVisible(b);
        }
    }
   
    
    private void showSearchWin() {
        if (searchWin == null) {
            searchWin = new SearchWin(this);
        }
        if (searchWin.isVisible()) 
            searchWin.toFront();
        else
            searchWin.setVisible(true);
    }
    
    public PhoneBook getCurrentPhoneBook() {
        PhoneBookPanel pbp = getCurrentPBPanel();
        if (pbp != null)
            return pbp.getPhoneBook();
        else
            return null;
    }
    
    public PhoneBookPanel getCurrentPBPanel() {
        return (PhoneBookPanel)tabPhonebooks.getSelectedComponent();
    }
    
    public void selectPhoneBookEntry(int index) {
        PhoneBookPanel pbp = getCurrentPBPanel();
        if (pbp != null)
            getCurrentPBPanel().selectPhoneBookEntry(index);
    }
    
    public int getSelectedPBEntry() {
        PhoneBookPanel pbp = getCurrentPBPanel();
        if (pbp != null)
            return getCurrentPBPanel().getSelectedPBEntryIndex();
        else 
            return -1;
    }
    
    void addPhoneBook(String descriptor) {
        // Try to check if the phone book has already been added:
        for (int i = 0; i < tabPhonebooks.getTabCount(); i++) {
            PhoneBook pb = ((PhoneBookPanel)tabPhonebooks.getComponent(i)).getPhoneBook();
            if (pb != null && descriptor.equals(pb.getDescriptor())) {
                JOptionPane.showMessageDialog(this, utils._("This phone book has already been added."), utils._("Add to list"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        PhoneBookPanel pbp = new PhoneBookPanel();
        pbp.addEnableListener(this);
        
        tabPhonebooks.add(pbp);
        
        pbp.setSelectButtonVisible(selBtnVisible);
        pbp.openPhoneBook(descriptor);
        
        int newIdx = tabPhonebooks.getTabCount() - 1;
        tabPhonebooks.setTitleAt(newIdx, pbp.getSensibleTitle());
        tabPhonebooks.setSelectedIndex(newIdx);
    }
    
    void closeCurrentPhoneBook() {
        PhoneBookPanel pbp = getCurrentPBPanel();
        if (pbp != null) {
            pbp.closePhoneBook();
            
            tabPhonebooks.remove(pbp);
            checkMenuEnable();
        }
    }
    
    void closeAndSaveAllPhonebooks() {
        List<String> pbList = utils.getFaxOptions().phoneBooks;
        pbList.clear();
        
        for (int i = 0; i < tabPhonebooks.getTabCount(); i++) {
            PhoneBookPanel pbp = (PhoneBookPanel)tabPhonebooks.getComponent(i);
            pbList.add(pbp.getPhoneBook().getDescriptor());
            pbp.closePhoneBook();
        }
        
        checkMenuEnable();
    }
    
    void checkMenuEnable() {
        boolean haveTabs = (tabPhonebooks.getTabCount() > 0);
        boolean delOK = false, writeOK = false, browseOK = false;
        if (haveTabs) {
            PhoneBookPanel pbp = getCurrentPBPanel();
            if (pbp != null) {
                delOK = pbp.isDeleteOK();
                writeOK = pbp.isWriteOK();
                browseOK = pbp.isBrowseOK();
            }
        }
        
        mnuDel.setEnabled(delOK);
        mnuAdd.setEnabled(writeOK);
    
        entryMenu.setEnabled(browseOK);
        importMenu.setEnabled(browseOK);
        
        listRemoveAction.setEnabled(haveTabs);
    }
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("add")) {
            getCurrentPBPanel().addEntry();
        } else if (cmd.equals("del")) {
            getCurrentPBPanel().deleteCurrentEntry();
        } else if (cmd.equals("close")) {
            usedSelectButton = false;
            dispose();
        } else if (cmd.equals("descopen")) {
            doDescOpen();
        } else if (cmd.equals("descimport")) {
            doDescImport();
        } else if (cmd.equals("search")) {
            showSearchWin();
        } else
            assert(false);
    }
    
    private String promptForDescriptor(String title) {
        return JOptionPane.showInputDialog(PhoneBookWin.this, utils._("Please enter the phone book descriptor to open."), title, JOptionPane.QUESTION_MESSAGE);
    }
    
    void doDescOpen() {
        String desc = promptForDescriptor(utils._("Open by descriptor"));
        if (desc != null) {
            addPhoneBook(desc);
        }
    }
    
    void doDescImport() {
        if (getCurrentPhoneBook().isReadOnly()) 
            return;
        
        String desc = promptForDescriptor(utils._("Import by descriptor"));
        if (desc != null) {
            getCurrentPBPanel().importPhoneBook(null, desc);
        }
    }
    
    public void currentItemSelected(EventObject evt) {
        usedSelectButton = true;
        setVisible(false);
    }
    
    public void deleteOKChanged(EnableEventObj evt) {
        if (evt.getSource() == getCurrentPBPanel()) {
            mnuDel.setEnabled(evt.isDeleteOK());
        }
    }
    
    public void phoneBookStateChanged(EnableEventObj evt) {
        if (evt.getSource() == getCurrentPBPanel()) {
            mnuDel.setEnabled(evt.isDeleteOK());
            mnuAdd.setEnabled(evt.isWriteOK());
        
            entryMenu.setEnabled(evt.isBrowseOK());
            importMenu.setEnabled(evt.isWriteOK());
        }
    }
    
    
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            
            jContentPane = new JPanel(new BorderLayout());
            
            tabPhonebooks = new JTabbedPane();
            tabPhonebooks.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    checkMenuEnable();
                }
            });
            
            jContentPane.add(tabPhonebooks, BorderLayout.CENTER);
        }
        return jContentPane;
    }
    
    private JMenu getPhonebookMenu() {
        if (pbMenu == null) {
            importMenu = new JMenu(utils._("Import"));
            importMenu.setIcon(utils.loadIcon("general/Import"));
            openMenu = new JMenu(utils._("Add to list"));
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
            
            JMenuItem mi = new JMenuItem(utils._("By descriptor..."));
            mi.setActionCommand("descimport");
            mi.addActionListener(this);
            
            importMenu.addSeparator();
            importMenu.add(mi);
            
            mi = new JMenuItem(utils._("By descriptor..."));
            mi.setActionCommand("descopen");
            mi.addActionListener(this);
            
            openMenu.addSeparator();
            openMenu.add(mi);
            
            listRemoveAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showConfirmDialog(PhoneBookWin.this, utils._("Do you want to remove the current phone book from the list?"), utils._("Remove from list"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        closeCurrentPhoneBook();
                    }
                }
            };
            listRemoveAction.putValue(Action.NAME, utils._("Remove from list"));
            listRemoveAction.putValue(Action.SMALL_ICON, utils.loadIcon("general/Remove"));
            
            JMenuItem closeWinMenu = new JMenuItem(utils._("Close"));
            closeWinMenu.setActionCommand("close");
            closeWinMenu.addActionListener(this);

            pbMenu = new JMenu(utils._("Phonebook"));
            pbMenu.add(openMenu);
            pbMenu.add(importMenu);
            //pbMenu.add(saveAsMenu);
            pbMenu.add(new JSeparator());
            pbMenu.add(new JMenuItem(listRemoveAction));
            pbMenu.add(new JSeparator());
            pbMenu.add(closeWinMenu);
        }
        return pbMenu;
    }
    
    private JMenu getEntryMenu() {
        if (entryMenu == null) {
            entryMenu = new JMenu(utils._("Entry"));

            mnuAdd = new JMenuItem(utils._("Add"), utils.loadIcon("general/Add"));
            mnuAdd.setToolTipText(utils._("Add new entry"));
            mnuAdd.setActionCommand("add");
            mnuAdd.addActionListener(this);
            mnuDel = new JMenuItem(utils._("Delete"), utils.loadIcon("general/Delete"));
            mnuDel.setToolTipText(utils._("Delete selected entry"));
            mnuDel.setEnabled(false);
            mnuDel.setActionCommand("del");
            mnuDel.addActionListener(this);
            mnuSearch = new JMenuItem(utils._("Find..."), utils.loadIcon("general/Search"));
            mnuSearch.setToolTipText(utils._("Search for an entry"));
            mnuSearch.setActionCommand("search");
            mnuSearch.addActionListener(this);

            entryMenu.add(mnuAdd);
            entryMenu.add(mnuDel);
            entryMenu.add(new JSeparator());
            entryMenu.add(mnuSearch);
        }
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
        setTitle(utils._("Phone book"));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                utils.getFaxOptions().lastSelectedPhonebook = tabPhonebooks.getSelectedIndex();
                utils.getFaxOptions().phoneWinBounds = getBounds();
                //utils.getFaxOptions().lastPhonebook = .getDescriptor();
                
                closeAndSaveAllPhonebooks();
                
                if (searchWin != null) {
                    searchWin.dispose();
                    searchWin = null;
                }
            }
        });
        
        FaxOptions fopts = utils.getFaxOptions();
        
        if (fopts.phoneWinBounds != null)
            this.setBounds(fopts.phoneWinBounds);
        else {
            this.setSize(640, 480);
            //this.setLocationByPlatform(true);
            utils.setDefWinPos(this);
        }
        
        if (fopts.phoneBooks.size() > 0)
            for (String pbdesc : fopts.phoneBooks) {
                addPhoneBook(pbdesc);
            }
        else
            addPhoneBook(PhoneBookFactory.getDefaultPhonebookDescriptor());
        
        if (fopts.lastSelectedPhonebook >= 0 && fopts.lastSelectedPhonebook < tabPhonebooks.getTabCount())
            tabPhonebooks.setSelectedIndex(fopts.lastSelectedPhonebook);
    }
    
    public PhoneBookWin(Dialog owner) {
        super(owner);
        initialize();
    }
    
    public PhoneBookWin(Frame owner) {
        super(owner);
        initialize();
    }
    
    public PhoneBookEntry[] selectNumbers() {
        usedSelectButton = false;
        setModal(true);
        setSelBtnVisible(true);
        
        setVisible(true);
        
        if (usedSelectButton && getCurrentPBPanel() != null) {
            PhoneBookEntry[] res = getCurrentPBPanel().getSelectedPBEntries();
            dispose();
            return res;
        } else
            return null;
    }

    private class PhonebookMenuActionListener implements ActionListener{
        
        public static final String IMPORT_COMMAND = "pb_import";
        //public static final String SAVEAS_COMMAND = "pb_saveas";
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
            
            if (getCurrentPhoneBook().isReadOnly()) 
                return;
            
            pb = pbType.createInstance(PhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook();
            if (descriptor != null) {
                getCurrentPBPanel().importPhoneBook(pb, descriptor);
            }
        }
        
        /*private void doSaveAs() {
            
        }*/
        
        private void doOpen() {
            PhoneBook pb;
            String descriptor;
            
//            phoneBook = getCurrentPhoneBook(); 
//            
//            if (pbType.targetClass.isInstance(phoneBook))
//                pb = phoneBook;
//            else
            pb = pbType.createInstance(PhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook();
            if (descriptor != null) {
                addPhoneBook(descriptor);                
            }
        }
        
        public PhonebookMenuActionListener(PhoneBookType pbType) {
            this.pbType = pbType;
        }
    }
}



