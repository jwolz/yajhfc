/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import yajhfc.PaperSize;
import yajhfc.Utils;

/**
 * Interface for File Converters
 * @author jonas
 *
 */
public interface FileConverter {
    
    /**
     * Converts the input from inStream to a format HylaFAX and GhostScript understand.
     * This means PostScript or PDF.
     * @param inFile the input file
     * @param destination the stream the converted data shall be written to
     * @param desiredFormat the desired format (PS or PDF). This is only a hint, i.e. this method may convert to any of these formats.
     * @throws ConversionException
     * @throws IOException
     */
    public void convertToHylaFormat(File inFile, OutputStream destination, PaperSize paperSize, FileFormat desiredFormat) throws ConversionException, IOException;
    
    /**
     * Returns if this FileConverter may be replaced by a user defined one
     * @return
     */
    public boolean isOverridable();
    
    /**
     * A dummy file converter that just copies the input to the output
     */
    public static final FileConverter IDENTITY_CONVERTER = new FileConverter() {
      public void convertToHylaFormat(File inFile,
                OutputStream destination, PaperSize paperSize, FileFormat desiredFormat) throws ConversionException, IOException {
            InputStream in = new FileInputStream(inFile);
            Utils.copyStream(in, destination);
            in.close();
        }  
      
      public boolean isOverridable() {
            return false;
        }
    };
    
    public static class ConversionException extends Exception {
        public ConversionException(String message) {
            super(message);
        }
        
        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConversionException(Throwable cause) {
            super(cause);
        }
    }
}
