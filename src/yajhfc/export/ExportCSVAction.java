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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.table.TableModel;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.phonebook.csv.CSVDialog;
import yajhfc.util.ExceptionDialog;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author jonas
 *
 */
public class ExportCSVAction {
	
	public static void exportToCSV(MainWin parent, File outputFile) {
		ExportCSVSettings settings = new ExportCSVSettings();
		settings.loadFromString(Utils.getFaxOptions().csvExportSettings);
		settings.fileName = outputFile.getPath();
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

    private static final long TIME_THRESHOLD = (36L*3600L*1000L);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    public static void exportTableModeltoCSV(TableModel model, CSVWriter writer, boolean writeHeader) {
        
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
