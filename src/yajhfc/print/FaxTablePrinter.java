/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.print;

import java.awt.Frame;
import java.awt.print.PrinterException;
import java.text.MessageFormat;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JDialog;

import yajhfc.FaxOptions;
import yajhfc.FmtItem;
import yajhfc.IconMap;
import yajhfc.Utils;
import yajhfc.model.TooltipJTable;
import yajhfc.print.tableprint.Alignment;
import yajhfc.print.tableprint.IconMapCellRenderer;
import yajhfc.print.tableprint.TablePrintable;
import yajhfc.util.ExceptionDialog;

/**
 * @author jonas
 *
 */
public class FaxTablePrinter extends JDialog {
    
    protected FaxTablePrinter(Frame owner, TooltipJTable<? extends FmtItem> selTable, String caption) {
        super(owner, MessageFormat.format("Print {0}", new Object[] { caption }), true);
        initialize();
    }
    
    private void initialize() {
        
    }
    

    public static void printFaxTable(Frame owner, TooltipJTable<? extends FmtItem> selTable, String caption) {
        try {
//          MessageFormat header = new MessageFormat(tabMain.getToolTipTextAt(tabMain.getSelectedIndex()));
//          Date now = new Date();
//          MessageFormat footer = new MessageFormat("'" + DateFormat.getDateInstance(DateFormat.SHORT, Utils.getLocale()).format(now) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Utils.getLocale()).format(now) + "' - " + Utils._("page {0}"));
//          selTable.print(PrintMode.FIT_WIDTH, header, footer);
          FaxOptions myopts = Utils.getFaxOptions();
          
          
          TablePrintable tp = new TablePrintable(selTable.getModel());
          tp.getPageHeader().put(Alignment.CENTER, new MessageFormat("'" + caption + "'"));
          tp.getRendererMap().put(IconMap.class, new IconMapCellRenderer());
          
          PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
          if (myopts.printAttributes == null) {
              pras.add(OrientationRequested.LANDSCAPE);
          } else {
              for (Attribute attr : myopts.printAttributes) {
                  pras.add(attr);
              }
          }
          if (StatusDialogPrintable.printWithDialog(owner, tp, pras)) {
              myopts.printAttributes = pras.toArray();
          }
      } catch (PrinterException pe) {
          ExceptionDialog.showExceptionDialog(owner, Utils._("Error printing the table:"), pe);
      }
    }
}
