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
package yajhfc.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jonas
 *
 */
public class ReplacerOutputStream extends FilterOutputStream {

    private final byte[] bytesToReplace;
    private final byte[] replacement;
    private final byte[] searchBuf; // ring buffer if the user uses write(int) 
    private int searchBufPtr, searchBufLen;
    private static final int searchBufScale = 3;
    
    public ReplacerOutputStream(OutputStream out, byte[] bytesToReplace,
            byte[] replacement) {
        super(out);
        this.bytesToReplace = bytesToReplace;
        this.replacement = replacement;
        searchBuf = new byte[bytesToReplace.length*searchBufScale];
        searchBufPtr = 0;
        searchBufLen = 0;
    }

    @Override
    public void flush() throws IOException {
        flushSearchBuf();
        out.flush();
    }

    /**
     * Flushes the search buffer without flushing the output stream
     */
    private void flushSearchBuf() throws IOException {
        if (searchBufLen > 0) {
            writeFromSearchBuf(searchBufPtr-searchBufLen, searchBufLen);
            searchBufPtr = 0;
            searchBufLen = 0;
        }
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        final int replLen = bytesToReplace.length;
        if (len <= 2*replLen) {
            writeSingleBytes(b, off, len);
        } else {
            writeSingleBytes(b, off, replLen); // Check if b begins with a suffix of the bytes to replace
            
            int readOff; // points to the first byte not already written to the out stream
            if (searchBufLen > replLen) { 
                // If there is more in the buffer than the bytes to replace, flush the buffer
                // except the bytes we appended above
                searchBufLen -= replLen;
                searchBufPtr = getBufPtr(searchBufPtr - replLen);
                readOff = off;
                flushSearchBuf();
            } else {
                // The complete buffer content has been appended by us -> discard it
                readOff = off + replLen - searchBufLen;
                searchBufLen = 0;
            }
            
            final int loopEnd = off+len-replLen;
            for (int i=readOff; i < loopEnd; i++) {
                boolean equals = true;
                for (int j = 0; j<replLen; j++) { // Try to find the bytes we should replace
                    if (b[i+j] != bytesToReplace[j]) {
                        equals = false;
                        break;
                    }
                }
                if (equals) {
                    if (i > readOff)
                        out.write(b, readOff, i-readOff); // Write the parts before the bytes to replace
                    out.write(replacement); // write the Replacement
                    readOff = i = i+replLen; // set the offset to the first byte not already written or skipped
                }
            }
            if (readOff < loopEnd) { // Write the bytes not already written
                out.write(b, readOff, loopEnd-readOff);
            }
            writeSingleBytes(b, loopEnd, replLen); // Append the last bytes in case they are a suffix of the bytes to replace
        }
    }

    private void writeSingleBytes(byte[] b, int off, int len) throws IOException {
        for (int i=off; i<(off+len); i++) {
            write(b[i]);
        }
    }    
    
    @Override
    public void write(int b) throws IOException {
        final int replLen = bytesToReplace.length;
        
        searchBuf[searchBufPtr] = (byte)b; // Apend the byte to the buffer
        searchBufPtr = getBufPtr(searchBufPtr+1); // and increment the pointer
        
        if (searchBufLen < searchBuf.length) {
            searchBufLen++; // If there is still space in the buffer, increase its length
        }
        
        if (searchBufLen >= replLen) { // If the buffer could contain the bytes to replace
            boolean equals = true;
            for (int i=0; i<replLen; i++) { // Check if the contents equals the bytes searched for
                if (bytesToReplace[i] != searchBuf[getBufPtr(searchBufPtr-replLen+i)]) {
                    equals = false;
                    break;
                }
            }
            if (equals) {
                if (searchBufLen > replLen) { // Write the parts not matching the replacements
                    writeFromSearchBuf(getBufPtr(searchBufPtr-searchBufLen), searchBufLen-replLen);
                }
                out.write(replacement); // Write the replacement
                searchBufPtr=0; // The buffer is now empty
                searchBufLen=0;
            } else if (searchBufLen == searchBuf.length) { //Buffer full -> flush it
                searchBufLen = replLen - 1; // Flush only the beginning of the buffer (the end could still be a prefix of the bytes to match)
                writeFromSearchBuf(searchBufPtr, searchBuf.length - searchBufLen);
            }
        }
    }
    
    /**
     * Returns a correct pointer into the search buffer (i.e. a pointer modulo its length)
     */
    private int getBufPtr(int ptr) {
        int rv = ptr%searchBuf.length;
        if (rv < 0) {
            return searchBuf.length+rv;
        } else {
            return rv;
        }
    }
    
    /**
     * Writes a number of bytes from the search buffer to the output stream
     * taking its ring buffer characteristic in account.
     */
    private void writeFromSearchBuf(int offset, int length) throws IOException {
        offset = getBufPtr(offset);
        if (offset+length < searchBuf.length) {
            out.write(searchBuf, offset, length);
        } else {
            final int toWrite = searchBuf.length - offset;
            out.write(searchBuf, offset, toWrite);
            out.write(searchBuf, 0, length - toWrite);
        }
    }
    
    
//    public static void main(String[] args) throws IOException {
//        OutputStream outStream = new FileOutputStream("/tmp/p2.ps");
//
//        long time = System.currentTimeMillis();
//        Utils.copyStream(new FileInputStream("/tmp/p.ps"), outStream);
//        outStream.close();
//        System.out.println("Copy 1: " +  (System.currentTimeMillis()-time) + "ms");
//        
//        outStream = new ReplacerOutputStream(new FileOutputStream("/tmp/p3.ps"), 
//                "% MozillaCharsetName: iso-8859-1".getBytes(),
//                "% MozillaCharsetName: iso-8859-15".getBytes());
//        time = System.currentTimeMillis();
//        Utils.copyStream(new FileInputStream("/tmp/p.ps"), outStream);
//        outStream.close();
//        System.out.println("Copy 2: " +  (System.currentTimeMillis()-time) + "ms");
//        
//        outStream = new FileOutputStream("/tmp/p4.ps");
//        time = System.currentTimeMillis();
//        Utils.copyStream(new FileInputStream("/tmp/p.ps"), outStream);
//        outStream.close();
//        System.out.println("Copy 3: " +  (System.currentTimeMillis()-time) + "ms");
//
//    }
}
