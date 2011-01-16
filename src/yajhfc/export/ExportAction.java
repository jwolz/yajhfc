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
import java.io.File;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class ExportAction extends ExcDialogAbstractAction {
	protected final MainWin parent;
	private JFileChooser fileChooser;
	
	private static final FileFormat[] supportedFormats = {
		FileFormat.CSV,
		FileFormat.HTML,
		FileFormat.XML
	};
	private FileFilter[] filters;
	
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new SafeJFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			for (FileFormat ff : supportedFormats) {
				fileChooser.addChoosableFileFilter(new ExampleFileFilter(ff.getPossibleExtensions(), ff.getDescription()));
			}
			filters = fileChooser.getChoosableFileFilters();
			
			fileChooser.setFileFilter(getFilterFromFormat(Utils.getFaxOptions().lastExportFormat));
			if (Utils.getFaxOptions().lastExportSavePath != null)
				fileChooser.setCurrentDirectory(new File(Utils.getFaxOptions().lastExportSavePath));
		}
		return fileChooser;
	}

	private FileFilter getFilterFromFormat(FileFormat ff) {
		for (int i=0; i<supportedFormats.length; i++) {
			if (supportedFormats[i] == ff) {
				return filters[i];
			}
		}
		return null;
	}
	
	private FileFormat getFormatFromFilter(FileFilter ff) {
		for (int i=0; i<filters.length; i++) {
			if (filters[i] == ff) {
				return supportedFormats[i];
			}
		}
		return null;
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
		    Utils.getFaxOptions().lastExportSavePath = chooser.getCurrentDirectory().getAbsolutePath();
		    FileFormat selectedFormat = getFormatFromFilter(chooser.getFileFilter());
		    Utils.getFaxOptions().lastExportFormat = selectedFormat;
		    File selectedFile = Utils.getSelectedFileFromSaveChooser(chooser);
		    
		    switch (selectedFormat) {
		    case CSV:
		    	ExportCSVAction.exportToCSV(parent, selectedFile);
		    	break;
		    case HTML:
		    	ExportHTMLAction.exportToHTML(parent, selectedFile);
		    	break;
		    case XML:
		    	ExportXMLAction.exportToXML(parent, selectedFile);
		    	break;
		    default:
		    	Logger.getLogger(ExportAction.class.getName()).severe("Unsupported file format selected.");
		    	JOptionPane.showMessageDialog(parent, "Unsupported file format selected.");
		    	break;
		    }
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table:"), ex);
        } finally {
            Utils.unsetWaitCursor(null);
        }
	}
	
	public ExportAction(MainWin parent) {
		putValue(Action.NAME, Utils._("Save as") + "...");
		putValue(Action.SHORT_DESCRIPTION, Utils._("Saves the list of faxes in CSV, HTML or XML format"));
		putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
		this.parent = parent;
	}
}
