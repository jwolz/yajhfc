/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.send;

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FormattedFile;
import yajhfc.file.MultiFileConverter;
import yajhfc.file.UnknownFormatException;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FormattedFile.FileFormat;
import yajhfc.options.MultiFileMode;
import yajhfc.util.ProgressWorker;

/**
 * @author jonas
 *
 */
public class SendFileManager {
    protected final List<HylaTFLItem> files;
    protected File coverFile;
    protected String pdfServerName;
    protected String coverName;
    
    protected final MultiFileMode mode;
    protected final FileFormat targetFormat;
    protected final boolean createAlwaysTargetFormat;
    protected final PaperSize paperSize;
    
    private static final int FILE_DISPLAY_LEN = 30;
    
    public void uploadFiles(HylaFAXClient hyfc, ProgressWorker toUpdate) throws FileNotFoundException, IOException, ServerResponseException, UnknownFormatException, ConversionException {    
        switch (mode) {
        case NONE:
            hyfc.type(HylaFAXClient.TYPE_IMAGE);
            MessageFormat format = new MessageFormat(Utils._("Uploading {0}"));
            for (HylaTFLItem item : files) {
                toUpdate.updateNote(format.format(new Object[] {Utils.shortenFileNameForDisplay(item.getText(), FILE_DISPLAY_LEN)}));
                item.upload(hyfc);
                
                toUpdate.stepProgressBar(20);
            }
            pdfServerName = null;
            break;
        case EXCEPT_COVER:
            pdfServerName = putPDFFile(hyfc, toUpdate, null);
            break;
        case FULL_FAX:
            // Do nothing yet
            pdfServerName = null;
            break;
        }
    }
    
    public int calcMaxProgress() {
        switch (mode) {
        case NONE:
            return 20*files.size();
        case EXCEPT_COVER:
        case FULL_FAX:
            return 100 + 10*files.size();
        default:
            return 0;
        }
    }
    
    /**
     * Creates a single pdf file for the files and uploads it to the server
     * @param includeCover
     * @return the name on the server
     * @throws IOException 
     * @throws ConversionException 
     * @throws UnknownFormatException 
     * @throws ServerResponseException 
     */
    private String putPDFFile(HylaFAXClient hyfc, ProgressWorker toUpdate, File coverFile) throws IOException, UnknownFormatException, ConversionException, ServerResponseException {

        List<FormattedFile> ffs = new ArrayList<FormattedFile>(files.size() + 1);
        if (coverFile != null) {
            ffs.add(new FormattedFile(coverFile));
        }
        MessageFormat format = new MessageFormat(Utils._("Converting {0}"));
        for (HylaTFLItem item : files) {
            toUpdate.updateNote(format.format(new Object[] {Utils.shortenFileNameForDisplay(item.getText(), FILE_DISPLAY_LEN)}));
            FormattedFile ff = item.getPreviewFilename(hyfc);
            if (ff != null) // Only add valid files
                ffs.add(ff);
            toUpdate.stepProgressBar(10);
        }
        
        if (ffs.size() == 0) {
            return null; 
        } else if (ffs.size() == 1 && (!createAlwaysTargetFormat || ffs.get(0).format == targetFormat)) {
            toUpdate.updateNote(Utils._("Uploading document"));
            FileInputStream fi = new FileInputStream(ffs.get(0).file);
            hyfc.type(HylaFAXClient.TYPE_IMAGE);
            String serverName = hyfc.putTemporary(fi);
            fi.close();
            
            toUpdate.stepProgressBar(100);
            return serverName;
        } else {
            toUpdate.updateNote(MessageFormat.format(Utils._("Creating {0} from documents to send"), targetFormat));
            File pdfFile = File.createTempFile("submit", "." + targetFormat.getDefaultExtension());
            MultiFileConverter.convertMultipleFilesToSingleFile(ffs, pdfFile, targetFormat, paperSize);

            toUpdate.stepProgressBar(50);
            toUpdate.updateNote((MessageFormat.format(Utils._("Uploading {0}"), targetFormat)));
            FileInputStream fi = new FileInputStream(pdfFile);
            hyfc.type(HylaFAXClient.TYPE_IMAGE);
            String serverName = hyfc.putTemporary(fi);
            fi.close();
            pdfFile.delete();

            toUpdate.stepProgressBar(50);
            return serverName;
        }
    }
    
    public void setCoverFile(File coverFile, HylaFAXClient hyfc) throws IOException, ServerResponseException {        
        switch (mode) {
        case NONE:
        case EXCEPT_COVER:
            if (coverFile != null) {
                hyfc.type(HylaFAXClient.TYPE_IMAGE);
                FileInputStream fi = new FileInputStream(coverFile);
                coverName = hyfc.putTemporary(fi);
                fi.close();

                coverFile.delete();
            } else {
                coverName = null; 
            }
            this.coverFile = null;
            break;
        case FULL_FAX:
        default:
            coverName = null;   
            this.coverFile = coverFile;
            break;
        }
    }

    /**
     * Attaches all documents to the given fax job
     * @return
     * @throws IOException 
     * @throws ServerResponseException 
     * @throws ConversionException 
     * @throws UnknownFormatException 
     */
    public void attachDocuments(HylaFAXClient hyfc, Job job, ProgressWorker toUpdate) throws ServerResponseException, IOException, UnknownFormatException, ConversionException {
        switch (mode) {
        case NONE:
            if (coverName != null) {
                job.setProperty("COVER", coverName);
            }
            
            for (HylaTFLItem item : files) {
                job.addDocument(item.getServerName());                        
            }
            break;
        case EXCEPT_COVER:
            if (coverName != null) {
                job.setProperty("COVER", coverName);
            }
            if (pdfServerName != null)
                job.addDocument(pdfServerName);
            break;
        case FULL_FAX:
            if (coverFile == null) {
                // If no cover page is requested, create a single doc and reuse it
                // (i.e. do basically the same as for EXCEPT_COVER mode)
                if (pdfServerName == null) {
                    pdfServerName = putPDFFile(hyfc, toUpdate, coverFile);
                }
                if (pdfServerName != null)
                    job.addDocument(pdfServerName);
            } else {
                String serverName = putPDFFile(hyfc, toUpdate, coverFile);
                if (serverName != null)
                    job.addDocument(serverName);
            }
            break;
        }
    }
    
    public void cleanup() {
        for (HylaTFLItem item  : files) {
            item.cleanup();
        }
    }
    
    
    public SendFileManager(PaperSize paperSize, List<HylaTFLItem> files) {
        this(Utils.getFaxOptions().multiFileSendMode, Utils.getFaxOptions().singleFileFormat, Utils.getFaxOptions().alwaysCreateTargetFormat, paperSize, files);
    }
    
    public SendFileManager(MultiFileMode mode, FileFormat targetFormat, boolean createAlwaysPDF, PaperSize paperSize, List<HylaTFLItem> files) {
        this.mode = mode;
        this.targetFormat = targetFormat;
        this.files = files;
        this.createAlwaysTargetFormat = createAlwaysPDF;
        this.paperSize = paperSize;
    }
}
