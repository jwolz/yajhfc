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

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.IntVerifier;
import yajhfc.util.PasswordDialog;

public final class ConnectionDialog extends JDialog implements ActionListener {
    JTextField textServerName, textPort, textBaseDN, textBindDN, textFilter, textDisplayCaption, textCountLimit;
    JCheckBox checkAskForPassword, checkDoAuth, checkSearchSubtree, checkInitiallyShowAll;
    JPasswordField textPassword;
    private JButton buttonOK, buttonCancel, buttonTest;
    
    private EnumMap<PBEntryField,JTextField> mappingFields = new EnumMap<PBEntryField, JTextField>(PBEntryField.class);
    
    private final static int border = 10;
    
    public boolean clickedOK;
    
    private JTextField addTextField(JPanel container, PBEntryField field, TableLayoutConstraints layout) {
        JTextField rv = new JTextField(20);
        rv.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        String label = field.getDescription() + ":";
        Utils.addWithLabelHorz(container, rv, label, layout);
        mappingFields.put(field, rv);
        return rv;
    }
    
    private void initialize() {
        final int rowCount = 29 + ((PBEntryField.FIELD_COUNT + 1)/2)*2;
        double dLay[][] = {
                {border, TableLayout.PREFERRED, border, 0.5, border, TableLayout.PREFERRED, border, TableLayout.FILL, border},      
                new double[rowCount]
        };
        double rowh = 1/(double)(rowCount-9)*2;
        for (int i = 0; i < rowCount; i++) {
            if ((i&1) == 0) {
                dLay[1][i] = border;
            } else {
                dLay[1][i] = rowh;
            }
        }
        dLay[1][11] = dLay[1][15] = dLay[1][21] = dLay[1][rowCount - 4] = 
        dLay[1][rowCount - 2] = TableLayout.PREFERRED; //Separators/Labels
        
        dLay[1][1] = TableLayout.FILL;
        
        TableLayout lay = new TableLayout(dLay);
        JPanel jContentPane = new JPanel(lay);
        
        textServerName = new JTextField(10);
        textServerName.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textPort = new JTextField(5);
        textPort.setInputVerifier(new IntVerifier());
        textPort.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textBaseDN = new JTextField(2);
        textBaseDN.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        checkDoAuth = new JCheckBox(_("Use authentication"));
        
        textBindDN = new JTextField(20);
        textBindDN.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textPassword = new JPasswordField(2);
        
        textDisplayCaption = new JTextField(16);
        textDisplayCaption.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
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
        
        textFilter = new JTextField(2);
        textFilter.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        textFilter.setToolTipText(Utils._("RFC 2254 Filter expression (e.g. \"objectClass=person\") selecting the directory entries to include. Leave blank to include all."));
        
        checkSearchSubtree = new JCheckBox(_("Also search subtrees"));
        
        textCountLimit = new JTextField(5);
        textCountLimit.setInputVerifier(new IntVerifier());
        textCountLimit.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        textCountLimit.setToolTipText(_("The maximum number of items loaded"));
        
        checkInitiallyShowAll = new JCheckBox(_("Load all entries when opened"));
        checkInitiallyShowAll.setToolTipText(_("If checked, all entries in the directory are loaded when the phone book is opened. If not, entries are loaded only if a quick search is performed."));
        
        CancelAction actCancel = new CancelAction(this);
        buttonCancel = actCancel.createCancelButton();
        
        buttonOK = new JButton(_("OK"));
        buttonOK.setActionCommand("ok");
        buttonOK.addActionListener(this);
        buttonTest = new JButton(_("Test connection"));
        buttonTest.setActionCommand("test");
        buttonTest.addActionListener(this);
        
        Utils.addWithLabelHorz(jContentPane, textServerName, _("Server name:"), "3, 1, f, c");
        Utils.addWithLabelHorz(jContentPane, textPort, _("Port:"), "7, 1, f, c");
        Utils.addWithLabelHorz(jContentPane, textBaseDN, _("Root (Base DN):"), "3, 3, 5, 3, f, c");
        jContentPane.add(buttonTest, "7, 3");
        jContentPane.add(checkDoAuth, "1, 5, 3, 5, f, c");
        Utils.addWithLabelHorz(jContentPane, textBindDN, _("User name (Bind DN):"), "3, 7, 7, 7, f, c");
        Utils.addWithLabelHorz(jContentPane, textPassword, _("Password:"), "3, 9, 5, 9, f, c");
        jContentPane.add(checkAskForPassword, "7, 9, f, c");
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 11, 8, 11");
        
        Utils.addWithLabelHorz(jContentPane, textDisplayCaption, _("Phone book name to display:"), "3, 13, 7, 13, f, c");
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 15, 8, 15");
        
        Utils.addWithLabelHorz(jContentPane, textFilter, _("Object filter:"), "3, 17, 5, 17, f, c");
        jContentPane.add(checkSearchSubtree, "7, 17");
        
        jContentPane.add(checkInitiallyShowAll, "1,19,3,19,f,c");
        Utils.addWithLabelHorz(jContentPane, textCountLimit, _("Count limit:"), "7,19,f,c");
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 21, 8, 21");
        
        jContentPane.add(new JLabel("<html>" + _("Please enter which LDAP attributes correspond to the phone book entry fields of YajHFC (default should usually work):") + "</html>"), "1, 23, 7, 23, f, c");
        
        int row = 25;
        int col = 3;
        for (PBEntryField field : PBEntryField.values()) {
            addTextField(jContentPane, field, new TableLayoutConstraints(col,row,col,row,TableLayoutConstraints.FULL,TableLayoutConstraints.CENTER));
            if (col == 3) {
                col = 7;
            } else {
                col = 3;
                row += 2;
            }
        }
        
        Box buttonBox = new Box(BoxLayout.LINE_AXIS);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(buttonOK);
        buttonBox.add(Box.createHorizontalStrut(border));
        buttonBox.add(buttonCancel);
        buttonBox.add(Box.createHorizontalGlue());
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), new TableLayoutConstraints(0, rowCount-4, 8, rowCount-4)); 
        jContentPane.add(buttonBox, new TableLayoutConstraints(0, rowCount-2, 8, rowCount-2)); 
        
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
            env.put(Context.PROVIDER_URL, "ldap://" + textServerName.getText()  + ":" + textPort.getText() +  "/" + LDAPSettings.sanitizeDN(textBaseDN.getText()));
            
            if (checkDoAuth.isSelected()) {
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, LDAPSettings.sanitizeDN(textBindDN.getText()));
                
                String password;
                if (checkAskForPassword.isSelected()) {
                    String[] pwd = PasswordDialog.showPasswordDialog(this, Utils._("LDAP password"), Utils._("Please enter the LDAP password:"), textBindDN.getText(), false);
                    if (pwd == null)
                        return false;
                    else
                        password = pwd[1];
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
        textFilter.setText(src.objectFilter);
        textPassword.setText(src.credential.getPassword());
        textPort.setText(Integer.toString(src.port));
        textServerName.setText(src.serverName);
        textDisplayCaption.setText(src.displayCaption);
        textCountLimit.setText(Integer.toString(src.countLimit));
        
        for (Map.Entry<PBEntryField, JTextField> entry : mappingFields.entrySet()) {
            entry.getValue().setText(src.getMappingFor(entry.getKey()));
        }
        
        checkAskForPassword.setSelected(src.askForCredential);
        checkDoAuth.setSelected(src.useAuth);
        checkSearchSubtree.setSelected(src.searchSubTree);
        checkInitiallyShowAll.setSelected(src.initiallyLoadAll);
    }
    
    private void writeToConnectionSettings(LDAPSettings dst) {
        dst.baseDN = textBaseDN.getText();
        dst.bindDN = textBindDN.getText();
        dst.objectFilter = textFilter.getText();
        dst.credential.setPassword(new String(textPassword.getPassword()));
        dst.port = Integer.parseInt(textPort.getText());
        dst.serverName = textServerName.getText();
        dst.displayCaption = textDisplayCaption.getText();
        dst.countLimit = Integer.parseInt(textCountLimit.getText());
        
        for (Map.Entry<PBEntryField, JTextField> entry : mappingFields.entrySet()) {
            dst.setMappingFor(entry.getKey(), entry.getValue().getText());
        }
        
        dst.askForCredential = checkAskForPassword.isSelected();
        dst.useAuth = checkDoAuth.isSelected();
        dst.searchSubTree = checkSearchSubtree.isSelected();
        dst.initiallyLoadAll = checkInitiallyShowAll.isSelected();
    }
    
    public ConnectionDialog(Frame owner) {
        super(owner, _("New LDAP phone book"));
     
        initialize();
    }
    public ConnectionDialog(Dialog owner) {
        super(owner, _("New LDAP phone book"));
     
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
