package yajhfc;
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;

/**
 * This class contains static method to load YajHFC plugins.
 * @author jonas
 *
 */
public class PluginManager {
    
    private static final Logger log = Logger.getLogger(PluginManager.class.getName());
    
    /**
     * The text of the key used in the jar manifest to set the init class
     * <p>
     * NOTE: This class must contain a method "public static boolean init()".
     *  That method should return true if the initialization was OK or false otherwise.
     */
    public static final String INITCLASS_KEY = "YajHFC-Plugin-InitClass";
    
    /**
     * A list of menu entries for the plugins. Elements are added to the "Extras" menu in mainwin.
     */
    public static final List<PluginMenuCreator> pluginMenuEntries = new ArrayList<PluginMenuCreator>();
    
    
    protected static PluginClassLoader pluginClassLoader;
    
    /**
     * Loads a YajHFC plugin from the specified jar file
     * @param pluginJar
     * @throws IOException 
     * @returns true if the plugin loaded successfully
     */
    public static boolean addPlugin(File pluginJar) throws IOException {
        JarFile jar = new JarFile(pluginJar);
        Manifest manifest = jar.getManifest();
        
        String initClassName = manifest.getMainAttributes().getValue(INITCLASS_KEY);
        jar.close();
        
        if (initClassName == null) {
            log.log(Level.WARNING, pluginJar.toString() + " is not a valid YajHFC Plugin.");
            return false;
        }
        
        try {
            Class<?> initClass = loadPluginClass(pluginJar, initClassName);
            
            Method initMethod = initClass.getMethod("init", new Class[0]);
            Object returnValue = initMethod.invoke(null);
            if (!(returnValue instanceof Boolean && ((Boolean)returnValue).booleanValue())) {
                log.log(Level.WARNING, "Initialization of plugin " + pluginJar + " failed." );
                return false;
            }
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not initialize plugin " + pluginJar + ":", e);
            return false;
        }
    }
 
    /**
     * Loads the specified class from the specified jar file
     * @param pluginJar
     * @throws MalformedURLException 
     * @throws ClassNotFoundException 
     * @throws IOException 
     * @returns The loaded class
     */
    public static Class<?> loadPluginClass(File pluginJar, String className) throws MalformedURLException, ClassNotFoundException {
        if (utils.debugMode) {
            log.info("Loading class " + className + " from file " + pluginJar);
        }
        //URLClassLoader clsLoader = new URLClassLoader(new URL[] { pluginJar.toURI().toURL() });
        if (pluginClassLoader == null) {
            pluginClassLoader = new PluginClassLoader(new URL[] { pluginJar.toURI().toURL() });
        } else {
            pluginClassLoader.addURL(pluginJar.toURI().toURL());
        }
        
        Class<?> rv = pluginClassLoader.loadClass(className);
        return rv;
    }
    
    /**
     * Loads the plugins referenced in the file "plugin.lst".
     * This file is first searched for in the user configuration directory,
     * and then at the location of the jar file.
     */
    public static void loadPluginList() {
        File pluginLst = new File(utils.getConfigDir(), "plugin.lst");
        if (!pluginLst.canRead()) {
            pluginLst = new File(utils.applicationDir, "plugin.lst");
            if (!pluginLst.canRead()) {
                return;
            }
        }
        try {
            BufferedReader lstReader = new BufferedReader(new FileReader(pluginLst));
            String line = lstReader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) {
                    File pluginJAR = new File(line);
                    if (!pluginJAR.canRead() && !pluginJAR.isAbsolute()) {
                            // Try if it can be found if we append the config file location:
                            pluginJAR = new File(pluginLst.getParentFile(), line);
                        }
                    if (pluginJAR.canRead()) {
                        addPlugin(pluginJAR);
                    } else {
                        if (!pluginJAR.canRead()) {
                            log.warning("Can not find plugin " + line);
                        }
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
     * This interface is used to create menu items for plugins
     * @author jonas
     *
     */
    public interface PluginMenuCreator {
        public JMenuItem[] createMenuItems();
    }

    protected static class PluginClassLoader extends URLClassLoader {

        public PluginClassLoader(URL[] urls, ClassLoader parent,
                URLStreamHandlerFactory factory) {
            super(urls, parent, factory);
        }

        public PluginClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        public PluginClassLoader(URL[] urls) {
            super(urls);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }
    }
}
