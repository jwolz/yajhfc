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
	
	
	public PhonebooksPrinter(Dialog owner, List<PhoneBook> phonebooks, boolean allowSelection) {
		super(owner, Utils._("Print phone books"), true);
		initialize(phonebooks, allowSelection);
	}
	
	private void initialize(List<PhoneBook> phonebooks, boolean allowSelection) {
    	okAction = new ExcDialogAbstractAction(_("Print")) {
			@Override
			protected void actualActionPerformed(ActionEvent e) {
				if (radUserSel.isSelected() && pbModel.countNumberOfSelectedItems() == 0) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				
				modalResult = true;
				dispose();
			}
		};
		CancelAction cancelAct = new CancelAction(this);
    	
    	JPanel contentPane = new JPanel();
    	contentPane.setLayout(new TableLayout(new double[][] { 
    			{border, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, border},
    			{border, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, TableLayout.FILL, border/2, TableLayout.PREFERRED, border/2, TableLayout.PREFERRED, border}
    	}));

    	radAll = new JRadioButton(_("All phone books"));
    	radSelItems = new JRadioButton(_("Selected phone book items"));
    	radSelItems.setEnabled(allowSelection);
    	radUserSel = new JRadioButton(_("Only the following phone books"));
    	ButtonGroup pbGroup = new ButtonGroup();
    	pbGroup.add(radAll);
    	pbGroup.add(radSelItems);
    	pbGroup.add(radUserSel);
    	
    	if (allowSelection) {
    		radSelItems.setSelected(true);
    	} else {
    		radAll.setSelected(true);
    	}
    	
    	pbModel = new SelectionTableModel<PhoneBook>(phonebooks.toArray(new PhoneBook[phonebooks.size()]));
    	pbModel.selectAll(true);
    	
    	colsModel = new SelectionTableModel<PBEntryField>(PBEntryField.values());
    	colsModel.selectAll(true);
    	
    	JTable pbTable = createSelectionTable(pbModel);
    	
    	JTable colsTable = createSelectionTable(colsModel);
    	
    	contentPane.add(radAll, "1,1,2,1");
    	contentPane.add(radSelItems, "1,3,2,3");
    	contentPane.add(radUserSel, "1,5,2,5");
    	contentPane.add(new JScrollPane(pbTable), "1,6,2,6");
    	
    	contentPane.add(new JLabel(_("Columns to print:")), "4,1,5,1");
    	contentPane.add(new JScrollPane(colsTable), "4,2,5,6");
    	
        contentPane.add(new JSeparator(), "0,8,6,8");
        contentPane.add(new JButton(okAction), "2,10");
        contentPane.add(cancelAct.createCancelButton(), "4,10");
        
        setContentPane(contentPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
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
	
	public static void printPhonebooks(Dialog owner, List<PhoneBook> phonebooks, List<PhoneBookEntry> selection, boolean printFilteredResults) {
		PhonebooksPrinter printDlg = new PhonebooksPrinter(owner, phonebooks, (selection != null && selection.size() > 1));
		printDlg.setVisible(true);
		
		TablePrintable tp;
		if (printDlg.radAll.isSelected()) {
			tp = new MultiPhonebookPrintable(phonebooks.toArray(new PhoneBook[phonebooks.size()]));
		} else if (printDlg.radSelItems.isSelected()) {
			tp = new TablePrintable(new PBEntryFieldTableModel(new ArrayList<PBEntryFieldContainer>(selection)));
			tp.getPageHeader().put(Alignment.CENTER, new MessageFormat("'" + _("Selected phone book entries") + "'"));
		} else {
			tp = new MultiPhonebookPrintable(printDlg.pbModel.getSelectedObjects());
		}
		setColumnLayout(printDlg.colsModel.getSelectedObjects(), tp);
		
		DistributionListCellRenderer distlistRenderer = new DistributionListCellRenderer();
		setColumnLayout(printDlg.colsModel.getSelectedObjects(), tp);
		tp.getRendererMap().put(DistributionList.class, distlistRenderer);
		
		try {
			FaxOptions myopts = Utils.getFaxOptions();
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

	protected static void setColumnLayout(PBEntryField[] columns, TablePrintable tp) {
		PhoneBookColumnLayout colLayout = new PhoneBookColumnLayout();
		colLayout.setMinFillColsWidth(100);
		colLayout.applyFilter(columns);
		tp.setColumnLayout(colLayout);
		TablePrintColumn commentsCol = colLayout.getColumnFor(PBEntryField.Comment);
		commentsCol.setWordWrap(true);
		commentsCol.setWidth(TablePrintColumn.WIDTH_FILL);
		commentsCol.setFont(new Font("Monospaced", Font.PLAIN, 12));
	}
}
