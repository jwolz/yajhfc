package yajhfc.phonebook.jdbc;
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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import yajhfc.PasswordDialog;
import yajhfc.utils;

public final class ConnectionDialog extends JDialog implements ActionListener {

    private JComboBox comboName, comboGivenName, comboTitle, comboCompany, comboLocation, comboVoiceNumber, comboFaxNumber, comboComment;
    private ArrayList<JComboBox> fieldCombos = new ArrayList<JComboBox>();
    private JTextField textDriverClass, textURL, textUserName/*, textQuery*/;
    private JComboBox comboTable;
    private JCheckBox checkAskForPassword;
    private JPasswordField textPassword;
    private JButton buttonOK, buttonCancel, buttonTest;
    private ClipboardPopup clpPop;
    
    private final double border = 10;
    
    public boolean clickedOK;
    
    private Connection conn;
    private Statement stmt;
    
    private static String _(String key) {
        return utils._(key);
    }
    
    private JLabel addWithLabel(JPanel container, Component comp, String label, String layout) {
        JLabel lbl = new JLabel(label);
        lbl.setLabelFor(comp);
        
        TableLayoutConstraints c = new TableLayoutConstraints(layout);
        container.add(comp, c);
        
        c.col1 = c.col2 = c.col1 - 2;
        c.vAlign = TableLayoutConstraints.BOTTOM;
        c.hAlign = TableLayoutConstraints.LEFT;
        container.add(lbl, c);
        
        return lbl; 
    }
    private JComboBox addFieldCombo(JPanel container, String label, String layout) {
        JComboBox rv = new JComboBox();
        rv.setEditable(true);
        fieldCombos.add(rv);
        
        addWithLabel(container, rv, label, layout);
        return rv;
    }
    private void initialize() {
        double dLay[][] = {
                {border, TableLayout.PREFERRED, border, 0.5, border, TableLayout.PREFERRED, border, TableLayout.FILL, border},      
                new double[26]
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
        dLay[1][11] = dLay[1][len - 3] = 
        dLay[1][len-1] = TableLayout.PREFERRED;
        
        TableLayout lay = new TableLayout(dLay);
        JPanel jContentPane = new JPanel(lay);
        
        clpPop = new ClipboardPopup();
        
        textDriverClass = new JTextField();
        textDriverClass.addMouseListener(clpPop);
        
        textURL = new JTextField();
        textURL.addMouseListener(clpPop);
        
        textUserName = new JTextField();
        textUserName.addMouseListener(clpPop);
        
        textPassword = new JPasswordField();

        //textQuery = new JTextField();
        //textQuery.addMouseListener(clpPop);
        comboTable = new JComboBox();
        comboTable.setEditable(true);
        comboTable.setActionCommand("tablesel");
        comboTable.addActionListener(this);
        
        checkAskForPassword = new JCheckBox(_("Always ask"));
        checkAskForPassword.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                textPassword.setEnabled(!checkAskForPassword.isSelected());
            }
        });
        
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
        
        addWithLabel(jContentPane, textDriverClass, _("Driver class:"), "3, 1, 7, 1, f, c");
        addWithLabel(jContentPane, textURL, _("Database URL:"), "3, 3, 7, 3, f, c");
        addWithLabel(jContentPane, textUserName, _("Username:"), "3, 5, 5, 5, f, c");
        jContentPane.add(buttonTest, "7, 5");
        addWithLabel(jContentPane, textPassword, _("Password:"), "3, 7, 5, 7, f, c");
        jContentPane.add(checkAskForPassword, "7, 7");
        //addWithLabel(jContentPane, textQuery, _("Query:"), "3, 9, 7, 9, f, c");
        addWithLabel(jContentPane, comboTable, _("Table:"), "3, 9, 7, 9, f, c");
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 11, 8, 11");
        jContentPane.add(new JLabel(_("Please select which database fields correspond to the Phonebook entry fields of YajHFC:")), "1, 13, 7, 13, f, c");
        comboGivenName = addFieldCombo(jContentPane, _("Given name:"), "3, 15, f, c");
        comboName = addFieldCombo(jContentPane, _("Name:"), "7, 15, f, c");
        comboTitle = addFieldCombo(jContentPane, _("Title:"), "3, 17, f, c");
        comboCompany = addFieldCombo(jContentPane, _("Company:"), "7, 17, f, c");
        comboLocation = addFieldCombo(jContentPane, _("Location:"), "3, 19, f, c");
        comboVoiceNumber = addFieldCombo(jContentPane, _("Voice number:"), "7, 19, f, c");
        comboFaxNumber = addFieldCombo(jContentPane, _("Fax number:"), "3, 21, f, c");
        comboComment = addFieldCombo(jContentPane, _("Comments:"), "7, 21, f, c");
        
        Box buttonBox = new Box(BoxLayout.LINE_AXIS);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(buttonOK);
        buttonBox.add(Box.createHorizontalStrut((int)border));
        buttonBox.add(buttonCancel);
        buttonBox.add(Box.createHorizontalGlue());
        
        jContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 23, 8, 23");
        jContentPane.add(buttonBox, "0, 25, 8, 25");
        
        setContentPane(jContentPane);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closeConnection();
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });
        pack();
    }
    
    private boolean inputMaybeValid() {
        if (textDriverClass.getDocument().getLength() == 0) {
            JOptionPane.showMessageDialog(this, _("You have to specify a driver class."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (textURL.getDocument().getLength() == 0) {
            JOptionPane.showMessageDialog(this, _("You have to specify a database URL."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        /*if (comboTable.getSelectedItem().toString().length() == 0) {
            JOptionPane.showMessageDialog(this, _("You have to specify a SQL query."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }*/
        return true;
    }
    
    private boolean testConnection() {
        if (!inputMaybeValid())
            return false;
        closeConnection();
        
        try {
            Class.forName(textDriverClass.getText());
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(this, _("Could not load the specified driver class:"), e);
            return false;
        }
        try {
            String password;
            if (checkAskForPassword.isSelected()) {
                password = PasswordDialog.showPasswordDialog(this, _("Database password"), MessageFormat.format(_("Please enter the database password for user {0}.\nDatabase: {1}"), textUserName.getText(), textURL.getText()));
            } else {
                password = textPassword.getText();
            }
            conn = DriverManager.getConnection(textURL.getText(), textUserName.getText(), password);
            
            stmt = conn.createStatement(); // ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
            DatabaseMetaData dbmd = conn.getMetaData();
            
            ResultSet rs = dbmd.getTables(null, null, "%", new String[] { "TABLE", "VIEW" });
            Vector<String> tables = new Vector<String>();
            while (rs.next()) {
                tables.add(rs.getString(("TABLE_NAME")));
            }
            rs.close();
            
            Object o = comboTable.getSelectedItem();
            comboTable.setModel(new DefaultComboBoxModel(tables));
            if (o != null && !o.equals(""))
                comboTable.setSelectedItem(o);
            
            loadFieldNames();
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(this, _("Could not connect to the database:"), e);
            return false;
        }

        return true;
    }
    
    private void closeConnection() {
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (Exception e) {
            //NOP
        }
    }
    
    private boolean loadFieldNames() {
        if (conn == null || stmt == null)
            return false;
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + comboTable.getSelectedItem().toString());
            ResultSetMetaData rsmd = rs.getMetaData();
            
            Vector<String> fieldNames = new Vector<String>(rsmd.getColumnCount() + 1);
            fieldNames.add(ConnectionSettings.noField_translated);
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                fieldNames.add(rsmd.getColumnName(i));
            }
            for (JComboBox combo : fieldCombos) {
                Object o = combo.getSelectedItem();
                combo.setModel(new DefaultComboBoxModel(fieldNames));
                if (o != null && !o.equals(""))
                    combo.setSelectedItem(o);
            }
            
            rs.close();
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(this, _("Could not get the field names:"), e);
            return false;
        }
        return true;
    }
    
    public void actionPerformed(ActionEvent e) {
        String aCmd = e.getActionCommand();
        if (aCmd.equals("ok")) {
            if (!inputMaybeValid())
                return;
            if (comboTable.getSelectedItem().toString().length() == 0) {
                JOptionPane.showMessageDialog(this, _("You have to specify a database table."), _("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            clickedOK = true;
            setVisible(false);
        } else if (aCmd.equals("test")) {
            if (testConnection()) {
                JOptionPane.showMessageDialog(this, _("Connection to the database succeeded."));
            }
        } else if (aCmd.equals("tablesel")) {
            loadFieldNames();
        }
    }
    
    private void setFieldComboSel(JComboBox combo, String field) {
        if (field.equals(ConnectionSettings.noField)) {
            field = ConnectionSettings.noField_translated;
        }
        combo.setSelectedItem(field);
    }
    
    private String getFieldComboSel(JComboBox combo) {
        Object sel = combo.getSelectedItem();
        if (sel.equals(ConnectionSettings.noField_translated)) {
            return ConnectionSettings.noField;
        } else {
            return (String)sel;
        }
    }
    
    private void readFromConnectionSettings(ConnectionSettings src) {
        textDriverClass.setText(src.driver);
        textURL.setText(src.dbURL);
        textUserName.setText(src.user);
        textPassword.setText(src.pwd);
        //textQuery.setText(src.query);
        comboTable.setSelectedItem(src.table);
        
        checkAskForPassword.setSelected(src.askForPWD);
        
        setFieldComboSel(comboComment, src.comment);
        setFieldComboSel(comboCompany, src.company);
        setFieldComboSel(comboFaxNumber, src.faxNumber);
        setFieldComboSel(comboGivenName, src.givenName);
        setFieldComboSel(comboLocation, src.location);
        setFieldComboSel(comboName, src.name);
        setFieldComboSel(comboTitle, src.title);
        setFieldComboSel(comboVoiceNumber, src.voiceNumber);
    }
    
    private void writeToConnectionSettings(ConnectionSettings dst) {
        dst.driver = textDriverClass.getText();
        dst.dbURL = textURL.getText();
        dst.user = textUserName.getText();
        dst.pwd = textPassword.getText();
        dst.table = comboTable.getSelectedItem().toString();
        
        dst.askForPWD = checkAskForPassword.isSelected();
        
        dst.comment = getFieldComboSel(comboComment);
        dst.company = getFieldComboSel(comboCompany);
        dst.faxNumber = getFieldComboSel(comboFaxNumber);
        dst.givenName = getFieldComboSel(comboGivenName);
        dst.location = getFieldComboSel(comboLocation);
        dst.name = getFieldComboSel(comboName);
        dst.title = getFieldComboSel(comboTitle);
        dst.voiceNumber = getFieldComboSel(comboVoiceNumber);
    }
    
    public ConnectionDialog(Window owner) {
        super(owner, _("New JDBC phonebook"));
     
        initialize();
    }
    
    /**
     * Shows the dialog (initialized with the values of target)
     * and writes the user input into target if the user clicks "OK"
     * @param target
     * @return true if the user clicked "OK"
     */
    public boolean browseForPhonebook(ConnectionSettings target) {
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
