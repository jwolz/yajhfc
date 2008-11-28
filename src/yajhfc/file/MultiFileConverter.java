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
package yajhfc.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FormattedFile.FileFormat;

/**
 * @author jonas
 *
 */
public abstract class MultiFileConverter {
   
    public abstract FileFormat getTargetFormat();
    
    public abstract void convertMultiplePSorPDFFiles(File[] files, File targetFile, PaperSize paperSize) throws IOException, ConversionException;
    
    public void convertMultipleFiles(FormattedFile[] files, File targetName, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        File[] target = new File[files.length];
        
        for (int i=0; i< files.length; i++) {
            FormattedFile ff = files[i];
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
                    conv.convertToHylaFormat(ff.file, out, paperSize);
                    out.close();
                    target[i] = tmpFile;
                }
            }
        }
        convertMultiplePSorPDFFiles(target, targetName, paperSize);
    }
    
    public static final Map<FileFormat,MultiFileConverter> targetFormats = new EnumMap<FileFormat,MultiFileConverter>(FileFormat.class);
    static {
        targetFormats.put(FileFormat.PDF, new PDFMultiFileConverter());
        targetFormats.put(FileFormat.PostScript, new PSMultiFileConverter());
    }
    
    /**
     * Views multiple files
     * @param files
     * @throws UnknownFormatException 
     * @throws IOException 
     * @throws ConversionException 
     */
    public static void viewMultipleFiles(FormattedFile[] files, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        if (!Utils.getFaxOptions().createSingleFileForViewing || files.length <= 1) {
            for (FormattedFile file: files) {
                file.view();
            }
        } else {
            File tmpFile = File.createTempFile("view", "." + Utils.getFaxOptions().singleFileFormat.getDefaultExtension());
            tmpFile.deleteOnExit();
            FormattedFile ff = convertMultipleFilesToSingleFile(files, tmpFile, Utils.getFaxOptions().singleFileFormat, paperSize);
            ff.view();
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
    public static FormattedFile convertMultipleFilesToSingleFile(FormattedFile[] files, File targetName, FileFormat targetFormat, PaperSize paperSize) throws IOException, UnknownFormatException, ConversionException {
        MultiFileConverter conv = targetFormats.get(targetFormat);
        if (conv == null) {
            throw new UnknownFormatException("Unsupported target format: " + targetFormat);
        } else {
            conv.convertMultipleFiles(files, targetName, paperSize);
            return new FormattedFile(targetName, conv.getTargetFormat());
        }
    }

    public static void main(String[] args) throws IOException, UnknownFormatException, ConversionException {
        FormattedFile[] files = {
          new FormattedFile(new File("/home/jonas/test.ps")),
          new FormattedFile(new File("/home/jonas/Karte.png")),
          new FormattedFile(new File("/home/jonas/timeline.pdf")),
        };
        
        File out = new File("/tmp/out.pdf");
        convertMultipleFilesToSingleFile(files, out, FileFormat.PDF, PaperSize.A4);
        
        Runtime.getRuntime().exec(new String[] {"kpdf", out.getPath()});
        
        out = new File("/tmp/out.ps");
        convertMultipleFilesToSingleFile(files, out, FileFormat.PostScript, PaperSize.A4);
        
        Runtime.getRuntime().exec(new String[] {"gv", out.getPath()});
    }
}
