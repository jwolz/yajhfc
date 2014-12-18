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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.LimitedPlainDocument;

/**
 * @author jonas
 *
 */
public class SingleEntryPhonebookPanel extends PhonebookPanel {

    Map<PBEntryField,JTextComponent> entryFields = new EnumMap<PBEntryField, JTextComponent>(PBEntryField.class);
    JScrollPane scrollComment;
    JTextArea textComment;
    boolean writable;
    
    /**
     * @param parent
     * @param layout
     * @param isDoubleBuffered
     */
    public SingleEntryPhonebookPanel(NewPhoneBookWin parent) {
        super(parent, null, false);
        
        int longFields = 0;
        int shortFields = 0;
        final PBEntryField[] fields = PBEntryField.values();
        for (PBEntryField field : fields) {
            if (field.isShortLength()) {
                shortFields++;
            } else {
                longFields++;
            }
        }
        final int rowCount = 2 * (longFields + (shortFields+1)/2);
        double[][] dLay = {
                {0.5, NewPhoneBookWin.border, TableLayout.FILL},
                new double[rowCount]
        };
        //final double rowH = 1.0 / (double)(rowCount+3);
        //Arrays.fill(dLay[1], 0, rowCount - 1, rowH);
        Arrays.fill(dLay[1], 0, rowCount - 1, TableLayout.PREFERRED);
        dLay[1][rowCount - 1] = TableLayout.FILL;
        
        TableLayout tl = new TableLayout(dLay);
        tl.setVGap(NewPhoneBookWin.border);
        setLayout(tl);
        
        final int STARTCOL = 0;
        final int ENDCOL = 2;
        int row = 1;
        int col = STARTCOL;
        for (PBEntryField field : fields) {
            if (field != PBEntryField.Comment) {
                JTextField textField = createEntryTextField(field);
                TableLayoutConstraints layout;
                if (field.isShortLength()) {
                    layout = new TableLayoutConstraints(col, row, col, row, TableLayoutConstraints.FULL, TableLayoutConstraints.TOP);
                    if (col == STARTCOL) {
                        col = ENDCOL;
                    } else {
                        row += 2;
                        col  = STARTCOL;
                    }
                } else {
                    layout = new TableLayoutConstraints(STARTCOL, row, ENDCOL, row, TableLayoutConstraints.FULL, TableLayoutConstraints.TOP);
                    col  = STARTCOL;
                    row += 2;
                }
                Utils.addWithLabel(this, textField, field.getDescription()+":", layout);
            }
        }
        
        textComment = new JTextArea(new LimitedPlainDocument(0));
        textComment.setWrapStyleWord(true);
        textComment.setLineWrap(true);
        textComment.addFocusListener(entryListener);
        textComment.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        entryFields.put(PBEntryField.Comment, textComment);
        textComment.putClientProperty(PBFIELD_PROP, PBEntryField.Comment);
        
        scrollComment = new JScrollPane(textComment, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        Utils.addWithLabel(this, scrollComment, Utils._("Comments:"), new TableLayoutConstraints(STARTCOL,row,ENDCOL,row,TableLayoutConstraints.FULL,TableLayoutConstraints.FULL));
    }

    public void writeToTextFields(PhoneBook phoneBook, PhoneBookEntry pb) {
        
        if (pb == null || phoneBook == null) {
            for (JTextComponent comp : entryFields.values()) {
                comp.setText("");
                comp.setEnabled(false);
            }
            scrollComment.setEnabled(false);
            writable = false;
        } else {
            boolean editable = !phoneBook.isReadOnly();
            for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
                JTextComponent comp = entry.getValue();
                PBEntryField field = entry.getKey();
                
                comp.setText(pb.getField(field));
                comp.setEnabled(phoneBook.isFieldAvailable(field));
                comp.setEditable(editable);
                ((LimitedPlainDocument)comp.getDocument()).setLimit(phoneBook.getMaxLength(field));
            }
            scrollComment.setEnabled(textComment.isEnabled());
            writable = editable;
        }
    }
    
    public void readFromTextFields(PhoneBookEntry pb, boolean updateOnly) {
        if (pb == null || !writable || pb.getParent().isReadOnly())
           return; 
        
        for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
            PBEntryField field = entry.getKey();
            if (pb.getParent().isFieldAvailable(field)) {
                JTextComponent comp = entry.getValue();            
                pb.setField(field, comp.getText());
            }
        }

        if (updateOnly)
            pb.updateDisplay();
        else
            pb.commit();
    }

    @Override
    protected JTextField createEntryTextField(PBEntryField field) {
        JTextField res = super.createEntryTextField(field);
        entryFields.put(field, res);
        return res;
    }
}
