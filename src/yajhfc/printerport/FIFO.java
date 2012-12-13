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
package yajhfc.printerport;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.PlatformInfo;

/**
 * @author jonas
 *
 */
public abstract class FIFO {
    public static Class<? extends FIFO> FIFO_IMPLEMENTATION;
    static {
        if (!PlatformInfo.IS_WINDOWS) {
            FIFO_IMPLEMENTATION = UnixFIFO.class;
        } else {
        	try {
				Class<?> win32FIFO = Class.forName("yajhfc.printerport.win32.Win32FIFO");
				Method isAvailable = win32FIFO.getMethod("isAvailable");
				if ((Boolean)isAvailable.invoke(null)) {
					FIFO_IMPLEMENTATION = (Class<? extends FIFO>) win32FIFO;
				}
			} catch (Exception e) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "Could not call yajhfc.printerport.win32.Win32FIFO.isAvailable()");
			} 
        }
    }
    
    /**
     * Creates a platform dependent FIFO (Named Pipe) with the specified name if available.
     * Throws an IOException if no FIFOs are supported on this platform or any error occurs creating the FIFO.
     * @param fifoName
     * @return
     */
    public static FIFO createFIFO(String fifoName) throws IOException {
        if (FIFO_IMPLEMENTATION == null) {
            throw new IOException("No FIFO implementation for this platform available");
        }
        try {
            Constructor<? extends FIFO> fifoConstructor = FIFO_IMPLEMENTATION.getConstructor(String.class);
            return fifoConstructor.newInstance(fifoName);
        } catch (Exception e) {
            throw (IOException)new IOException("Error creating the FIFO").initCause(e);
        } 
    }
    
    
    //////////////////////////////////////////////////////////////////////////////    
    
    protected String fifoName;
    
    /**
     * Creates a FIFO with the specified name
     * All sub classes must implement a public constructor in this form.
     * @param fileName
     */
    protected FIFO(String fifoName) {
        super();
        this.fifoName = fifoName;
    }
    
    /**
     * Opens an InpuStream reading from this FIFO.
     * May be called multiple times during the lifetime of the FIFO
     * @return
     */
    public abstract InputStream openInputStream() throws IOException;
    
    /**
     * Closes the FIFO (e.g. deletes the special file created for it)
     * Calling openInputStream after this will probably fail.
     */
    public abstract void close();
    
    /**
     * Returns a description of this FIFO (usually the file name)
     * @return
     */
    public String getFIFOName() {
        return fifoName;
    }
    
    @Override
    public String toString() {
        return getFIFOName();
    }
}
