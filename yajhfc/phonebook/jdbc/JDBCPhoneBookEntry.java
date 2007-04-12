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

import yajhfc.phonebook.SimplePhoneBookEntry;
import yajhfc.utils;

public class JDBCPhoneBookEntry extends SimplePhoneBookEntry {
    private JDBCPhoneBook parent;
    
    private Object[] rowKeys;
    
    int entryStatus;
    static final int ENTRY_NOTINSERTED = 1;
    static final int ENTRY_UNCHANGED = 2;
    static final int ENTRY_CHANGED = 3;
    static final int ENTRY_DELETED = 4;
    
    private boolean dirty = false;
    
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
            return utils._("<error>");
        }
    }
    void readFromCurrentDataset(ResultSet rs) throws SQLException {        
        ConnectionSettings cs = parent.settings;
        surname = fetchValue(cs.name, rs);
        givenname = fetchValue(cs.givenName, rs);
        company = fetchValue(cs.company, rs);
        title = fetchValue(cs.title, rs);
        location = fetchValue(cs.location, rs);
        comment = fetchValue(cs.comment, rs);
        voicenumber = fetchValue(cs.voiceNumber, rs);
        faxnumber = fetchValue(cs.faxNumber, rs);
        
        readKeys(rs);
        entryStatus = ENTRY_UNCHANGED;
        dirty = false;
    }
    private void readKeys(ResultSet rs) throws SQLException {
        for (int i = 0; i < parent.rowId.size(); i++) {
            rowKeys[i] = rs.getObject(parent.rowId.get(i).columnName);
        }
    }
    /*
    private void updateResultSet(ResultSet rs) throws SQLException {
        ConnectionSettings settings = parent.settings;

        if (!ConnectionSettings.isNoField(settings.name)) {
            rs.updateString(settings.name, surname);
        }
        if (!ConnectionSettings.isNoField(settings.givenName)) {
            rs.updateString(settings.givenName, givenname);
        }
        if (!ConnectionSettings.isNoField(settings.company)) {
            rs.updateString(settings.company, company);
        }
        if (!ConnectionSettings.isNoField(settings.location)) {
            rs.updateString(settings.location, location);
        }
        if (!ConnectionSettings.isNoField(settings.title)) {
            rs.updateString(settings.title, title);
        }
        if (!ConnectionSettings.isNoField(settings.faxNumber)) {
            rs.updateString(settings.faxNumber, faxnumber);
        }
        if (!ConnectionSettings.isNoField(settings.voiceNumber)) {
            rs.updateString(settings.voiceNumber, voicenumber);
        }
        if (!ConnectionSettings.isNoField(settings.comment)) {
            rs.updateString(settings.comment, comment);
        }
    }*/
    
    private int setChangedValues(PreparedStatement stmt, int offset) throws SQLException{
        ConnectionSettings settings = parent.settings;

        if (!ConnectionSettings.isNoField(settings.name)) {
            stmt.setString(offset, this.surname);
            offset++;
        }
        if (!ConnectionSettings.isNoField(settings.givenName)) {
            stmt.setString(offset, this.givenname);
            offset++;
        }
        if (!ConnectionSettings.isNoField(settings.company)) {
            stmt.setString(offset, this.company);
            offset++;
        }
        if (!ConnectionSettings.isNoField( settings.location)) {
            stmt.setString(offset, this.location);
            offset++;
        }
        if (!ConnectionSettings.isNoField(settings.title)) {
            stmt.setString(offset, this.title);
            offset++;
        }
        if (!ConnectionSettings.isNoField(settings.faxNumber)) {
            stmt.setString(offset, this.faxnumber);
            offset++;
        }
        if (!ConnectionSettings.isNoField(settings.voiceNumber)) {
            stmt.setString(offset, this.voicenumber);
            offset++;
        }
        if (!ConnectionSettings.isNoField(settings.comment)) {
            stmt.setString(offset, this.comment);
            offset++;
        }
        return offset;
    }
    /*
    private String getValueForDBField(String fieldName) {
        ConnectionSettings settings = parent.settings;
        
        if (fieldName.equalsIgnoreCase(settings.comment))
            return comment;
        if (fieldName.equalsIgnoreCase(settings.company))
            return company;
        if (fieldName.equalsIgnoreCase(settings.faxNumber))
            return faxnumber;
        if (fieldName.equalsIgnoreCase(settings.givenName))
            return givenname;
        if (fieldName.equalsIgnoreCase(settings.location))
            return location;
        if (fieldName.equalsIgnoreCase(settings.name))
            return surname;
        if (fieldName.equalsIgnoreCase(settings.title))
            return title;
        if (fieldName.equalsIgnoreCase(settings.voiceNumber))
            return voicenumber;

        return null;
    }*/
    
    private int setOriginalValues(PreparedStatement stmt, int offset) throws SQLException{

        for (int i = 0; i < parent.rowId.size(); i++) {
            stmt.setObject(offset++, rowKeys[i]);
        }
        return offset;
    }
    
    void commitToDB(PreparedStatement insertStmt, PreparedStatement updateStmt, PreparedStatement deleteStmt) throws SQLException {
        /*
            if (inserted) {
                int off = setChangedValues(parent.updateStmt, 1);
                setOriginalValues(parent.updateStmt, off);
                parent.updateStmt.execute();

                if (parent.updateStmt.getUpdateCount() == 0) {
                    //TODO: Warn user
                    return;
                }

                for (int i = 0; i < parent.rowId.size(); i++) {
                    if (parent.rowId.get(i).isDataColumn)
                        rowKeys[i] = getValueForDBField(parent.rowId.get(i).columnName);
                }
            } else {
                /*setChangedValues(parent.insertStmt, 1);
                parent.insertStmt.execute();*//*
                ResultSet rs = parent.selectStmt.executeQuery();
                rs.moveToInsertRow();
                updateResultSet(rs);
                rs.insertRow();

                rs.refreshRow();
                readKeys(rs);
                rs.close();

                inserted = true;
            }*/
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
        if (dirty) {
            parent.updatePosition(this);
        }
    }
    
    @Override
    public void commit() {
        if (dirty) {
            parent.updatePosition(this);

            if (entryStatus == ENTRY_UNCHANGED)
                entryStatus = ENTRY_CHANGED;
            dirty = false;
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
    public void setComment(String newComment) {
        if (!newComment.equals(comment)) {
            dirty = true;
            super.setComment(newComment);
        }     
    }

    @Override
    public void setCompany(String newCompany) {
        if (!newCompany.equals(company)) {
            dirty = true;
            super.setCompany(newCompany);
        }
    }

    @Override
    public void setFaxNumber(String newFaxNumber) {
        if (!newFaxNumber.equals(faxnumber)) {
            dirty = true;
            super.setFaxNumber(newFaxNumber);
        }
    }

    @Override
    public void setGivenName(String newGivenName) {
        if (!newGivenName.equals(givenname)) {
            dirty = true;
            super.setGivenName(newGivenName);
        }
    }

    @Override
    public void setLocation(String newLocation) {
        if (!newLocation.equals(location)) {
            dirty = true;
            super.setLocation(newLocation);
        }
    }

    @Override
    public void setName(String newName) {
        if (!newName.equals(surname)) {
            dirty = true;
            super.setName(newName);
        }
    }

    @Override
    public void setTitle(String newTitle) {
        if (!newTitle.equals(title)) {
            dirty = true;
            super.setTitle(newTitle);
        }
    }

    @Override
    public void setVoiceNumber(String newVoiceNumber) {
        if (!newVoiceNumber.equals(voicenumber)) {
            dirty = true;
            super.setVoiceNumber(newVoiceNumber);
        }
    } 

}
