package yajhfc.plugin;

/**
 * An enumeration of the possible plugin types
 * @author jonas
 *
 */
public enum PluginType {
    // This enum is accessed during command line parsing 
    // => DO NOT access Utils here!
    PLUGIN,
    JDBCDRIVER;
}