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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import yajhfc.DateKind;
import yajhfc.Utils;
import yajhfc.file.FormattedFile;

public class StreamTFLItem extends HylaTFLItem {
    protected FormattedFile tempFile;
    protected String text;
    
    @Override
    public boolean equals(Object obj) {
        return (obj == this);
    }
    
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
        InputStream inStream = getInputStream();
        if (inStream == null) {
            serverName = null;
        } else { 
            hyfc.form(tempFile.format.getHylaFAXFormatString());
            serverName = hyfc.putTemporary(inStream);
        }
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean isMutable() {
        return false;
    }
    
    @Override
    public boolean isDeletable() {
        return true;
    }
    
    @Override
    public FormattedFile getPreviewFilename(HylaFAXClient hyfc) {
        return tempFile;
    }

    @Override
    public void setText(String newText) {
        throw new IllegalArgumentException("StreamTFLItem is immutable!");
    }
    

    /**
     * Creates a new StreamTFLItem by copying the specified stream to a temporary
     * file.
     * @param inStream
     * @param streamSource textual description of stream source for user display or null for a default text
     * @throws IOException
     * @throws FileNotFoundException
     */
    public StreamTFLItem(InputStream inStream, String streamSource) throws IOException, FileNotFoundException {
        File tmp;
        // Copy input stream to a temporary file:
        tmp = File.createTempFile("submit", ".ps");
        tmp.deleteOnExit();
        FileOutputStream fOut = new FileOutputStream(tmp);
        Utils.copyStream(inStream, fOut);
        fOut.close();
        
        tempFile = new FormattedFile(tmp);
        if (streamSource == null)
            streamSource = Utils._("<stdin>");
        text = streamSource + " – " + DateKind.TIME_ONLY.getFormat().format(new Date());
    }
    
}
