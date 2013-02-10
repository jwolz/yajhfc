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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A Panel that with a process indicator that may be shown above the content
 * @author jonas
 */
public class ProgressPanel extends JLayeredPane implements ProgressWorker.ProgressUI {

    protected JPanel progressPanel;
    public JPanel getProgressPanel() {
        return progressPanel;
    }

    protected JProgressBar progressBar;
    protected JLabel progressLabel;
    protected JLabel noteLabel;
    
    protected JComponent contentComponent;
    
    protected AlphaPanel alphaPanel;
    
    protected static final int inset = 6;
    
    public ProgressPanel() {     
        progressPanel = new JPanel(null, false);
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(inset, inset, inset, inset)));
        progressPanel.setVisible(false);
        
        progressLabel = new JLabel("Doing something...");
        noteLabel = new JLabel();
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
                
        progressPanel.add(progressLabel);
        progressPanel.add(noteLabel);
        progressPanel.add(Box.createVerticalStrut(inset));
        progressPanel.add(progressBar);
        
        alphaPanel = new AlphaPanel();
        
        add(progressPanel, JLayeredPane.MODAL_LAYER);
        
        add(alphaPanel, JLayeredPane.MODAL_LAYER-1);
        
        setLayout(new LayoutManager() {
            public void layoutContainer(Container parent) {
                relayout();
            }

            public Dimension minimumLayoutSize(Container parent) {
                return contentComponent.getMinimumSize();
            }

            public Dimension preferredLayoutSize(Container parent) {
                return contentComponent.getPreferredSize();
            }

            public void removeLayoutComponent(Component comp) {
                //stub
            }
            
            public void addLayoutComponent(String name, Component comp) {
                // stub
            }
        });
    }

    public JComponent getContentComponent() {
        return contentComponent;
    }

    public void setContentComponent(JComponent contentComponent) {
        if (this.contentComponent != null) {
            remove(this.contentComponent);
        }
        this.contentComponent = contentComponent;
        add(contentComponent, JLayeredPane.DEFAULT_LAYER);
    }

    /**
     * Recomputes the layout
     */
    public void relayout() {
        relayout(isShowingProgress());
    }
    
    protected void relayout(boolean computeProgressPanel) {
        int width = getWidth();
        int height = getHeight();
            
       contentComponent.setBounds(0, 0, width, height);
       if (computeProgressPanel) {
           alphaPanel.setBounds(0, 0, width, height);           
           
           Dimension progressSize = progressPanel.getPreferredSize();
           int x = (width - progressSize.width) / 2;
           int y = (height - progressSize.height) / 2;
           progressPanel.setBounds(x, y, progressSize.width, progressSize.height);
       }
    }
    
    public boolean supportsIndeterminateProgress() {
        return true;
    }
    
    public void showIndeterminateProgress(String message) {
        showIndeterminateProgress(message, null);
    }

    public void showIndeterminateProgress(String message, String initialNote) {
        progressBar.setIndeterminate(true);
        commonProgressSetup(message, initialNote);
    }
    
    protected void commonProgressSetup(String progressText, String noteText) {
        progressLabel.setText(progressText);
        setNote(noteText);
        if (!isShowingProgress()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            contentComponent.setEnabled(false);
            relayout(true);
            alphaPanel.setVisible(true);
            progressPanel.setVisible(true);
        }
    }
    
    public void hideProgress() {
        progressBar.setIndeterminate(false);
        progressPanel.setVisible(false);
        alphaPanel.setVisible(false);
        contentComponent.setEnabled(true);
        setCursor(Cursor.getDefaultCursor());
    }
    
    public boolean isShowingProgress() {
        return progressPanel.isVisible();
    }

    public void close() {
        hideProgress();
    }

    
    public void setNote(String note) {
        if (note == null || note.length() == 0) {
            noteLabel.setText("");
            noteLabel.setVisible(false);
        }else {
            noteLabel.setText(note);
            noteLabel.setVisible(true);
        }
        if (isShowingProgress()) {
            relayout(true);
        }
    }

    public void setProgress(int progress) {
        if (progressBar.isIndeterminate())
            progressBar.setIndeterminate(false);
        progressBar.setValue(progress);
    }

    public void setMaximum(int progress) {
        progressBar.setMaximum(progress);
    }
    
    public void showDeterminateProgress(String message, String initialNote, int min, int max) {
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setValue(min);
        commonProgressSetup(message, initialNote);
    }

    public boolean isShowingIndeterminate() {
        return progressBar.isIndeterminate();
    }

}
