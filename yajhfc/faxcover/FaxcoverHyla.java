package yajhfc.faxcover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

class FaxcoverHyla extends Faxcover {
    protected int      maxcomments;    // max # of comment lines
    protected int      maxlencomments; // max length of comment lines

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

    public void makeCoverSheet(OutputStream out) 
    throws IOException {
        wOut = new BufferedWriter(new OutputStreamWriter(out, "ISO8859-1"));

        BufferedReader rCover = new BufferedReader(new InputStreamReader(coverTemplate.openStream(), "ISO8859-1"));

        wOut.write("%!PS-Adobe-2.0 EPSF-2.0\n");
        wOut.write("%%Creator: faxcover\n");
        wOut.write("%%Title: HylaFAX Cover Sheet\n");
        wOut.write("%%CreationDate: " + (new Date()).toString() + "\n");
        wOut.write("%%Origin: 0 0\n");
        wOut.write(String.format(Locale.US, "%%%%BoundingBox: 0 0 %.0f %.0f\n",
                (pageSize.size.width/25.4)*72, (pageSize.size.height/25.4)*72));
        wOut.write("%%Pages: 1 +1\n");
        wOut.write("%%EndComments\n");
        wOut.write("%%BeginProlog\n");
        wOut.write(String.format(Locale.US, "%d dict begin\n", maxcomments*2 + 80));
        wOut.write(prologue);
        emitToDefs(toName);
        wOut.write(String.format(Locale.US, "/pageWidth %d def\n", pageSize.size.width));
        wOut.write(String.format(Locale.US, "/pageLength %d def\n", pageSize.size.height));
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
        char[] buf = new char[8100];
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
        coverDef("from-mail-address",fromMailAddress);
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
        wOut.write('/'); wOut.write(tag); wOut.write(" (");
        for (int i=0; i < value.length(); i++) {
            char v = value.charAt(i);
            if (v == '(' || v == ')' || v == '\\')
                wOut.write('\\');
            wOut.write(v);
        }
        wOut.write(") def\n");
    }

    public FaxcoverHyla(URL coverTemplate) {
        super(coverTemplate);
        
        maxcomments = 20;
        maxlencomments = 35;
    }
}
