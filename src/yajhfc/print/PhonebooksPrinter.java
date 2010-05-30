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

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
import yajhfc.print.tableprint.Alignment;
import yajhfc.print.tableprint.TablePrintColumn;
import yajhfc.print.tableprint.TablePrintable;
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SelectionTableModel;

/**
 * @author jonas
 *
 */
public class PhonebooksPrinter extends JDialog {
	private static final int border = 6;
	
	Action okAction;
	SelectionTableModel<PhoneBook> pbModel;
	SelectionTableModel<PBEntryField> colsModel;
	JRadioButton radAll, radSelItems, radUserSel;
	
	boolean modalResult = false;
	
	
	public PhonebooksPrinter(Dialog owner, List<PhoneBook> phonebooks, PhoneBook currentPhonebook, boolean allowSelection) {
		super(owner, Utils._("Print phone books"), true);
		initialize(phonebooks, currentPhonebook, allowSelection);
	}
	
	private void initialize(List<PhoneBook> phonebooks, PhoneBook currentPhonebook, boolean allowSelection) {
    	okAction = new ExcDialogAbstractAction(_("Print")) {
			@Override
			protected void actualActionPerformed(ActionEvent e) {
				if (radUserSel.isSelected() && pbModel.countNumberOfSelectedItems() == 0 || colsModel.countNumberOfSelectedItems() == 0) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				
				modalResult = true;
				dispose();
			}
		};
		CancelAction cancelAct = new CancelAction(this);
    	FaxOptions fo = Utils.getFaxOptions();
		
    	JPanel contentPane = new JPanel();
    	contentPane.setLayout(new TableLayout(new double[][] { 
    			{border, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border},
    			{border, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, TableLayout.FILL, border/2, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, border}
    	}));

    	radAll = new JRadioButton(_("All phone books"));
    	radSelItems = new JRadioButton(_("Selected phone book entries"));
    	radSelItems.setEnabled(allowSelection);
    	radUserSel = new JRadioButton(_("Only the following phone book(s):"));
    	ButtonGroup pbGroup = new ButtonGroup();
    	pbGroup.add(radAll);
    	pbGroup.add(radSelItems);
    	pbGroup.add(radUserSel);
    	
        pbModel = new SelectionTableModel<PhoneBook>(phonebooks.toArray(new PhoneBook[phonebooks.size()]));
    	
    	if (allowSelection) {
    		radSelItems.setSelected(true);
            pbModel.selectAll(true);
    	} else if (currentPhonebook != null) {
    	    radUserSel.setSelected(true);
    	    pbModel.setSelectedObjects(Collections.singleton(currentPhonebook));
    	} else {
    		radAll.setSelected(true);
            pbModel.selectAll(true);
    	}
    	
    	
    	colsModel = new SelectionTableModel<PBEntryField>(PBEntryField.values());
    	if (fo.pbprintPrintColumns.size() == 0) {
    	    colsModel.selectAll(true);
    	} else {
    	    colsModel.setSelectedObjects(fo.pbprintPrintColumns);
    	}
    	
    	JTable pbTable = createSelectionTable(pbModel);
    	pbModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                radUserSel.setSelected(true);
                checkEnabled();
            }
        });
    	JScrollPane pbScroller = new JScrollPane(pbTable);
    	pbScroller.getViewport().setBackground(pbTable.getBackground());
    	pbScroller.getViewport().setOpaque(true);
    	
    	JTable colsTable = createSelectionTable(colsModel);
        colsModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                checkEnabled();
            }
        });
    	JScrollPane colsScroller = new JScrollPane(colsTable);
    	colsScroller.getViewport().setBackground(colsTable.getBackground());
    	colsScroller.getViewport().setOpaque(true);
    	
    	contentPane.add(radAll, "1,1,2,1");
    	contentPane.add(radSelItems, "1,3,2,3");
    	contentPane.add(radUserSel, "1,5,2,5");
    	contentPane.add(pbScroller, "1,6,2,6");
    	
    	contentPane.add(new JLabel(_("Columns to print:")), "4,1,5,1");
    	contentPane.add(colsScroller, "4,2,5,6");
    	
        contentPane.add(new JSeparator(), "0,8,6,8");
        contentPane.add(new JButton(okAction), "2,10");
        contentPane.add(cancelAct.createCancelButton(), "4,10");
        
        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 480);
        Utils.setDefWinPos(this);
	}
	
	private JTable createSelectionTable(SelectionTableModel<?> model) {
		JTable res = new JTable(model);
		res.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		res.setShowGrid(false);
		res.setRowSelectionAllowed(true);
		res.setTableHeader(null);
		res.getColumnModel().getColumn(0).setMaxWidth(15);
		return res;
	}
	
	void checkEnabled() {
	    boolean enablePrint = (!radUserSel.isSelected() || pbModel.countNumberOfSelectedItems() > 0) && colsModel.countNumberOfSelectedItems() > 0;
	    okAction.setEnabled(enablePrint);
	}
	
	public static void printPhonebooks(Dialog owner, List<PhoneBook> phonebooks, PhoneBook currentPhonebook, List<PhoneBookEntry> selection, boolean printFilteredResults) {
		PhonebooksPrinter printDlg = new PhonebooksPrinter(owner, phonebooks, currentPhonebook, (selection != null && selection.size() > 1));
		printDlg.setVisible(true);
		if (!printDlg.modalResult)
		    return;
		final PBEntryField[] selectedColumns = printDlg.colsModel.getSelectedObjects();
        FaxOptions myopts = Utils.getFaxOptions();
        myopts.pbprintPrintColumns.clear();
        myopts.pbprintPrintColumns.addAll(Arrays.asList(selectedColumns));
		
		TablePrintable tp;
		if (printDlg.radAll.isSelected()) {
			tp = new MultiPhonebookPrintable(phonebooks.toArray(new PhoneBook[phonebooks.size()]));
		} else if (printDlg.radSelItems.isSelected()) {
			tp = new TablePrintable(new PBEntryFieldTableModel(new ArrayList<PBEntryFieldContainer>(selection)));
			tp.getPageHeader().put(Alignment.CENTER, new MessageFormat("'" + _("Selected phone book entries") + "'"));
		} else {
			tp = new MultiPhonebookPrintable(printDlg.pbModel.getSelectedObjects());
		}
        setColumnLayout(selectedColumns, tp);
		
		DistributionListCellRenderer distlistRenderer = new DistributionListCellRenderer();
		setColumnLayout(selectedColumns, distlistRenderer);
		distlistRenderer.setHeaderFont(tp.getHeaderFont().deriveFont(Font.ITALIC));
		tp.getRendererMap().put(DistributionList.class, distlistRenderer);
		
		try {
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

	private static void setColumnLayout(PBEntryField[] columns, TablePrintable tp) {
		PhoneBookColumnLayout colLayout = new PhoneBookColumnLayout();
		colLayout.setMinFillColsWidth(100);
		colLayout.applyFilter(columns);
		tp.setColumnLayout(colLayout);
		
		TablePrintColumn distlistHeader = colLayout.getDistListColumn(true);
		distlistHeader.setFont(tp.getTableFont().deriveFont(Font.ITALIC));
		distlistHeader.setBackgroundColor(tp.getHeaderBackground());
		
		TablePrintColumn commentsCol = colLayout.getColumnFor(PBEntryField.Comment);
		commentsCol.setWordWrap(true);
		commentsCol.setWidth(TablePrintColumn.WIDTH_FILL);
		commentsCol.setFont(new Font("Monospaced", Font.PLAIN, 12));
	}
}
