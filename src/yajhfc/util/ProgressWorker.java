package yajhfc.util;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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


import java.awt.Window;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import yajhfc.Utils;
import yajhfc.ui.YajOptionPane;
import yajhfc.ui.swing.SwingYajOptionPane;

public abstract class ProgressWorker extends Thread{
    static final Logger log = Logger.getLogger(ProgressWorker.class.getName());
    
    protected ProgressUI progressMonitor;
    protected int progress;
    //protected Component parent;
    protected YajOptionPane dialogs;
    protected boolean closeOnExit = true;
    protected boolean working = false;
 
    /**
     * Does the actual work. Is run in a separate thread.
     */
    public abstract void doWork();
    
    /**
     * Is called when the startWork method is called.
     */
    protected void initialize() {
        // NOP
    }
    
    /**
     * Is called (in the event dispatching thread) after the work has been done,
     * but *before* the progress monitor is closed;
     */
    protected void done() {
        // NOP
    }
    
    /**
     * Is called (in the event dispatching thread) after the progress monitor has been closed;
     */
    protected void pMonClosed() {
        // NOP
    }
    
    /**
     * Return the maximum value for the progressMonitor here.
     */
    protected int calculateMaxProgress() {
        return 100;
    }
    
    public void updateNote(String note) {
        SwingUtilities.invokeLater(new NoteUpdater(note, progressMonitor));
    }
    
    public void stepProgressBar(int step) {
        progress += step;
        SwingUtilities.invokeLater(new ProgressUpdater(progress, progressMonitor));
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
        SwingUtilities.invokeLater(new ProgressUpdater(progress, progressMonitor));
    }
    
    /**
     * Returns the progress monitor used by this ProgressWorker
     * @return
     */
    public ProgressUI getProgressMonitor() {
        return progressMonitor;
    }

    /**
     * Sets the progress monitor to be used by this progress worker. 
     * <br>
     * If this is null, a default progress monitor is created and used when startWork is called.
     * @param progressMonitor
     */
    public void setProgressMonitor(ProgressUI progressMonitor) {
        if (isAlive())
            throw new IllegalStateException("Can not set progress monitor after thread has been started.");
        this.progressMonitor = progressMonitor;
    }

    public boolean isCloseOnExit() {
        return closeOnExit;
    }

    /**
     * If set to false the progress monitor is not closed on exit.
     * @param closeOnExit
     */
    public void setCloseOnExit(boolean closeOnExit) {
        this.closeOnExit = closeOnExit;
    }

    public void showExceptionDialog(String message, Exception ex) {
        dialogs.showExceptionDialog(message, ex);
    }
    
    public void showMessageDialog(String message, String title, int msgType) {
        dialogs.showMessageDialog(message, title, msgType);
    }
    
    public int showConfirmDialog(String message, String title, int optionType, int msgType) {
        return dialogs.showConfirmDialog(message, title, optionType, msgType);
    }
    
    public void startWork(Window parent, String text) {
        startWork(new SwingYajOptionPane(parent), text);
    }
    
    public void startWork(YajOptionPane dialogs, String text) {
        try {
            startWorkPriv(dialogs, text);
        } catch (Exception e) { 
            dialogs.showExceptionDialog(Utils._("Error performing the operation:"), e);
            working = false;
        } 
    }
    
    private void startWorkPriv(YajOptionPane dialogs, String text) {
        working = true;
        initialize();
        if (progressMonitor == null) {
            progressMonitor = dialogs.createDefaultProgressMonitor(text, Utils._("Initializing..."), 0, calculateMaxProgress());
        } else {
            if (progressMonitor.supportsIndeterminateProgress()) {
                progressMonitor.showIndeterminateProgress(text, Utils._("Initializing..."));
                progressMonitor.setMaximum(calculateMaxProgress());
            } else {
                progressMonitor.showDeterminateProgress(text, Utils._("Initializing..."), 0, calculateMaxProgress());
            }
        }
        progress = 0;
        //parent.setEnabled(false);
        this.dialogs = dialogs;        
        
        start();
    }
    
    @Override
    public final void run() {
        try {
            doWork();
        } catch (Exception e) { 
            dialogs.showExceptionDialog(Utils._("Error performing the operation:"), e);
        } finally {
            dialogs.invokeLater(new Runnable() {
                public void run() {
                    try {
                        try {
                            done();
                        } catch (Exception e) { 
                            dialogs.showExceptionDialog(Utils._("Error performing the operation:"), e);
                        }
                        if (dialogs.getParent() != null)
                            dialogs.getParent().setEnabled(true);
                        if (closeOnExit) {
                            progressMonitor.close();
//                            if (progressMonitor instanceof MyProgressMonitor) {
//                                progressMonitor = null;
//                            }
                            try {
                                pMonClosed();
                            } catch (Exception e) { 
                                dialogs.showExceptionDialog(Utils._("Error performing the operation:"), e);
                            } 
                        }
                    } finally {
                        working = false;
                    }
                }
            });
        }
    }
    
    public boolean isWorking() {
        return working;
    }
    
    private static class ProgressUpdater implements Runnable {
        private int progress;
        private ProgressUI pMon;
        
        public void run() {
            if (pMon != null) {
                pMon.setProgress(progress);
            }
        }
        
        public ProgressUpdater(int progress, ProgressUI pMon) {
            this.progress = progress;
            this.pMon = pMon;
        }
    }
    private static class NoteUpdater implements Runnable {
        private String note;
        private ProgressUI pMon;
        
        public void run() {
            if (pMon != null)
                pMon.setNote(note);
        }
        
        public NoteUpdater(String note, ProgressUI pMon) {
            this.note = note;
            this.pMon = pMon;
            if (Utils.debugMode) {
                log.fine("ProgressWorker setNote: " + note);
            }
        }
    }
    
    /**
     * Interface implementing the progress methods used by the ProgressWorker
     * @author jonas
     *
     */
    public interface ProgressUI {
        public void close();
        public void setNote(String note);
        /**
         * Sets the progress. If this UI is currently in indeterminate mode this
         * has to switch it to determinate.
         * @param progress
         */
        public void setProgress(int progress);
        /**
         * Sets the maximum. Should not change (in)determinate mode.
         * @param progress
         */
        public void setMaximum(int progress);
        
        public boolean supportsIndeterminateProgress();
        public void showIndeterminateProgress(String message, String initialNote);
        public boolean isShowingIndeterminate();
        
        public void showDeterminateProgress(String message, String initialNote, int min, int max);
    }
}
