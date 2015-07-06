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

import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.VirtualColumnType;
import yajhfc.util.TransactFileOutputStream;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author jonas
 *
 */
public class LocalVirtColPersister extends CachingVirtColPersister implements Runnable {

    static final Logger log = Logger.getLogger(LocalVirtColPersister.class.getName());

    protected static final String KEY_COLUMN_NAME = "KEY";

    protected static final String FILE_CHARSET = "UTF-8";
    
    protected static final int SAVE_INTERVAL = 5;
    
    protected File saveFile;
    protected File oldFormatFile;
    protected ScheduledFuture<?> sft;
    
    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#shutdown()
     */
    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        if (sft!=null) {
            sft.cancel(false);
            sft=null;
        }
        persistValues();
    }
    
    public void run() {
        // Periodically save
        persistValues();
    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#persistReadState()
     */
    @Override
    public synchronized void persistValues() {
        try {
            if (dirty)
                saveValues(saveFile);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error saving values", e);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#addVirtColChangeListener(yajhfc.virtualcolumnstore.VirtColChangeListener)
     */
    @Override
    public void addVirtColChangeListener(VirtColChangeListener listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#removeVirtColChangeListener(yajhfc.virtualcolumnstore.VirtColChangeListener)
     */
    @Override
    public void removeVirtColChangeListener(VirtColChangeListener listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#cleanupState(java.util.Collection)
     */
    @Override
    public synchronized void cleanupState(Collection<String> existingFaxes) {
        checkInitialized();
        
        // TODO
        data.keySet().retainAll(existingFaxes);
    }

    protected void checkInitialized() {
        if (data != null) 
            return;
        
        data = new TreeMap<String,Object[]>();
        if (saveFile.canRead())
            try {
                loadValues(saveFile);
            } catch (IOException e) {
                log.log(Level.WARNING, "Error loading values", e);
            }
        else if (oldFormatFile.canRead()) {
            try {
                loadValuesFromOldFormatFile(oldFormatFile);
            } catch (IOException e) {
                log.log(Level.WARNING, "Error loading values", e);
            }
        } else {
            log.info("No saved values found");
        }
        
        sft = Utils.executorService.scheduleWithFixedDelay(this, SAVE_INTERVAL, SAVE_INTERVAL, TimeUnit.SECONDS);
    }
    
    protected static String booleanToString(boolean b) {
        return b ? "Y" : "N";
    }
    
    protected static boolean stringToBoolean(String s) {
        if ("Y".equalsIgnoreCase(s))
            return true;
        else if ("N".equalsIgnoreCase(s))
            return false;
        else
            return Boolean.parseBoolean(s);
    }
    
    protected synchronized void loadValuesFromOldFormatFile(File file) throws IOException {
        log.fine("Reading values from old format file " + file);
        
        data.clear();
        
        BufferedReader bIn = new BufferedReader(new FileReader(file));
        
        String line = null;
        while ((line = bIn.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#") && line.length() > 0) {
                setValue(line, VirtualColumnType.READ, Boolean.TRUE);
            }
        }
        bIn.close();
        
        dirty = false;
    }
    
    protected synchronized void loadValues(File file) throws IOException {
        log.fine("Reading values from " + file);
        
        data.clear();
        
        CSVReader r = new CSVReader(new InputStreamReader(new FileInputStream(file), FILE_CHARSET));
        String[] buf;
        
        buf = r.readNext();
        if (buf == null)
            throw new IOException("File has no header line!");
        
        if (Utils.debugMode)
            log.fine("First line is: " + Arrays.toString(buf));
        
        if (!KEY_COLUMN_NAME.equals(buf[0])) 
            throw new IOException("Invalid file format: First column must be " + KEY_COLUMN_NAME);
        
        
        VirtualColumnType[] map = new VirtualColumnType[buf.length-1];
        
        for (int i=1; i<buf.length;i++) {
            VirtualColumnType vtc;
            
            try {
                vtc = Enum.valueOf(VirtualColumnType.class, buf[i]);
            } catch (Exception e) {
                vtc = null;
                log.log(Level.WARNING, "File " + file + " has unknown column named " + buf[i], e);
            }
            
            map[i-1] = vtc;
        }
        
        if (Utils.debugMode)
            log.fine("map is: " + Arrays.toString(map));
        
        while ((buf = r.readNext()) != null) {
            int len = Math.min(buf.length-1, map.length);

            if (len <= 0)
                continue;
            
            String key = buf[0];
            
            Object[] keyData = allocateKeyData();
            
            for (int i = 0; i<len; i++) {
                Object value;
                String strValue = buf[i+1];
                VirtualColumnType vtc = map[i];
                Class<?> dataType = vtc.getDataType();
                
                if (dataType == String.class) {
                    value = strValue;
                } else {
                    if (strValue == null || strValue.length()==0) {
                        value = null;
                    } else {
                        if (dataType == Boolean.class) {
                            value = Boolean.valueOf(stringToBoolean(strValue));
                        } else if (dataType == Integer.class) {
                            value = Integer.valueOf(strValue);
                        } else if (dataType == Long.class) {
                            value = Long.valueOf(strValue);
                        } else {
                            log.warning("Unsupported data type: " + dataType);
                            value = strValue;
                        }
                    }
                }
                
                keyData[columnToIndex(vtc)] = value;
            }
            
            if (Utils.debugMode)
                log.finest("line " + Arrays.toString(buf) + " parsed to: key=" + key + "; keyData=" + Arrays.toString(keyData));
            
            data.put(key, keyData);
        }
        
        r.close();
        dirty = false;
    }
    
    protected synchronized void saveValues(File file) throws IOException {
        log.fine("Saving values to " + file);
        
        CSVWriter w = new CSVWriter(new OutputStreamWriter(new TransactFileOutputStream(file), FILE_CHARSET));
        // buffer; buf[0] is the key
        String[] buf = new String[columnMap.size()+1]; 
        
        // Build the header line
        VirtualColumnType[] revMap = getReverseMap();
        buf[0] = KEY_COLUMN_NAME;
        for (int i=0; i<revMap.length; i++) {
            buf[i+1] = revMap[i].name();
        }
        w.writeNext(buf);
        
        for (Entry<String,Object[]> e : data.entrySet()) {
            Object[] vals = e.getValue();
            
            buf[0] = e.getKey();
            for (int i=0; i<vals.length; i++) {
                String strValue;
                final Object value = vals[i];
                
                if (value != null) {
                    if (value instanceof Boolean)
                        strValue = booleanToString(((Boolean)value).booleanValue());
                    else
                        strValue = value.toString();
                } else {
                    strValue = "";
                }
                
                buf[i+1] = strValue;
            }
            
            w.writeNext(buf);
        }
        
        w.close();
        dirty = false;
    }
    
    public LocalVirtColPersister(File file)  {
        this(file, null);
    }
    
    public LocalVirtColPersister(File file, File oldFormatFile)  {
        super();
        this.saveFile = file;
        this.oldFormatFile = oldFormatFile;
    }
    
    static class PersistenceMethod implements AvailablePersistenceMethod {

        public boolean canConfigure() {
            return false;
        }

        public VirtColPersister createInstance(String config, int serverID) {
            File recvread = new File(Utils.getConfigDir(), "recvread-" + serverID + ".csv");
            TransactFileOutputStream.checkRecovery(recvread);
            
            File recvreadOld = new File(Utils.getConfigDir(), "recvread-" + serverID);
            TransactFileOutputStream.checkRecovery(recvreadOld);
            
            // Port old file before multi-server support
            File recvreadReallyOld = new File(Utils.getConfigDir(), "recvread");
            if (recvreadReallyOld.exists()) {
                recvreadReallyOld.renameTo(recvreadOld);
            }
            return new LocalVirtColPersister(recvread, recvreadOld);
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
}
