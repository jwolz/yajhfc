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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This class contains static method to load YajHFC plugins.
 * @author jonas
 *
 */
public class PluginManager {
    
    /**
     * The text of the key used in the jar manifest to set the init class
     * <p>
     * NOTE: This class must contain a method "public static boolean init()".
     *  That method should return true if the initialization was OK or false otherwise.
     */
    public static final String INITCLASS_KEY = "YajHFC-Plugin-InitClass";
    
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
            utils.printWarning(pluginJar.toString() + " is not a valid YajHFC Plugin.");
            return false;
        }
        
        try {
            Class<?> initClass = loadPluginClass(pluginJar, initClassName);
            
            Method initMethod = initClass.getMethod("init", new Class[0]);
            Object returnValue = initMethod.invoke(null);
            if (!(returnValue instanceof Boolean && ((Boolean)returnValue).booleanValue())) {
                utils.printWarning("Initialization of plugin " + pluginJar + " failed." );
                return false;
            }
            return true;
        } catch (Exception e) {
            utils.printWarning("Could not initialize plugin " + pluginJar + ":", e);
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
            utils.debugOut.println("Loading class " + className + " from file " + pluginJar);
        }
        URLClassLoader clsLoader = new URLClassLoader(new URL[] { pluginJar.toURI().toURL() });
        Class<?> rv = clsLoader.loadClass(className);
        return rv;
    }
}
