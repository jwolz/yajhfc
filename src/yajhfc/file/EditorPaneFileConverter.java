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
 */
package yajhfc.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.print.DocFlavor;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

import yajhfc.PaperSize;
import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class EditorPaneFileConverter extends PrintServiceFileConverter {
    public static final EditorPaneFileConverter HTML_CONVERTER = new EditorPaneFileConverter("text/html");
    
    protected String contentType;
    
    public EditorPaneFileConverter(String contentType) {
        super(DocFlavor.SERVICE_FORMATTED.PRINTABLE);
        this.contentType = contentType;
    }
    
    
    @Override
    public void convertToHylaFormat(File inFile, OutputStream destination,
            PaperSize paperSize, FileFormat desiredFormat) throws ConversionException {
        try {
            URL inURL = inFile.toURI().toURL();
            convertToHylaFormat(inURL, destination, paperSize, inURL);
        } catch (MalformedURLException e) {
            throw new ConversionException(e);
        } 
    }
    
    public void convertToHylaFormat(URL inURL, OutputStream destination,
            PaperSize paperSize, URL baseURL) throws ConversionException {
        try {            
            PrintableEditorPane pep = new PrintableEditorPane(paperSize.getSize());
     
            pep.loadURL(inURL, contentType, baseURL);
            convertUsingPrintService(pep, destination, paperSize);
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }
    
    public static class PrintableEditorPane extends JEditorPane implements Printable {
        private static final int assumedBordersMM = 0; //44;
        
        public PrintableEditorPane(Dimension pageSizeMM){
            super();
            setDoubleBuffered(false);
            this.setSize((int)((pageSizeMM.width - assumedBordersMM)*72/25.4), (int)((pageSizeMM.height - assumedBordersMM)*72/25.4));
        }

        public void loadURL(URL url, String contentType, URL baseURL)  throws IOException {
            setContentType(contentType);
            if (contentType.contains("text/html")) {
                setEditorKit(new SyncHTMLEditorKit());
                HTMLDocument hdoc = new FixedBaseHTMLDocument(baseURL);
                hdoc.putProperty(Document.StreamDescriptionProperty, url);
                read(url.openStream(), hdoc);
            } else {
                read(url.openStream(), baseURL);
            }
        }
        
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                throws PrinterException {
            Graphics2D g2 = (Graphics2D)graphics;
            g2.setColor (Color.black);

            Dimension pD = getPreferredSize();
            double panelWidth = getWidth();
            double panelHeight = pD.height;
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();
            double scale = pageWidth / panelWidth;
            
            int totalNumPages = (int)Math.ceil(scale * panelHeight /  pageHeight);

            if (Utils.debugMode) {
                Logger log = Logger.getLogger(PrintableEditorPane.class.getName());
                log.fine(String.format("Panel: Size : %d x %d; Preferred Size = %d x %d", getWidth(), getHeight(), pD.width, pD.height));
                log.fine(String.format("ImageableWidth: %f, ImageableHeight: %f, pageIndex: %d, totalNumPages: %d", pageWidth, pageHeight, pageIndex, totalNumPages));
            }
            
            // Check for empty pages
            if (pageIndex >= totalNumPages) return Printable.NO_SUCH_PAGE;

            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY() - pageIndex * pageHeight);
            g2.scale(scale, scale);
            this.paint(g2);

            return Printable.PAGE_EXISTS;
        }
    }
    
    public static class FixedBaseHTMLDocument extends HTMLDocument {
        protected URL fixedBase;
        
        public FixedBaseHTMLDocument(URL fixedBase) {
            super();
            this.fixedBase = fixedBase;
            setAsynchronousLoadPriority(-1);
        }
        
        @Override
        public URL getBase() {
            return fixedBase;
        }
    }
    
    public static class SyncHTMLEditorKit extends HTMLEditorKit {
        protected static final HTMLFactory factory = new SyncHTMLFactory();
        
        @Override
        public ViewFactory getViewFactory() {
            return factory;
        } 
    }
    
    public static class SyncHTMLFactory extends HTMLFactory {
        @Override
        public View create(Element elem) {
            Object kind = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (kind == HTML.Tag.IMG) {
                ImageView iv = new ImageView(elem);
                iv.setLoadsSynchronously(true);
                return iv;
            } else {
                return super.create(elem);
            } 
        }
    }
}
