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
import java.io.InputStreamReader;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FormattedFile.FileFormat;

/**
 * @author jonas
 *
 */
public class PDFMultiFileConverter extends MultiFileConverter {

    private static final Logger log = Logger.getLogger(PDFMultiFileConverter.class.getName());
    
    private static final String[] additionalGSParams = {
        "-dPDFSETTINGS=/default",
        "-dDownsampleColorImages=true",
        "-dDownsampleGrayImages=true",
        "-dDownsampleMonoImages=true",
        "-dMonoImageDownsampleType=/Bicubic",
        "-dGrayImageDownsampleType=/Bicubic",
        "-dColorImageDownsampleType=/Bicubic",
        "-dMonoImageResolution=196",
        "-dGrayImageResolution=196",
        "-dColorImageResolution=196",        
        "-dOptimize=true",
        "-c",
        ".setpdfwrite"
    };
    
    protected static final String[] defaultGSParams = {
        "-q",
        "-dBATCH",
        "-dSAFER",
        "-dNOPAUSE"
    };
    
    
    /* (non-Javadoc)
     * @see yajhfc.file.MultiFileConverter#convertMultiplePSorPDFFiles(java.io.File[], java.io.File)
     */
    @Override
    public void convertMultiplePSorPDFFiles(File[] files, File targetFile, PaperSize paperSize)
            throws IOException, ConversionException {
        String gsPath = Utils.getFaxOptions().ghostScriptLocation;
        
        String[] additionalParams = getAdditionalGSParams();
        String[] cmdList =  new String[5 + additionalParams.length + defaultGSParams.length + 2*files.length];
        int listIndex = 0;
        
        cmdList[listIndex++] = gsPath;
        System.arraycopy(defaultGSParams, 0, cmdList, listIndex, defaultGSParams.length);
        listIndex += defaultGSParams.length;
        cmdList[listIndex++] = "-sDEVICE=" + getGSDevice();
        cmdList[listIndex++] = "-sOutputFile=" + targetFile.getAbsolutePath();
        cmdList[listIndex++] = "-sPAPERSIZE=" + paperSize.name().toLowerCase();
        cmdList[listIndex++] = calcResolution(paperSize);
        System.arraycopy(additionalParams, 0, cmdList, listIndex, additionalParams.length);
        listIndex += additionalParams.length;
        for (File file : files) {
            cmdList[listIndex++] = "-f";
            cmdList[listIndex++] = file.getAbsolutePath();
        }
        
        if (Utils.debugMode) {
            log.fine("Ghostscript command line:");
            for (String cmd : cmdList) {
                log.fine(cmd);
            }
        }
        
        Process gs = new ProcessBuilder(cmdList).redirectErrorStream(true).start();
        gs.getOutputStream().close();
        BufferedReader bufR = new BufferedReader(new InputStreamReader(gs.getInputStream()));
        String line;
        while ((line = bufR.readLine()) != null) {
            log.info("gs output: " + line);
        }
        bufR.close();
        try {
            int exitVal = gs.waitFor();
            if (exitVal != 0) {
                throw new ConversionException("Non-zero exit code of GhostScript (" + exitVal + ")");
            }
        } catch (InterruptedException e) {
            throw new ConversionException(e);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.file.MultiFileConverter#getTargetFormat()
     */
    @Override
    public FileFormat getTargetFormat() {
        return FileFormat.PDF;
    }

    /**
     * Returns the GhostScript "-r" parameter
     * @param paperSize
     * @return
     */
    protected String calcResolution(PaperSize paperSize) {
        return "-r196";
    }
    
    protected String[] getAdditionalGSParams() {
        return additionalGSParams;
    }
    
    protected String getGSDevice() {
        return "pdfwrite";
    }
}
