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
package yajhfc.model.servconn.hylafax;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.io.OutputStream;

import yajhfc.file.FileFormat;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.defimpl.AbstractFaxDocument;

/**
 * @author jonas
 *
 */
public class HylaServerDoc<T extends FmtItem> extends AbstractFaxDocument<T> implements FaxDocument {
    private static final long serialVersionUID = 1;
    
    protected HylaServerDoc(FaxJob<T> parent, String path, FileFormat type) {
        super(parent, path, type);
    }
    
    @Override
    public String getHylafaxPath() {
        return path;
    }

    protected void downloadFromServer(final OutputStream target) throws IOException, ServerResponseException {
        HylaFAXClient hyfc = ((AbstractHylaFaxJob<T>)parent).getConnection().beginServerTransaction();
        try {
            synchronized (hyfc) {
                log.fine("Downloading " + path + " from server...");
                hyfc.type(gnu.inet.ftp.FtpClientProtocol.TYPE_IMAGE);
                hyfc.get(path, target);
            }
        } finally {
            ((AbstractHylaFaxJob<T>)parent).getConnection().endServerTransaction();
        }
    }
}
