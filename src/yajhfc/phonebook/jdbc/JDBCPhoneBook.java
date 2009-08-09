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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import yajhfc.Utils;
import yajhfc.phonebook.AbstractConnectionSettings;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.GeneralConnectionSettings;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookException;
import yajhfc.phonebook.WrapperDistributionList;
import yajhfc.phonebook.GeneralConnectionSettings.PBEntrySettingsField;
import yajhfc.plugin.PluginManager;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.PasswordDialog;

public class JDBCPhoneBook extends PhoneBook {
    private static final Logger log = Logger.getLogger(JDBCPhoneBook.class.getName());
    
    ConnectionSettings settings;
    boolean open = false;
    /**
     * The actual items. <br>
     * <b><em>Note:</em></b> Be careful to keep this List and the corresponding
     * itemsView List synchronized (i.e. corresponding items are at the same position)
     */
    List<JDBCPhoneBookEntry> items = new ArrayList<JDBCPhoneBookEntry>();
    List<JDBCPhoneBookEntry> deleted_items = new ArrayList<JDBCPhoneBookEntry>();
    /**
     * A "view" containing either a {@link JDBCPhoneBookEntry} or a {@link WrapperDistributionList} <br>
     * <b><em>Note:</em></b> Be careful to keep this List and the corresponding
     * items List synchronized (i.e. corresponding items are at the same position).
     */
    List<PhoneBookEntry> itemsView = new ArrayList<PhoneBookEntry>();
    
    List<DBKey> rowId = new ArrayList<DBKey>();
    int[] maxLength = new int[PBEntryField.FIELD_COUNT];
    
    protected static final Map<String,ConnectionDialog.FieldMapEntry> fieldNameMap = new HashMap<String,ConnectionDialog.FieldMapEntry>(); 
    static {
        PBEntrySettingsField[] fields = GeneralConnectionSettings.entryFields;
        for (int i = 0; i < fields.length; i++) {
            PBEntrySettingsField field = fields[i];
            fieldNameMap.put(field.getName(), new ConnectionDialog.FieldMapEntry(field.getField().getDescription()+":", i));
        }
//        fieldNameMap.put("givenName", new ConnectionDialog.FieldMapEntry(Utils._("Given name:"),0));
//        fieldNameMap.put("name", new ConnectionDialog.FieldMapEntry(Utils._("Name:"),1));
//        fieldNameMap.put("title", new ConnectionDialog.FieldMapEntry(Utils. _("Title:"),2));
//        fieldNameMap.put("company", new ConnectionDialog.FieldMapEntry(Utils._("Company:"),3));
//        fieldNameMap.put("location", new ConnectionDialog.FieldMapEntry(Utils._("Location:"),4));
//        fieldNameMap.put("faxNumber", new ConnectionDialog.FieldMapEntry(Utils._("Fax number:"),5));
//        fieldNameMap.put("voiceNumber", new ConnectionDialog.FieldMapEntry(Utils._("Voice number:"),6));
//        fieldNameMap.put("comment", new ConnectionDialog.FieldMapEntry(Utils._("Comments:"),7));
        
        fieldNameMap.put("readOnly", new ConnectionDialog.FieldMapEntry(Utils._("Open as read only"),0,false,Boolean.class));
        fieldNameMap.put("displayCaption", new ConnectionDialog.FieldMapEntry(Utils._("Phone book name to display:"),2,false,String.class));
        fieldNameMap.put("allowDistLists", new ConnectionDialog.FieldMapEntry(Utils._("Allow distribution list entries"),1,false,Boolean.class));
    }
    
    public static final String PB_Prefix = "JDBC";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = Utils._("JDBC phone book"); // A user-readable name for this Phonebook type
    public static final String PB_Description = Utils._("A phone book saving its entries in a relational database using JDBC."); // A user-readable description of this Phonebook type
    
    public JDBCPhoneBook(Dialog parent) {
        super(parent);
        
    }

    @Override
    public PhoneBookEntry addNewEntry() {
        JDBCPhoneBookEntry pb = new JDBCPhoneBookEntry(this);
        int pos = getInsertionPos(pb, itemsView);
        items.add(pos, pb);
        itemsView.add(pos, pb);
        fireEntriesAdded(pos, pb);
        return pb;
    }

    @Override
    public DistributionList addDistributionList() {
        if (!settings.allowDistLists) 
            throw new UnsupportedOperationException("No distribution lists allowed.");
        
        JDBCPhoneBookEntry pb = new JDBCPhoneBookEntry(this);
        WrapperDistributionList dl = new WrapperDistributionList(pb);
        
        int pos = getInsertionPos(dl, itemsView);
        items.add(pos, pb);
        itemsView.add(pos, dl);
        fireEntriesAdded(pos, dl);
        return dl;
    }
    
    @Override
    public String browseForPhoneBook(boolean exportMode) {
        ConnectionSettings cs = new ConnectionSettings(settings);
        ConnectionDialog cDlg = new ConnectionDialog(parentDialog, Utils._("New JDBC phone book"),
                Utils._("Please select which database fields correspond to the phone book entry fields of YajHFC:"),
                fieldNameMap, true);
        if (cDlg.promptForNewSettings(cs))
            return PB_Prefix + ":" + cs.saveToString();
        else
            return null;
    }

    @Override
    public void close() {        
        if (open) {
            commitToDB();
            open = false;
        }
    }

    /**
     * Returns the PBEntryField corresponding to the given column name or null if
     * no such field exists.
     * @param fieldName
     * @return
     */
    private PBEntryField getPBEntryFieldFor(String fieldName) {
        for (PBEntryField field : PBEntryField.values()) {
            if (fieldName.equalsIgnoreCase(settings.getMappingFor(field))) {
                return field;
            }
        }
        return null;
    }
    
    private int appendFieldList(StringBuilder s, String suffix, String separator) {
        int fieldCount = 0;
        for (PBEntryField field : PBEntryField.values()) {
            String fieldName = settings.getMappingFor(field);
            if (!ConnectionSettings.isNoField(fieldName)) {
                s.append(fieldName).append(suffix).append(separator);
                fieldCount++;
            }
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
            if (!col.isDataColumn()) {
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
    
    /**
     * Opens the database connection. The caller is responsible for properly closing it again.
     * @return A new connection or null if the user canceled the connecting process.
     * @throws PhoneBookException 
     */
    protected Connection openConnection() throws PhoneBookException {
        try {
            PluginManager.registerJDBCDriver(settings.driver);
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Could not load the specified driver class:"), e);
            throw new PhoneBookException(e, true);
        }
        String password;
        if (settings.askForPWD) {
            String[] pwd = PasswordDialog.showPasswordDialog(parentDialog, Utils._("Database password"), MessageFormat.format(Utils._("Please enter the database password (database: {0}):"), settings.dbURL), settings.user, false);
            if (pwd == null)
                return null;
            else
                password = pwd[1];
        } else {
            password = settings.pwd.getPassword();
        }
        
        try {
            return DriverManager.getConnection(settings.dbURL, settings.user, password);
        } catch (SQLException e) {
            ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Could not connect to the database:"), e);
            throw new PhoneBookException(e, true);
        }
    }
    
    @Override
    protected void openInternal(String descriptorWithoutPrefix) throws PhoneBookException {
        settings = new ConnectionSettings(descriptorWithoutPrefix);

        Connection connection = openConnection();
        if (connection == null)
            return;
        
        try {            
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet rs = dbmd.getBestRowIdentifier(null, null, settings.table, DatabaseMetaData.bestRowSession, true);
            rowId.clear();
            while (rs.next()) {
                // Workaround for gettext
                final String COLUMN_NAME = "COLUMN_NAME";
                String columnName = rs.getString(COLUMN_NAME);
                rowId.add(new DBKey(columnName, getPBEntryFieldFor(columnName)));
            }
            rs.close();
            
            if (rowId.size() == 0) {
                log.info("No key found, using all data fields as replacement");
                
                for (PBEntryField field : PBEntryField.values()) {
                    String mapping = settings.getMappingFor(field);
                    if (!AbstractConnectionSettings.isNoField(mapping)) {
                        rowId.add(new DBKey(mapping, field));
                    }
                }
            }
            
            if (Utils.debugMode) {
                log.fine("Table key is: " + rowId);
            }
            
            loadItems(connection);
            
            open = true;
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Could not load the phone book:"), e);
            throw new PhoneBookException(e, true);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, "Error closing the database connection:", e);
            }
        }
    }
    
    protected void loadItems(Connection connection) throws SQLException {
        String query = getSELECTQuery();
        if (Utils.debugMode) {
            log.fine("JDBC phone book: SELECT query: " + query);
        }
        if (query == null) {
            JOptionPane.showMessageDialog(parentDialog, Utils._("Cannot open phone book since no database fields were selected!"));
            return;
        }
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(query);
        
        final ResultSetMetaData rsmd = resultSet.getMetaData();
        int colPtr = 1;
        // This relies on the fact that appendFieldList() appends the fields
        // in order with PBEntryFields.values() and that these fields come first
        // in the SELECT query
        for (PBEntryField field : PBEntryField.values()) {
            if (!ConnectionSettings.isNoField(settings.getMappingFor(field))) {
                maxLength[field.ordinal()] = rsmd.getColumnDisplaySize(colPtr++);
            } else {
                maxLength[field.ordinal()] = 0;
            }
        }
        
        deleted_items.clear();
        items.clear();
        itemsView.clear();
        while (resultSet.next()) {
            JDBCPhoneBookEntry jPBE = new JDBCPhoneBookEntry(this);
            jPBE.readFromCurrentDataset(resultSet);
            items.add(jPBE);
            if (WrapperDistributionList.isDistributionList(jPBE)) {
                itemsView.add(new WrapperDistributionList(jPBE));
            } else {
                itemsView.add(jPBE);
            }
        }
        resultSet.close();
        stmt.close();
        
        resort();
    }
        
    protected void commitToDB() {
        if (isReadOnly())
            return;

        Connection connection = null;
        try {
            connection = openConnection();
            if (connection == null) {
                return;
            }

            commitToDB(connection);

        } catch (PhoneBookException pbe) {
            if (!pbe.messageAlreadyDisplayed()) {
                ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Could not save the phone book:"), pbe);
            }
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(parentDialog, Utils._("Could not save the phone book:"), e);
        } finally {
            try {
                if (connection != null) {
                    connection.close(); 
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, "Error closing the database connection:", e);
            }
        }
    }

    protected void commitToDB(Connection connection) throws SQLException {
        PreparedStatement insertStmt, updateStmt, deleteStmt;
        if (isReadOnly())
            return;
        
        String query = getINSERTQuery();
        if (Utils.debugMode) {
            log.fine("JDBC phone book: INSERT query: " + query);
        }
        if (query == null) {
            JOptionPane.showMessageDialog(parentDialog, MessageFormat.format(Utils._("Could not save the changes: No valid {0} query."), "INSERT"), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        insertStmt = connection.prepareStatement(query);

        query = getUPDATEQuery();
        if (Utils.debugMode) {
            log.fine("JDBC phone book: UPDATE query: " + query);
        }
        if (query == null) {
            JOptionPane.showMessageDialog(parentDialog, MessageFormat.format(Utils._("Could not save the changes: No valid {0} query."), "UPDATE"), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        updateStmt = connection.prepareStatement(query);

        query = getDELETEQuery();
        if (Utils.debugMode) {
            log.fine("JDBC phone book: DELETE query: " + query);
        }
        if (query == null) {
            JOptionPane.showMessageDialog(parentDialog, MessageFormat.format(Utils._("Could not save the changes: No valid {0} query."), "DELETE"), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        deleteStmt = connection.prepareStatement(query);

        connection.setAutoCommit(false);

        for (JDBCPhoneBookEntry e : deleted_items) {
            try {
                e.commitToDB(insertStmt, updateStmt, deleteStmt);
            } catch (SQLException ex) {
                ExceptionDialog.showExceptionDialog(parentDialog, MessageFormat.format(Utils._("Could not save the changes for entry {0}:"), e.toString()), ex);
            } 
        }
        for (JDBCPhoneBookEntry e : items) {
            try {
                e.commitToDB(insertStmt, updateStmt, deleteStmt);
            } catch (SQLException ex) {
                ExceptionDialog.showExceptionDialog(parentDialog, MessageFormat.format(Utils._("Could not save the changes for entry {0}:"), e.toString()), ex);
            }                
        }

        connection.commit();

        insertStmt.close();
        updateStmt.close();
        deleteStmt.close();
    }
    
    private static int getInsertionPos(PhoneBookEntry pbe, List<PhoneBookEntry> targetList) {
        int res = Collections.binarySearch(targetList, pbe);
        if (res >= 0) // Element found?
            return res + 1;
        else
            return -(res + 1);
    }
    
    void updatePosition(JDBCPhoneBookEntry entry) {
        int oldpos = Utils.identityIndexOf(items, entry);
        items.remove(oldpos);
        PhoneBookEntry oldView = itemsView.remove(oldpos);
        
        int pos = getInsertionPos(oldView, itemsView);
        
        items.add(pos, entry);
        itemsView.add(pos, oldView);
        
        fireEntriesChanged(eventObjectForInterval(oldpos, pos));
    }
    
    void removeFromList(JDBCPhoneBookEntry entry) {
        int pos = Utils.identityIndexOf(items, entry);
        if (pos >= 0) {
            items.remove(pos);
            itemsView.remove(pos);
        
            fireEntriesRemoved(pos, entry);
        }
    }
    
    @Override
    public void resort() {
        Collections.sort(itemsView);
        for (int i = 0; i < itemsView.size(); i++) {
            items.set(i, toJDBCEntry(itemsView.get(i)));
        }
    }

    @Override
    public List<PhoneBookEntry> getEntries() {
        return itemsView;
    }

    @Override
    public String getDisplayCaption() {
        if (settings.displayCaption != null && settings.displayCaption.length() > 0) {
            return settings.displayCaption;
        } else {
            String rv = settings.dbURL;

            if (rv.length() > CAPTION_LENGTH)
                return rv.substring(0, CAPTION_LENGTH-3) + "...";
            else
                return rv;
        }
    }
    
    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isReadOnly() {
        return settings.readOnly;
    }
    
    @Override
    public boolean supportsDistributionLists() {
        return settings.allowDistLists && WrapperDistributionList.areDistributionListsSupported(this);
    }
    
    @Override
    public boolean isFieldAvailable(PBEntryField field) {
        return (!ConnectionSettings.isNoField(settings.getMappingFor(field)));
    }
    
    @Override
    public int getMaxLength(PBEntryField field) {
        return maxLength[field.ordinal()];
    }
    
    /**
     * Returns the corresponding {@link JDBCPhoneBookEntry} for the specified entry
     * (which may be either a JDBCPhoneBookEntry or a WrapperDistributionList wrapping
     *  a JDBCPhoneBookEntry)
     * @param entry
     * @return
     */
    private static JDBCPhoneBookEntry toJDBCEntry(PhoneBookEntry entry) {
        if (entry instanceof WrapperDistributionList) {
            return (JDBCPhoneBookEntry)((WrapperDistributionList)entry).getWrappedEntry();
        } else {
            return (JDBCPhoneBookEntry)entry;
        }
    }
    
    protected static class DBKey {
        public final String columnName;
        /**
         * The data field this key corresponds to or null if no such field exists.
         */
        public final PBEntryField dataField;
        
        public DBKey(String columnName, PBEntryField dataField) {
            this.columnName = columnName;
            this.dataField = dataField;
        }
        
        @Override
        public String toString() {
            return "DBKey[columnName=" + columnName + ";dataField=" + ((dataField == null) ? "<null>" : dataField.name()) + "]";
        }

        /**
         * @return the isDataColumn
         */
        public boolean isDataColumn() {
            return (dataField != null);
        }
    }
}
