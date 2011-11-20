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
import yajhfc.model.FmtItem;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.server.ServerManager;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressWorker;
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
		//Utils.setWaitCursor(null);
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
		    	exportToHTML(selectedFile);
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
        } /*finally {
            Utils.unsetWaitCursor(null);
        }*/
	}
	
    private void exportToHTML(final File selectedFile) {
        Utils.setWaitCursor(null);

        final String title = parent.getSelectedTableDescription();
        final TooltipJTable<? extends FmtItem> selectedTable = parent.getSelectedTable();

        ProgressWorker pw = new ProgressWorker() {
            @Override
            protected void initialize() {
                this.progressMonitor = ExportAction.this.parent.getTablePanel();
            }

            @Override
            public void doWork() {
                try {
                    updateNote(Utils._("Exporting..."));
                    final String footer = Utils._("Server") + ": " + ServerManager.getDefault().getCurrent().toString();
                    TooltipJTableHTMLExporter hexp = new TooltipJTableHTMLExporter();
                    hexp.saveToFile(selectedFile, selectedTable.getModel(), title, footer);
                } catch (Exception ex) {
                    ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table:"), ex);
                } 
            }

            @Override
            protected void done() {
                Utils.unsetWaitCursor(null);
            }
        };
        pw.startWork(parent, Utils._("Export to HTML"));
    }
	
	public ExportAction(MainWin parent) {
		putValue(Action.NAME, Utils._("Export") + "...");
		putValue(Action.SHORT_DESCRIPTION, Utils._("Exports the list of faxes in CSV, HTML or XML format"));
		putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
		this.parent = parent;
	}
}
