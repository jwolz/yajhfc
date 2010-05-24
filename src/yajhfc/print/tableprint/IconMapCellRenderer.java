/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.print.tableprint;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.Format;

import javax.swing.ImageIcon;

import yajhfc.IconMap;

/**
 * @author jonas
 *
 */
public class IconMapCellRenderer implements TableCellRenderer {
   
    private static final double ICON_SPACING = 0.25;

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.TableCellRenderer#drawCell(java.awt.Graphics2D, double, double, java.lang.Object, yajhfc.print.tableprint.TablePrintColumn, java.awt.Font, java.awt.Color, double, double, double, boolean)
     */
    public double drawCell(Graphics2D graphics, double x, double y,
            Object value, TablePrintColumn column, Color background,
            double spaceX, double spaceY, double maxY, boolean pageContinuation,
            ColumnPageData pageData) {
        if (pageContinuation) {
            return DRAWCELL_NOTHING_REMAINED;
        }
        
        IconMap im = (IconMap)value;
        String text = im.getText();
        
        FontMetrics fm = graphics.getFontMetrics();
        Rectangle2D box = fm.getStringBounds(text, graphics);
        double cellHeight = box.getHeight() + 2*spaceY;
        if (y+cellHeight > maxY) { // Need a pagebreak
            return DRAWCELL_NOTHING;
        }
        
        Shape oldClip = graphics.getClip();
        Rectangle2D.Double cellRect = new Rectangle2D.Double(x, y, column.getEffectiveColumnWidth(), cellHeight);
        graphics.clip(cellRect);
        if (background != null) {
            Color oldColor = graphics.getColor();
            graphics.setColor(background);
            graphics.fill(cellRect);
            graphics.setColor(oldColor);
        }
        
        ImageIcon icon = im.getDisplayIcon();
        double iconScale = box.getHeight() / icon.getIconHeight();
        double iconWidth = icon.getIconWidth() * iconScale;
        double drawWidth = box.getWidth() + iconWidth + fm.getHeight()*ICON_SPACING;
        
        double drawX = column.getAlignment().calculateX(x, drawWidth, column.getEffectiveColumnWidth(), spaceX);
        //graphics.drawImage(icon.getImage(), (int)drawX, (int)(y+spaceY), (int)iconWidth, (int)box.getHeight(), null);
        AffineTransform imgtrans = AffineTransform.getTranslateInstance(drawX, y+spaceY);
        imgtrans.scale(iconScale, iconScale);
        graphics.drawImage(icon.getImage(), imgtrans, null);
        graphics.drawString(text, (float)(drawX+iconWidth+fm.getHeight()*ICON_SPACING), (float)(y+spaceY+fm.getAscent()));
        graphics.setClip(oldClip);
        return cellHeight;
    }

    /* (non-Javadoc)
     * @see yajhfc.print.tableprint.TableCellRenderer#getPreferredWidth(java.awt.Graphics2D, java.lang.Object, java.awt.FontMetrics, java.text.Format, yajhfc.print.tableprint.TablePrintColumn)
     */
    public double getPreferredWidth(Graphics2D graphics, Object value,
            FontMetrics fm, Format format, TablePrintColumn column) {
        IconMap im = (IconMap)value;
        Rectangle2D box = fm.getStringBounds(im.getText(), graphics);
        double scale = box.getHeight() / im.getDisplayIcon().getIconHeight();
        return box.getWidth() + fm.getHeight()*ICON_SPACING + im.getDisplayIcon().getIconWidth()*scale;
    }

}
