package yajhfc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSize;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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

public class FileConverter implements PrintJobListener {
    protected int completed;
    protected final static int NOT_COMPLETED = 0;
    protected final static int COMPLETED_SUCCESSFULLY = 1;
    protected final static int FAILED = 2;
    
    public PaperSize paperSize = utils.papersizes[0]; // A4
    public DocFlavor.INPUT_STREAM flavor;
    
    public FileConverter(DocFlavor.INPUT_STREAM flavor) {        
        this.flavor = flavor;
    }
    
    public File convertToPSTemp(InputStream inStream) throws IOException, FileNotFoundException, ConversionException, PrintException {
        File tempFile = File.createTempFile("conv", ".ps");
        tempFile.deleteOnExit();
        
        FileOutputStream outStream = new FileOutputStream(tempFile);
        convertToPS(inStream, outStream);
        outStream.close();
        return tempFile;
    }
    
    public void convertToPS(InputStream inStream, OutputStream destination) throws IOException, FileNotFoundException, ConversionException, PrintException {
                
        if (!(inStream instanceof BufferedInputStream))
            inStream = new BufferedInputStream(inStream);
        if (!(destination instanceof BufferedOutputStream))
            destination = new BufferedOutputStream(destination);
        
        StreamPrintServiceFactory[] services = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
        if (services.length < 1) {
            throw new ConversionException(MessageFormat.format(utils._("Cannot find a PrintService to convert files of type {0} to Postscript!"), flavor.getMimeType())); 
        }
        
        PrintService ps = services[0].getPrintService(destination);
        DocPrintJob dpj = ps.createPrintJob();

        dpj.addPrintJobListener(this);
        completed = NOT_COMPLETED;
        
        PrintRequestAttributeSet prset = new HashPrintRequestAttributeSet();
        
        MediaSize mediaSize;
        if (paperSize.desc.equals("A4"))
            mediaSize = MediaSize.ISO.A4;
        else if (paperSize.desc.equals("A5"))
            mediaSize = MediaSize.ISO.A5;
        else if (paperSize.desc.equals("Letter"))
            mediaSize = MediaSize.NA.LETTER;
        else if (paperSize.desc.equals("Legal"))
            mediaSize = MediaSize.NA.LEGAL;
        else
            mediaSize = MediaSize.ISO.A4;
        prset.add(mediaSize.getMediaSizeName());
        
        dpj.print(new SimpleDoc(inStream, flavor, null), prset);

        while (completed == NOT_COMPLETED) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                // NOP
            }            
        }
        
        if (completed != COMPLETED_SUCCESSFULLY) {
            throw new ConversionException(utils._("An error occured while converting the document to PostScript."));
        }
    }
    
    public void printDataTransferCompleted(PrintJobEvent pje) {
        // NOP
        //System.out.println("printDataTransferCompleted");
    }

    public void printJobCompleted(PrintJobEvent pje) {
        completed = COMPLETED_SUCCESSFULLY;
    }

    public void printJobCanceled(PrintJobEvent pje) {
        completed = FAILED;
    }

    public void printJobFailed(PrintJobEvent pje) {
        completed = FAILED;
    }

    public void printJobNoMoreEvents(PrintJobEvent pje) {
        completed = COMPLETED_SUCCESSFULLY;
    }

    public void printJobRequiresAttention(PrintJobEvent pje) {
        //NOP
        //System.out.println("printJobRequiresAttention");
    }       
    
    public static class ConversionException extends Exception {
        public ConversionException(String message) {
            super(message);
        }
        
        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
