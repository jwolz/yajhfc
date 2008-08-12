package yajhfc.faxcover;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

import yajhfc.FormattedFile;
import yajhfc.utils;
import yajhfc.FormattedFile.FileFormat;

// This (and FaxCoverHyla) is a port of "faxcover.c++" from the hylafax source code distribution to java
/**
 * Subclasses of this class *must* implement a constructor of the form
 * Constructor(URL coverTemplate)
 */
public abstract class Faxcover {
    public DateFormat dateFmt;    // date format for Output
    
//    public float    pageWidth;  // page width (mm)
//    public float    pageLength; // page length (mm)
    public yajhfc.PaperSize pageSize;
    
    public String   toName;     // to person's name
    public String   toFaxNumber;    // to's fax number
    public String   toVoiceNumber;  // to's voice number
    public String   toLocation; // to's geographical location
    public String   toCompany;  // to's company/institution
    public String   fromFaxNumber;  // sender's fax number
    public String   fromVoiceNumber;    // sender's voice number
    public String   fromLocation;   // sender's geographical location
    public String   fromCompany;    // sender's company/institution
    public String   fromMailAddress; // sender's mail address
    public String   regarding;  // fax is regarding...
    public String   comments;   // general comments
    public String   sender;     // sender's identity
    public int      pageCount;  // # pages, not counting cover page
    
    protected URL coverTemplate;
   
    public abstract void makeCoverSheet(OutputStream out) throws IOException; 
    
    final static byte[] PDF_Signature = "%PDF".getBytes();
    
    /**
     * Estimates the number of pages in the given PostScript or PDF stream and
     * adds the number of pages to pageCount.
     * Returns the number of pages found in the given stream.
     */
    // Original from SendFaxClient.c++ (HylaFax)
    public int estimatePostscriptPages(InputStream psFile) 
        throws IOException {
        int pages = 0;
        byte[] sig = new byte[4];
        if (psFile.read(sig) == sig.length) {
            if (sig[0] == '%' && sig[1] == '!') { // is Postscript
                BufferedReader rIn = new BufferedReader(new InputStreamReader(psFile));
                String line = null;
                int nPages = 0, nPage = 0;
                Pattern patPages = Pattern.compile("%%Pages:\\s+(\\d+)");
                
                while ((line = rIn.readLine()) != null) {
                    if (line.startsWith("%%Page:")) {
                        nPage++;
                    } else {
                        Matcher m = patPages.matcher(line);
                        if (m.matches())
                            nPages = Integer.parseInt(m.group(1));
                    }
                }
                if (nPages > 0)
                    pages = nPages;
                else
                    pages = nPage;
                rIn.close();
            } else if (Arrays.equals(sig, PDF_Signature)) {
                BufferedInputStream bIn = new BufferedInputStream(psFile);
                byte[] buf = new byte[4000];
                final byte[] search1 = "/Type".getBytes();
                final byte[] search2 = "/Page".getBytes();
                final int searchlen = search1.length + search2.length + 2;
                
                int offset = 0, len = 0;
                
                while ((len = bIn.read(buf, offset, buf.length - offset)) > 0) {
                    int slash = -1;
                    
                    len += offset; // len is max. index of buf
                    
                    offset = 0;
                    while ((slash = arrIndexOf(buf, (byte)'/', slash + 1)) >= 0) {
                        if (slash < (len - searchlen)) {
                            // Try to match the pattern "/Type\w*/page"
                            boolean found = true;
                            for (int i = 0; i < search1.length; i++) {
                                if (buf[slash+i] != search1[i]) {
                                    found = false;
                                    break;
                                }
                            }
                            if (found) {
                                int off = slash + search1.length;
                                while (Character.isWhitespace(buf[off])) {
                                    off++;
                                }
                                
                                found = true;
                                for (int i = 0; i < search2.length; i++) {
                                    if (buf[off+i] != search2[i]) {
                                        found = false;
                                        break;
                                    }
                                }
                                if (found && (buf[off + search2.length] != (byte)'s')) {
                                    pages++;
                                }
                            }
                        } else {
                            offset = len - slash;
                            if (offset > 0)
                                System.arraycopy(buf, slash, buf, 0, offset);
                            break;
                        }
                    }
                    if (offset < 0)
                        break;
                }
                bIn.close();
            }
        }        
        pageCount += pages;
        return pages;
    }
    
    
    private int arrIndexOf(byte[] arr, byte key, int offset) {
        for (int i = offset; i < arr.length; i++) {
            if (arr[i] == key)
                return i;
        }
        return -1;
    }
    
    
    protected Faxcover(URL coverTemplate) {
        //dateFmt("%a %b %d %Y, %H:%M %Z")
        dateFmt = DateFormat.getDateInstance(DateFormat.FULL, utils.getLocale());
        this.coverTemplate = coverTemplate;
    }
    
    ///////////////
    // Static part:
    
    public static final java.util.Map<FileFormat,Class<? extends Faxcover>> supportedCoverFormats = new EnumMap<FileFormat, Class<? extends Faxcover>>(FileFormat.class);
    static {
        supportedCoverFormats.put(FileFormat.PostScript, FaxcoverHyla.class);
        supportedCoverFormats.put(FileFormat.HTML, HTMLFaxcover.class);
    }
    
    protected static FileFilter[] acceptedFilters;
    public static FileFilter[] getAcceptedFileFilters() {
        if (acceptedFilters == null) {
            acceptedFilters = FormattedFile.createFileFiltersFromFormats(supportedCoverFormats.keySet());
        }
        return acceptedFilters;
    }
    
    public static Faxcover createInstanceForTemplate(File coverTemplate) throws IOException, InvalidCoverFormatException {
        URL coverURL;
        
        if (coverTemplate == null) {
            coverURL = utils.getLocalizedFile("faxcover/faxcover.ps");
            if (coverURL == null) {
                throw new IOException("Default cover page not found!");
            }
        } else {
            coverURL = coverTemplate.toURI().toURL();
        }
        
        FileFormat format = FormattedFile.detectFileFormat(coverURL.openStream());
        Class<? extends Faxcover> supportClass = supportedCoverFormats.get(format);
        if (supportClass == null) {
            throw new InvalidCoverFormatException("Unsupported cover page format!");
        } else {
            try {
                Constructor<? extends Faxcover> constructor = supportClass.getConstructor(URL.class);
                
                return constructor.newInstance(coverURL);
            } catch (Exception e) {
                throw new InvalidCoverFormatException(e);
            }
        }
    }
    
    public static class InvalidCoverFormatException extends Exception {

        public InvalidCoverFormatException() {
            super();
        }

        public InvalidCoverFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidCoverFormatException(String message) {
            super(message);
        }

        public InvalidCoverFormatException(Throwable cause) {
            super(cause);
        }
    }
    
//    // Testing code:
//    public static void main(String[] args) {
//        System.out.println("Creating cover page...");
//        Faxcover cov = new Faxcover();
//        cov.coverTemplate = new File("yajhfc/faxcover/faxcover.ps");
//        
//        cov.comments = "foo\niniun iunuini uinini ninuin iuniuniu 9889hz h897h789 bnin uibiubui ubuib uibub ubiu bib bib ib uib i \nbar";
//        cov.fromCompany = "foo Ü&Ö OHG";
//        cov.fromFaxNumber = "989898";
//        cov.fromLocation = "Bardorf";
//        cov.fromVoiceNumber = "515616";
//        
//        //cov.pageCount = 10;
//        String[] docs = { "/home/jonas/mozilla.ps", "/home/jonas/nssg.pdf" };
//        for (int i=0; i<docs.length; i++)
//            try {
//                System.out.println(docs[i] + " pages: " + cov.estimatePostscriptPages(new FileInputStream(docs[i])));
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        
//        cov.pageCount = 1;
//        cov.pageLength = 297;
//        cov.pageWidth = 210;
//        cov.regarding = "Test fax";
//        cov.sender = "Werner Meißner";
//        
//        cov.toCompany = "Bâr GmbH & Co. KGaA";
//        cov.toFaxNumber = "87878787";
//        cov.toLocation = "Foostädtle";
//        cov.toName = "Otto Müller";
//        cov.toVoiceNumber = "4545454";
//        
//        try {
//            cov.makeCoverSheet(new FileOutputStream("/tmp/test.ps"));
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
    
}
