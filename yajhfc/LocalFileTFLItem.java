package yajhfc;
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

import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.print.DocFlavor;

import yajhfc.FormattedFile.FileFormat;

public class LocalFileTFLItem extends HylaTFLItem {
    protected String fileName;
    protected boolean prepared = false;
    protected FormattedFile preparedFile;
    
    private void convertFile(DocFlavor.INPUT_STREAM flavor) {
        FileConverter fconv = new FileConverter(flavor);
        try {
            fconv.paperSize = desiredPaperSize;
            File f = fconv.convertToPSTemp(new FileInputStream(fileName));
            preparedFile = new FormattedFile(f, FileFormat.PostScript);
        }  catch (Exception e) {
            ExceptionDialog.showExceptionDialog((Dialog)null, MessageFormat.format(utils._("The document {0} could not get converted to PostScript. Reason:"), getText()), e);
        }        
    }
    
    @Override
    public void setDesiredPaperSize(PaperSize newSize) {
        if (!newSize.equals(desiredPaperSize)) {
            super.setDesiredPaperSize(newSize);
            prepared = false;
        }
    }
    
    protected void prepareFile() throws FileNotFoundException, IOException {
        if (prepared)
            return;
        
        FileFormat format = FormattedFile.detectFileFormat(fileName);
        switch (format) { 
        case JPEG:
            convertFile(DocFlavor.INPUT_STREAM.JPEG);
            break;
        case GIF:
            convertFile(DocFlavor.INPUT_STREAM.GIF);
            break;
        case PNG:
            convertFile(DocFlavor.INPUT_STREAM.PNG);
            break;
        /*case PlainText:
            convertFile(DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST);
            break;*/
        /*case Unknown:
            if (JOptionPane.showConfirmDialog(null, MessageFormat.format(utils._("The document \"{0}\" has a unknown data format. It might not get transferred by HylaFAX. Use it anyway?"), getText()), utils._("Unknown format"), JOptionPane.QUESTION_MESSAGE | JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                preparedFile = new FormattedFile(fileName, FileFormat.Unknown);
            } else {
                preparedFile = null;
            }
            break;
        */
        default: // PostScript, PDF, TIFF, ...
            preparedFile = new FormattedFile(fileName, format);
            break;
        }
        prepared = true;
    }
    
    @Override
    protected FormattedFile getPreviewFilename() {
        try {
            prepareFile();
        } catch (Exception ex) {
            return null;
        }
        
        return preparedFile;
    }
    
    @Override
    public InputStream getInputStream() throws FileNotFoundException, IOException {
        prepareFile();
        if (preparedFile == null) 
            return null;
        
        return new FileInputStream(preparedFile.file);
    }

    @Override
    public void upload(HylaFAXClient hyfc) throws FileNotFoundException, IOException, ServerResponseException {
        serverName = hyfc.putTemporary(getInputStream());
    }

    @Override
    public String getText() {
        return fileName;
    }

    @Override
    public void setText(String newText) {
        if (!fileName.equals(newText)) {
            fileName = newText;
            prepared = false;
        }        
    }
    
    public LocalFileTFLItem(String fileName) {
        this.fileName = fileName;
    }
}
