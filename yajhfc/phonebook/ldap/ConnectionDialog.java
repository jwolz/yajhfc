package yajhfc.phonebook.ldap;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import yajhfc.ClipboardPopup;
import yajhfc.ExceptionDialog;
import yajhfc.IntVerifier;
import yajhfc.PasswordDialog;
import yajhfc.utils;

public final class ConnectionDialog extends JDialog implements ActionListener {

    private JTextField textName, textGivenName, textTitle, textCompany, textLocation, textVoiceNumber, textFaxNumber, textComment;
    private JTextField textServerName, textPort, textBaseDN, textBindDN, textFilter;
    private JCheckBox checkAskForPassword, checkDoAuth, checkSearchSubtree;
    private JPasswordField textPassword;
    private JButton buttonOK, buttonCancel, buttonTest;
    private ClipboardPopup clpPop;
    
    private final double border = 10;
    
    public boolean clickedOK;
    
    private static String _(String key) {
        return utils._(key);
    }
    
    private JLabel addWithLabel(JPanel container, Component comp, String label, String layout) {
        JLabel lbl = new JLabel(label);
        lbl.setLabelFor(comp);
        
        TableLayoutConstraints c = new TableLayoutConstraints(layout);
        container.add(comp, c);
        
        c.col1 = c.col2 = c.col1 - 2;
        c.vAlign = TableLayoutConstraints.CENTER;
        c.hAlign = TableLayoutConstraints.LEFT;
        container.add(lbl, c);
        
        return lbl; 
    }

    private JTextField addTextField(JPanel container, String label, String layout) {
        JTextField rv = new JTextField();
        rv.addMouseListener(clpPop);
        addWithLabel(container, rv, label, layout);
        return rv;
    }
    
    private void initialize() {
        double dLay[][] = {
                {border, TableLayout.PREFERRED, border, 0.5, border, TableLayout.PREFERRED, border, TableLayout.FILL, border},      
                new double[31]
        };
        int len = dLay[1].length;
        double rowh = 1/(double)(len-5)*2;
        for (int i = 0; i < len; i++) {
            if ((i&1) == 0) {
                dLay[1][i] = border;
            } else {
                dLay[1][i] = rowh;
            }
        }
        dLay[1][11] = dLay[1][15] = dLay[1][len - 4] = 
        dLay[1][len - 2] = TableLayout.PREFERRED; //Separators/Labels
        
        dLay[1][1] = TableLayout.FILL;
        
        TableLayout lay = new TableLayout(dLay);
        JPanel jContentPane = new JPanel(lay);
        
        clpPop = new ClipboardPopup();
        
        textServerName = new JTextField();
        textServerName.addMouseListener(clpPop);
        
        textPort = new JTextField();
        textPort.setInputVerifier(new IntVerifier());
        textPort.addMouseListener(clpPop);
        
        textBaseDN = new JTextField();
        textBaseDN.addMouseListener(clpPop);
        
        checkDoAuth = new JCheckBox(_("Use authentication"));
        
        textBindDN = new JTextField();
        textBindDN.addMouseListener(clpPop);
        
        textPassword = new JPasswordField();
        
        checkAskForPassword = new JCheckBox(_("Always ask"));
        
        ChangeListener credentialListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean useAuth = checkDoAuth.isSelected();
                textBindDN.setEnabled(useAuth);
                checkAskForPassword.setEnabled(useAuth);
                textPassword.setEnabled(useAuth && !checkAskForPassword.isSelected());
            }
        };
        checkAskForPassword.addChangeListener(credentialListener);
        checkDoAuth.addChangeListener(credentialListener);
        credentialListener.stateChanged(null);
        
        textFilter = new JTextField();
        textFilter.addMouseListener(clpPop);
        textFilter.setToolTipText(utils._("RFC 2254 Filter expression (e.g. \"objectClass=person\") selecting the directory entries to include. Leave blank to include all."));
        
        checkSearchSubtree = new JCheckBox(_("Also search subtrees"));
        
        Action actCancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            };
        };
        actCancel.putValue(Action.NAME, _("Cancel"));
        buttonCancel = new JButton(actCancel);
        buttonCancel.getActionMap().put("EscapePressed", actCancel);
        buttonCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapePressed");
        
        buttonOK = new JButton(_("OK"));
        buttonOK.setActionCommand("ok");
        buttonOK.addActionListener(this);
        buttonTest = new JButton(_("Test connection"));
        buttonTest.setActionCommand("test");
        buttonTest.addActionListener(this);
        
        addWithLabel(jContentPane, textServerName, _("Server name:"), "3, 1, f, c");
        addWithLabel(jContentPane, textPort, _("Port:"), "7, 1, f, c");
        addWithLabel(jContentPane, textBaseDN, _("Root (Base DN):"), "3, 3, 5, 3, f, c");
        jContentPane.add(buttonTest, "7, 3");
        jContentPane.add(checkDoAuth, "1, 5, 3, 5, f, c");
        addWithLabel(jContentPane, textBindDN, _("User name (Bind DN):"), "3, 7, 7, 7, f, c");
        addWithLabel(jContentPane, textPassword, _("Password:"), "3, 9, 5, 9, f, c");
        jContentPane.add(checkAskForPassword, "7, 9, f, c");

        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 11, 8, 11");
        
        addWithLabel(jContentPane, textFilter, _("Object filter:"), "3, 13, 5, 13, f, c");
        jContentPane.add(checkSearchSubtree, "7, 13");
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 15, 8, 15");
        
        jContentPane.add(new JLabel(_("Please enter which LDAP attributes correspond to the Phonebook entry fields of YajHFC (default should usually work):")), "1, 17, 7, 17, f, c");
        textGivenName = addTextField(jContentPane, _("Given name:"), "3, 19, f, c");
        textName = addTextField(jContentPane, _("Name:"), "7, 19, f, c");
        textTitle = addTextField(jContentPane, _("Title:"), "3, 21, f, c");
        textCompany = addTextField(jContentPane, _("Company:"), "7, 21, f, c");
        textLocation = addTextField(jContentPane, _("Location:"), "3, 23, f, c");
        textVoiceNumber = addTextField(jContentPane, _("Voice number:"), "7, 23, f, c");
        textFaxNumber = addTextField(jContentPane, _("Fax number:"), "3, 25, f, c");
        textComment = addTextField(jContentPane, _("Comments:"), "7, 25, f, c");
        
        Box buttonBox = new Box(BoxLayout.LINE_AXIS);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(buttonOK);
        buttonBox.add(Box.createHorizontalStrut((int)border));
        buttonBox.add(buttonCancel);
        buttonBox.add(Box.createHorizontalGlue());
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 27, 8, 27");
        jContentPane.add(buttonBox, "0, 29, 8, 29");
        
        setContentPane(jContentPane);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosed(WindowEvent e) {
//                closeConnection();
//            }
//            
//            @Override
//            public void windowClosing(WindowEvent e) {
//                closeConnection();
//            }
//        });
        pack();
    }
    
    private boolean inputMaybeValid() {
        boolean portValid;
        try {
            int port = Integer.parseInt(textPort.getText());
            portValid = (port > 0 && port < 65536);
        } catch (NumberFormatException e) {
            portValid = false;
        }
        if (!portValid || textServerName.getDocument().getLength() == 0) {
            JOptionPane.showMessageDialog(this, _("You have to specify a server name and port."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        /*if (textBaseDN.getDocument().getLength() == 0) {
            JOptionPane.showMessageDialog(this, _("You have to specify a Base DN."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }*/
        return true;
    }
    
    private boolean testConnection() {
        if (!inputMaybeValid())
            return false;

        try {
            Hashtable<String,String> env = new Hashtable<String,String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://" + textServerName.getText()  + ":" + textPort.getText() +  "/" + textBaseDN.getText());
            
            if (checkDoAuth.isSelected()) {
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, textBindDN.getText());
                
                String password;
                if (checkAskForPassword.isSelected()) {
                    password = PasswordDialog.showPasswordDialog(this, utils._("LDAP password"), MessageFormat.format(utils._("Please enter the LDAP password for user {0}."), textBindDN.getText()));
                    if (password == null)
                        return false;
                } else {
                    password = new String(textPassword.getPassword());
                }
                env.put(Context.SECURITY_CREDENTIALS, password);
            } else {
                env.put(Context.SECURITY_AUTHENTICATION, "none");
            }
            
            DirContext ctx = new InitialDirContext(env); 

            SearchControls sctl = new SearchControls();
            if (checkSearchSubtree.isSelected()) 
                sctl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            else
                sctl.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            
            sctl.setReturningAttributes(new String[] {});
            
            String filter = textFilter.getText();
            if (filter.length() == 0)
                filter = "(objectClass=*)";

            NamingEnumeration<SearchResult> res = ctx.search("", filter, sctl);

            res.close();
            
            ctx.close();
            
            return true;
        } catch (NamingException e) {
            ExceptionDialog.showExceptionDialog(this, _("Could not connect to the LDAP server:"), e);
            return false;
        }
    }
    
    

    
    public void actionPerformed(ActionEvent e) {
        String aCmd = e.getActionCommand();
        if (aCmd.equals("ok")) {
            if (!inputMaybeValid())
                return;

            
            clickedOK = true;
            setVisible(false);
        } else if (aCmd.equals("test")) {
            if (testConnection()) {
                JOptionPane.showMessageDialog(this, _("Connection to the LDAP server succeeded."));
            }
        }
    }
    

    
    private void readFromConnectionSettings(LDAPSettings src) {
        textBaseDN.setText(src.baseDN);
        textBindDN.setText(src.bindDN);
        textComment.setText(src.comment);
        textCompany.setText(src.company);
        textFaxNumber.setText(src.faxNumber);
        textFilter.setText(src.objectFilter);
        textGivenName.setText(src.givenName);
        textLocation.setText(src.location);
        textName.setText(src.name);
        textPassword.setText(src.credential);
        textPort.setText(Integer.toString(src.port));
        textServerName.setText(src.serverName);
        textTitle.setText(src.title);
        textVoiceNumber.setText(src.voiceNumber);
        
        checkAskForPassword.setSelected(src.askForCredential);
        checkDoAuth.setSelected(src.useAuth);
        checkSearchSubtree.setSelected(src.searchSubTree);
    }
    
    private void writeToConnectionSettings(LDAPSettings dst) {
        dst.baseDN = textBaseDN.getText();
        dst.bindDN = textBindDN.getText();
        dst.comment = textComment.getText();
        dst.company = textCompany.getText();
        dst.faxNumber = textFaxNumber.getText();
        dst.objectFilter = textFilter.getText();
        dst.givenName = textGivenName.getText();
        dst.location = textLocation.getText();
        dst.name = textName.getText();
        dst.credential = new String(textPassword.getPassword());
        dst.port = Integer.parseInt(textPort.getText());
        dst.serverName = textServerName.getText();
        dst.title = textTitle.getText();
        dst.voiceNumber = textVoiceNumber.getText();
        
        dst.askForCredential = checkAskForPassword.isSelected();
        dst.useAuth = checkDoAuth.isSelected();
        dst.searchSubTree = checkSearchSubtree.isSelected();
    }
    
    public ConnectionDialog(Frame owner) {
        super(owner, _("New LDAP phonebook"));
     
        initialize();
    }
    public ConnectionDialog(Dialog owner) {
        super(owner, _("New LDAP phonebook"));
     
        initialize();
    }
    
    /**
     * Shows the dialog (initialized with the values of target)
     * and writes the user input into target if the user clicks "OK"
     * @param target
     * @return true if the user clicked "OK"
     */
    public boolean browseForPhonebook(LDAPSettings target) {
        readFromConnectionSettings(target);
        clickedOK = false;
        setVisible(true);
        if (clickedOK) {
            writeToConnectionSettings(target);
        }
        dispose();
        return clickedOK;
    }
}
