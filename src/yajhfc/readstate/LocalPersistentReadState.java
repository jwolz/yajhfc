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

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.VersionInfo;
import yajhfc.util.TransactFileOutputStream;

/**
 * Stores the read state of faxes in a local file.
 * @author jonas
 *
 */
public class LocalPersistentReadState extends PersistentReadState {
    private static final Logger log = Logger.getLogger(LocalPersistentReadState.class.getName());
    
    protected File file;
    protected Map<String,Boolean> readStateMap = null;
    protected boolean dirty = false;
    
    public LocalPersistentReadState(File file) {
        this.file = file;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.PersistentReadState#loadReadFaxes()
     */
    protected void loadReadFaxes() {    
        readStateMap = new HashMap<String, Boolean>();
        try {
            BufferedReader bIn = new BufferedReader(new FileReader(file));
            
            String line = null;
            while ((line = bIn.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    readStateMap.put(line, Boolean.TRUE);
                }
            }
            bIn.close();
            
            dirty = false;
        } catch (FileNotFoundException e) { 
            // No file yet - keep empty
        } catch (IOException e) {
            log.log(Level.WARNING, "Error reading read status: ", e);
        }
    }

    
    @Override
    public void persistReadState() {
        persistReadState(false);
    }
    
    @Override
    public void shutdown() {
        persistReadState(true);
    }

    protected void persistReadState(boolean always) {
        if (readStateMap == null || !(always || dirty) )
            return;
        
        try {
            BufferedWriter bOut = new BufferedWriter(new OutputStreamWriter(new TransactFileOutputStream(file, true)));
            
            bOut.write("# " + VersionInfo.AppShortName + " " + VersionInfo.AppVersion + " configuration file\n");
            bOut.write("# This file contains a list of faxes considered read\n\n");
            
            for ( Map.Entry<String, Boolean> entry : readStateMap.entrySet()) {
                if (entry.getValue()) {
                    bOut.write(entry.getKey());
                    bOut.write('\n');
                }
            }
            bOut.close();
            
            dirty = false;
        } catch (IOException e) {
            log.log(Level.WARNING, "Error storing read state: ", e);
        }
    }


    @Override
    public boolean isRead(String idValue) {
        Boolean value = getReadStateMap().get(idValue);
        return value == null ? false : value;
    }


    @Override
    public void setRead(String idValue, boolean read) {
        getReadStateMap().put(idValue, read);
        dirty = true;
    }

    
    @Override
    public void addReadStateChangedListener(ReadStateChangedListener listener) {
        // Do nothing as no notifications are sent
    }

    @Override
    public void removeReadStateChangedListener(ReadStateChangedListener listener) {
        // Do nothing as no notifications are sent
    }

    protected Map<String, Boolean> getReadStateMap() {
        if (readStateMap == null) {
            loadReadFaxes();
        }
        return readStateMap;
    }

    @Override
    public void cleanupState(Collection<String> existingFaxes) {
        if (existingFaxes.size() == 0)
            return; //"Safety" measure
        
        readStateMap.keySet().retainAll(existingFaxes);
        dirty = true;
    }
    
    static class PersistenceMethod implements AvailablePersistenceMethod {

        public boolean canConfigure() {
            return false;
        }

        public PersistentReadState createInstance(String config, int serverID) {
            File recvread = new File(Utils.getConfigDir(), "recvread-" + serverID);
            TransactFileOutputStream.checkRecovery(recvread);
            
            // Port old file
            File recvreadOld = new File(Utils.getConfigDir(), "recvread");
            if (recvreadOld.exists()) {
                recvreadOld.renameTo(recvread);
            }
            return new LocalPersistentReadState(recvread);
        }

        public String getDescription() {
            return Utils._("Local file");
        }

        public String getKey() {
            return "local";
        }

        @Override
        public String toString() {
            return getDescription();
        }
        
        public String showConfigDialog(Window parent, String oldConfig) {
            return null;
        }
        
    }

    @Override
    public void prepareReadStates() {
        if (readStateMap == null)
            loadReadFaxes();
    }
}
