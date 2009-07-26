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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.SimplePhoneBookEntry;
import yajhfc.phonebook.jdbc.JDBCPhoneBook.DBKey;

public class JDBCPhoneBookEntry extends SimplePhoneBookEntry {
    private static final Logger log = Logger.getLogger(JDBCPhoneBookEntry.class.getName());
    
    private JDBCPhoneBook parent;
    
    private Object[] rowKeys;
    
    int entryStatus;
    static final int ENTRY_NOTINSERTED = 1;
    static final int ENTRY_UNCHANGED = 2;
    static final int ENTRY_CHANGED = 3;
    static final int ENTRY_DELETED = 4;
    
    
    JDBCPhoneBookEntry(JDBCPhoneBook parent) {
        this.parent = parent;
        rowKeys = new Object[parent.rowId.size()];
        
        entryStatus = ENTRY_NOTINSERTED;
    }

    private String fetchValue(String fieldName, ResultSet rs) {
        if (ConnectionSettings.isNoField(fieldName)) {
            return "";
        }
        try {
            return rs.getString(fieldName);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error loading value " + fieldName + " from " + rs, e);
            return Utils._("<error>");
        }
    }
    void readFromCurrentDataset(ResultSet rs) throws SQLException {        
        ConnectionSettings cs = parent.settings;
        for (PBEntryField field : PBEntryField.values()) {
            setFieldUndirty(field, fetchValue(cs.getMappingFor(field), rs));
        }
        
        readKeys(rs);
        entryStatus = ENTRY_UNCHANGED;
        setDirty(false);
    }
    
    private void readKeys(ResultSet rs) throws SQLException {
        for (int i = 0; i < parent.rowId.size(); i++) {
            DBKey key = parent.rowId.get(i);
            if (key.isDataColumn()) {
                rowKeys[i] = getField(key.dataField);
            } else {
                rowKeys[i] = rs.getObject(key.columnName);
            }
        }
    }

    /**
     * Sets the changed value in the prepared statement. The order of fields must
     * be the one specified by PBEntryField.values().
     * @param stmt
     * @param offset
     * @return
     * @throws SQLException
     */
    private int setChangedValues(PreparedStatement stmt, int offset) throws SQLException{
        ConnectionSettings settings = parent.settings;

        for (PBEntryField field : PBEntryField.values()) {
            if (!ConnectionSettings.isNoField(settings.getMappingFor(field))) {
                stmt.setString(offset, getField(field));
                offset++;
            }
        }
        return offset;
    }
    
    private int setOriginalValues(PreparedStatement stmt, int offset) throws SQLException{

        for (int i = 0; i < parent.rowId.size(); i++) {
            stmt.setObject(offset++, rowKeys[i]);
        }
        return offset;
    }
    
    void commitToDB(PreparedStatement insertStmt, PreparedStatement updateStmt, PreparedStatement deleteStmt) throws SQLException {
        switch (entryStatus) {
        case ENTRY_UNCHANGED:
            break;
        case ENTRY_CHANGED:
            int off = setChangedValues(updateStmt, 1);
            setOriginalValues(updateStmt, off);
            updateStmt.execute();
            break;
        case ENTRY_NOTINSERTED:
            setChangedValues(insertStmt, 1);
            insertStmt.execute();
            break;
        case ENTRY_DELETED:
            setOriginalValues(deleteStmt, 1);
            deleteStmt.execute();
            break;
        }
    }
    
    @Override
    public void updateDisplay() {
        if (isDirty()) {
            parent.updatePosition(this);
        }
    }
    
    @Override
    public void commit() {
        if (isDirty()) {
            parent.updatePosition(this);

            if (entryStatus == ENTRY_UNCHANGED)
                entryStatus = ENTRY_CHANGED;
            setDirty(false);
        }
    }

    @Override
    public void delete() {
        if (entryStatus != ENTRY_NOTINSERTED) {
                entryStatus = ENTRY_DELETED;
                parent.deleted_items.add(this);
        }
            
        parent.removeFromList(this);
    }

    @Override
    public PhoneBook getParent() {
        return parent;
    }

}
