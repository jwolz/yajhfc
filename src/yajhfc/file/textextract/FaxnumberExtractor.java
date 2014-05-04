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
package yajhfc.file.textextract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FormattedFile;
import yajhfc.send.HylaTFLItem;

/**
 * @author jonas
 *
 */
public class FaxnumberExtractor {
    private static final Logger log = Logger.getLogger(FaxnumberExtractor.class.getName());
    /**
     * A comparator to sort strings by length, descending
     */
    static final Comparator<String> LENGTH_COMPARATOR_DESC = new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o2.length() - o1.length();
        }
    };
    
    /**
     * Subject indicating the document title should be used
     */
    public static final String SUBJECT_DOCTITLE = "<doctitle>";
    
    protected final Pattern[] faxnumberPatterns;
    protected final HylaToTextConverter converter;

       
    public FaxnumberExtractor(HylaToTextConverter converter,
            Pattern... faxnumberPatterns) {
        super();
        this.converter = converter;
        this.faxnumberPatterns = faxnumberPatterns;
    }

    public FaxnumberExtractor(HylaToTextConverter converter) {
        this(converter,
                getDefaultPattern());
    }
    
    public FaxnumberExtractor(Pattern... faxnumberPatterns) {
        this(HylaToTextConverter.findDefault(), faxnumberPatterns);
    }

    public FaxnumberExtractor() {
        this(HylaToTextConverter.findDefault());
    }
    
    public static Pattern getDefaultPattern() {
        //return Pattern.compile("@@\\s*(?:recipient|fax)\\s*:?(.+?)@@", Pattern.CASE_INSENSITIVE);
        return buildPatternFromOptions(Utils.getFaxOptions(), PATTERN_PREFIX_FAX);
    }
    
    public static Pattern getDefaultMailPattern() {
        //return Pattern.compile("@@\\s*mail(?:recipient)?\\s*:?(.+?)@@", Pattern.CASE_INSENSITIVE);
        return buildPatternFromOptions(Utils.getFaxOptions(), PATTERN_PREFIX_MAIL);
    }
    
    public static Pattern getDefaultSubjectPattern() {
        return buildPatternFromOptions(Utils.getFaxOptions(), PATTERN_PREFIX_SUBJECT);
    }
    
    public static final char PATTERN_PREFIX_FAX  = 'F';
    public static final char PATTERN_PREFIX_MAIL = 'M';
    public static final char PATTERN_PREFIX_SUBJECT = 'S';
    
    public static Pattern buildPatternFromOptions(FaxOptions fo, char prefix) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        String[] tags = fo.recipientExtractionTags.toArray(new String[fo.recipientExtractionTags.size()]);
        // Sort tags by length, longest first
        // This ensures that more "specific" tags will match first (e.g. mailrecipient vs. mail)
        Arrays.sort(tags, LENGTH_COMPARATOR_DESC);
        
        result.append("@@\\s*(?:");
        for (String s : tags) {
            if (s.length() > 1 && s.charAt(0) == prefix) {
                if (first)
                    first=false;
                else
                    result.append('|');
                result.append(Pattern.quote(s.substring(1)));
            }
        }
        if (first) {
            log.info("No tag names for prefix " + prefix + " found, returning null as Pattern.");
            return null;
        }
        result.append(")\\s*:");
        if (!fo.recipientExtractionTagMandatoryColon)
            result.append('?');
        result.append("(.+?)@@");
        
        log.fine("Built pattern for prefix " + prefix + ": " + result);
        return Pattern.compile(result.toString(), Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Extract fax numbers from the specified input files and add them to listToAddTo
     * @param input
     * @param listToAddTo
     * @return the number of fax numbers found
     * @throws IOException
     * @throws ConversionException
     */
    public int extractFromMultipleFileNames(Collection<String> input, Collection<String>... listsToAddTo) throws IOException, ConversionException {
        if (input.size() == 0)
            return 0;
        
        List<FormattedFile> formattedInput = new ArrayList<FormattedFile>(input.size());
        for (String f : input) {
            formattedInput.add(new FormattedFile(f));
        }
        return extractFromMultipleFiles(formattedInput, listsToAddTo);
    }

    /**
     * Extract fax numbers from the specified input files and add them to listToAddTo
     * @param input
     * @param listToAddTo
     * @return the number of fax numbers found
     * @throws IOException
     * @throws ConversionException
     */
    public int extractFromMultipleDocuments(Collection<HylaTFLItem> input, Collection<String>... listsToAddTo) throws IOException, ConversionException {
        if (input.size() == 0)
            return 0;
        
        List<FormattedFile> formattedInput = new ArrayList<FormattedFile>(input.size());
        for (HylaTFLItem f : input) {
            formattedInput.add(f.getPreviewFilename());
        }
        return extractFromMultipleFiles(formattedInput, listsToAddTo);
    }
    
    /**
     * Extract fax numbers from the specified input files and add them to listToAddTo
     * @param input
     * @param listToAddTo
     * @return the number of fax numbers found
     * @throws IOException
     * @throws ConversionException
     */
    public int extractFromMultipleFiles(List<FormattedFile> input, Collection<String>... listsToAddTo) throws IOException, ConversionException {
        if (input.size() == 0)
            return 0;
        if (listsToAddTo.length != faxnumberPatterns.length) {
            throw new IllegalArgumentException("The number of output lists must match the number of patterns!");
        }
        
        CharSequence[] texts = converter.convertFilesToText(input);
        
        int n = 0;
        for (int i=0; i<listsToAddTo.length; i++) {
            Collection<String> listToAddTo = listsToAddTo[i];
            Pattern pattern = faxnumberPatterns[i];
            if (pattern == null) {
                log.fine("Null pattern at index " + i + ", not extracting anything.");
                continue;
            }
            
            Collection<String> tempColl;
            // Make sure we dedup the numbers
            if (listToAddTo instanceof Set) {
                tempColl = listToAddTo;
            } else {
                tempColl = new TreeSet<String>();
            }

            for (CharSequence text : texts) {
                n += getMatchesInText(text, pattern, 1, tempColl);
            }

            if (tempColl != listToAddTo) {
                listToAddTo.addAll(tempColl);
            }
        }
        return n;
    }
    
    /**
     * Search for patterns matching faxnumberPattern and add the specified captureGroup to listToAddTo
     * @param text
     * @param captureGroup
     * @param listToAddTo
     * @return the number of matches found
     * @throws IOException
     */
    public int getMatchesInText(CharSequence text, Pattern pattern, int captureGroup, Collection<String> listToAddTo) throws IOException {
        if (Utils.debugMode) {
            log.finest("input text is:\n" + text);
        }
        //System.out.println(text);
        Matcher m = pattern.matcher(text);
        int n = 0;
        
        while (m.find()) {
            if (Utils.debugMode) {
                log.fine("Found match: " + m);
            }
            String num = m.group(captureGroup).trim();
            if (num.length() > 0) {
                listToAddTo.add(num);
                n++;
            }
        }
        if (Utils.debugMode) {
            log.fine("No more matches; " + n + " matches found in total.");
        }
        return n;
    }

}
