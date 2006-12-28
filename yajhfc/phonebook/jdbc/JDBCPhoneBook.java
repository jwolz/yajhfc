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

import java.awt.Dialog;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import yajhfc.ExceptionDialog;
import yajhfc.PasswordDialog;
import yajhfc.utils;
import yajhfc.phonebook.DefaultPhoneBookEntryComparator;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;

public class JDBCPhoneBook extends PhoneBook {
    ConnectionSettings settings;
    Connection connection;
    ArrayList<JDBCPhoneBookEntry> items = new ArrayList<JDBCPhoneBookEntry>();
    ArrayList<JDBCPhoneBookEntry> deleted_items = new ArrayList<JDBCPhoneBookEntry>();
    ArrayList<DBKey> rowId = new ArrayList<DBKey>();
    
    //PreparedStatement insertStmt, deleteStmt, updateStmt/*, selectStmt*/;
    
    public static final String PB_Prefix = "JDBC";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = utils._("JDBC Phonebook"); // A user-readable name for this Phonebook type
    public static final String PB_Description = utils._("A Phonebook saving its entries in a relational database using JDBC."); // A user-readable description of this Phonebook type
    
    public JDBCPhoneBook(Dialog parent) {
        super(parent);
        
    }

    @Override
    public PhoneBookEntry addNewEntry() {
        JDBCPhoneBookEntry pb = new JDBCPhoneBookEntry(this);
        int pos = getInsertionPos(pb);
        items.add(pos, pb);
        fireIntervalAdded(this, pos, pos);
        return pb;
    }

    @Override
    public String browseForPhoneBook() {
        ConnectionDialog cDlg = new ConnectionDialog(parentDialog);
        ConnectionSettings cs = new ConnectionSettings(settings);
        if (cDlg.browseForPhonebook(cs))
            return PB_Prefix + ":" + cs.saveToString();
        else
            return null;
    }

    @Override
    public void close() {
        /*if (selectStmt != null) {
            try {
                selectStmt.close();
            } catch (Exception e) {
                //NOP
            }
            selectStmt = null;
        }
        if (insertStmt != null) {
            try {
                insertStmt.close();
            } catch (Exception e) {
                //NOP
            }
            insertStmt = null;
        }
        if (updateStmt != null) {
            try {
                updateStmt.close();
            } catch (Exception e) {
                //NOP
            }
            updateStmt = null;
        }
        if (deleteStmt != null) {
            try {
                deleteStmt.close();
            } catch (Exception e) {
                //NOP
            }
            deleteStmt = null;
        }*/
        
        if (connection != null) {
            commitToDB();
            
            try {
                connection.close();
            } catch (Exception e) {
                //NOP
            }
            connection = null;
        }
    }

    private boolean isDataField(String fieldName) {
        return
        (fieldName.equalsIgnoreCase(settings.name)) ||
        (fieldName.equalsIgnoreCase(settings.givenName)) ||
        (fieldName.equalsIgnoreCase(settings.comment)) ||
        (fieldName.equalsIgnoreCase(settings.company)) ||
        (fieldName.equalsIgnoreCase(settings.faxNumber)) ||
        (fieldName.equalsIgnoreCase(settings.location)) ||
        (fieldName.equalsIgnoreCase(settings.title)) ||
        (fieldName.equalsIgnoreCase(settings.voiceNumber));
    }
    
    private int appendFieldList(StringBuilder s, String suffix, String separator) {
        int fieldCount = 0;
        if (!ConnectionSettings.isNoField(settings.name)) {
            s.append(settings.name).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.givenName)) {
            s.append(settings.givenName).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.company)) {
            s.append(settings.company).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.location)) {
            s.append(settings.location).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.title)) {
            s.append(settings.title).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.faxNumber)) {
            s.append(settings.faxNumber).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.voiceNumber)) {
            s.append(settings.voiceNumber).append(suffix).append(separator);
            fieldCount++;
        }
        if (!ConnectionSettings.isNoField(settings.comment)) {
            s.append(settings.comment).append(suffix).append(separator);
            fieldCount++;
        }
        s.setLength(s.length()-separator.length()); // Strip last separator
        
        return fieldCount;
    }
    private int appendKeyList(StringBuilder s, String suffix, String separator) {
        int fieldCount = 0;
        for (DBKey col : rowId) {
            s.append(col.columnName).append(suffix).append(separator);
            fieldCount++;
        }
        if (fieldCount > 0)
            s.setLength(s.length()-separator.length()); // Strip last separator
        
        return fieldCount;
    }
    private String getSELECTQuery() {
        StringBuilder s = new StringBuilder("SELECT ");
        
        if (appendFieldList(s, "", ", ") == 0) {
            return null; // No fields selected
        }
        for (DBKey col : rowId) {
            if (!col.isDataColumn) {
                s.append(", ").append(col.columnName);
            }
        }
        
        s.append(" FROM ").append(settings.table);


        return s.toString();
    }
    private String getDELETEQuery() {
        StringBuilder s = new StringBuilder("DELETE FROM ");
        s.append(settings.table);
        s.append(" WHERE ");
        if (appendKeyList(s, " = ?", " AND ") == 0)
            return null;

        return s.toString();
    }
    
    private String getINSERTQuery() {
        StringBuilder s = new StringBuilder("INSERT INTO ");
        s.append(settings.table);
        s.append(" (");
        int fieldCount = appendFieldList(s, "", ", ");
        if (fieldCount == 0)
            return null;
        s.append(") VALUES (");
        for (int i = 0; i < fieldCount; i++) {
            s.append("?, ");
        }
        s.setLength(s.length() - 2); // Strip last ", "
        s.append(')');
        
        return s.toString();
    }
    private String getUPDATEQuery() {
        StringBuilder s = new StringBuilder("UPDATE ");
        s.append(settings.table);
        s.append(" SET ");
        if (appendFieldList(s, " = ?", ", ") == 0)
            return null;
        s.append(" WHERE ");
        appendKeyList(s, " = ?", " AND ");

        return s.toString();
    }
    
    @Override
    protected void openInternal(String descriptorWithoutPrefix) {
        settings = new ConnectionSettings(descriptorWithoutPrefix);

        try {
            Class.forName(settings.driver);
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, utils._("Could not load the specified driver class:"), e);
            return;
        }
        try {
            String password;
            if (settings.askForPWD) {
                password = PasswordDialog.showPasswordDialog(parentDialog, utils._("Database password"), MessageFormat.format(utils._("Please enter the database password for user {0} (database: {1})."), settings.user, settings.dbURL));
                if (password == null)
                    return;
            } else {
                password = settings.pwd;
            }
            connection = DriverManager.getConnection(settings.dbURL, settings.user, password);
            
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet rs = dbmd.getBestRowIdentifier(null, null, settings.table, DatabaseMetaData.bestRowSession, true);
            rowId.clear();
            while (rs.next()) {
                DBKey key = new DBKey(rs.getString(("COLUMN_NAME")));
                key.isDataColumn = isDataField(key.columnName);
                rowId.add(key);
            }
            rs.close();
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, utils._("Could not connect to the database:"), e);
            connection = null;
            return;
        }
        try {
            String query = getSELECTQuery();
            if (utils.debugMode) {
                System.out.println("JDBC phone book: SELECT query: " + query);
            }
            if (query == null) {
                JOptionPane.showMessageDialog(parentDialog, utils._("Cannot open phonebook since no database fields were selected!"));
                return;
            }
            /*selectStmt = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = selectStmt.executeQuery();*/
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);
            
            items.clear();
            while (resultSet.next()) {
                JDBCPhoneBookEntry jPBE = new JDBCPhoneBookEntry(this);
                jPBE.readFromCurrentDataset(resultSet);
                items.add(jPBE);
            }
            resultSet.close();
            stmt.close();
            
            resort();
            
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, utils._("Could not load the phone book:"), e);
        }
    }

    void commitToDB() {
        PreparedStatement insertStmt, updateStmt, deleteStmt;
        try {   
            String query = getINSERTQuery();
            if (utils.debugMode) {
                System.out.println("JDBC phone book: INSERT query: " + query);
            }
            insertStmt = connection.prepareStatement(query);

            query = getUPDATEQuery();
            if (utils.debugMode) {
                System.out.println("JDBC phone book: UPDATE query: " + query);
            }
            updateStmt = connection.prepareStatement(query);

            query = getDELETEQuery();
            if (utils.debugMode) {
                System.out.println("JDBC phone book: DELETE query: " + query);
            }
            deleteStmt = connection.prepareStatement(query);

            connection.setAutoCommit(false);

            for (JDBCPhoneBookEntry e : deleted_items) {
                try {
                    e.commitToDB(insertStmt, updateStmt, deleteStmt);
                } catch (SQLException ex) {
                    ExceptionDialog.showExceptionDialog(parentDialog, MessageFormat.format(utils._("Could not save the changes for entry {0}:"), e.toString()), ex);
                } 
            }
            for (JDBCPhoneBookEntry e : items) {
                try {
                    e.commitToDB(insertStmt, updateStmt, deleteStmt);
                } catch (SQLException ex) {
                    ExceptionDialog.showExceptionDialog(parentDialog, MessageFormat.format(utils._("Could not save the changes for entry {0}:"), e.toString()), ex);
                }                
            }

            connection.commit();

            insertStmt.close();
            insertStmt = null;
            updateStmt.close();
            updateStmt = null;
            deleteStmt.close();
            deleteStmt = null;
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, utils._("Could not save the phone book:"), e);
        }
    }
    
    private int getInsertionPos(PhoneBookEntry pbe) {
        int res = Collections.binarySearch(items, pbe, DefaultPhoneBookEntryComparator.globalInstance);
        if (res >= 0) // Element found?
            return res + 1;
        else
            return -(res + 1);
    }
    
    void updatePosition(JDBCPhoneBookEntry entry) {
        int oldpos = items.indexOf(entry);
        items.remove(oldpos);
        int pos = getInsertionPos(entry);
        items.add(pos, entry);
        fireContentsChanged(this, oldpos, pos);
    }
    
    void removeFromList(JDBCPhoneBookEntry entry) {
        int pos = items.indexOf(entry);
        items.remove(pos);
        
        fireIntervalRemoved(this, pos, pos);
    }
    
    @Override
    public void resort() {
        Collections.sort(items, DefaultPhoneBookEntryComparator.globalInstance);
    }

    public Object getElementAt(int index) {
        return items.get(index);
    }

    public int getSize() {
        return items.size();
    }

    public boolean isFieldNameAvailable() {
        return (!ConnectionSettings.isNoField(settings.name));
    }
    public boolean isFieldGivenNameAvailable() {
        return (!ConnectionSettings.isNoField(settings.givenName));
    }
    public boolean isFieldTitleAvailable() {
        return (!ConnectionSettings.isNoField(settings.title));
    }
    public boolean isFieldCompanyAvailable() {
        return (!ConnectionSettings.isNoField(settings.company));
    }
    public boolean isFieldLocationAvailable() {
        return (!ConnectionSettings.isNoField(settings.location));
    }
    public boolean isFieldVoiceNumberAvailable() {
        return (!ConnectionSettings.isNoField(settings.voiceNumber));
    }
    public boolean isFieldFaxNumberAvailable() {
        return (!ConnectionSettings.isNoField(settings.faxNumber));
    }
    public boolean isFieldCommentAvailable() {
        return (!ConnectionSettings.isNoField(settings.comment));
    }
    
    static class DBKey {
        public String columnName;
        public boolean isDataColumn;
        
        public DBKey(String columnName) {
            this.columnName = columnName;
        }
    }
}