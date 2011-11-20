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
package yajhfc.readstate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.server.ServerOptions;

/**
 * This class supports loading and saving the (un)read state of faxes.
 * @author jonas
 *
 */
public abstract class PersistentReadState {
    private static final Logger log = Logger.getLogger(PersistentReadState.class.getName());
    
    /**
     * Saves the list of faxes considered read and closes any open connections.
     * Must be called before the application exits!
     * @param readFaxes
     */
    public abstract void shutdown();
    
    /**
     * Saves the list of faxes considered read.
     * @param readFaxes
     */
    public abstract void persistReadState();
    
    /**
     * Returns if the fax identified by the given idValue is to be considered
     * read
     * @param idValue
     * @return
     */
    public abstract boolean isRead(String idValue);
    
    /**
     * Sets the read state of the fax identified by the given idValue 
     * @param idValue
     * @param read
     */
    public abstract void setRead(String idValue, boolean read);
    
    /**
     * Adds the given listener to the list of listeners
     * @param listener
     */
    public abstract void addReadStateChangedListener(ReadStateChangedListener listener);    
    
    /**
     * Removes the given listener from the list of listeners
     * @param listener
     */
    public abstract void removeReadStateChangedListener(ReadStateChangedListener listener);
    
    /**
     * "Cleans" the read state up, i.e. tells the persistent read state class which
     * faxes still exist, so the read state of deleted faxes can be deleted.
     * @param existingFaxes
     */
    public abstract void cleanupState(Collection<String> existingFaxes);
    
    /**
     * Prepares reading of the read state. This is intended to allow
     * any time consuming initializing to be done outside the event dispatching thread.
     * Sub classes should not rely on this method being called before the first call to isRead(), however.
     */
    public abstract void prepareReadStates();


    /**
     * The default persistence method. The createInstance() method of this class
     * MUST accept a null config as this is used as a fall back.
     */
    private static AvailablePersistenceMethod DEFAULT_METHOD;

    public static final List<AvailablePersistenceMethod> persistenceMethods = new ArrayList<AvailablePersistenceMethod>();
    static {
        DEFAULT_METHOD = new LocalPersistentReadState.PersistenceMethod();
        persistenceMethods.add(DEFAULT_METHOD);
        persistenceMethods.add(new JDBCPersistentReadState.PersistenceMethod());
    }
    
    /**
     * Returns the currently selected persistent read state
     * @return 
     */
    public static PersistentReadState createFromOptions(ServerOptions so) {
        String keyToFind = so.persistenceMethod;
        try {
            for (AvailablePersistenceMethod method : persistenceMethods) {
                if (method.getKey().equals(keyToFind)) {
                    String config = so.persistenceConfig;
                    if (config != null && config.length() == 0) {
                        config = null;
                    }
                    if (Utils.debugMode) {
                        log.info(String.format("Using persistence method %s with Config \"%s\".", keyToFind, config));
                    }
                    return method.createInstance(config, so.id);
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not create persistence object:", e);
        }

        // Something did not work...
        log.warning(String.format("Could not create instance for persistence method %s, using default.", keyToFind));
        return DEFAULT_METHOD.createInstance(null, so.id);
    }
    
}
