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
import yajhfc.readstate.PersistentReadState;
import yajhfc.ui.YajOptionPane;

public class Server {
    static final Logger log = Logger.getLogger(Server.class.getName());
    
    protected ServerOptions options;
    protected FaxListConnection connection;
    protected PersistentReadState persistence;
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
    
    public synchronized PersistentReadState getPersistence() {
        if (persistence == null) {
            if (Utils.debugMode)
                log.fine("Server " + options.id + ": creating persistence");
            persistence = PersistentReadState.createFromOptions(options);
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
            persistence.persistReadState();
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
