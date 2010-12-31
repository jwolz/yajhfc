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
package yajhfc.util;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import yajhfc.util.ProgressWorker.ProgressUI;


/**
 * @author jonas
 *
 */
public class ProgressContentPane extends JPanel implements ProgressUI {
    JLabel progressLabel, noteLabel;
    JProgressBar progressBar;
    
    public ProgressContentPane() {
        super(null);
        initialize();
    }
    
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        progressLabel = new JLabel("Logging in...");
        noteLabel = new JLabel();
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
                
        this.add(progressLabel);
        this.add(noteLabel);
        this.add(Box.createVerticalStrut(5));
        this.add(progressBar);
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#close()
     */
    public void close() {
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#setNote(java.lang.String)
     */
    public void setNote(String note) {
        noteLabel.setText(note);
        SwingUtilities.getWindowAncestor(this).pack();
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#setProgress(int)
     */
    public void setProgress(int progress) {
        if (progressBar.isIndeterminate())
            progressBar.setIndeterminate(false);
        progressBar.setValue(progress);
    }

    /* (non-Javadoc)
     * @see yajhfc.util.ProgressWorker.ProgressUI#showDeterminateProgress(java.lang.String, java.lang.String, int, int)
     */
    public void showDeterminateProgress(String message, String initialNote,
            int min, int max) {
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setValue(min);
        commonProgressSetup(message, initialNote);
    }

    public boolean supportsIndeterminateProgress() {
        return true;
    }
    
    public void showIndeterminateProgress(String message, String initialNote) {
        progressBar.setIndeterminate(true);
        commonProgressSetup(message, initialNote);
    }
    
    protected void commonProgressSetup(String progressText, String noteText) {
        progressLabel.setText(progressText);
        setNote(noteText);
        SwingUtilities.getWindowAncestor(this).setVisible(true);
    }
    
    public boolean isShowingIndeterminate() {
        return progressBar.isIndeterminate();
    }
    
    public void setMaximum(int progress) {
        progressBar.setMaximum(progress);
    }
}
