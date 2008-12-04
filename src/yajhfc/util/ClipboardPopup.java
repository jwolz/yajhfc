package yajhfc.util;
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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import yajhfc.Utils;

/**
 * Implements a Windows-Like customizable (via getPopupMenu()) PopupMenu for JTextFields
 * Usage: Simply attach it as a mouse listener to the JTextComponent you want the menu for.
 * @author jonas
 *
 */
public class ClipboardPopup implements MouseListener, ActionListener {

    protected JPopupMenu popupMenu;
    protected JMenuItem cut, copy, paste, delete, selectAll;
    protected JTextComponent lastTarget;
    
    public ClipboardPopup() {
        super();
        createPopupMenu();
    }

    protected void createPopupMenu() {
        cut = new JMenuItem(Utils._("Cut"), Utils.loadIcon("general/Cut"));
        cut.addActionListener(this);
        copy = new JMenuItem(Utils._("Copy"), Utils.loadIcon("general/Copy"));
        copy.addActionListener(this);
        paste = new JMenuItem(Utils._("Paste"), Utils.loadIcon("general/Paste"));
        paste.addActionListener(this);
        delete = new JMenuItem(Utils._("Delete"), Utils.loadIcon("general/Remove"));
        delete.addActionListener(this);
        selectAll = new JMenuItem(Utils._("Select All"));
        selectAll.addActionListener(this);
        
        popupMenu = new JPopupMenu();
        popupMenu.add(cut);
        popupMenu.add(copy);
        popupMenu.add(paste);
        popupMenu.add(delete);
        popupMenu.addSeparator();
        popupMenu.add(selectAll);
    }
    
    protected void preparePopup(JTextComponent target) {
        boolean haveSelection = target.getSelectionStart() < target.getSelectionEnd();
        boolean editable = target.isEditable();
        boolean clipboardAvail = Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor);
        
        cut.setEnabled(haveSelection && editable);
        copy.setEnabled(haveSelection);
        paste.setEnabled(editable && clipboardAvail);
        delete.setEnabled(haveSelection && editable);
    }
    
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }
    
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        
        if (src == cut) {
            lastTarget.cut();
        } else if (src == copy) {
            lastTarget.copy();
        } else if (src == paste) {
            lastTarget.paste();
        } else if (src == delete) {
            lastTarget.replaceSelection("");
        } else if (src == selectAll) {
            lastTarget.selectAll();
        }
    }
    
    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
           JTextComponent src = (JTextComponent) e.getSource();
           
           preparePopup(src);
           lastTarget = src;
           popupMenu.show(src, e.getX(), e.getY());
        }
    }
    
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    
    public void mouseClicked(MouseEvent e) {
        // method stub
    }

    public void mouseEntered(MouseEvent e) {
        // method stub
    }

    public void mouseExited(MouseEvent e) {
        // method stub
    }

    /**
     * The application wide, shared default popup
     */
    public static final ClipboardPopup DEFAULT_POPUP = new ClipboardPopup();
}
