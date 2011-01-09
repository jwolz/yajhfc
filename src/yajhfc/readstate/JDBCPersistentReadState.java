/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.readstate;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Password;
import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.phonebook.AbstractConnectionSettings;
import yajhfc.phonebook.jdbc.ConnectionDialog;
import yajhfc.phonebook.jdbc.ConnectionDialog.FieldMapEntry;
import yajhfc.plugin.PluginManager;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.PasswordDialog;

/**
 * @author jonas
 *
 */
public class JDBCPersistentReadState extends PersistentReadState {
    static final Logger log = Logger.getLogger(JDBCPersistentReadState.class.getName());
    
    protected ConnectionSettings settings;
    
    protected Connection connection;
    protected PreparedStatement selectStmt, updateStmt, insertStmt;
    private Map<String,Boolean> readStateMap = null;
    
    protected ScheduledFuture<?> updateTask;
    
    protected List<ReadStateChangedListener> listeners = new ArrayList<ReadStateChangedListener>();
    
    private static final String SELECT_TEMPLATE = "SELECT {1}, {2} FROM {0};";
    private static final String UPDATE_TEMPLATE = "UPDATE {0} SET {2} = ? WHERE {1} = ?;";
    private static final String INSERT_TEMPLATE = "INSERT INTO {0} ({1},{2}) VALUES (?,?);";
    
    protected static final Map<String,FieldMapEntry> fieldCaptionMap = new HashMap<String,FieldMapEntry>();
    static {
        fieldCaptionMap.put("faxNameField", new FieldMapEntry(Utils._("Fax filename:"),0));
        fieldCaptionMap.put("isReadField", new FieldMapEntry(Utils._("Read/Unread State:"),1));
    }
    
    protected synchronized void openConnection() throws ClassNotFoundException, SQLException {
        if (Utils.debugMode) {
            log.fine(String.format("Connecting: driver=%s, URL=%s, username=%s, askForPassword=%s", settings.driver, settings.dbURL, settings.user, settings.askForPWD));
        }
        
        PluginManager.registerJDBCDriver(settings.driver);

        String password;
        if (settings.askForPWD) {
            String[] pwd = PasswordDialog.showPasswordDialog(Launcher2.application.getFrame(), Utils._("Database password"), MessageFormat.format(Utils._("Please enter the database password (database: {0}):"), settings.dbURL), settings.user, false);
            if (pwd == null)
                return;
            else
                password = pwd[1];
        } else {
            password = settings.pwd.getPassword();
        }
        connection = DriverManager.getConnection(settings.dbURL, settings.user, password);
        connection.setAutoCommit(true);
        
        String sql = MessageFormat.format(SELECT_TEMPLATE, settings.table, settings.faxNameField, settings.isReadField);
        if (Utils.debugMode) {
            log.fine("SELECT statement: " + sql);
        }
        selectStmt = connection.prepareStatement(sql);
        
        sql = MessageFormat.format(INSERT_TEMPLATE, settings.table, settings.faxNameField, settings.isReadField);
        if (Utils.debugMode) {
            log.fine("INSERT statement: " + sql);
        }
        insertStmt = connection.prepareStatement(sql);
        
        sql= MessageFormat.format(UPDATE_TEMPLATE, settings.table, settings.faxNameField, settings.isReadField);
        if (Utils.debugMode) {
            log.fine("UPDATE statement: " + sql);
        }
        updateStmt = connection.prepareStatement(sql);
    }

    protected Map<String,Boolean> loadReadState() throws SQLException, ClassNotFoundException {
        if (connection == null) {
            openConnection();
        }
        Map<String,Boolean> readMap = new HashMap<String,Boolean>();
        ResultSet rs = selectStmt.executeQuery();
        int fieldIdx = rs.findColumn(settings.faxNameField);
        int readIdx = rs.findColumn(settings.isReadField);
        
        while (rs.next()) {
            readMap.put(rs.getString(fieldIdx),rs.getBoolean(readIdx));
        }
        rs.close();
        return readMap;
    }
    
    protected synchronized void setReadStateMap(Map<String,Boolean> newMap) {
        this.readStateMap = newMap;
    }
    
    public synchronized Map<String, Boolean> getReadStateMap() {
        if (readStateMap == null) {          
            try {
                readStateMap = loadReadState();
            } catch (Exception e) {
                readStateMap = new HashMap<String,Boolean>();
                //log.log(Level.SEVERE, "Could not read read state table.", e);
                ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Utils._("Could not open the database table to store the read/unread state. The current read/unread state will not be saved.\n Reason:"), e);
            }
            
            Runnable updater = new Runnable() {
                public void run() {
                    try {      
                        Map<String,Boolean> newReadStateMap = loadReadState();
                        Map<String,Boolean> oldReadStateMap = getReadStateMap();
                        
                        Set<String> changedFaxes = new HashSet<String>();
                        for (Map.Entry<String, Boolean> newEntry : newReadStateMap.entrySet()) {
                            Boolean oldRead = oldReadStateMap.get(newEntry.getKey());
                            if (oldRead == null || oldRead != newEntry.getValue()) {
                                changedFaxes.add(newEntry.getKey());
                            }
                        }
                        setReadStateMap(newReadStateMap);
                        
//                        System.out.println("OLD STATE: " + oldReadStateMap);
//                        System.out.println("NEW STATE: " + newReadStateMap);
//                        System.out.println("Changed Faxes: " + changedFaxes);
                        
                        if (changedFaxes.size() > 0) {
                            fireReadStateChanged(changedFaxes);
                        }
                    } catch (SQLException e) {
                        log.log(Level.WARNING, "Could not read read state table.", e);
                    } catch (ClassNotFoundException e) {
                        log.log(Level.WARNING, "Could not read read state table.", e);
                    }
                }  
            };
            updateTask = Utils.executorService.scheduleAtFixedRate(updater, 
                    Utils.getFaxOptions().statusUpdateInterval,
                    Utils.getFaxOptions().statusUpdateInterval,
                    TimeUnit.MILLISECONDS);
        }
        return readStateMap;
    }
    
    @Override
    public void prepareReadStates() {
        getReadStateMap();
    }
    
    /* (non-Javadoc)
     * @see yajhfc.readstate.PersistentReadState#isRead(java.lang.String)
     */
    @Override
    public boolean isRead(String idValue) {
        Boolean value = getReadStateMap().get(idValue);
        return (value == null) ? false : value;
    }

    /* (non-Javadoc)
     * @see yajhfc.readstate.PersistentReadState#setRead(java.lang.String, boolean)
     */
    @Override
    public synchronized void setRead(final String idValue, final boolean read) {
        Map<String,Boolean> map = getReadStateMap();
        Boolean oldValue = map.get(idValue);
        if (oldValue != null && oldValue.booleanValue() == read) 
            return; // Read state has not changed, no update necessary
        
        final boolean isNew = (oldValue == null);
        
        Runnable dbUpdater = new TimerTask() {
            public void run() {
                try {
                    if (isNew) {
                        if (Utils.debugMode) {
                            log.fine("Insert tuple (" + idValue + ", " + read + ")");
                        }
                        
                        insertStmt.setString(1, idValue);
                        insertStmt.setBoolean(2, read);
                        insertStmt.execute();
                    } else {
                        if (Utils.debugMode) {
                            log.fine("Update tuple (" + idValue + ", " + read + ")");
                        }
                        
                        updateStmt.setString(2, idValue);
                        updateStmt.setBoolean(1, read);
                        updateStmt.execute();
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Could not update read state table.", e);
                }
            };
        };
        Utils.executorService.submit(dbUpdater);
        
        map.put(idValue, read);
    }
    
    
    protected synchronized void disconnect() {
        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
        
        if (connection != null) {
            try {
                selectStmt.close();
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
            insertStmt = null;
            updateStmt = null;
            connection = null;
            
            readStateMap = null;
        }
    }
    
    /* (non-Javadoc)
     * @see yajhfc.readstate.PersistentReadState#persistReadState()
     */
    @Override
    public void persistReadState() {
        // NOP
    }
    
    @Override
    public void shutdown() {
        disconnect();
    }

    /* (non-Javadoc)
     * @see yajhfc.readstate.PersistentReadState#removeReadStateChangedListener(yajhfc.readstate.ReadStateChangedListener)
     */
    @Override
    public void removeReadStateChangedListener(ReadStateChangedListener listener) {
        listeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.readstate.PersistentReadState#addReadStateChangedListener(yajhfc.readstate.ReadStateChangedListener)
     */
    @Override
    public void addReadStateChangedListener(ReadStateChangedListener listener) {
        listeners.add(listener);
    }

    protected void fireReadStateChanged(Set<String> changedFaxes) {
        log.fine("fireReadStateChanged: " + changedFaxes);
        for (ReadStateChangedListener rscl : listeners) {
            rscl.readStateChanged(this, changedFaxes);
        }
    }
    
    public JDBCPersistentReadState(ConnectionSettings settings) {
        this.settings = settings;
    }
    
    @Override
    public synchronized void cleanupState(Collection<String> existingFaxes) {
        if (existingFaxes.size() == 0 || connection == null)
            return; //"Safety" measure
        
        List<String> removedFaxes = new ArrayList<String>();
        for (String id : readStateMap.keySet()) {
            if (!existingFaxes.contains(id)) {
                removedFaxes.add(id);
            }
        }
        if (removedFaxes.size() == 0)
            return; // Nothing to do
        
        StringBuilder sqlBuf = new StringBuilder();
        sqlBuf.append("DELETE FROM ").append(settings.table).append(" WHERE ");
        sqlBuf.append(settings.faxNameField).append(" IN (");
        for (String id : removedFaxes) {
            sqlBuf.append("'").append(id).append("', ");
        }
        sqlBuf.delete(sqlBuf.length()-2, sqlBuf.length());
        sqlBuf.append(");");
        
        if (Utils.debugMode) {
            log.fine("DELETE statement: " + sqlBuf);
        }
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sqlBuf.toString());
            stmt.close();
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error cleaning up state table:", e);
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
        //public int refreshDelay = 4000;
        
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

        public PersistentReadState createInstance(String config, int serverID) {
            return new JDBCPersistentReadState(new ConnectionSettings(config));
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
            final String dialogTitle = Utils._("JDBC settings to save read/unread state");
            final String dialogPrompt = Utils._("Please select which fields in the table correspond to the necessary fields to save the read/unread state");
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
