package yajhfc.plugin;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.util.TransactFileOutputStream;

/**
 * This class contains static method to load YajHFC plugins.
 * @author jonas
 *
 */
public class PluginManager {
    
    public static final int STARTUP_MODE_NORMAL = 0;
    public static final int STARTUP_MODE_NO_GUI = 1;
    public static final int STARTUP_MODE_SEND_ONLY = 2;
    public static final int STARTUP_MODE_LOAD_WHILE_RUNNING = 3;
    
    private static final Logger log = Logger.getLogger(PluginManager.class.getName());
    
    /**
     * A list of classes implementing a plugin's init interface that should
     * be initialized just like a plug in at start up.
     * This is used for plug ins integrated into the main tree or otherwise parts of
     * the program that can be separated from the rest.
     */
    private static final Class<?>[] internalPlugins = {
        yajhfc.printerport.EntryPoint.class        
    };
    
    
    /**
     * The text of the key used in the jar manifest to set the init class
     * <p>
     * NOTE: This class must contain a method "public static boolean init()".
     *  That method should return true if the initialization was OK or false otherwise.
     */
    public static final String INITCLASS_KEY = "YajHFC-Plugin-InitClass";
    
    /**
     * A list of helper classes to create a user interface for the plugins. Elements are added to the "Extras" menu in MainWin.
     */
    public static final List<PluginUI> pluginUIs = new ArrayList<PluginUI>();
    
    /**
     * The "Set" of known Plugins
     */
    private static final Map<File,PluginInfo> knownPlugins = new TreeMap<File,PluginInfo>();
    
    /**
     * The class loader used to load the plugin classes
     */
    public static PluginClassLoader pluginClassLoader;
    
    
    /**
     * Returns a collection of all known plugins
     * @return
     */
    public static Collection<PluginInfo> getKnownPlugins() {
        return knownPlugins.values();
    }
    
    /**
     * Adds a collection of plugins to the list of loaded plugins, but
     * does *not* load them.
     * @param plugins
     */
    public static void addPlugins(Collection<? extends PluginInfo> plugins) {
        for (PluginInfo info : plugins) {
            knownPlugins.put(info.file, info);
        }
    }
    
    /**
     * Adds the given plugin/driver and optionally tries to load it.
     * @param info
     * @param load
     */
    public static boolean addPlugin(PluginInfo info, boolean load) {
        PluginInfo oldMapping = knownPlugins.put(info.file, info);
        
        if (oldMapping != null && oldMapping.loaded) {
            info.loaded = true;
            return true;
        } else {
            if (load) {
                try {
                    switch (info.type) {
                    case JDBCDRIVER:
                        addJARToClassPath(info.file);
                        return info.loaded = true;
                    case PLUGIN:
                        info.initClass = loadInitClass(info.file, true);
                        if (info.initClass != null) {
                            return info.loaded = initializePluginClass(info.file, info.initClass, STARTUP_MODE_LOAD_WHILE_RUNNING);
                        } else {
                            return info.loaded = false;
                        }
                    }
                } catch (IOException e) {
                    log.log(Level.WARNING, "Can not load plugin " + info.file, e);
                }
                return false;
            } else {
                return true;
            }
        }
    }
    
    /**
     * Updates the list of plugins, loads JDBC drivers
     * and writes it to the plugin.lst if necessary.
     * @param newList
     * @return true if a restart is necessary for the change to take effect.
     */
    public static boolean updatePluginList(Collection<? extends PluginInfo> newList) {
        Set<File> deletedItems = new HashSet<File>(knownPlugins.keySet());
        boolean changed = false;
        boolean needRestart = false;
        
        for (PluginInfo info : newList) {
            deletedItems.remove(info.file);
            PluginInfo existing = knownPlugins.get(info.file);
            if (existing != null) {
                info.loaded = existing.loaded;
            }
            if (existing == null || !existing.equals(info)) { 
                knownPlugins.put(info.file, info);
                if (!info.loaded) {
                    switch (info.type) {
                    case JDBCDRIVER:
                        try {
                            addJARToClassPath(info.file);
                            info.loaded = true;
                        } catch (Exception e) {
                            log.log(Level.WARNING, "Could not load driver", e);
                            info.loaded = false;
                        }
                        break;
                    case PLUGIN:
                        needRestart = true;
                        break;
                    }
                }
                changed = true;
            }
        }
        for (File f : deletedItems) {
            PluginInfo info = knownPlugins.get(f);
            if (info.loaded) {
                needRestart = true;
                info.persistent = false;
            } else {
                knownPlugins.remove(f);
            }
            changed = true;
        }
        if (changed) {
            try {
                writePluginList();
            } catch (IOException e) {
                // TODO?
                log.log(Level.WARNING, "Could not save plugin list:", e);
            }
        }
        return needRestart;
    }
    
    /**
     * Saves the list of plugins to a file
     * @throws IOException 
     */
    public static void writePluginList() throws IOException {
        writePluginList(new File(Utils.getConfigDir(), "plugin.lst"));
    }
    
    /**
     * Saves the list of plugins to a file
     * @throws IOException 
     */
    public static void writePluginList(File file) throws IOException {
        Writer outWriter = new BufferedWriter(new OutputStreamWriter(new TransactFileOutputStream(file)));
        outWriter.write("# This file contains the list of plugins and JDBC drivers loaded by YajHFC on startup.\n");
        outWriter.write("# Format: One file name per line, a prefix of \":jdbcdriver:\" denotes a JDBC driver.\n");
        outWriter.write("# \n");
        for (PluginInfo info : knownPlugins.values()) {
            if (info.persistent) {
                switch (info.type) {
                case JDBCDRIVER:
                    outWriter.write(":jdbcdriver:" + info.file.getPath() + "\n");
                    break;
                case PLUGIN:
                    outWriter.write(info.file.getPath() + "\n");
                    break;
                }
            }
        }
        outWriter.close();
    }
    
    /**
     * Tests if the given File is a valid YajHFC plugin without initializing it.
     * @param pluginJar
     * @return
     */
    public static boolean isValidPlugin(File pluginJar) {
        try {
            String initClassName = extractInitClassName(pluginJar);
            if (initClassName == null)
                return false;
            
            PluginClassLoader loader = new PluginClassLoader(new URL[] { pluginJar.toURI().toURL() });
            
            loader.loadClass(initClassName);
            
            return true;
        } catch (Exception e) {
            log.log(Level.FINE, "Exception testing plugin for validity:", e);
            return false;
        }
    }
    
    /**
     * Loads a YajHFC plugin from the specified jar file
     * @param pluginJar
     * @throws IOException 
     * @returns true if the plugin loaded successfully
     */    
    private static Class<?> loadInitClass(File pluginJar, boolean addToClassPath) throws IOException {
        String initClassName = extractInitClassName(pluginJar);
        
        if (initClassName == null) {
            log.log(Level.WARNING, pluginJar.toString() + " is not a valid YajHFC Plugin.");
            return null;
        }
        
        try {
            if (Utils.debugMode) {
                log.info("Loading class " + initClassName + " from file " + pluginJar);
            }
            if (addToClassPath) {
                addJARToClassPath(pluginJar);
            }
            return Class.forName(initClassName, true, pluginClassLoader);
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not initialize plugin " + pluginJar + ":", e);
            return null;
        }
    }

    /**
     * Initializes the plugin's init class
     * @param pluginJar the jar file the class was loaded from or null for internal plugins
     * @param initClass
     * @return true if initialized successfully, false otherwise
     */
    private static boolean initializePluginClass(File pluginJar, Class<?> initClass, int startupMode) {        
        try {
            if (Utils.debugMode) {
                log.fine("Initializing " + initClass + " from plugin jar " + pluginJar);
            }
            
            Method initMethod;
            Object[] args;
            try {
                initMethod = initClass.getMethod("init", new Class[] { Integer.TYPE });
                args = new Object[] { startupMode };
            } catch (NoSuchMethodException nme) {
                if (Utils.debugMode) {
                    log.log(Level.FINER, "init(int) not found, trying init()...", nme);
                }
                initMethod = initClass.getMethod("init", new Class[0]);
                args = null;
            } 
                        
            Object returnValue = initMethod.invoke(null, args);
            if (!(returnValue instanceof Boolean && ((Boolean)returnValue).booleanValue())) {
                log.log(Level.WARNING, "Initialization of plugin class " + initClass + " (from " + pluginJar + ") failed." );
                return false;
            }
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not initialize plugin class " + initClass + " (from " + pluginJar + "):", e);
            return false;
        }
    }
 
    
    private static String extractInitClassName(File pluginJar) throws IOException {
        JarFile jar = new JarFile(pluginJar);
        Manifest manifest = jar.getManifest();
        
        String initClassName = manifest.getMainAttributes().getValue(INITCLASS_KEY);
        jar.close();
        return initClassName;
    }
    /**
     * Adds the specified class to the class path of the plugin class loader
     * @param jarFile
     */
    private static void addJARToClassPath(File jarFile) throws MalformedURLException {
        if (pluginClassLoader == null) {
            pluginClassLoader = new PluginClassLoader(new URL[] { jarFile.toURI().toURL() });
        } else {
            pluginClassLoader.addURL(jarFile.toURI().toURL());
        }
    }
    
    /**
     * Reads the list of plugins from the file "plugin.lst".
     * This file is first searched for in the user configuration directory,
     * and then at the location of the jar file.
     */
    public static void readPluginList() {
        File pluginLst = new File(Utils.getApplicationDir(), "plugin.lst");
        if (pluginLst.canRead()) {
            readPluginList(pluginLst);
        }
        
        pluginLst = new File(Utils.getConfigDir(), "plugin.lst");
        if (pluginLst.canRead()) {
            readPluginList(pluginLst);
        }
    }
    
    /**
     * Reads the specified plugin list
     * @param pluginLst
     */
    public static void readPluginList(File pluginLst) {
        try {
            log.fine("Reading plugin list from " + pluginLst);
            BufferedReader lstReader = new BufferedReader(new FileReader(pluginLst));
            String line = lstReader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#")) {
                    boolean isJDBC = line.startsWith(":jdbcdriver:");
                    String fileName;
                    if (isJDBC) {
                        fileName = line.substring(":jdbcdriver:".length());
                    } else {
                        fileName = line;
                    }

                    File pluginJAR = new File(fileName);
                    if (!pluginJAR.canRead() && !pluginJAR.isAbsolute()) {
                        // Try if it can be found if we append the config file location:
                        pluginJAR = new File(pluginLst.getParentFile(), fileName);
                    }
                    if (pluginJAR.canRead()) {
                        knownPlugins.put(pluginJAR, 
                                new PluginInfo(pluginJAR, isJDBC ? PluginType.JDBCDRIVER : PluginType.PLUGIN, true));
                    } else  {
                        log.warning("Can not find (plugin) JAR file " + line);
                    }
                }
                line = lstReader.readLine();
            }
            lstReader.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error reading plugin list:", e);
        }
    }
    
    /**
     * Loads all known plugins (i.e. add to class path and load the init class, but do
     * not call the init() method).
     */
    public static void loadAllKnownPlugins() {
        log.fine("Loading all plugins...");

        if (knownPlugins.size() == 0) 
            return; // Nothing else to do
        if (pluginClassLoader != null)
            log.warning("In loadAllKnownPlugins(): Class loader already exists!");
        
        URL[] jarURLs = new URL[knownPlugins.size()];
        int i = 0;
        for (File jar : knownPlugins.keySet()) {
            try {
                jarURLs[i++] = jar.toURI().toURL();
            } catch (MalformedURLException e) {
                log.log(Level.WARNING, "Can not convert file to URL (File: " + jar + ")", e);
            }
        }
        pluginClassLoader = new PluginClassLoader(jarURLs);
        
        for (PluginInfo info : knownPlugins.values()) {
            try {
                switch (info.type) {
                case JDBCDRIVER:
                    info.loaded = true;
                    break;
                case PLUGIN:
                    info.initClass = loadInitClass(info.file, false);
                    break;
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "Can not load plugin " + info.file, e);
            }
        }
    }
    
    /**
     * Initializes all known plugins. 
     * loadAllKnownPlugins must have been called at some point of time before calling this method.
     */
    public static void initializeAllKnownPlugins(int startupMode) {
        log.fine("Initializing all plugins...");
        
        // First, initialize all internal plugins, then the external ones
        for (Class <?> plugClass : internalPlugins) {
            initializePluginClass(null, plugClass, startupMode);
        }
        for (PluginInfo info : knownPlugins.values()) {
            if (info.initClass != null) {
                info.loaded = initializePluginClass(info.file, info.initClass, startupMode);
            }
        }
    }
    
    public static class PluginInfo {
        public final File file;
        public final PluginType type;
        Class<?> initClass;
        public boolean persistent;
        public boolean loaded;
        
        public PluginInfo(File file, PluginType type, boolean persistent) {
            super();
            this.file = file;
            this.type = type;
            this.loaded = false;
            this.persistent = persistent;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            if (file != null)
                hashCode ^= file.hashCode();
            if (type != null)
                hashCode ^= type.hashCode();
            if (persistent)
                hashCode ^= 2323;
            if (loaded)
                hashCode ^= 4242;
            
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final PluginInfo other = (PluginInfo) obj;
            if (file == null) {
                if (other.file != null)
                    return false;
            } else if (!file.equals(other.file))
                return false;
            if (loaded != other.loaded)
                return false;
            if (persistent != other.persistent)
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
        
        
    }
    protected static final Set<String> registeredDrivers = new HashSet<String>();
    /**
     * Registers an JDBC driver if necessary
     * @param driverName
     * @throws ClassNotFoundException
     */
    public static void registerJDBCDriver(String driverName) throws ClassNotFoundException {
        if (registeredDrivers.contains(driverName))
            return; // Already registered
        
        Class<?> driverClass = Class.forName(driverName, true, pluginClassLoader == null ? PluginManager.class.getClassLoader() : pluginClassLoader);
        if (driverClass.getClassLoader() != PluginManager.class.getClassLoader()) {
            try {
                Driver driver = (Driver)driverClass.newInstance();

                // Bypass security check in driver manager
                DriverManager.registerDriver(new DriverWrapper(driver));
                
                registeredDrivers.add(driverName);
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not register driver " + driverName, e);
            }
        } else {
            registeredDrivers.add(driverName);
        }
    }
    
    /**
     * A wrapper around a JDBC driver to bypass (useless) security checks in
     * the driver manager
     * @author jonas
     *
     */
    public static class DriverWrapper implements Driver {
        protected Driver wrapped;

        public DriverWrapper(Driver wrapped) {
            super();
            this.wrapped = wrapped;
        }

        public boolean acceptsURL(String url) throws SQLException {
            return wrapped.acceptsURL(url);
        }

        public Connection connect(String url, Properties info)
                throws SQLException {
            return wrapped.connect(url, info);
        }

        public int getMajorVersion() {
            return wrapped.getMajorVersion();
        }

        public int getMinorVersion() {
            return wrapped.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
                throws SQLException {
            return wrapped.getPropertyInfo(url, info);
        }

        public boolean jdbcCompliant() {
            return wrapped.jdbcCompliant();
        }
        
    }
}
