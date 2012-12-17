/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2012 Jonas Wolz <info@yajhfc.de>
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
 */
package yajhfc.report;

import static yajhfc.Utils._;
import static yajhfc.options.OptionsWin.border;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.util.CancelAction;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.IntVerifier;
import yajhfc.util.SelectionTableModel;


/**
 * @author jonas
 *
 */
public class SendReportDialog<T extends FmtItem> extends JDialog {
    static final Logger log = Logger.getLogger(SendReportDialog.class.getName());

	JTextField textPageFrom, textPageTo, textThumbnailsPerPage;
	JRadioButton radAllPages, radSelectedPages, radUnlimitedThumbs, radLimitThumbs;
	
	SelectionTableModel<T> colsModel;
	FmtItemList<T> columns;
	TableType tableType;
	
	public boolean modalResult = false;
	
	public SendReportDialog(Frame owner, FmtItemList<T> columns, TableType tableType) {
	    super(owner, true);
	    this.columns = columns;
	    this.tableType = tableType;
	    initialize();
	}
	public SendReportDialog(Dialog owner, FmtItemList<T> columns, TableType tableType) {
	    super(owner, true);
        this.columns = columns;
        this.tableType = tableType;
        initialize();
	}
	
	private void initialize() {	    
	    
	    final DocumentListener docListener = new DocumentListener() {
	        public void removeUpdate(DocumentEvent e) {
	            radSelectedPages.setSelected(true);
	        }

	        public void insertUpdate(DocumentEvent e) {
	            radSelectedPages.setSelected(true);
	        }

	        public void changedUpdate(DocumentEvent e) {
	        }
	    };
	    
	    textPageTo = new JTextField(4);
	    textPageTo.setInputVerifier(new IntVerifier(1, 9999));
        textPageTo.getDocument().addDocumentListener(docListener);
        
        textPageFrom = new JTextField(4);
        textPageFrom.setInputVerifier(new IntVerifier(1, 9999));
        textPageFrom.getDocument().addDocumentListener(docListener);

	    textThumbnailsPerPage = new JTextField(4);
	    textThumbnailsPerPage.setInputVerifier(new IntVerifier(1, 9999));
	    textThumbnailsPerPage.getDocument().addDocumentListener(new DocumentListener() {
	        public void removeUpdate(DocumentEvent e) {
	            radLimitThumbs.setSelected(true);
	        }

	        public void insertUpdate(DocumentEvent e) {
	            radLimitThumbs.setSelected(true);
	        }
	        
	        public void changedUpdate(DocumentEvent e) {
	        }
	    });
	    
	    radAllPages = new JRadioButton(_("All pages"));
	    radSelectedPages = new JRadioButton(_("Only for the following pages:"));
	    ButtonGroup groupPages = new ButtonGroup();
	    groupPages.add(radAllPages);
        groupPages.add(radSelectedPages);
	    
	    radUnlimitedThumbs = new JRadioButton(_("Unlimited (put all on one page)"));
	    radLimitThumbs = new JRadioButton(_("At most:"));
	    ButtonGroup groupThumbs = new ButtonGroup();
	    groupThumbs.add(radUnlimitedThumbs);
	    groupThumbs.add(radLimitThumbs);
	    
	    final T[] availableKeys = columns.getAvailableKeys();
        colsModel = new SelectionTableModel<T>(availableKeys);

	    JTable tableCols = createSelectionTable(colsModel);
	    tableCols.setDefaultRenderer(availableKeys.getClass().getComponentType(), new DefaultTableCellRenderer() {
	        @Override
	        public Component getTableCellRendererComponent(JTable table,
	                Object value, boolean isSelected, boolean hasFocus,
	                int row, int column) {
	            String text;
	            if (value != null) {
	                FmtItem fi = (FmtItem)value;
	                
	                text = fi.getDescription();
	            } else {
	                text = "";
	            }
	            
	            return super.getTableCellRendererComponent(table, text, isSelected, hasFocus,
	                    row, column);
	        }
	    });
	    
	    JScrollPane scrollCols = new JScrollPane(tableCols);
	    scrollCols.getViewport().setBackground(tableCols.getBackground());
	    scrollCols.getViewport().setOpaque(true);
	    
	    Action actOK = new ExcDialogAbstractAction(_("OK")) {
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (validateInput()) {
                    modalResult = true;
                    setVisible(false);
                }
            }
        };
	    CancelAction actCancel = new CancelAction(this);

	    double[][] dLay;
	    dLay = new double[][] {
	            {border, TableLayout.PREFERRED, border/2, TableLayout.FILL, border},
	            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border}
	    };
	    JPanel panelThumbs = new JPanel(new TableLayout(dLay), false);
	    panelThumbs.setBorder(BorderFactory.createTitledBorder(_("Number of thumbnails per page")));
	    panelThumbs.add(radUnlimitedThumbs, "1,1,3,1,l,c");
	    panelThumbs.add(radLimitThumbs, "1,3,l,c");
	    panelThumbs.add(textThumbnailsPerPage, "1,4,f,f");
	    panelThumbs.add(new JLabel(_("thumbnails per page")), "3,4,l,c");
	    
	    dLay = new double[][] {
	            {border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border},
	            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border}
	    };
	    JPanel panelPages = new JPanel(new TableLayout(dLay), false);
	    panelPages.setBorder(BorderFactory.createTitledBorder(_("Fax pages to print thumbnails for")));
	    panelPages.add(radAllPages, "1,1,3,1,l,c");
	    panelPages.add(radSelectedPages, "1,3,,3,3,l,c");
	    panelPages.add(textPageFrom, "1,4,f,f");
	    panelPages.add(new JLabel (_(" to ")), "2,4,f,f");
	    panelPages.add(textPageTo, "3,4,l,f");
	    
	    JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    panelButtons.add(new JButton(actOK));
	    panelButtons.add(actCancel.createCancelButton());
	    
	    dLay = new double[][] {
	      {border, TableLayout.FILL, border, 0.5, border},
	      {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED, border}
	    };
	    JPanel contentPane = new JPanel(new TableLayout(dLay));
	    
	    Utils.addWithLabel(contentPane, scrollCols, _("Information to print on report"), "1,2,1,8,f,f");
	    
	    contentPane.add(panelPages, "3,2,3,2,f,f");
	    contentPane.add(panelThumbs, "3,4,3,4,f,f");
	    contentPane.add(panelButtons, "3,8,3,8,f,f");
	    
	    setContentPane(contentPane);
	    setLocationByPlatform(true);
	    //setSize(900, 500);
	    pack();
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    
	    initializeValues();
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
	
	
	private boolean validateNumericInput(JTextField textField, int lowBound, int upBound, String msg) {
        try {
            int val = Integer.valueOf(textField.getText());
            if (val < lowBound || val > upBound) {
                textField.requestFocusInWindow();
                JOptionPane.showMessageDialog(this, msg);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            textField.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, msg);
            return false;
        }
	}
	protected boolean validateInput() {
	    return  validateNumericInput(textThumbnailsPerPage, 1, 9999, _("The number of thumbnails per page must be between 1 and 9999.")) &&
	            validateNumericInput(textPageFrom, 1, 9999, _("The start page must be between 1 and 9999.")) &&
	            validateNumericInput(textPageTo, 1, 9999, _("The end page must be between 1 and 9999.")) ;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void initializeValues() {
	    String title;
	    List selectedColumns;
	    ReportOptions opts = EntryPoint.getOptions();
	    
	    switch (tableType) {
	    case RECEIVED:
	        title = _("Fax receive report");
	        selectedColumns = opts.reportRecvColumns;
	        break;
	    case SENT:
	        title = _("Fax send report");
	        selectedColumns = opts.reportSentColumns;
	        break;
	    default:
	        throw new IllegalArgumentException("Only TableType SENT and RECEIVED supported!");
	    }
	    
	    setTitle(title);
	    if (selectedColumns.size() > 0) {
	        colsModel.setSelectedObjects(selectedColumns);
	    } else {
	        colsModel.selectAll();
	    }
	    textPageFrom.setText(String.valueOf(opts.reportPageFrom));
	    textPageTo.setText(String.valueOf(opts.reportPageTo));
	    textThumbnailsPerPage.setText(String.valueOf(opts.reportThumbsPerPage));
	    
	    if (opts.reportUnlimitedThumbs) {
	        radUnlimitedThumbs.setSelected(true);
	    } else {
	        radLimitThumbs.setSelected(true);
	    }
	    if (opts.reportPrintAllPages) {
	        radAllPages.setSelected(true);
	    } else {
	        radSelectedPages.setSelected(true);
	    }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeToAndSaveDefaults(SendReport<T> sr) {	    
	    boolean unlimitedThumbs = radUnlimitedThumbs.isSelected();
	    boolean printAllPages = radAllPages.isSelected();
	    
	    int thumbnailsPerPage = Integer.parseInt(textThumbnailsPerPage.getText());
	    int pageFrom = Integer.parseInt(textPageFrom.getText());
	    int pageTo = Integer.parseInt(textPageTo.getText());
	    
	    List<T> selectedCols = Arrays.asList(colsModel.getSelectedObjects());
	    
	    ReportOptions opts = EntryPoint.getOptions();
	    
	    opts.reportPrintAllPages = printAllPages;
	    opts.reportPageFrom = pageFrom;
	    opts.reportPageTo = pageTo;
	    opts.reportThumbsPerPage = thumbnailsPerPage;
	    opts.reportUnlimitedThumbs = unlimitedThumbs;
	    String reportTitle;
	    switch (tableType) {
	    case RECEIVED:
	    	reportTitle = _("Fax receive report");
	        opts.reportRecvColumns.clear();
	        opts.reportRecvColumns.addAll((List)selectedCols);
	        break;
	    case SENT:
	    	reportTitle = _("Fax send report");
	        opts.reportSentColumns.clear();
	        opts.reportSentColumns.addAll((List)selectedCols);
	        break;
	    default:
	        throw new IllegalArgumentException("Only TableType SENT and RECEIVED supported!");
	    }
	    
	    //reportTitle = "äöüß 海納百 لى مستوى αναπτυχθεί  музыкальная";
	    sr.setHeadLine(reportTitle);
	    sr.setColumns(selectedCols);

	    if (printAllPages) {
	        sr.setStartPage(1);
	        sr.setEndPage(0);
	    } else {
            sr.setStartPage(pageFrom);
            sr.setEndPage(pageTo);
	    }
	    if (unlimitedThumbs) {
	        sr.setThumbnailsPerPage(0);
	    } else {
	        sr.setThumbnailsPerPage(thumbnailsPerPage);
	    }
	}
	
	public static <T extends FmtItem> SendReport<T> showSendReportDialog(Frame owner, FmtItemList<T> columns, TableType tt)  {
	    SendReportDialog<T> srd = new SendReportDialog<T>(owner, columns, tt);
	    srd.setVisible(true);
	    if (srd.modalResult) {
	        SendReport<T> rv = new SendReport<T>();
	    	srd.writeToAndSaveDefaults(rv);
	    	srd.dispose();
	    	return rv;
	    } else {
	    	return null;
	    }
	}
}
