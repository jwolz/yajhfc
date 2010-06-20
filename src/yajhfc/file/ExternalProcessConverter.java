/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
import java.util.LinkedList;
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
        
        List<String> commandLineArgs = ExternalProcessExecutor.splitCommandLine(cmdLine);
        ExternalProcessExecutor.quoteCommandLine(commandLineArgs);
        if (Utils.debugMode) {
            log.fine("Invoking " + commandLineArgs.get(0) + " with the following command line:");
            for (String item : commandLineArgs) {
                log.fine(item);
            }
        }
        Process filter = new ProcessBuilder(commandLineArgs).start();

        final InputStream inputStream = filter.getInputStream();
        Utils.copyStream(inputStream, destination);
        
        BufferedReader errReader = new BufferedReader(new InputStreamReader(filter.getErrorStream()));
        String line;
        LinkedList<String> tail = new LinkedList<String>();
        while ((line = errReader.readLine()) != null) {
            log.info(commandLineArgs.get(0) + " output: " + line);
            tail.offer(line);
            while (tail.size() > 10) {
                tail.poll();
            }
        }
        errReader.close();
        filter.getOutputStream().close();
        try {
            int exitVal = filter.waitFor();
            if (exitVal != 0) {
                StringBuilder excText = new StringBuilder();
                if (exitVal != 111) // Magic value to suppress the printing of the header
                    excText.append("Non-zero exit code of ").append(commandLineArgs.get(0)).append(" (").append(exitVal).append("):\n");
                for (String text : tail) {
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
