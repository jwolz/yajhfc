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

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.Date;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.IconMap;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.table.FaxListTableModel;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.print.tableprint.Alignment;
import yajhfc.print.tableprint.DefaultCellFormatModel;
import yajhfc.print.tableprint.IconMapCellRenderer;
import yajhfc.print.tableprint.TablePrintColumn;
import yajhfc.print.tableprint.TablePrintable;
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.TableSorter;

/**
 * @author jonas
 *
 */
public class FaxTablePrinter extends JDialog {
    final static String COLUMN_WIDTH_AUTO = _("Fit to contents");
    final static String COLUMN_WIDTH_USER = _("Same as on screen");
	private static final int border = 6;
	
	JComboBox comboColumnWidths;
	JCheckBox checkMarkUnreadFaxes, checkMarkErrors;
	boolean showUnreadOptions;
	Action okAction;
	
	boolean modalResult = false;
	
    protected FaxTablePrinter(Frame owner, TooltipJTable<? extends FmtItem> selTable, String caption, boolean showUnreadOptions) {
        super(owner, MessageFormat.format(_("Print {0}"), new Object[] { caption }), true);
        this.showUnreadOptions = showUnreadOptions;
        initialize();
    }
    
    private void initialize() {
    	okAction = new ExcDialogAbstractAction(_("Print")) {
			@Override
			protected void actualActionPerformed(ActionEvent e) {
				modalResult = true;
		    	FaxOptions fo = Utils.getFaxOptions();
		    	fo.faxprintColumnWidthAsOnScreen = (comboColumnWidths.getSelectedItem() == COLUMN_WIDTH_USER);
		    	fo.faxprintMarkErrors = checkMarkErrors.isSelected();
		    	if (showUnreadOptions)
		    		fo.faxprintMarkUnread = checkMarkUnreadFaxes.isSelected();
		    	
				dispose();
			}
		};
		CancelAction cancelAct = new CancelAction(this);
    	
    	JPanel contentPane = new JPanel();
    	contentPane.setLayout(new TableLayout(new double[][] { 
    			{border, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border},
    			{border, TableLayout.PREFERRED, TableLayout.PREFERRED, showUnreadOptions ? border : 0, showUnreadOptions ? TableLayout.PREFERRED : 0, border, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, border,}
    	}));
    	
    	FaxOptions fo = Utils.getFaxOptions();
        comboColumnWidths = new JComboBox(new Object[] {COLUMN_WIDTH_AUTO, COLUMN_WIDTH_USER});
        comboColumnWidths.setSelectedIndex(fo.faxprintColumnWidthAsOnScreen ? 1 : 0);
        
        if (showUnreadOptions) {
        	checkMarkUnreadFaxes = new JCheckBox(_("Use a bold font for unread faxes"));
        	checkMarkUnreadFaxes.setSelected(fo.faxprintMarkUnread);
        }
        checkMarkErrors = new JCheckBox(_("Mark failed jobs"));
        checkMarkErrors.setSelected(fo.faxprintMarkErrors);
        
        Utils.addWithLabel(contentPane, comboColumnWidths, _("Column widths:"), "1,2,5,2");
        if (showUnreadOptions) {
        	contentPane.add(checkMarkUnreadFaxes, "1,4,5,4");
        }
        contentPane.add(checkMarkErrors, "1,6,5,6");
        contentPane.add(new JSeparator(), "0,8,6,8");
        contentPane.add(new JButton(okAction), "2,10");
        contentPane.add(cancelAct.createCancelButton(), "4,10");
        
        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        Utils.setDefWinPos(this);
    }
    

    public static void printFaxTable(Frame owner, TooltipJTable<? extends FmtItem> selTable, String caption) {
        try {
//          MessageFormat header = new MessageFormat(tabMain.getToolTipTextAt(tabMain.getSelectedIndex()));
//          Date now = new Date();
//          MessageFormat footer = new MessageFormat("'" + DateFormat.getDateInstance(DateFormat.SHORT, Utils.getLocale()).format(now) + " " + DateFormat.getTimeInstance(DateFormat.SHORT, Utils.getLocale()).format(now) + "' - " + Utils._("page {0}"));
//          selTable.print(PrintMode.FIT_WIDTH, header, footer);
          FaxOptions myopts = Utils.getFaxOptions();
          TableModel model = selTable.getModel();
          FaxListTableModel<? extends FmtItem> realModel = selTable.getRealModel();
          boolean showUnreadOptions = realModel.getTableType() == TableType.RECEIVED;
          FaxTablePrinter ftpDlg = new FaxTablePrinter(owner, selTable, caption, showUnreadOptions);
          ftpDlg.setVisible(true);
          if (!ftpDlg.modalResult) {
        	  return;
          }
          
          TablePrintable tp = new TablePrintable(model);
          tp.getPageHeader().put(Alignment.CENTER, new MessageFormat("'" + caption + "'"));
          if (myopts.faxprintMarkErrors || (showUnreadOptions && myopts.faxprintMarkUnread)) {
        	  Color errorBackground = null;
        	  if (myopts.faxprintMarkErrors) {
        		  errorBackground = realModel.getErrorColor();
        	  }
        	  Font unreadFont = null;
        	  if (showUnreadOptions && myopts.faxprintMarkUnread) {
        		  unreadFont = tp.getTableFont().deriveFont(Font.BOLD);
        	  }
        	  tp.setFormatModel(new FaxCellFormatModel(errorBackground, unreadFont));
          }
          if (myopts.faxprintColumnWidthAsOnScreen) {
        	  for (int i=0; i<selTable.getColumnCount(); i++) {
        		  TablePrintColumn tpCol = tp.getColumnLayout().getHeaderLayout()[i];
        		  TableColumn jtCol = selTable.getColumnModel().getColumn(i);
        		  
        		  tpCol.setWidth(jtCol.getWidth());
        	  }
          }
          for (int i=0; i<selTable.getColumnCount(); i++) {
        	  int realCol = selTable.getColumnModel().getColumn(i).getModelIndex();
    		  FmtItem fi = realModel.getColumns().get(realCol);
    		  if (fi.getDataType() == Date.class) {
    			  tp.getColumnLayout().getHeaderLayout()[i].setColumnFormat(fi.getDisplayDateFormat());
    		  }
          }
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
    
    public static class FaxCellFormatModel extends DefaultCellFormatModel {
    	protected Color errorBackground;
    	protected Font unreadFont;
    	
    	@SuppressWarnings("unchecked")
		protected FaxJob<? extends FmtItem> getJob(TableModel model, int rowIndex) {
    		FaxListTableModel<? extends FmtItem> realModel;
    		if (model instanceof TableSorter) {
    			TableSorter sorter = (TableSorter)model;
    			realModel = (FaxListTableModel<? extends FmtItem>)sorter.getTableModel();
    			rowIndex = sorter.modelIndex(rowIndex);
    		} else if (model instanceof FaxListTableModel) {
    			realModel = (FaxListTableModel<? extends FmtItem>)model;
    		} else {
    			return null;
    		}
    		return realModel.getJob(rowIndex);
    	}
    	
    	@Override
    	public Color getCellBackgroundColor(TablePrintColumn col,
    			TableModel model, int rowIndex) {
    		if (errorBackground != null) {
    			FaxJob<? extends FmtItem> job = getJob(model, rowIndex);
    			if (job.isError())
    				return errorBackground;
    		}
    		return super.getCellBackgroundColor(col, model, rowIndex);
    	}
    	@Override
    	public Font getCellFont(TablePrintColumn col, TableModel model,
    	        int rowIndex) {
    	    if (unreadFont != null) {
    	        FaxJob<? extends FmtItem> job = getJob(model, rowIndex);
    	        if (job.isRead()) {
    	            return unreadFont;
    	        }
    	    }
    		return super.getCellFont(col, model, rowIndex);
    	}
    	
		public FaxCellFormatModel(Color errorBackground, Font unreadFont) {
			super();
			this.errorBackground = errorBackground;
			this.unreadFont = unreadFont;
		}
    	
    	
    }
}
