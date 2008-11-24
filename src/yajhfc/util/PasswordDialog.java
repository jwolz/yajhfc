package yajhfc.util;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import yajhfc.Utils;

public class PasswordDialog extends JDialog {

    private JPanel jContentFrame;
    private JLabel labelPrompt;
    private JPasswordField passField;
    private JButton btnOK, btnCancel;
    
    public String returnValue = null;
    
    private void initialize(String prompt) {
        final int border = 12;
        jContentFrame = new JPanel(new BorderLayout());
        
        labelPrompt = new JLabel(prompt, JLabel.CENTER);
        labelPrompt.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        passField = new JPasswordField(10);
        
        btnOK = new JButton(Utils._("OK"));
        btnOK.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               returnValue = new String(passField.getPassword());
               dispose();
            }             
        });

        CancelAction actCancel = new CancelAction(this);
        btnCancel = actCancel.createCancelButton();
        
        Dimension boxy = new Dimension(border, border);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.NORTH);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.SOUTH);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.EAST);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.WEST);
        
        Box box = Box.createVerticalBox();
        box.add(labelPrompt);
        box.add(Box.createRigidArea(boxy));
        box.add(passField);
        box.add(Box.createRigidArea(boxy));
        
        Box boxButtons = Box.createHorizontalBox();
        boxButtons.add(Box.createHorizontalGlue());
        boxButtons.add(btnOK);
        boxButtons.add(Box.createRigidArea(boxy));
        boxButtons.add(btnCancel);
        boxButtons.add(Box.createHorizontalGlue());
        
        box.add(boxButtons);
        
        jContentFrame.add(box, BorderLayout.CENTER);
        
        this.add(jContentFrame);
        this.setResizable(false);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(btnOK);
        this.pack();
        //this.setLocationByPlatform(true);
    }
    
    

    public PasswordDialog(Frame owner, String title, String prompt)  {
        super(owner, title, true);
        initialize(prompt);       
        this.setLocationRelativeTo(owner);
    }

    public PasswordDialog(Dialog owner, String title, String prompt)  {
        super(owner, title, true);
        initialize(prompt);
        this.setLocationRelativeTo(owner);
    }
    
    public static String showPasswordDialog(Frame owner, String title, String prompt) {
        PasswordDialog pdlg = new PasswordDialog(owner, title, prompt);
        pdlg.setVisible(true);
        return pdlg.returnValue;
    }

    public static String showPasswordDialog(Dialog owner, String title, String prompt) {
        PasswordDialog pdlg = new PasswordDialog(owner, title, prompt);
        pdlg.setVisible(true);
        return pdlg.returnValue;
    }
        
    public static String showPasswordDialogThreaded(Window owner, String title, String prompt) {
        DisplayRunnable runner = new DisplayRunnable(owner, title, prompt);
        try {
            SwingUtilities.invokeAndWait(runner);
            return runner.result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Implements an Runnable that may be used in conjunction with the SwingUtilities.invoke*()
     * to display an password dialog from another thread 
     * @author jonas
     *
     */
    public static class DisplayRunnable implements Runnable {
        protected Window owner;
        protected String title;
        protected String prompt;
        
        public String result;
        
        public void run() {
            result = null;
            if (owner instanceof Dialog) {
                result = showPasswordDialog((Dialog)owner, title, prompt);
            } else if (owner instanceof Frame) {
                result = showPasswordDialog((Frame)owner, title, prompt);
            }
        }

        public DisplayRunnable(Window owner, String title, String prompt) {
            super();
            this.owner = owner;
            if (!(owner instanceof Frame || owner instanceof Dialog)) {
                throw new IllegalArgumentException("owner must be of type Dialog or Frame!");
            }
            this.title = title;
            this.prompt = prompt;
        }
    }
}
