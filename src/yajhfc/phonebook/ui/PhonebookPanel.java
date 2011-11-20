/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */
package yajhfc.phonebook.ui;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.LimitedPlainDocument;

/**
 * @author jonas
 *
 */
public abstract class PhonebookPanel extends JPanel {
    
    protected NewPhoneBookWin parent;
    protected EntryTextFieldListener entryListener = new EntryTextFieldListener();
    
    
    public abstract void writeToTextFields(PhoneBook phoneBook, PhoneBookEntry pb);
    public abstract void readFromTextFields(PhoneBookEntry pb, boolean updateOnly);
    
    
    protected static final String PBFIELD_PROP = "YajHFC-PBEntryfield";
    
    protected JTextField createEntryTextField(PBEntryField field) {
        JTextField res = new JTextField(new LimitedPlainDocument(0), "", 0);
        res.addFocusListener(entryListener);
        res.addActionListener(entryListener);
        res.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        res.putClientProperty(PBFIELD_PROP, field);
        return res;
    }
    
    /**
     * Returns the list of selected entries in the phonebook window
     * @return
     */
    protected List<PhoneBookEntry> getSelectedItems() {
        return parent.selectedItems;
    }
    
    
    /**
     * @param layout
     * @param isDoubleBuffered
     */
    public PhonebookPanel(NewPhoneBookWin parent, LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.parent = parent;
    }

    protected class EntryTextFieldListener implements ActionListener, FocusListener {

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
            List<PhoneBookEntry> selectedItems = getSelectedItems();
            if (isVisible() && selectedItems.size() == 1) {
                readFromTextFields(selectedItems.get(0), true); 
            }
        }
    }
}
