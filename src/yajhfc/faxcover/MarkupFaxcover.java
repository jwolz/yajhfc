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
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.file.FileConverter.ConversionException;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.EntryToStringRule;

/**
 * @author jonas
 *
 */
public abstract class MarkupFaxcover extends Faxcover {
    
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
    
    
    public static final Map<String,Tag> availableTags = new HashMap<String,Tag>();
    static {
        // Tag names MUST be lower case to allow case insensitive comparison!
    
        // Sender
        availableTags.put("name", new RuleTag("nameRule", false));
        availableTags.put("surname", new PBFieldTag(PBEntryField.Name, false));
        availableTags.put("givenname", new PBFieldTag(PBEntryField.GivenName, false));
        availableTags.put("title", new PBFieldTag(PBEntryField.Title, false));
        availableTags.put("position", new PBFieldTag(PBEntryField.Position, false));

        availableTags.put("company", new RuleTag("companyRule", false));
        availableTags.put("companyname", new PBFieldTag(PBEntryField.Company, false));
        availableTags.put("department", new PBFieldTag(PBEntryField.Department, false));

        availableTags.put("location", new RuleTag("locationRule", false));
        availableTags.put("street", new PBFieldTag(PBEntryField.Street, false));
        availableTags.put("place", new PBFieldTag(PBEntryField.Location, false));
        availableTags.put("zipcode", new PBFieldTag(PBEntryField.ZIPCode, false));
        availableTags.put("state", new PBFieldTag(PBEntryField.State, false));
        availableTags.put("country", new PBFieldTag(PBEntryField.Country, false));

        availableTags.put("faxnumber", new PBFieldTag(PBEntryField.FaxNumber, false));
        availableTags.put("voicenumber", new PBFieldTag(PBEntryField.VoiceNumber, false));
        availableTags.put("email", new PBFieldTag(PBEntryField.EMailAddress, false)); 
        availableTags.put("website", new PBFieldTag(PBEntryField.WebSite, false));

        // Recipient:
        availableTags.put("fromname", new RuleTag("nameRule", true));
        availableTags.put("fromsurname", new PBFieldTag(PBEntryField.Name, true));
        availableTags.put("fromgivenname", new PBFieldTag(PBEntryField.GivenName, true));
        availableTags.put("fromtitle", new PBFieldTag(PBEntryField.Title, true));
        availableTags.put("fromposition", new PBFieldTag(PBEntryField.Position, true));

        availableTags.put("fromcompany", new RuleTag("companyRule", true));
        availableTags.put("fromcompanyname", new PBFieldTag(PBEntryField.Company, true));
        availableTags.put("fromdepartment", new PBFieldTag(PBEntryField.Department, true));

        availableTags.put("fromlocation", new RuleTag("locationRule", true));
        availableTags.put("fromstreet", new PBFieldTag(PBEntryField.Street, true));
        availableTags.put("fromplace", new PBFieldTag(PBEntryField.Location, true));
        availableTags.put("fromzipcode", new PBFieldTag(PBEntryField.ZIPCode, true));
        availableTags.put("fromstate", new PBFieldTag(PBEntryField.State, true));
        availableTags.put("fromcountry", new PBFieldTag(PBEntryField.Country, true));

        availableTags.put("fromfaxnumber", new PBFieldTag(PBEntryField.FaxNumber, true));
        availableTags.put("fromvoicenumber", new PBFieldTag(PBEntryField.VoiceNumber, true));
        availableTags.put("fromemail", new PBFieldTag(PBEntryField.EMailAddress, true));
        availableTags.put("fromwebsite", new PBFieldTag(PBEntryField.WebSite, true));
        
        // Misc. tags:
        availableTags.put("subject", new ReflectionTag("regarding"));
        availableTags.put("comments", new ReflectionTag("comments"));
        availableTags.put("date", new Tag() {
            @Override
            public String getValue(Faxcover arg0) {
                return arg0.dateFmt.format(arg0.coverDate);
            }
        });
        availableTags.put("pagecount", new ReflectionTag("pageCount"));
        availableTags.put("totalpagecount", new Tag() {
            @Override
            public String getValue(Faxcover arg0) {
                return String.valueOf(arg0.pageCount+1);
            }
        });
    }
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
            int loopEnd = (numRead < 0 ? 0 : numRead) + readOffset;
            for (int i = 0; i < loopEnd; i++) {
                if (buf[i] == TAGCHAR && (i+1) < loopEnd && buf[i+1] == TAGCHAR) {
                    // Found start/end of a tag
                    if (lastTag != -1 && i - lastTag < MAXTAGLENGTH) {
//                        if (matchesTag(buf, lastTag, NAME_TAG)) {
//                            replacement = nameRule.applyRule(toData);
//                        } else if (matchesTag(buf, lastTag, LOCATION_TAG)) {
//                            replacement = locationRule.applyRule(toData);
//                        } else if (matchesTag(buf, lastTag, COMPANY_TAG)) {
//                            replacement = companyRule.applyRule(toData);
//                        } else if (matchesTag(buf, lastTag, FAXNUMBER_TAG)) {
//                            replacement = toData.get(PBEntryField.FaxNumber);
//                        } else if (matchesTag(buf, lastTag, VOICENUMBER_TAG)) {
//                            replacement = toData.get(PBEntryField.VoiceNumber);
//                        } else if (matchesTag(buf, lastTag, FROMNAME_TAG)) {
//                            replacement = nameRule.applyRule(fromData);
//                        } else if (matchesTag(buf, lastTag, FROMCOMPANY_TAG)) {
//                            replacement = companyRule.applyRule(fromData);
//                        } else if (matchesTag(buf, lastTag, FROMLOCATION_TAG)) {
//                            replacement = locationRule.applyRule(fromData);
//                        } else if (matchesTag(buf, lastTag, FROMFAXNUMBER_TAG)) {
//                            replacement = fromData.get(PBEntryField.FaxNumber);
//                        } else if (matchesTag(buf, lastTag, FROMVOICENUMBER_TAG)) {
//                            replacement = fromData.get(PBEntryField.VoiceNumber);
//                        } else if (matchesTag(buf, lastTag, FROMEMAIL_TAG)) {
//                            replacement = fromData.get(PBEntryField.EMailAddress);
//                        } else if (matchesTag(buf, lastTag, SUBJECT_TAG)) {
//                            replacement = this.regarding;
//                        } else if (matchesTag(buf, lastTag, COMMENT_TAG)) {
//                            replacement = this.comments;
//                        } else if (matchesTag(buf, lastTag, DATE_TAG)) {
//                            replacement = this.dateFmt.format(new Date());
//                        } else if (matchesTag(buf, lastTag, NUMPAGES_TAG)) {
//                            replacement = String.valueOf(this.pageCount);
//                        }
                        
                        String tagName = new String(buf, lastTag, i - lastTag).toLowerCase();
                        Tag tag = availableTags.get(tagName);
                        if (tag == null) { // Doesn't match any tag -> copy unmodified
                            lastTag = i+2;
                            i++; // skip second @
                        } else {
                            String replacement = tag.getValue(this);
                            if (replacement == null) {
                                replacement = "";
                            }
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
                readOffset = 2;
            }

            out.write(buf, writePointer, loopEnd - readOffset - writePointer);

            if (readOffset > 0) { // Copy stuff that may be the start of a tag for processing in the next iteration
                System.arraycopy(buf, loopEnd - readOffset, buf, 0, readOffset);
            }
        } while (numRead >= 0); // (numRead == readLen);

        out.close();
    }

//    private static boolean matchesTag(char[] buffer, int offset, String tag) {
//        int tagLen = tag.length();
//        for (int i = 0; i < tagLen; i++) {
//            if (Character.toLowerCase(buffer[i + offset]) != tag.charAt(i)) {
//                return false;
//            }
//        }
//        return (buffer[offset+tagLen] == TAGCHAR && buffer[offset+tagLen+1] == TAGCHAR);
//    }

    protected static abstract class Tag {
        public abstract String getValue(Faxcover instance);
    }
    
    protected static class PBFieldTag extends Tag {
        protected final boolean isFrom;
        protected final PBEntryField field;
        
        @Override
        public String getValue(Faxcover arg0) {
            return (isFrom ? arg0.fromData : arg0.toData).getField(field);
        }

        protected PBFieldTag(PBEntryField field, boolean isFrom) {
            super();
            this.field = field;
            this.isFrom = isFrom;
        }
    }
    
    protected static class RuleTag extends Tag {
        protected final boolean isFrom;
        protected final Field ruleField;
        
        @Override
        public String getValue(Faxcover arg0) {
            try {
                EntryToStringRule entryRule = (EntryToStringRule)ruleField.get(arg0);
                return entryRule.applyRule(isFrom ? arg0.fromData : arg0.toData);
            } catch (Exception e) {
                return "";
            }
        }

        protected RuleTag(String ruleFieldName, boolean isFrom) {
            super();
            this.isFrom = isFrom;
            Field field = null;
            try {
                field = Faxcover.class.getField(ruleFieldName);
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Invalid field", e);
            }
            this.ruleField = field;
        }
    }
    
    protected static class ReflectionTag extends Tag {
        protected final Field field;

        @Override
        public String getValue(Faxcover instance) {
            try {
                return field.get(instance).toString();
            } catch (Exception e) {
                return "";
            }
        }

        protected ReflectionTag(String fieldName) {
            super();
            Field rField = null;
            try {
                rField = Faxcover.class.getField(fieldName);
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Invalid field", e);
            }
            this.field = rField;
        }

    }
}
