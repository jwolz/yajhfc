/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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
package yajhfc.export;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import yajhfc.DateKind;
import yajhfc.Utils;
import yajhfc.model.IconMap;

/**
 * @author jonas
 *
 */
public class HTMLExporter {
    protected String systemID = "http://www.w3.org/TR/html4/strict.dtd"; //"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
    protected String docTypePublic = "-//W3C//DTD HTML 4.01//EN";//"-//W3C//DTD XHTML 1.0 Strict//EN";
    protected String outputMethod = "html";
    protected String headerBackground = "#DADADA";
    protected String[] columnBackground = {
            null,
            "#F0F0F0",
    };
    protected boolean exportImages = true;
    

    public String getSystemID() {
        return systemID;
    }

    public String getDocTypePublic() {
        return docTypePublic;
    }

    public String getOutputMethod() {
        return outputMethod;
    }

    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }

    public void setDocTypePublic(String docTypePublic) {
        this.docTypePublic = docTypePublic;
    }

    public void setOutputMethod(String outputMethod) {
        this.outputMethod = outputMethod;
    }

    public String getHeaderBackground() {
        return headerBackground;
    }

    public String[] getColumnBackground() {
        return columnBackground;
    }
    public void setHeaderBackground(String headerBackground) {
        this.headerBackground = headerBackground;
    }

    public void setColumnBackground(String[] columnBackground) {
        this.columnBackground = columnBackground;
    }
    
    public void setExportImages(boolean exportImages) {
        this.exportImages = exportImages;
    }
    
    public boolean isExportImages() {
        return exportImages;
    }
    
    /**
     * Saves the table model to the specified file in HTML format
     * @param outputFile
     * @param tableModel
     * @param title
     * @param footer
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public void saveToFile(File outputFile, TableModel tableModel, String title, String footer) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        DOMImplementation domImpl = docBuilder.getDOMImplementation();
        ImageExportManager imgExp;
        if (exportImages) {
            imgExp = new ImageExportManager(outputFile);   
        } else {
            imgExp = null;
        }

        DocumentType docType = domImpl.createDocumentType("html", docTypePublic, systemID);

        //Document doc = domImpl.createDocument("http://www.w3.org/1999/xhtml", "html", docType);
        Document doc = domImpl.createDocument(null, "html", docType);

        Element root = doc.getDocumentElement(); // html

        //// Create the head
        Element head = doc.createElement("head");

        Element titleEl = doc.createElement("title");
        titleEl.setTextContent(title);
        head.appendChild(titleEl);

        root.appendChild(head);
        ////////////////////////////

        //// Create the body
        Element body = doc.createElement("body");

        Element h1 = doc.createElement("h1");
        h1.setTextContent(title);
        body.appendChild(h1);

        appendTable(doc, body, tableModel, null, imgExp);

        // Footer:
        body.appendChild(doc.createElement("hr"));
        
        Element p = doc.createElement("p");
        Text text1 = doc.createTextNode(footer);
        Text text2 = doc.createTextNode(MessageFormat.format(Utils._("List saved at {0}."), DateKind.DATE_AND_TIME.getFormat().format(new Date())));
        Element app = doc.createElement("a");
        app.setAttribute("href", Utils.HomepageURL);
        app.setTextContent(Utils.AppShortName + " " + Utils.AppVersion);
        
        p.appendChild(text1);
        p.appendChild(doc.createElement("br"));
        p.appendChild(text2);
        p.appendChild(doc.createElement("br"));
        p.appendChild(app);
        body.appendChild(p);

        root.appendChild(body);        
        //////////////////////////////

        //root.normalize();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docTypePublic);
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemID);
        transformer.setOutputProperty(OutputKeys.METHOD, outputMethod);

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        
        transformer.transform(source, result);
    }

    protected void appendTable(Document doc, Element parent, TableModel model, String headerStyle, ImageExportManager imgExp) {
        Element table = doc.createElement("table");
        //table.setAttribute("border", "1");

        Element tr;
        // Create table header
        Element thead = doc.createElement("thead");
        if (headerStyle == null) {
            headerStyle = "background-color:" + headerBackground + ";";
        } 
        thead.setAttribute("style", headerStyle);
        
        tr = doc.createElement("tr");   
        for (int i=0; i<model.getColumnCount(); i++) {
            Element th = doc.createElement("th");
            th.setTextContent(model.getColumnName(i));
            tr.appendChild(th);
        }
        thead.appendChild(tr);
        table.appendChild(thead);

        // Create table body
        Element tbody = doc.createElement("tbody");
        for (int row=0; row<model.getRowCount(); row++) {
            tr = doc.createElement("tr");
            String bgcolor = columnBackground[row%columnBackground.length];
            if (bgcolor != null)
                tr.setAttribute("style", "background-color:" + bgcolor + ";");

            for (int col=0; col<model.getColumnCount(); col++) {           
                Node td = createTDElement(doc, model, row, col, imgExp);
                if (td != null)
                    tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);

        parent.appendChild(table);
    }

    protected Node createTDElement(Document doc, TableModel model, int row, int col, ImageExportManager imgExp) {
        Object val = model.getValueAt(row, col);     

        Node firstChild = null;
        String text;
        if (val instanceof Date) {
            text = getDateFormat(model, col).format(val);
        } else if (val instanceof Boolean) {
            text = ((Boolean)val).booleanValue() ? "X" : " ";
        } else if (val instanceof IconMap) {
            IconMap im = (IconMap)val;
            if (imgExp != null) {
                text = " " + im.getText();
                String imgPath = imgExp.getRelativePathFor(im.getDisplayIcon().getImage());
                if (imgPath != null) {
                    Element img = doc.createElement("img");
                    img.setAttribute("alt", im.getText());
                    img.setAttribute("src", imgPath);
                    firstChild = img;
                }
            } else {
                text = im.getText();
            }
        } else if (val != null) {
            text = val.toString();
        } else {
            text = "";
        }

        String align;
        if (val instanceof Number) {
            align = "right";
        } else if (val instanceof Boolean) {
            align = "center";
        } else {
            align = null;
        }

        String style = getCSSStyle(model, row, col);

        return createTDElement(doc, text, align, style, firstChild);
    }

    protected Node createTDElement(Document doc, String text, String align, String style, Node firstChild) {
        Element td = doc.createElement("td");
        if (align != null)
            td.setAttribute("align", align);
        if (style != null)
            td.setAttribute("style", style);
        
        if (firstChild != null) {
            td.appendChild(firstChild);
        }
        if (text.indexOf('\n') < 0) {
            td.appendChild(doc.createTextNode(text));
        } else {
            for (String line : Utils.fastSplit(text, '\n')) {
                td.appendChild(doc.createTextNode(line));
                td.appendChild(doc.createElement("br"));
            }
        }
        return td;
    }

    protected DateFormat getDateFormat(TableModel model, int colIndex) {
        return DateFormat.getDateTimeInstance();
    }

    protected Color getCellBackground(TableModel model, int row, int col) {
        return null;
    }
    protected Color getCellForeground(TableModel model, int row, int col) {
        return null;
    }
    protected Font getFont(TableModel model, int row, int col) {
        return null;
    }        

    protected String getCSSStyle(TableModel model, int row, int col) {
        Color bg = getCellBackground(model, row, col);
        Color fg = getCellForeground(model,row, col);
        Font font = getFont(model, row, col);

        if (bg == null && fg == null && font == null) {
            return null;
        } else {
            StringBuilder out = new StringBuilder();
            if (bg != null) {
                out.append("background-color:#");
                out.append(Integer.toHexString(bg.getRGB() & 0xffffff));
                out.append(';');
            }
            if (fg != null) {
                out.append("color:#");
                out.append(Integer.toHexString(fg.getRGB() & 0xffffff));
                out.append(';');
            }
            if (font != null) {
                int style = font.getStyle();
                if ((style & Font.BOLD) != 0) {
                    out.append("font-weight: bold;");
                }
                if ((style & Font.ITALIC) != 0) {
                    out.append("font-style: italic;");
                }
                // TODO: More font attributes if needed...
            }
            return out.toString();
        }
    }
}
