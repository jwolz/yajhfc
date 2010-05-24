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
import java.awt.geom.Rectangle2D;
import java.text.FieldPosition;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import yajhfc.Utils;

public class DefaultCellRenderer implements TableCellRenderer {
    private StringBuffer sbuf = new StringBuffer();
    private FieldPosition fpos = new FieldPosition(0);
    
    protected String valueToString(Format format, Object value) {
        if (format != null) {
            sbuf.setLength(0);
            format.format(value, sbuf, fpos);
            return sbuf.toString();
        } else {
            return (value == null) ? "" : value.toString();
        }
    }
    
    protected Format getFormat(TablePrintColumn col) {
        return col.getEffectiveFormat();
    }
    
    protected Alignment getAlignment(TablePrintColumn col) {
        return col.getAlignment();
    }
    
    /**
     * Word wraps the specified line. If the line needs to be split, consecutive lines
     * are inserted into the list after the index
     * @param lines
     * @param index
     * @param maxWidth
     * @param fm
     */
    protected int wordWrapText(List<String> lines, List<Rectangle2D> boxes, int index, Graphics2D graphics, double maxWidth, FontMetrics fm) {
        int blankOffset = -1;
        
        String text = lines.get(index);
        String oldWords = null;
        Rectangle2D oldBox = null;
        
        do {
            blankOffset = text.indexOf(' ', blankOffset+1);
            String words;
            if (blankOffset < 0) {
                words = text;
            } else {
                words = text.substring(0, blankOffset);
            }
            Rectangle2D box = fm.getStringBounds(words, graphics);
            if (box.getWidth() > maxWidth && oldWords != null) {
                lines.set(index, oldWords);
                boxes.set(index, oldBox);
                index++;
                lines.add(index, null);
                boxes.add(index, null);
                
                text = text.substring(oldWords.length()+1);
                oldWords = null;
                oldBox = null;
                blankOffset = 0;
            } else {
                oldWords = words;
                oldBox = box;
            }
        } while (blankOffset >= 0);
        
        lines.set(index, text);
        boxes.set(index, fm.getStringBounds(text, graphics));
        return index;
    }
    
    @SuppressWarnings("unchecked")
    public double drawCell(Graphics2D graphics, double x, double y, 
            Object value, TablePrintColumn column, Color background,
            double spaceX, double spaceY, double maxY, boolean pageContinuation, ColumnPageData pageData) {         
        List<String> lines;
        if (pageContinuation) {
            if (pageData.remainingData == null) {
                return DRAWCELL_NOTHING_REMAINED;
            } else {
                lines = (List<String>)pageData.remainingData;
            }
        } else {
            lines = Utils.fastSplitToList(valueToString(getFormat(column), value), '\n');
        }
        
        FontMetrics fm = graphics.getFontMetrics();
        List<Rectangle2D> boxes = new ArrayList<Rectangle2D>(lines.size());
        for (int i=0; i<lines.size(); i++) {
            Rectangle2D box = fm.getStringBounds(lines.get(i), graphics);
            boxes.add(box);
            if (isWordWrap(column) && 
                    box.getWidth() > column.getEffectiveColumnWidth() - 2*spaceX) {
                i = wordWrapText(lines, boxes, i, graphics, column.getEffectiveColumnWidth()-2*spaceX, fm);
            }
        }
           
        double cellHeight = 2*spaceY;
        int numLines = 0;
        boolean needPagebreak = false;
        for (Rectangle2D box : boxes) {
            if (y+cellHeight+box.getHeight() > maxY) {
                needPagebreak = true;
                break;
            }
            cellHeight += box.getHeight();
            numLines++;
        }
        double rv;
        int lineCount = lines.size();
        if (needPagebreak) { // Need a pagebreak
            if (numLines < 1) {
                if (pageData != null)
                    pageData.remainingData = lines;
                return DRAWCELL_NOTHING;
            } else {
                if (pageData != null)
                    pageData.remainingData = lines.subList(numLines, lines.size());
                lineCount = numLines;
            }
            rv = -cellHeight;
        } else {
            if (pageData != null)
                pageData.remainingData = null;
            rv = cellHeight;
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
        y += spaceY+fm.getAscent();
        for (int i=0; i<lineCount; i++) {
            final Rectangle2D boundingBox = boxes.get(i);
            graphics.drawString(lines.get(i),
                    (float)getAlignment(column).calculateX(x, boundingBox.getWidth(), column.getEffectiveColumnWidth(), spaceX),
                    (float)y);
            y += boundingBox.getHeight();
        }
        graphics.setClip(oldClip);
        return rv;
    }

    protected boolean isWordWrap(TablePrintColumn column) {
        return column.isWordWrap();
    }
    
    public double getPreferredWidth(Graphics2D graphics, Object value,
            FontMetrics fm, Format format, TablePrintColumn column) {
        String[] lines = Utils.fastSplit(valueToString(format, value), '\n');
        double width = 0;
        for (String line : lines) {
            Rectangle2D b = fm.getStringBounds(line, graphics);
            if (b.getWidth() > width)
                width = b.getWidth();
        }
        return width;
    }        
}