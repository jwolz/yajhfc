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
package yajhfc.print.tableprint;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.Format;

import javax.swing.ImageIcon;

import yajhfc.model.IconMap;

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
