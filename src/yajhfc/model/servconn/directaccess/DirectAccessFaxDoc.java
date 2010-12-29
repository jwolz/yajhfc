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
package yajhfc.model.servconn.directaccess;

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.io.OutputStream;

import yajhfc.file.FileFormat;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.defimpl.AbstractFaxDocument;

public class DirectAccessFaxDoc<T extends FmtItem> extends AbstractFaxDocument<T> {
    private static final long serialVersionUID = 1;
    
    public DirectAccessFaxDoc(DirectAccessFaxJob<T> parent, String path,
            FileFormat type) {
        super(parent, path, type);
    }

    @Override
    protected void downloadFromServer(OutputStream target) throws IOException,
            ServerResponseException {
        ((DirectAccessFaxJob<T>)parent).getDirAccessor().copyFile(path, target);
    }
}
