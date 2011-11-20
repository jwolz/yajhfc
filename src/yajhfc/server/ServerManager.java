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
            if (servers.size() > 0)
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
