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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.MultiFileConverter;
import yajhfc.file.UnknownFormatException;
import yajhfc.server.ServerManager;

/**
 * @author jonas
 *
 */
public abstract class HylaToTextConverter {
    private static final Logger log = Logger.getLogger(HylaToTextConverter.class.getName());
    public static final List<HylaToTextConverter> availableConverters = new ArrayList<HylaToTextConverter>();
    static {
        availableConverters.add(new PDFToTextConverter());
        availableConverters.add(new PSToTextConverter());
        availableConverters.add(new PSToAsciiConverter());
    }
    
    public static final String DEFAULT_CONVERTER = "yajhfc.file.textextract.PSToTextConverter";
    
    public static HylaToTextConverter findByString(String converter) {
        for (HylaToTextConverter conv : availableConverters) {
            if (conv.name().equals(converter)) {
                return conv;
            }
        }
        return null;
    }
    
    public static HylaToTextConverter findDefault() {
        if (Utils.debugMode) {
            log.fine("Trying to find converter " + Utils.getFaxOptions().hylaToTextConverter);
        }
        HylaToTextConverter rv = findByString(Utils.getFaxOptions().hylaToTextConverter);
        if (rv != null) {
            if (Utils.debugMode) {
                log.fine("Found converter of class " + rv.getClass().getName() + "; description=" + rv.getDescription());
            }
            return rv;
        } else {
            log.info("HylaToTextConverter " + Utils.getFaxOptions().hylaToTextConverter + " not found, falling back to " + DEFAULT_CONVERTER);
            rv = findByString(DEFAULT_CONVERTER);
            if (rv != null) {
                return rv;
            } else {
                throw new RuntimeException("Neither the configured converter " + Utils.getFaxOptions().hylaToTextConverter + " nor the default converter " + DEFAULT_CONVERTER + " could be found!");
            }
        }
    }
    
    /**
     * Returns an (internal and unique) name for this method
     * @return
     */
    public String name() {
        return getClass().getName();
    }
    
    /**
     * Returns a new instance using the specified options instead of Utils.getFaxOptions().
     * 
     * May also return this if this converter does not have configurable options;
     * @param options
     * @return
     */
    public HylaToTextConverter getInstanceForOptions(FaxOptions options) {
        try {
            Constructor<? extends HylaToTextConverter> constructor = getClass().getConstructor(FaxOptions.class);
            return constructor.newInstance(options);
        } catch (Exception e) {
            log.log(Level.FINE, "Assuming converter has no options", e);
            return this;
        } 
    }
    
    /**
     * Returns a user-readable (possibly localized) description of this converter
     * @return
     */
    public abstract String getDescription();
    
    @Override
    public String toString() {
        return getDescription();
    }
    
    /**
     * Returns the allowed input formats (PDF or PostScript).
     * If multiple formats are allowed, they should be ordered in order of preference (i.e. the preferred format should be first)
     * @return
     */
    public abstract FileFormat[] getAllowedInputFormats();
    
    
    /**
     * Converts the specified files to text. This method can be given files in any supported format (it converts these to an
     * allowed format if necessary).
     * This method may return one CharSequence for all files, one CharSequence per file or one CharSequence per page or something in between
     * (depending on the actual conversion method)
     * @param input
     * @return
     */
    public CharSequence[] convertFilesToText(List<FormattedFile> input) throws IOException, ConversionException {
        File[] convertedInput = ensureInputFormat(input);
        if (Utils.debugMode) {
            log.fine("Extracting text from the following files " + Arrays.toString(convertedInput) + " using " + getClass().getName());
        }
        return convertToText(convertedInput);
    }
   
    
    /**
     * Ensures that the given list of files has the correct input format
     * @param input
     * @return
     * @throws ConversionException
     * @throws IOException
     */
    protected File[] ensureInputFormat(List<FormattedFile> input) throws ConversionException, IOException {
        FileFormat[] allowedFormats = getAllowedInputFormats();
        if (Utils.debugMode) {
            log.fine("Allowed input formats: " + Arrays.toString(allowedFormats));
            log.fine("Input files: " + input);
        }
        
        List<File> okFiles = new ArrayList<File>();
        List<FormattedFile> filesToConvert = new ArrayList<FormattedFile>();
        for (FormattedFile ff : input) {
            if (Utils.indexOfArray(allowedFormats, ff.getFormat()) >= 0) {
                okFiles.add(ff.file); // File has an allowed format -> do not convert
            } else {
                filesToConvert.add(ff); // File needs conversion
            }
        }
        if (filesToConvert.size() > 0) {
            log.fine("Need to convert the following input files: " + filesToConvert);
            MultiFileConvFormat mfcf = null;
            for (FileFormat ff : allowedFormats) {
                mfcf = MultiFileConvFormat.getByFileFormat(ff);
                if (mfcf != null) {
                    break;
                }
            }
            if (mfcf == null) {
                throw new ConversionException("Cannot find a converter to " +  Arrays.toString(allowedFormats));
            }
            
            MultiFileConverter conv = mfcf.getConverter();
            File tempFile = File.createTempFile("textextract", "." + conv.getTargetFormat().getDefaultExtension());
            yajhfc.shutdown.ShutdownManager.deleteOnExit(tempFile);
            try {
                conv.convertMultipleFiles(filesToConvert, tempFile, ServerManager.getDefault().getCurrent().getOptions().paperSize); // Paper size does not actually matter here...
            } catch (UnknownFormatException e) {
                throw new ConversionException(e);
            } 
            
            okFiles.add(tempFile);
        }
        return okFiles.toArray(new File[okFiles.size()]);
    }
    
    /**
     * Converts the specified files to text. This method can assume that all files in input have a format that is one
     * of the allowed input formats.
     * This method may return one CharSequence for all files, one CharSequence per file or one CharSequence per page
     * (depending on the actual conversion method)
     * @param input
     * @return
     */
    protected abstract CharSequence[] convertToText(File[] input) throws IOException, ConversionException;
}
