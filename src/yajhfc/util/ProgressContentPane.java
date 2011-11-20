/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
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
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
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
