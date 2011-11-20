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
package yajhfc.logconsole;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.SwingUtilities;

/**
 * @author jonas
 *
 */
public class SwingLogHandler extends Handler {
    final protected List<LogListener> listeners = new ArrayList<LogListener>();
    final protected Queue<LogRecord> recordBuffer;
    protected int bufferSize;
    
    public void addLogListenerAndPublishBuffer(LogListener listener) {
        addLogListener(listener);
        publishBufferTo(listener);
    }
   
    public void addLogListener(LogListener listener) {
        listeners.add(listener);
    }
    
    public void removeLogListener(LogListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Publishes all buffered LogRecords to the listener
     * @param listener
     */
    public void publishBufferTo(LogListener listener) {
        LogRecord[] records;
        synchronized (recordBuffer) {
            records = recordBuffer.toArray(new LogRecord[recordBuffer.size()]);
        }
        for (LogRecord record : records) {
            listener.recordPublished(record);
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.logging.Handler#close()
     */
    @Override
    public void close() throws SecurityException {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see java.util.logging.Handler#flush()
     */
    @Override
    public void flush() {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    @Override
    public void publish(final LogRecord record) {
        if (!isLoggable(record))
            return;
        
        bufferRecord(record);
        if (listeners.size() > 0) {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   for (LogListener ll : listeners) {
                       ll.recordPublished(record);
                   }
                } 
            });
        }
    }

    /**
     * Adds the specified log record to the buffer
     * @param record
     */
    protected void bufferRecord(final LogRecord record) {
        synchronized (recordBuffer) {
            recordBuffer.offer(record);
            while (recordBuffer.size() > bufferSize) {
                recordBuffer.poll();
            }
        }
    }
    
    public SwingLogHandler() {
        this(20);
    }
    
    public SwingLogHandler(int bufferSize) {
        recordBuffer = new LinkedList<LogRecord>();
        this.bufferSize = bufferSize;
        setLevel(Level.INFO);
    }
    

    public interface LogListener {
        public void recordPublished(LogRecord record);
    }
}
