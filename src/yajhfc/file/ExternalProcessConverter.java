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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.util.ExternalProcessExecutor;

/**
 * @author jonas
 *
 */
public class ExternalProcessConverter implements FileConverter {
    private static final Logger log = Logger.getLogger(ExternalProcessConverter.class.getName());
    
    protected String commandLine;
    protected final FileConverter internalConverter;

    /* (non-Javadoc)
     * @see yajhfc.file.FileConverter#convertToHylaFormat(java.io.File, java.io.OutputStream, yajhfc.PaperSize, yajhfc.file.FileFormat)
     */
    public void convertToHylaFormat(File inFile, OutputStream destination,
            PaperSize paperSize, FileFormat desiredFormat) throws ConversionException, IOException {
        
        /*
         * Supported arguments:
         * %s the file to convert
         * %p the name of the paper size
         * %h the height of the paper in tenths of mm
         * %w the width of the paper in tenths of mm
         * %F the desired output format (only a hint)
         */
        String cmdLine = addArgument(commandLine, "%p", paperSize.name(), false);
        cmdLine = addArgument(cmdLine, "%h", paperSize.getSize().height, false);
        cmdLine = addArgument(cmdLine, "%w", paperSize.getSize().width, false);
        cmdLine = addArgument(cmdLine, "%F", desiredFormat.name(), false);
        cmdLine = addArgument(cmdLine, "%s", inFile.getAbsolutePath(), true);
        
        final List<String> commandLineArgs = ExternalProcessExecutor.splitCommandLine(cmdLine);
        ExternalProcessExecutor.quoteCommandLine(commandLineArgs);
        if (Utils.debugMode) {
            log.fine("Invoking " + commandLineArgs.get(0) + " with the following command line:");
            for (String item : commandLineArgs) {
                log.fine(item);
            }
        }
        final Process filter = new ProcessBuilder(commandLineArgs).start();

        StdErrThread errLogger = new StdErrThread(commandLineArgs.get(0), filter.getErrorStream());
        
        final InputStream inputStream = filter.getInputStream();
        Utils.copyStream(inputStream, destination);

        filter.getOutputStream().close();
        try {
            int exitVal = filter.waitFor();
            if (exitVal != 0) {
                StringBuilder excText = new StringBuilder();
                if (exitVal != 111) // Magic value to suppress the printing of the header
                    excText.append("Non-zero exit code of ").append(commandLineArgs.get(0)).append(" (").append(exitVal).append("):\n");
                for (String text : errLogger.getTail()) {
                    excText.append(text).append('\n');
                }
                throw new ConversionException(excText.toString());
            }
        } catch (InterruptedException e) {
            throw new ConversionException(e);
        }

    }

    private String addArgument(String input, String argName, Object value, boolean alwaysAdd) {
        if (input.contains(argName)) {
            return input.replace(argName, value.toString());
        } else {
            if (alwaysAdd) {
                return input + " \"" + value + '\"';
            } else {
                return input;
            }
        }
    }
    
    public boolean isOverridable() {
        return true;
    }
    
    public FileConverter getInternalConverter() {
        return internalConverter;
    }
    
    public String getCommandLine() {
        return commandLine;
    }
    
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }
    
    /**
     * Returns if this converter has been specified by the user
     * @return
     */
    public boolean isUserDefined() {
        return true;
    }

    public ExternalProcessConverter(String commandLine,
            FileConverter internalConv) {
        super();
        this.commandLine = commandLine;
        this.internalConverter = internalConv;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + "[commandLine=" + commandLine + ";internalConverter=" + internalConverter + "]";
    }
}
