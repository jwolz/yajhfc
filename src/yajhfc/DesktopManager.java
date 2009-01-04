/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
    protected final String viewer = Utils.getSystemViewerCommandLine();
    
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
