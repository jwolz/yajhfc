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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.EntryToStringRule;

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
            public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
                return arg0.dateFmt.format(arg0.coverDate);
            }
        });
        availableTags.put("pagecount", new ReflectionTag("pageCount"));
        availableTags.put("totalpagecount", new Tag() {
            @Override
            public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
                return String.valueOf(arg0.pageCount+1);
            }
        });
        
        // Conditionals:
        IfTag allFilled = new IfAllFilledTag();
        IfTag someFilled = new IfSomeFilledTag();
        availableTags.put("ifallfilled", allFilled);
        availableTags.put("ifsomefilled", someFilled);
        availableTags.put("ifallempty", new IfNotTag(someFilled));
        availableTags.put("ifsomeempty", new IfNotTag(allFilled));
        availableTags.put("else", new ElseTag());
        availableTags.put("endif", new EndIfTag());
    }
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
                        
                        Tag tag = availableTags.get(tagName);
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
        
        if (conditionStack.size() > 0) {
            log.warning("Found " + conditionStack.size() + " if tags without an @@ENDIF@@!");
        }
    }
    
    protected static abstract class Tag {
        /**
         * Returns the String the tag should be replaced with
         * @param instance
         * @param conditionStack
         * @param param
         * @return
         */
        public abstract String getValue(Faxcover instance, List<ConditionState> conditionStack, String param);
        
        /**
         * Determines if the value should be copied unmodified or if characters should be escaped
         * @return
         */
        public boolean valueIsRaw() {
            return false;
        }
    }
    
    protected abstract static class IfTag extends Tag { 
        protected abstract boolean evaluate(Faxcover arg0, List<ConditionState> conditionStack, String param);
        
        @Override
        public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
            if (param == null) {
                log.info("Found If without an parameter!");
                return "Found If without an parameter!";
            }
            boolean val = evaluate(arg0, conditionStack, param);
            conditionStack.add(new ConditionState(val));
            if (val) {
                return "";
            } else {
                return "<!-- ";
            }
        }
        
        @Override
        public boolean valueIsRaw() {
            return true;
        }
    }
    
    protected static class IfNotTag extends IfTag {
        protected final IfTag wrapped;
        
        @Override
        protected boolean evaluate(Faxcover arg0, List<ConditionState> arg1,
                String arg2) {
            return !wrapped.evaluate(arg0, arg1, arg2);
        }

        public IfNotTag(IfTag wrapped) {
            super();
            this.wrapped = wrapped;
        }
    }
    
    /**
     * State for IfTags
     * @author jonas
     *
     */
    protected static class ConditionState {
        public final boolean ifWasTaken;
        public boolean hadElse = false;
        
        public ConditionState(boolean ifWasTaken) {
            super();
            this.ifWasTaken = ifWasTaken;
        }
    }
    
    protected static class IfAllFilledTag extends IfTag {
        @Override
        protected boolean evaluate(Faxcover arg0, List<ConditionState> conditionStack, String param) {
            String[] childTags = Utils.fastSplit(param, ',');
            for (String sTag : childTags) {
                Tag tag = availableTags.get(sTag);
                String tagValue = null;
                if (tag != null) {
                    tagValue = tag.getValue(arg0, conditionStack, null);
                }
                if (tagValue == null || tagValue.length() == 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    protected static class IfSomeFilledTag extends IfTag {
        @Override
        protected boolean evaluate(Faxcover arg0, List<ConditionState> conditionStack, String param) {
            String[] childTags = Utils.fastSplit(param, ',');
            for (String sTag : childTags) {
                Tag tag = availableTags.get(sTag);
                String tagValue = null;
                if (tag != null) {
                    tagValue = tag.getValue(arg0, conditionStack, null);
                }
                if (tagValue != null && tagValue.length() > 0) {
                    return true;
                }
            }
            return false;
        }
    }
    
    protected static class ElseTag extends Tag {        
        @Override
        public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
            int size = conditionStack.size();
            if (size == 0) {
                log.warning("Found @@ELSE@@ without an if!");
                return "Found @@ELSE@@ without an if!";
            }
            
            ConditionState state = conditionStack.get(size-1);
            if (state.hadElse) {
                log.warning("Found more than one @@ELSE@@ for an IF!");
                return "Found more than one @@ELSE@@ for an IF!";
            } else {
                state.hadElse = true;
                if (state.ifWasTaken) { // Last if was taken
                    return "<!-- ";
                } else {
                    // Check if this else is embedded in an already commented out section:
                    for (int i = size - 2; i >= 0; i--) {
                        state = conditionStack.get(i);
                        if (!state.ifWasTaken || state.hadElse) {
                            return "---";
                        }
                    }
                    return "-->";
                }
            }
        }
        
        @Override
        public boolean valueIsRaw() {
            return true;
        }
    }
    
    protected static class EndIfTag extends Tag {
        @Override
        public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
            int size = conditionStack.size();
            if (size == 0) {
                log.warning("Found @@ENDIF@@ without an if!");
                return "Found @@ENDIF@@ without an if!";
            }
            
            ConditionState lastState = conditionStack.remove(size-1);
            boolean writeEndComment = !lastState.ifWasTaken || lastState.hadElse;
            
            if (writeEndComment) {
                // Check if this end if is embedded in an already commented out section:
                for (ConditionState state : conditionStack) {
                    if (!state.ifWasTaken || state.hadElse) {
                        return "---";
                    }
                }
                return "-->";
            }

            return "";
        }
        
        @Override
        public boolean valueIsRaw() {
            return true;
        }
    }
    
    protected static class PBFieldTag extends Tag {
        protected final boolean isFrom;
        protected final PBEntryField field;
        
        @Override
        public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
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
        public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
            try {
                EntryToStringRule entryRule = (EntryToStringRule)ruleField.get(arg0);
                return entryRule.applyRule(isFrom ? arg0.fromData : arg0.toData);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error getting value", e);
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
                log.log(Level.SEVERE, "Invalid field", e);
            }
            this.ruleField = field;
        }
    }
    
    protected static class ReflectionTag extends Tag {
        protected final Field field;

        @Override
        public String getValue(Faxcover instance, List<ConditionState> conditionStack, String param) {
            try {
                return field.get(instance).toString();
            } catch (Exception e) {
                log.log(Level.WARNING, "Error getting value", e);
                return "";
            }
        }

        protected ReflectionTag(String fieldName) {
            super();
            Field rField = null;
            try {
                rField = Faxcover.class.getField(fieldName);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Invalid field", e);
            }
            this.field = rField;
        }

    }
}
