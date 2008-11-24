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
package yajhfc;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * A simple message box in pure AWT
 * 
 * @author jonas
 *
 */
public class AWTMessageBox extends Dialog implements WindowListener,
        ActionListener {

    private Label msgLabel;
    
    /**
     * @param title
     * @throws HeadlessException
     */
    public AWTMessageBox(Frame owner, String title) throws HeadlessException {
        super(owner, title, true);
        setLayout(new BorderLayout());
        msgLabel = new Label();
        
        Button ok = new Button("OK");
        ok.addActionListener(this);
        Panel okPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        okPanel.add(ok);
        
        add(msgLabel, BorderLayout.CENTER);
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
        msgLabel.setText(message);
        pack();
        show();
    }
    
    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}
}
