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

package yajhfc;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.util.ExceptionDialog;

/**
 * This class allows to open URLs or files with the system viewer.
 * On Java 6 the JDK implementation is used, else a "home brewn" one.
 * 
 * @author jonas
 *
 */
public class DesktopManager {
    protected final String viewer = PlatformInfo.getSystemViewerCommandLine();
    
    /**
     * Opens the uri in the default browser and displays an error message if this fails
     * @param uri
     * @param parent
     */
    public void safeBrowse(URI uri, Component parent) {
        try {
            browse(uri);
        } catch (IOException e1) {
            StringSelection contents = new StringSelection(uri.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
            
            ExceptionDialog.showExceptionDialog(parent, MessageFormat.format(Utils._("Could not start the default browser for the URL \"{0}\".\nThe URL has been copied to the clipboard instead."), uri), e1);
        }
    }
    
    /**
     * Opens the uri in the default browser and throws an exception if this fails
     * @param uri
     * @param parent
     */
    public void browse(URI uri) throws IOException {
        if (viewer != null) {
            Utils.startViewer(viewer, uri);
        } else {
            throw new IOException("Could not determine the system viewer application.");
        }
    }

    public void open(File file) throws IOException {
        if (viewer != null) {
            Utils.startViewer(viewer, file);
        } else {
            throw new IOException("Could not determine the system viewer application.");
        }
    }
    
    ///////
    
    private static DesktopManager defaultManager;
    private static final String JAVA6_MANAGER = "yajhfc.Java6DesktopManager";
    
    public static DesktopManager getDefault() {
        if (defaultManager == null) {
            try {
                Class.forName("java.awt.Desktop"); // Check if java.awt.Desktop is available
                defaultManager = (DesktopManager)Class.forName(JAVA6_MANAGER).newInstance();
            } catch (Exception e) {
                if (Utils.debugMode) {
                    Logger.getLogger(DesktopManager.class.getName()).log(Level.INFO, "Using Java 5 desktop manager:", e);
                }
                defaultManager = new DesktopManager();
            }
        }
        return defaultManager;
    }
}
