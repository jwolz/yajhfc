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
package yajhfc.printerport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.util.ExternalProcessExecutor;

/**
 * Unix named pipe implementation (i.e. file created by mkfifo command)
 * 
 * @author jonas
 *
 */
public class UnixFIFO extends FIFO {
    private static final Logger log = Logger.getLogger(UnixFIFO.class.getName());

    protected File fifo;
    
    public UnixFIFO(String fifoName) throws IOException, InterruptedException {
        super(fifoName);
        createUnixFIFO(fifoName);
    }

    private void createUnixFIFO(String fifoName) throws IOException, InterruptedException {
        fifo = new File(fifoName);
        
        final String mkfifo = EntryPoint.getOptions().mkfifo;
        if (fifo.exists())
            fifo.delete();
        
        log.fine("Executing \"" + mkfifo + " " + fifo + "\".");
        List<String> commandLine = ExternalProcessExecutor.splitCommandLine(mkfifo);
        commandLine.add(fifo.getPath());
        ExternalProcessExecutor.quoteCommandLine(commandLine);
        
        Process process = new ProcessBuilder(commandLine)
                .redirectErrorStream(true).start();
        process.getOutputStream().close();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info(mkfifo + " output: " + line);
        }
        reader.close();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException(mkfifo + " failed with exit code " + exitCode);
        }
        log.fine("FIFO created successfully.");
        yajhfc.shutdown.ShutdownManager.deleteOnExit(fifo);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.printerport.FIFO#openInputStream()
     */
    @Override
    public InputStream openInputStream() throws IOException {
        return new FileInputStream(fifo);
    }

    /* (non-Javadoc)
     * @see yajhfc.printerport.FIFO#close()
     */
    @Override
    public void close() {
        fifo.delete();
    }

}
