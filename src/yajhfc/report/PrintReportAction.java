package yajhfc.report;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.MainWin;
import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemList;
import yajhfc.model.TableType;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.ui.TooltipJTable;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.ProgressWorker;
import yajhfc.util.ProgressWorker.ProgressUI;

public final class PrintReportAction extends ExcDialogAbstractAction implements ListSelectionListener, ChangeListener {
    public PrintReportAction() {
        this.putValue(Action.NAME, Utils._("Print report") + "...");
        this.putValue(Action.SHORT_DESCRIPTION, Utils._("Prints a send or receive report for the fax"));
        this.putValue(Action.SMALL_ICON, Utils.loadCustomIcon("printreport.gif"));
        setEnabled(false);
    }

    public void stateChanged(ChangeEvent e) {
        doEnableCheck();
    }

    public void valueChanged(ListSelectionEvent e) {
        doEnableCheck();
    }

    public void doEnableCheck() {            
        MainWin mw = (MainWin)Launcher2.application;
        TooltipJTable<? extends FmtItem> table = mw.getSelectedTable();
        boolean enable;

        switch (table.getRealModel().getTableType()) {
        case RECEIVED:
        case SENT:
            enable = (table.getSelectedRow() >= 0);
            break;
        case ARCHIVE:
        case SENDING:
        default:
            enable = false;
            break;
        }

        setEnabled(enable);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void actualActionPerformed(ActionEvent e) {
        try {
            MainWin mw = (MainWin)Launcher2.application;
            @SuppressWarnings("rawtypes")
            TooltipJTable table = mw.getSelectedTable();

            printReport(mw, table.getSelectedJobs(), table.getRealModel().getColumns(), table.getRealModel().getTableType(), mw.getTablePanel());
        } catch (Exception e2) {
            ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), Utils._("Error generating report:"), e2);
        }
    }

    public static <T extends FmtItem> void printReport(Frame owner, final FaxJob<T>[] selJobs, FmtItemList<T> columns, TableType tt, ProgressUI progressUI) {
        final SendReport<T> sr = SendReportDialog.showSendReportDialog(owner, columns, tt);
        if (sr == null)
            return;

        ProgressWorker pw = new ProgressWorker() {
            boolean success;

            @Override
            public void doWork() {
                SendReport<T>.SendReportPageable srp = null;
                try {
                    srp = sr.createPageableForJobs(selJobs, this);

                    updateNote(Utils._("Initializing print dialog..."));

                    ReportOptions myopts = EntryPoint.getOptions();
                    final PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
                    if (myopts.printAttributes == null) {
                        pras.add(OrientationRequested.PORTRAIT);
                    } else {
                        for (Attribute attr : myopts.printAttributes) {
                            pras.add(attr);
                        }
                    }

                    final PrinterJob pj = PrinterJob.getPrinterJob();
                    pj.setPageable(srp);

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            getProgressMonitor().setNote(Utils._("Showing print dialog"));
                            success = pj.printDialog(pras);
                        } 
                    });

                    if (!success)
                        return;

                    updateNote(Utils._("Printing..."));
                    srp.setPageFormat(retrievePageFormat(pj, pras)); 
                    pj.print(pras);

                    updateNote(Utils._("Cleaning up..."));
                    myopts.printAttributes = pras.toArray();
                } catch (Exception e) {
                    showExceptionDialog(Utils._("Error printing report:"), e);
                } finally {
                    if (srp != null)
                        srp.cleanup();
                }
            }
        };
        pw.setProgressMonitor(progressUI);
        pw.startWork(owner, Utils._("Printing reports..."));
    }

    
    static PageFormat retrievePageFormat(PrinterJob pj, PrintRequestAttributeSet pras) {
        try {
            // Java 5 compatibility...
            Method getPageFormat = pj.getClass().getMethod("getPageFormat", PrintRequestAttributeSet.class);
            return (PageFormat)getPageFormat.invoke(pj, pras);
        } catch (Exception e) {
            Logger.getLogger(PrintReportAction.class.getName()).log(Level.INFO, "Using default page format", e);
            return pj.defaultPage();
        }
    }
}