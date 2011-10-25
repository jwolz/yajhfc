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
