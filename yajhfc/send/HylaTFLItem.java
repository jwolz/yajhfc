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

import java.awt.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import yajhfc.FormattedFile;
import yajhfc.PaperSize;
import yajhfc.UnknownFormatException;
import yajhfc.utils;

public abstract class HylaTFLItem extends TFLItem {
    protected String serverName = "<invalid>";
    protected PaperSize desiredPaperSize = utils.papersizes[0];
    
    public abstract void upload(HylaFAXClient hyfc) throws FileNotFoundException, IOException, ServerResponseException ;
    
    // May return null!
    public abstract InputStream getInputStream() throws FileNotFoundException, IOException;
    
    
    /**
     * Previews the file in a viewer.
     */
    public boolean preview(Component parent, HylaFAXClient hyfc) throws IOException, UnknownFormatException {
        FormattedFile previewFile = getPreviewFilename();
        if (previewFile == null) {
            JOptionPane.showMessageDialog(parent, MessageFormat.format(utils._("Preview is not supported for document \"{0}\"."), this.getText()), utils._("Preview"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        previewFile.view();
        return true;
    }
    
    /**
     * Returns a local file to be used by the default implementation of preview().
     * Return null if preview is not supported.
     * @return
     */
    protected FormattedFile getPreviewFilename() {
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
