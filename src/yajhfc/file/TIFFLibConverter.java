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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.util.ExternalProcessExecutor;

/**
 * @author jonas
 *
 */
public class TIFFLibConverter implements FileConverter {
    private static final Logger log = Logger.getLogger(TIFFLibConverter.class.getName());
    
    protected static final String[] tiff2pdfParams = {
    };

    protected static final String[] tiff2psParams = {
        "-a1",
    };
    
    /* (non-Javadoc)
     * @see yajhfc.file.FileConverter#convertToHylaFormat(java.io.File, java.io.OutputStream, yajhfc.PaperSize)
     */
    public void convertToHylaFormat(File inFile, OutputStream destination,
            PaperSize paperSize, FileFormat desiredFormat) throws ConversionException, IOException {
        
        List<String> commandLine = getCommandLine(desiredFormat, inFile);
        ExternalProcessExecutor.quoteCommandLine(commandLine);
        if (Utils.debugMode) {
            log.fine("Invoking tiff2pdf with the following command line:");
            for (String item : commandLine) {
                log.fine(item);
            }
        }
        Process tiff2PDF = new ProcessBuilder(commandLine).start();
        //tiff2PDF.getOutputStream().close();

        final InputStream inputStream = tiff2PDF.getInputStream();
        if (desiredFormat == FileFormat.PDF) {
            // Work around a strange bug which results in garbage (a TIFF signature followed by NULs)
            // at the beginning of the stream
            final byte[] buf = new byte[1000];
            int readLen;
            int buflen = readLen = inputStream.read(buf);

            while (readLen >=0 && buflen < 3) {
                readLen = inputStream.read(buf, buflen, buf.length-buflen);
                if (readLen > 0)
                    buflen += readLen;
            }
            if (readLen < 0 || buflen < 0)
                throw new IOException("Premature end of stream reading stdout from tiff2pdf (readLen=" + readLen + ", buflen=" + buflen + ").");
            
            if (buf[0] == 'I' && buf[1] == 'I' && buf[2] == '*') {
                int offset = 3;
                readLoop: 
                    do {
                        for (int i=offset; i<buflen; i++) {
                            if (buf[i] != 0) { //Strip everything up to the first non-NUL char
                                destination.write(buf, i, buflen-i);
                                break readLoop;
                            }
                        }
                        offset = 0;
                        buflen = inputStream.read(buf);
                    } while (buflen >= 0);
            } else {
                destination.write(buf, 0, buflen);
            }
        }
        Utils.copyStream(inputStream, destination);
        
        BufferedReader errReader = new BufferedReader(new InputStreamReader(tiff2PDF.getErrorStream()));
        String line;
        while ((line = errReader.readLine()) != null) {
            log.info(commandLine.get(0) + " output: " + line);
        }
        errReader.close();
        try {
            int exitVal = tiff2PDF.waitFor();
            if (exitVal != 0) {
                throw new ConversionException("Non-zero exit code of tiff2pdf (" + exitVal + ")");
            }
        } catch (InterruptedException e) {
            throw new ConversionException(e);
        }

    }
    
    protected List<String> getCommandLine(FileFormat targetFormat, File input) throws ConversionException {
        List<String> commandLine = new ArrayList<String>();
        switch (targetFormat) {
        case PDF:
            commandLine.add(Utils.getFaxOptions().tiff2PDFLocation);
            for (String opt : tiff2pdfParams) {
                commandLine.add(opt);
            }
            commandLine.add(input.getAbsolutePath());
            break;
        case PostScript:
        default:
            // Try to find tiff2ps by the simple minded logic that it will be in the same directory as tiff2pdf
            File tiff2ps = Utils.searchExecutableInPath(new File(new File(Utils.getFaxOptions().tiff2PDFLocation).getParentFile(), "tiff2ps").getPath());
            if (tiff2ps == null) {
                // Use tiff2pdf instead
                return getCommandLine(FileFormat.PDF, input);
            }
            commandLine.add(tiff2ps.getPath());
            for (String opt : tiff2psParams) {
                commandLine.add(opt);
            }
            commandLine.add(input.getAbsolutePath());
            break;
//        default:
//            throw new ConversionException("Unsupported file format: " + targetFormat);
        }
        return commandLine;
    }

}
