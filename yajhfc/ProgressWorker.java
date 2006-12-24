package yajhfc;
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


import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

public abstract class ProgressWorker extends Thread {
    
    protected ProgressMonitor pMon;
    private int progress;
    private Component parent;
    
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
        SwingUtilities.invokeLater(new NoteUpdater(note, pMon));
    }
    
    public void stepProgressBar(int step) {
        progress += step;
        SwingUtilities.invokeLater(new ProgressUpdater(progress, pMon));
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
        SwingUtilities.invokeLater(new ProgressUpdater(progress, pMon));
    }
    
    public void showExceptionDialog(String message, Exception ex) {
        try {
            SwingUtilities.invokeAndWait(new ExcDlgDisplayer(parent, ex, message));
        } catch (InterruptedException e) {
            // NOP
        } catch (InvocationTargetException e) {
            // NOP
        }
    }
    
    public void showMessageDialog(String message, String title, int msgType) {
        try {
            SwingUtilities.invokeAndWait(new MsgDlgDisplayer(parent, message, title, msgType));
        } catch (InterruptedException e) {
            // NOP
        } catch (InvocationTargetException e) {
            // NOP
        }
    }
    
    public void startWork(Dialog parent, String text) {
        startWorkPriv(parent, text);
    }
    public void startWork(Frame parent, String text) {
        startWorkPriv(parent, text);
    }
    
    private void startWorkPriv(Component parent, String text) {
        initialize();
        pMon = new ProgressMonitor(parent, text, utils._("Initializing..."), 0, calculateMaxProgress());
        progress = 0;
        parent.setEnabled(false);
        this.parent = parent;
        
        start();
    }
    
    @Override
    public void run() {
        doWork();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                done();
                
                parent.setEnabled(true);
                pMon.close();
                pMon = null;
                
                pMonClosed();
            }
        });
    }
    
    private static class MsgDlgDisplayer implements Runnable {
        private Component parent;
        private String msg;
        private String title;
        private int msgType;
        
        public void run() {
            JOptionPane.showMessageDialog(parent, msg, title, msgType);
        }
        
        public MsgDlgDisplayer(Component parent, String msg, String title, int msgType) {
            this.parent = parent;
            this.msg = msg;
            this.title = title;
            this.msgType = msgType;
        }
    }
    private static class ExcDlgDisplayer implements Runnable {
        private Component parent;
        private Exception ex;
        private String msg;
        
        public ExcDlgDisplayer(Component parent, Exception ex, String msg) {
            this.parent = parent;
            this.ex = ex;
            this.msg = msg;
        }
        
        public void run() {
            if (parent instanceof Dialog)
                ExceptionDialog.showExceptionDialog((Dialog)parent, msg, ex);
            else if (parent instanceof Frame)
                ExceptionDialog.showExceptionDialog((Frame)parent, msg, ex);
            else
                JOptionPane.showMessageDialog(parent, msg + "\n" + ex.getMessage(), utils._("Error"), JOptionPane.ERROR_MESSAGE);;
        }
    }
    private static class ProgressUpdater implements Runnable {
        private int progress;
        private ProgressMonitor pMon;
        
        public void run() {
            if (pMon != null)
                pMon.setProgress(progress);
        }
        
        public ProgressUpdater(int progress, ProgressMonitor pMon) {
            this.progress = progress;
            this.pMon = pMon;
        }
    }
    private static class NoteUpdater implements Runnable {
        private String note;
        private ProgressMonitor pMon;
        
        public void run() {
            if (pMon != null)
                pMon.setNote(note);
        }
        
        public NoteUpdater(String note, ProgressMonitor pMon) {
            this.note = note;
            this.pMon = pMon;
        }
    }
}
