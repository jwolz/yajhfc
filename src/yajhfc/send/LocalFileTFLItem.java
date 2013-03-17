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
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
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
import yajhfc.file.FileConverters;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.launch.Launcher2;

public class LocalFileTFLItem extends HylaTFLItem {    
    private static final Logger log = Logger.getLogger(LocalFileTFLItem.class.getName());
    
    protected String fileName;
    protected boolean prepared = false;
    protected FormattedFile preparedFile;
    
    private void convertFile(FileConverter fconv) {
        try {
            File tempFile = File.createTempFile("conv", ".ps");
            yajhfc.shutdown.ShutdownManager.deleteOnExit(tempFile);
            
            FileOutputStream outStream = new FileOutputStream(tempFile);
            fconv.convertToHylaFormat(new File(fileName), outStream, desiredPaperSize, FileFormat.PDF);
            outStream.close();
            
            preparedFile = new FormattedFile(tempFile);
            switch (preparedFile.getFormat()) {
            case PDF:
            case PostScript:
                break;
            default:
                throw new ConversionException("Converter output for file " + fileName + " has an unsupported file format " + preparedFile.getFormat() + " (converter=" + fconv + ")");
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
            FileConverter fconv = FileConverters.getConverterFor(format);
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
    
    public String getFileName() {
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
