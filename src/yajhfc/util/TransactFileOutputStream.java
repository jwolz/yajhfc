/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * A file output stream that first writes its output to a temporary file and
 * renames it on close (to avoid "half written" files).
 * @author jonas
 *
 */
public class TransactFileOutputStream extends FileOutputStream {
    private static final Logger log = Logger.getLogger(TransactFileOutputStream.class.getName());
    
    protected File origFile;
    /**
     * Keep the old file as backup?
     */
    protected final boolean keepOld;
    
    public TransactFileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }
    
    public TransactFileOutputStream(File file, boolean keepOld) throws FileNotFoundException {
        super(getOutFile(file));
        
        this.origFile = file;
        this.keepOld = keepOld;
    }

    @Override
    public void close() throws IOException {
        flush();
        getFD().sync();
        super.close();
        
        File outFile = getOutFile(origFile);
        if (Utils.debugMode) {
            log.fine(outFile.getPath() + " was closed, renaming to original name (keepOld=" + keepOld + ").");
        }
        
        // Rename outFile to origFile
        if (keepOld || !outFile.renameTo(origFile)) {
            File oldFile = getBackupFileName(origFile);
            if (oldFile.exists())
                oldFile.delete();
            
            if (origFile.renameTo(oldFile)) {
                if (outFile.renameTo(origFile) && !keepOld)
                    oldFile.delete();
            } else {
                throw new IOException("Could not rename " + outFile + " to " + origFile);
            }
        }
        
        if (Utils.debugMode) {
            log.fine(outFile.getPath() + " successfully renamed to " + origFile.getPath());
        }
    }
    
    protected static File getOutFile(File origFile) {
        return new File(origFile.getPath() + "~new");
    }

    /**
     * Get the name of the generated backup file for the given "original" file
     * @param origFile
     * @return
     */
    public static File getBackupFileName(File origFile) {
        return new File(origFile.getPath() + "~old");
    }
    
    /**
     * Checks if the given file does not exist or is empty.
     * If yes, checks if a backup exists and tries to recover the file from that.
     * @param file
     * @return true if a recovery occured
     */
    public static boolean checkRecovery(File file) {
        if (!file.exists() || file.length() == 0) {
            log.fine(file + " file does not exist or is empty");
            
            File backupFile = getBackupFileName(file);
            if (backupFile.exists() && backupFile.length() > 0) {
                // Recover settings from backup file
                try {
                    FileInputStream inStream = new FileInputStream(backupFile);
                    FileOutputStream outStream = new FileOutputStream(file);
                    Utils.copyStream(inStream, outStream);
                    inStream.close();
                    outStream.close();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Error during recovery", e);
                }
                
                log.severe("Recovered " + file + " from backup file " + backupFile);
                return true;
            } else {
                log.info("Backup does not exist or is empty.");
            }
        }
        return false;
    }
}
