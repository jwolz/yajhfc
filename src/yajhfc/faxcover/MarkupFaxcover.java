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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.faxcover.tag.ConditionState;
import yajhfc.faxcover.tag.Tag;
import yajhfc.file.FileConverter.ConversionException;

/**
 * @author jonas
 *
 */
public abstract class MarkupFaxcover extends Faxcover {
    
    static final Logger log = Logger.getLogger(MarkupFaxcover.class.getName());
    
    /**
     * Set this to true if non-ASCII characters should be encoded as 
     * HTML/XML entity
     */
    protected boolean encodeNonASCIIAsEntity = false;
    /**
     * Set this to the string the new line character should be replaced with.
     */
    protected String newLineReplacement = "\n";
    
    /**
     * Set this to the string the apostrophe character (') should be replaced with.
     */
    protected String aposReplacement = "&apos;";
    
    /**
     * The encoding used for input and output streams
     */
    protected String encoding = "utf-8";
    
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
            Reader inReader = new InputStreamReader(in, encoding);
            Writer outWriter = new OutputStreamWriter(new FileOutputStream(tempFile), encoding);
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
    
    
    public static final int MAXTAGLENGTH = 300;
    public static final char TAGCHAR = '@';
    
    protected void replaceTags(Reader in, Writer out) throws IOException {
        final char[] buf = new char[8000];
        final List<ConditionState> conditionStack = new ArrayList<ConditionState>();
        int readLen;
        int readOffset = 0;
        int numRead;
        out = new BufferedWriter(out);
        do {
            readLen = buf.length - readOffset;
            numRead = in.read(buf, readOffset, readLen);

            int lastTag = -1;
            int writePointer = 0;
            int loopEnd = (numRead < 0 ? 0 : numRead) + readOffset;
            for (int i = 0; i < loopEnd; i++) {
                if (buf[i] == TAGCHAR && (i+1) < loopEnd && buf[i+1] == TAGCHAR) {
                    // Found start/end of a tag
                    if (lastTag != -1 && i - lastTag < MAXTAGLENGTH) {
                        
                        String tagText = new String(buf, lastTag, i - lastTag).toLowerCase();
                        String tagName;
                        String tagParam;
                        int pos = tagText.indexOf(':');
                        if (pos > 0) {
                            tagName = tagText.substring(0, pos);
                            tagParam = tagText.substring(pos+1);
                        } else {
                            tagName = tagText;
                            tagParam = null;
                        }
                        
                        Tag tag = Tag.availableTags.get(tagName);
                        if (tag == null) { // Doesn't match any tag -> copy unmodified
                            lastTag = i+2;
                            i++; // skip second @
                        } else {
                            String replacement = tag.getValue(this, conditionStack, tagParam);
                            if (replacement == null) {
                                replacement = "";
                            }
                            // Write the unmodified part
                            out.write(buf, writePointer, lastTag - writePointer - 2);
                            if (tag.valueIsRaw()) {
                                out.write(replacement);
                            } else {
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
                                        out.write(aposReplacement);
                                        break;
                                    case '\n':
                                        out.write(newLineReplacement);
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
            if (numRead < 0) {
                readOffset = 0;
            } else if (lastTag >= 0 && loopEnd - lastTag < MAXTAGLENGTH + 4) { // There might be an incomplete tag at the end
                readOffset = loopEnd - lastTag + 2;
            } else {
                // Copy an eventual @@ at the beginning
                readOffset = Math.min(2, loopEnd - writePointer);
            }

            out.write(buf, writePointer, loopEnd - readOffset - writePointer);

            if (readOffset > 0) { // Copy stuff that may be the start of a tag for processing in the next iteration
                System.arraycopy(buf, loopEnd - readOffset, buf, 0, readOffset);
            }
        } while (numRead >= 0); // (numRead == readLen);

        out.close();
        
        if (conditionStack.size() > 0) {
            log.warning("Found " + conditionStack.size() + " if tags without an @@ENDIF@@!");
        }
    }
}
