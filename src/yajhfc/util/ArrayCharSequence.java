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
package yajhfc.util;

import java.io.IOException;
import java.io.Reader;


/**
 * @author jonas
 *
 */
public class ArrayCharSequence implements CharSequence {
    protected final char[] wrapped;
    protected final int offset;
    protected final int length;

    /* (non-Javadoc)
     * @see java.lang.CharSequence#length()
     */
    public int length() {
        return length;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#charAt(int)
     */
    public char charAt(int index) {
        if (index >= length)
            throw new ArrayIndexOutOfBoundsException(index);
        return wrapped[offset+index];
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    public CharSequence subSequence(int start, int end) {
        return new ArrayCharSequence(wrapped, start, end-start);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof CharSequence))
            return false;
        CharSequence csq = (CharSequence)obj;
        if (csq.length() != length)
            return false;
        
        for (int i=0; i<length; i++) {
            if (charAt(i) != csq.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int rv = 1;
        for (int i=offset; i<offset+length; i++) {
            rv = 31 * rv + wrapped[i]; // Copied from Arrays.hashCode(char[])
        }
        return rv;
    }
    
    @Override
    public String toString() {
        return new String(wrapped, offset, length);
    }
    
    public int getLength() {
        return length;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public char[] getArray() {
        return wrapped;
    }
    
    /**
     * Reads the content of the given reader completely into a CharSequence.
     * Avoids copying as far as possible.
     * @param in
     * @return
     * @throws IOException
     */
    public static ArrayCharSequence readCompletely(Reader in) throws IOException {
        char[] buf = new char[4000];
        int offset = 0;
        int numRead;
        
        while ((numRead = in.read(buf, offset, buf.length - offset)) >= 0) {
            offset += numRead;
            if (offset >= buf.length) { // Array too small -> make larger
                char[] newBuf = new char[buf.length*2];
                System.arraycopy(buf, 0, newBuf, 0, buf.length);
                buf = newBuf;
            }
        }
        
        return new ArrayCharSequence(buf, 0, offset);
    }

    public ArrayCharSequence(char[] wrapped) {
        this(wrapped, 0, wrapped.length);
    }

    public ArrayCharSequence(char[] wrapped, int offset, int length) {
        super();
        this.wrapped = wrapped;
        this.offset = offset;
        this.length = length;
    }

}
