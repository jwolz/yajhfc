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
 */
package yajhfc.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public abstract class AbstractQuickSearchHelper implements DocumentListener, ActionListener {
    /**
     * Set this to true to disable the handling of document events
     */
    protected boolean eventLock = false;
    
    protected JToolBar quickSearchBar;
    protected JTextField textQuickSearch;
    protected JButton clearQuickSearchButton;
    
    private int searchTXID = 0;
    
    /**
     * Delay in milliseconds before the quick search is performed
     */
    protected final static int QUICKSEARCH_DELAY = 500;
    
    private static Timer quickSearchTimer = null;
    protected static Timer getQuickSearchTimer() {
        if (quickSearchTimer == null) {
            quickSearchTimer = new Timer("QuickSearchTimer", true);
        }
        return quickSearchTimer;
    }
    
    /**
     * Returns a new "transaction id" for a new quick search
     * @return
     */
    protected final synchronized int nextTXID() {
        return ++searchTXID;
    }
    
    /**
     * Returns the current "transaction id" for quick searches
     * @return
     */
    protected final synchronized int curTXID() {
        return searchTXID;
    }
    
    public void changedUpdate(DocumentEvent e) {
        // do nothing
    }

    public void insertUpdate(DocumentEvent e) {
        if (eventLock)
            return;
        
        performQuickSearch();
    }

    public void removeUpdate(DocumentEvent e) {
        if (eventLock)
            return;
        
        performQuickSearch();
    }
    
    /**
     * Schedules the quick search to be performed asynchronously
     */
    protected void performQuickSearch() {
        clearQuickSearchButton.setEnabled(textQuickSearch.getText().length() > 0);
        
        getQuickSearchTimer().schedule(new TimerTask() {
            final int txID = nextTXID();
            
            @Override
            public void run() {
                if (txID == curTXID()) {
                    // Only perform the quick search if there is no newer one requested
                    SwingUtilities.invokeLater(new Runnable() {
                       public void run() {
                           if (txID == curTXID()) {
                               performActualQuickSearch();
                           }
                        } 
                    });
                }
            }
        }, QUICKSEARCH_DELAY);
    }
    
    /**
     * Performs the actual quick search synchronously
     */
    protected abstract void performActualQuickSearch();
    
    /**
     * Returns the component to focus when RETURN is pressed
     * @return
     */
    protected abstract Component getFocusComponent();
    
    public void actionPerformed(ActionEvent e) {
        String actCmd = e.getActionCommand();
        if (actCmd.equals("clear")) {
            textQuickSearch.setText("");
        } else if (actCmd.equals("focus")) {
            getFocusComponent().requestFocusInWindow();
        }
    }
    
    public JToolBar getQuickSearchBar(Action searchAction, String quickSearchTooltip, String resetTooltip) {
        return getQuickSearchBar(quickSearchTooltip, resetTooltip, searchAction);
    }
    
    public JToolBar getQuickSearchBar(String quickSearchTooltip, String resetTooltip, Action... additionalActions) {
        if (quickSearchBar == null) {
            quickSearchBar = new JToolBar();
            
            textQuickSearch = new JTextField(20);
            textQuickSearch.getDocument().addDocumentListener(this);
            textQuickSearch.setActionCommand("focus");
            textQuickSearch.addActionListener(this);
            textQuickSearch.setToolTipText(quickSearchTooltip);
            Dimension prefSize = textQuickSearch.getPreferredSize();
            prefSize.width = Integer.MAX_VALUE;
            prefSize.height += 4;
            textQuickSearch.setMaximumSize(prefSize);
            textQuickSearch.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            
            clearQuickSearchButton = new JButton(Utils._("Reset"));
            clearQuickSearchButton.setActionCommand("clear");
            clearQuickSearchButton.addActionListener(this);
            clearQuickSearchButton.setEnabled(false);
            clearQuickSearchButton.setToolTipText(resetTooltip);
            
            quickSearchBar.add(new JLabel(Utils._("Search") + ": "));
            quickSearchBar.add(textQuickSearch);
            quickSearchBar.add(clearQuickSearchButton);
            if (additionalActions != null && additionalActions.length > 0) {
                quickSearchBar.addSeparator();
                for (Action act : additionalActions) {
                    quickSearchBar.add(act);
                }
            }
        }
        return quickSearchBar;
    }
}
