package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import yajhfc.file.FormattedFile;
import yajhfc.file.UnknownFormatException;
import yajhfc.file.FormattedFile.FileFormat;

public class HylaServerFile {
    protected String path;
    protected FileFormat type;
    
    public String getPath() {
        return path;
    }
    
    public FileFormat getType() {
        return type;
    }
    
    public void download(HylaFAXClient hyfc, File target) 
        throws IOException, FileNotFoundException, ServerResponseException {
        FileOutputStream out = new FileOutputStream(target);
        
        synchronized (hyfc) {
            hyfc.type(gnu.inet.ftp.FtpClientProtocol.TYPE_IMAGE);
            hyfc.get(path, out);
        }
        out.close();
    }
    
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
    
    public void view(HylaFAXClient hyfc)
        throws IOException, FileNotFoundException, ServerResponseException, UnknownFormatException {
        
        File tmptif = File.createTempFile("fax", "." + getDefaultExtension());
        tmptif.deleteOnExit();
        
        download(hyfc, tmptif);        
        
        if (type == FileFormat.Unknown) { // Try to autodetect
            type = FormattedFile.detectFileFormat(tmptif.getPath());
        }
        FormattedFile.viewFile(tmptif.getPath(), type);
    }
    
    @Override
    public String toString() {
        return path;
    }
    
    public HylaServerFile(String path, FileFormat type) {
        this.path = path;
        this.type = type;
    }
}

