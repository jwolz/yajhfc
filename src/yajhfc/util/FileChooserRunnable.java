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
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import yajhfc.Utils;

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
        if (showOpen) {
            if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                selection = fileChooser.getSelectedFile();
            } else {
                selection = null;
            }
        } else {
            if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                selection = Utils.getSelectedFileFromSaveChooser(fileChooser);
            } else {
                selection = null;
            }
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
