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
import java.awt.Dialog;
import java.awt.Font;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;

import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
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
import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.table.FaxListTableModel;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
import yajhfc.server.ServerManager;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.TableSorter;

/**
 * @author jonas
 *
 */
public class ExportHTMLAction {
    private static final String SYSTEM_ID = "http://www.w3.org/TR/html4/strict.dtd"; //"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
    private static final String DOCTYPE_PUBLIC = "-//W3C//DTD HTML 4.01//EN";//"-//W3C//DTD XHTML 1.0 Strict//EN";

    public static void exportToHTML(MainWin parent, File selectedFile) {
        Utils.setWaitCursor(null);
        try {
            StreamResult sr = new StreamResult(selectedFile);
            String title = parent.getSelectedTableDescription();
            String footer = Utils._("Server") + ": " + ServerManager.getDefault().getCurrent().toString();
            final TooltipJTable<? extends FmtItem> selectedTable = parent.getSelectedTable();

            saveToResult(sr, selectedTable.getModel(), new TooltipJTableCellFormatter(selectedTable), title, footer);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table:"), ex);
        } finally {
            Utils.unsetWaitCursor(null);
        }
    }

    public static void exportPhonebookToHTML(Dialog parent, PhoneBook pb, File selectedFile) {
        Utils.setWaitCursor(null);
        try {
            StreamResult sr = new StreamResult(selectedFile);
            String title = pb.toString();
            String footer = "";

            saveToResult(sr, new PBEntryFieldTableModel(Collections.<PBEntryFieldContainer>unmodifiableList(pb.getEntries())), 
                    new PhoneBookCellFormatter(), title, footer);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table:"), ex);
        } finally {
            Utils.unsetWaitCursor(null);
        }
    }
    
    protected static void saveToResult(Result result, TableModel tableModel, CellFormatter formatter, String title, String footer) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        DOMImplementation domImpl = docBuilder.getDOMImplementation();

        DocumentType docType = domImpl.createDocumentType("html", DOCTYPE_PUBLIC, SYSTEM_ID);

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

        appendTable(doc, body, tableModel, formatter, null);

        Element p = doc.createElement("p");
        Text text1 = doc.createTextNode(footer);
        Text text2 = doc.createTextNode(MessageFormat.format(Utils._("List saved at {0}."), DateKind.DATE_AND_TIME.getFormat().format(new Date())));
        p.appendChild(text1);
        p.appendChild(doc.createElement("br"));
        p.appendChild(text2);
        body.appendChild(p);

        root.appendChild(body);        
        //////////////////////////////

        //root.normalize();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, DOCTYPE_PUBLIC);
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, SYSTEM_ID);
        transformer.setOutputProperty(OutputKeys.METHOD, "html");

        DOMSource source = new DOMSource(doc);

        transformer.transform(source, result);
    }

    private static final String headerBackground = "#DADADA";
    private static final String[] colBackground = {
        null,
        "#F0F0F0",
    };
    
    protected static void appendTable(Document doc, Element parent, TableModel model, CellFormatter formatter, String headerStyle) {
        Element table = doc.createElement("table");
        //table.setAttribute("border", "1");

        Element tr;
        // Create table header
        Element thead = doc.createElement("thead");
        if (headerStyle == null) {
            thead.setAttribute("bgcolor", headerBackground);
        } else {
            thead.setAttribute("style", headerStyle);
        }
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
            String bgcolor = colBackground[row%colBackground.length];
            if (bgcolor != null)
                tr.setAttribute("bgcolor", bgcolor);
            
            for (int col=0; col<model.getColumnCount(); col++) {           
                Node td = formatter.createTDElement(doc, model, row, col);
                if (td != null)
                    tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);

        parent.appendChild(table);
    }
    
    protected static class CellFormatter {
        public Node createTDElement(Document doc, TableModel model, int row, int col) {
            Object val = model.getValueAt(row, col);     
            
            String text;
            if (val instanceof Date) {
                text = getDateFormat(col).format(val);
            } else if (val instanceof Boolean) {
                text = ((Boolean)val).booleanValue() ? "X" : " ";
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
            
            String style = getCSSStyle(row, col);
            
            return createTDElement(doc, text, align, style);
        }

        protected Node createTDElement(Document doc, String text, String align, String style) {
            Element td = doc.createElement("td");
            if (align != null)
                td.setAttribute("align", align);
            if (style != null)
                td.setAttribute("style", style);
            if (text.indexOf('\n') < 0) {
                td.setTextContent(text);
            } else {
                for (String line : Utils.fastSplit(text, '\n')) {
                    td.appendChild(doc.createTextNode(line));
                    td.appendChild(doc.createElement("br"));
                }
            }
            return td;
        }
        
        protected DateFormat getDateFormat(int colIndex) {
            return DateFormat.getDateTimeInstance();
        }
        protected Color getCellBackground(int row, int col) {
            return null;
        }
        protected Color getCellForeground(int row, int col) {
            return null;
        }
        protected Font getFont(int row, int col) {
            return null;
        }        
        
        public String getCSSStyle(int row, int col) {
            Color bg = getCellBackground(row, col);
            Color fg = getCellForeground(row, col);
            Font font = getFont(row, col);
            
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
    
    protected static class TooltipJTableCellFormatter extends CellFormatter {
        protected final TableSorter sorter;
        protected final FaxListTableModel<? extends FmtItem> realModel;
        
        @Override
        protected DateFormat getDateFormat(int colIndex) {
            return realModel.getColumns().get(colIndex).getDisplayDateFormat();
        }
        
        @Override
        protected Color getCellBackground(int row, int col) {
            int realRow = sorter.modelIndex(row);
            return realModel.getCellBackgroundColor(realRow, col);
        }
        
        @Override
        protected Color getCellForeground(int row, int col) {
            int realRow = sorter.modelIndex(row);
            return realModel.getCellForegroundColor(realRow, col);
        }
        
        @Override
        protected Font getFont(int row, int col) {
            int realRow = sorter.modelIndex(row);
            return realModel.getCellFont(realRow, col);
        }
        
        public TooltipJTableCellFormatter(TooltipJTable<? extends FmtItem> table) {
            sorter = table.getSorter();
            realModel = table.getRealModel();
        }
    }
    
    protected static class PhoneBookCellFormatter extends CellFormatter {
        @Override
        public Node createTDElement(Document doc, TableModel model, int row,
                int col) {
            PBEntryFieldTableModel pbeModel = (PBEntryFieldTableModel)model;
            PBEntryFieldContainer pbec = pbeModel.getRow(row);
            if (pbec instanceof DistributionList) {
                if (col == 0) {
                    return createTDElement(doc, pbec.getField(PBEntryField.Name), null, "font-style: italic; background-color:" + headerBackground + ";");
                } else if (col == 1) {
                    Element td = doc.createElement("td");
                    td.setAttribute("colspan", String.valueOf(model.getColumnCount() - 1));
                    appendTable(doc, td, 
                            new PBEntryFieldTableModel(Collections.<PBEntryFieldContainer>unmodifiableList(((DistributionList)pbec).getEntries())),
                            this, "font-style: italic; background-color:" + headerBackground + ";");
                    return td;
                } else {
                    return null;
                }
            } else {
                String text = (String)model.getValueAt(row, col);
                String style;
                if (pbeModel.getColumn(col) == PBEntryField.Comment) {
                    style = "font-family:monospace;";
                } else {
                    style = null;
                }
                return createTDElement(doc, text, null, style);
            }
        }
    }
}
