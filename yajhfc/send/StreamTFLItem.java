package yajhfc.send;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yajhfc.FormattedFile;
import yajhfc.utils;

public class StreamTFLItem extends HylaTFLItem {
    protected FormattedFile tempFile;
    
    @Override
    public void cleanup() {
        tempFile.file.delete();
        tempFile = null;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(tempFile.file);
    }


    @Override
    public void upload(HylaFAXClient hyfc) throws FileNotFoundException, IOException, ServerResponseException {
        serverName = hyfc.putTemporary(getInputStream());
    }

    @Override
    public String getText() {
        return utils._("<stdin>");
    }

    @Override
    public boolean isMutable() {
        return false;
    }
    
    @Override
    protected FormattedFile getPreviewFilename() {
        return tempFile;
    }

    @Override
    public void setText(String newText) {
        throw new IllegalArgumentException("StreamTFLItem is immutable!");
    }
    
    public StreamTFLItem(InputStream inStream) throws IOException, FileNotFoundException {
        File tmp;
        // Copy input stream to a temporary file:
        tmp = File.createTempFile("submit", ".tmp");
        tmp.deleteOnExit();
        byte[] buf = new byte[8000];
        int len = 0;
        FileOutputStream fOut = new FileOutputStream(tmp);
        BufferedInputStream fIn = new BufferedInputStream(inStream);
        while ((len = fIn.read(buf)) >= 0) {
            fOut.write(buf, 0, len);
        }
        fOut.close();
        
        tempFile = new FormattedFile(tmp);
        tempFile.detectFormat();
    }
    
}
