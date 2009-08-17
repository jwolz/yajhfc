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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A file output stream that first writes its output to a temporary file and
 * renames it on close (to avoid "half written" files).
 * @author jonas
 *
 */
public class TransactFileOutputStream extends FileOutputStream {

    protected File origFile;  
    
    public TransactFileOutputStream(File file) throws FileNotFoundException {
        super(getOutFile(file));
        
        this.origFile = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        
        File outFile = getOutFile(origFile);
        // Rename outFile to origFile
        if (!outFile.renameTo(origFile)) {
            File oldFile = new File(origFile.getPath() + "~old");
            if (origFile.renameTo(oldFile)) {
                outFile.renameTo(origFile);
                oldFile.delete();
            } else {
                throw new IOException("Could not rename " + outFile + " to " + origFile);
            }
        }
    }
    
    protected static File getOutFile(File origFile) {
        return new File(origFile.getPath() + "~new");
    }

}
