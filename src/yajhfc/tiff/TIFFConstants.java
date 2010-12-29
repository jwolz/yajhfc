package yajhfc.tiff;

public class TIFFConstants {
    /**
     * Bytes. Value is byte[]
     */
    public static final int DATATYPE_BYTE = 1;
    /**
     * ASCII text. Value is String
     */
    public static final int DATATYPE_ASCII = 2;
    /**
     * 16bit int unsigned.
     * Value is int[]
     */
    public static final int DATATYPE_SHORT = 3;
    /**
     * 32bit int unsigned.
     * Value is long[]
     */
    public static final int DATATYPE_LONG = 4;
    /**
     * Rational value, two longs unsigned.
     * Value is long[] with a length of 2*numberOfElements:
     *  value[2*i] is numerator, value[2*i+1] is denominator of the i'th element
     **/
    public static final int DATATYPE_RATIONAL = 5;
    /**
     * 8bit signed int.
     * Value is byte[]
     */
    public static final int DATATYPE_SBYTE = 6;
    /**
     * 8bit raw data.
     * Value is byte[]
     */
    public static final int DATATYPE_UNDEFINED = 7;
    /**
     * 16bit signed int.
     * Value is short[]
     */
    public static final int DATATYPE_SSHORT = 8;
    /**
     * 32bit signed int.
     * Value is int[]
     */
    public static final int DATATYPE_SLONG = 9;
    /**
     * Rational value, two slongs.
     * Value is int[] with a length of 2*numberOfElements:
     *  value[2*i] is numerator, value[2*i+1] is denominator of the i'th element
     **/
    public static final int DATATYPE_SRATIONAL = 10;
    /**
     * 4 byte IEEE float.
     * Value is float[]
     */
    public static final int DATATYPE_FLOAT = 11;
    /**
     * 8 byte IEEE float.
     * Value is double[]
     */
    public static final int DATATYPE_DOUBLE = 12;


    /**
 subfile data descriptor 
     **/
    public static final int TIFFTAG_SUBFILETYPE = 254;
    /**
 reduced resolution version 
     **/
    public static final int FILETYPE_REDUCEDIMAGE = 0x1;
    /**
 one page of many 
     **/
    public static final int FILETYPE_PAGE = 0x2;
    /**
 transparency mask 
     **/
    public static final int FILETYPE_MASK = 0x4;
    /**
 +kind of data in subfile 
     **/
    public static final int TIFFTAG_OSUBFILETYPE = 255;
    /**
 full resolution image data 
     **/
    public static final int OFILETYPE_IMAGE = 1;
    /**
 reduced size image data 
     **/
    public static final int OFILETYPE_REDUCEDIMAGE = 2;
    /**
 one page of many 
     **/
    public static final int OFILETYPE_PAGE = 3;
    /**
 image width in pixels 
     **/
    public static final int TIFFTAG_IMAGEWIDTH = 256;
    /**
 image height in pixels 
     **/
    public static final int TIFFTAG_IMAGELENGTH = 257;
    /**
 bits per channel (sample) 
     **/
    public static final int TIFFTAG_BITSPERSAMPLE = 258;
    /**
 data compression technique 
     **/
    public static final int TIFFTAG_COMPRESSION = 259;
    /**
 dump mode 
     **/
    public static final int COMPRESSION_NONE = 1;
    /**
 CCITT modified Huffman RLE 
     **/
    public static final int COMPRESSION_CCITTRLE = 2;
    /**
 CCITT Group 3 fax encoding 
     **/
    public static final int COMPRESSION_CCITTFAX3 = 3;
    /**
 CCITT T.4 (TIFF 6 name) 
     **/
    public static final int COMPRESSION_CCITT_T4 = 3;
    /**
 CCITT Group 4 fax encoding 
     **/
    public static final int COMPRESSION_CCITTFAX4 = 4;
    /**
 CCITT T.6 (TIFF 6 name) 
     **/
    public static final int COMPRESSION_CCITT_T6 = 4;
    /**
 Lempel-Ziv  & Welch 
     **/
    public static final int COMPRESSION_LZW = 5;
    /**
 !6.0 JPEG 
     **/
    public static final int COMPRESSION_OJPEG = 6;
    /**
 %JPEG DCT compression 
     **/
    public static final int COMPRESSION_JPEG = 7;
    /**
 NeXT 2-bit RLE 
     **/
    public static final int COMPRESSION_NEXT = 32766;
    /**
 #1 w/ word alignment 
     **/
    public static final int COMPRESSION_CCITTRLEW = 32771;
    /**
 Macintosh RLE 
     **/
    public static final int COMPRESSION_PACKBITS = 32773;
    /**
 ThunderScan RLE 
     **/
    public static final int COMPRESSION_THUNDERSCAN = 32809;
    /* codes 32895-32898 are reserved for ANSI IT8 TIFF/IT <dkelly@apago.com) */
    /**
 IT8 CT w/padding 
     **/
    public static final int COMPRESSION_IT8CTPAD = 32895;
    /**
 IT8 Linework RLE 
     **/
    public static final int COMPRESSION_IT8LW = 32896;
    /**
 IT8 Monochrome picture 
     **/
    public static final int COMPRESSION_IT8MP = 32897;
    /**
 IT8 Binary line art 
     **/
    public static final int COMPRESSION_IT8BL = 32898;
    /* compression codes 32908-32911 are reserved for Pixar */
    /**
 Pixar companded 10bit LZW 
     **/
    public static final int COMPRESSION_PIXARFILM = 32908;
    /**
 Pixar companded 11bit ZIP 
     **/
    public static final int COMPRESSION_PIXARLOG = 32909;
    /**
 Deflate compression 
     **/
    public static final int COMPRESSION_DEFLATE = 32946;
    /**
 Deflate compression,
                               as recognized by Adobe 
     **/
    public static final int COMPRESSION_ADOBE_DEFLATE = 8;
    /* compression code 32947 is reserved for Oceana Matrix <dev@oceana.com> */
    /**
 Kodak DCS encoding 
     **/
    public static final int COMPRESSION_DCS = 32947;
    /**
 ISO JBIG 
     **/
    public static final int COMPRESSION_JBIG = 34661;
    /**
 SGI Log Luminance RLE 
     **/
    public static final int COMPRESSION_SGILOG = 34676;
    /**
 SGI Log 24-bit packed 
     **/
    public static final int COMPRESSION_SGILOG24 = 34677;
    /**
 Leadtools JPEG2000 
     **/
    public static final int COMPRESSION_JP2000 = 34712;
    /**
 photometric interpretation 
     **/
    public static final int TIFFTAG_PHOTOMETRIC = 262;
    /**
 min value is white 
     **/
    public static final int PHOTOMETRIC_MINISWHITE = 0;
    /**
 min value is black 
     **/
    public static final int PHOTOMETRIC_MINISBLACK = 1;
    /**
 RGB color model 
     **/
    public static final int PHOTOMETRIC_RGB = 2;
    /**
 color map indexed 
     **/
    public static final int PHOTOMETRIC_PALETTE = 3;
    /**
 $holdout mask 
     **/
    public static final int PHOTOMETRIC_MASK = 4;
    /**
 !color separations 
     **/
    public static final int PHOTOMETRIC_SEPARATED = 5;
    /**
 !CCIR 601 
     **/
    public static final int PHOTOMETRIC_YCBCR = 6;
    /**
 !1976 CIE L*a*b* 
     **/
    public static final int PHOTOMETRIC_CIELAB = 8;
    /**
 ICC L*a*b* [Adobe TIFF Technote 4] 
     **/
    public static final int PHOTOMETRIC_ICCLAB = 9;
    /**
 ITU L*a*b* 
     **/
    public static final int PHOTOMETRIC_ITULAB = 10;
    /**
 CIE Log2(L) 
     **/
    public static final int PHOTOMETRIC_LOGL = 32844;
    /**
 CIE Log2(L) (u',v') 
     **/
    public static final int PHOTOMETRIC_LOGLUV = 32845;
    /**
 +thresholding used on data 
     **/
    public static final int TIFFTAG_THRESHHOLDING = 263;
    /**
 b&w art scan 
     **/
    public static final int THRESHHOLD_BILEVEL = 1;
    /**
 or dithered scan 
     **/
    public static final int THRESHHOLD_HALFTONE = 2;
    /**
 usually floyd-steinberg 
     **/
    public static final int THRESHHOLD_ERRORDIFFUSE = 3;
    /**
 +dithering matrix width 
     **/
    public static final int TIFFTAG_CELLWIDTH = 264;
    /**
 +dithering matrix height 
     **/
    public static final int TIFFTAG_CELLLENGTH = 265;
    /**
 data order within a byte 
     **/
    public static final int TIFFTAG_FILLORDER = 266;
    /**
 most significant -> least 
     **/
    public static final int FILLORDER_MSB2LSB = 1;
    /**
 least significant -> most 
     **/
    public static final int FILLORDER_LSB2MSB = 2;
    /**
 name of doc. image is from 
     **/
    public static final int TIFFTAG_DOCUMENTNAME = 269;
    /**
 info about image 
     **/
    public static final int TIFFTAG_IMAGEDESCRIPTION = 270;
    /**
 scanner manufacturer name 
     **/
    public static final int TIFFTAG_MAKE = 271;
    /**
 scanner model name/number 
     **/
    public static final int TIFFTAG_MODEL = 272;
    /**
 offsets to data strips 
     **/
    public static final int TIFFTAG_STRIPOFFSETS = 273;
    /**
 +image orientation 
     **/
    public static final int TIFFTAG_ORIENTATION = 274;
    /**
 row 0 top, col 0 lhs 
     **/
    public static final int ORIENTATION_TOPLEFT = 1;
    /**
 row 0 top, col 0 rhs 
     **/
    public static final int ORIENTATION_TOPRIGHT = 2;
    /**
 row 0 bottom, col 0 rhs 
     **/
    public static final int ORIENTATION_BOTRIGHT = 3;
    /**
 row 0 bottom, col 0 lhs 
     **/
    public static final int ORIENTATION_BOTLEFT = 4;
    /**
 row 0 lhs, col 0 top 
     **/
    public static final int ORIENTATION_LEFTTOP = 5;
    /**
 row 0 rhs, col 0 top 
     **/
    public static final int ORIENTATION_RIGHTTOP = 6;
    /**
 row 0 rhs, col 0 bottom 
     **/
    public static final int ORIENTATION_RIGHTBOT = 7;
    /**
 row 0 lhs, col 0 bottom 
     **/
    public static final int ORIENTATION_LEFTBOT = 8;
    /**
 samples per pixel 
     **/
    public static final int TIFFTAG_SAMPLESPERPIXEL = 277;
    /**
 rows per strip of data 
     **/
    public static final int TIFFTAG_ROWSPERSTRIP = 278;
    /**
 bytes counts for strips 
     **/
    public static final int TIFFTAG_STRIPBYTECOUNTS = 279;
    /**
 +minimum sample value 
     **/
    public static final int TIFFTAG_MINSAMPLEVALUE = 280;
    /**
 +maximum sample value 
     **/
    public static final int TIFFTAG_MAXSAMPLEVALUE = 281;
    /**
 pixels/resolution in x 
     **/
    public static final int TIFFTAG_XRESOLUTION = 282;
    /**
 pixels/resolution in y 
     **/
    public static final int TIFFTAG_YRESOLUTION = 283;
    /**
 storage organization 
     **/
    public static final int TIFFTAG_PLANARCONFIG = 284;
    /**
 single image plane 
     **/
    public static final int PLANARCONFIG_CONTIG = 1;
    /**
 separate planes of data 
     **/
    public static final int PLANARCONFIG_SEPARATE = 2;
    /**
 page name image is from 
     **/
    public static final int TIFFTAG_PAGENAME = 285;
    /**
 x page offset of image lhs 
     **/
    public static final int TIFFTAG_XPOSITION = 286;
    /**
 y page offset of image lhs 
     **/
    public static final int TIFFTAG_YPOSITION = 287;
    /**
 +byte offset to free block 
     **/
    public static final int TIFFTAG_FREEOFFSETS = 288;
    /**
 +sizes of free blocks 
     **/
    public static final int TIFFTAG_FREEBYTECOUNTS = 289;
    /**
 $gray scale curve accuracy 
     **/
    public static final int TIFFTAG_GRAYRESPONSEUNIT = 290;
    /**
 tenths of a unit 
     **/
    public static final int GRAYRESPONSEUNIT_10S = 1;
    /**
 hundredths of a unit 
     **/
    public static final int GRAYRESPONSEUNIT_100S = 2;
    /**
 thousandths of a unit 
     **/
    public static final int GRAYRESPONSEUNIT_1000S = 3;
    /**
 ten-thousandths of a unit 
     **/
    public static final int GRAYRESPONSEUNIT_10000S = 4;
    /**
 hundred-thousandths 
     **/
    public static final int GRAYRESPONSEUNIT_100000S = 5;
    /**
 $gray scale response curve 
     **/
    public static final int TIFFTAG_GRAYRESPONSECURVE = 291;
    /**
 32 flag bits 
     **/
    public static final int TIFFTAG_GROUP3OPTIONS = 292;
    /**
 TIFF 6.0 proper name alias 
     **/
    public static final int TIFFTAG_T4OPTIONS = 292;
    /**
 2-dimensional coding 
     **/
    public static final int GROUP3OPT_2DENCODING = 0x1;
    /**
 data not compressed 
     **/
    public static final int GROUP3OPT_UNCOMPRESSED = 0x2;
    /**
 fill to byte boundary 
     **/
    public static final int GROUP3OPT_FILLBITS = 0x4;
    /**
 32 flag bits 
     **/
    public static final int TIFFTAG_GROUP4OPTIONS = 293;
    /**
 TIFF 6.0 proper name 
     **/
    public static final int TIFFTAG_T6OPTIONS = 293;
    /**
 data not compressed 
     **/
    public static final int GROUP4OPT_UNCOMPRESSED = 0x2;
    /**
 units of resolutions 
     **/
    public static final int TIFFTAG_RESOLUTIONUNIT = 296;
    /**
 no meaningful units 
     **/
    public static final int RESUNIT_NONE = 1;
    /**
 english 
     **/
    public static final int RESUNIT_INCH = 2;
    /**
 metric 
     **/
    public static final int RESUNIT_CENTIMETER = 3;
    /**
 page numbers of multi-page 
     **/
    public static final int TIFFTAG_PAGENUMBER = 297;
    /**
 $color curve accuracy 
     **/
    public static final int TIFFTAG_COLORRESPONSEUNIT = 300;
    /**
 tenths of a unit 
     **/
    public static final int COLORRESPONSEUNIT_10S = 1;
    /**
 hundredths of a unit 
     **/
    public static final int COLORRESPONSEUNIT_100S = 2;
    /**
 thousandths of a unit 
     **/
    public static final int COLORRESPONSEUNIT_1000S = 3;
    /**
 ten-thousandths of a unit 
     **/
    public static final int COLORRESPONSEUNIT_10000S = 4;
    /**
 hundred-thousandths 
     **/
    public static final int COLORRESPONSEUNIT_100000S = 5;
    /**
 !colorimetry info 
     **/
    public static final int TIFFTAG_TRANSFERFUNCTION = 301;
    /**
 name & release 
     **/
    public static final int TIFFTAG_SOFTWARE = 305;
    /**
 creation date and time 
     **/
    public static final int TIFFTAG_DATETIME = 306;
    /**
 creator of image 
     **/
    public static final int TIFFTAG_ARTIST = 315;
    /**
 machine where created 
     **/
    public static final int TIFFTAG_HOSTCOMPUTER = 316;
    /**
 prediction scheme w/ LZW 
     **/
    public static final int TIFFTAG_PREDICTOR = 317;
    /**
 no prediction scheme used 
     **/
    public static final int PREDICTOR_NONE = 1;
    /**
 horizontal differencing 
     **/
    public static final int PREDICTOR_HORIZONTAL = 2;
    /**
 floating point predictor 
     **/
    public static final int PREDICTOR_FLOATINGPOINT = 3;
    /**
 image white point 
     **/
    public static final int TIFFTAG_WHITEPOINT = 318;
    /**
 !primary chromaticities 
     **/
    public static final int TIFFTAG_PRIMARYCHROMATICITIES = 319;
    /**
 RGB map for pallette image 
     **/
    public static final int TIFFTAG_COLORMAP = 320;
    /**
 !highlight+shadow info 
     **/
    public static final int TIFFTAG_HALFTONEHINTS = 321;
    /**
 !tile width in pixels 
     **/
    public static final int TIFFTAG_TILEWIDTH = 322;
    /**
 !tile height in pixels 
     **/
    public static final int TIFFTAG_TILELENGTH = 323;
    /**
 !offsets to data tiles 
     **/
    public static final int TIFFTAG_TILEOFFSETS = 324;
    /**
 !byte counts for tiles 
     **/
    public static final int TIFFTAG_TILEBYTECOUNTS = 325;
    /**
 lines w/ wrong pixel count 
     **/
    public static final int TIFFTAG_BADFAXLINES = 326;
    /**
 regenerated line info 
     **/
    public static final int TIFFTAG_CLEANFAXDATA = 327;
    /**
 no errors detected 
     **/
    public static final int CLEANFAXDATA_CLEAN = 0;
    /**
 receiver regenerated lines 
     **/
    public static final int CLEANFAXDATA_REGENERATED = 1;
    /**
 uncorrected errors exist 
     **/
    public static final int CLEANFAXDATA_UNCLEAN = 2;
    /**
 max consecutive bad lines 
     **/
    public static final int TIFFTAG_CONSECUTIVEBADFAXLINES = 328;
    /**
 subimage descriptors 
     **/
    public static final int TIFFTAG_SUBIFD = 330;
    /**
 !inks in separated image 
     **/
    public static final int TIFFTAG_INKSET = 332;
    /**
 !cyan-magenta-yellow-black color 
     **/
    public static final int INKSET_CMYK = 1;
    /**
 !multi-ink or hi-fi color 
     **/
    public static final int INKSET_MULTIINK = 2;
    /**
 !ascii names of inks 
     **/
    public static final int TIFFTAG_INKNAMES = 333;
    /**
 !number of inks 
     **/
    public static final int TIFFTAG_NUMBEROFINKS = 334;
    /**
 !0% and 100% dot codes 
     **/
    public static final int TIFFTAG_DOTRANGE = 336;
    /**
 !separation target 
     **/
    public static final int TIFFTAG_TARGETPRINTER = 337;
    /**
 !info about extra samples 
     **/
    public static final int TIFFTAG_EXTRASAMPLES = 338;
    /**
 !unspecified data 
     **/
    public static final int EXTRASAMPLE_UNSPECIFIED = 0;
    /**
 !associated alpha data 
     **/
    public static final int EXTRASAMPLE_ASSOCALPHA = 1;
    /**
 !unassociated alpha data 
     **/
    public static final int EXTRASAMPLE_UNASSALPHA = 2;
    /**
 !data sample format 
     **/
    public static final int TIFFTAG_SAMPLEFORMAT = 339;
    /**
 !unsigned integer data 
     **/
    public static final int SAMPLEFORMAT_UINT = 1;
    /**
 !signed integer data 
     **/
    public static final int SAMPLEFORMAT_INT = 2;
    /**
 !IEEE floating point data 
     **/
    public static final int SAMPLEFORMAT_IEEEFP = 3;
    /**
 !untyped data 
     **/
    public static final int SAMPLEFORMAT_VOID = 4;
    /**
 !complex signed int 
     **/
    public static final int SAMPLEFORMAT_COMPLEXINT = 5;
    /**
 !complex ieee floating 
     **/
    public static final int SAMPLEFORMAT_COMPLEXIEEEFP = 6;
    /**
 !variable MinSampleValue 
     **/
    public static final int TIFFTAG_SMINSAMPLEVALUE = 340;
    /**
 !variable MaxSampleValue 
     **/
    public static final int TIFFTAG_SMAXSAMPLEVALUE = 341;
    /**
 %ClipPath
                               [Adobe TIFF technote 2] 
     **/
    public static final int TIFFTAG_CLIPPATH = 343;
    /**
 %XClipPathUnits
                               [Adobe TIFF technote 2] 
     **/
    public static final int TIFFTAG_XCLIPPATHUNITS = 344;
    /**
 %YClipPathUnits
                               [Adobe TIFF technote 2] 
     **/
    public static final int TIFFTAG_YCLIPPATHUNITS = 345;
    /**
 %Indexed
                               [Adobe TIFF Technote 3] 
     **/
    public static final int TIFFTAG_INDEXED = 346;
    /**
 %JPEG table stream 
     **/
    public static final int TIFFTAG_JPEGTABLES = 347;
    /**
 %OPI Proxy [Adobe TIFF technote] 
     **/
    public static final int TIFFTAG_OPIPROXY = 351;
    /*
     * Tags 512-521 are obsoleted by Technical Note #2 which specifies a
     * revised JPEG-in-TIFF scheme.
     */
    /**
 !JPEG processing algorithm 
     **/
    public static final int TIFFTAG_JPEGPROC = 512;
    /**
 !baseline sequential 
     **/
    public static final int JPEGPROC_BASELINE = 1;
    /**
 !Huffman coded lossless 
     **/
    public static final int JPEGPROC_LOSSLESS = 14;
    /**
 !pointer to SOI marker 
     **/
    public static final int TIFFTAG_JPEGIFOFFSET = 513;
    /**
 !JFIF stream length 
     **/
    public static final int TIFFTAG_JPEGIFBYTECOUNT = 514;
    /**
 !restart interval length 
     **/
    public static final int TIFFTAG_JPEGRESTARTINTERVAL = 515;
    /**
 !lossless proc predictor 
     **/
    public static final int TIFFTAG_JPEGLOSSLESSPREDICTORS = 517;
    /**
 !lossless point transform 
     **/
    public static final int TIFFTAG_JPEGPOINTTRANSFORM = 518;
    /**
 !Q matrice offsets 
     **/
    public static final int TIFFTAG_JPEGQTABLES = 519;
    /**
 !DCT table offsets 
     **/
    public static final int TIFFTAG_JPEGDCTABLES = 520;
    /**
 !AC coefficient offsets 
     **/
    public static final int TIFFTAG_JPEGACTABLES = 521;
    /**
 !RGB -> YCbCr transform 
     **/
    public static final int TIFFTAG_YCBCRCOEFFICIENTS = 529;
    /**
 !YCbCr subsampling factors 
     **/
    public static final int TIFFTAG_YCBCRSUBSAMPLING = 530;
    /**
 !subsample positioning 
     **/
    public static final int TIFFTAG_YCBCRPOSITIONING = 531;
    /**
 !as in PostScript Level 2 
     **/
    public static final int YCBCRPOSITION_CENTERED = 1;
    /**
 !as in CCIR 601-1 
     **/
    public static final int YCBCRPOSITION_COSITED = 2;
    /**
 !colorimetry info 
     **/
    public static final int TIFFTAG_REFERENCEBLACKWHITE = 532;
    /**
 %XML packet
                               [Adobe XMP Specification,
                               January 2004 
     **/
    public static final int TIFFTAG_XMLPACKET = 700;
    /**
 %OPI ImageID
                               [Adobe TIFF technote] 
     **/
    public static final int TIFFTAG_OPIIMAGEID = 32781;
    /* tags 32952-32956 are private tags registered to Island Graphics */
    /**
 image reference points 
     **/
    public static final int TIFFTAG_REFPTS = 32953;
    /**
 region-xform tack point 
     **/
    public static final int TIFFTAG_REGIONTACKPOINT = 32954;
    /**
 warp quadrilateral 
     **/
    public static final int TIFFTAG_REGIONWARPCORNERS = 32955;
    /**
 affine transformation mat 
     **/
    public static final int TIFFTAG_REGIONAFFINE = 32956;
    /* tags 32995-32999 are private tags registered to SGI */
    /**
 $use ExtraSamples 
     **/
    public static final int TIFFTAG_MATTEING = 32995;
    /**
 $use SampleFormat 
     **/
    public static final int TIFFTAG_DATATYPE = 32996;
    /**
 z depth of image 
     **/
    public static final int TIFFTAG_IMAGEDEPTH = 32997;
    /**
 z depth/data tile 
     **/
    public static final int TIFFTAG_TILEDEPTH = 32998;
    /* tags 33300-33309 are private tags registered to Pixar */
    /*
     * TIFFTAG_PIXAR_IMAGEFULLWIDTH and TIFFTAG_PIXAR_IMAGEFULLLENGTH
     * are set when an image has been cropped out of a larger image.  
     * They reflect the size of the original uncropped image.
     * The TIFFTAG_XPOSITION and TIFFTAG_YPOSITION can be used
     * to determine the position of the smaller image in the larger one.
     */
    /**
 full image size in x 
     **/
    public static final int TIFFTAG_PIXAR_IMAGEFULLWIDTH = 33300;
    /**
 full image size in y 
     **/
    public static final int TIFFTAG_PIXAR_IMAGEFULLLENGTH = 33301;
    /* Tags 33302-33306 are used to identify special image modes and data
     * used by Pixar's texture formats.
     */
    /**
 texture map format 
     **/
    public static final int TIFFTAG_PIXAR_TEXTUREFORMAT = 33302;
    /**
 s & t wrap modes 
     **/
    public static final int TIFFTAG_PIXAR_WRAPMODES = 33303;
    /**
 cotan(fov) for env. maps 
     **/
    public static final int TIFFTAG_PIXAR_FOVCOT = 33304;
    /**

     **/
    public static final int TIFFTAG_PIXAR_MATRIX_WORLDTOSCREEN = 33305;/**
 tag 33405 is a private tag registered to Eastman Kodak 
     **/
    public static final int TIFFTAG_PIXAR_MATRIX_WORLDTOCAMERA = 33306;
    /**
 device serial number 
     **/
    public static final int TIFFTAG_WRITERSERIALNUMBER = 33405;
    /* tag 33432 is listed in the 6.0 spec w/ unknown ownership */
    /**
 copyright string 
     **/
    public static final int TIFFTAG_COPYRIGHT = 33432;
    /* IPTC TAG from RichTIFF specifications */
    /**
 34016-34029 are reserved for ANSI IT8 TIFF/IT <dkelly@apago.com) 
     **/
    public static final int TIFFTAG_RICHTIFFIPTC = 33723;
    /**
 site name 
     **/
    public static final int TIFFTAG_IT8SITE = 34016;
    /**
 color seq. [RGB,CMYK,etc] 
     **/
    public static final int TIFFTAG_IT8COLORSEQUENCE = 34017;
    /**
 DDES Header 
     **/
    public static final int TIFFTAG_IT8HEADER = 34018;
    /**
 raster scanline padding 
     **/
    public static final int TIFFTAG_IT8RASTERPADDING = 34019;
    /**
 # of bits in short run 
     **/
    public static final int TIFFTAG_IT8BITSPERRUNLENGTH = 34020;
    /**
 # of bits in long run 
     **/
    public static final int TIFFTAG_IT8BITSPEREXTENDEDRUNLENGTH = 34021;
    /**
 LW colortable 
     **/
    public static final int TIFFTAG_IT8COLORTABLE = 34022;
    /**
 BP/BL image color switch 
     **/
    public static final int TIFFTAG_IT8IMAGECOLORINDICATOR = 34023;
    /**
 BP/BL bg color switch 
     **/
    public static final int TIFFTAG_IT8BKGCOLORINDICATOR = 34024;
    /**
 BP/BL image color value 
     **/
    public static final int TIFFTAG_IT8IMAGECOLORVALUE = 34025;
    /**
 BP/BL bg color value 
     **/
    public static final int TIFFTAG_IT8BKGCOLORVALUE = 34026;
    /**
 MP pixel intensity value 
     **/
    public static final int TIFFTAG_IT8PIXELINTENSITYRANGE = 34027;
    /**
 HC transparency switch 
     **/
    public static final int TIFFTAG_IT8TRANSPARENCYINDICATOR = 34028;
    /**
 color character. table 
     **/
    public static final int TIFFTAG_IT8COLORCHARACTERIZATION = 34029;
    /**
 HC usage indicator 
     **/
    public static final int TIFFTAG_IT8HCUSAGE = 34030;
    /**
 Trapping indicator
                               (untrapped=0, trapped=1) 
     **/
    public static final int TIFFTAG_IT8TRAPINDICATOR = 34031;
    /**
 CMYK color equivalents 
     **/
    public static final int TIFFTAG_IT8CMYKEQUIVALENT = 34032;
    /* tags 34232-34236 are private tags registered to Texas Instruments */
    /**
 Sequence Frame Count 
     **/
    public static final int TIFFTAG_FRAMECOUNT = 34232;
    /* tag 34377 is private tag registered to Adobe for PhotoShop */
    /**
 tags 34665, 34853 and 40965 are documented in EXIF specification 
     **/
    public static final int TIFFTAG_PHOTOSHOP = 34377;
    /**
 Pointer to EXIF private directory 
     **/
    public static final int TIFFTAG_EXIFIFD = 34665;
    /* tag 34750 is a private tag registered to Adobe? */
    /**
 ICC profile data 
     **/
    public static final int TIFFTAG_ICCPROFILE = 34675;
    /* tag 34750 is a private tag registered to Pixel Magic */
    /**
 JBIG options 
     **/
    public static final int TIFFTAG_JBIGOPTIONS = 34750;
    /**
 Pointer to GPS private directory 
     **/
    public static final int TIFFTAG_GPSIFD = 34853;
    /* tags 34908-34914 are private tags registered to SGI */
    /**
 encoded Class 2 ses. parms 
     **/
    public static final int TIFFTAG_FAXRECVPARAMS = 34908;
    /**
 received SubAddr string 
     **/
    public static final int TIFFTAG_FAXSUBADDRESS = 34909;
    /**
 receive time (secs) 
     **/
    public static final int TIFFTAG_FAXRECVTIME = 34910;
    /**
 encoded fax ses. params, Table 2/T.30 
     **/
    public static final int TIFFTAG_FAXDCS = 34911;
    /* tags 37439-37443 are registered to SGI <gregl@sgi.com> */
    /**
 Sample value to Nits 
     **/
    public static final int TIFFTAG_STONITS = 37439;
    /* tag 34929 is a private tag registered to FedEx */
    /**
 unknown use 
     **/
    public static final int TIFFTAG_FEDEX_EDR = 34929;
    /**
 Pointer to Interoperability private directory 
     **/
    public static final int TIFFTAG_INTEROPERABILITYIFD = 40965;
    /* Adobe Digital Negative (DNG) format tags */
    /**
 &DNG version number 
     **/
    public static final int TIFFTAG_DNGVERSION = 50706;
    /**
 &DNG compatibility version 
     **/
    public static final int TIFFTAG_DNGBACKWARDVERSION = 50707;
    /**
 &name for the camera model 
     **/
    public static final int TIFFTAG_UNIQUECAMERAMODEL = 50708;
    /**
 &localized camera model
                               name 
     **/
    public static final int TIFFTAG_LOCALIZEDCAMERAMODEL = 50709;
    /**
 &CFAPattern->LinearRaw space
                               mapping 
     **/
    public static final int TIFFTAG_CFAPLANECOLOR = 50710;
    /**
 &spatial layout of the CFA 
     **/
    public static final int TIFFTAG_CFALAYOUT = 50711;
    /**
 &lookup table description 
     **/
    public static final int TIFFTAG_LINEARIZATIONTABLE = 50712;
    /**
 &repeat pattern size for
                               the BlackLevel tag 
     **/
    public static final int TIFFTAG_BLACKLEVELREPEATDIM = 50713;
    /**
 &zero light encoding level 
     **/
    public static final int TIFFTAG_BLACKLEVEL = 50714;
    /**
 &zero light encoding level
                               differences (columns) 
     **/
    public static final int TIFFTAG_BLACKLEVELDELTAH = 50715;
    /**
 &zero light encoding level
                               differences (rows) 
     **/
    public static final int TIFFTAG_BLACKLEVELDELTAV = 50716;
    /**
 &fully saturated encoding
                               level 
     **/
    public static final int TIFFTAG_WHITELEVEL = 50717;
    /**
 &default scale factors 
     **/
    public static final int TIFFTAG_DEFAULTSCALE = 50718;
    /**
 &origin of the final image
                               area 
     **/
    public static final int TIFFTAG_DEFAULTCROPORIGIN = 50719;
    /**
 &size of the final image 
                               area 
     **/
    public static final int TIFFTAG_DEFAULTCROPSIZE = 50720;
    /**
 &XYZ->reference color space
                               transformation matrix 1 
     **/
    public static final int TIFFTAG_COLORMATRIX1 = 50721;
    /**
 &XYZ->reference color space
                               transformation matrix 2 
     **/
    public static final int TIFFTAG_COLORMATRIX2 = 50722;
    /**
 &calibration matrix 1 
     **/
    public static final int TIFFTAG_CAMERACALIBRATION1 = 50723;
    /**
 &calibration matrix 2 
     **/
    public static final int TIFFTAG_CAMERACALIBRATION2 = 50724;
    /**
 &dimensionality reduction
                               matrix 1 
     **/
    public static final int TIFFTAG_REDUCTIONMATRIX1 = 50725;
    /**
 &dimensionality reduction
                               matrix 2 
     **/
    public static final int TIFFTAG_REDUCTIONMATRIX2 = 50726;
    /**
 &gain applied the stored raw
                               values
     **/
    public static final int TIFFTAG_ANALOGBALANCE = 50727;
    /**
 &selected white balance in
                               linear reference space 
     **/
    public static final int TIFFTAG_ASSHOTNEUTRAL = 50728;
    /**
 &selected white balance in
                               x-y chromaticity
                               coordinates 
     **/
    public static final int TIFFTAG_ASSHOTWHITEXY = 50729;
    /**
 &how much to move the zero
                               point 
     **/
    public static final int TIFFTAG_BASELINEEXPOSURE = 50730;
    /**
 &relative noise level 
     **/
    public static final int TIFFTAG_BASELINENOISE = 50731;
    /**
 &relative amount of
                               sharpening 
     **/
    public static final int TIFFTAG_BASELINESHARPNESS = 50732;
    /**
 &how closely the values of
                               the green pixels in the
                               blue/green rows track the
                               values of the green pixels
                               in the red/green rows 
     **/
    public static final int TIFFTAG_BAYERGREENSPLIT = 50733;
    /**
 &non-linear encoding range 
     **/
    public static final int TIFFTAG_LINEARRESPONSELIMIT = 50734;
    /**
 &camera's serial number 
     **/
    public static final int TIFFTAG_CAMERASERIALNUMBER = 50735;
    /**
 info about the lens 
     **/
    public static final int TIFFTAG_LENSINFO = 50736;
    /**
 &chroma blur radius 
     **/
    public static final int TIFFTAG_CHROMABLURRADIUS = 50737;
    /**
 &relative strength of the
                               camera's anti-alias filter 
     **/
    public static final int TIFFTAG_ANTIALIASSTRENGTH = 50738;
    /**
 &used by Adobe Camera Raw 
     **/
    public static final int TIFFTAG_SHADOWSCALE = 50739;
    /**
 &manufacturer's private data 
     **/
    public static final int TIFFTAG_DNGPRIVATEDATA = 50740;
    /**
 &whether the EXIF MakerNote
                               tag is safe to preserve
                               along with the rest of the
                               EXIF data 
     **/
    public static final int TIFFTAG_MAKERNOTESAFETY = 50741;
    /**
 &illuminant 1 
     **/
    public static final int TIFFTAG_CALIBRATIONILLUMINANT1 = 50778;
    /**
 &illuminant 2 
     **/
    public static final int TIFFTAG_CALIBRATIONILLUMINANT2 = 50779;
    /**
 &best quality multiplier 
     **/
    public static final int TIFFTAG_BESTQUALITYSCALE = 50780;
    /**
 &unique identifier for
                               the raw image data 
     **/
    public static final int TIFFTAG_RAWDATAUNIQUEID = 50781;
    /**
 &file name of the original
                               raw file 
     **/
    public static final int TIFFTAG_ORIGINALRAWFILENAME = 50827;
    /**
 &contents of the original
                               raw file 
     **/
    public static final int TIFFTAG_ORIGINALRAWFILEDATA = 50828;
    /**
 &active (non-masked) pixels
                               of the sensor 
     **/
    public static final int TIFFTAG_ACTIVEAREA = 50829;
    /**
 &list of coordinates
                               of fully masked pixels 
     **/
    public static final int TIFFTAG_MASKEDAREAS = 50830;
    /**
 &these two tags used to 
     **/
    public static final int TIFFTAG_ASSHOTICCPROFILE = 50831;
    /**
 map cameras's color space
                               into ICC profile space 
     **/
    public static final int TIFFTAG_ASSHOTPREPROFILEMATRIX = 50832;
    /**
 & 
     **/
    public static final int TIFFTAG_CURRENTICCPROFILE = 50833;
    /**
 & 
     **/
    public static final int TIFFTAG_CURRENTPREPROFILEMATRIX = 50834;
    /* tag 65535 is an undefined tag used by Eastman Kodak */
    /**
 hue shift correction data 
     **/
    public static final int TIFFTAG_DCSHUESHIFTVALUES = 65535;

    /*
     * The following are ``pseudo tags'' that can be used to control
     * codec-specific functionality.  These tags are not written to file.
     * Note that these values start at 0xffff+1 so that they'll never
     * collide with Aldus-assigned tags.
     *
     * If you want your private pseudo tags ``registered'' (i.e. added to
     * this file), please post a bug report via the tracking system at
     * http://www.remotesensing.org/libtiff/bugs.html with the appropriate
     * C definitions to add.
     */
    /**
 Group 3/4 format control 
     **/
    public static final int TIFFTAG_FAXMODE = 65536;
    /**
 default, include RTC 
     **/
    public static final int FAXMODE_CLASSIC = 0x0000;
    /**
 no RTC at end of data 
     **/
    public static final int FAXMODE_NORTC = 0x0001;
    /**
 no EOL code at end of row 
     **/
    public static final int FAXMODE_NOEOL = 0x0002;
    /**
 byte align row 
     **/
    public static final int FAXMODE_BYTEALIGN = 0x0004;
    /**
 word align row 
     **/
    public static final int FAXMODE_WORDALIGN = 0x0008;
    /**
 TIFF Class F 
     **/
    public static final int FAXMODE_CLASSF = FAXMODE_NORTC;
    /**
 Compression quality level 
     **/
    public static final int TIFFTAG_JPEGQUALITY = 65537;
    /* Note: quality level is on the IJG 0-100 scale.  Default value is 75 */
    /**
 Auto RGB<=>YCbCr convert? 
     **/
    public static final int TIFFTAG_JPEGCOLORMODE = 65538;
    /**
 no conversion (default) 
     **/
    public static final int JPEGCOLORMODE_RAW = 0x0000;
    /**
 do auto conversion 
     **/
    public static final int JPEGCOLORMODE_RGB = 0x0001;
    /**
 What to put in JPEGTables 
     **/
    public static final int TIFFTAG_JPEGTABLESMODE = 65539;
    /**
 include quantization tbls 
     **/
    public static final int JPEGTABLESMODE_QUANT = 0x0001;
    /**
 include Huffman tbls 
     **/
    public static final int JPEGTABLESMODE_HUFF = 0x0002;
    /* Note: default is JPEGTABLESMODE_QUANT | JPEGTABLESMODE_HUFF */
    /**
 G3/G4 fill function 
     **/
    public static final int TIFFTAG_FAXFILLFUNC = 65540;
    /**
 PixarLogCodec I/O data sz 
     **/
    public static final int TIFFTAG_PIXARLOGDATAFMT = 65549;
    /**
 regular u_char samples 
     **/
    public static final int PIXARLOGDATAFMT_8BIT = 0;
    /**
 ABGR-order u_chars 
     **/
    public static final int PIXARLOGDATAFMT_8BITABGR = 1;
    /**
 11-bit log-encoded (raw) 
     **/
    public static final int PIXARLOGDATAFMT_11BITLOG = 2;
    /**
 as per PICIO (1.0==2048) 
     **/
    public static final int PIXARLOGDATAFMT_12BITPICIO = 3;
    /**
 signed short samples 
     **/
    public static final int PIXARLOGDATAFMT_16BIT = 4;
    /**
 IEEE float samples 
     **/
    public static final int PIXARLOGDATAFMT_FLOAT = 5;
    /* 65550-65556 are allocated to Oceana Matrix <dev@oceana.com> */
    /**
 imager model & filter 
     **/
    public static final int TIFFTAG_DCSIMAGERTYPE = 65550;
    /**
 M3 chip (1280 x 1024) 
     **/
    public static final int DCSIMAGERMODEL_M3 = 0;
    /**
 M5 chip (1536 x 1024) 
     **/
    public static final int DCSIMAGERMODEL_M5 = 1;
    /**
 M6 chip (3072 x 2048) 
     **/
    public static final int DCSIMAGERMODEL_M6 = 2;
    /**
 infrared filter 
     **/
    public static final int DCSIMAGERFILTER_IR = 0;
    /**
 monochrome filter 
     **/
    public static final int DCSIMAGERFILTER_MONO = 1;
    /**
 color filter array 
     **/
    public static final int DCSIMAGERFILTER_CFA = 2;
    /**
 other filter 
     **/
    public static final int DCSIMAGERFILTER_OTHER = 3;
    /**
 interpolation mode 
     **/
    public static final int TIFFTAG_DCSINTERPMODE = 65551;
    /**
 whole image, default 
     **/
    public static final int DCSINTERPMODE_NORMAL = 0x0;
    /**
 preview of image (384x256) 
     **/
    public static final int DCSINTERPMODE_PREVIEW = 0x1;
    /**
 color balance values 
     **/
    public static final int TIFFTAG_DCSBALANCEARRAY = 65552;
    /**
 color correction values 
     **/
    public static final int TIFFTAG_DCSCORRECTMATRIX = 65553;
    /**
 gamma value 
     **/
    public static final int TIFFTAG_DCSGAMMA = 65554;
    /**
 toe & shoulder points 
     **/
    public static final int TIFFTAG_DCSTOESHOULDERPTS = 65555;
    /**
 calibration file desc 
     **/
    public static final int TIFFTAG_DCSCALIBRATIONFD = 65556;
    /* Note: quality level is on the ZLIB 1-9 scale. Default value is -1 */
    /**
 compression quality level 
     **/
    public static final int TIFFTAG_ZIPQUALITY = 65557;
    /**
 PixarLog uses same scale 
     **/
    public static final int TIFFTAG_PIXARLOGQUALITY = 65558;
    /* 65559 is allocated to Oceana Matrix <dev@oceana.com> */
    /**
 area of image to acquire 
     **/
    public static final int TIFFTAG_DCSCLIPRECTANGLE = 65559;
    /**
 SGILog user data format 
     **/
    public static final int TIFFTAG_SGILOGDATAFMT = 65560;
    /**
 IEEE float samples 
     **/
    public static final int SGILOGDATAFMT_FLOAT = 0;
    /**
 16-bit samples 
     **/
    public static final int SGILOGDATAFMT_16BIT = 1;
    /**
 uninterpreted data 
     **/
    public static final int SGILOGDATAFMT_RAW = 2;
    /**
 8-bit RGB monitor values 
     **/
    public static final int SGILOGDATAFMT_8BIT = 3;
    /**
 SGILog data encoding control
     **/
    public static final int TIFFTAG_SGILOGENCODE = 65561;
    /**
 do not dither encoded values
     **/
    public static final int SGILOGENCODE_NODITHER = 0;
    /**
 randomly dither encd values 
     **/
    public static final int SGILOGENCODE_RANDITHER = 1;

    /*
     * EXIF tags
     */
    /**
 Exposure time 
     **/
    public static final int EXIFTAG_EXPOSURETIME = 33434;
    /**
 F number 
     **/
    public static final int EXIFTAG_FNUMBER = 33437;
    /**
 Exposure program 
     **/
    public static final int EXIFTAG_EXPOSUREPROGRAM = 34850;
    /**
 Spectral sensitivity 
     **/
    public static final int EXIFTAG_SPECTRALSENSITIVITY = 34852;
    /**
 ISO speed rating 
     **/
    public static final int EXIFTAG_ISOSPEEDRATINGS = 34855;
    /**
 Optoelectric conversion
                               factor 
     **/
    public static final int EXIFTAG_OECF = 34856;
    /**
 Exif version 
     **/
    public static final int EXIFTAG_EXIFVERSION = 36864;
    /**
 Date and time of original
                               data generation 
     **/
    public static final int EXIFTAG_DATETIMEORIGINAL = 36867;
    /**
 Date and time of digital
                               data generation 
     **/
    public static final int EXIFTAG_DATETIMEDIGITIZED = 36868;
    /**
 Meaning of each component 
     **/
    public static final int EXIFTAG_COMPONENTSCONFIGURATION = 37121;
    /**
 Image compression mode 
     **/
    public static final int EXIFTAG_COMPRESSEDBITSPERPIXEL = 37122;
    /**
 Shutter speed 
     **/
    public static final int EXIFTAG_SHUTTERSPEEDVALUE = 37377;
    /**
 Aperture 
     **/
    public static final int EXIFTAG_APERTUREVALUE = 37378;
    /**
 Brightness 
     **/
    public static final int EXIFTAG_BRIGHTNESSVALUE = 37379;
    /**
 Exposure bias 
     **/
    public static final int EXIFTAG_EXPOSUREBIASVALUE = 37380;
    /**
 Maximum lens aperture 
     **/
    public static final int EXIFTAG_MAXAPERTUREVALUE = 37381;
    /**
 Subject distance 
     **/
    public static final int EXIFTAG_SUBJECTDISTANCE = 37382;
    /**
 Metering mode 
     **/
    public static final int EXIFTAG_METERINGMODE = 37383;
    /**
 Light source 
     **/
    public static final int EXIFTAG_LIGHTSOURCE = 37384;
    /**
 Flash 
     **/
    public static final int EXIFTAG_FLASH = 37385;
    /**
 Lens focal length 
     **/
    public static final int EXIFTAG_FOCALLENGTH = 37386;
    /**
 Subject area 
     **/
    public static final int EXIFTAG_SUBJECTAREA = 37396;
    /**
 Manufacturer notes 
     **/
    public static final int EXIFTAG_MAKERNOTE = 37500;
    /**
 User comments 
     **/
    public static final int EXIFTAG_USERCOMMENT = 37510;
    /**
 DateTime subseconds 
     **/
    public static final int EXIFTAG_SUBSECTIME = 37520;
    /**
 DateTimeOriginal subseconds 
     **/
    public static final int EXIFTAG_SUBSECTIMEORIGINAL = 37521;
    /**
 DateTimeDigitized subseconds 
     **/
    public static final int EXIFTAG_SUBSECTIMEDIGITIZED = 37522;
    /**
 Supported Flashpix version 
     **/
    public static final int EXIFTAG_FLASHPIXVERSION = 40960;
    /**
 Color space information 
     **/
    public static final int EXIFTAG_COLORSPACE = 40961;
    /**
 Valid image width 
     **/
    public static final int EXIFTAG_PIXELXDIMENSION = 40962;
    /**
 Valid image height 
     **/
    public static final int EXIFTAG_PIXELYDIMENSION = 40963;
    /**
 Related audio file 
     **/
    public static final int EXIFTAG_RELATEDSOUNDFILE = 40964;
    /**
 Flash energy 
     **/
    public static final int EXIFTAG_FLASHENERGY = 41483;
    /**
 Spatial frequency response 
     **/
    public static final int EXIFTAG_SPATIALFREQUENCYRESPONSE = 41484;
    /**
 Focal plane X resolution 
     **/
    public static final int EXIFTAG_FOCALPLANEXRESOLUTION = 41486;
    /**
 Focal plane Y resolution 
     **/
    public static final int EXIFTAG_FOCALPLANEYRESOLUTION = 41487;
    /**
 Focal plane resolution unit 
     **/
    public static final int EXIFTAG_FOCALPLANERESOLUTIONUNIT = 41488;
    /**
 Subject location 
     **/
    public static final int EXIFTAG_SUBJECTLOCATION = 41492;
    /**
 Exposure index 
     **/
    public static final int EXIFTAG_EXPOSUREINDEX = 41493;
    /**
 Sensing method 
     **/
    public static final int EXIFTAG_SENSINGMETHOD = 41495;
    /**
 File source 
     **/
    public static final int EXIFTAG_FILESOURCE = 41728;
    /**
 Scene type 
     **/
    public static final int EXIFTAG_SCENETYPE = 41729;
    /**
 CFA pattern 
     **/
    public static final int EXIFTAG_CFAPATTERN = 41730;
    /**
 Custom image processing 
     **/
    public static final int EXIFTAG_CUSTOMRENDERED = 41985;
    /**
 Exposure mode 
     **/
    public static final int EXIFTAG_EXPOSUREMODE = 41986;
    /**
 White balance 
     **/
    public static final int EXIFTAG_WHITEBALANCE = 41987;
    /**
 Digital zoom ratio 
     **/
    public static final int EXIFTAG_DIGITALZOOMRATIO = 41988;
    /**
 Focal length in 35 mm film 
     **/
    public static final int EXIFTAG_FOCALLENGTHIN35MMFILM = 41989;
    /**
 Scene capture type 
     **/
    public static final int EXIFTAG_SCENECAPTURETYPE = 41990;
    /**
 Gain control 
     **/
    public static final int EXIFTAG_GAINCONTROL = 41991;
    /**
 Contrast 
     **/
    public static final int EXIFTAG_CONTRAST = 41992;
    /**
 Saturation 
     **/
    public static final int EXIFTAG_SATURATION = 41993;
    /**
 Sharpness 
     **/
    public static final int EXIFTAG_SHARPNESS = 41994;
    /**
 Device settings description 
     **/
    public static final int EXIFTAG_DEVICESETTINGDESCRIPTION = 41995;
    /**
 Subject distance range 
     **/
    public static final int EXIFTAG_SUBJECTDISTANCERANGE = 41996;

    /**
 Unique image ID 
     **/
    public static final int EXIFTAG_IMAGEUNIQUEID = 42016;


    private TIFFConstants() {};

}
