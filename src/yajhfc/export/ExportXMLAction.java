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
package yajhfc.export;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.IconMap;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.util.ProgressWorker;

/**
 * @author jonas
 *
 */
public class ExportXMLAction  {
	public static void exportToXML(final MainWin mwParent, final File outputFile) {
	    Utils.setWaitCursor(null);
	    final FaxJobList<?> jobList = mwParent.getSelectedTable().getRealModel().getJobs();
	    ProgressWorker pw = new ProgressWorker() {
	        @Override
	        protected void initialize() {
	            this.progressMonitor = mwParent.getTablePanel();
	        }

	        @Override
	        public void doWork() {
	            try {
	                updateNote(Utils._("Exporting..."));
	                StreamResult out = new StreamResult(outputFile);
	                saveToResult(out, jobList);
	            } catch (Exception ex) {
	                dialogs.showExceptionDialog(Utils._("Error saving the table:"), ex);
	            }
	        }  

	        @Override
	        protected void done() {
	            Utils.unsetWaitCursor(null);
	        }
	    };
	    pw.startWork(mwParent, Utils._("Export to XML"));
	}
	
	protected static void saveToResult(Result result, FaxJobList<? extends FmtItem> faxList) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element root = doc.createElement("faxlist");
        root.setAttribute("xmlns", "http://yajhfc.de/schema/tableexport");
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
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);

        transformer.transform(source, result);
    }
	
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	protected static void saveRows(FaxJobList<? extends FmtItem> faxList, Element root, Document doc) {
	    List<? extends FmtItem> cols = faxList.getColumns().getCompleteView();
	    for (FaxJob<?> job : faxList.getJobs()) {
	        Element rowEl = doc.createElement("row");
	        rowEl.setAttribute("id", job.getIDValue().toString());
	        for (int i=0; i<cols.size(); i++) {
	            Element cellEl = doc.createElement("cell");
	            
	            Class<?> dataType = cols.get(i).getDataType();
	            if (Date.class.isAssignableFrom(dataType)) {
	                Date data = (Date)job.getData(i);
	                cellEl.setAttribute("rawValue", String.valueOf((data == null) ? Long.MIN_VALUE : data.getTime()));
	                
	                cellEl.setTextContent((data == null) ? "" : DATE_FORMAT.format(data));
	            } else {
	                Object data = job.getData(i);
	                cellEl.setTextContent((data == null) ? "" : data.toString());
	            }
	            
	            rowEl.appendChild(cellEl);
	        }
	        
	        root.appendChild(rowEl);
	    }
	}
	
	protected static void saveColumns(FmtItemList<? extends FmtItem> cols, Element root, Document doc) {
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
}
