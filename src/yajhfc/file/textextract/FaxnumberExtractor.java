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
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    protected final Pattern faxnumberPattern;
    protected final HylaToTextConverter converter;
       
    public FaxnumberExtractor(HylaToTextConverter converter,
            Pattern faxnumberPattern) {
        super();
        this.converter = converter;
        this.faxnumberPattern = faxnumberPattern;
    }

    public FaxnumberExtractor(HylaToTextConverter converter) {
        this(converter,
                Pattern.compile("@@\\s*(?:recipient|fax)\\s*:?(.+?)@@", Pattern.CASE_INSENSITIVE));
    }

    public FaxnumberExtractor() {
        this(HylaToTextConverter.findDefault());
    }
    
    /**
     * Extract fax numbers from the specified input files and add them to listToAddTo
     * @param input
     * @param listToAddTo
     * @return the number of fax numbers found
     * @throws IOException
     * @throws ConversionException
     */
    public int extractFromMultipleFileNames(Collection<String> input, List<String> listToAddTo) throws IOException, ConversionException {
        if (input.size() == 0)
            return 0;
        
        List<FormattedFile> formattedInput = new ArrayList<FormattedFile>(input.size());
        for (String f : input) {
            formattedInput.add(new FormattedFile(f));
        }
        return extractFromMultipleFiles(formattedInput, listToAddTo);
    }

    /**
     * Extract fax numbers from the specified input files and add them to listToAddTo
     * @param input
     * @param listToAddTo
     * @return the number of fax numbers found
     * @throws IOException
     * @throws ConversionException
     */
    public int extractFromMultipleDocuments(Collection<HylaTFLItem> input, List<String> listToAddTo) throws IOException, ConversionException {
        if (input.size() == 0)
            return 0;
        
        List<FormattedFile> formattedInput = new ArrayList<FormattedFile>(input.size());
        for (HylaTFLItem f : input) {
            formattedInput.add(f.getPreviewFilename());
        }
        return extractFromMultipleFiles(formattedInput, listToAddTo);
    }
    
    /**
     * Extract fax numbers from the specified input files and add them to listToAddTo
     * @param input
     * @param listToAddTo
     * @return the number of fax numbers found
     * @throws IOException
     * @throws ConversionException
     */
    public int extractFromMultipleFiles(List<FormattedFile> input, List<String> listToAddTo) throws IOException, ConversionException {
        if (input.size() == 0)
            return 0;
        
        CharSequence[] texts = converter.convertFilesToText(input);        
        
        int n = 0;
        for (CharSequence text : texts) {
            n += getMatchesInText(text, 1, listToAddTo);
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
    public int getMatchesInText(CharSequence text, int captureGroup, List<String> listToAddTo) throws IOException {
        if (Utils.debugMode) {
            log.finest("input text is:\n" + text);
        }
        //System.out.println(text);
        Matcher m = faxnumberPattern.matcher(text);
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
