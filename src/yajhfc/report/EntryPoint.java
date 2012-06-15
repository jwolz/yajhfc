/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2011 Jonas Wolz
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
 */
package yajhfc.report;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JMenuItem;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.plugin.PluginManager;
import yajhfc.plugin.PluginUI;

/**
 * Example initialization class for a YajHFC plugin.
 * 
 * The name of this class can be chosen freely, but must match the name
 * set in the YajHFC-Plugin-InitClass entry in the jar file.
 * @author jonas
 *
 */
public class EntryPoint {

    /**
     * Plugin initialization method.
     * The name and signature of this method must be exactly as follows 
     * (i.e. it must always be "public static boolean init(int)" )
     * @param startupMode the mode YajHFC is starting up in. The possible
     *    values are one of the STARTUP_MODE_* constants defined in yajhfc.plugin.PluginManager
     * @return true if the initialization was successful, false otherwise.
     */
    public static boolean init(int startupMode) {      
        PluginManager.pluginUIs.add(new PluginUI() {
            final PrintReportAction actPrintReport = new PrintReportAction();
            
            @Override
            public void configureMainWin(MainWin mainWin) {
                insertMenuItemAfter(mainWin.getMenuFax(), "ViewLog", new JMenuItem(actPrintReport));
                insertMenuItemAfter(mainWin.getTablePopupMenu(), "ViewLog", new JMenuItem(actPrintReport));
                
                mainWin.getTabMain().addChangeListener(actPrintReport);
                mainWin.getTableRecv().getSelectionModel().addListSelectionListener(actPrintReport);
                mainWin.getTableSent().getSelectionModel().addListSelectionListener(actPrintReport);
            }
            
            @Override
            public Map<String, Action> createToolbarActions() {
                Map<String,Action> actionMap = new HashMap<String,Action>();
                actionMap.put("PrintReport", actPrintReport);
                return actionMap;
            }
            
            @Override
            public void saveOptions(Properties p) {
                getOptions().storeToProperties(p);
            }

        });
        return true;
    }
    
    private static ReportOptions options;
    /**
     * Lazily load some options (optional, only if you want to save settings)
     * @return
     */
    public static ReportOptions getOptions() {
        if (options == null) {
            options = new ReportOptions();
            options.loadFromProperties(Utils.getSettingsProperties());
        }
        return options;
    }
}
