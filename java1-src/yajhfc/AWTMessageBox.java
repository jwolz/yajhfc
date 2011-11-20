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
package yajhfc;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

/**
 * A simple message box in pure AWT
 * 
 * @author jonas
 *
 */
public class AWTMessageBox extends Dialog implements WindowListener,
        ActionListener {

    private Panel msgPanel;
    
    /**
     * @param title
     * @throws HeadlessException
     */
    public AWTMessageBox(Frame owner, String title) throws HeadlessException {
        super(owner, title, true);
        setLayout(new BorderLayout());
        msgPanel = new Panel();
        
        Button ok = new Button("OK");
        ok.addActionListener(this);
        Panel okPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        okPanel.add(ok);
        
        add(msgPanel, BorderLayout.CENTER);
        add(okPanel, BorderLayout.SOUTH);
        
        addWindowListener(this);
    }

   public void windowClosing(WindowEvent e) {
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        dispose();
    }
    
    public void showMsgBox(String message) {
        Vector lines = fastSplit(message, '\n');
        msgPanel.removeAll();
        msgPanel.setLayout(new GridLayout(lines.size(), 1, 0, 0));
        for (int i=0; i < lines.size(); i++) {
            Label label = new Label((String)lines.elementAt(i));
            msgPanel.add(label);
        }
        pack();
        show();
    }
    
    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}
    
    /**
     * Splits the String at the locations of splitChar (just like String.split()).
     * This should be much faster than String.split(), however.
     * @param str
     * @param splitChar
     * @return
     */
    public static Vector fastSplit(String str, char splitChar) {
        Vector resList = new Vector();
        
        int pos = 0;
        int charPos = str.indexOf(splitChar);        
        while (charPos > -1) {
            resList.addElement(str.substring(pos, charPos));
            pos = charPos + 1;
            charPos = str.indexOf(splitChar, pos);
        }
        // Do not include a trailing empty String
        if (pos < str.length()) {
            resList.addElement(str.substring(pos));
        }
        
        return resList;
    }
}
