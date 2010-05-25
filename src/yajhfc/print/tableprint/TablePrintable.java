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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableModel;

import yajhfc.DateKind;
import yajhfc.Utils;
import yajhfc.print.tableprint.TableCellRenderer.ColumnPageData;

/**
 * @author jonas
 *
 */
public class TablePrintable implements Printable {
    
    protected TableModel model;
    protected Font tableFont;
    protected Font headerFont;
    protected HeaderPrintMode headerPrintMode = HeaderPrintMode.PRINT_ALWAYS;
    protected ResizeMode resizeMode = ResizeMode.RESIZE_SHRINK_ONLY;
    
    protected final Map<Alignment,MessageFormat> pageHeader = new EnumMap<Alignment, MessageFormat>(Alignment.class);
    protected final Map<Alignment,MessageFormat> pageFooter = new EnumMap<Alignment, MessageFormat>(Alignment.class);
    protected Font pageHeaderFont;
    protected Font pageFooterFont;
    
    protected float lineWidth = 0.125f;
    protected float headerLineWidth = 0.125f;
    
    protected float cellInsetX = -0.333f;
    protected float cellInsetY = -0.333f;
    
    protected final Map<Class<?>,Format> formatMap = new HashMap<Class<?>,Format>();
    protected final Map<Class<?>,TableCellRenderer> rendererMap = new HashMap<Class<?>,TableCellRenderer>();
    
    protected TableCellRenderer defaultRenderer = new DefaultCellRenderer();
    
    protected TableCellRenderer headerRenderer = new DefaultHeaderRenderer();
    
    protected Color headerBackground = new Color(240,240,240);
    protected Color[] cellBackground = null;
    
    protected ColumnLayout columnLayout;
    protected CellFormatModel formatModel = new DefaultCellFormatModel();
    
    // Status variables
    protected int currentRow; 
    protected int lastStartRow;
    protected int currentPage;
    protected final List<ColumnPageData[]> pageData = new ArrayList<ColumnPageData[]>();
    
    public TablePrintable(TableModel model) {
    	this(model, new SimpleColumnLayout());
    }
    
    public TablePrintable(TableModel model, ColumnLayout colLayout) {
    	this.columnLayout = colLayout;
    	
        pageFooter.put(Alignment.LEFT, new MessageFormat("'" + DateKind.DATE_AND_TIME.getFormat().format(new Date()) + "'"));
        pageFooter.put(Alignment.RIGHT, new MessageFormat(Utils._("page {0}")));
        
        tableFont  = new Font("sans-serif", Font.PLAIN, 10);
        headerFont = new Font("sans-serif", Font.BOLD, 10);
        
        pageHeaderFont = new Font("sans-serif", Font.BOLD, 11);
        pageFooterFont = new Font("sans-serif", Font.PLAIN, 9);
        
        formatMap.put(Date.class, DateKind.DATE_AND_TIME.getFormat());
        //formatMap.put(Boolean.class, new BooleanFormat());
        rendererMap.put(Boolean.class, new BooleanRenderer());
        
        if (model != null)
        	setModel(model);
    }
    
    /* (non-Javadoc)
     * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        Graphics2D g2d = (Graphics2D)graphics;
        Rectangle2D.Double imageableArea = new Rectangle2D.Double(pageFormat.getImageableX(), pageFormat.getImageableY(), pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
        if (pageHeader.size() > 0) {
            g2d.setFont(pageHeaderFont);
            double headerHeight   = drawHeaderOrFooter(g2d, pageHeader, imageableArea, pageIndex, false) + pageHeaderFont.getSize2D();
            imageableArea.y      += headerHeight;
            imageableArea.height -= headerHeight;
        }
        if (pageFooter.size() > 0) {
            g2d.setFont(pageFooterFont);
            imageableArea.height -= drawHeaderOrFooter(g2d, pageFooter, imageableArea, pageIndex, true) + pageFooterFont.getSize2D();
        }
        double maxLineWidth = Math.max(lineWidth, headerLineWidth);
        if (maxLineWidth <= 0) 
        	maxLineWidth = 1;
        else
        	maxLineWidth += 1;
        
        imageableArea.height -= maxLineWidth;
        imageableArea.width -= maxLineWidth;
        
        if (pageIndex == 0) { // First page
            currentRow = 0;
            lastStartRow = 0;
            currentPage = 0;
        } else if (pageIndex == currentPage) {
            currentRow = lastStartRow;
        } else {
            lastStartRow = currentRow;
            currentPage = pageIndex;
        }

        return (drawTable(g2d, imageableArea, pageIndex) == 0.0) ? NO_SUCH_PAGE : PAGE_EXISTS;
    }
    
    /**
     * Draw header or footer and return its height
     * @param g2d
     * @param fmts
     * @param imageableArea
     * @param pageIndex
     * @param isFooter
     * @return
     */
    protected double drawHeaderOrFooter(Graphics2D g2d, Map<Alignment,MessageFormat> fmts, Rectangle2D imageableArea, int pageIndex, boolean isFooter) {
        FontMetrics fm = g2d.getFontMetrics();
        double height = 0;
        
        Object[] formatArgs = { pageIndex+1 };
        Alignment[] alignments = Alignment.values();
        String[] texts = new String[alignments.length];
        Rectangle2D[] boxes = new Rectangle2D[alignments.length];
        
        // Measure heights
        for (Alignment al : alignments) {
            MessageFormat fmt = fmts.get(al);
            if (fmt != null) {
                String text = texts[al.ordinal()] = fmt.format(formatArgs);
                Rectangle2D box = boxes[al.ordinal()] = fm.getStringBounds(text, g2d);
                if (box.getHeight() > height)
                    height = box.getHeight();
            }
        }
        
        float y;
        if (isFooter) {
            y = (float)(imageableArea.getY() + imageableArea.getHeight() - height + fm.getAscent());
        } else {
            y = (float)(imageableArea.getY() + fm.getAscent());
        }
        if (fmts.containsKey(Alignment.LEFT)) {
            g2d.drawString(texts[Alignment.LEFT.ordinal()], (float)imageableArea.getX(), y);
        }
        if (fmts.containsKey(Alignment.CENTER)) {
            g2d.drawString(texts[Alignment.CENTER.ordinal()], (float)(imageableArea.getX() + (imageableArea.getWidth() - boxes[Alignment.CENTER.ordinal()].getWidth())/2), y);
        }
        if (fmts.containsKey(Alignment.RIGHT)) {
            g2d.drawString(texts[Alignment.RIGHT.ordinal()], (float)(imageableArea.getX() + imageableArea.getWidth() - boxes[Alignment.RIGHT.ordinal()].getWidth()), y);
        }
        return height;
    }
    
    /**
     * Draw the table into the given space
     * @param graphics
     * @param drawArea
     * @return the drawn height; positive if the table fit on the page; negative if there is more to draw or 0.0 if there was nothing to draw
     */
    public double drawTable(Graphics2D graphics, Rectangle2D drawArea, int pageIndex) {
        if (currentRow >= model.getRowCount()) {
            return 0.0;
        }
        
        graphics.translate(drawArea.getX(), drawArea.getY());
        
        final double spaceY;
        if (cellInsetY < 0.0f) {
            spaceY = tableFont.getSize2D() * -cellInsetY;
        } else {
            spaceY = cellInsetY;
        }
        final double spaceX;
        if (cellInsetX < 0.0f) {
            spaceX = tableFont.getSize2D() * -cellInsetX;
        } else {
            spaceX = cellInsetX;
        }
        
        if (pageIndex == 0) { 
            columnLayout.calculateColumnWidths(graphics, drawArea.getWidth(), spaceX);
            pageData.clear();
        }
        
        double tableWidth = columnLayout.getTableWidth();
        double maxHeight = drawArea.getHeight();
        double tableLeft;
        boolean resizeTable;
        switch (resizeMode) {
        case RESIZE_NEVER:
        default:
        	resizeTable = false;
        	break;
        case RESIZE_SHRINK_ONLY:
        	resizeTable = (tableWidth > drawArea.getWidth());
        	break;
        case RESIZE_GROW_ONLY:
        	resizeTable = (tableWidth < drawArea.getWidth());
        	break;
        case RESIZE_GROW_AND_SHRINK:
        	resizeTable = (tableWidth != drawArea.getWidth());
        	break;
        }
        
        double sf = 1.0;
        if (resizeTable) {
        	tableLeft = 0;
        	sf = drawArea.getWidth() / tableWidth;
        	graphics.scale(sf, sf);
        	maxHeight /= sf; // If we shrink the page, more lines fit on the page
        } else if (tableWidth < drawArea.getWidth()) {
        	tableLeft = (drawArea.getWidth() - tableWidth) / 2;
        } else {
        	tableLeft = 0;
        }
        double x = tableLeft; double y = 0;
        Line2D.Double line = new Line2D.Double();
        double[] lineHeights = new double[columnLayout.getMaximumColumnCount()];
        Rectangle2D.Double fillRect = new Rectangle2D.Double();
        
        boolean printHeader;
        switch (headerPrintMode) {
        case PRINT_ALWAYS:
            printHeader = true;
            break;
        case PRINT_NEVER:
        default:
            printHeader = false;
            break;
        case PRINT_ON_FIRST_PAGE:
            printHeader = (pageIndex == 0);
            break;         
        }
        if (printHeader) {
            if (headerLinesEnabled()) {
                Stroke s = new BasicStroke(getHeaderLineWidth());
                graphics.setStroke(s);
            }
            // Draw page header
            double lineHeight = 0;
            x = tableLeft;
            final TablePrintColumn[] headerLayout = columnLayout.getHeaderLayout();
            for (int i=0; i < headerLayout.length; i++) {
                TablePrintColumn column = headerLayout[i];    
                graphics.setFont(column.getEffectiveHeaderFont());
                double h = headerRenderer.drawCell(graphics, x, y, column.getHeaderText(), column,
                        headerBackground, spaceX, spaceY, maxHeight, false, null);
                lineHeights[i] = h;
                if (h > lineHeight)
                    lineHeight = h;
                x += column.effectiveColumnWidth;
            }
            double newY = y + lineHeight;
            
            if (headerBackground != null) {
                // Fill the parts of the columns that have less height than the maximum:
                x = tableLeft;
                for (int i=0; i<lineHeights.length; i++) {
                    final TablePrintColumn column = headerLayout[i];
                    if (lineHeights[i] < lineHeight) {
                        fillRect.x = x;
                        fillRect.width = column.getEffectiveColumnWidth();
                        fillRect.y = y + lineHeights[i];
                        fillRect.height = lineHeight - lineHeights[i];
                        Color oldColor = graphics.getColor();
                        graphics.setColor(headerBackground);
                        graphics.fill(fillRect);
                        graphics.setColor(oldColor);
                    }
                    x += column.getEffectiveColumnWidth();
                }
            }
       
            // Draw lines
            if (headerLinesEnabled()) {
                x = tableLeft;
                line.y1 = y;
                line.y2 = newY;
                for (int i=0; i < headerLayout.length; i++) {
                    line.x1 = line.x2 = x;
                    graphics.draw(line);
                    x += headerLayout[i].effectiveColumnWidth;
                }
                line.x1 = line.x2 = x;
                graphics.draw(line);
                line.x1 = tableLeft;
                line.x2 = tableLeft + tableWidth;
                line.y1 = line.y2 = y;
                graphics.draw(line);
                // Also draw a line below the header just in case grid lines are disabled
                line.y1 = line.y2 = newY;
                graphics.draw(line);
            }
            
            y = newY;                
        }
        
        if (gridLinesEnabled()) {
            Stroke s = new BasicStroke(getLineWidth());
            graphics.setStroke(s);
        }
        boolean pageContinuation = (pageIndex != 0);
        ColumnPageData[] newPageData = new ColumnPageData[columnLayout.getMaximumColumnCount()];
        ColumnPageData[] oldPageData;
        try {
            oldPageData = pageContinuation ? pageData.get(pageIndex-1) : null;
        } catch (ArrayIndexOutOfBoundsException e) {
            oldPageData = null;
        }        
        for (; currentRow < model.getRowCount(); currentRow++) {
            TablePrintColumn[] columns = columnLayout.getLayoutForRow(currentRow);
            double lineHeight = 0;
            Arrays.fill(lineHeights, 0);
            boolean needPagebreak = false;
            
            x = tableLeft;
            for (int col=0; col < columns.length; col++) {
                TablePrintColumn column = columns[col];
                
                ColumnPageData pd = null;
                if (pageContinuation) {
                    if (oldPageData != null)
                        pd = oldPageData[col];
                    if (pd == null)
                        pd = new ColumnPageData();
                    else
                        pd = (ColumnPageData)pd.clone();
                } else {
                    pd = newPageData[col];
                    if (pd == null) 
                        pd = new ColumnPageData();                      
                }

                graphics.setFont(formatModel.getCellFont(column, model, currentRow));
                double h = column.getEffectiveRenderer().drawCell(graphics, x, y, column.getData(currentRow),
                         column, formatModel.getCellBackgroundColor(column, model, currentRow), spaceX,
                         spaceY, maxHeight, pageContinuation, pd);
                newPageData[col] = pd;
                if (h == TableCellRenderer.DRAWCELL_NOTHING) {
                    needPagebreak = true;
                } else if (h == TableCellRenderer.DRAWCELL_NOTHING_REMAINED) {
                    // Do nothing
                } else if (h < 0.0) {
                    needPagebreak = true;
                    lineHeights[col] = -h;
                    if (-h > lineHeight)
                        lineHeight = -h;
                } else {
                    lineHeights[col] = h;
                    if (h > lineHeight)
                        lineHeight = h;
                }
                x += column.effectiveColumnWidth;
            }
            double newY = y + lineHeight;
            
            // Fill the parts of the columns that have less height than the maximum:
            x = tableLeft;
            for (int i=0; i<columns.length; i++) {
                final TablePrintColumn column = columns[i];
                if (lineHeights[i] < lineHeight) {
                    Color background = formatModel.getCellBackgroundColor(column, model, currentRow);
                    if (background != null) {
                        fillRect.x = x;
                        fillRect.width = column.getEffectiveColumnWidth();
                        fillRect.y = y + lineHeights[i];
                        fillRect.height = lineHeight - lineHeights[i];
                        Color oldColor = graphics.getColor();
                        graphics.setColor(background);
                        graphics.fill(fillRect);
                        graphics.setColor(oldColor);
                    }
                }
                x += column.getEffectiveColumnWidth();
            }
            

            // Draw lines
            if (gridLinesEnabled()) {
                x = tableLeft;
                line.y1 = y;
                line.y2 = newY;
                for (int i=0; i < columns.length; i++) {
                    line.x1 = line.x2 = x;
                    graphics.draw(line);
                    x += columns[i].effectiveColumnWidth;
                }
                line.x1 = line.x2 = x;
                graphics.draw(line);
                line.x1 = tableLeft;
                line.x2 = tableLeft + tableWidth;
                line.y1 = line.y2 = y;
                graphics.draw(line);
            }
            y = newY;
            if (needPagebreak) {
                break;
            }
            pageContinuation = false;
        }
        if (pageData.size() > pageIndex) {
            pageData.set(pageIndex, newPageData);
        } else {
            while (pageData.size() < pageIndex) {
                // Ensure we add at the right index; should actually never be necessary
                pageData.add(null);
            }
            pageData.add(newPageData);
        }
        if (gridLinesEnabled()) {
            line.x1 = tableLeft;
            line.x2 = tableLeft + tableWidth;
            line.y1 = line.y2 = y;
            graphics.draw(line);
        }
        
        return ((currentRow >= model.getRowCount()) ? y : -y) * sf;
    }
    
    protected boolean gridLinesEnabled() {
        return (lineWidth >= 0.0f);
    }
    
    protected boolean headerLinesEnabled() {
        return (headerLineWidth >= 0.0f);
    }
    
    /**
     * The table model the data comes from. The TablePrintable does *not* listen for
     * TableModelEvents, so it is your responsibility to make sure the structure of the table model
     * does not change between calling setModel and the actual printout.
     * @return
     */
    public TableModel getModel() {
        return model;
    }
    
    /**
     * The table model the data comes from. The TablePrintable does *not* listen for
     * TableModelEvents, so it is your responsibility to make sure the structure of the table model
     * does not change between calling setModel and the actual printout.
     * @return
     */
    public void setModel(TableModel model) {
        this.model = model;
        columnLayout.initializeLayout(this, model);
    }

    /**
     * The default font to use for table cells
     * @return
     */
    public Font getTableFont() {
        return tableFont;
    }
    /**
     * The default font to use for table cells
     */
    public void setTableFont(Font tableFont) {
        this.tableFont = tableFont;
    }

    /**
     * The default font to use for the table header
     * @return
     */
    public Font getHeaderFont() {
        return headerFont;
    }

    /**
     * The default font to use for the table header
     */
    public void setHeaderFont(Font headerFont) {
        this.headerFont = headerFont;
    }
    
    /**
     * The font to use for the page header
     */
    public Font getPageHeaderFont() {
        return pageHeaderFont;
    }
    /**
     * The font to use for the page header
     */
    public void setPageHeaderFont(Font pageHeaderFont) {
        this.pageHeaderFont = pageHeaderFont;
    }

    /**
     * The font to use for the page footer
     * @return
     */
    public Font getPageFooterFont() {
        return pageFooterFont;
    }

    /**
     * The font to use for the page footer
     */
    public void setPageFooterFont(Font pageFooterFont) {
        this.pageFooterFont = pageFooterFont;
    }

    /**
     * Line width of the grid lines. Negative values disable the grid lines
     * @param lineWidth
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Line width of the grid lines. Negative values disable the grid lines
     * @param lineWidth
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Returns a map to set the formatters to use to format data of the specified data type.
     * @return
     */
    public Map<Class<?>, Format> getFormatMap() {
        return formatMap;
    }

    /**
     * Returns a map to set the renderers to use to render data of the specified data type.
     * @return
     */
    public Map<Class<?>, TableCellRenderer> getRendererMap() {
        return rendererMap;
    }
    
    /**
     * Returns a map to set MessageFormat instances to create a page footer.
     * The MessageFormats are given a single Integer parameter specifying the page number.
     * @return
     */
    public Map<Alignment, MessageFormat> getPageFooter() {
        return pageFooter;
    }
    
    /**
     * Returns a map to set MessageFormat instances to create a page header.
     * The MessageFormats are given a single Integer parameter specifying the page number.
     * @return
     */
    public Map<Alignment, MessageFormat> getPageHeader() {
        return pageHeader;
    }
    
    /**
     * When to print a page header
     */
    public void setHeaderPrintMode(HeaderPrintMode headerPrintMode) {
        this.headerPrintMode = headerPrintMode;
    }
    
    /**
     * When to print a page header
     * @return
     */
    public HeaderPrintMode getHeaderPrintMode() {
        return headerPrintMode;
    }
    
    /**
     * The background color of the table header
     */
    public Color getHeaderBackground() {
        return headerBackground;
    }
    
    /**
     * The background color of the table header
     * @param headerBackground
     */
    public void setHeaderBackground(Color headerBackground) {
        this.headerBackground = headerBackground;
    }
    
    /**
     * Sets the cell background.
     * The logic is as follows:
     * For row number x the color cellBackground[x%cellBackground.length] is used.
     * @return
     */
    public Color[] getCellBackground() {
        return cellBackground;
    }
    
    /**
     * Sets the cell background.
     * The logic is as follows:
     * For row number x the color cellBackground[x%cellBackground.length] is used.
     * @return
     */
    public void setCellBackground(Color[] cellBackground) {
        this.cellBackground = cellBackground;
    }
    
    /**
     * How to resize tables larger or smaller than the page
     * @return
     */
    public ResizeMode getResizeMode() {
		return resizeMode;
	}
    
    /**
     * How to resize tables larger or smaller than the page
     * @return
     */
    public void setResizeMode(ResizeMode resizeMode) {
		this.resizeMode = resizeMode;
	}

    /**
     * The renderer used to paint the table header cells
     */
    public TableCellRenderer getHeaderRenderer() {
        return headerRenderer;
    }

    /**
     * The renderer used to paint the table header cells
     */
    public void setHeaderRenderer(TableCellRenderer headerRenderer) {
        this.headerRenderer = headerRenderer;
    }

    /**
     * The column layout for this table
     */
    public void setColumnLayout(ColumnLayout columnLayout) {
        this.columnLayout = columnLayout;
        if (model != null) {
            columnLayout.initializeLayout(this, model);
        }
    }
    
    /**
     * The column layout for this table
     */
    public ColumnLayout getColumnLayout() {
        return columnLayout;
    }
    
    /**
     * The default cell renderer to use when no special renderer can be found
     */
    public TableCellRenderer getDefaultRenderer() {
        return defaultRenderer;
    }
    
    /**
     * The default cell renderer to use when no special renderer can be found
     */
    public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    /**
     * The width of the lines around the column header. Negative values disable the lines.
     */
    public float getHeaderLineWidth() {
        return headerLineWidth;
    }
    
    /**
     * The horizontal cell inset.
     * Positive values give absolute insets, negative values parts of the line height
     */
    public float getCellInsetX() {
        return cellInsetX;
    }
    
    /**
     * The vertical cell inset.
     * Positive values give absolute insets, negative values parts of the line height
     */
    public float getCellInsetY() {
        return cellInsetY;
    }

    /**
     * The format model to format the individual cells
     * @return
     */
    public CellFormatModel getFormatModel() {
        return formatModel;
    }
    
    /**
     * The format model to format the individual cells
     */
    public void setFormatModel(CellFormatModel formatModel) {
        this.formatModel = formatModel;
    }

    /**
     * The width of the lines around the column header. Negative values disable the lines.
     * @param headerLineWidth
     */
    public void setHeaderLineWidth(float headerLineWidth) {
        this.headerLineWidth = headerLineWidth;
    }

    /**
     * Sets the horizontal cell inset.
     * Positive values give absolute insets, negative values parts of the line height
     * @param cellInsetX
     */
    public void setCellInsetX(float cellInsetX) {
        this.cellInsetX = cellInsetX;
    }

    /**
     * Sets the vertical cell inset.
     * Positive values give absolute insets, negative values parts of the line height
     * @param cellInsetX
     */
    public void setCellInsetY(float cellInsetY) {
        this.cellInsetY = cellInsetY;
    }
   
}
