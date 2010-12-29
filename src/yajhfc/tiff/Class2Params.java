package yajhfc.tiff;
/**
 * Class 2 params processing; ported from C++ from the HylaFAX source code
 * @author jonas
 *
 */
@SuppressWarnings("unused")
public class Class2Params {
    /**
     * vertical resolution (VR_*)
     */
    public int vr;      
    /**
     * bit rate (BR_*)
     */
    public int br;       
    /**
     * page width (WD_*)
     */
    public int wd;      
    /**
     * page length (LN_*)
     */
    public int ln;       
    /**
     * data format (DF_*)
     */
    public int df;       
    /**
     * error correction protocol (EC_*)
     */
    public int ec;       
    /**
     * binary file transfer protocol (BF_*)
     */
    public int bf;       
    /**
     * minimum scanline time (ST_*)
     */
    public int st;       
    /**
     * JPEG support (JP_*)
     */
    public int jp; 

    private static final int MAX_BITSTRING_BYTES = 16;
    public int[] m_bits = new int[MAX_BITSTRING_BYTES];


    private static final int BIT(int i) {
        return (1<<(i));
    }

    // bit ordering directives +fbor=<n>
    /**
 phase C direct
     **/
    private static final int BOR_C_DIR = 0;
    /**
 phase C reversed
     **/
    private static final int BOR_C_REV = 1;

    private static final int BOR_C = 0x1;
    private static final int BOR_BD_DIR    = (0<<1);   // phase B/D direct
    private static final int BOR_BD_REV    = (1<<1);   // phase B/D reversed
    /**
 service types returned by +fclass=?
     **/
    private static final int BOR_BD = 0x2;
    private static final int SERVICE_DATA   = BIT(0);  // data service
    private static final int SERVICE_CLASS1 = BIT(1);  // class 1 interface
    private static final int SERVICE_CLASS2 = BIT(2);  // class 2 interface
    private static final int SERVICE_CLASS20 = BIT(3); // class 2.0 interface
    private static final int SERVICE_CLASS10 = BIT(4); // class 1.0 interface
    private static final int SERVICE_CLASS21 = BIT(5); // class 2.1 interface
    private static final int SERVICE_VOICE  = BIT(8);  // voice service (ZyXEL extension)
    private static final int SERVICE_ALL    = BIT(9)-1;

    // t.30 session subparameter codes
    /**
 98 lpi
     **/
    private static final int VR_NORMAL = 0x00;
    /**
 196 lpi
     **/
    private static final int VR_FINE = 0x01;
    /**
 R8  x 15.4 l/mm
     **/
    private static final int VR_R8 = 0x02;
    /**
 R16 x 15.4 l/mm
     **/
    private static final int VR_R16 = 0x04;
    /**
 200 dpi x 100 l/25.4mm
     **/
    private static final int VR_200X100 = 0x08;
    /**
 200 dpi x 200 l/25.4mm
     **/
    private static final int VR_200X200 = 0x10;
    /**
 200 dpi x 400 l/25.4mm
     **/
    private static final int VR_200X400 = 0x20;
    /**
 300 dpi x 300 l/25.4mm
     **/
    private static final int VR_300X300 = 0x40;
    /**

     **/
    private static final int VR_ALL = 0x7F;/**
 2400 bit/s
     **/
    private static final int BR_2400 = 0;
    /**
 4800 bit/s
     **/
    private static final int BR_4800 = 1;
    /**
 7200 bit/s
     **/
    private static final int BR_7200 = 2;
    /**
 9600 bit/s
     **/
    private static final int BR_9600 = 3;
    /**
 12000 bit/s
     **/
    private static final int BR_12000 = 4;
    /**
 14400 bit/s
     **/
    private static final int BR_14400 = 5;
    /**
 16800 bit/s
     **/
    private static final int BR_16800 = 6;
    /**
 19200 bit/s
     **/
    private static final int BR_19200 = 7;
    /**
 21600 bit/s
     **/
    private static final int BR_21600 = 8;
    /**
 24000 bit/s
     **/
    private static final int BR_24000 = 9;
    /**
 26400 bit/s
     **/
    private static final int BR_26400 = 10;
    /**
 28800 bit/s
     **/
    private static final int BR_28800 = 11;
    /**
 31200 bit/s
     **/
    private static final int BR_31200 = 12;
    /**
 33600 bit/s
     **/
    private static final int BR_33600 = 13;
    private static final int BR_ALL    = BIT(BR_33600+1)-1;

    /**
 1728 pixels in 215 mm
     **/
    private static final int WD_A4 = 0;
    /**
 2048 pixels in 255 mm
     **/
    private static final int WD_B4 = 1;
    /**
 2432 pixels in 303 mm
     **/
    private static final int WD_A3 = 2;
    private static final int WD_ALL    = BIT(WD_A3+1)-1;

    /**
 A4, 297 mm
     **/
    private static final int LN_A4 = 0;
    /**
 B4, 364 mm
     **/
    private static final int LN_B4 = 1;
    /**
 Unlimited length
     **/
    private static final int LN_INF = 2;
    private static final int LN_ALL    = BIT(LN_INF+1)-1;

    /**
 XXX US Letter size (used internally)
     **/
    private static final int LN_LET = 3;

    /**
 1-D Modified Huffman
     **/
    private static final int DF_1DMH = 0;
    /**
 2-D Modified Read
     **/
    private static final int DF_2DMR = 1;
    /**
 2-D Uncompressed Mode
     **/
    private static final int DF_2DMRUNCOMP = 2;
    /**
 2-D Modified Modified Read
     **/
    private static final int DF_2DMMR = 3;
    /**
 Single-progression sequential coding (Rec. T.85)
     **/
    private static final int DF_JBIG = 4;
    private static final int DF_ALL    = BIT(DF_JBIG+1)-1 ^ BIT(DF_2DMRUNCOMP);    // no uncompressed

    /*
     * The EC definition varies between the Class 2 and Class 2.0 spec, so
     * this is a merger of both of them.
     */
    /**
 disable ECM
     **/
    private static final int EC_DISABLE = 0;
    /**
 enable T.30 Annex A, 64-byte ECM
     **/
    private static final int EC_ENABLE64 = 1;
    /**
 enable T.30 Annex A, 256-byte ECM
     **/
    private static final int EC_ENABLE256 = 2;
    /**
 enable T.30 Annex C, half duplex
     **/
    private static final int EC_ECLHALF = 3;
    /**
 enable T.30 Annex C, full duplex
     **/
    private static final int EC_ECLFULL = 4;
    private static final int EC_ALL        = BIT(EC_ECLFULL+1)-1;

    /**
 disable file transfer modes
     **/
    private static final int BF_DISABLE = 0;
    /**
 select BFT, T.434
     **/
    private static final int BF_ENABLE = 1;
    /**
 select Document Transfer Mode
     **/
    private static final int BF_DTM = 2;
    /**
 select Edifact Mode
     **/
    private static final int BF_EDI = 4;
    /**
 select Basic Transfer Mode
     **/
    private static final int BF_BTM = 8;
    /**
 select character mode T.4 Annex D
     **/
    private static final int BF_CM = 10;
    /**
 select Mixed mode, T.4 Annex E
     **/
    private static final int BF_MM = 20;
    /**
 select Processable mode, T.505
     **/
    private static final int BF_PM = 40;
    /**

     **/
    private static final int BF_ALL = 0x3;/**
 scan time/line: 0 ms/0 ms
     **/
    private static final int ST_0MS = 0;
    /**
 scan time/line: 5 ms/5 ms
     **/
    private static final int ST_5MS = 1;
    /**
 scan time/line: 10 ms/5 ms
     **/
    private static final int ST_10MS2 = 2;
    /**
 scan time/line: 10 ms/10 ms
     **/
    private static final int ST_10MS = 3;
    /**
 scan time/line: 20 ms/10 ms
     **/
    private static final int ST_20MS2 = 4;
    /**
 scan time/line: 20 ms/20 ms
     **/
    private static final int ST_20MS = 5;
    /**
 scan time/line: 40 ms/20 ms
     **/
    private static final int ST_40MS2 = 6;
    /**
 scan time/line: 40 ms/40 ms
     **/
    private static final int ST_40MS = 7;
    private static final int ST_ALL    = BIT(ST_40MS+1)-1;

    /**
 disable JPEG
     **/
    private static final int JP_NONE = 0;
    /**
 Greyscale JPEG (T.4 Annex E and T.81)
     **/
    private static final int JP_GREY = 1;
    /**
 Full-color JPEG (T.4 Annex E and T.81)
     **/
    private static final int JP_COLOR = 2;
    /**
 Enable preferred Huffman tables
     **/
    private static final int JP_HUFFMAN = 3;
    /**
 12 bits/pel/component
     **/
    private static final int JP_12BIT = 4;
    /**
 no subsampling
     **/
    private static final int JP_NOSUB = 5;
    /**
 custom illuminant
     **/
    private static final int JP_ILLUM = 6;
    /**
 custom gamut range
     **/
    private static final int JP_GAMUT = 7;
    private static final int JP_ALL    = BIT(JP_GAMUT+1)-1;

    // post page message codes
    /**
 another page next, same document
     **/
    private static final int PPM_MPS = 0;
    /**
 another document next
     **/
    private static final int PPM_EOM = 1;
    /**
 no more pages or documents
     **/
    private static final int PPM_EOP = 2;
    /**
 another page, procedure interrupt
     **/
    private static final int PPM_PRI_MPS = 4;
    /**
 another doc, procedure interrupt
     **/
    private static final int PPM_PRI_EOM = 5;
    /**
 all done, procedure interrupt
     **/
    private static final int PPM_PRI_EOP = 6;
    // Extra message codes for decodePPM() use.
    // Must not conflict with the related FCF_XXX / FCF_PRI_XXX flags from class2.h
    /**
 page actually has to be skipped
     **/
    private static final int PPH_SKIP = 251;

    // post page response codes
    /**
 page good
     **/
    private static final int PPR_MCF = 1;
    /**
 page bad, retrain requested
     **/
    private static final int PPR_RTN = 2;
    /**
 page good, retrain requested
     **/
    private static final int PPR_RTP = 3;
    /**
 page bad, interrupt requested
     **/
    private static final int PPR_PIN = 4;
    /**
 page good, interrupt requested
     **/
    private static final int PPR_PIP = 5;

    // important stream transfer codes
    // These are actual (char) recived, so thes aren't unsigned int
    /**
 transparent character escape
     **/
    private static final int DLE = 16;
    /**
 <DLE><SUB> => <DLE><DLE> for Class 2.0
     **/
    private static final int SUB = 26;
    /**
 <DLE><ETX> means end of transfer
     **/
    private static final int ETX = 3;
    /**
 start data transfer (Class 2)
     **/
    private static final int DC1 = 17;
    /**
 start data transfer (Class 2.0 and ZyXEL)
     **/
    private static final int DC2 = 18;
    /**
 abort data transfer
     **/
    private static final int CAN = 24;
    /**
 end transmission (Class 1.0)
     **/
    private static final int EOT = 4;

    /*
     * Digital Identification Signal (DIS) definitions.
     *
     * The DIS is sent from the called station to the calling station
     * to identify its capabilities.  This information may also appear
     * in a DTC frame if the line is to be turned around.
     *
     * The values given below assume a 24-bit representation for the DIS;
     * i.e. the first 3 bytes of the frame are treated as a single 24-bit
     * value.  Additional bytes of the DIS are optional and indicated by
     * a 1 in the least significant bit of the last byte.  There are currently
     * as many as 6 additional bytes that may follow the required 3-byte
     * minimum DIS frame;  we only process the first 4.
     */
    private static final int DIS_V8 = 0x040000;    // supports V.8 training
    private static final int DIS_FRAMESIZE = 0x020000;    // preferred ECM frame size indicator
    private static final int DIS_T4XMTR = 0x008000;    // T.4 sender & has docs to poll
    private static final int DIS_T4RCVR = 0x004000;    // T.4 receiver
    private static final int DIS_SIGRATE = 0x003C00;    // data signalling rate
    private static final int DISSIGRATE_V27FB = 0x0; // V.27ter fallback mode: 2400 BPS
    private static final int DISSIGRATE_V27 = 0x4; // V.27ter: 4800 + 2400 BPS
    private static final int DISSIGRATE_V29 = 0x8; // V.29: 9600 + 7200 BPS
    private static final int DISSIGRATE_V2729 = 0xC; // V.27ter+V.29
    private static final int DISSIGRATE_V33 = 0xE; // V.27ter+V.29+V.33
    private static final int DISSIGRATE_V17 = 0xD; // V.27ter+V.29+V.33+V.17
    private static final int DIS_7MMVRES = 0x000200;    // vertical resolution = 7.7 line/mm
    private static final int DIS_2DENCODE = 0x000100;    // 2-d compression supported
    private static final int DIS_PAGEWIDTH = 0x0000C0;    // recording width capabilities
    private static final int DISWIDTH_A4 = 0;   // only 215mm (A4)
    private static final int DISWIDTH_A3 = 1;   // 215, 255, and 303 (A4, B4, A3)
    private static final int DISWIDTH_B4 = 2;   // 215, and 255 (A4, B4)
    private static final int DISWIDTH_INVALID = 3;   // invalid, but treat as A3
    private static final int DIS_PAGELENGTH = 0x000030;    // max recording length capabilities
    private static final int DISLENGTH_A4 = 0;   // A4 (297 mm)
    private static final int DISLENGTH_UNLIMITED = 1;   // no max length
    private static final int DISLENGTH_A4B4 = 2;   // A4 and B4 (364 mm)
    private static final int DISLENGTH_INVALID = 3;
    private static final int DIS_MINSCAN = 0x00000E;    // receiver min scan line time
    private static final int DISMINSCAN_20MS = 0x0;
    private static final int DISMINSCAN_40MS = 0x1;
    private static final int DISMINSCAN_10MS = 0x2;
    private static final int DISMINSCAN_10MS2 = 0x3;
    private static final int DISMINSCAN_5MS = 0x4;
    private static final int DISMINSCAN_40MS2 = 0x5;
    private static final int DISMINSCAN_20MS2 = 0x6;
    private static final int DISMINSCAN_0MS = 0x7;
    private static final int DIS_XTNDFIELD = 0x000001;    // extended field indicator

    // 1st extension byte (alternative mode capabilities)
    private static final int DIS_2400HS = (0x80<<24);  // 2400 bit/s handshaking
    private static final int DIS_2DUNCOMP = (0x40<<24);  // uncompressed 2-d data supported
    private static final int DIS_ECMODE = (0x20<<24);  // error correction mode supported
    // NB: bit 0x10 must be zero
    private static final int DIS_ELMODE = (0x08<<24);  // error limiting mode suported
    private static final int DIS_G4COMP = (0x02<<24);  // T.6 compression supported
    // bit 0x01 indicates an extension byte follows

    // The meaning of the 2nd extension byte changed after the 1993 recommendation.
    // If DIS_IGNOLD is set, then the 1993 definitions can be understood, otherwise
    // the current definitions should be understood.

    // 2nd extension byte - 1993 meaning - (alternative paper width capabilities)
    private static final int DIS_IGNOLD = (0x80<<16);  // ignore old paper widths in byte 3
    private static final int DIS_1216 = (0x40<<16);  // 1216 pixels in 151 mm scanline
    private static final int DIS_864 = (0x20<<16);  // 864 pixels in 107 mm scanline
    private static final int DIS_1728L = (0x10<<16);  // 1728 pixels in 151 mm scanline
    private static final int DIS_1728H = (0x08<<16);  // 1728 pixels in 107 mm scanline
    // bits 0x04 and 0x02 are reserved
    // bit 0x01 indicates an extension byte follows

    // 2nd extension byte - current meaning
    // bit 0x80 is not valid and must be unset
    private static final int DIS_MULTSEP = (0x40<<16);  // multiple selective polling capability
    private static final int DIS_POLLSUB = (0x20<<16);  // polled subaddress
    private static final int DIS_T43 = (0x10<<16);  // T.43 coding
    private static final int DIS_INTERLV = (0x08<<16);  // plane interleave
    private static final int DIS_VOICE = (0x04<<16);  // voice coding - G.726
    private static final int DIS_VOICEXT = (0x02<<16);  // extended voice coding
    // bit 0x01 indicates an extension byte follows

    // 3rd extension byte (alternative resolution capabilities)
    private static final int DIS_200X400 = (0x80<<8);   // 200 x 400 pixels/inch resolution
    private static final int DIS_300X300 = (0x40<<8);   // 300 x 300 pixels/inch resolution
    private static final int DIS_400X400 = (0x20<<8);   // 400 x 400 pixels/inch resolution
    private static final int DIS_INCHRES = (0x10<<8);   // inch-based resolution preferred
    private static final int DIS_METRES = (0x08<<8);   // metric-based resolution preferred
    private static final int DIS_400MST2 = (0x04<<8);   // mst for 400 l/inch = 1/ 200 l/inch
    private static final int DIS_SEP = (0x02<<8);   // selective polling supported
    // bit 0x01 indicates an extension byte follows

    // 4th extension byte (enhanced features capabilities)
    private static final int DIS_SUB = (0x80<<0);   // sub-address supported (SUB frames)
    private static final int DIS_PWD = (0x40<<0);   // password supported (PWD frames)
    private static final int DIS_DATAFILE = (0x20<<0);   // can emit data file
    // bit 0x10 is reserved for facsimile service information
    private static final int DIS_BFT = (0x08<<0);   // supports Binary File Transfer (BFT)
    private static final int DIS_DTM = (0x04<<0);   // supports Document Transfer Mode (DTM)
    private static final int DIS_EDI = (0x02<<0);   // supports Edifact Transfer (EDI)
    // bit 0x01 indicates an extension byte follows

    /*
     * Digital Command Signal (DCS) definitions.
     *
     * The DCS is sent from the calling station to the called station
     * prior to the training procedure; it identifies the capabilities
     * to use for session operation.
     *
     * The values given below assume a 24-bit representation for the DCS;
     * i.e. the first 3 bytes of the frame are treated as a single 24-bit
     * value.  Additional bytes of the DCS are optional and indicated by
     * a 1 in the least significant bit of the last byte.  There are currently
     * as many as 6 additional bytes that may follow the required 3-byte
     * minimum DCS frame; we only process the first 4.
     */
    private static final int DCS_T4RCVR = 0x004000;    // receiver honors T.4
    private static final int DCS_SIGRATE = 0x003C00;    // data signalling rate
    private static final int DCSSIGRATE_2400V27 = (0x0<<10);
    private static final int DCSSIGRATE_4800V27 = (0x4<<10);
    private static final int DCSSIGRATE_9600V29 = (0x8<<10);
    private static final int DCSSIGRATE_7200V29 = (0xC<<10);
    private static final int DCSSIGRATE_14400V33 = (0x2<<10);
    private static final int DCSSIGRATE_12000V33 = (0x6<<10);
    private static final int DCSSIGRATE_14400V17 = (0x1<<10);
    private static final int DCSSIGRATE_12000V17 = (0x5<<10);
    private static final int DCSSIGRATE_9600V17 = (0x9<<10);
    private static final int DCSSIGRATE_7200V17 = (0xD<<10);
    private static final int DCS_7MMVRES = 0x000200;    // vertical resolution = 7.7 line/mm
    private static final int DCS_2DENCODE = 0x000100;    // use 2-d encoding
    private static final int DCS_PAGEWIDTH = 0x0000C0;    // recording width
    private static final int DCSWIDTH_A4 = (0<<6);
    private static final int DCSWIDTH_A3 = (1<<6);
    private static final int DCSWIDTH_B4 = (2<<6);
    private static final int DCS_PAGELENGTH = 0x000030;    // max recording length
    private static final int DCSLENGTH_A4 = (0<<4);
    private static final int DCSLENGTH_UNLIMITED = (1<<4);
    private static final int DCSLENGTH_B4 = (2<<4);
    private static final int DCS_MINSCAN = 0x00000E;    // receiver min scan line time
    private static final int DCSMINSCAN_20MS = (0x0<<1);
    private static final int DCSMINSCAN_40MS = (0x1<<1);
    private static final int DCSMINSCAN_10MS = (0x2<<1);
    private static final int DCSMINSCAN_5MS = (0x4<<1);
    private static final int DCSMINSCAN_0MS = (0x7<<1);
    private static final int DCS_XTNDFIELD = 0x000001;    // extended field indicator

    // 1st extension byte (alternative mode capabilities)
    private static final int DCS_2400HS = (0x80<<24);  // 2400 bit/s handshaking
    private static final int DCS_2DUNCOMP = (0x40<<24);  // use uncompressed 2-d data
    private static final int DCS_ECMODE = (0x20<<24);  // use error correction mode
    private static final int DCS_FRAMESIZE = (0x10<<24);  // EC frame size
    private static final int DCSFRAME_256 = (0<<28); // 256 octets
    private static final int DCSFRAME_64 = (1<<28); // 64 octets
    private static final int DCS_ELMODE = (0x08<<24);  // use error limiting mode
    // bit 0x04 is reserved for Group 4
    private static final int DCS_G4COMP = (0x02<<24);  // use T.6 compression
    // bit 0x01 indicates another information byte follows

    // 2nd extension byte (alternative paper width capabilities)
    private static final int DCS_IGNOLD = (0x80<<16);  // ignore old paper widths in byte 3
    private static final int DCS_1216 = (0x40<<16);  // use 1216 pixels in 151 mm scanline
    private static final int DCS_864 = (0x20<<16);  // use 864 pixels in 107 mm scanline
    // bits 0x10 and 0x08 are invalid
    // bits 0x04 and 0x02 are not used
    // bit 0x01 indicates another information byte follows

    // 3rd extension byte (alternative resolution capabilities)
    private static final int DCS_200X400 = (0x80<<8);   // use 200 x 400 pixels/inch resolution
    private static final int DCS_300X300 = (0x40<<8);   // use 300 x 300 pixels/inch resolution
    private static final int DCS_400X400 = (0x20<<8);   // use 400 x 400 pixels/inch resolution
    private static final int DCS_INCHRES = (0x10<<8);   // use inch-based resolution
    // bits 0x08 and 0x04 are ``don't care''
    // bit 0x02 should be zero
    // bit 0x01 indicates another information byte follows

    // 4th extension byte (enhanced features capabilities)
    // bits 0x80 and 0x40 should be zero
    // bit 0x20 is not used
    // bit 0x10 is reserved for facsimile service information
    private static final int DCS_BFT = (0x08<<0);   // use Binary File Transfer (BFT)
    private static final int DCS_DTM = (0x04<<0);   // use Document Transfer Mode (DTM)
    private static final int DCS_EDI = (0x02<<0);   // use Edifact Transfer (EDI)
    // bit 0x01 indicates another information byte follows

    private static final int BITNUM_V8_CAPABLE = 6;
    private static final int BITNUM_FRAMESIZE_DIS = 7;
    private static final int BITNUM_T4XMTR = 9;
    private static final int BITNUM_T4RCVR = 10;
    private static final int BITNUM_SIGRATE_11 = 11;
    private static final int BITNUM_SIGRATE_12 = 12;
    private static final int BITNUM_SIGRATE_13 = 13;
    private static final int BITNUM_SIGRATE_14 = 14;
    private static final int BITNUM_VR_FINE = 15;
    private static final int BITNUM_2DMR = 16;
    private static final int BITNUM_WIDTH_17 = 17;
    private static final int BITNUM_WIDTH_18 = 18;
    private static final int BITNUM_LENGTH_19 = 19;
    private static final int BITNUM_LENGTH_20 = 20;
    private static final int BITNUM_ST_21 = 21;
    private static final int BITNUM_ST_22 = 22;
    private static final int BITNUM_ST_23 = 23;
    private static final int BITNUM_ECM = 27;
    private static final int BITNUM_FRAMESIZE_DCS = 28;
    private static final int BITNUM_2DMMR = 31;
    private static final int BITNUM_JBIG = 36;
    private static final int BITNUM_VR_R8 = 41;
    private static final int BITNUM_VR_300X300 = 42;
    private static final int BITNUM_VR_R16 = 43;
    private static final int BITNUM_INCH_RES = 44;
    private static final int BITNUM_METRIC_RES = 45;
    private static final int BITNUM_SEP = 47;
    private static final int BITNUM_SUB = 49;
    private static final int BITNUM_PWD = 50;
    private static final int BITNUM_JPEG = 68;
    private static final int BITNUM_FULLCOLOR = 69;
    private static final int BITNUM_LETTER_SIZE = 76;
    private static final int BITNUM_LEGAL_SIZE = 77;
    private static final int BITNUM_JBIG_BASIC = 78;
    private static final int BITNUM_JBIG_L0 = 79;

    
    /*
     * Tables for mapping a T.30 DIS to Class 2
     * subparameter code values.
     */
    private static final int[] DISdfTab = {
        DF_1DMH,            // !DIS_2DENCODE
        DF_2DMR         // DIS_2DENCODE
    };
    private static final int[] DISvrTab = {
        VR_NORMAL,          // !DIS_7MMVRES
        VR_FINE         // DIS_7MMVRES
    };
    /*
     * Beware that this table returns the ``best speed''
     * based on the signalling capabilities of the DIS.
     */
    private static final int[] DISbrTab = {
        BR_2400,            // 0x0 V27ter fall-back (BR_4800 could be assumed, T.30 Table 2 Note 3)
        BR_14400,           // 0x1 undefined
        BR_9600,            // 0x2 undefined
        BR_14400,           // 0x3 undefined
        BR_4800,            // 0x4 V27ter
        BR_14400,           // 0x5 reserved
        BR_4800,            // 0x6 reserved
        BR_14400,           // 0x7 reserved
        BR_9600,            // 0x8 V29
        BR_14400,           // 0x9 undefined
        BR_9600,            // 0xA undefined
        BR_14400,           // 0xB undefined
        BR_9600,            // 0xC V27ter+V29
        BR_14400,           // 0xD V27ter+V29+V17 (not V.33, post-1994)
        BR_14400,           // 0xE V27ter+V29+V33 (invalid, post-1994, T.30 Table 2 Notes 31, 32)
        BR_14400,           // 0xF undefined
    };
    private static final int[] DISwdTab = {
        WD_A4,          // DISWIDTH_A4
        WD_A3,          // DISWIDTH_A3
        WD_B4           // DISWIDTH_B4
    };
    private static final int[] DISlnTab = {
        LN_A4,          // DISLENGTH_A4
        LN_INF,         // DISLENGTH_UNLIMITED
        LN_B4,          // DISLENGTH_B4
        LN_A4           // undefined
    };
    private static final int[] DISstTab = {
        ST_20MS,            // DISMINSCAN_20MS
        ST_40MS,            // DISMINSCAN_40MS
        ST_10MS,            // DISMINSCAN_10MS
        ST_10MS2,           // DISMINSCAN_10MS2
        ST_5MS,         // DISMINSCAN_5MS
        ST_40MS2,           // DISMINSCAN_40MS2
        ST_20MS2,           // DISMINSCAN_20MS2
        ST_0MS          // DISMINSCAN_0MS
    };
    private static final int[] DCSbrTab = {
        BR_2400,            // 0x0/2400 V27
        BR_14400,           // 0x1/14400 V17
        BR_14400,           // 0x2/14400 V33
        0,              // 0x3/undefined
        BR_4800,            // 0x4/4800 V27
        BR_12000,           // 0x5/12000 V17
        BR_12000,           // 0x6/12000 V33
        0,              // 0x7/undefined
        BR_9600,            // 0x8/9600 V29
        BR_9600,            // 0x9/9600 V17
        BR_9600,            // 0xA/9600 V33
        0,              // 0xB/undefined
        BR_7200,            // 0xC/7200 V29
        BR_7200,            // 0xD/7200 V17
        BR_7200,            // 0xE/7200 V33
        0,              // 0xF/undefined
    };

    public void asciiDecode(char[] dcs)
    {
        int b = 0;
        int i = 0;
        while (i+1 < dcs.length) {
            m_bits[b] = ((dcs[i+0] - (dcs[i+0] > 64 ? 55 : 48)) << 4) + (dcs[i+1] - (dcs[i+1] > 64 ? 55 : 48));
            setExtendBits(b++);
            i += 2;
            if (i < dcs.length && dcs[i] == ' ') i++;
        }
    }

    private void setExtendBits(int byteNum)
    {
        if (byteNum >= 3) {
            for (int b = byteNum-1; b >= 2; b--)
                m_bits[b] = m_bits[b] | 0x01;
        }
    }

    public void setPageWidthInPixels(int w)
    {
        /*
         * Here we attempt to determine the WD parameter with
         * a pixel width which is impossible to be perfect
         * because there are colliding values.  However,
         * since we don't use > WD_A3, this is fine.
         */
        wd = (w == 1728 ? WD_A4 :
            w == 2048 ? WD_B4 :
                w == 2432 ? WD_A3 :
                    w == 3456 ? WD_A4 :
                        w == 4096 ? WD_B4 :
                            w == 4864 ? WD_A3 :
                                w == 2592 ? WD_A4 :
                                    w == 3072 ? WD_B4 :
                                        w == 3648 ? WD_A3 :
                                            WD_A4);
    }

    public void setPageLengthInMM(int l) {
        ln = (l == -1 ?  LN_INF :
            l <= 280 ?         LN_LET :
                l <= 300 ?         LN_A4 :
                    l <= 380 ?         LN_B4 :
                        LN_INF);
    }

    public void setRes(int xres, int yres) {
        vr = (xres > 300 && yres > 391 ? VR_R16 :
            xres > 204 && yres > 250 ? VR_300X300 :
                yres > 391 ? VR_200X400 :
                    yres > 250 ? VR_R8 :
                        yres > 196 ? VR_200X200 :
                            yres > 150 ? VR_FINE :
                                yres > 98 ? VR_200X100 : VR_NORMAL);
    }

    public static char[] sanitize(char[] chars) {
        // Do nothing for the moment...
        return chars;
    }

    public int getHorizontalRes() 
    {
        /*
         * Technically horizontal resolution depends upon the
         * the number of pixels across the page and the page width.
         * But, these are just used for writing TIFF tags for
         * received faxes, so we do this to accomodate the session
         * parameters, even though it may be slightly off.
         */
        return (vr == VR_NORMAL ? 204 :
            vr == VR_FINE ? 204 :
                vr == VR_R8 ? 204 :
                    vr == VR_R16 ? 408 :
                        vr == VR_200X100 ? 200 :
                            vr == VR_200X200 ? 200 :
                                vr == VR_200X400 ? 200 :
                                    vr == VR_300X300 ? 300 :
                                        -1);
    }

    public int getVerticalRes() 
    {
        return (vr == VR_NORMAL ? 98 :
            vr == VR_FINE ? 196 :
                vr == VR_R8 ? 391 :
                    vr == VR_R16 ? 391 :
                        vr == VR_200X100 ? 100 :
                            vr == VR_200X200 ? 200 :
                                vr == VR_200X400 ? 400 :
                                    vr == VR_300X300 ? 300 :
                                        -1);
    }

    public void decodeClass2Params(long v) {
        if (v>>21 == 1) {       // check version
            vr = (int)((v>>0) & 7);  // VR is a bitmap
            br = (int)(v>>3) & 15;
            wd = (int)(v>>9) & 7;
            ln = (int)(v>>12) & 3;
            if (ln == LN_LET)   // force protocol value
                ln = LN_A4;
            df = (int)(v>>14) & 3;
            ec = (int)(v>>16) & 1;
            bf = (int)(v>>17) & 1;
            st = (int)(v>>18) & 7;
        } else {            // original version
            vr = (int)(v>>0) & 1;
            br = (int)(v>>1) & 7;
            wd = (int)(v>>4) & 7;
            ln = (int)(v>>7) & 3;
            if (ln == LN_LET)   // force protocol value
                ln = LN_A4;
            df = (int)(v>>9) & 3;
            ec = (int)(v>>11) & 1;
            bf = (int)(v>>12) & 1;
            st = (int)(v>>13) & 7;
        }
    }


    private int getByte(int idx) {
        return m_bits[idx];
    }

    /*
     * Convert a T.30 DCS to a Class 2 parameter block.
     */
    public void setFromDCS()
    {
        int dcs = 0;
        int xinfo = 0;

        dcs |= getByte(0) << 16;
        dcs |= getByte(1) << 8;
        dcs |= getByte(2) << 0;

        xinfo |= getByte(3) << 24;
        xinfo |= getByte(4) << 16;
        xinfo |= getByte(5) << 8;
        xinfo |= getByte(6) << 0;

        setFromDCS(dcs, xinfo);

        if (isBitEnabled(BITNUM_LETTER_SIZE) ||
                isBitEnabled(BITNUM_LEGAL_SIZE)) {
            // we map letter and legal onto WD_A4 + LN_INF for convenience
            wd = WD_A4;
            ln = LN_INF;
        }
        // Dex 855 sets MMR when also indicating JBIG.  We deliberately let JBIG override.
        if (isBitEnabled(BITNUM_JBIG_BASIC)) df = DF_JBIG;
        if (isBitEnabled(BITNUM_JBIG_L0)) df = DF_JBIG;
        if (isBitEnabled(BITNUM_JPEG)) jp = JP_GREY;
        if (isBitEnabled(BITNUM_FULLCOLOR)) {
            if (jp == JP_GREY) jp = JP_COLOR;
        }
        if (ec == EC_DISABLE &&
                (df == DF_2DMMR || df == DF_JBIG || jp == JP_GREY || jp == JP_COLOR)) {
            // MMR, JBIG, and JPEG require ECM... we've seen cases where fax
            // senders screw up and don't signal ECM but do send ECM-framed
            // image data in the signalled format, and an RTN will break protocol,
            // and thus a failure, so we correct the sender's mistake
            // guessing at 256-byte ECM since 64-byte is so rarely used.
            ec = EC_ENABLE256;
        }
    }

    /*
     * Convert a T.30 DCS to a Class 2 parameter block.
     */
    void setFromDCS(int dcs, int xinfo)
    {
        setFromDIS(dcs, xinfo);
        // override DIS setup
        br = DCSbrTab[(dcs & DCS_SIGRATE) >> 10];
        if ((xinfo & DCS_INCHRES) != 0) {
            if ((xinfo & DCS_400X400) != 0) vr = VR_R16;   // rather than adding a VR_400X400
            else if ((xinfo & DCS_300X300) != 0) vr = VR_300X300;
            else if ((xinfo & DCS_200X400) != 0) vr = VR_200X400;
            else if ((dcs & DCS_7MMVRES) != 0) vr = VR_200X200;
            else vr = VR_200X100;
        } else {            // bit 44 of DCS is 0
            // some manufacturers don't send DCS_INCHRES with DCS_300X300
            if ((xinfo & DCS_300X300) != 0) vr = VR_300X300;
            else if ((xinfo & DCS_400X400) != 0) vr = VR_R16;
            else if ((xinfo & DCS_200X400) != 0) vr = VR_R8;
            else vr = DISvrTab[(dcs & DCS_7MMVRES) >> 9];
        }

        // DF here is a setting, not a bitmap, max of DF_2DMMR (JPEG, JBIG set later)
        if ((df & BIT(DF_2DMMR)) != 0) df = DF_2DMMR;
        else if ((df & BIT(DF_2DMR)) != 0) df = DF_2DMR;
        else df = DF_1DMH;

        if ((xinfo & DCS_ECMODE) != 0)
            ec = ((xinfo & DCSFRAME_64) != 0) ? EC_ENABLE64 : EC_ENABLE256;
        else
            ec = EC_DISABLE;
    }

    /*
     * Convert a T.30 DIS to a Class 2 parameter block.
     */
    void setFromDIS(int dis, int xinfo)
    {
        // VR is a bitmap of available settings, not a maximum
        vr = DISvrTab[(dis & DIS_7MMVRES) >> 9];
        if ((xinfo & DIS_METRES)!= 0)  {
            if ((xinfo & DIS_200X400)!= 0) vr |= VR_R8;
            if ((xinfo & DIS_400X400)!= 0) vr |= VR_R16;
        }
        if ((xinfo & DIS_INCHRES)!= 0) {
            vr |= VR_200X100;
            if ((dis & DIS_7MMVRES)!= 0) vr |= VR_200X200;
            if ((xinfo & DIS_200X400)!= 0) vr |= VR_200X400;
        }
        if ((xinfo & DIS_300X300)!= 0) vr |= VR_300X300;
        /*
         * Beware that some modems (e.g. the Supra) indicate they
         * support the V.17 bit rates, but not the normal V.27+V.29
         * signalling rates.  The DISbrTab is NOT setup to mark the
         * V.27 and V.29 if V.17 is set.  Instead we let the upper
         * layers select appropriate signalling rate knowing that
         * we'll fall back to something that the modem will support.
         */
        if (((dis & DIS_V8)!= 0) && ((xinfo & DIS_ECMODE)!= 0))
            br = BR_33600;  // Is V.8 only used by V.34 (SuperG3) faxes?
        else
            br = DISbrTab[(dis & DIS_SIGRATE) >> 10];
        wd = DISwdTab[(dis & DIS_PAGEWIDTH) >> 6];
        ln = DISlnTab[(dis & DIS_PAGELENGTH) >> 4];

        // DF here is a bitmap
        df = BIT(DF_1DMH);      // required support for all G3 facsimile
        if (((xinfo & DIS_G4COMP)!= 0) && ((xinfo & DIS_ECMODE)!= 0))   // MMR requires ECM
            df |= BIT(DF_2DMMR);
        if ((xinfo & DIS_2DUNCOMP)!= 0)
            df |= BIT(DF_2DMRUNCOMP);
        if ((dis & DIS_2DENCODE)!= 0)
            df |= BIT(DF_2DMR);

        if ((xinfo & DIS_ECMODE)!= 0)
            ec = ((dis & DIS_FRAMESIZE)!= 0) ? EC_ENABLE64 : EC_ENABLE256;
        else
            ec = EC_DISABLE;
        bf = BF_DISABLE;            // XXX from xinfo
        st = DISstTab[(dis & DIS_MINSCAN) >> 1];
        jp = 0;
    }
    
    /*
     * Table 2 T.30  defines bit numbers 1 ... 127.
     * Anything else is invalid and should not be used.
     */
    private boolean validBitNumber(int bitNum)
    {
        return ((bitNum >= 1) && (bitNum <= 127));
    }

    private boolean  isBitEnabled(int bitNum)
    {
        if (!validBitNumber(bitNum)) return false;
        return (m_bits[calculateByteNumber(bitNum)] & calculateMask(bitNum)) != 0;
    }
    
    private int calculateByteNumber(int bitNum)
    {
        //Subtract 1 from bitNum because Table 2 T.30 indexes
        //bit numbers from 1 and C indexes everything from 0.

        return (bitNum-1)/8;
    }

    private int calculateMask(int bitNum)
    {
        //Subtract 1 from bitNum because Table 2 T.30 indexes
        //bit numbers from 1 and C indexes everything from 0.

        int shiftLeft = 7-((bitNum-1)%8);
        int mask = 0x01 << shiftLeft;
        return mask;
    }

    private static final int brRates[] = {
        2400,   // BR_2400
        4800,   // BR_4800
        7200,   // BR_7200
        9600,   // BR_9600
        12000,  // BR_12000
        14400,  // BR_14400
        16800,  // BR_16800
        19200,  // BR_19200
        21600,  // BR_21600
        24000,  // BR_24000
        26400,  // BR_26400
        28800,  // BR_28800
        31200,  // BR_31200
        33600,  // BR_33600
        14400,  // 14? XXX
        14400,  // 15? XXX
        };
    public int bitRate() 
    {
        return (brRates[br & 15]);
    }
    
    private static final String[] dataFormatNames = {
        ("1-D MH"),           // DF_1DMH
        ("2-D MR"),           // DF_2DMR
        ("2-D Uncompressed Mode"),    // DF_2DMRUNCOMP
        ("2-D MMR"),          // DF_2DMMR
        ("JBIG"),             // DF_JBIG
        ("JPEG Greyscale"),       // JP_GREY
        ("JPEG Full-Color")       // JP_COLOR
    };

    public String dataFormatName() 
    {
        int dfid = df + (jp > 0 ? jp + 4 : 0);
        return (dataFormatNames[dfid > 6 ? 0 : dfid]);
    }

    private static final int lengths[] = {
        297,        // A4 paper
        364,        // B4 paper
        -1,         // unlimited
        280,        // US letter (used internally)
        };
    
    public int pageLength() 
    {
        return (lengths[ln&3]);
    }

    
    public int pageWidth() 
    {
        int widths[] = {
                1728,   // 1728 in 215 mm line
                2048,   // 2048 in 255 mm line
                2432,   // 2432 in 303 mm line
                1216,   // 1216 in 151 mm line
                864,    // 864 in 107 mm line
                1728,   // undefined
                1728,   // undefined
                1728,   // undefined
                };
        switch (vr) {
        case VR_300X300:
            widths[0] = 2592;
            widths[1] = 3072;
            widths[2] = 3648;
            widths[3] = 1824;
            widths[4] = 1296;
            break;
        case VR_R16:
            widths[0] = 3456;
            widths[1] = 4096;
            widths[2] = 4864;
            widths[3] = 2432;
            widths[4] = 1728;
            break;
        case VR_NORMAL:
        case VR_FINE:
        case VR_R8:
        case VR_200X100:
        case VR_200X200:
        case VR_200X400:
            // nothing
            break;
        }
        return (widths[wd&7]);
    }
}
