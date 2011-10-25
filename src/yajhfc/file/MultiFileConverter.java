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
 */
package yajhfc.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import yajhfc.FaxOptions;
import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.options.MultiFileMode;

/**
 * @author jonas
 *
 */
public abstract class MultiFileConverter {
   
    public abstract FileFormat getTargetFormat();
    
    public abstract void convertMultiplePSorPDFFiles(File[] files, File targetFile, PaperSize paperSize) throws IOException, ConversionException;

    public void convertMultipleFiles(List<FormattedFile> files, File targetName, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        // Do not convert the files if we already have a single file *and* it has the right format
        if ((files.size() == 1 && files.get(0).format == getTargetFormat())) {
            FileInputStream in = new FileInputStream(files.get(0).file);
            FileOutputStream out = new FileOutputStream(targetName);
            Utils.copyStream(in, out);
            in.close();
            out.close();
            return;
        }
        
        
        File[] target = new File[files.size()];

        for (int i=0; i<files.size(); i++) {
            FormattedFile ff = files.get(i);
            switch (ff.format) {
            case PDF:
            case PostScript:
                target[i] = ff.file;
                break;
            default:
                FileConverter conv = FormattedFile.fileConverters.get(ff.format);
                if (conv == null || conv == FileConverter.IDENTITY_CONVERTER) {
                    throw new UnknownFormatException("Unsupported file format: " + ff.format);
                } else {
                    File tmpFile = File.createTempFile("multi", ".ps");
                    tmpFile.deleteOnExit();
                    
                    FileOutputStream out = new FileOutputStream(tmpFile);
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
                    conv.convertToHylaFormat(ff.file, out, paperSize, targetFormat);
                    out.close();
                    
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
            FileInputStream in = new FileInputStream(target[0]);
            FileOutputStream out = new FileOutputStream(targetName);
            Utils.copyStream(in, out);
            in.close();
            out.close();
        } else {
            convertMultiplePSorPDFFiles(target, targetName, paperSize);
        }
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
                    (files.get(0).format == singleFileFormat.getFileFormat() || !alwaysCreateTargetFormat)) {
                files.get(0).view();
            } else {
                File tmpFile = File.createTempFile("view", "." + singleFileFormat.getFileFormat().getDefaultExtension());
                tmpFile.deleteOnExit();
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
