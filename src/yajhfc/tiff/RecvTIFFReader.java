package yajhfc.tiff;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;

/**
 * Reads information about a fax from the TIFF file.
 * Parts are ported from the HylaFAX source code.
 * 
 * Information avaible using rcvfmt not supported here: 
 *  f (file name), 
 *  m (protection), q (protection),
 *  n (file size),
 *  o (owner (using GID)),
 *  
 * @author jonas
 *
 */
public class RecvTIFFReader extends TIFFReader {
    private static final Logger log = Logger.getLogger(TIFFReader.class.getName());
    
    protected Class2Params params;
    
    protected String sender;
    protected List<String> callid;
    protected String subaddr;
    protected Date date;
    protected String strDate;
    protected int recvTime;
    protected boolean inProgress;
    
    @Override
    public void read(FileInputStream inStream, boolean readTags) {
        try {
            super.read(inStream, true);
        } catch (IOException e) {
            log.log(Level.INFO, "Exception reading TIFF, assuming it's in progress: " + inStream, e);
            inProgress = true;
            return;
        }
//        FileChannel channel = inStream.getChannel();
//        
//        try {
//            FileLock lock = channel.tryLock();
//            lock.release();
//            inProgress = false;
//        } catch (IOException e) {
//            log.log(Level.INFO, "Exception getting lock, assuming it's in progress for " + inStream , e);
//            inProgress = true;
//        }
        
        params = new Class2Params();
        
        long[] v = (long[])getTagValue(TIFFConstants.TIFFTAG_FAXRECVPARAMS);
        if (v != null) {
            params.decodeClass2Params(v[0]);
        } else {
            float vres = 3.85f;                  // XXX default
            float[] pvres = (float[]) getTagValue(TIFFConstants.TIFFTAG_YRESOLUTION);
            if (pvres != null) {
                vres = pvres[0];

                int resunit = TIFFConstants.RESUNIT_INCH;          // TIFF spec default
                int[] presunit = (int[])getTagValue(TIFFConstants.TIFFTAG_RESOLUTIONUNIT);
                if (presunit != null) {
                    resunit = presunit[0];
                }
                if (resunit == TIFFConstants.RESUNIT_INCH)
                    vres /= 25.4;
                if (resunit == TIFFConstants.RESUNIT_NONE)
                    vres /= 720.0;              // postscript units ?
            }
            float hres = 8.03f;                  // XXX default
            float[] phres = (float[]) getTagValue(TIFFConstants.TIFFTAG_XRESOLUTION);
            if (phres != null) {
                hres = phres[0];
                int resunit = TIFFConstants.RESUNIT_INCH;          // TIFF spec default
                int[] presunit = (int[])getTagValue(TIFFConstants.TIFFTAG_RESOLUTIONUNIT);
                if (presunit != null) {
                    resunit = presunit[0];
                }
                if (resunit == TIFFConstants.RESUNIT_INCH)
                    hres /= 25.4;
                if (resunit == TIFFConstants.RESUNIT_NONE)
                    hres /= 720.0;              // postscript units ?
            }
            params.setRes((int) hres, (int) vres);   // resolution
            v = (long[]) getTagValue(TIFFConstants.TIFFTAG_IMAGEWIDTH);
            if (v != null) {
                params.setPageWidthInPixels((int)v[0]);
            }
            v = (long[]) getTagValue(TIFFConstants.TIFFTAG_IMAGELENGTH);
            if (v != null) {
                params.setPageLengthInMM((int)(v[0] / vres));
            }
        }
        
        String faxDCS = (String)getTagValue(TIFFConstants.TIFFTAG_FAXDCS);
        if (faxDCS != null && !faxDCS.equals("00 00 00")) {
            // cannot trust br from faxdcs as V.34-Fax does not provide it there
            int brhold = params.br;
            
            params.asciiDecode(Class2Params.sanitize(faxDCS.toCharArray()));    // params per Table 2/T.30
            params.setFromDCS();
            params.br = brhold;
        }
    
        sender = "";
        callid = Collections.emptyList();
        String imageDesc = (String)getTagValue(TIFFConstants.TIFFTAG_IMAGEDESCRIPTION);
        if (imageDesc != null) {
            List<String> l = Utils.fastSplitToList(imageDesc, '\n');
            if (l.size() >= 1) {
                sender = l.get(0);
                if (l.size() >= 2) {
                    callid = l.subList(1, l.size());
                }
            }
        } else
            sender = "<unknown>";
        
        String subAddr = (String)getTagValue(TIFFConstants.TIFFTAG_FAXSUBADDRESS);
        if (subAddr != null) {
            subaddr = subAddr;
        } else
            subaddr = "";
        
        strDate = (String)getTagValue(TIFFConstants.TIFFTAG_DATETIME);
        if (strDate != null) {
            try {
                date = Utils.HYLA_LONG_DATE_FORMAT.parse(strDate);
            } catch (ParseException e) {
                log.log(Level.INFO, "Error parsing date " + strDate, e);
                date = null;
            }
        } else {
            date = null;
        }
        recvTime = 0;           // page count
        
        for (TIFFTag tag : tags) {
            if (tag.ID == TIFFConstants.TIFFTAG_FAXRECVTIME) {
                recvTime += ((long[])tag.value)[0];
            }
        }
    }

    
    private Object getTagValue(int id) {
        for (TIFFTag tag : tags) {
            if (tag.ID == id) {
                return tag.value;
            }
        }
        return null;
    }
    

    /**
     * Return the sub address (format a)
     * @return
     */
    public String getSubAddress() {
        return subaddr;
    }
    
    /**
     * Return the bit rate (format b)
     * @return
     */
    public int getBitRate() {
        return params.bitRate();
    }
    
    /**
     * Return the data format name (format d)
     * @return
     */
    public String getDataFormatName() {
        return params.dataFormatName();
    }

    /**
     * Return the reason (format e)
     * @return
     */
    public String getReason() {
        //TODO: Check if available (probably not, this is the same implementation as in HylaFAX...)
        return "";
    }
    
    /**
     * Return the recv time in seconds (~ format h)
     * @return
     */
    public int getRecvTime() {
        return recvTime;
    }
    
    private static final int CallID_NUMBER = 0;
    private static final int CallID_NAME = 1;
    
    /**
     * Returns the number portion of the caller ID (format j)
     * @return
     */
    public String getCallIDNumber() {
        if (callid.size() > CallID_NUMBER) {
            return callid.get(CallID_NUMBER);
        } else {
            return "";
        }
    }
    
    /**
     * Returns the name portion of the caller ID (format i)
     * @return
     */
    public String getCallIDName() {
        if (callid.size() > CallID_NAME) {
            return callid.get(CallID_NAME);
        } else {
            return "";
        }
    }
    
    /**
     * Returns the vertical resolution (format r)
     */
    public int getVerticalRes() {
        return params.getVerticalRes();
    }
    
    /**
     * Returns the sender (format s)
     */
    public String getSender() {
        return sender;
    }
    
    /**
     * Returns the page width (format w)
     */
    public int getPageWidth() {
        return params.pageWidth();
    }
    
    /**
     * Returns the page length (format l)
     * @return
     */
    public int getPageLength() {
        return params.pageLength();
    }
    
    /**
     * Returns if this fax is in progress (format z)
     * @return
     */
    public boolean isInProgress() {
        return inProgress;
    }
    
    /**
     * Returns the receive date 
     * (formats Y, Z, t)
     * NOTE: HylaFAX uses the mtime here instead...
     * @return
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * Returns the original, unparsed date string
     * @return
     */
    public String getStrDate() {
        return strDate;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: TIFFReader file1 ...");
        }
        RecvTIFFReader r = new RecvTIFFReader();
        for (String file : args) {
            System.out.println("Reading " + file + ": ");
            FileInputStream inStream = new FileInputStream(file);
            r.read(inStream, true);
            inStream.close();
            
            System.out.println("Number of pages: \t" + r.getNumberOfPages());
            System.out.println("Bit rate: \t" + r.getBitRate());
            System.out.println("Call ID name: \t" + r.getCallIDName());
            System.out.println("Call ID number: \t" + r.getCallIDNumber());
            System.out.println("Data format: \t" + r.getDataFormatName());
            System.out.println("Receive Date: \t" + r.getDate());
            System.out.println("Page width: \t" + r.getPageWidth());
            System.out.println("Error reason: \t" + r.getReason());
            System.out.println("Time to receive: \t" + r.getRecvTime());
            System.out.println("Sender: \t" + r.getSender());
            System.out.println("SubAddress: \t" + r.getSubAddress());
            System.out.println("Vertical resolution: \t" + r.getVerticalRes());
            
            System.out.println("-----------------------------");
            System.out.println();
        }
    }
}
