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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import yajhfc.file.FormattedFile;
import yajhfc.model.servconn.FaxDocument;

public class ServerFileTFLItem extends HylaTFLItem {
    private FaxDocument hysf;
    
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return null;
    }

    @Override
    public void upload(HylaFAXClient hyfc) throws FileNotFoundException, IOException, ServerResponseException {
        // NOP
    }

    @Override
    public FormattedFile getPreviewFilename(HylaFAXClient hyfc) throws IOException {
        try {
            return hysf.getDocument();
        } catch (ServerResponseException e) {
            IOException ioEx = new IOException(e.getMessage());
            ioEx.initCause(e);
            throw ioEx;
        }
    }
    
//    @Override
//    public boolean preview(Component parent, HylaFAXClient hyfc) throws IOException, UnknownFormatException {
//        try {
//            hysf.view(hyfc);
//            return true;
//        } catch (ServerResponseException e) {
//            IOException ioEx = new IOException(e.getMessage());
//            ioEx.initCause(e);
//            throw ioEx;
//        }
//    }
    
    @Override
    public String getText() {
        return "@server:" + serverName;
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
    public void setText(String newText) {
        throw new IllegalArgumentException("ServerFileTFLItem is immutable!");
    }
    
    public ServerFileTFLItem(FaxDocument serverFile) {
        this.hysf = serverFile;
        this.serverName = serverFile.getPath();
    }
}
