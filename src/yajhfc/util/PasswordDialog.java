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
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import yajhfc.Utils;

public class PasswordDialog extends JDialog {

    JPanel jContentFrame;
    JLabel labelPrompt;
    JTextField userField;
    JPasswordField passField;
    JButton btnOK, btnCancel;
    
    public String returnedPassword = null;
    public String returnedUsername = null;
    
    private void initialize(String prompt, String userName, boolean editableUserName, final boolean allowEmptyPassword) {
        final int border = 12;
        jContentFrame = new JPanel(new BorderLayout());
        
        labelPrompt = new JLabel(prompt, JLabel.CENTER);
        labelPrompt.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        
        passField = new JPasswordField(10);
        passField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        userField = new JTextField(10);
        userField.setText(userName);
        userField.setEditable(editableUserName);
        userField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        btnOK = new JButton(Utils._("OK"));
        btnOK.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               final char[] password = passField.getPassword();
               
               if (!allowEmptyPassword && password.length == 0) {
                   Toolkit.getDefaultToolkit().beep();
                   return;
               }
               
               returnedPassword = new String(password);
               returnedUsername = userField.getText();
               dispose();
            }             
        });

        CancelAction actCancel = new CancelAction(this);
        btnCancel = actCancel.createCancelButton();
        
        JLabel labelUserName = new JLabel(Utils._("Username:"));
        labelUserName.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        JLabel labelPassword = new JLabel(Utils._("Password:"));
        labelUserName.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        if (!allowEmptyPassword) {
            passField.getDocument().addDocumentListener(new DocumentListener() {

                public void changedUpdate(DocumentEvent e) {
                    //NOP
                }

                public void insertUpdate(DocumentEvent e) {
                    passwordChanged();                    
                }

                public void removeUpdate(DocumentEvent e) {
                    passwordChanged();  
                }
                
                private void passwordChanged() {
                    btnOK.setEnabled(passField.getDocument().getLength() > 0);
                }
            });
            btnOK.setEnabled(false);
        }
        
        Dimension boxy = new Dimension(border, border);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.NORTH);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.SOUTH);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.EAST);
        jContentFrame.add(Box.createRigidArea(boxy), BorderLayout.WEST);
        
        boxy = new Dimension(border, border);
        Box box = Box.createVerticalBox();
        box.add(labelPrompt);
        box.add(Box.createRigidArea(boxy));
        box.add(labelUserName);
        box.add(userField);
        box.add(Box.createRigidArea(boxy));
        box.add(labelPassword);
        box.add(passField);
        box.add(Box.createRigidArea(boxy));
        
        Box boxButtons = Box.createHorizontalBox();
        boxButtons.add(Box.createHorizontalGlue());
        boxButtons.add(btnOK);
        boxButtons.add(Box.createRigidArea(boxy));
        boxButtons.add(btnCancel);
        boxButtons.add(Box.createHorizontalGlue());
        boxButtons.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        box.add(boxButtons);
        
        jContentFrame.add(box, BorderLayout.CENTER);
        
        this.add(jContentFrame);
        this.setResizable(false);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(btnOK);
        this.pack();
        //this.setLocationByPlatform(true);
        
        if (editableUserName) {
            userField.requestFocusInWindow();
            userField.setSelectionStart(0);
            userField.setSelectionStart(userName.length());
        } else {
            passField.requestFocusInWindow();
        }
    }
    
    

    public PasswordDialog(Frame owner, String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword)  {
        super(owner, title, true);
        initialize(prompt, userName, editableUsername, allowEmptyPassword);       
        this.setLocationRelativeTo(owner);
    }

    public PasswordDialog(Dialog owner, String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword)  {
        super(owner, title, true);
        initialize(prompt, userName, editableUsername, allowEmptyPassword);
        this.setLocationRelativeTo(owner);
    }
    
    
    /**
     * Shows the password dialog and returns a tuple (username, password)
     * or null if the user selected cancel.
     * @param owner
     * @param title
     * @param prompt
     * @param userName
     * @param editableUsername
     * @return
     */
    public static String[] showPasswordDialog(Window owner, String title, String prompt, String userName, boolean editableUsername) {
        return showPasswordDialog(owner, title, prompt, userName, editableUsername, true);
    }
    
    /**
     * Shows the password dialog and returns a tuple (username, password)
     * or null if the user selected cancel.
     * @param owner
     * @param title
     * @param prompt
     * @param userName
     * @param editableUsername
     * @return
     */
    public static String[] showPasswordDialog(Window owner, String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword) {
        if (SwingUtilities.isEventDispatchThread()) {
            return showPasswordDialogUnthreaded(owner, title, prompt, userName, editableUsername, allowEmptyPassword);
        } else {
            return showPasswordDialogThreaded(owner, title, prompt, userName, editableUsername, allowEmptyPassword);
        }
    }
    
    /**
     * Shows the password dialog and returns a tuple (username, password)
     * or null if the user selected cancel.
     * @param owner
     * @param title
     * @param prompt
     * @param userName
     * @param editableUsername
     * @return
     */
    static String[] showPasswordDialogUnthreaded(Window owner, String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword) {
        PasswordDialog pdlg;
        if (owner instanceof Dialog)
            pdlg = new PasswordDialog((Dialog)owner, title, prompt, userName, editableUsername, allowEmptyPassword);
        else if (owner instanceof Frame)
            pdlg = new PasswordDialog((Frame)owner, title, prompt, userName, editableUsername, allowEmptyPassword);
        else
            return null;
        
        pdlg.setVisible(true);
        if (pdlg.returnedPassword != null) {
            return new String[] { pdlg.returnedUsername, pdlg.returnedPassword };
        } else {
            return null;
        }
    }
    /**
     * Shows the password dialog in the event dispatching thread and returns a tuple (username, password)
     * or null if the user selected cancel.
     * @param owner
     * @param title
     * @param prompt
     * @param userName
     * @param editableUsername
     * @return
     */
    private static String[] showPasswordDialogThreaded(Window owner, String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword) {
        DisplayRunnable runner = new DisplayRunnable(owner, title, prompt, userName, editableUsername, allowEmptyPassword);
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
        protected final Window owner;
        protected final String title;
        protected final String prompt;
        protected final boolean editableUsername;
        protected final String userName;
        protected final boolean allowEmptyPassword;
        
        public String[] result;
        
        public void run() {
            result = showPasswordDialogUnthreaded(owner, title, prompt, userName, editableUsername, allowEmptyPassword);
        }

        public DisplayRunnable(Window owner, String title, String prompt, String userName, boolean editableUsername, boolean allowEmptyPassword) {
            super();
            this.owner = owner;
            if (!(owner instanceof Frame || owner instanceof Dialog)) {
                throw new IllegalArgumentException("owner must be of type Dialog or Frame!");
            }
            this.title = title;
            this.prompt = prompt;
            this.userName = userName;
            this.editableUsername = editableUsername;
            this.allowEmptyPassword = allowEmptyPassword;
        }
    }
}
