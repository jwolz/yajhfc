/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2015 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.virtualcolumnstore;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import yajhfc.Password;
import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.model.VirtualColumnType;
import yajhfc.phonebook.AbstractConnectionSettings;
import yajhfc.phonebook.jdbc.ConnectionDialog;
import yajhfc.phonebook.jdbc.ConnectionDialog.FieldMapEntry;
import yajhfc.plugin.PluginManager;
import yajhfc.util.DoNotAskAgainDialog;

/**
 * @author jonas
 *
 */
public class JDBCVirtColPersister extends CachingVirtColPersister {
    static final Logger log = Logger.getLogger(JDBCVirtColPersister.class.getName());

    protected ConnectionSettings settings;
    
    protected Connection connection;
    protected PreparedStatement selectStmt, updateStmt, insertStmt, lmtsStmt;
    protected int updateStmtKeyIdx, insertStmtKeyIdx;
    protected int[] updateStmtVTCIdx, insertStmtVTCIdx;
    protected ColumnMetaData[] columnMetaData;
    
    protected long lastLastModified;
    
    protected ScheduledFuture<?> updateTask;
    
    protected List<VirtColChangeListener> listeners = new ArrayList<VirtColChangeListener>();
    
    //private static final String SELECT_TEMPLATE = "SELECT {1}, {2} FROM {0};";
    //private static final String UPDATE_TEMPLATE = "UPDATE {0} SET {2} = ? WHERE {1} = ?;";
    //private static final String INSERT_TEMPLATE = "INSERT INTO {0} ({1},{2}) VALUES (?,?);";
    
    protected static final Map<String,FieldMapEntry> fieldCaptionMap = new HashMap<String,FieldMapEntry>();
    static {
        fieldCaptionMap.put("faxNameField", new FieldMapEntry(Utils._("Key (fax filename):"),0));
        fieldCaptionMap.put("isReadField", new FieldMapEntry(Utils._("Read/Unread state:"),1));
        fieldCaptionMap.put("commentField", new FieldMapEntry(Utils._("Comment:"),2));
        fieldCaptionMap.put("lastModifiedField", new FieldMapEntry(Utils._("Last modified:"),3));
    }

    @Override
    public void shutdown() {
        disconnect();
    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#persistValues()
     */
    @Override
    public void persistValues() {
        // NOP
    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#addVirtColChangeListener(yajhfc.virtualcolumnstore.VirtColChangeListener)
     */
    @Override
    public synchronized void addVirtColChangeListener(VirtColChangeListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#removeVirtColChangeListener(yajhfc.virtualcolumnstore.VirtColChangeListener)
     */
    @Override
    public synchronized void removeVirtColChangeListener(VirtColChangeListener listener) {
        listeners.remove(listener);
    }

    protected synchronized void fireColumnsChanged(Set<String> inserts, Set<String> updates, Set<String> deletes) {
        for (VirtColChangeListener l : listeners) {
            l.columnsChanged(inserts, updates, deletes);
        }
    }
    
    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#cleanupState(java.util.Collection)
     */
    @Override
    public synchronized void cleanupState(Collection<String> existingFaxes) {
        if (existingFaxes.size() == 0 || connection == null) {
            log.info("cleanupState called with empty list or closed connection");
            return; //"Safety" measure
        }
        
        try {
            String sql = "DELETE FROM " + settings.table + 
                    "WHERE " + settings.getKeyFieldName() + " = ?";
            if (Utils.debugMode) {
                log.fine("DELETE statement: " + sql);
            }
            PreparedStatement deleteStmt = connection.prepareStatement(sql);

            for (String key : data.keySet()) {
                if (!existingFaxes.contains(key)) {
                    if (Utils.debugMode) {
                        log.fine("Deleting key " + key);
                    }
                    deleteStmt.setString(1, key);
                    deleteStmt.execute();
                    if (Utils.debugMode) {
                        log.fine("" + deleteStmt.getUpdateCount() + " rows deleted");
                    }
                }
            }
            
            deleteStmt.close();
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error cleaning up", e);
        }
        
    }
    
    @Override
    protected synchronized void checkInitialized() {
        if (data==null) {
            try {
                data = loadValues(true);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error loading data ", e);
                data = new HashMap<String,Object[]>();
            } 
            
            Runnable updater = new Runnable() {
                public void run() {
                    checkForUpdates();
                }  
            };
            updateTask = Utils.executorService.scheduleAtFixedRate(updater, 
                    Utils.getFaxOptions().statusUpdateInterval,
                    Utils.getFaxOptions().statusUpdateInterval,
                    TimeUnit.MILLISECONDS);
        }
    }
    
    protected synchronized void openConnection() throws ClassNotFoundException, SQLException {
        if (Utils.debugMode) {
            log.fine(String.format("Connecting: driver=%s, URL=%s, username=%s, askForPassword=%s", settings.driver, settings.dbURL, settings.user, settings.askForPWD));
        }
        
        PluginManager.registerJDBCDriver(settings.driver);

        String password;
        if (settings.askForPWD) {
            String[] pwd = Launcher2.application.getDialogUI().showPasswordDialog(Utils._("Database password"), MessageFormat.format(Utils._("Please enter the database password (database: {0}):"), settings.dbURL), settings.user, false);
            if (pwd == null)
                return;
            else
                password = pwd[1];
        } else {
            password = settings.pwd.getPassword();
        }
        connection = DriverManager.getConnection(settings.dbURL, settings.user, password);
        connection.setAutoCommit(true);
        
        final VirtualColumnType[] vtcs = VirtualColumnType.values();
        StringBuilder sql = new StringBuilder();
        final boolean haveLastModified = haveLastModified();
        int count;
        
        sql.append("SELECT ");
        sql.append(settings.getKeyFieldName());
        for (VirtualColumnType vtc : vtcs) {
            if (vtc.isSaveable()) {
                String field = settings.getFieldNameForVirtualColumnType(vtc);
                if (!ConnectionSettings.isNoField(field))
                    sql.append(", ").append(field);
            }
        }
        if (haveLastModified) {
            sql.append(", ").append(settings.lastModifiedField);
        }
        sql.append("\nFROM ").append(settings.table);
        if (Utils.debugMode) {
            log.fine("SELECT statement: " + sql);
        }
        selectStmt = connection.prepareStatement(sql.toString());
        
        lastLastModified = -1;
        if (haveLastModified) {
            sql.setLength(0);
            sql.append("SELECT ").append("max(");
            sql.append(settings.lastModifiedField);
            sql.append(')');
            sql.append("\nFROM ").append(settings.table);
            if (Utils.debugMode) {
                log.fine("max last modified SELECT statement: " + sql);
            }
            lmtsStmt = connection.prepareStatement(sql.toString());
        } else {
            lmtsStmt = null;
        }
        
        sql.setLength(0);
        insertStmtVTCIdx = new int[vtcs.length];
        sql.append("INSERT INTO ").append(settings.table);
        sql.append('(').append(settings.getKeyFieldName());
        insertStmtKeyIdx = 1;
        count=2;
        for (int i=0; i<vtcs.length; i++) {
            VirtualColumnType vtc = vtcs[i];
            insertStmtVTCIdx[i] = -1;
            
            if (vtc.isSaveable()) {
                String field = settings.getFieldNameForVirtualColumnType(vtc);
                if (!ConnectionSettings.isNoField(field)) {
                    sql.append(", ").append(field);
                    insertStmtVTCIdx[i] = count++;
                }
            }
        }
        if (haveLastModified) {
            sql.append(", ").append(settings.lastModifiedField);
        }
        sql.append(")\n VALUES (?");
        for (VirtualColumnType vtc : vtcs) {
            if (vtc.isSaveable()) {
                String field = settings.getFieldNameForVirtualColumnType(vtc);
                if (!ConnectionSettings.isNoField(field))
                    sql.append(", ?");
            }
        }
        if (haveLastModified) {
            sql.append(", ").append("current_timestamp");
        }
        sql.append(')');
        if (Utils.debugMode) {
            log.fine("INSERT statement: " + sql);
        }
        insertStmt = connection.prepareStatement(sql.toString());
        
        sql.setLength(0);
        updateStmtVTCIdx = new int[vtcs.length];
        sql.append("UPDATE ").append(settings.table);
        sql.append("\nSET ");
        boolean first=true;
        count = 1;
        for (int i=0; i<vtcs.length; i++) {
            VirtualColumnType vtc = vtcs[i];
            updateStmtVTCIdx[i] = -1;
            if (vtc.isSaveable()) {
                String field = settings.getFieldNameForVirtualColumnType(vtc);
                if (!ConnectionSettings.isNoField(field)) {
                    if (first)
                        first=false;
                    else
                        sql.append(", ");
                    sql.append(field).append(" = ?");
                    updateStmtVTCIdx[i] = count++;
                }
            }
        }
        if (haveLastModified) {
            sql.append(", ").append(settings.lastModifiedField).append(" = current_timestamp");
        }
        sql.append("\nWHERE ").append(settings.getKeyFieldName()).append(" = ?");
        updateStmtKeyIdx = count;
        if (Utils.debugMode) {
            log.fine("UPDATE statement: " + sql);
        }
        updateStmt = connection.prepareStatement(sql.toString());
        
        if (ConnectionSettings.isNoField(settings.getFieldNameForVirtualColumnType(VirtualColumnType.USER_COMMENT))) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DoNotAskAgainDialog.showMessageDialog(JDBCVirtColPersister.class.getName() + ".CommentUnset", Launcher2.application.getFrame(), MessageFormat.format(Utils._("No database field is set to save column \"User comment\". Values will be lost after restart.\nPlease see {0} for more information."), "http://www.yajhfc.de/documentation/knowledge-base/152-database-migration-to-0-6-0"), Utils._("User comment"), JOptionPane.WARNING_MESSAGE);
                }
            });
        }
    }

    protected boolean haveLastModified() {
        return !ConnectionSettings.isNoField(settings.lastModifiedField);
    }
    
    protected synchronized void disconnect() {
        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
        
        if (connection != null) {
            try {
                selectStmt.close();
                if (lmtsStmt != null)
                    lmtsStmt.close();
                insertStmt.close();
                updateStmt.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, "Could not close statement", e);
            }
            try {
                connection.close();
            } catch (SQLException e) {
                log.log(Level.WARNING, "Could not close database connection", e);
            }
            
            selectStmt = null;
            lmtsStmt = null;
            insertStmt = null;
            updateStmt = null;
            connection = null;
            
            //readStateMap = null;
        }
    }

    /**
     * Returns true if there probably was a modification
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    protected boolean checkLastModified() throws SQLException, ClassNotFoundException {
        if (connection == null) {
            openConnection();
        }
        log.fine("Checking last modified timestamp...");
        
        ResultSet rs = lmtsStmt.executeQuery();
        if (rs.next()) {
            Timestamp ts = rs.getTimestamp(1);
            if (Utils.debugMode)
                log.fine("Last modified timestamp: " + ts);
            if (ts == null)
                return true;
            
            long tsMillis = ts.getTime();
            
            synchronized (this) {
                if (Utils.debugMode)
                    log.fine("Last modified: old: " + lastLastModified + "; new: " + tsMillis);
                return (tsMillis != lastLastModified);
            }
        } else {
            // If the query fails, assume modification
            return true;
        }
    }
    

    protected Map<String,Object[]> loadValues(boolean updateMetadata) throws SQLException, ClassNotFoundException {
        if (connection == null) {
            openConnection();
        }
        log.fine("Querying database table...");
        Map<String,Object[]> readMap = new HashMap<String,Object[]>();
        ResultSet rs = selectStmt.executeQuery();

        
        int keyIdx = rs.findColumn(settings.getKeyFieldName());
        int lmtsIdx = -1;
        if (haveLastModified())
            lmtsIdx = rs.findColumn(settings.lastModifiedField);
        
        VirtualColumnType[] vtcs = VirtualColumnType.values();        
        int vtcIdx[] = new int[vtcs.length];
        ResultSetMetaData rsmd = null;
        
        if (updateMetadata) {
            rsmd = rs.getMetaData();
            columnMetaData = new ColumnMetaData[vtcs.length];
        }
        
        for (int i=0; i<vtcs.length; i++) {
            VirtualColumnType vtc = vtcs[i];
            vtcIdx[i] = -1;
            
            if (vtc.isSaveable()) {
                String fieldName = settings.getFieldNameForVirtualColumnType(vtc);
                if (!ConnectionSettings.isNoField(fieldName)) {
                    int col = vtcIdx[i] = rs.findColumn(fieldName);
                    
                    if (updateMetadata)
                        columnMetaData[i] = new ColumnMetaData(rsmd, col);
                }
            }
        }

        if (Utils.debugMode)
            log.fine("keyIdx="+keyIdx+"; vtcIdx="+Arrays.toString(vtcIdx));
        
        long maxLastModified = -1;
        
        while (rs.next()) {
            String key = rs.getString(keyIdx);
            Object[] keyData = allocateKeyData();
            
            for (int i=0; i<vtcs.length; i++) {
                int idx = vtcIdx[i];
                if (idx >= 0) {
                    VirtualColumnType vtc = vtcs[i];
                    Class<?> dataType = vtc.getDataType();
                    Object value;
                    
                    if (dataType == String.class) {
                        value = rs.getString(idx);
                    } else if (dataType == Boolean.class) {
                        value = Boolean.valueOf(rs.getBoolean(idx));
                    } else if (dataType == Integer.class) {
                        value = Integer.valueOf(rs.getInt(idx));
                    } else if (dataType == Long.class) {
                        value = Long.valueOf(rs.getLong(idx));
                    } else {
                        log.warning("Unsupported data type: " + dataType);
                        value = rs.getObject(idx);
                    }
                    if (rs.wasNull())
                        value = null;
                    
                    keyData[columnToIndex(vtc)] = value;
                }
            }
            
            readMap.put(key, keyData);
            
            if (lmtsIdx >= 0) {
                Timestamp lmts = rs.getTimestamp(lmtsIdx);
                if (lmts != null) {
                    long tsMillis = lmts.getTime();
                
                    if (tsMillis > maxLastModified)
                        maxLastModified = tsMillis;
                }
            }
        }
        rs.close();
        
        synchronized (this) {
            // Update last seen modification
            lastLastModified = maxLastModified;
        }
        
        return readMap;
    }
    
    private void setStatementValues(PreparedStatement stmt, int keyIdx, int[] vtcIdx, String key, Object[] keyData) throws SQLException {
        if (Utils.debugMode)
            log.finer("keyIdx=" + keyIdx + "; vtcIdx=" + Arrays.toString(vtcIdx) + "; key=" + key + "; keyData=" + Arrays.toString(keyData));
        stmt.setString(keyIdx, key);
        
        VirtualColumnType[] vtcs = VirtualColumnType.values();
        for (int i=0; i<vtcs.length; i++) {
            int idx = vtcIdx[i];
            if (idx >= 0) {
                VirtualColumnType vtc = vtcs[i];
                Class<?> dataType = vtc.getDataType();
                Object value = keyData[columnToIndex(vtc)];
                ColumnMetaData metaData = columnMetaData[i];
                
                if (dataType == String.class) {
                    if (value == null) {
                        if (metaData.nullable) {
                            stmt.setNull(idx, metaData.type);
                        } else {
                            stmt.setString(idx, "");
                        }
                    } else {
                        String sValue = (String)value;
                        if (metaData.length > 0 && sValue.length() > metaData.length) // If the string is too long, simply cut it off
                            sValue = sValue.substring(0, metaData.length);
                        
                        stmt.setString(idx, sValue);
                    }
                } else if (dataType == Boolean.class) {
                    if (value == null) {
                        if (metaData.nullable) {
                            stmt.setNull(idx, metaData.type);
                        } else {
                            stmt.setBoolean(idx, false);
                        }
                    } else {
                        stmt.setBoolean(idx, (Boolean)value);
                    }
                } else if (dataType == Integer.class) {
                    if (value == null){
                        if (metaData.nullable) {
                            stmt.setNull(idx, metaData.type);
                        } else {
                            stmt.setInt(idx, -1);
                        }
                    } else {
                        stmt.setInt(idx, (Integer)value);
                    }
                } else if (dataType == Long.class) {
                    if (value == null){
                        if (metaData.nullable) {
                            stmt.setNull(idx, metaData.type);
                        } else {
                            stmt.setLong(idx, -1);
                        }
                    } else {
                        stmt.setLong(idx, (Long)value);
                    }
                } else {
                    log.warning("Unsupported data type: " + dataType);
                    stmt.setObject(idx, value);
                }

            }
        }
        if (Utils.debugMode)
            log.finer(stmt.toString());
    }
    
    protected synchronized void writeSingleRow(String key, Object[] keyData) throws SQLException {
        if (Utils.debugMode)
            log.fine("Trying UPDATE for key " + key);
        setStatementValues(updateStmt, updateStmtKeyIdx, updateStmtVTCIdx, key, keyData);
        updateStmt.execute();
        int updateCnt = updateStmt.getUpdateCount();
        if (Utils.debugMode)
            log.fine("Updated " + updateCnt + " columns");
        if (updateCnt == 0) {
            log.fine("0 columns updated, trying INSERT");
            setStatementValues(insertStmt, insertStmtKeyIdx, insertStmtVTCIdx, key, keyData);
            insertStmt.execute();
            updateCnt = insertStmt.getUpdateCount();
            if (Utils.debugMode)
                log.fine("Inserted " + updateCnt + " columns");
        }
        
    }
    
    @Override
    protected void valueChanged(final String key, VirtualColumnType column,
            int columnIndex, Object value, Object oldValue) {
        if (SwingUtilities.isEventDispatchThread()) {
            // Make the DB write outside the event dispatch thread
            Utils.executorService.schedule(new Runnable() {
                public void run() {
                    realValueChanged(key);
                }
            }, 0, TimeUnit.MILLISECONDS);
        } else {
            realValueChanged(key);
        }
    }
    
    protected synchronized void realValueChanged(String key) {
        try {
            writeSingleRow(key, data.get(key));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error saving data for key " + key, e);
        }
    }
    
    protected void checkForUpdates() {
        try {
            log.fine("Checking for updates on the DB...");
            
            if (haveLastModified()) {
                if (!checkLastModified())
                    return;
            }
            
            log.fine("No last modified or modification found, doing full diff...");
            Map<String,Object[]> newData = loadValues(false);
            
            synchronized (this) {
                Map<String,Object[]>[] cmp = compareMaps(data, newData);

                if (cmp[COMPARE_MAP_INSERT].size() > 0 || cmp[COMPARE_MAP_UPDATE].size() > 0 || cmp[COMPARE_MAP_DELETE].size() > 0) {
                    log.fine("Differences found");
                    if (Utils.debugMode) {
                        log.finer("Inserts: " + cmp[COMPARE_MAP_INSERT].keySet());
                        log.finer("Updates: " + cmp[COMPARE_MAP_UPDATE].keySet());
                        log.finer("Deletes: " + cmp[COMPARE_MAP_DELETE].keySet());
                    }
                    
                    if (cmp[COMPARE_MAP_DELETE].size() > 0) {
                        for (String key : cmp[COMPARE_MAP_DELETE].keySet()) {
                            data.remove(key);
                        }
                    }
                    if (cmp[COMPARE_MAP_UPDATE].size() > 0) {
                        for (Entry<String,Object[]> update : cmp[COMPARE_MAP_UPDATE].entrySet()) {
                            data.put(update.getKey(), update.getValue());
                        }
                    }
                    if (cmp[COMPARE_MAP_INSERT].size() > 0) {
                        for (Entry<String,Object[]> insert : cmp[COMPARE_MAP_INSERT].entrySet()) {
                            data.put(insert.getKey(), insert.getValue());
                        }
                    }

                    fireColumnsChanged(cmp[COMPARE_MAP_INSERT].keySet(), cmp[COMPARE_MAP_UPDATE].keySet(), cmp[COMPARE_MAP_DELETE].keySet());
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error checking for updates", e);
        } 
    }
    
    public static int COMPARE_MAP_INSERT = 0;
    public static int COMPARE_MAP_UPDATE = 1;
    public static int COMPARE_MAP_DELETE = 2;
    /**
     * Compares two maps
     * @param oldMap
     * @param newMap
     * @return a tuple [INSERTs, UPDATEs, DELETEs] (also see the COMPARE_MAP* constants)
     */
    @SuppressWarnings("unchecked")
    public static Map<String,Object[]>[] compareMaps(Map<String,Object[]> oldMap, Map<String,Object[]> newMap) {
        Map<String,Object[]> inserts = new HashMap<String,Object[]>();
        Map<String,Object[]> updates = new HashMap<String,Object[]>(oldMap.size());
        Map<String,Object[]> deletes = new HashMap<String,Object[]>();
        
        for (Entry<String,Object[]> newEntry : newMap.entrySet()) {
            final String key = newEntry.getKey();
            
            Object[] oldValue = oldMap.get(key);
            if (oldValue != null) {
                Object[] newValue = newEntry.getValue();
                
                if (!Arrays.equals(oldValue, newValue)) {
                    updates.put(key, newValue);
                }
            } else {
                inserts.put(key, newEntry.getValue());
            }
        }
        
        for (Entry<String,Object[]> oldEntry : oldMap.entrySet()) {
            final String key = oldEntry.getKey();
            
            if (!newMap.containsKey(key)){
                deletes.put(key, oldEntry.getValue());
            }
        }
        
        return new Map[] {
                inserts,
                updates,
                deletes
        };
    }
    
    public JDBCVirtColPersister(ConnectionSettings settings) {
        this.settings = settings;
    }
    
    static class ColumnMetaData {
        
        public final String columnName;
        
        /**
         * data type as in java.util.sql.Types
         */
        public final int type;
        
        /**
         * max length
         */
        public final int length;
        
        /**
         * nullable
         */
        public final boolean nullable;

        public ColumnMetaData(ResultSetMetaData rsmd, int colIdx) throws SQLException {
            this.columnName = rsmd.getColumnName(colIdx);
            this.type = rsmd.getColumnType(colIdx);
            this.length = rsmd.getPrecision(colIdx);
            
            this.nullable = (rsmd.isNullable(colIdx) == ResultSetMetaData.columnNullable);
        }
        
        public ColumnMetaData(String columnName, int type, int length,
                boolean nullable) {
            super();
            this.columnName = columnName;
            this.type = type;
            this.length = length;
            this.nullable = nullable;
        }        
    }
    
    public static class ConnectionSettings extends AbstractConnectionSettings {
        public String driver = ""; //"org.postgresql.Driver";
        public String dbURL = "jdbc:"; //"jdbc:postgresql://hylafax-test/yajhfc";
        public String user = ""; //"fax";
        public final Password pwd = new Password(); //"fax";
        public boolean askForPWD = false;
        public String table = ""; //"ReadState";
        
        public String faxNameField = ""; //"Faxname";
        public String isReadField = ""; //"isRead";
        public String commentField = ""; 
        public String lastModifiedField = "";

        public String getFieldNameForVirtualColumnType(VirtualColumnType vtc) {
            switch (vtc) {
            case READ:
                return isReadField;
            case USER_COMMENT:
                return commentField;
            default:
                log.severe("Unknown column type " + vtc.name());
                return null;
            }
        }
        
        public String getKeyFieldName() {
            return faxNameField;
        }
        
        public ConnectionSettings() {
            super();
        }
        
        public ConnectionSettings(ConnectionSettings other) {
            super();
            copyFrom(other);
        }
        
        public ConnectionSettings(String config) {
            super();
            loadFromString(config);
        }
    }

    static class PersistenceMethod implements AvailablePersistenceMethod {        
        public boolean canConfigure() {
            return true;
        }

        public VirtColPersister createInstance(String config, int serverID) {
            return new JDBCVirtColPersister(new ConnectionSettings(config));
        }

        public String getDescription() {
            return Utils._("Database table");
        }

        public String getKey() {
            return "jdbc";
        }

        @Override
        public String toString() {
            return getDescription();
        }
        
        public String showConfigDialog(Window parent, String oldConfig) {
            ConnectionDialog cd;
            final String dialogTitle = Utils._("JDBC settings to save read/unread state and comment");
            final String dialogPrompt = Utils._("Please select which database fields correspond to the key, read/unread state and comment");
            ConnectionSettings cs = new ConnectionSettings(oldConfig);
            
            if (parent instanceof Dialog) {
                cd = new ConnectionDialog((Dialog)parent, dialogTitle, dialogPrompt, fieldCaptionMap, false);
            } else if (parent instanceof Frame) {
                cd = new ConnectionDialog((Frame)parent, dialogTitle, dialogPrompt, fieldCaptionMap, false);
            } else {
                throw new IllegalArgumentException("parent must be a Dialog or a Frame!");
            }
            
            if (cd.promptForNewSettings(cs)) {
                return cs.saveToString();
            } else {
                return null;
            }
        }
        
    }
}
