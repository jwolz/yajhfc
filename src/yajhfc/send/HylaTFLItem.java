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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FormattedFile;

public abstract class HylaTFLItem extends TFLItem {
    protected String serverName = null;
    protected PaperSize desiredPaperSize = PaperSize.A4;
    
    /**
     * Uploads this file. Assumes that hyfc.type(HylaFAXClient.TYPE_IMAGE) has been called before.
     * @param hyfc
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ServerResponseException
     */
    public void upload(HylaFAXClient hyfc) throws FileNotFoundException, IOException, ServerResponseException {
        if (serverName == null) {
            FormattedFile inFile = getPreviewFilename();
            if (inFile == null) {
                serverName = null;
            } else { 
                FileInputStream inStream = new FileInputStream(inFile.file);
                if (Utils.getFaxOptions().sendFORMCommand)
                    hyfc.form(inFile.format.getHylaFAXFormatString());
                serverName = hyfc.putTemporary(inStream);
                inStream.close();
            }
        }
    }
    
    /**
     * Returns a local file to be used by the default implementation of preview().
     * Return null if preview is not supported.
     * @return
     */
    public FormattedFile getPreviewFilename() throws IOException {
        return null;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void cleanup() {
        // NOP
    }
    
    public void setDesiredPaperSize(PaperSize newSize) {
        desiredPaperSize = newSize;
    }
    
    public PaperSize getDesiredPaperSize() {
        return desiredPaperSize;
    }
}
