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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.phonebook.csv.CSVDialog;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;
import au.com.bytecode.opencsv.CSVWriter;

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
		Utils.setDefWinPos(csvd);
		csvd.setVisible(true);
		if (!csvd.clickedOK)
			return;
		Utils.setWaitCursor(null);
		try {
            CSVWriter out = settings.createWriter();
            TableModel model = parent.getSelectedTable().getModel();
            exportTableModeltoCSV(model, out, settings.firstLineAreHeaders);
            out.close();
            Utils.getFaxOptions().csvExportSettings = settings.saveToString();
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table as CSV:"), ex);
        } finally {
            Utils.unsetWaitCursor(null);
        }
	}

	public ExportCSVAction(MainWin parent) {
		putValue(Action.NAME, Utils._("Save as CSV") + "...");
		putValue(Action.SHORT_DESCRIPTION, Utils._("Saves the list of faxes in CSV format"));
		putValue(Action.SMALL_ICON, Utils.loadCustomIcon("saveAsCSV.png"));
		this.parent = parent;
	}
	
    private static final long TIME_THRESHOLD = (36L*3600L*1000L);
    
    public static void exportTableModeltoCSV(TableModel model, CSVWriter writer, boolean writeHeader) {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
        
        String[] buf = new String[model.getColumnCount()];
        if (writeHeader) {
            for (int i=0; i<buf.length; i++) {
                buf[i] = model.getColumnName(i);
            }
            writer.writeNext(buf);
        }
        for (int row=0; row<model.getRowCount(); row++) {
            for (int col=0; col<buf.length; col++) {
                Object val = model.getValueAt(row, col);
                if (val instanceof Date) {
                    Date dVal = (Date)val;
                    if (dVal.getTime() > -TIME_THRESHOLD && dVal.getTime() < TIME_THRESHOLD) { // If it's a date around 1970-01-01, assume it's time only
                        buf[col] = TIME_FORMAT.format(dVal);
                    } else {
                        buf[col] = DATE_FORMAT.format(dVal);
                    }
                } else if (val != null) {
                    buf[col] = val.toString();
                } else {
                    buf[col] = "";
                }
            }
            writer.writeNext(buf);
        }
    }
}
