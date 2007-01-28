package yajhfc;
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
import java.text.MessageFormat;

public class FormattedFile {
    
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
    

    public enum FileFormat {
        PostScript("ps"), PDF("pdf"), PCL("pcl"), JPEG("jpeg"), PNG("png"), GIF("gif"), TIFF("tiff"), PlainText("txt"), Unknown("");
        
        private String defaultExt;
        
        private FileFormat(String defaultExt) {
            this.defaultExt = defaultExt;
        }
        
        public String getDefaultExtension() {
            return defaultExt;
        }
        
        public String getDescription() {
            return MessageFormat.format(utils._("{0} files"), toString());
        }
    }

    // Static methods:
    public static FileFormat detectFileFormat(String fileName) throws FileNotFoundException, IOException {
        FileInputStream fIn = new FileInputStream(fileName);
        byte[] data = new byte[maxSignatureLen];
        fIn.read(data);
        fIn.close();

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

        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xff;
            
            if (b == 127)
                return FileFormat.Unknown;
            if (b < 32) {
                if (b != 10 && b != 13 && b != 9)
                    return FileFormat.Unknown;
            }
        }
        return FileFormat.PlainText;
    }   
    private static final short[] JPEGSignature = { 0xff, 0xd8, 0xff, 0xe0, -1, -1, 'J', 'F', 'I', 'F', 0 };

    private static final short[] PNGSignature = { 137,  80,  78,  71,  13,  10,  26,  10 };

    private static final short[] GIFSignature1 = { 'G', 'I', 'F', '8', '9', 'a' };
    private static final short[] GIFSignature2 = { 'G', 'I', 'F', '8', '7', 'a' };

    private static final short[] TIFFSignature1 = { 'M', 'M', 0, 42 };
    private static final short[] TIFFSignature2 = { 'I', 'I', 42, 0 };

    private static final short[] PostScriptSignature = { '%', '!' };

    private static final short[] PDFSignature = { '%', 'P', 'D', 'F', '-' };

    private static final short[] PCLSignature = { 033, 'E', 033 };

    private static final int maxSignatureLen = 256;


    /**
     * Checks if the first bytes of raf equal those given in signature.
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
            execCmd = utils.getFaxOptions().faxViewer;
            break;
        case PostScript:
        case PDF:
            execCmd = utils.getFaxOptions().psViewer;
            break;
        default:
            throw new UnknownFormatException(MessageFormat.format(utils._("File format {0} not supported."), format.toString()));
        }


        if (execCmd.indexOf("%s") >= 0)
            execCmd = execCmd.replace("%s", fileName);
        else
            execCmd += " " + fileName;

        Runtime.getRuntime().exec(execCmd);
    }


}
