/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.util;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import yajhfc.utils;

/**
 * @author jonas
 *
 */
public class JTableTABAction extends AbstractAction {
    private static final Logger log = Logger.getLogger(JTableTABAction.class.getName());
    
    private JTable table;
    private boolean forwardMode;
    private boolean checkCol;
    
    private String origKey;

    JTableTABAction(JTable table, boolean forward, boolean checkCol, String origKey) {
        this.table = table;
        this.forwardMode = forward;
        this.checkCol = checkCol;
        this.origKey = origKey;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        int rowCount = table.getRowCount();
        if (forwardMode) {
            if (rowCount == 0 || 
                    (table.getSelectedRow() == rowCount - 1 && 
                            (!checkCol || table.getSelectedColumn() == table.getColumnCount() - 1))) {
                Container focusAncestor = table.getFocusCycleRootAncestor();
                focusAncestor.getFocusTraversalPolicy().getComponentAfter(focusAncestor, table).requestFocusInWindow();
            } else {
                table.getActionMap().get(origKey).actionPerformed(e);
            }
        } else {
            if (rowCount == 0 || 
                    (table.getSelectedRow() == 0 && 
                            (!checkCol || table.getSelectedColumn() == 0))) {
                Container focusAncestor = table.getFocusCycleRootAncestor();
                focusAncestor.getFocusTraversalPolicy().getComponentBefore(focusAncestor, table).requestFocusInWindow();
            } else {
                table.getActionMap().get(origKey).actionPerformed(e);
            }
        }
    }

    
    private static final String nextRowAction = "selectNextRow";
    private static final String prevRowAction = "selectPreviousRow";
    private static final String nextRowActionWrapped = nextRowAction + "-YajHFCWrapped";
    private static final String prevRowActionWrapped = prevRowAction + "-YajHFCWrapped";
    
    public static void replaceTABWithNextRow(JTable table) {
        InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        JTableTABAction wrapper = new JTableTABAction(table, true, false, nextRowAction);
        am.put(nextRowActionWrapped, wrapper);
        im.put(keyStroke, nextRowActionWrapped);

        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
        wrapper = new JTableTABAction(table, false, false, prevRowAction);
        am.put(prevRowActionWrapped, wrapper);
        im.put(keyStroke, prevRowActionWrapped);
    }
    
    private static final String nextTABAction = "yajhfc-nextTabAction";
    private static final String prevTABAction = "yajhfc-prevTabAction";
    public static void wrapDefTabAction(JTable table) {
        InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();
        
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        String oldAction = (String)im.get(keyStroke);
        if (oldAction != null) {
            JTableTABAction wrapper = new JTableTABAction(table, true, true, oldAction);
            am.put(nextTABAction, wrapper);
            im.put(keyStroke, nextTABAction);
            
            if (utils.debugMode) {
                log.fine("Replaced action " + oldAction + " for key " + keyStroke);
            }
        } else {
            log.info("No existing binding for " + keyStroke + " found.");
        }
        
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
        oldAction = (String)im.get(keyStroke);
        if (oldAction != null) {
            JTableTABAction wrapper = new JTableTABAction(table, false, true, oldAction);
            am.put(prevTABAction, wrapper);
            im.put(keyStroke, prevTABAction);
            
            if (utils.debugMode) {
                log.fine("Replaced action " + oldAction + " for key " + keyStroke);
            }
        } else {
            log.info("No existing binding for " + keyStroke + " found.");
        }
        
    }
}
