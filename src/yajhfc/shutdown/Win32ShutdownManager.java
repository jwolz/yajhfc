/**
 * 
 */
package yajhfc.shutdown;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    }

    @Override
    public void registerShutdownHook(Runnable run) {
        run = new SingleInvocationRunnable(run);
        
        runnables.add(run);
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
