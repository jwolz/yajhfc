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

import java.util.Collection;

import yajhfc.model.FmtItem;
import yajhfc.model.VirtualColumnType;
import yajhfc.model.servconn.FaxJob;

/**
 * @author jonas
 *
 */
public abstract class VirtColPersister {
    protected static final String KEY_PREFIX_QUEUE = "/Q/";

    /**
     * Saves the values and closes any open connections.
     * Must be called before the application exits!
     */
    public abstract void shutdown();
    
    /**
     * Saves the values.
     */
    public abstract void persistReadState();
        
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
     * Initialized the persister. This is intended to allow
     * any time consuming initializing to be done outside the event dispatching thread.
     * Sub classes should not rely on this method being called before the first call to isRead(), however.
     */
    public abstract void prepareReadStates();
    
    /**
     * Returns the String key to save the given FaxJob under
     * @param job
     * @return
     */
    public String getKeyForFaxJob(FaxJob<? extends FmtItem> job) {
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
    public Object parseKey(String key) {
        if (key.startsWith(KEY_PREFIX_QUEUE)) {
            return Integer.valueOf(key.substring(KEY_PREFIX_QUEUE.length()));
        } else { // Received
            return key;
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
}
