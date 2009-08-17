/**
 * 
 */
package yajhfc.shutdown;

import java.util.ArrayList;
import java.util.List;

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
            if (started)
                return;
            started = true;
            
            wrapped.run();
        }
    }
}
