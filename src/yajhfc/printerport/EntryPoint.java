/*
 * YajHFC - Yet another Java Hylafax client
 * Copyright (C) 2009 Jonas Wolz
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
package yajhfc.printerport;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.options.PanelTreeNode;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;



/**
 * @author jonas
 *
 */
public final class EntryPoint {

    static final Logger log = Logger.getLogger(EntryPoint.class.getName());
    
    private static PortOptions options;
    
    public static ListenThread TCP_THREAD;
    public static FIFOThread FIFO_THREAD;
    
    public static PortOptions getOptions() {
        if (options == null) {
            options = new PortOptions();
            options.loadFromProperties(Utils.getSettingsProperties());
        }
        return options;
    }
    
    public static boolean init(int mode) {
        if (mode != PluginManager.STARTUP_MODE_NORMAL) {
            log.fine("Not loading for startup mode " + mode);
            return true;
        }
        
        PluginManager.pluginUIs.add(new PluginUI() {
            @Override
            public int getOptionsPanelParent() {
                return OPTION_PANEL_ADVANCED;
            }
            
           @Override
            public PanelTreeNode createOptionsPanel(PanelTreeNode parent) {
               return new PanelTreeNode(parent, new PortOptionsPanel(), Utils._("Printer port"), Utils.loadCustomIcon("printerport-icon.gif"));
            }
           
           @Override
            public void saveOptions(Properties p) {
               getOptions().storeToProperties(p);
            }
        });
        
        
        reopenTCPThread();
        reopenFIFOThread();

        return true;
    }
    
    public static void reopenTCPThread() {
        if (TCP_THREAD != null) {
            TCP_THREAD.close();
            TCP_THREAD = null;
        }
        final PortOptions portOpts = getOptions();
        if (portOpts.enabled) {
            try {
                TCP_THREAD = new ListenThread(portOpts.bindAddress, portOpts.port);
                TCP_THREAD.start();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error creating server socket:", e);
            }
        }
    }
    
    public static void reopenFIFOThread() {
        if (FIFO_THREAD != null) {
            FIFO_THREAD.close();
            FIFO_THREAD = null;
        }
        final PortOptions portOpts = getOptions();
        if (portOpts.enableFIFO) {
            String fifoName = portOpts.fifoName.replace("%u", System.getProperty("user.name"));
            FIFO_THREAD = new FIFOThread(fifoName);
            FIFO_THREAD.start();
        }
    }
}
