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
package yajhfc.shutdown;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import yajhfc.Utils;

/**
 * A shutdown handler that uses a signal handler in addition to the
 * normal shutdown hooks to detect shutdown. This is to work around a Java bug
 * especially on Windows Vista that prevents the normal shutdown hook from running.
 * 
 * @author jonas
 *
 */
public class Win32ShutdownManager extends ShutdownManager {
    protected final List<Runnable> runnables = new ArrayList<Runnable>();
    protected final Set<File> deleteOnExitFiles = new HashSet<File>();
    
    // Test code:
    static Writer shutdownLog;
    static final SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS: ", Locale.US);
    protected static void logShutdownMsg(String msg) {
        if (shutdownLog == null) {
            try {
                shutdownLog = new FileWriter(new File(Utils.getConfigDir(), "shutdown.log"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            shutdownLog.write(logFormat.format(new Date()));
            shutdownLog.write(msg);
            shutdownLog.write(System.getProperty("line.separator", "\n"));
            shutdownLog.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Win32ShutdownManager() {
        MySignalHandler.install("TERM", runnables);
        MySignalHandler.install("INT", runnables);
        
        registerShutdownHook(new Runnable() {
           public void run() {
               synchronized (deleteOnExitFiles) {
                   for (File f : deleteOnExitFiles) {
                       f.delete();
                   }
                   deleteOnExitFiles.clear();
               } 
           }
        });
    }
    
    @Override
    public void registerDeleteOnExit(File f) {
        synchronized (deleteOnExitFiles) {
            deleteOnExitFiles.add(f);
        }
    }

    @Override
    public void registerShutdownHook(Runnable run) {
        run = new SingleInvocationRunnable(run);
        
        synchronized(runnables) {
            runnables.add(run);
        }
        super.registerShutdownHook(run);
    }

    protected static class SingleInvocationRunnable implements Runnable {
        private final Runnable wrapped;
        private boolean started = false;
        
        public SingleInvocationRunnable(Runnable wrapped) {
            this.wrapped = wrapped;
        }
        
        public synchronized void run() {
            logShutdownMsg(getClass().getName() + ".run(): wrapped=" + wrapped + ",started=" + started);
            if (started)
                return;
            started = true;
            
            wrapped.run();
            logShutdownMsg(getClass().getName() + ".run(): wrapped=" + wrapped + "; DONE!");
        }
    }
}
