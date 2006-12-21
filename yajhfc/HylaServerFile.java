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
import java.text.MessageFormat;

import yajhfc.FormattedFile.FileFormat;

public class HylaServerFile {
    protected String path;
    protected String type;
    
    public String getPath() {
        return path;
    }
    
    public String getType() {
        return type;
    }
    
    public void download(HylaFAXClient hyfc, File target) 
        throws IOException, FileNotFoundException, ServerResponseException {
        FileOutputStream out = new FileOutputStream(target);
        
        hyfc.type(gnu.inet.ftp.FtpClientProtocol.TYPE_IMAGE);
        hyfc.get(path, out);
        out.close();
    }
    
    public void view(HylaFAXClient hyfc, FaxOptions opts)
        throws IOException, FileNotFoundException, ServerResponseException, UnknownFormatException {
        
        File tmptif = File.createTempFile("fax", "." + type);
        FileFormat format;
        tmptif.deleteOnExit();
        
        download(hyfc, tmptif);        
        
        if (type.equalsIgnoreCase("tif") || type.equalsIgnoreCase("tiff")) 
            format = FileFormat.TIFF;
        else if (type.equalsIgnoreCase("ps"))
            format = FileFormat.PostScript;
        else if(type.equalsIgnoreCase("pdf"))
            format = FileFormat.PDF;
        else
            throw new UnknownFormatException(MessageFormat.format(utils._("File format {0} not supported."), type));
        
        FormattedFile.viewFile(tmptif.getPath(), format);
    }
    
    @Override
    public String toString() {
        return path;
    }
    
    public HylaServerFile(String path, String type) {
        this.path = path;
        this.type = type;
    }
}

