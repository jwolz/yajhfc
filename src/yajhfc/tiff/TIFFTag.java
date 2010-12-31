/**
 * 
 */
package yajhfc.tiff;

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