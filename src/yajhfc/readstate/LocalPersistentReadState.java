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
            
            bOut.write("# " + Utils.AppShortName + " " + Utils.AppVersion + " configuration file\n");
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

        public PersistentReadState createInstance(String config) {
            File recvread = new File(Utils.getConfigDir(), "recvread");
            TransactFileOutputStream.checkRecovery(recvread);
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
