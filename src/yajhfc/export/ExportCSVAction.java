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
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.phonebook.csv.CSVDialog;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class ExportCSVAction extends ExcDialogAbstractAction {
	protected final MainWin parent;
	private JFileChooser fileChooser;
	
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new SafeJFileChooser();
			fileChooser.resetChoosableFileFilters();
			FileFilter csvFilter = new ExampleFileFilter(new String[] { "txt", "csv" }, Utils._("CSV files"));;
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
		ExportCSVSettings settings = new ExportCSVSettings();
		settings.loadFromString(Utils.getFaxOptions().csvExportSettings);
		JFileChooser chooser = getFileChooser();
		if (settings.fileName != null)
			chooser.setSelectedFile(new File(settings.fileName));
		if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		settings.fileName = Utils.getSelectedFileFromSaveChooser(chooser).getPath();
		CSVDialog csvd = new CSVDialog(parent, settings, MessageFormat.format(Utils._("Save to CSV file {0}"), Utils.shortenFileNameForDisplay(settings.fileName, 30)));
		csvd.setVisible(true);
		if (!csvd.clickedOK)
			return;
		// Save...
		
		Utils.getFaxOptions().csvExportSettings = settings.saveToString();
	}

	public ExportCSVAction(MainWin parent) {
		putValue(Action.NAME, Utils._("Save list of faxes as..."));
		putValue(Action.SHORT_DESCRIPTION, Utils._("Saves the list of faxes in CSV format"));
		putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
		this.parent = parent;
	}
}
