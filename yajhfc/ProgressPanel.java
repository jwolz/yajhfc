/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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
package yajhfc;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
    protected JProgressBar progressBar;
    protected JLabel progressLabel;
    protected JLabel noteLabel;
    
    protected JComponent contentComponent;
    
    protected AlphaPanel alphaPanel;
    
    protected static final int inset = 6;
    
    public ProgressPanel() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayout();
            }
        });
        
        progressPanel = new JPanel(null);
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
    }

    public JComponent getContentComponent() {
        return contentComponent;
    }

    public void setContentComponent(JComponent contentComponent) {
        if (this.contentComponent != null) {
            remove(this.contentComponent);
        }
        this.contentComponent = contentComponent;
        add(contentComponent, JLayeredPane.FRAME_CONTENT_LAYER);
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
        progressBar.setValue(progress);
    }

    public void showDeterminateProgress(String message, String initialNote, int min, int max) {
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setValue(min);
        commonProgressSetup(message, initialNote);
    }

}
