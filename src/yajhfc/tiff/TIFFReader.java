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
package yajhfc.tiff;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

public class TIFFReader {

    protected int numberOfPages;
    protected TIFFTag[] tags;
    
    public int getNumberOfPages() {
        return numberOfPages;
    }
    
    public TIFFTag[] getTags() {
        return tags;
    }
    
    public void read(FileInputStream inStream) throws IOException {
        read(inStream, true);
    }
    
    /**
     * Allows a sub class to control which tag IDs are to be read.
     * Return true to read the specified tag or false to skip it.
     * @param tagID
     * @param nIFD
     * @return
     */
    protected boolean shouldTagBeRead(int tagID, int nIFD) {
        return true;
    }
    
    /**
     * Finds the first tag having the specified ID
     * @param tagID
     * @return the tag or null if no such tag exists
     */
    public TIFFTag findTag(int tagID) {
        return findTag(tagID, -1);
    }
    
    /**
     * Finds the first tag having the specified ID and IFD number
     * @param tagID
     * @param nIFD
     * @return the tag or null if no such tag exists
     */
    public TIFFTag findTag(int tagID, int nIFD) {
        for (TIFFTag tag : tags) {
            if (tag.ID == tagID && (nIFD < 0 || tag.nIFD == nIFD)) {
                return tag;
            }
        }
        return null;
    }
    
    public void read(FileInputStream inStream, boolean readTags) throws IOException {
        List<TIFFTag> tagList = new ArrayList<TIFFTag>();
        numberOfPages = 0;
        FileChannel channel = inStream.getChannel();
        ByteBuffer buf = channel.map(MapMode.READ_ONLY, 0, channel.size());

        byte[] header = new byte[4];
        buf.position(0);
        buf.get(header);
        if (header[0] == 'M' && header[1] == 'M' && header[2] == 0 && header[3] == 42 ) {
            // TIFF Big Endian
            buf.order(ByteOrder.BIG_ENDIAN);
        } else if (header[0] == 'I' && header[1] == 'I' && header[2] == 42 && header[3] == 0 ) {
            // TIFF Little Endian
            buf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            throw new IOException("Not a supported TIFF file");
        }

        int ifdOffset = buf.getInt();
        while (ifdOffset > 0) {
            buf.position(ifdOffset);
            int numEntries = readUShort(buf);
            if (readTags) {
                for (int i = 0; i < numEntries; i++) {
                    int tag = readUShort(buf);
                    if (!shouldTagBeRead(tag, numberOfPages)) {
                        buf.position(buf.position() + 10); // Skip rest of tag
                        continue;
                    }
                    int dataType = buf.getShort();
                    int numValues = buf.getInt();

                    int length = TIFFTag.getLengthForDatatype(dataType) * numValues;
                    Object value;
                    int oldPosition = buf.position();
                    if (length <= 0) {
                        buf.position(oldPosition+4);
                        continue;
                    } else if (length <= 4) {  
                        value = readTagData(buf, dataType, numValues);
                    } else {                    
                        int newOffset = buf.getInt();
                        buf.position(newOffset);
                        value = readTagData(buf, dataType, numValues);
                    }
                    buf.position(oldPosition+4);

                    tagList.add(new TIFFTag(tag, numberOfPages, dataType, value));
                }
            } else {
                // Skip tags
                buf.position(buf.position() + numEntries * 12);
            }
            
            ifdOffset = buf.getInt();
            numberOfPages++;
        }
        tags = tagList.toArray(new TIFFTag[tagList.size()]);
    }
    
    protected Object readTagData(ByteBuffer buf, int dataType, int numValues) throws UnsupportedEncodingException {
        switch (dataType) {
        case TIFFConstants.DATATYPE_BYTE:
        case TIFFConstants.DATATYPE_SBYTE:
        case TIFFConstants.DATATYPE_UNDEFINED: {
            byte[] rv = new byte[numValues];
            buf.get(rv);
            return rv;
        }
        case TIFFConstants.DATATYPE_ASCII: {
            byte[] rv = new byte[numValues-1]; // Last byte is NUL
            buf.get(rv);
            return new String(rv, "ISO-8859-1");
        }
        case TIFFConstants.DATATYPE_SHORT: {
            int[] rv = new int[numValues];
            for (int i=0; i<numValues; i++) {
                rv[i] = readUShort(buf);
            }
            return rv;
        }
        case TIFFConstants.DATATYPE_SSHORT: {
            short[] rv = new short[numValues];
            for (int i=0; i<numValues; i++) {
                rv[i] = buf.getShort();
            }
            return rv;
        }
        case TIFFConstants.DATATYPE_LONG: {
            long[] rv = new long[numValues];
            for (int i=0; i<numValues; i++) {
                rv[i] = readUInt(buf);
            }
            return rv;
        }
        case TIFFConstants.DATATYPE_SLONG: {
            int[] rv = new int[numValues];
            for (int i=0; i<numValues; i++) {
                rv[i] = buf.getInt();
            }
            return rv;
        }
        case TIFFConstants.DATATYPE_FLOAT: {
            float[] rv = new float[numValues];
            for (int i=0; i<numValues; i++) {
                rv[i] = buf.getFloat();
            }
            return rv;
        }
        case TIFFConstants.DATATYPE_DOUBLE: {
            double[] rv = new double[numValues];
            for (int i=0; i<numValues; i++) {
                rv[i] = buf.getDouble();
            }
            return rv;
        }    
        case TIFFConstants.DATATYPE_RATIONAL: {
            long[] rv = new long[numValues*2];
            for (int i=0; i<numValues*2; i++) {
                rv[i] = readUInt(buf);
            }
            return rv;
        }
        case TIFFConstants.DATATYPE_SRATIONAL: {
            int[] rv = new int[numValues*2];
            for (int i=0; i<numValues*2; i++) {
                rv[i] = buf.getInt();
            }
            return rv;
        }
        default:
            return null;
        }
    }
    
    private static long readUInt(ByteBuffer buf) {
        return ((long)buf.getInt() & 0xffffffffL);
    }
    
    private static int readUShort(ByteBuffer buf) {
        return ((int)buf.getShort() & 0xffff);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: TIFFReader file1 ...");
        }
        TIFFReader r = new TIFFReader();
        for (String file : args) {
            System.out.println("Reading " + file + "...");
            FileInputStream inStream = new FileInputStream(file);
            r.read(inStream, true);
            inStream.close();
            
            System.out.println("Number of pages: " + r.numberOfPages);
            System.out.println("Tags:");
            for (TIFFTag tag : r.tags) {
                System.out.println(tag);
            }
            System.out.println("Resolution X: " + r.findTag(TIFFConstants.TIFFTAG_XRESOLUTION).doubleValue());
            System.out.println("-----------------------------");
            System.out.println();
        }
    }
}
