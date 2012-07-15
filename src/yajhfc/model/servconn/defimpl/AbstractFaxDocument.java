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
package yajhfc.model.servconn.defimpl;

import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.hylafax.HylaServerDoc;

public abstract class AbstractFaxDocument<T extends FmtItem> implements FaxDocument, Serializable {

    protected static final Logger log = Logger.getLogger(HylaServerDoc.class.getName());
    protected final String path;
    protected FaxJob<T> parent;
    protected FileFormat type;
    protected transient FormattedFile localDocument = null;

    public String getDefaultExtension() {
        if (type == FileFormat.Unknown) {
            int pos = path.lastIndexOf('.');
            if (pos < 0)
                return "tmp";
            else
                return path.substring(pos+1);
        } else
            return type.getDefaultExtension();
    }

    public void downloadToStream(OutputStream target) throws IOException,
            ServerResponseException {
                if (localDocument == null) {
                    log.fine("Using direct download from server");
                    downloadFromServer(target);
                } else {
                    log.fine("Reading file from local copy");
                    FileInputStream inStream = new FileInputStream(localDocument.file);
                    Utils.copyStream(inStream, target);
                    inStream.close();
                }
            }

    public FormattedFile getDocument() throws IOException, ServerResponseException {
        if (localDocument == null) {
            File tmpFile = File.createTempFile("fax", "." + getDefaultExtension());
            yajhfc.shutdown.ShutdownManager.deleteOnExit(tmpFile);
            log.fine("Copying " + path + " into " + tmpFile);
            
            FileOutputStream outStream = new FileOutputStream(tmpFile);
            downloadFromServer(outStream);
            outStream.close();
            if (type == FileFormat.Unknown) { // Try to autodetect
                type = FormattedFile.detectFileFormat(tmpFile);
            }
            localDocument = new FormattedFile(tmpFile, type);
            log.fine("Downloaded " + path + " into local file " + tmpFile + " of type " + type);
        }
        return localDocument;
    }

    protected abstract void downloadFromServer(OutputStream target) throws IOException, ServerResponseException;
    
    public String getPath() {
        return path;
    }
    
    public String getHylafaxPath() {
        return null;
    }

    public FileFormat getType() {
        return type;
    }

    @Override
    public String toString() {
        return path;
    }
    
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    
    protected AbstractFaxDocument(FaxJob<T> parent, String path, FileFormat type) {
        super();
        this.path = path;
        this.parent = parent;
        this.type = type;
    }
}