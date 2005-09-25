package yajhfc.faxcover;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.utils;

// This is a port of "faxcover.c++" from the hylafax source code distribution to java
public class Faxcover {
    public File     coverTemplate;      // prototype cover sheet
    public DateFormat dateFmt;    // date format for Output
    public int      maxcomments;    // max # of comment lines
    public int      maxlencomments; // max length of comment lines
    
    public float    pageWidth;  // page width (mm)
    public float    pageLength; // page length (mm)
    
    public String   toName;     // to person's name
    public String   toFaxNumber;    // to's fax number
    public String   toVoiceNumber;  // to's voice number
    public String   toLocation; // to's geographical location
    public String   toCompany;  // to's company/institution
    public String   fromFaxNumber;  // sender's fax number
    public String   fromVoiceNumber;    // sender's voice number
    public String   fromLocation;   // sender's geographical location
    public String   fromCompany;    // sender's company/institution
    public String   regarding;  // fax is regarding...
    public String   comments;   // general comments
    public String   sender;     // sender's identity
    public int      pageCount;  // # pages, not counting cover page

    
    private BufferedWriter wOut = null;
    
    final static String prologue =
    "/wordbreak ( ) def\n" +
    "/linebreak (\\n) def\n" +
    "/doLine {\n" +
    "% <line> <width> <height> <x> <y> doLine <width> <height> <x> <y>\n" +
    "    2 copy moveto 5 -1 roll\n" +
    "    wordbreak\n" +
    "    {\n" +
    "        search {\n" +
    "            dup stringwidth pop currentpoint pop add 7 index 6 index add gt {\n" +
    "                6 3 roll 2 index sub 2 copy moveto 6 3 roll\n" +
    "            } if\n" +
    "            show wordbreak show\n" +
    "        }{\n" +
    "            dup stringwidth pop currentpoint pop add 5 index 4 index add gt {\n" +
    "                3 1 roll 3 index sub 2 copy moveto 3 -1 roll\n" +
    "            } if\n" +
    "            show exit\n" +
    "        } ifelse\n" +
    "    } loop\n" +
    "    2 index sub 2 copy moveto\n" +
    "} def\n" +
    "/BreakIntoLines {\n" +
    "% <width> <height> <x> <y> <text> BreakIntoLines\n" +
    "    linebreak\n" +
    "    {\n" +
    "         search {\n" +
    "             7 3 roll doLine 6 -2 roll\n" +
    "         }{\n" +
    "             5 1 roll doLine exit\n" +
    "         } ifelse\n" +
    "    } loop\n" +
    "    pop pop pop pop\n" +
    "} def\n" +
    "/BreakIntoCommentX {\n" +
    "% <maxlines> <text> BreakIntoCommentX -\n" +
    "    /cbuf (Comment ) def\n" +
    "    0 exch\n" +
    "    linebreak { search { 4 -1 roll 1 add 4 2 roll }{ exch 1 add exit } ifelse } loop\n" +
    "    dup dup 2 add 1 roll\n" +
    "    -1 1 { cbuf exch 7 exch 48 add put cbuf cvn exch def } for\n" +
    "    1 add exch 1 exch { cbuf exch 7 exch 48 add put cbuf cvn () def } for\n" +
    "} def\n" +
    "/XtoCommentsX {\n" +
    "% <X> XtoCommentsX <commentsX>\n" +
    "    3 string cvs (comments) dup length dup 4 1 roll\n" +
    "    2 index length add string dup 0 4 -1 roll\n" +
    "    putinterval dup 4 -2 roll putinterval\n" +
    "} def\n" +
    "/BreakIntoCommentsX {\n" +
    "% <maxlines> <text> BreakIntoCommentsX -\n" +
    "    exch 1 1 3 2 roll\n" +
    "    { XtoCommentsX cvn () def } for\n" +
    "    dup length string copy 0 1 index 0 4 1 roll\n" +
    "    {   linebreak 0 get eq {\n" +
    "            exch dup 0 3 index getinterval 4 -1 roll 1 add dup 5 1 roll\n" +
    "            XtoCommentsX cvn exch def dup length 2 index sub 1 sub\n" +
    "            2 index 1 add exch getinterval exch pop 0\n" +
    "        }{ 1 add } ifelse\n" +
    "        dup MaxLenComments gt {\n" +
    "            exch MaxLenComments 1 sub -1 0 {\n" +
    "                2 copy get wordbreak 0 get eq {\n" +
    "                    mark 4 1 roll\n" +
    "                    {   2 copy 1 add 1 index length 1 index 1 add sub\n" +
    "                        getinterval 5 -1 roll search { 3 -2 roll pop pop } if\n" +
    "                        length MaxLenComments gt { 4 -1 roll exec\n" +
    "                        }{ false } ifelse\n" +
    "                    }\n" +
    "                    { true }\n" +
    "                    5 1 roll linebreak 1 index wordbreak 7 3 roll exec\n" +
    "                    counttomark 1 add 4 roll cleartomark { pop exit } if\n" +
    "                    2 copy 1 add 0 exch getinterval 5 -1 roll\n" +
    "                    1 add dup 6 1 roll XtoCommentsX cvn exch def\n" +
    "                    2 copy 1 add 1 index length 1 index sub getinterval\n" +
    "                    3 -1 roll pop 3 -2 roll 1 add sub exch exit\n" +
    "                } if\n" +
    "                pop\n" +
    "            } for\n" +
    "            exch dup MaxLenComments gt {\n" +
    "                pop dup 0 MaxLenComments getinterval 3 -1 roll\n" +
    "                1 add dup 4 1 roll XtoCommentsX cvn exch def\n" +
    "                dup length MaxLenComments sub\n" +
    "                MaxLenComments exch getinterval 1\n" +
    "            } if\n" +
    "        }if\n" +
    "    } forall\n" +
    "    pop exch 1 add XtoCommentsX cvn exch def\n" +
    "} def\n";
    
    public void setPageSize(Dimension size) {
        pageWidth = size.width;
        pageLength = size.height;
    }
    
    public void makeCoverSheet(OutputStream out) 
        throws IOException {
        wOut = new BufferedWriter(new OutputStreamWriter(out, "ISO8859-1"));
        
        InputStream coverIS;
        if (coverTemplate == null) {
            // Try to find a locale specific cover page
            final String prefix = "faxcover";
            final String suffix = ".ps";
            Locale loc = utils.getLocale();
            
            String[] tryList = {
                    "_" + loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant(),
                    "_" + loc.getLanguage() + "_" + loc.getCountry(),
                    "_" + loc.getLanguage(),
                    ""
            };
            URL coverURL = null;
            for (int i = 0; i < tryList.length; i++) {
                coverURL = Faxcover.class.getResource(prefix + tryList[i] + suffix);
                if (coverURL != null)
                    break;
            }
            if (coverURL == null) {
               throw new IOException("Default cover page not found!");
            }
            coverIS = coverURL.openStream();
        } else
            coverIS = new FileInputStream(coverTemplate);
        BufferedReader rCover = new BufferedReader(new InputStreamReader(coverIS, "ISO8859-1"));
        
        wOut.write("%!PS-Adobe-2.0 EPSF-2.0\n");
        wOut.write("%%Creator: faxcover\n");
        wOut.write("%%Title: HylaFAX Cover Sheet\n");
        wOut.write("%%CreationDate: " + (new Date()).toString() + "\n");
        wOut.write("%%Origin: 0 0\n");
        wOut.write(String.format(Locale.US, "%%%%BoundingBox: 0 0 %.0f %.0f\n",
        (pageWidth/25.4)*72, (pageLength/25.4)*72));
        wOut.write("%%Pages: 1 +1\n");
        wOut.write("%%EndComments\n");
        wOut.write("%%BeginProlog\n");
        wOut.write(String.format(Locale.US, "%d dict begin\n", maxcomments*2 + 80));
        wOut.write(prologue);
        emitToDefs(toName);
        wOut.write(String.format(Locale.US, "/pageWidth %.2f def\n", pageWidth));
        wOut.write(String.format(Locale.US, "/pageLength %.2f def\n", pageLength));
        emitFromDefs();
        coverDef("page-count", String.valueOf(pageCount));
        emitDateDefs();
        coverDef("regarding", regarding);
        emitCommentDefs();
        wOut.write(String.format(Locale.US, "/MaxComments %d def\n", maxcomments));
        wOut.write("MaxComments comments BreakIntoCommentX\n");
        wOut.write(String.format(Locale.US, "/MaxLenComments %d def\n", maxlencomments));
        wOut.write("MaxComments comments BreakIntoCommentsX\n");
        wOut.write("%%EndProlog\n");
        wOut.write("%%Page: \"1\" 1\n");
        // Copy prototype cover page:
        char[] buf = new char[8191];
        int cRead;
        do {
            cRead = rCover.read(buf);
            if (cRead > 0)
                wOut.write(buf, 0, cRead);
        } while (cRead >= 0);
        wOut.write("\nend\n");
        
        rCover.close();
        wOut.close();
    }
    
    private void emitToDefs(String to) 
        throws IOException {
        coverDef("to",      to);
        coverDef("to-company",  toCompany);
        coverDef("to-location", toLocation);
        coverDef("to-voice-number", toVoiceNumber);
        coverDef("to-fax-number",   toFaxNumber);
    }
    
    private void emitFromDefs() 
        throws IOException {
        coverDef("from",        sender);
        coverDef("from-fax-number", fromFaxNumber);
        coverDef("from-voice-number",fromVoiceNumber);
        coverDef("from-company",    fromCompany);
        coverDef("from-location",   fromLocation);
    }
    
    private void emitCommentDefs() 
        throws IOException {
        // Fix up new line characters omitted
        //comments.replaceAll("\\\\n", "\n");
        coverDef("comments", comments);
    }
    
    private void emitDateDefs() 
        throws IOException {
        coverDef("todays-date", dateFmt.format(new Date()));
    }
    
    private void coverDef(String tag, String value) 
        throws IOException {
        wOut.write("/" + tag + " (");
        for (int i=0; i < value.length(); i++) {
            char v = value.charAt(i);
            if (v == '(' || v == ')' || v == '\\')
                wOut.write('\\');
            wOut.write(v);
        }
        wOut.write(") def\n");
    }
    
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
            } else if (Arrays.equals(sig, "%PDF".getBytes())) {
                BufferedInputStream bIn = new BufferedInputStream(psFile);
                byte[] buf = new byte[4000];
                final byte[] search = "/Type /Page".getBytes();
                int offset = 0, len = 0;
                
                while ((len = bIn.read(buf, offset, buf.length - offset)) > 0) {
                    int slash = -1;
                    
                    len += offset; // len is max. index of buf
                    
                    offset = 0;
                    while ((slash = arrIndexOf(buf, (byte)'/', slash + 1)) >= 0) {
                        if (slash < (len - search.length)) {
                            if (arrEqualsOffset(buf, search, slash) && (buf[slash + search.length] != (byte)'s'))
                                pages++;
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
    
    private boolean arrEqualsOffset(byte[] arrSrc, byte[] arrCmp, int srcOffset) {
        for (int i = 0; i < arrCmp.length; i++) {
            if (arrSrc[i+srcOffset] != arrCmp[i])
                return false;
        }
        return true;
    }
    
    private int arrIndexOf(byte[] arr, byte key, int offset) {
        for (int i = offset; i < arr.length; i++) {
            if (arr[i] == key)
                return i;
        }
        return -1;
    }
    
    
    public Faxcover() {
        //dateFmt("%a %b %d %Y, %H:%M %Z")
        dateFmt = DateFormat.getDateInstance(DateFormat.FULL, utils.getLocale());
        coverTemplate = null;
        
        maxcomments = 20;
        maxlencomments = 35;
    }
    
    
    // Testing code:
    public static void main(String[] args) {
        System.out.println("Creating cover page...");
        Faxcover cov = new Faxcover();
        cov.coverTemplate = new File("yajhfc/faxcover/faxcover.ps");
        
        cov.comments = "foo\niniun iunuini uinini ninuin iuniuniu 9889hz h897h789 bnin uibiubui ubuib uibub ubiu bib bib ib uib i \nbar";
        cov.fromCompany = "foo Ü&Ö OHG";
        cov.fromFaxNumber = "989898";
        cov.fromLocation = "Bardorf";
        cov.fromVoiceNumber = "515616";
        
        //cov.pageCount = 10;
        String[] docs = { "/home/jonas/mozilla.ps", "/home/jonas/nssg.pdf" };
        for (int i=0; i<docs.length; i++)
            try {
                System.out.println(docs[i] + " pages: " + cov.estimatePostscriptPages(new FileInputStream(docs[i])));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        cov.pageCount = 1;
        cov.pageLength = 297;
        cov.pageWidth = 210;
        cov.regarding = "Test fax";
        cov.sender = "Werner Meißner";
        
        cov.toCompany = "Bâr GmbH & Co. KGaA";
        cov.toFaxNumber = "87878787";
        cov.toLocation = "Foostädtle";
        cov.toName = "Otto Müller";
        cov.toVoiceNumber = "4545454";
        
        try {
            cov.makeCoverSheet(new FileOutputStream("/tmp/test.ps"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
