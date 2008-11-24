/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * This class supports loading and saving the (un)read state of faxes.
 * @author jonas
 *
 */
public abstract class PersistentReadState {
    private static final Logger log = Logger.getLogger(PersistentReadState.class.getName());
    
    /**
     * Saves the list of faxes considered read. Must be called
     * before the application exits!
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
     * Contains the currently used PersistentReadState class
     */
    private static PersistentReadState CURRENT; 
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
    public static PersistentReadState getCurrent() {
        if (CURRENT == null) {
            String keyToFind = Utils.getFaxOptions().persistenceMethod;
            try {
                for (AvailablePersistenceMethod method : persistenceMethods) {
                    if (method.getKey().equals(keyToFind)) {
                        String config = Utils.getFaxOptions().persistenceConfig;
                        if (config != null && config.length() == 0) {
                            config = null;
                        }
                        if (Utils.debugMode) {
                            log.info(String.format("Using persistence method %s with Config \"%s\".", keyToFind, config));
                        }
                        CURRENT = method.createInstance(config);
                        break;
                    }
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not create persistence object:", e);
            }
            
            if (CURRENT == null) { // Something did not work...
                log.warning(String.format("Could not create instance for persistence method %s, using default.", keyToFind));
                CURRENT = DEFAULT_METHOD.createInstance(null);
            }
        }
        return CURRENT;
    }
    
    public static void resetCurrent() {
        CURRENT = null;
    }
    
}
