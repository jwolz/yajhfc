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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 * A special JFileChooser implementation which includes a workaround for Java bug
 * #6544857, see: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
 * @author jonas
 *
 */
public class SafeJFileChooser extends JFileChooser {

    /**
     * 
     */
    public SafeJFileChooser() {
    }

    /**
     * @param currentDirectoryPath
     */
    public SafeJFileChooser(String currentDirectoryPath) {
        super(currentDirectoryPath);
    }

    /**
     * @param currentDirectory
     */
    public SafeJFileChooser(File currentDirectory) {
        super(currentDirectory);
    }

    
    @Override
    protected void setup(FileSystemView view) {
        try {
            super.setup(view);
        } catch (Exception ex) {
            Logger.getLogger(SafeJFileChooser.class.getName()).log(Level.WARNING, "Original setup method failed:", ex);
            super.setup(new SafeFileSystemView());
        }
    }

}
