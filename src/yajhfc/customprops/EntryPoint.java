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
package yajhfc.customprops;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.options.PanelTreeNode;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;

/**
 * @author jonas
 *
 */
public class EntryPoint extends PluginUI {
    
    public static final String PROP_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";
    public static final String PROP_HTTP_HOST = "http.proxyHost";
    public static final String PROP_HTTP_PORT = "http.proxyPort";
    public static final String PROP_HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";    
    
	private static final String PREFIX = "javaprop";
    private static final int VERSION = 1;

    static final Logger log = Logger.getLogger(EntryPoint.class.getName());
    
    protected static Map<String,String> customJavaProperties;
    
    private static Map<String,String> loadCustomProperties(Properties source) {        
        String sVersion = source.getProperty(PREFIX + "-VERSION");
        int version;
        if (sVersion == null) {
            version = 0;
        } else {
            try {
                version = Integer.parseInt(sVersion);
            } catch (NumberFormatException e) {
                version = -1;
            }
        }
        
        Map<String,String> res = new HashMap<String,String>();
        if (version < 1) {
            //res.put(PROP_USE_SYSTEM_PROXIES, "true"); // May cause problems...
        }
        
        Enumeration<?> propertyNames = source.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String key = (String)propertyNames.nextElement();
            if (key.startsWith(PREFIX + "--")) {
                res.put(key.substring(PREFIX.length()+2), source.getProperty(key));
            }
        }
        return res;
    }
    
    private static void saveCustomProperties(Map<String,String> customProps, Properties target) {
        target.put(PREFIX + "-VERSION", String.valueOf(VERSION));
        for (Map.Entry<String,String> prop : customProps.entrySet()) {
            target.put(PREFIX + "--" + prop.getKey(), prop.getValue());
        }
    }
    
    
    public static Map<String,String> getCustomJavaProperties() {
        return customJavaProperties;
    }
    
    public static boolean init(int mode) {
        PluginManager.pluginUIs.add(new EntryPoint());
        
        customJavaProperties = loadCustomProperties(Utils.getSettingsProperties());
        
        System.getProperties().putAll(customJavaProperties);
        
        return true;
    }
    
    @Override
    public void saveOptions(Properties p) {
        saveCustomProperties(customJavaProperties, p);
    }
    
    @Override
    public PanelTreeNode createOptionsPanel(PanelTreeNode parent) {
        return new PanelTreeNode(parent, new CustomPropOptionsPanel(), Utils._("Proxies and system properties"), Utils.loadIcon("development/WebComponent")); 
    }
    
    @Override
    public int getOptionsPanelParent() {
        return OPTION_PANEL_ADVANCED;
    }
}
