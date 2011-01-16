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

import java.io.File;
import java.text.DateFormat;
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
import org.w3c.dom.Text;

import yajhfc.DateKind;
import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.server.ServerManager;
import yajhfc.util.ExceptionDialog;

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
			TableModel model = parent.getSelectedTable().getModel();
			
			saveToResult(sr, model, title, footer);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table as HTML:"), ex);
        } finally {
            Utils.unsetWaitCursor(null);
        }
	}

	protected static void saveToResult(Result result, TableModel tableModel, String title, String footer) throws ParserConfigurationException, TransformerException {
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
        
        appendTable(doc, body, tableModel);
        
        Element p = doc.createElement("p");
        Text text1 = doc.createTextNode(footer);
        Text text2 = doc.createTextNode(Utils._("Saved at:") + ' ' + DateKind.DATE_AND_TIME.getFormat().format(new Date()));
        p.appendChild(text1);
        p.appendChild(doc.createElement("br"));
        p.appendChild(text2);
        body.appendChild(p);
        
        root.appendChild(body);        
        //////////////////////////////
        
        //root.normalize();
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);

        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, DOCTYPE_PUBLIC);
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, SYSTEM_ID);
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.transform(source, result);
    }
	
    private static final long TIME_THRESHOLD = (36L*3600L*1000L);
	protected static void appendTable(Document doc, Element parent, TableModel model) {
    		DateFormat dateFormat = DateKind.DATE_AND_TIME.getFormat();
        	DateFormat timeFormat = DateKind.TIME_ONLY.getFormat();
		
	        Element table = doc.createElement("table");
	        
	        // Create table header
	        Element thead = doc.createElement("thead");
	        Element tr = doc.createElement("tr");
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
	            for (int col=0; col<model.getColumnCount(); col++) {
	                Object val = model.getValueAt(row, col);
	                String text;
	                if (val instanceof Date) {
	                    Date dVal = (Date)val;
	                    if (dVal.getTime() > -TIME_THRESHOLD && dVal.getTime() < TIME_THRESHOLD) { // If it's a date around 1970-01-01, assume it's time only
							text = timeFormat.format(dVal);
	                    } else {
							text = dateFormat.format(dVal);
	                    }
	                } else if (val != null) {
	                	text = val.toString();
	                } else {
	                	text = "";
	                }
	                
	                Element td = doc.createElement("td");
	                td.setTextContent(text);
	                tr.appendChild(td);
	            }
	            tbody.appendChild(tr);
	        }
	        table.appendChild(tbody);
	        
	        parent.appendChild(table);
	}
}
