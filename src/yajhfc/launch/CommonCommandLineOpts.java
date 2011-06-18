package yajhfc.launch;

import java.util.ArrayList;
import java.util.List;

import yajhfc.plugin.PluginManager.PluginInfo;

public abstract class CommonCommandLineOpts {
    /**
     * Plugins to use. Communicated over command line.
     */
    public final List<PluginInfo> plugins = new ArrayList<PluginInfo>();
    /**
     * Overridden settings. Communicated over command line.
     */
    public final StringBuilder overrideSettings = new StringBuilder();
    /**
     * Do not load plugin.lst. Communicated over command line.
     */
    public boolean noPlugins = false;
    /**
     * Log file. Communicated over command line.
     */
    public String logFile = null;
    /**
     * Append to log file? Communicated over command line.
     */
    public boolean appendToLog = false;
    /**
     * Config dir. Communicated over command line.
     */
    public String configDir = null;
    /**
     * Location where the job id of newly created fax jobs should be written to.
     * Not communicated.
     */
    public String jobIDOutput = null;
    /**
     * Debug mode? Communicated over command line.
     */
    public boolean debugMode = false;

    protected CommonCommandLineOpts() {
        super();
    }
}