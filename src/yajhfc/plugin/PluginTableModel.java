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
package yajhfc.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import yajhfc.Utils;
import yajhfc.model.IconMap;
import yajhfc.plugin.PluginManager.PluginInfo;



/**
 * @author jonas
 *
 */
public class PluginTableModel extends AbstractTableModel {
    
    protected static final String[] colNames = { Utils._("File name"), Utils._("Type"), Utils._("Loaded"), Utils._("Only this session") };
    protected static final Map<PluginType, PluginTypeDesc> typeDisplayMap = new EnumMap<PluginType, PluginTypeDesc>(PluginType.class);
    static {
        typeDisplayMap.put(PluginType.JDBCDRIVER, 
                new PluginTypeDesc(Utils._("JDBC driver"), Utils.loadIcon("development/Jar")));
        typeDisplayMap.put(PluginType.PLUGIN, 
                new PluginTypeDesc(Utils._("Plugin"), Utils.loadIcon("development/J2EEApplicationClient")));
    }
    
    protected List<Entry> entries = new ArrayList<Entry>();
    
    public PluginTableModel() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 4;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return entries.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Entry entry = entries.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return entry;
        case 1:
            return typeDisplayMap.get(entry.type).description;
        case 2:
            return entry.loaded;
        case 3:
            return !entry.persistent;
        default:
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return IconMap.class;
        case 1:
            return String.class;
        case 2:
        case 3:
            return Boolean.class;
        default:
            return Object.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 3);
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 3:
            entries.get(rowIndex).persistent = !(Boolean)value;
            fireTableRowsUpdated(rowIndex, rowIndex);
            break;
        }
    }
    
    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }
    
    public void addAllItems(Collection<PluginInfo> infos) {
        int oldCount = entries.size();
        for (PluginInfo info : infos) {
            entries.add(new Entry(info.file, info.type, info.persistent, info.loaded));
        }
        fireTableRowsInserted(oldCount, entries.size() - 1);
    }
    
    public void addItem(PluginInfo info) {
        addItem(info.file, info.type, info.persistent, info.loaded);
    }
    
    public void addItem(File jar, PluginType type) {
        addItem(jar, type, true, false);
    }
    
    public void addItem(File jar, PluginType type, boolean persistent, boolean loaded) {
        entries.add(new Entry(jar, type, persistent, loaded));
        int idx = entries.size() - 1;
        fireTableRowsInserted(idx, idx);
    }
    
    public void removeItemAt(int index) {
        entries.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public Entry getEntry(int index) {
        return entries.get(index);
    }
    
    public List<Entry> getEntries() {
        return entries;
    }
    
    public static class Entry extends PluginInfo implements IconMap {
        public final ImageIcon icon;
        public final String fileName;
        
        public Entry(File file, PluginType type, boolean persistent, boolean loaded) {
            super(file, type, persistent);
            this.fileName = file.getAbsolutePath();
            this.icon = typeDisplayMap.get(type).icon;
            this.loaded = loaded;
        }

        public String getDescription() {
            return null;
        }

        public ImageIcon getDisplayIcon() {
            return icon;
        }

        public String getText() {
            return fileName;
        }
        
        @Override
        public String toString() {
            return fileName;
        }
    }
    protected static class PluginTypeDesc {
        public final String description;
        public final ImageIcon icon;
        
        public PluginTypeDesc(String description, ImageIcon icon) {
            super();
            this.description = description;
            this.icon = icon;
        }
    }
}
