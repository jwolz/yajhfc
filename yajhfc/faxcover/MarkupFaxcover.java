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
package yajhfc.faxcover;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Date;

import yajhfc.FileConverter.ConversionException;

/**
 * @author jonas
 *
 */
public abstract class MarkupFaxcover extends Faxcover {

    protected boolean encodeNonASCIIAsEntity = false;
    
    /**
     * @param coverTemplate
     */
    public MarkupFaxcover(URL coverTemplate) {
        super(coverTemplate);
    }

    /* (non-Javadoc)
     * @see yajhfc.faxcover.Faxcover#makeCoverSheet(java.io.OutputStream)
     */
    @Override
    public void makeCoverSheet(OutputStream out) throws IOException {
        try {
            createCoverSheet(coverTemplate.openStream(), out);
        } catch (ConversionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Replaces tags in the source document from in, converts it to HylaFAX format
     * using the FileConverter converter and writes it to out.
     * @param in
     * @param out
     * @throws IOException
     * @throws ConversionException
     */
    protected void createCoverSheet(InputStream in, OutputStream out) throws IOException, ConversionException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("cover", ".tmp");
            Reader inReader = new InputStreamReader(in, "utf-8");
            Writer outWriter = new OutputStreamWriter(new FileOutputStream(tempFile), "utf-8");
            replaceTags(inReader, outWriter);
            inReader.close();
            outWriter.close();

            convertMarkupToHyla(tempFile, out);
        } finally {
            if (tempFile != null) tempFile.delete();
        }
    }
    
    /**
     * Converts the given markup to hyla format
     * @param tempFile
     * @param out
     * @throws IOException
     * @throws ConversionException
     */
    protected abstract void convertMarkupToHyla(File tempFile, OutputStream out) throws IOException, ConversionException;
    
    // Tag names. MUST be lower case to allow case insensitive comparison
    public static final String NAME_TAG =            "name";
    public static final String LOCATION_TAG =        "location";
    public static final String COMPANY_TAG =         "company";
    public static final String FAXNUMBER_TAG =       "faxnumber";
    public static final String VOICENUMBER_TAG =     "voicenumber";
    public static final String FROMNAME_TAG =        "fromname";
    public static final String FROMLOCATION_TAG =    "fromlocation";
    public static final String FROMCOMPANY_TAG =     "fromcompany";
    public static final String FROMFAXNUMBER_TAG =   "fromfaxnumber";
    public static final String FROMVOICENUMBER_TAG = "fromvoicenumber";
    public static final String FROMEMAIL_TAG =       "fromemail";
    public static final String SUBJECT_TAG =         "subject";
    public static final String COMMENT_TAG =         "comments";
    public static final String DATE_TAG =            "date";
    public static final String NUMPAGES_TAG =        "pagecount";
    public static final int MAXTAGLENGTH = 16;
    public static final char TAGCHAR = '@';
    
    protected void replaceTags(Reader in, Writer out) throws IOException {
        final char[] buf = new char[8000];
        int readLen;
        int readOffset = 0;
        int numRead;
        out = new BufferedWriter(out);
        do {
            readLen = buf.length - readOffset;
            numRead = in.read(buf, readOffset, readLen);

            int lastTag = -1;
            int writePointer = 0;
            int loopEnd = numRead + readOffset;
            for (int i = 0; i < loopEnd; i++) {
                if (buf[i] == TAGCHAR && (i+1) < loopEnd && buf[i+1] == TAGCHAR) {
                    // Found start/end of a tag
                    if (lastTag != -1 && i - lastTag < MAXTAGLENGTH) {
                        String replacement = null;
                        if (matchesTag(buf, lastTag, NAME_TAG)) {
                            //tagLen = NAME_TAG.length();
                            replacement = this.toName;
                        } else if (matchesTag(buf, lastTag, LOCATION_TAG)) {
                            //tagLen = LOCATION_TAG.length();
                            replacement = this.toLocation;
                        } else if (matchesTag(buf, lastTag, COMPANY_TAG)) {
                            //tagLen = COMPANY_TAG.length();
                            replacement = this.toCompany;
                        } else if (matchesTag(buf, lastTag, FAXNUMBER_TAG)) {
                            //tagLen = FAXNUMBER_TAG.length();
                            replacement = this.toFaxNumber;
                        } else if (matchesTag(buf, lastTag, VOICENUMBER_TAG)) {
                            //tagLen = VOICENUMBER_TAG.length();
                            replacement = this.toVoiceNumber;
                        } else if (matchesTag(buf, lastTag, FROMNAME_TAG)) {
                            //tagLen = FROMNAME_TAG.length();
                            replacement = this.sender;
                        } else if (matchesTag(buf, lastTag, FROMCOMPANY_TAG)) {
                            //tagLen = FROMCOMPANY_TAG.length();
                            replacement = this.fromCompany;
                        } else if (matchesTag(buf, lastTag, FROMLOCATION_TAG)) {
                            //tagLen = FROMLOCATION_TAG.length();
                            replacement = this.fromLocation;
                        } else if (matchesTag(buf, lastTag, FROMFAXNUMBER_TAG)) {
                            //tagLen = FROMFAXNUMBER_TAG.length();
                            replacement = this.fromFaxNumber;
                        } else if (matchesTag(buf, lastTag, FROMVOICENUMBER_TAG)) {
                            //tagLen = FROMVOICENUMBER_TAG.length();
                            replacement = this.fromVoiceNumber;
                        } else if (matchesTag(buf, lastTag, FROMEMAIL_TAG)) {
                            //tagLen = FROMEMAIL_TAG.length();
                            replacement = this.fromMailAddress;
                        } else if (matchesTag(buf, lastTag, SUBJECT_TAG)) {
                            //tagLen = SUBJECT_TAG.length();
                            replacement = this.regarding;
                        } else if (matchesTag(buf, lastTag, COMMENT_TAG)) {
                            //tagLen = COMMENT_TAG.length();
                            replacement = this.comments;
                        } else if (matchesTag(buf, lastTag, DATE_TAG)) {
                            //tagLen = DATE_TAG.length();
                            replacement = this.dateFmt.format(new Date());
                        } else if (matchesTag(buf, lastTag, NUMPAGES_TAG)) {
                            //tagLen = NUMPAGES_TAG.length();
                            replacement = String.valueOf(this.pageCount);
                        }
                        if (replacement == null) { // Doesn't match any tag -> copy unmodified
                            lastTag = i+2;
                            i++; // skip second @
                        } else {
                            // Write the unmodified part
                            out.write(buf, writePointer, lastTag - writePointer - 2);
                            for (int j = 0; j < replacement.length(); j++) {
                                char c = replacement.charAt(j);
                                // Escape &, <, >, " and '
                                switch (c) {
                                case '&':
                                    out.write("&amp;");
                                    break;
                                case '<':
                                    out.write("&lt;");
                                    break;
                                case '>':
                                    out.write("&gt;");
                                    break;
                                case '\"':
                                    out.write("&quot;");
                                    break;
                                case '\'':
                                    out.write("&apos;");
                                    break;
                                default:
                                    if (encodeNonASCIIAsEntity) {
                                        if (c <= 127) {
                                            out.write(c);
                                        } else {
                                            out.write("&#" + (int)c + ";");
                                        }
                                    } else {
                                        out.write(c);
                                    }
                                }
                            }
                            // Set the write pointer behind the replaced tag
                            writePointer = i = i+2;
                            lastTag = -1;
                        }
                    } else {
                        lastTag = i+2;
                        i++; // skip second @
                    }
                }
            }
            if (numRead < readLen) {
                readOffset = 0;
            } else if (lastTag >= 0 && loopEnd - lastTag < MAXTAGLENGTH + 4) { // There might be an incomplete tag at the end
                readOffset = loopEnd - lastTag + 2;
            } else {
                readOffset = 2;
            }

            out.write(buf, writePointer, loopEnd - readOffset - writePointer);

            if (readOffset > 0) { // Copy stuff that may be the start of a tag for processing in the next iteration
                System.arraycopy(buf, loopEnd - readOffset, buf, 0, readOffset);
            }
        } while (numRead == readLen);

        out.close();
    }

    private boolean matchesTag(char[] buffer, int offset, String tag) {
        int tagLen = tag.length();
        for (int i = 0; i < tagLen; i++) {
            if (Character.toLowerCase(buffer[i + offset]) != tag.charAt(i)) {
                return false;
            }
        }
        return (buffer[offset+tagLen] == TAGCHAR && buffer[offset+tagLen+1] == TAGCHAR);
    }

}
