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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Printable with status dialog.
 * Template originally "stolen" from JTable
 * @author jonas
 *
 */
public final class StatusDialogPrintable implements Printable {

    static final Logger log = Logger.getLogger(StatusDialogPrintable.class.getName());
    
    Throwable printError = null;
    
    /** The delegate <code>Printable</code>. */
    Printable printDelegate;

    /** The formatter to prepare the status message. */
    MessageFormat statusFormat;

    /** The <code>JLabel</code> to update with the status. */
    JLabel statusLabel;

    /**
     * Construct a <code>ThreadSafePrintable</code> around the given
     * delegate.
     *
     * @param printDelegate the <code>Printable</code> to delegate to
     */
    private StatusDialogPrintable(Printable printDelegate) {
        this.printDelegate = printDelegate;
    }

    /**
     * Provide the <code>MessageFormat</code> and <code>JLabel</code>
     * to use in updating the status.
     *
     * @param statusFormat the format to prepare the status message
     * @param statusPane the JOptionPane to set the status message on
     */
    public void startUpdatingStatus(MessageFormat statusFormat,
                                    JLabel statusLabel) {
        this.statusFormat = statusFormat;
        this.statusLabel = statusLabel;
    }

    /**
     * Indicate that the <code>JLabel</code> should not be updated
     * any more.
     */
    public void stopUpdatingStatus() {
        statusFormat = null;
        statusLabel = null;
    }

    /**
     * Prints the specified page into the given {@link Graphics}
     * context, in the specified format.
     * <p>
     * Regardless of what thread this method is called on, all calls into
     * the delegate will be done on the event-dispatch thread.
     *
     * @param   graphics    the context into which the page is drawn
     * @param   pageFormat  the size and orientation of the page being drawn
     * @param   pageIndex   the zero based index of the page to be drawn
     * @return  PAGE_EXISTS if the page is rendered successfully, or
     *          NO_SUCH_PAGE if a non-existent page index is specified
     * @throws  PrinterException if an error causes printing to be aborted
     */
    public int print(final Graphics graphics,
            final PageFormat pageFormat,
            final int pageIndex) throws PrinterException {

        // We'll use this Runnable
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if (statusLabel != null) {
                        // set the status message on the JOptionPane with
                        // the current page number
                        Object[] pageNumber = new Object[]{
                                Integer.valueOf(pageIndex + 1)};
                        statusLabel.setText(statusFormat.format(pageNumber));
                    }
                } catch (Throwable throwable) {
                    log.log(Level.WARNING, "Error showing status dialog", throwable);
                }
            }
        };   
        // call into the EDT
        SwingUtilities.invokeLater(runnable);
        
        // call into the delegate and save the return value
        return printDelegate.print(graphics, pageFormat, pageIndex);     
    }

    
    /**
     * Print the specified printable while showing a status dialog
     *
     * @param  attr             a <code>PrintRequestAttributeSet</code>
     *                          specifying any printing attributes,
     *                          or null for none
     * @return true, unless printing is cancelled by the user
     * @throws PrinterException if an error in the print system causes the job
     *                          to be aborted
     * @throws HeadlessException if the method is asked to show a printing
     *                           dialog or run interactively, and
     *                           <code>GraphicsEnvironment.isHeadless</code>
     *                           returns true
     */
    public static boolean printWithDialog(Component parentComp, Printable printable,
                         PrintRequestAttributeSet attr) throws PrinterException,
                                                     HeadlessException {

        if (attr == null) {
            attr = new HashPrintRequestAttributeSet();
        }

        // get a PrinterJob
        final PrinterJob job = PrinterJob.getPrinterJob();

        // wrap the Printable
        // need a final reference to the printable for later
        final StatusDialogPrintable wrappedPrintable = new StatusDialogPrintable(printable);

        // set the printable on the PrinterJob
        job.setPrintable(wrappedPrintable);

        // if requested, show the print dialog
        if (!job.printDialog(attr)) {
            // the user cancelled the print dialog
            return false;
        }

        // interactive, drive printing from another thread
        // and show a modal status dialog for the duration

        // prepare the status JOptionPane
        String progressTitle =
            UIManager.getString("PrintingDialog.titleProgressText");

        String dialogInitialContent =
            UIManager.getString("PrintingDialog.contentInitialText");

        // this one's a MessageFormat since it must include the page
        // number in its text
        MessageFormat statusFormat =
            new MessageFormat(
                UIManager.getString("PrintingDialog.contentProgressText"));

        String abortText =
            UIManager.getString("PrintingDialog.abortButtonText");
        String abortTooltip =
            UIManager.getString("PrintingDialog.abortButtonToolTipText");
        int abortMnemonic =
            UIManager.getInt("PrintingDialog.abortButtonMnemonic");
        int abortMnemonicIndex =
            UIManager.getInt("PrintingDialog.abortButtonDisplayedMnemonicIndex");

        final JButton abortButton = new JButton(abortText);
        abortButton.setToolTipText(abortTooltip);
        if (abortMnemonic != -1) {
            abortButton.setMnemonic(abortMnemonic);
        }
        if (abortMnemonicIndex != -1) {
            abortButton.setDisplayedMnemonicIndex(abortMnemonicIndex);
        }

        final JLabel statusLabel = new JLabel(dialogInitialContent);

        JOptionPane abortPane = new JOptionPane(statusLabel,
                                                JOptionPane.INFORMATION_MESSAGE,
                                                JOptionPane.DEFAULT_OPTION,
                                                null, new Object[] {abortButton},
                                                abortButton);

        // set the label which the wrapped printable will update
        wrappedPrintable.startUpdatingStatus(statusFormat, statusLabel);

        // create the dialog to display the JOptionPane
        final JDialog abortDialog = abortPane.createDialog(parentComp, progressTitle);
        // clicking the X button should not hide the dialog
        abortDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // the action that will abort printing
        final Action abortAction = new AbstractAction() {
            boolean isAborted = false;
            public void actionPerformed(ActionEvent ae) {
                if (!isAborted) {
                    isAborted = true;

                    // update the status dialog to indicate aborting
                    abortButton.setEnabled(false);
                    abortDialog.setTitle(
                        UIManager.getString("PrintingDialog.titleAbortingText"));
                    statusLabel.setText(
                        UIManager.getString("PrintingDialog.contentAbortingText"));

                    // we don't want the aborting status message to be clobbered
                    wrappedPrintable.stopUpdatingStatus();

                    // cancel the PrinterJob
                    job.cancel();
                }
            }
        };

        // clicking the abort button should abort printing
        abortButton.addActionListener(abortAction);

        // the look and feels set up a close action (typically bound
        // to ESCAPE) that also needs to be modified to simply abort
        // printing
        abortPane.getActionMap().put("close", abortAction);

        // clicking the X button should also abort printing
        final WindowAdapter closeListener = new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                abortAction.actionPerformed(null);
            }
        };
        abortDialog.addWindowListener(closeListener);

        // make sure this is clear since we'll check it after
        wrappedPrintable.printError = null;

        // to synchronize on
        final Object lock = new Object();

        // copied so we can access from the inner class
        final PrintRequestAttributeSet copyAttr = attr;

        // this runnable will be used to do the printing
        // (and save any throwables) on another thread
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    // do the printing
                    job.print(copyAttr);
                } catch (Throwable t) {
                    // save any Throwable to be rethrown
                    synchronized(lock) {
                        wrappedPrintable.printError = t;
                    }
                } finally {
                    // we're finished - hide the dialog, allowing
                    // processing in the original EDT to continue
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // don't want to notify the abort action
                            abortDialog.removeWindowListener(closeListener);
                            abortDialog.dispose();
                        }
                    });
                }
            }
        };

        // start printing on another thread
        Thread th = new Thread(runnable);
        th.start();

        // show the modal status dialog (and wait for it to be hidden)
        abortDialog.setVisible(true);

        // dialog has been hidden

        // look for any error that the printing may have generated
        Throwable pe;
        synchronized(lock) {
            pe = wrappedPrintable.printError;
            wrappedPrintable.printError = null;
        }

        // check the type of error and handle it
        if (pe != null) {
            // a subclass of PrinterException meaning the job was aborted,
            // in this case, by the user
            if (pe instanceof PrinterAbortException) {
                return false;
            } else if (pe instanceof PrinterException) {
                throw (PrinterException)pe;
            } else if (pe instanceof RuntimeException) {
                throw (RuntimeException)pe;
            } else if (pe instanceof Error) {
                throw (Error)pe;
            }

            // can not happen
            throw new AssertionError(pe);
        }

        return true;
    }
}
