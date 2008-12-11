/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.options;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.FmtItemDescComparator;
import yajhfc.FmtItemRenderer;
import yajhfc.Utils;
import yajhfc.model.archive.QueueFileFormat;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.fmtEditor;

/**
 * @author jonas
 *
 */
public class ArchivePanel extends JPanel implements OptionsPage {

    fmtEditor<QueueFileFormat> columnEditor;
    JCheckBox checkUseArchive;
    FileTextField ftfArchiveLocation;
    List<QueueFileFormat> selection = new ArrayList<QueueFileFormat>();
    JLabel archiveLabel;
    
    public ArchivePanel() {
        super(false);
        initialize();
    }

    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        archiveLabel = new JLabel(Utils._("Location of archive directory:"));
        
        checkUseArchive = new JCheckBox(Utils._("Show archive table"));
        checkUseArchive.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkEnable();
            }
        });
        ftfArchiveLocation = new FileTextField();
        ftfArchiveLocation.getJFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ftfArchiveLocation.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        columnEditor = new fmtEditor<QueueFileFormat>(QueueFileFormat.values(), selection, Collections.<QueueFileFormat>emptyList(), new FmtItemRenderer(), FmtItemDescComparator.<QueueFileFormat>getInstance(), null, Utils._("Selected columns:"), Utils._("Available columns:"));
        
        checkUseArchive.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        ftfArchiveLocation.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        Dimension prefSize = ftfArchiveLocation.getPreferredSize();
        prefSize.width = Integer.MAX_VALUE;
        ftfArchiveLocation.setMaximumSize(prefSize);
        columnEditor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        archiveLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        Dimension spacer = new Dimension(0, OptionsWin.border);
        add(checkUseArchive);
        add(Box.createRigidArea(spacer));
        add(archiveLabel);
        add(ftfArchiveLocation);
        add(Box.createRigidArea(spacer));
        add(columnEditor);
    }

    void checkEnable() {
        boolean enable = checkUseArchive.isSelected();
        ftfArchiveLocation.setEnabled(enable);
        columnEditor.setEnabled(enable);
        archiveLabel.setEnabled(enable);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        checkUseArchive.setSelected(foEdit.showArchive);
        
        ftfArchiveLocation.setText(foEdit.archiveLocation);
        
        columnEditor.setNewSelection(foEdit.archiveFmt);
        
        checkEnable();
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.showArchive = checkUseArchive.isSelected();
        
        foEdit.archiveLocation = ftfArchiveLocation.getText();
        
        foEdit.archiveFmt.clear();
        foEdit.archiveFmt.addAll(selection);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        return true;
    }

}
