/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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
package yajhfc;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * An interface containing both a String and an icon for display purposes (e.g. in tables)
 * @author jonas
 *
 */
public interface IconMap {
    
    /**
     * The short description text.
     * @return
     */
    public String getText();
    /**
     * A more verbose description for this item (e.g. for tool tips)
     * May return null to signal that there is no such description. 
     * @return
     */
    public String getDescription();
    /**
     * The icon that should be displayed.
     * @return
     */
    public ImageIcon getDisplayIcon();
    
    /**
     * Defines a table cell renderer that can be used to display icon and text for an IconMap.
     * @author jonas
     *
     */
    public static class TableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            IconMap data = (IconMap)value;
            String text;
            String tooltip;
            Icon icon;
            if (data == null) {
                text = "";
                icon = null;
                tooltip = null;
            } else {
                text = data.getText();
                icon = data.getDisplayIcon();
                tooltip = data.getDescription();
            }
            
            JLabel renderer = (JLabel)super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
            renderer.setIcon(icon);
            renderer.setToolTipText(tooltip);
            return renderer;
        }
    }
    
    /**
     * Defines a list cell renderer that can be used to display icon and text for an IconMap.
     * @author jonas
     *
     */
    public static class ListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            IconMap data = (IconMap)value;
            String text;
            Icon icon;
            String tooltip;
            if (data == null) {
                text = "";
                icon = null;
                tooltip = null;
            } else {
                text = data.getText();
                icon = data.getDisplayIcon();
                tooltip = data.getDescription();
            }
            
            JLabel renderer = (JLabel)super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            renderer.setIcon(icon);
            renderer.setToolTipText(tooltip);
            return renderer;
        }
    }
}
