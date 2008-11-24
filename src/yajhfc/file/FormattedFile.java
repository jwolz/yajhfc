package yajhfc.file;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.print.DocFlavor;
import javax.swing.filechooser.FileFilter;

import yajhfc.Utils;
import yajhfc.util.ExampleFileFilter;

public class FormattedFile {
    public enum FileFormat {
        PostScript(Utils._("Postscript documents"), "ps"),
        PDF(Utils._("PDF documents"), "pdf"),
        PCL(Utils._("PCL files"), "pcl"),
        JPEG(Utils._("JPEG pictures"), "jpeg", "jpg"),
        PNG(Utils._("PNG pictures"),"png"),
        GIF(Utils._("GIF pictures"),"gif"),
        TIFF(Utils._("TIFF pictures"),"tiff", "tif"),
        PlainText(Utils._("Plain text files"),"txt"),
        XML(Utils._("XML documents"), "xml"),
        FOP(Utils._("XSL:FO documents"), "fo", "xml", "fop"),
        ODT(Utils._("OpenDocument text documents"), "odt"),
        HTML(Utils._("HTML documents"), "html", "htm"),
        RTF(Utils._("RTF documents"), "rtf"),
        Unknown(Utils._("Unknown files"), "");
        
        private String defaultExt;
        private String[] possibleExts;
        private String description;
        
        private FileFormat(String description, String... possibleExts) {
            this(description, possibleExts[0], possibleExts);
        }
        
        private FileFormat(String description, String defaultExt, String[] possibleExts) {
            this.defaultExt = defaultExt;
            this.possibleExts = possibleExts;
            this.description = description;
        }
        
        public String getDefaultExtension() {
            return defaultExt;
        }
        
        public String getDescription() {
            return description;
            //return MessageFormat.format(Utils._("{0} files"), toString());
        }
        
        public String[] getPossibleExtensions() {
            return possibleExts;
        }
    }
    
    
    public File file = null;
    public FileFormat format = FileFormat.Unknown;

    public FormattedFile(String fileName, FileFormat format) {
        this(new File(fileName), format);
    }
    
    public FormattedFile(File file, FileFormat format) {
        this.file = file;
        this.format = format;
    }
    
    public FormattedFile(File file) {
        this.file = file;
        try {
            detectFormat();
        } catch (Exception e) {
            format = FileFormat.Unknown;
        }
    }
    
    public void view() throws IOException, UnknownFormatException {
        viewFile(file.getPath(), format);
    }
    
    public void detectFormat() throws FileNotFoundException, IOException {
        format = detectFileFormat(file.getPath());
    }
    


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static fields & methods:
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static final Map<FileFormat,FileConverter> fileConverters = new EnumMap<FileFormat, FileConverter>(FileFormat.class);
    static {
        fileConverters.put(FileFormat.PostScript, FileConverter.IDENTITY_CONVERTER);
        fileConverters.put(FileFormat.PDF, FileConverter.IDENTITY_CONVERTER);
        fileConverters.put(FileFormat.TIFF, FileConverter.IDENTITY_CONVERTER);
        
        fileConverters.put(FileFormat.PNG, new PrintServiceFileConverter(DocFlavor.URL.PNG));
        fileConverters.put(FileFormat.GIF, new PrintServiceFileConverter(DocFlavor.URL.GIF));
        fileConverters.put(FileFormat.JPEG, new PrintServiceFileConverter(DocFlavor.URL.JPEG));
        
        fileConverters.put(FileFormat.HTML, EditorPaneFileConverter.HTML_CONVERTER);
        // Doesn't work very well
        //fileConverters.put(FileFormat.RTF, new EditorPaneFileConverter("text/rtf"));
    }
    
    //private static final short[] JPEGSignature = { 0xff, 0xd8, 0xff, 0xe0, -1, -1, 'J', 'F', 'I', 'F', 0 };
    private static final short[] JPEGSignature = { 0xff, 0xd8, 0xff }; //, -1, -1, -1, 'J', 'F', 'I', 'F', 0 };

    private static final short[] PNGSignature = { 137,  80,  78,  71,  13,  10,  26,  10 };

    private static final short[] GIFSignature1 = { 'G', 'I', 'F', '8', '9', 'a' };
    private static final short[] GIFSignature2 = { 'G', 'I', 'F', '8', '7', 'a' };

    private static final short[] TIFFSignature1 = { 'M', 'M', 0, 42 };
    private static final short[] TIFFSignature2 = { 'I', 'I', 42, 0 };

    private static final short[] PostScriptSignature = { '%', '!' };

    private static final short[] PDFSignature = { '%', 'P', 'D', 'F', '-' };

    private static final short[] PCLSignature = { 033, 'E', 033 };
    
    private static final short[] XMLSignature = { '<', '?', 'x', 'm', 'l', ' ' };

    private static final String ODTMimeString = "mimetypeapplication/vnd.oasis.opendocument.text";
    private static final short[] ODTSignature;
    static {
        // See http://lists.oasis-open.org/archives/office/200505/msg00006.html
        ODTSignature = new short[30 + ODTMimeString.length()];
        ODTSignature[0] = 'P';
        ODTSignature[1] = 'K';
        ODTSignature[2] = 3;
        ODTSignature[3] = 4;
        for (int i = 4; i < 30; i++) {
            ODTSignature[i] = -1;
        }
        for (int i = 0; i < ODTMimeString.length(); i++) {
            ODTSignature[30+i] = (short)ODTMimeString.charAt(i);
        }
    }
    
    private static final int maxSignatureLen = 4096;

    private static final Pattern FOPattern = Pattern.compile("<[^>]+?xmlns(?::\\w+)?=\"http://www\\.w3\\.org/1999/XSL/Format\"");
    
    public static FileFormat detectFileFormat(String fileName) throws FileNotFoundException, IOException {
        return detectFileFormat(new FileInputStream(fileName));
    }
    
    /**
     * Detects the file format of the given InputStream and closes it afterwards
     * @param fIn
     * @return
     * @throws IOException
     */
    public static FileFormat detectFileFormat(InputStream fIn) throws IOException {
        try {
            byte[] data = new byte[maxSignatureLen];
            int bytesRead = fIn.read(data);

            if (matchesSignature(data, JPEGSignature))
                return FileFormat.JPEG;

            if (matchesSignature(data, PNGSignature))
                return FileFormat.PNG;

            if (matchesSignature(data, GIFSignature1) || matchesSignature(data, GIFSignature2))
                return FileFormat.GIF;

            if (matchesSignature(data, TIFFSignature1) || matchesSignature(data, TIFFSignature2))
                return FileFormat.TIFF;

            if (matchesSignature(data, PDFSignature))
                return FileFormat.PDF;

            if (matchesSignature(data, PostScriptSignature))
                return FileFormat.PostScript;

            if (matchesSignature(data, PCLSignature))
                return FileFormat.PCL;
            
            if (matchesSignature(data, ODTSignature)) {
                return FileFormat.ODT;
            }
            
            if (matchesSignature(data, XMLSignature)) {
                // Check if there is a namespace definition for FOP
                String startOfFile = new String(data, "UTF-8");
                if (FOPattern.matcher(startOfFile).find()) {
                    return FileFormat.FOP;
                } else {
                    return FileFormat.XML;
                }
            }
            
            String startOfFileLower = new String(data, 0, 32, "ISO-8859-1").toLowerCase();
            if (startOfFileLower.startsWith("<html") || startOfFileLower.startsWith("<!doctype html") ||
                startOfFileLower.startsWith("<head") || startOfFileLower.startsWith("<title")) {
                return FileFormat.HTML;
            }
            
            if (startOfFileLower.startsWith("{\\rtf")) {
                return FileFormat.RTF;
            }
            
            for (int i = 0; i < bytesRead; i++) {
                int b = data[i] & 0xff;

                if (b == 127)
                    return FileFormat.Unknown;
                if (b < 32) {
                    if (b != 10 && b != 13 && b != 9)
                        return FileFormat.Unknown;
                }
            }
            return FileFormat.PlainText;
        } finally {
            fIn.close();
        }
    }   
    
    /**
     * Checks if the first bytes of data equal those given in signature.
     * @param raf The file to check
     * @param signature The signature to check against. A value of -1 for a byte means "don't care".
     * @return true if the signature matches.
     * @throws IOException
     */
    private static boolean matchesSignature(byte[] data, short[] signature){       
        for (int i = 0; i < signature.length; i++) {
            short s = signature[i];
            if (s >= 0 && s <= 255) {
                int b = data[i] & 0xff; // If data[i] is negative this ensures we get the corresponding positive byte value 
                
                if (b != s)
                    return false;
            }
        }
        return true;
    }

    public static void viewFile(String fileName, FileFormat format) throws UnknownFormatException, IOException {
        String execCmd;

        switch (format) {
        case TIFF:
            execCmd = Utils.getFaxOptions().faxViewer;
            break;
        case PostScript:
        case PDF:
            execCmd = Utils.getFaxOptions().psViewer;
            break;
        default:
            throw new UnknownFormatException(MessageFormat.format(Utils._("File format {0} not supported."), format.toString()));
        }


        if (execCmd.indexOf("%s") >= 0)
            execCmd = execCmd.replace("%s", fileName);
        else
            execCmd += " " + fileName;

        Runtime.getRuntime().exec(execCmd);
    }

//  protected static final FileFormat[] acceptedFormats = {
//  FileFormat.PostScript, FileFormat.PDF, FileFormat.JPEG, FileFormat.GIF, FileFormat.PNG, FileFormat.TIFF
//  };
    protected static FileFilter[] acceptedFilters;

    public static FileFilter[] createFileFiltersFromFormats(Collection<FileFormat> formats) {
        ArrayList<String> allExts = new ArrayList<String>();
        FileFilter[] filters = new FileFilter[formats.size() + 1];
        int i = 0;
        for (FileFormat ff : formats) {
            for (String ext : ff.getPossibleExtensions()) {
                allExts.add(ext);
            }
            filters[++i] = new ExampleFileFilter(ff.getPossibleExtensions(), ff.getDescription());
        }
        ExampleFileFilter allSupported = new ExampleFileFilter(allExts.toArray(new String[allExts.size()]), Utils._("All supported file formats"));
        allSupported.setExtensionListInDescription(false);
        filters[0] = allSupported;
        
        return filters;
    }
    
    public static FileFilter[] getConvertableFileFilters() {
        if (acceptedFilters == null) {
            acceptedFilters = createFileFiltersFromFormats(fileConverters.keySet());
        }
        return acceptedFilters;
    }

    public static void main(String[] args) throws Exception {
        for (String file : args) {
            System.out.println(file + ": " + detectFileFormat(file));
        }
    }
}
