/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.printerport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.launch.SendWinSubmitProtocol;
import yajhfc.launch.SubmitProtocol;
import yajhfc.util.ExternalProcessExecutor;

/**
 * @author jonas
 *
 */
public class FIFOThread extends Thread {
    private static final Logger log = Logger.getLogger(ListenThread.class.getName());

    protected File fifo;
    
    public FIFOThread(String fifoName) {
        super("PrinterFIFO-" + fifoName);
        fifo = new File(fifoName);
    }

    private boolean createFIFO() {
        try {
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
                log.warning(mkfifo + " failed with exit code " + exitCode);
                return false;
            }
            log.fine("FIFO created successfully.");
            fifo.deleteOnExit();
            return fifo.exists();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error executing mkfifo:", e);
            return false;
        }
    }
    
    public void close() {
        interrupt();
        fifo.delete();
    }
    
    @Override
    public void run() {
        if (!createFIFO()) {
            log.severe("Could not create FIFO, not created printer port");
            return;
        }
        try {
            while (!isInterrupted()) {
                PushbackInputStream inStream = new PushbackInputStream(new FileInputStream(fifo));
                int b = inStream.read();
                if (b != -1) {
                    inStream.unread(b);
                    SubmitProtocol sp = new SendWinSubmitProtocol();
                    sp.setInputStream(inStream, fifo.toString());
                    sp.submit(true);
                }
                inStream.close();               
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Error waiting for a document to be printed:", e);
        } finally {
            close();
        }
    }

}
