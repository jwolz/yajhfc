package yajhfc.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yajhfc.Utils;


public class ServerManager {

    private static ServerManager DEFAULT = new ServerManager();
    
    public static ServerManager getDefault() {
        return DEFAULT;
    }
    
    
    protected final List<Server> servers = new ArrayList<Server>();
    protected final Map<Integer,Server> serversByID = new HashMap<Integer,Server>();
    protected Server currentServer = null;
    
    public synchronized void optionsChanged() {
        servers.clear();
        for (ServerOptions serverOpt : Utils.getFaxOptions().servers) {
            Server srv = serversByID.get(serverOpt.id);
            if (srv != null) {
                srv.setOptions(serverOpt);
            } else {
                srv = new Server(serverOpt);
            }
            servers.add(srv);
        }
        
        // Rebuild serversByID
        serversByID.clear();
        for (Server server : servers) {
            serversByID.put(server.getID(), server);
        }
       
        if (currentServer != null) {
            if (!setCurrentByID(currentServer.getID())) {
                setCurrentByIndex(0);
            }
        } else {
            setCurrentByIndex(0);
        }
    }
    
    public void setCurrentByIndex(int serverIndex) {
        currentServer = servers.get(serverIndex);
    }
    
    /**
     * Sets the current server by ID.
     * Returns true if this was successful (i.e. a server with the specified ID was found) and false otherwise
     * @param serverID
     * @return
     */
    public synchronized boolean setCurrentByID(int serverID) {
        Server newServer = serversByID.get(serverID);
        if (newServer != null) {
            currentServer = newServer;
            return true;
        } else {
            return false;
        }
    }
    
    public Server getServerByID(int serverID) {
        return serversByID.get(serverID);
    }
    
    public synchronized List<Server> getServers() {
        return servers;
    }
    
    public synchronized Server getCurrent() {
        return currentServer;
    }
    
    public void shutdownCleanup() {
        for (Server server : servers) {
            server.shutdownCleanup();
        }
    }
    
    public ServerManager() {
        optionsChanged();
    }
}
