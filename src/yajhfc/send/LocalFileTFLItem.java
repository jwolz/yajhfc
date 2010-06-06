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
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.launch.Launcher2;
import yajhfc.util.ExceptionDialog;

public class LocalFileTFLItem extends HylaTFLItem {    
    private static final Logger log = Logger.getLogger(LocalFileTFLItem.class.getName());
    
    protected String fileName;
    protected boolean prepared = false;
    protected FormattedFile preparedFile;
    
    private void convertFile(FileConverter fconv) {
        try {
            File tempFile = File.createTempFile("conv", ".ps");
            tempFile.deleteOnExit();
            
            FileOutputStream outStream = new FileOutputStream(tempFile);
            fconv.convertToHylaFormat(new File(fileName), outStream, desiredPaperSize, FileFormat.PDF);
            outStream.close();
            
            preparedFile = new FormattedFile(tempFile);
        }  catch (Exception e) {
            ExceptionDialog.showExceptionDialog(Launcher2.application.getFrame(), MessageFormat.format(Utils._("The document {0} could not be converted to PostScript, PDF or TIFF. Reason:"), getText()), e);
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
        if (FormattedFile.canViewFormat(format)) {
            preparedFile = new FormattedFile(fileName, format);
        } else {
            FileConverter fconv = FormattedFile.fileConverters.get(format);
            if (Utils.debugMode) {
                log.info("prepareFile: fileName='" + fileName + "' format: " + format);
            }
            if (fconv == null) {
                log.warning("Unconvertable file: " + fileName + ", format: " + format);
                preparedFile = new FormattedFile(fileName, format);
            } else {
                convertFile(fconv);
            }    
        }
        prepared = true;
    }
    
    @Override
    public FormattedFile getPreviewFilename(HylaFAXClient hyfc) {
        try {
            prepareFile();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Error preparing preview:", ex);
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
        InputStream inStream = getInputStream();
        if (inStream == null) {
            serverName = null;
        } else { 
            hyfc.form(preparedFile.format.getHylaFAXFormatString());
            serverName = hyfc.putTemporary(inStream);
        }
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
