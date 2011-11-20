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
package yajhfc.model.jobq;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

/**
 * An abstract interface used to access the archive directory.
 * The directory separator is "/".
 * 
 * @author jonas
 *
 */
public interface HylaDirAccessor {
    /**
     * Lists the root directory
     * @return
     * @throws IOException
     */
    public String[] listDirectory() throws IOException;
    
    /**
     * Lists the given directory
     * @param dir
     * @return
     * @throws IOException
     */
    public String[] listDirectory(String dir) throws IOException;
    
    /**
     * Returns an Reader for the given file name
     * @param fileName
     * @return
     * @throws IOException
     */
    public Reader getInputReader(String fileName) throws IOException;
    
    /**
     * Copies the given file's content to the target stream
     * @param fileName
     * @param target
     * @throws IOException
     */
    public void copyFile(String fileName, OutputStream target) throws IOException;
    
    
    /**
     * Returns a (possibly temporary) File containing the data for the specified file.
     * @param fileName
     * @throws IOException
     */
    public File getFile(String fileName) throws IOException;
    
    /**
     * Deletes the given file
     * @param fileName
     * @throws IOException
     */
    public void deleteFile(String fileName) throws IOException;
    
    /**
     * Deletes the given directory including its content
     * @param fileName
     * @throws IOException
     */
    public void deleteTree(String dirName) throws IOException;
    
    /**
     * Gets the last modification time of the root directory.
     * Note: This may be a logical time, too
     * @return
     * @throws IOException
     */
    public long getLastModified() throws IOException;
    
    /**
     * Gets the last modification time of the given file or directory.
     * Note: This may be a logical time, too
     * @return
     * @throws IOException
     */
    public long getLastModified(String fileName) throws IOException;
    
    /**
     * Returns the given file's size
     * @param fileName
     * @return
     * @throws IOException
     */
    public long getSize(String fileName) throws IOException;
    
    /**
     * Returns the given file's protection as bit mask similar to the 
     * st_mode field from the stat(2) UNIX API, i.e. this is the same value as you would give to chmod
     * @param fileName
     * @return
     * @throws IOException
     */
    public int getProtection(String fileName) throws IOException;
    
    /**
     * Returns the base path used in this dir accessor
     * @return
     */
    public String getBasePath();
}
