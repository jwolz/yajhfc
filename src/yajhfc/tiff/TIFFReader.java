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
    protected final List<TIFFTag> tags = new ArrayList<TIFFTag>();
    
    public int getNumberOfPages() {
        return numberOfPages;
    }
    
    public List<TIFFTag> getTags() {
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
    
    public void read(FileInputStream inStream, boolean readTags) throws IOException {
        tags.clear();
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

                    tags.add(new TIFFTag(tag, numberOfPages, dataType, value));
                }
            } else {
                // Skip tags
                buf.position(buf.position() + numEntries * 12);
            }
            
            ifdOffset = buf.getInt();
            numberOfPages++;
        }
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
            System.out.println("-----------------------------");
            System.out.println();
        }
    }
}
