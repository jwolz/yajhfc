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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/**
 * A "safe" file system view that does not use any ShellFolder classes.
 * Most code taken from http://jython.svn.sourceforge.net/viewvc/jython/branches/Release_2_2maint/installer/src/java/org/python/util/install/RestrictedFileSystemView.java 
 * @author jonas
 *
 */
public class SafeFileSystemView extends FileSystemView {

    /**
     * 
     */
    public SafeFileSystemView() {
        
    }

    private static final String newFolderStringID = "FileChooser.other.newFolder";
    private static final String newFolderString = UIManager.getString(newFolderStringID);

    private File _defaultDirectory = null;


    /**
     * Determines if the given file is a root in the navigatable tree(s).
     *  
     * @param f a <code>File</code> object representing a directory
     * @return <code>true</code> if <code>f</code> is a root in the navigatable tree.
     * @see #isFileSystemRoot
     */
    public boolean isRoot(File f) {
        if (f == null || !f.isAbsolute()) {
            return false;
        }

        File[] roots = getRoots();
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].equals(f)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the file (directory) can be visited. Returns false if the directory cannot be traversed.
     * 
     * @param f the <code>File</code>
     * @return <code>true</code> if the file/directory can be traversed, otherwise <code>false</code>
     * @see JFileChooser#isTraversable
     * @see FileView#isTraversable
     */
    public Boolean isTraversable(File f) {
        return Boolean.valueOf(f.isDirectory());
    }

    /**
     * Name of a file, directory, or folder as it would be displayed in a system file browser
     *  
     * @param f a <code>File</code> object
     * @return the file name as it would be displayed by a native file chooser
     * @see JFileChooser#getName
     */
    public String getSystemDisplayName(File f) {
        String name = null;
        if (f != null) {
            if (isRoot(f)) {
                name = f.getAbsolutePath();
            } else {
                name = f.getName();
            }
        }
        return name;
    }

    /**
     * Type description for a file, directory, or folder as it would be displayed in a system file browser.
     * 
     * @param f a <code>File</code> object
     * @return the file type description as it would be displayed by a native file chooser or null if no native
     * information is available.
     * @see JFileChooser#getTypeDescription
     */
    public String getSystemTypeDescription(File f) {
        return null;
    }

    /**
     * Icon for a file, directory, or folder as it would be displayed in a system file browser.
     * 
     * @param f a <code>File</code> object
     * @return an icon as it would be displayed by a native file chooser, null if not available
     * @see JFileChooser#getIcon
     */
    public Icon getSystemIcon(File f) {
        if (f != null) {
            return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
        } else {
            return null;
        }
    }

    /**
     * @param folder a <code>File</code> object repesenting a directory
     * @param file a <code>File</code> object
     * @return <code>true</code> if <code>folder</code> is a directory and contains
     * <code>file</code>.
     */
    public boolean isParent(File folder, File file) {
        if (folder == null || file == null) {
            return false;
        } else {
            return folder.equals(file.getParentFile());
        }
    }

    /**
     * @param parent a <code>File</code> object repesenting a directory
     * @param fileName a name of a file or folder which exists in <code>parent</code>
     * @return a File object. This is normally constructed with <code>new
     * File(parent, fileName)</code>.
     */
    public File getChild(File parent, String fileName) {
        return createFileObject(parent, fileName);
    }

    /**
     * Checks if <code>f</code> represents a real directory or file as opposed to a special folder such as
     * <code>"Desktop"</code>. Used by UI classes to decide if a folder is selectable when doing directory choosing.
     * 
     * @param f a <code>File</code> object
     * @return <code>true</code> if <code>f</code> is a real file or directory.
     */
    public boolean isFileSystem(File f) {
        return true;
    }

    /**
     * Returns whether a file is hidden or not.
     */
    public boolean isHiddenFile(File f) {
        return f.isHidden();
    }

    /**
     * Is dir the root of a tree in the file system, such as a drive or partition.
     * 
     * @param f a <code>File</code> object representing a directory
     * @return <code>true</code> if <code>f</code> is a root of a filesystem
     * @see #isRoot
     */
    public boolean isFileSystemRoot(File dir) {
        return isRoot(dir);
    }

    /**
     * Used by UI classes to decide whether to display a special icon for drives or partitions, e.g. a "hard disk" icon.
     * 
     * The default implementation has no way of knowing, so always returns false.
     * 
     * @param dir a directory
     * @return <code>false</code> always
     */
    public boolean isDrive(File dir) {
        return false;
    }

    /**
     * Used by UI classes to decide whether to display a special icon for a floppy disk. Implies isDrive(dir).
     * 
     * The default implementation has no way of knowing, so always returns false.
     * 
     * @param dir a directory
     * @return <code>false</code> always
     */
    public boolean isFloppyDrive(File dir) {
        return false;
    }

    /**
     * Used by UI classes to decide whether to display a special icon for a computer node, e.g. "My Computer" or a
     * network server.
     * 
     * The default implementation has no way of knowing, so always returns false.
     * 
     * @param dir a directory
     * @return <code>false</code> always
     */
    public boolean isComputerNode(File dir) {
        return false;
    }

    /**
     * Returns all root partitions on this system. For example, on Windows, this would be the "Desktop" folder, while on
     * DOS this would be the A: through Z: drives.
     */
    public File[] getRoots() {
        return File.listRoots();
    }

    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.

    public File getHomeDirectory() {
        return createFileObject(System.getProperty("user.home"));
    }

    /**
     * Return the user's default starting directory for the file chooser.
     * 
     * @return a <code>File</code> object representing the default starting folder
     */
    public File getDefaultDirectory() {
        if (_defaultDirectory == null) {
            try {
                File tempFile = File.createTempFile("filesystemview", "restricted");
                tempFile.deleteOnExit();
                _defaultDirectory = tempFile.getParentFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _defaultDirectory;
    }

    /**
     * Returns a File object constructed in dir from the given filename.
     */
    public File createFileObject(File dir, String filename) {
        if (dir == null) {
            return new File(filename);
        } else {
            return new File(dir, filename);
        }
    }

    /**
     * Returns a File object constructed from the given path string.
     */
    public File createFileObject(String path) {
        File f = new File(path);
        if (isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }
        return f;
    }

    /**
     * Gets the list of shown (i.e. not hidden) files.
     */
    public File[] getFiles(File dir, boolean useFileHiding) {
        ArrayList<File> files = new ArrayList<File>();

        // add all files in dir
        File[] names;
        names = dir.listFiles();
        File f;

        int nameCount = (names == null) ? 0 : names.length;
        for (int i = 0; i < nameCount; i++) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            f = names[i];
            if (!useFileHiding || !isHiddenFile(f)) {
                files.add(f);
            }
        }

        return files.toArray(new File[files.size()]);
    }

    /**
     * Returns the parent directory of <code>dir</code>.
     * 
     * @param dir the <code>File</code> being queried
     * @return the parent directory of <code>dir</code>, or <code>null</code> if <code>dir</code> is
     * <code>null</code>
     */
    public File getParentDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File psf = dir.getParentFile();
            if (psf != null) {
                if (isFileSystem(psf)) {
                    File f = psf;
                    if (!f.exists()) {
                        // This could be a node under "Network Neighborhood".
                        File ppsf = psf.getParentFile();
                        if (ppsf == null || !isFileSystem(ppsf)) {
                            // We're mostly after the exists() override for windows below.
                            f = createFileSystemRoot(f);
                        }
                    }
                    return f;
                } else {
                    return psf;
                }
            }
        }
        return null;
    }

//    /**
//     * Creates a new <code>File</code> object for <code>f</code> with correct behavior for a file system root
//     * directory.
//     * 
//     * @param f a <code>File</code> object representing a file system root directory, for example "/" on Unix or "C:\"
//     * on Windows.
//     * @return a new <code>File</code> object
//     */
//    protected File createFileSystemRoot(File f) {
//        return new FileSystemRoot(f);
//    }
//
//    static class FileSystemRoot extends File {
//        public FileSystemRoot(File f) {
//            super(f, "");
//        }
//
//        public FileSystemRoot(String s) {
//            super(s);
//        }
//
//        public boolean isDirectory() {
//            return true;
//        }
//
//        public String getName() {
//            return getPath();
//        }
//    }

    public File createNewFolder(File containingDir) throws IOException {
        if (containingDir == null) {
            throw new IOException("Containing directory is null:");
        }
        File newFolder = null;
        newFolder = createFileObject(containingDir, newFolderString);
        int i = 2;
        while (newFolder.exists() && (i < 100)) {
            newFolder = createFileObject(containingDir, MessageFormat.format(newFolderString,
                    new Object[] { new Integer(i) }));
            i++;
        }

        if (newFolder.exists()) {
            throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
        } else {
            newFolder.mkdirs();
        }

        return newFolder;
    }
}
