/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2013 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.model.servconn.directaccess.fritz;

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import yajhfc.Utils;
import yajhfc.options.OptionsWin;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ComponentEnabler;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.IntVerifier;

/**
 * @author jonas
 *
 */
public class FritzConfigDialog extends JDialog {
    JTextField textHostname;
    JTextField textPort;
    JTextField textUser ;
    JPasswordField textPassword;
    JCheckBox checkAlwaysAsk;
    JCheckBox checkPassive;
    JTextField textFaxboxDir ;
    
    JTextField textFaxPattern;
    JTextField textFaxDateFormat;
    
    CancelAction cancelAction;
    Action okAction;
    
    boolean okClicked = false;
    
    /**
     * @param owner
     */
    public FritzConfigDialog(Window owner) {
        super(owner, _("Fritz!Box configuration"), Dialog.DEFAULT_MODALITY_TYPE);
        initialize();
    }


    private void initialize() {
        textHostname = new JTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textHostname);
        textPort = new JTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textPort);
        textPort.setInputVerifier(new IntVerifier(1, 65536));
        
        textUser = new JTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textUser);
        textPassword = new JPasswordField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textPassword);
        textFaxboxDir = new JTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textFaxboxDir);
        textFaxPattern = new JTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textFaxPattern);
        textFaxDateFormat = new JTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textFaxDateFormat);
        
        checkAlwaysAsk = new JCheckBox(_("Always ask"));
        checkPassive = new JCheckBox(_("Use passive mode"));
        
        cancelAction = new CancelAction(this);
        okAction = new ExcDialogAbstractAction(_("OK")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (validateSettings()) {
                    okClicked = true;
                    setVisible(false);
                }
            }
        };
        
        double[][] dLay = {
                {OptionsWin.border, 0.5, OptionsWin.border, 0.25, OptionsWin.border, TableLayout.FILL, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border}
        };
        
        JPanel contentPane = new JPanel(new TableLayout(dLay));
        Utils.addWithLabel(contentPane, textHostname, _("Host name:"), "1,2,3,2,f,c");
        Utils.addWithLabel(contentPane, textPort, _("Port:"), "5,2,5,2,f,c");
        contentPane.add(checkPassive, "7,2,7,2,l,c");
        Utils.addWithLabel(contentPane, textUser, _("Username:"), "1,5,1,5,f,c");
        JLabel labelPassword = Utils.addWithLabel(contentPane, textPassword, _("Password:"), "3,5,5,5,f,c");
        contentPane.add(checkAlwaysAsk, "7,5,7,5,l,c");
        Utils.addWithLabel(contentPane, textFaxboxDir, _("Path to \"faxbox\" directory:"), "1,8,7,8,f,c");
        Utils.addWithLabel(contentPane, textFaxPattern, _("File name pattern:"), "1,11,3,11,f,c");
        Utils.addWithLabel(contentPane, textFaxDateFormat, _("Date format in file name:"), "5,11,7,11,f,c");
        
        contentPane.add(new JSeparator(), "0,14,8,14,f,c");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, OptionsWin.border, OptionsWin.border), false);
        buttonPanel.add(new JButton(okAction));
        buttonPanel.add(cancelAction.createCancelButton());
        
        contentPane.add(buttonPanel, "0,16,8,16,f,f");
        
        ComponentEnabler.installOn(checkAlwaysAsk, false, textPassword, labelPassword);
        
        setContentPane(contentPane);
        setLocationRelativeTo(getOwner());
        pack();
    }
    
    /**
     * Loads the settings from the options
     * @param foEdit
     */
    public void loadSettings(FritzFaxConfig ffc) {
        textHostname.setText(ffc.hostname);
        textPort.setText(String.valueOf(ffc.port));
        textUser.setText(ffc.user);
        textPassword.setText(ffc.pass.getPassword());
        textFaxboxDir.setText(ffc.faxboxDir);
        textFaxPattern.setText(ffc.faxPattern);
        textFaxDateFormat.setText(ffc.faxDateFormat);
        
        checkAlwaysAsk.setSelected(ffc.alwaysAsk);
        checkPassive.setSelected(ffc.passive);
    }
    /**
     * Saves the settings to the options.
     * @param foEdit
     */
    public void saveSettings(FritzFaxConfig ffc) {
        ffc.hostname = textHostname.getText();
        ffc.port = Integer.parseInt(textPort.getText());
        ffc.user = textUser.getText();
        ffc.pass.setPassword(new String(textPassword.getPassword()));
        
        ffc.faxboxDir = textFaxboxDir.getText();
        ffc.faxPattern = textFaxPattern.getText();
        ffc.faxDateFormat = textFaxDateFormat.getText();
        
        ffc.alwaysAsk = checkAlwaysAsk.isSelected();
        ffc.passive = checkPassive.isSelected();
    }

    /**
     * Validates the user's settings
     * @param optionsWin
     * @return true if settings are valid, false otherwise
     */
    public boolean validateSettings() {
        if (textHostname.getText().length() == 0) {
            textHostname.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("Please enter a host name."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (textUser.getText().length() == 0) {
            textUser.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("Please enter a user name."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String port = textPort.getText();
        boolean valid = true;
        if (port.length() == 0) {
            valid = false;
        } else {
            try {
                int iPort = Integer.parseInt(port);
                valid = (iPort > 0 && iPort < 65536);
            } catch (NumberFormatException e) {
                valid = false;
            }
        }
        if (!valid) {
            textPort.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, _("Please enter a valid port number."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        
        return true;
    }
    
    /**
     * Shows the config dialog
     * @param configData
     * @return true if the user clicked OK, false for cancel
     */
    public static boolean showConfigDialog(Window parent, FritzFaxConfig configData) {
        FritzConfigDialog fcd = new FritzConfigDialog(parent);
        fcd.loadSettings(configData);
        fcd.setVisible(true);
        if (fcd.okClicked) {
            fcd.saveSettings(configData);
            fcd.dispose();
            return true;
        } else {
            return false;
        }
    }
}
