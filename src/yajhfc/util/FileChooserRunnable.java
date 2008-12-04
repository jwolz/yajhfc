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
package yajhfc.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author jonas
 *
 */
public class FileChooserRunnable implements Runnable {
    protected final JFileChooser fileChooser;
    protected final Component parent;
    protected File selection;
    protected final boolean showOpen;
    protected final String title;
    protected final FileFilter[] fileFilters;
    
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (title != null) {
            fileChooser.setDialogTitle(title);
        }
        if (fileFilters != null) {
            fileChooser.resetChoosableFileFilters();
            if (fileFilters.length >= 1) {
                for (FileFilter ff : fileFilters) {
                    fileChooser.addChoosableFileFilter(ff);
                }
                fileChooser.setFileFilter(fileFilters[0]);
            }
        }
        if (selection != null) {
            fileChooser.setSelectedFile(selection);
        }
        int res;
        if (showOpen) {
            res = fileChooser.showOpenDialog(parent);
        } else {
            res = fileChooser.showSaveDialog(parent);
        }
        if (res == JFileChooser.APPROVE_OPTION) {
            selection = fileChooser.getSelectedFile();
        } else {
            selection = null;
        }
    }

    /**
     * The selected file. Is null if the user clicked cancel.
     * @return
     */
    public File getSelection() {
        return selection;
    }

    /**
     * Create a new file chooser runnable
     * @param parent the dialog's parent
     * @param fileChooser the file chooser to show
     * @param title The dialog's title. Specify null to leave unchanged.
     * @param fileFilters The file filters. The first one will be pre-selected. Specify null to leave unchanged.
     * @param defaultSelection The file to preselect. Specify null to leave unchanged.
     * @param showOpen true to show an open dialog, false to show a save dialog.
     */
    public FileChooserRunnable(Component parent, JFileChooser fileChooser, String title, FileFilter[] fileFilters, File defaultSelection, boolean showOpen) {
        super();
        this.fileChooser = fileChooser;
        this.showOpen = showOpen;
        this.parent = parent;
        this.title = title;
        this.fileFilters = fileFilters;
        this.selection = defaultSelection;
    }

}
