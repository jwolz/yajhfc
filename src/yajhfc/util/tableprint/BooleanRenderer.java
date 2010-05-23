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
package yajhfc.util.tableprint;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.Format;

public class BooleanRenderer implements TableCellRenderer {
    private static final double scaleScale = 0.8;
    
    protected Shape checkedShape;
    protected Shape uncheckedShape;
    
    /**
     * Returns a shape for a "checked" symbol
     * Currently this is the ☑ glyph from DejaVu Sans
     * @return
     */
    protected Shape getCheckedShape() {
        if (checkedShape == null) {
            GeneralPath shape = new GeneralPath(1);
            shape.moveTo(1.84375f, -14.578125f);
            shape.lineTo(16.09375f, -14.578125f);
            shape.lineTo(16.15625f, -14.515625f);
            shape.lineTo(16.15625f, -0.0625f);
            shape.lineTo(16.09375f, 0.0f);
            shape.lineTo(1.84375f, 0.0f);
            shape.lineTo(1.78125f, -0.0625f);
            shape.lineTo(1.78125f, -14.515625f);
            shape.lineTo(1.84375f, -14.578125f);
            shape.closePath();
            shape.moveTo(2.875f, -13.5f);
            shape.lineTo(2.875f, -1.078125f);
            shape.lineTo(15.0625f, -1.078125f);
            shape.lineTo(15.0625f, -13.5f);
            shape.lineTo(2.875f, -13.5f);
            shape.closePath();
            shape.moveTo(13.734375f, -11.96875f);
            shape.lineTo(13.734375f, -11.90625f);
            shape.quadTo(13.0f, -11.265625f, 12.59375f, -10.703125f);
            shape.quadTo(9.40625f, -6.390625f, 8.796875f, -3.234375f);
            shape.quadTo(8.734375f, -2.65625f, 8.578125f, -2.65625f);
            shape.quadTo(7.4375f, -2.203125f, 7.3125f, -2.203125f);
            shape.quadTo(6.328125f, -4.71875f, 4.328125f, -4.71875f);
            shape.lineTo(4.109375f, -4.71875f);
            shape.lineTo(4.109375f, -4.78125f);
            shape.lineTo(5.6875f, -5.5f);
            shape.quadTo(7.15625f, -5.5f, 7.875f, -4.21875f);
            shape.lineTo(7.9375f, -4.21875f);
            shape.quadTo(8.421875f, -5.84375f, 9.046875f, -6.96875f);
            shape.quadTo(10.0f, -8.71875f, 11.515625f, -10.546875f);
            shape.quadTo(12.46875f, -11.71875f, 13.734375f, -11.96875f);
            shape.closePath();
            checkedShape = shape;
        }
        return checkedShape;
    }
    
    /**
     * Returns a shape for a "checked" symbol
     * Currently this is the ☐ glyph from DejaVu Sans
     * @return
     */
    protected Shape getUncheckedShape() {
        if (uncheckedShape == null) {
            GeneralPath shape = new GeneralPath(1);
            shape.moveTo(1.859375f, -14.578125f);
            shape.lineTo(16.078125f, -14.578125f);
            shape.lineTo(16.140625f, -14.515625f);
            shape.lineTo(16.140625f, -0.0625f);
            shape.lineTo(16.078125f, 0.0f);
            shape.lineTo(1.859375f, 0.0f);
            shape.lineTo(1.796875f, -0.0625f);
            shape.lineTo(1.796875f, -14.515625f);
            shape.lineTo(1.859375f, -14.578125f);
            shape.closePath();
            shape.moveTo(2.875f, -13.5f);
            shape.lineTo(2.875f, -1.078125f);
            shape.lineTo(15.0625f, -1.078125f);
            shape.lineTo(15.0625f, -13.5f);
            shape.lineTo(2.875f, -13.5f);
            shape.closePath();
            uncheckedShape = shape;
        }
        return uncheckedShape;
    }
    
    protected Shape getShapeForValue(Object value) {
        Boolean boolVal = (Boolean)value;
        if (boolVal != null && boolVal.booleanValue()) {
            return getCheckedShape();
        } else {
            return getUncheckedShape();
        }
    }
    
    public double drawCell(Graphics2D graphics, double x, double y,
            Object value, TablePrintColumn column, Font colFont,
            Color background, double spaceX, double spaceY, double maxY,
            boolean pageContinuation, ColumnPageData pageData) {
        if (pageContinuation) {
            return DRAWCELL_NOTHING_REMAINED;
        }
        
        graphics.setFont(colFont);
        FontMetrics fm = graphics.getFontMetrics();
        double cellHeight = fm.getHeight() + 2*spaceY;
        if (y+cellHeight > maxY) { // Need a pagebreak
            return DRAWCELL_NOTHING;
        }
        
        Shape shape = getShapeForValue(value);
        Rectangle2D bounds = shape.getBounds2D();
        double scale = scaleScale * fm.getHeight() / bounds.getHeight();
        
        Shape oldClip = graphics.getClip();
        Rectangle2D.Double cellRect = new Rectangle2D.Double(x, y, column.getEffectiveColumnWidth(), cellHeight);
        graphics.clip(cellRect);
        if (background != null) {
            Color oldColor = graphics.getColor();
            graphics.setColor(background);
            graphics.fill(cellRect);
            graphics.setColor(oldColor);
        }
        
        double drawWidth = bounds.getWidth() * scale;
        
        AffineTransform oldTrans = graphics.getTransform();
        graphics.translate(column.getAlignment().calculateX(x, drawWidth, column.getEffectiveColumnWidth(), spaceX),
                    y+spaceY+(fm.getHeight()+bounds.getHeight()*scale)/2);
        graphics.scale(scale, scale);
        graphics.fill(shape);
        
        graphics.setTransform(oldTrans);
        graphics.setClip(oldClip);
        return cellHeight;
    }

    public double getPreferredWidth(Graphics2D graphics, Object value,
            FontMetrics fm, Format format, TablePrintColumn column) {
        Shape s = getShapeForValue(value);
        Rectangle2D bounds = s.getBounds2D();
        double scale = scaleScale * fm.getHeight() / bounds.getHeight();
        return bounds.getWidth() * scale;
    }
    
}