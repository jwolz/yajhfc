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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.VirtualColumnType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.server.ServerOptions;

/**
 * @author jonas
 *
 */
public abstract class VirtColPersister {
    static final Logger log = Logger.getLogger(VirtColPersister.class.getName());
    
    protected static final String KEY_PREFIX_QUEUE = "/Q/";

    /**
     * Saves the values and closes any open connections.
     * Must be called before the application exits!
     */
    public abstract void shutdown();
    
    /**
     * Saves the values.
     */
    public abstract void persistValues();
        
    /**
     * Adds the given listener to the list of listeners
     * @param listener
     */
    public abstract void addVirtColChangeListener(VirtColChangeListener listener);    
    
    /**
     * Removes the given listener from the list of listeners
     * @param listener
     */
    public abstract void removeVirtColChangeListener(VirtColChangeListener listener);
    
    /**
     * "Cleans" the read state up, i.e. tells the persister which
     * faxes still exist, so the deleted faxes can be deleted.
     * @param existingFaxes
     */
    public abstract void cleanupState(Collection<String> existingFaxes);
    
    /**
     * "Cleans" the read state up, i.e. tells the persister which
     * faxes still exist, so the deleted faxes can be deleted.
     * @param existingFaxes
     */
    public <T extends FmtItem> void cleanupStateFromJobs(Collection<FaxJob<T>>... existingFaxes) {
        Set<String> existing = new HashSet<String>();
        for (Collection<FaxJob<T>> jobList : existingFaxes) {
            for (FaxJob<T> job : jobList) {
                existing.add(getKeyForFaxJob(job));
            }
        }
        cleanupState(existing);
    }
    
    /**
     * Initialized the persister. This is intended to allow
     * any time consuming initializing to be done outside the event dispatching thread.
     * Sub classes should not rely on this method being called before the first call to isRead(), however.
     */
    public abstract void prepareValues();
    
    /**
     * Returns the String key to save the given FaxJob under
     * @param job
     * @return
     */
    public static String getKeyForFaxJob(FaxJob<? extends FmtItem> job) {
        switch (job.getParent().getJobType()) {
        case RECEIVED:
            return job.getIDValue().toString();            
        case SENT:
        case SENDING:
        case ARCHIVE:
            return KEY_PREFIX_QUEUE + job.getIDValue();
        default:
            assert false : "Should never happen!";
            return "N/A";
        }
    }
    
    /**
     * Parses the given key into a job ID value
     * @param key
     * @return
     */
    public static Object parseKey(String key) {
        if (key.startsWith(KEY_PREFIX_QUEUE)) {
            return Integer.valueOf(key.substring(KEY_PREFIX_QUEUE.length()));
        } else { // Received
            return key;
        }
    }
    
    /**
     * Updates the given fax job with the values from this persister
     * @param job
     */
    public <T extends FmtItem> void updateToFaxJob(FaxJob<T> job, boolean fireEvent) {
        String key = getKeyForFaxJob(job);
        for (Entry<VirtualColumnType,Integer> entry : job.getParent().getColumns().getVirtualColumnIndexes().entrySet()) {
            VirtualColumnType vtc = entry.getKey();
            if (vtc.isSaveable()) {
                job.setData(entry.getValue().intValue(), getValue(key, vtc), fireEvent);
            }
        }
    }
    
    /**
     * Updates the given fax jobs with the values from this persister
     * @param job
     */
    public <T extends FmtItem> void updateToAllFaxJobs(Collection<FaxJob<T>> jobs, boolean fireEvent) {
        for (FaxJob<T> job : jobs) {
            updateToFaxJob(job, fireEvent);
        }
    }
    
    /**
     * Updates the values in this persister with the values in the given fax job
     * @param job
     */
    public <T extends FmtItem> void updateFromFaxJob(FaxJob<T> job) {
        String key = getKeyForFaxJob(job);
        for (Entry<VirtualColumnType,Integer> entry : job.getParent().getColumns().getVirtualColumnIndexes().entrySet()) {
            VirtualColumnType vtc = entry.getKey();
            if (vtc.isSaveable()) {
                setValue(key, vtc, job.getData(entry.getValue().intValue()));
            }
        }
    }
    
    /**
     * Updates the given fax jobs with the values from this persister
     * @param job
     */
    public <T extends FmtItem> void updateFromAllFaxJobs(Collection<FaxJob<T>> jobs) {
        for (FaxJob<T> job : jobs) {
            updateFromFaxJob(job);
        }
    }
    
    /**
     * Returns the value of column for the given fax
     * @param key
     * @param column
     * @return
     */
    public Object getValue(FaxJob<? extends FmtItem> job, VirtualColumnType column) {
        return getValue(getKeyForFaxJob(job), column);
    }
    
    /**
     * Returns the value of column for the given fax
     * @param key
     * @param column
     * @return
     */
    public abstract Object getValue(String key, VirtualColumnType column);

    /**
     * Sets the value of column for the given fax
     * @param key
     * @param column
     * @return
     */
    public void setValue(FaxJob<? extends FmtItem> job, VirtualColumnType column, Object value) {
        setValue(getKeyForFaxJob(job), column, value);
    }
    
    /**
     * Sets the value of column for the given fax
     * @param key
     * @param column
     * @return
     */
    public abstract void setValue(String key, VirtualColumnType column, Object value);



    /**
     * The default persistence method. The createInstance() method of this class
     * MUST accept a null config as this is used as a fall back.
     */
    private static AvailablePersistenceMethod DEFAULT_METHOD;

    public static final List<AvailablePersistenceMethod> persistenceMethods = new ArrayList<AvailablePersistenceMethod>();
    static {
        DEFAULT_METHOD = new LocalVirtColPersister.PersistenceMethod();
        persistenceMethods.add(DEFAULT_METHOD);
        persistenceMethods.add(new JDBCVirtColPersister.PersistenceMethod());
    }
    
    /**
     * Returns the currently selected persistent read state
     * @return 
     */
    public static VirtColPersister createFromOptions(ServerOptions so) {
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
