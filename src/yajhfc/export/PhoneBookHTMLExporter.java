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

import java.util.Collections;

import javax.swing.table.TableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;

/**
 * @author jonas
 *
 */
public class PhoneBookHTMLExporter extends HTMLExporter {
    @Override
    protected Node createTDElement(Document doc, TableModel model, int row,
            int col, ImageExportManager imgExp) {
        PBEntryFieldTableModel pbeModel = (PBEntryFieldTableModel)model;
        PBEntryFieldContainer pbec = pbeModel.getRow(row);
        if (pbec instanceof DistributionList) {
            if (col == 0) {
                return createTDElement(doc, pbec.getField(PBEntryField.Name), null, "font-style: italic; background-color:" + headerBackground + ";", null);
            } else if (col == 1) {
                Element td = doc.createElement("td");
                td.setAttribute("colspan", String.valueOf(model.getColumnCount() - 1));
                appendTable(doc, td, 
                        new PBEntryFieldTableModel(Collections.<PBEntryFieldContainer>unmodifiableList(((DistributionList)pbec).getEntries())),
                        "font-style: italic; background-color:" + headerBackground + ";", imgExp);
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
            return createTDElement(doc, text, null, style, null);
        }
    }
    
    public PhoneBookHTMLExporter() {
        exportImages = false;
    }
}
