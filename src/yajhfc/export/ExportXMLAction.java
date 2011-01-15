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

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.IconMap;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class ExportXMLAction extends ExcDialogAbstractAction {
	protected final MainWin parent;
	private JFileChooser fileChooser;
	
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new SafeJFileChooser();
			fileChooser.resetChoosableFileFilters();
			FileFilter csvFilter = new ExampleFileFilter("xml", Utils._("XML files"));;
			fileChooser.addChoosableFileFilter(csvFilter);
			fileChooser.setFileFilter(csvFilter);
		}
		return fileChooser;
	}

	/* (non-Javadoc)
	 * @see yajhfc.util.ExcDialogAbstractAction#actualActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	protected void actualActionPerformed(ActionEvent e) {
		JFileChooser chooser = getFileChooser();
		if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Utils.setWaitCursor(null);
		try {
		    FaxJobList<?> jobList = parent.getSelectedTable().getRealModel().getJobs();
		    StreamResult out = new StreamResult(Utils.getSelectedFileFromSaveChooser(chooser));
		    saveToResult(out, jobList);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving table as XML:"), ex);
        } finally {
            Utils.unsetWaitCursor(null);
        }
	}
	
    protected static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    protected static TransformerFactory TRANSFORMER_FACTORY;
    
    protected static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        if (DOCUMENT_BUILDER_FACTORY == null) {
            DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        }
        return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
    }
    
    protected static TransformerFactory getTransformerFactory() {
        if (TRANSFORMER_FACTORY == null) {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
        }
        return TRANSFORMER_FACTORY;
    }

	protected void saveToResult(Result result, FaxJobList<? extends FmtItem> faxList) throws ParserConfigurationException, TransformerException {
        Document doc = createDocumentBuilder().newDocument();

        Element root = doc.createElement("faxlist");
        root.setAttribute("xmlns", "http://yajhfc.berlios.de/schema/tableexport");
        root.setAttribute("tableType", faxList.getJobType().name());
        
        Element el;
        el = doc.createElement("columns");
        saveColumns(faxList.getColumns(), el, doc);
        root.appendChild(el);
        
        el = doc.createElement("contents");
        saveRows(faxList, el, doc);
        root.appendChild(el);
        
        doc.appendChild(root);

        root.normalize();
        
        TransformerFactory tFactory = getTransformerFactory();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(doc);

        transformer.transform(source, result);
    }
	
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	protected void saveRows(FaxJobList<? extends FmtItem> faxList, Element root, Document doc) {
	    List<? extends FmtItem> cols = faxList.getColumns().getCompleteView();
	    for (FaxJob<?> job : faxList.getJobs()) {
	        Element rowEl = doc.createElement("row");
	        rowEl.setAttribute("id", job.getIDValue().toString());
	        for (int i=0; i<cols.size(); i++) {
	            Element cellEl = doc.createElement("cell");
	            
	            Class<?> dataType = cols.get(i).getDataType();
	            if (Date.class.isAssignableFrom(dataType)) {
	                Date data = (Date)job.getData(i);
	                cellEl.setAttribute("rawValue", String.valueOf(data.getTime()));
	                
	                cellEl.setTextContent(DATE_FORMAT.format(data));
	            } else {
	                cellEl.setTextContent(job.getData(i).toString());
	            }
	            
	            rowEl.appendChild(cellEl);
	        }
	        
	        root.appendChild(rowEl);
	    }
	}
	
	protected void saveColumns(FmtItemList<? extends FmtItem> cols, Element root, Document doc) {
	    int visiCount = cols.size();
	    List<? extends FmtItem> completeView = cols.getCompleteView();
	    for (int i=0; i<completeView.size(); i++) {
	        Element el = doc.createElement("column");
	        FmtItem fi = completeView.get(i);
	       
	        el.setTextContent(fi.getDescription());
	        el.setAttribute("name", fi.name());
	        el.setAttribute("longDescription", fi.getLongDescription());
	        el.setAttribute("visible", String.valueOf(i < visiCount));
	        String dataType;
	        if (IconMap.class.isAssignableFrom(fi.getDataType())) {
	            dataType = "String";
	        } else {
	            dataType = fi.getDataType().getSimpleName();
	        }
	        el.setAttribute("dataType", dataType);
	        
	        root.appendChild(el);
	    }
	}
	
	public ExportXMLAction(MainWin parent) {
		putValue(Action.NAME, Utils._("Save fax list as XML..."));
		putValue(Action.SHORT_DESCRIPTION, Utils._("Saves the list of faxes in XML format"));
		putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
		this.parent = parent;
	}
}
