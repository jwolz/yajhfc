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
package yajhfc.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.FaxOptions;
import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.options.MultiFileMode;
import yajhfc.send.HylaTFLItem;

/**
 * @author jonas
 *
 */
public abstract class MultiFileConverter {
    private static final Logger log = Logger.getLogger(MultiFileConverter.class.getName());
    
    protected FileCache cache;
    
    /**
     * @return the cache
     */
    protected FileCache getCache() {
        if (cache == null) {
            cache = new FileCache();
        }
        return cache;
    }

    public abstract FileFormat getTargetFormat();
    
    public abstract void convertMultiplePSorPDFFiles(File[] files, File targetFile, PaperSize paperSize) throws IOException, ConversionException;

    public void convertMultipleFiles(List<FormattedFile> files, File targetName, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        // Do not convert the files if we already have a single file *and* it has the right format
        if ((files.size() == 1 && files.get(0).getFormat() == getTargetFormat())) {
            Utils.copyFile(files.get(0).file, targetName);
            return;
        }
        
        File cached = getCache().checkCache(files, paperSize);
        if (cached != null) {
            log.fine("Found valid cached file " + cached);
            Utils.copyFile(cached, targetName);
            return;
        }
        log.fine("Did not find cached file");
        
        File[] target = new File[files.size()];

        for (int i=0; i<files.size(); i++) {
            FormattedFile ff = files.get(i);
            switch (ff.getFormat()) {
            case PDF:
            case PostScript:
                target[i] = ff.file;
                break;
            default:
                FileConverter conv = FileConverters.getConverterFor(ff.getFormat());
                if (conv == null || conv == FileConverter.IDENTITY_CONVERTER) {
                    throw new UnknownFormatException("Unsupported file format: " + ff.getFormat());
                } else {
                    File tmpFile = File.createTempFile("multi", ".ps");
                    yajhfc.shutdown.ShutdownManager.deleteOnExit(tmpFile);
                    
                    FileFormat targetFormat = getTargetFormat();
                    switch (targetFormat) {
                    case PDF:
                    case PostScript:
                        break;
                    default:
                        // We always need PDF or PS here since the output may be fed to GS
                        targetFormat = FileFormat.PDF;
                        break;
                    }
                    if (conv instanceof FileConverterToFile) {
                        ((FileConverterToFile) conv).convertToHylaFormatFile(ff.file, tmpFile, paperSize, targetFormat);
                    } else {
                        FileOutputStream out = new FileOutputStream(tmpFile);
                        conv.convertToHylaFormat(ff.file, out, paperSize, targetFormat);
                        out.close();
                    }
                    
                    FileFormat tempFmt = FormattedFile.detectFileFormat(tmpFile);
                    switch (tempFmt) {
                    case PDF:
                    case PostScript:
                        break;
                    default:
                        throw new ConversionException("Converter output for file " + ff.file + " has an unsupported file format " + tempFmt + " (converter=" + conv + ")");
                    }
                    
                    target[i] = tmpFile;
                }
            }
        }
        // Do not convert the files if we already have a single file *and* it has the right format
        if ((target.length == 1 && FormattedFile.detectFileFormat(target[0]) == getTargetFormat())) {
            Utils.copyFile(target[0], targetName);
        } else {
            convertMultiplePSorPDFFiles(target, targetName, paperSize);
        }
        getCache().addToCache(files, targetName, paperSize);
    }
    
    /**
     * Views multiple files
     * @param files
     * @throws UnknownFormatException 
     * @throws IOException 
     * @throws ConversionException 
     */
    public static void viewMultipleFiles(List<FormattedFile> files, PaperSize paperSize, boolean isPreview) throws IOException, UnknownFormatException, ConversionException {
        final FaxOptions faxOptions = Utils.getFaxOptions();
        boolean alwaysCreateTargetFormat;
        MultiFileConvFormat singleFileFormat;
        boolean createSingleFile;
        //boolean createSingleFile = faxOptions.createSingleFilesForViewing ||
        //        (isPreview &&  faxOptions.multiFileSendMode != MultiFileMode.NONE);
        if (isPreview) {
        	alwaysCreateTargetFormat = faxOptions.alwaysCreateTargetFormat;
        	singleFileFormat = faxOptions.singleFileFormat;
        	createSingleFile = faxOptions.multiFileSendMode != MultiFileMode.NONE;
        } else {
           	alwaysCreateTargetFormat = faxOptions.alwaysCreateTargetFormatForViewing;
        	singleFileFormat = faxOptions.singleFileFormatForViewing;
        	createSingleFile = faxOptions.createSingleFilesForViewing;
        }
        
        
		if (createSingleFile) {
			if (files.size() == 1 && 
                    (files.get(0).getFormat() == singleFileFormat.getFileFormat() || !alwaysCreateTargetFormat)) {
                files.get(0).view();
            } else {
                File tmpFile = File.createTempFile("view", "." + singleFileFormat.getFileFormat().getDefaultExtension());
                yajhfc.shutdown.ShutdownManager.deleteOnExit(tmpFile);
                FormattedFile ff = convertMultipleFilesToSingleFile(files, tmpFile, singleFileFormat, paperSize);
                ff.view();
            }
        } else {
            for (FormattedFile file: files) {
                file.view();
            }
        }
    }
    
    /**
     * Converts multiple input files into a single output file
     * @param files
     * @param targetName
     * @param targetFormat
     * @return
     * @throws ConversionException 
     * @throws UnknownFormatException 
     * @throws IOException 
     */
    public static FormattedFile convertMultipleFilesToSingleFile(List<FormattedFile> files, File targetName, MultiFileConvFormat targetFormat, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        MultiFileConverter conv = targetFormat.getConverter();
        if (conv == null) {
            throw new UnknownFormatException("Unsupported target format: " + targetFormat);
        } else {
            conv.convertMultipleFiles(files, targetName, paperSize);
            return new FormattedFile(targetName, conv.getTargetFormat());
        }
    }
    
    public static FormattedFile convertTFLItemsToSingleFile(List<HylaTFLItem> files, File targetName, MultiFileConvFormat targetFormat, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        List<FormattedFile> ffs = new ArrayList<FormattedFile>(files.size());
        for (HylaTFLItem item : files) {
            ffs.add(item.getPreviewFilename());
        }
        return convertMultipleFilesToSingleFile(ffs, targetName, targetFormat, paperSize);
    }

    public static void main(String[] args) throws IOException, UnknownFormatException, ConversionException {
        List<FormattedFile> files = Arrays.asList(new FormattedFile[] {
          new FormattedFile(new File("/home/jonas/test.ps")),
          new FormattedFile(new File("/home/jonas/Karte.png")),
          new FormattedFile(new File("/home/jonas/timeline.pdf")),
        });
        
        File out = new File("/tmp/out.pdf");
        convertMultipleFilesToSingleFile(files, out, MultiFileConvFormat.PDF, PaperSize.A4);
        
        Runtime.getRuntime().exec(new String[] {"kpdf", out.getPath()});
        
        out = new File("/tmp/out.ps");
        convertMultipleFilesToSingleFile(files, out, MultiFileConvFormat.PostScript, PaperSize.A4);
        
        Runtime.getRuntime().exec(new String[] {"gv", out.getPath()});
    }
}
