/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.server;

import java.util.logging.Logger;

import yajhfc.FaxOptions;
import yajhfc.HylaClientManager;
import yajhfc.IDAndNameOptions;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.model.servconn.ConnectionState;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.FaxListConnectionFactory;
import yajhfc.ui.YajOptionPane;
import yajhfc.virtualcolumnstore.VirtColPersister;

public class Server {
    static final Logger log = Logger.getLogger(Server.class.getName());
    
    protected ServerOptions options;
    protected FaxListConnection connection;
    protected VirtColPersister persistence;
    protected HylaClientManager clientManager;
    
    public int getID() {
        return options.id;
    }
    
    public String getDisplayName() {
        return options.name;
    }
    
    @Override
    public String toString() {
        return getOptions().toString();
    }
    
    public ServerOptions getOptions() {
        return options;
    }
    
    public synchronized void setOptions(ServerOptions options) {
        this.options = options;
        if (connection != null) {
            if (FaxListConnectionFactory.isConnectionTypeStillValid(connection, options)) {
                if (Utils.debugMode)
                    log.fine("Server " + options.id + ": reloading connection settings");
                connection.setOptions(options);
            } else {
                if (Utils.debugMode)
                    log.fine("Server " + options.id + ": removing old connection");
                if (connection.getConnectionState() != ConnectionState.DISCONNECTED) {
                    connection.disconnect();
                }
                connection = null;
            }
        }
        cleanupPersistence();
        cleanupClientManager();
    }
    
    public synchronized FaxListConnection getConnection() {
        if (connection == null) {
            try {
                if (Utils.debugMode)
                    log.fine("Server " + options.id + ": creating connection");
                connection = FaxListConnectionFactory.getFaxListConnection(options, getDialogUI());
                cleanupClientManager();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    protected YajOptionPane getDialogUI() {
        return Launcher2.application.getDialogUI();
    }
    
    public synchronized HylaClientManager getClientManager() {
        if (connection != null) {
            if (Utils.debugMode)
                log.fine("Server " + options.id + ": getting client manager from connection");
            return connection.getClientManager();
        } else {
            // Prefer using a client manager only and not a full connection
            if (clientManager == null) {
                if (Utils.debugMode)
                    log.fine("Server " + options.id + ": creating client manager");
                clientManager = new HylaClientManager(options);
            }
            if (Utils.debugMode)
                log.fine("Server " + options.id + ": using separate client manager");
            return clientManager;
        }
    }
    
    public synchronized VirtColPersister getPersistence() {
        if (persistence == null) {
            if (Utils.debugMode)
                log.fine("Server " + options.id + ": creating persistence");
            persistence = VirtColPersister.createFromOptions(options);
        }
        return persistence;
    }
    
    public SenderIdentity getDefaultIdentity() {
        FaxOptions fo = Utils.getFaxOptions();
        SenderIdentity si = null;
        if (options.defaultIdentity >= 0) {
            si = IDAndNameOptions.getItemByID(fo.identities, options.defaultIdentity);
        } 
        if (si == null) {
            si = fo.getDefaultIdentity();
        }
        return si;
    }
    
    public boolean isConnected() {
        return ((connection != null) && (connection.getConnectionState() == ConnectionState.CONNECTED))
                || (clientManager != null);
    }
    
//    /**
//     * The interval in which the read state is automatically saved
//     */
//    private static final int READ_PERSIST_INTERVAL = 12345;
////    Utils.executorService.scheduleAtFixedRate(new Runnable() {
////        public void run() {
////            PersistentReadState.getCurrent().persistReadState();
////        }
////    }, READ_PERSIST_INTERVAL, READ_PERSIST_INTERVAL, TimeUnit.MILLISECONDS);
    
    private void cleanupPersistence() {
        if (persistence != null) {
            if (Utils.debugMode)
                log.fine("Server " + options.id + ": saving persistence");
            persistence.persistValues();
            persistence.shutdown();
            persistence = null;
        }
    }
    
    private void cleanupClientManager() {
        if (clientManager != null) {
            if (Utils.debugMode)
                log.fine("Server " + options.id + ": destroying old client manager");
            clientManager.forceLogout();
            clientManager = null;
        }
    }
    
    public void shutdownCleanup() {
        cleanupPersistence();
        cleanupClientManager();
    }
    
    public Server(ServerOptions options) {
        setOptions(options);
    }
}
