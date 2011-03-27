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
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.TableModel;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.phonebook.csv.CSVDialog;
import yajhfc.util.ProgressWorker;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author jonas
 *
 */
public class ExportCSVAction {
	
	public static void exportToCSV(final MainWin mwParent, final File outputFile) {
		final ExportCSVSettings settings = new ExportCSVSettings();
		settings.loadFromString(Utils.getFaxOptions().csvExportSettings);
		settings.fileName = outputFile.getPath();
		CSVDialog csvd = new CSVDialog(mwParent, settings, MessageFormat.format(Utils._("Save to CSV file {0}"), Utils.shortenFileNameForDisplay(settings.fileName, 30)));
		Utils.setDefWinPos(csvd);
		csvd.setVisible(true);
		if (!csvd.clickedOK)
			return;
		Utils.setWaitCursor(null);
        final TooltipJTable<? extends FmtItem> selectedTable = mwParent.getSelectedTable();
		ProgressWorker pw = new ProgressWorker() {
		    @Override
		    protected void initialize() {
		        progressMonitor = mwParent.getTablePanel();
		    }
		    
            @Override
            public void doWork() {     
                try {
                    updateNote(Utils._("Exporting..."));
                    CSVWriter out = settings.createWriter();
                    exportTableModeltoCSV(selectedTable.getModel(), selectedTable.getRealModel().getColumns(), out, settings.firstLineAreHeaders);
                    out.close();
                    Utils.getFaxOptions().csvExportSettings = settings.saveToString();
                } catch (Exception ex) {
                    showExceptionDialog(Utils._("Error saving the table:"), ex);
                } 
            }
            
            @Override
            protected void done() {
                Utils.unsetWaitCursor(null);
            }
            
        };
        pw.startWork(mwParent, Utils._("Save to CSV"));
	}
    
    public static void exportTableModeltoCSV(TableModel model, List<? extends FmtItem> columns, CSVWriter writer, boolean writeHeader) {     
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
                    buf[col] = columns.get(col).getDisplayDateFormat().format(val);
                } else  if (val instanceof Boolean) {
                    buf[col] = ((Boolean)val).booleanValue() ? "Y" : "N";
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
