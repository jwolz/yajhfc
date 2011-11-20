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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.util.ExternalProcessExecutor;

/**
 * @author jonas
 *
 */
public class PDFMultiFileConverter extends MultiFileConverter {

    /**
     * The number of lines of error output to show in the message to the user
     */
    private static final int LINES_OF_ERROR_OUTPUT = 20;

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
        List<String> cmdList = new ArrayList<String>(5 + additionalParams.length + defaultGSParams.length + 2*files.length);
        
        cmdList.add(gsPath);
        for (String param : defaultGSParams) {
            cmdList.add(param);
        }
        cmdList.add("-sDEVICE=" + getGSDevice());
        cmdList.add("-sOutputFile=" + targetFile.getAbsolutePath());
        cmdList.add("-sPAPERSIZE=" + paperSize.name().toLowerCase());
        cmdList.add(calcResolution(paperSize));
        for (String param : additionalParams) {
            cmdList.add(param);
        }
        for (File file : files) {
            cmdList.add("-f");
            cmdList.add(file.getAbsolutePath());
        }
        
        ExternalProcessExecutor.quoteCommandLine(cmdList);
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
        LinkedList<String> tail = new LinkedList<String>();
        while ((line = bufR.readLine()) != null) {
            log.info("gs output: " + line);
            tail.offer(line);
            while (tail.size() > LINES_OF_ERROR_OUTPUT) {
                tail.poll();
            }
        }
        bufR.close();
        try {
            int exitVal = gs.waitFor();
            if (exitVal != 0) {
                StringBuilder excText = new StringBuilder();
                excText.append("Non-zero exit code of GhostScript (").append(exitVal).append("):\n");
                for (String text : tail) {
                    excText.append(text).append('\n');
                }
                throw new ConversionException(excText.toString());
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
