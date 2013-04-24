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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class MySignalHandler implements SignalHandler {
    private static final Logger log = Logger.getLogger(MySignalHandler.class.getName());
    
    private SignalHandler oldHandler;
    protected final List<Runnable> listToRun;

    public static SignalHandler install(String signalName, List<Runnable> listToRun) {
        log.info("Installing signal handler for signal " + signalName);
        Signal diagSignal = new Signal(signalName);
        MySignalHandler instance = new MySignalHandler(listToRun);
        instance.oldHandler = Signal.handle(diagSignal, instance);
        log.fine("Installed signal handler " + instance + "(prev: " + instance.oldHandler + ")");
        return instance;
    }

    public void handle(Signal signal) {
        log.fine("Signal handler called for signal " + signal);
        Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".handle(): signal=" + signal);
        try {

            signalAction(signal);

            // Chain back to previous handler, if one exists
            if (oldHandler != SIG_DFL && oldHandler != SIG_IGN) {
                Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".handle(): signal=" + signal + "; CHAINING");
                oldHandler.handle(signal);
            }

        } catch (Exception e) {
            log.log(Level.WARNING, "Signal handler failed", e);
            Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".handle(): signal=" + signal + ": " + e);
        }
        Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".handle(): signal=" + signal + "; DONE");
    }

    public void signalAction(Signal signal) {
        log.fine("Running runnables...");
        Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".signalAction(): signal=" + signal);
        for (Runnable run : listToRun) {
            try {
                run.run();
            } catch (Throwable t) {
                log.log(Level.WARNING, "Error running runnable", t);
                Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".signalAction(): signal=" + signal + ": " + t);
            }
        }
        Win32ShutdownManager.logShutdownMsg(getClass().getName() + ".signalAction(): signal=" + signal + "; END!");
        log.fine("Runnables ran.");
    }

    private MySignalHandler(List<Runnable> listToRun) {
        super();
        this.listToRun = listToRun;
    }
}
