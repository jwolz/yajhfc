/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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
package yajhfc.customprops;

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
    
    private static final String PREFIX = "java";
    private static final int VERSION = 1;

    static final Logger log = Logger.getLogger(EntryPoint.class.getName());
    
    protected static Properties customJavaProperties;
    
    private static Properties loadCustomProperties(Properties source) {        
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
        
        Properties res = new Properties();
        if (version < 1) {
            res.put("java.net.useSystemProxies", "true");
        }
        
        for (Map.Entry<Object,Object> prop : source.entrySet()) {
            String key = (String)prop.getKey();
            if (key.startsWith(PREFIX + "--")) {
                res.put(key.substring(PREFIX.length()+2), prop.getValue());
            }
        }
        return res;
    }
    
    private static void saveCustomProperties(Properties customProps, Properties target) {
        target.put(PREFIX + "-VERSION", String.valueOf(VERSION));
        for (Map.Entry<Object,Object> prop : customProps.entrySet()) {
            target.put(PREFIX + "--" + prop.getKey(), prop.getValue());
        }
    }
    
    public static void activateCustomProperties() {
        System.getProperties().putAll(customJavaProperties);
    }
    
    public static Properties getCustomJavaProperties() {
        return customJavaProperties;
    }
    
    public static boolean init(int mode) {
        PluginManager.pluginUIs.add(new EntryPoint());
        
        customJavaProperties = loadCustomProperties(Utils.getSettingsProperties());
        
        activateCustomProperties();
        
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
