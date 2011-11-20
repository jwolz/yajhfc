package yajhfc.file;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.MessageFormat;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSize;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.util.ReplacerOutputStream;

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

public class PrintServiceFileConverter implements PrintJobListener, FileConverter {
    protected int completed;
    protected final static int NOT_COMPLETED = 0;
    protected final static int COMPLETED_SUCCESSFULLY = 1;
    protected final static int FAILED = 2;
    
    public DocFlavor flavor;
    
    public PrintServiceFileConverter(DocFlavor flavor) {
        this.flavor = flavor;
    }
      
    public void convertToHylaFormat(File inFile, OutputStream destination, PaperSize paperSize, FileFormat desiredFormat) throws ConversionException {
        try {
            convertUsingPrintService(inFile.toURI().toURL(), destination, paperSize);
        } catch (MalformedURLException e) {
            throw new ConversionException(e);
        }
    }
    
    public boolean isOverridable() {
        return true;
    }
    
    private static final byte[] jdk16PSToReplace;
    private static final byte[] jdk16PSReplacement;
    static {
        byte[] bytes;
        try {
            bytes = "/DeferredMediaSelection true".getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            bytes = new byte[0];
        }
        jdk16PSToReplace = bytes;
        jdk16PSReplacement = new byte[0];
    }
    protected void convertUsingPrintService(Object inObject, OutputStream destination, PaperSize paperSize) throws ConversionException {
//        if (!(destination instanceof BufferedOutputStream))
//            destination = new BufferedOutputStream(destination);
        if (Utils.getFaxOptions().useJDK16PSBugfix) {
            destination = new ReplacerOutputStream(destination, jdk16PSToReplace, jdk16PSReplacement);
        }
        
        StreamPrintServiceFactory[] services = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
        if (services.length < 1) {
            throw new ConversionException(MessageFormat.format(Utils._("Cannot find a PrintService to convert files of type {0} to Postscript!"), flavor.getMimeType())); 
        }
        
        PrintService ps = services[0].getPrintService(destination);
        DocPrintJob dpj = ps.createPrintJob();

        dpj.addPrintJobListener(this);
        completed = NOT_COMPLETED;
        
        PrintRequestAttributeSet prset = new HashPrintRequestAttributeSet();
        
        MediaSize mediaSize;
        switch (paperSize) {
        default:
        case A4:
            mediaSize = MediaSize.ISO.A4;
            break;
        case A5:
            mediaSize = MediaSize.ISO.A5;
            break;
        case LETTER:
            mediaSize = MediaSize.NA.LETTER;
            break;
        case LEGAL:
            mediaSize = MediaSize.NA.LEGAL;
            break;
        }
        prset.add(mediaSize.getMediaSizeName());
        
        try {
            dpj.print(new SimpleDoc(inObject, flavor, null), prset);
        } catch (PrintException e1) {
            throw new ConversionException(e1);
        }

        while (completed == NOT_COMPLETED) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                // NOP
            }            
        }
        
        if (completed != COMPLETED_SUCCESSFULLY) {
            throw new ConversionException(Utils._("An error occured while converting the document to PostScript."));
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
 
}
