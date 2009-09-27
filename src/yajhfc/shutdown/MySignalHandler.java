package yajhfc.shutdown;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

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
