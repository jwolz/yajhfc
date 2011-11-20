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

import java.lang.reflect.Array;
import java.util.Arrays;

public class TIFFTag {
    /**
     * Returns the length of a single element of the given data type in
     * the source file in bytes
     * @param dataType
     * @return
     */
    public static int getLengthForDatatype(int dataType) {
        switch (dataType) {
        case TIFFConstants.DATATYPE_BYTE:
        case TIFFConstants.DATATYPE_ASCII:
        case TIFFConstants.DATATYPE_SBYTE:
        case TIFFConstants.DATATYPE_UNDEFINED:
            return 1;
        case TIFFConstants.DATATYPE_SHORT:
        case TIFFConstants.DATATYPE_SSHORT:
            return 2;
        case TIFFConstants.DATATYPE_LONG:
        case TIFFConstants.DATATYPE_SLONG:
        case TIFFConstants.DATATYPE_FLOAT:
            return 4;
        case TIFFConstants.DATATYPE_RATIONAL:
        case TIFFConstants.DATATYPE_SRATIONAL:
        case TIFFConstants.DATATYPE_DOUBLE:
            return 8;
        default:
            return -1;
        }
    }
    
    /**
     * The tag's ID number
     */
    public final int ID;
    /**
     * The IFD (page) this tag was found in
     */
    public final int nIFD;
    /**
     * This tag's data type
     */
    public final int dataType;
    /**
     * This tag's value
     */
    public final Object value;
    
    
    /**
     * Returns the first value of this tag as a double (if applicable to the data type)
     * @return
     */
    public double doubleValue() {
        return doubleValue(0);
    }
    
    /**
     * Returns the nth value of this tag as a double (if applicable to the data type)
     * @return
     */
    public double doubleValue(int n) {
        switch (dataType) {
        case TIFFConstants.DATATYPE_BYTE:
        case TIFFConstants.DATATYPE_SBYTE:
        case TIFFConstants.DATATYPE_SHORT:
        case TIFFConstants.DATATYPE_SSHORT:
        case TIFFConstants.DATATYPE_LONG:
        case TIFFConstants.DATATYPE_SLONG:
        case TIFFConstants.DATATYPE_FLOAT:
        case TIFFConstants.DATATYPE_DOUBLE:
            return Array.getDouble(value, n);
        case TIFFConstants.DATATYPE_RATIONAL:
        case TIFFConstants.DATATYPE_SRATIONAL:
            double numerator   = Array.getDouble(value, 2*n);
            double denominator = Array.getDouble(value, 2*n+1);
            return (numerator / denominator);
        case TIFFConstants.DATATYPE_ASCII:
        case TIFFConstants.DATATYPE_UNDEFINED:
        default:
            throw new IllegalArgumentException("This tag's data type does not represent a double value");
        }
    }
    
    /**
     * Returns the first value of this tag as a float (if applicable to the data type)
     * @return
     */
    public float floatValue() {
        return floatValue(0);
    }
    
    /**
     * Returns the nth value of this tag as a double (if applicable to the data type)
     * @return
     */
    public float floatValue(int n) {
        return (float)doubleValue(n);
    }
    
    
    /**
     * Returns the first value of this tag as a long (if applicable to the data type)
     * @return
     */
    public long longValue() {
        return longValue(0);
    }
    
    /**
     * Returns the nth value of this tag as a long (if applicable to the data type)
     * @return
     */
    public long longValue(int n) {
        switch (dataType) {
        case TIFFConstants.DATATYPE_BYTE:
        case TIFFConstants.DATATYPE_SBYTE:
        case TIFFConstants.DATATYPE_SHORT:
        case TIFFConstants.DATATYPE_SSHORT:
        case TIFFConstants.DATATYPE_LONG:
        case TIFFConstants.DATATYPE_SLONG:
            return Array.getLong(value, n);
        case TIFFConstants.DATATYPE_FLOAT:
        case TIFFConstants.DATATYPE_DOUBLE:
        case TIFFConstants.DATATYPE_RATIONAL:
        case TIFFConstants.DATATYPE_SRATIONAL:
            return (long)doubleValue(n);
        case TIFFConstants.DATATYPE_ASCII:
        case TIFFConstants.DATATYPE_UNDEFINED:
        default:
            throw new IllegalArgumentException("This tag's data type does not represent a long value");
        }
    }
    
    /**
     * Returns the first value of this tag as a int (if applicable to the data type)
     * @return
     */
    public int intValue() {
        return intValue(0);
    }
    
    /**
     * Returns the nth value of this tag as a int (if applicable to the data type)
     * @return
     */
    public int intValue(int n) {
        return (int)longValue(n);
    }
    
    @Override
    public String toString() {
        return "{ID: " + ID + "; nIFD: " + nIFD + "; dataType: " + dataType + "; value: " + valueToString() + '}';
    }
    
    protected String valueToString() {
        if (value instanceof byte[]) {
            return Arrays.toString((byte[])value);
        } else if (value instanceof short[]) {
            return Arrays.toString((short[])value);
        } else if (value instanceof int[]) {
            return Arrays.toString((int[])value);
        } else if (value instanceof long[]) {
            return Arrays.toString((long[])value);
        } else if (value instanceof float[]) {
            return Arrays.toString((float[])value);
        } else if (value instanceof double[]) {
            return Arrays.toString((double[])value);
        } else {
            return value.toString();
        }
    }
    
    public TIFFTag(int iD, int nIFD, int dataType, Object value) {
        super();
        this.ID = iD;
        this.nIFD = nIFD;
        this.dataType = dataType;
        this.value = value;
    } 
}