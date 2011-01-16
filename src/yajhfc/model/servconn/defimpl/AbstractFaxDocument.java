/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
            tmpFile.deleteOnExit();
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