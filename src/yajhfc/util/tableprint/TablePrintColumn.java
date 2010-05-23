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

import java.awt.Font;
import java.text.Format;

/**
 * @author jonas
 *
 */
public class TablePrintColumn {
    protected final TablePrintable parent;
    protected final int index;
    protected Font font = null;
    protected Font headerFont = null;
    
    public static final float WIDTH_PREFERRED = -1f;
    public static final float WIDTH_FILL = -2f;
    /**
     * The width.
     * Values < 0    : special values
     * 0 < value < 1 : part of the full width
     * value >= 1    : width in device units
     */
    protected float width = WIDTH_PREFERRED;
    protected Alignment alignment;
    protected boolean wordWrap = false;
    protected Alignment headerAlignment = Alignment.CENTER;
    
    protected Format columnFormat = null;

    /**
     * Saves the effective column width in device units. Set by
     * the column layout.
     */
    protected double effectiveColumnWidth;
    
    /**
     * The font to use in this column. null means "use default"
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * The font to use in this column. null means "use default"
     * @return
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * The font to use for this column's header. null means "use default"
     * @return
     */
    public Font getHeaderFont() {
        return headerFont;
    }

    /**
     * The font to use for this column's header. null means "use default"
     * @return
     */
    public void setHeaderFont(Font headerFont) {
        this.headerFont = headerFont;
    }

    /**
     * This column's width. The following values are valid: <br>
     * WIDTH_PREFERRED: The maximum width of text in this column <br>
     * WIDTH_FILL     : The rest of the space available on this page <br>
     * 0 < width < 1  : given part of the page width <br>
     * width >= 1     : width in device units <br>
     */
    public float getWidth() {
        return width;
    }
    /**
     * This column's width. The following values are valid: <br>
     * WIDTH_PREFERRED: The maximum width of text in this column <br>
     * WIDTH_FILL     : The rest of the space available on this page <br>
     * 0 < width < 1  : given part of the page width <br>
     * width >= 1     : width in device units <br>
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * The horizontal alignment of this column
     * @return
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * The horizontal alignment of this column
     * @return
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    /**
     * The formatter to use to convert the values of this column to String.
     * null means "use default"
     * @return
     */
    public Format getColumnFormat() {
        return columnFormat;
    }

    /**
     * The formatter to use to convert the values of this column to String.
     * null means "use default"
     * @return
     */
    public void setColumnFormat(Format columnFormat) {
        this.columnFormat = columnFormat;
    }

    /**
     * Word wrap text in this column?
     * @return
     */
    public boolean isWordWrap() {
        return wordWrap;
    }

    /**
     * Word wrap text in this column?
     * @return
     */
    public void setWordWrap(boolean wordWrap) {
        this.wordWrap = wordWrap;
    }
    /**
     * The horizontal alignment of this column's header
     * @return
     */
    public Alignment getHeaderAlignment() {
        return headerAlignment;
    }
    /**
     * The horizontal alignment of this column's header
     * @return
     */
    public void setHeaderAlignment(Alignment headerAlignment) {
        this.headerAlignment = headerAlignment;
    }

    public TablePrintable getParent() {
        return parent;
    }
    
    /**
     * Returns this column's index in the table model.
     * @return
     */
    public int getModelIndex() {
        return index;
    }

    public Font getEffectiveFont() {
        return (getFont() == null) ? parent.getTableFont() : getFont();
    }
    
    public Font getEffectiveHeaderFont() {
        return (getHeaderFont() == null) ? parent.getHeaderFont() : getHeaderFont();
    }
    
    public Format getEffectiveFormat() {
        if (getColumnFormat() != null) { 
            return getColumnFormat();
        } else {
            return parent.getFormatMap().get(getColumnClass());
        }
    }

    public String getHeaderText() {
        return parent.getModel().getColumnName(index);
    }
    
    public Object getData(int rowIndex) {
        return parent.getModel().getValueAt(rowIndex, index);
    }
    
    protected Class<?> getColumnClass() {
        return parent.getModel().getColumnClass(index);
    }
    
    public TableCellRenderer getEffectiveRenderer() {
        TableCellRenderer rv = parent.getRendererMap().get(getColumnClass());
        return (rv == null) ? parent.getDefaultRenderer() : rv;
    }
    
    /**
     * Returns the effective column width. Only valid after drawTable has been started (i.e. you may use it in a TableCellRenderer)
     * @return
     */
    public double getEffectiveColumnWidth() {
        return effectiveColumnWidth;
    }
    
    /**
     * Sets the effective column width. For use with a TablePrintable
     * @param effectiveColumnWidth
     */
    public void setEffectiveColumnWidth(double effectiveColumnWidth) {
        this.effectiveColumnWidth = effectiveColumnWidth;
    }
    
    protected TablePrintColumn(TablePrintable parent, int index) {
        super();
        this.parent = parent;
        this.index = index;
        
        Class<?> colClass = getColumnClass();
        if (Number.class.isAssignableFrom(colClass)) {
            setAlignment(Alignment.RIGHT);
        } else if (colClass == Boolean.class) {
            setAlignment(Alignment.CENTER);
        } else {
            setAlignment(Alignment.LEFT);
        }
    }
}
