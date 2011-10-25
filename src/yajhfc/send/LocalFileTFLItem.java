package yajhfc.send;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter;
import yajhfc.file.FileConverter.ConversionException;
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
            switch (preparedFile.format) {
            case PDF:
            case PostScript:
                break;
            default:
                throw new ConversionException("Converter output for file " + fileName + " has an unsupported file format " + preparedFile.format + " (converter=" + fconv + ")");
            }
        }  catch (Exception e) {
            Launcher2.application.getDialogUI().showExceptionDialog(MessageFormat.format(Utils._("The document {0} could not be converted to PostScript, PDF or TIFF. Reason:"), getText()), e);
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
    public FormattedFile getPreviewFilename() {
        try {
            prepareFile();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Error preparing preview:", ex);
            return null;
        }
        
        return preparedFile;
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
