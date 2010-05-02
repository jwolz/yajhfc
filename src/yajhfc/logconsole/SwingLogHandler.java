/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
