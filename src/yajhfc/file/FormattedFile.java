package yajhfc.file;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.DesktopManager;
import yajhfc.Utils;
import yajhfc.shutdown.ShutdownManager;

public class FormattedFile {
    public final File file;
    public FileFormat format = FileFormat.Unknown;

    public FormattedFile(String fileName, FileFormat format) {
        this(new File(fileName), format);
    }
    
    public FormattedFile(File file, FileFormat format) {
        this.file = file;
        this.format = format;
    }
    
    public FormattedFile(String fileName) {
        this(new File(fileName));
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
        String execCmd;
        boolean useCustomViewer;

        switch (format) {
        case TIFF:
            execCmd = Utils.getFaxOptions().faxViewer;
            useCustomViewer = Utils.getFaxOptions().useCustomFaxViewer;
            break;
        case PostScript:
            execCmd = Utils.getFaxOptions().psViewer;
            useCustomViewer = Utils.getFaxOptions().useCustomPSViewer;
            break;
        case PDF:
            execCmd = Utils.getFaxOptions().pdfViewer;
            useCustomViewer = Utils.getFaxOptions().useCustomPDFViewer;
            break;
        default:
            throw new UnknownFormatException(MessageFormat.format(Utils._("File format {0} not supported."), format.toString()));
        }
        
        if (useCustomViewer) {
            Utils.startViewer(execCmd, file);
        } else {
            DesktopManager.getDefault().open(file);
        }
    }
    
    public void detectFormat() throws FileNotFoundException, IOException {
        format = detectFileFormat(file.getPath());
    }
    

    @Override
    public String toString() {
        return file + " [format=" + format.name() + "]";
    }
    
    @Override
    public int hashCode() {
        return file.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return file.equals(((FormattedFile)obj).file);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static fields & methods:
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //private static final short[] JPEGSignature = { 0xff, 0xd8, 0xff, 0xe0, -1, -1, 'J', 'F', 'I', 'F', 0 };
    private static final short[] JPEGSignature = { 0xff, 0xd8, 0xff }; //, -1, -1, -1, 'J', 'F', 'I', 'F', 0 };

    private static final short[] PNGSignature = { 137,  80,  78,  71,  13,  10,  26,  10 };

    private static final short[] GIFSignature1 = { 'G', 'I', 'F', '8', '9', 'a' };
    private static final short[] GIFSignature2 = { 'G', 'I', 'F', '8', '7', 'a' };

    private static final short[] TIFFSignature1 = { 'M', 'M', 0, 42 };
    private static final short[] TIFFSignature2 = { 'I', 'I', 42, 0 };

    private static final short[] PostScriptSignature = { '%', '!' };
    
    private static final short[] PJLSignature = { 27, '%', '-', '1', '2', '3', '4', '5', 'X', '@', 'P', 'J', 'L' };
    private static final short[] PJLSignature2 = { '@', 'P', 'J', 'L', ' ' };
    private static final Pattern PJL_EnterLangPattern = Pattern.compile("@PJL\\s+ENTER\\s+LANGUAGE\\s*=\\s*(\\w+)\\s*\r?\n", Pattern.CASE_INSENSITIVE);

    private static final short[] PDFSignature = { '%', 'P', 'D', 'F', '-' };

    private static final short[] PCLSignature = { 033, 'E', 033 };
    
    private static final short[] XMLSignature = { '<', '?', 'x', 'm', 'l', ' ' };

    private static final String ODTMimeString = "mimetypeapplication/vnd.oasis.opendocument.text";
    private static final short[] ODTSignature;
    static {
        final char[] odtMime = ODTMimeString.toCharArray();
        // See http://lists.oasis-open.org/archives/office/200505/msg00006.html
        ODTSignature = new short[30 + odtMime.length];
        ODTSignature[0] = 'P';
        ODTSignature[1] = 'K';
        ODTSignature[2] = 3;
        ODTSignature[3] = 4;
        for (int i = 4; i < 30; i++) {
            ODTSignature[i] = -1;
        }
        for (int i = 0; i < odtMime.length; i++) {
            ODTSignature[30+i] = (short)odtMime[i];
        }
    }
    
    private static final int maxSignatureLen = 4096;

    private static final Pattern FOPattern = Pattern.compile("<[^>]+?xmlns(?::\\w+)?=\"http://www\\.w3\\.org/1999/XSL/Format\"");
    
    public static FileFormat detectFileFormat(String fileName) throws FileNotFoundException, IOException {
        return detectFileFormat(new FileInputStream(fileName));
    }
    
    public static FileFormat detectFileFormat(File file) throws FileNotFoundException, IOException {
        return detectFileFormat(new FileInputStream(file));
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
            
            if (matchesSignature(data, PJLSignature) || matchesSignature(data, PJLSignature2)) {
                // Check the language entered
                String startOfFile = new String(data, "ISO-8859-1");
                Matcher m = PJL_EnterLangPattern.matcher(startOfFile);
                if (m.find()) {
                    String lang = m.group(1);
                    if ("POSTSCRIPT".equalsIgnoreCase(lang)) {
                        return FileFormat.PostScript;
                    } else if ("PCL".equalsIgnoreCase(lang)) {
                        return FileFormat.PCL;
                    } else {
                        return FileFormat.PJL;
                    }
                } else {
                    return FileFormat.PJL;
                }
            }
            
            String startOfFileLower;
            if (data[0] == (byte)0xef && data[1] == (byte)0xbb && data[2] == (byte)0xbf) { // Byte order mark (utf-8)
                startOfFileLower = new String(data, 3, 35, "UTF-8");
            } else {
                startOfFileLower = new String(data, 0, 32, "ISO-8859-1");
            }
            startOfFileLower = startOfFileLower.trim().toLowerCase();
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
                    return FileFormat.Any;
                if (b < 32) {
                    if (b != 10 && b != 13 && b != 9)
                        return FileFormat.Any;
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

    /**
     * Returns true if the given format can be viewed without conversion.
     * @param format
     * @return
     */
    public static boolean canViewFormat(FileFormat format) {
        switch (format) {
        case TIFF:
        case PostScript:
        case PDF:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * Returns a temporary file that has the correct extension given its format
     * @param tempFile
     * @return
     * @throws IOException
     */
    public static FormattedFile getTempFileWithCorrectExtension(File tempFile) throws IOException {
    	FileFormat ff = detectFileFormat(tempFile);
    	File file;
    	if (ff==FileFormat.Any || ff==FileFormat.Unknown) {
    		file = tempFile;
    	} else {
    		String fileName = tempFile.getName();
    		int pos = fileName.lastIndexOf('.');
    		String fileExt;
    		if (pos >= 0) {
    			fileExt = fileName.substring(pos+1);
    		} else {
    			fileExt = "";
    		}
    		
    		boolean matches = false;
    		for (String ext : ff.getPossibleExtensions()) {
    			if (ext.equalsIgnoreCase(fileExt)) {
    				matches = true;
    				break;
    			}
    		}
    		
    		if (matches) {
    			file=tempFile;
    		} else {
    			File newTempFile = File.createTempFile("temp"+ff.name(), "."+ff.getDefaultExtension());
    			ShutdownManager.deleteOnExit(newTempFile);
    			file = newTempFile;
    			if (!tempFile.renameTo(newTempFile)) {
    				// First try overwriting the file, if that fails, try deleting it first...
    				newTempFile.delete();
    				if (!tempFile.renameTo(newTempFile)) {
    					// If everything fails, keep the old file
    					file = tempFile;
    				}
    			}
    		}
    	}
    	return new FormattedFile(file, ff);
    }
    
    public static void main(String[] args) throws Exception {
        for (String file : args) {
            System.out.println(file + ": " + detectFileFormat(file));
        }
    }
}
