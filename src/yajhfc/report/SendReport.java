/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2012 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.report;

import static yajhfc.Utils._;
import gnu.inet.ftp.ServerResponseException;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FormattedFile;
import yajhfc.file.UnknownFormatException;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJob;
import yajhfc.util.ProgressWorker;


/**
 * @author jonas
 *
 */
public class SendReport<T extends FmtItem> {    
    static final Logger log = Logger.getLogger(SendReport.class.getName());
    
    protected List<T> columns = new ArrayList<T>();
    
    /**
     * Number of thumbnails per page. 0 means unlimited (all on one page)
     */
    protected int thumbnailsPerPage = 0;
    
    protected String headLine = _("Fax send report");
    
    protected Font normalFont = new Font("sans-serif", Font.PLAIN, 10);
    protected Font headerFont = new Font("sans-serif", Font.BOLD, 18);
    
    protected float lineWidth = 0.125f;
    
    protected int startPage = 0;
    protected int endPage = 0;
    
    /**
     * Initializes this report
     * @param job
     * @param reader
     * @param statusWorker
     * @return the number of pages this report will have
     * @throws ConversionException 
     * @throws UnknownFormatException 
     * @throws ServerResponseException 
     * @throws IOException 
     * @throws Exception
     */
    public SendReportPrintable createPrintableForJob(FaxJob<T> job, ProgressWorker statusWorker) throws IOException, ServerResponseException, UnknownFormatException, ConversionException {
        return new SendReportPrintable(job, statusWorker);
    }
   
    public SendReportPageable createPageableForJobs(FaxJob<T>[] jobs, ProgressWorker statusWorker) throws IOException, ServerResponseException, UnknownFormatException, ConversionException {
        return new SendReportPageable(jobs, statusWorker);
    }


    public List<T> getColumns() {
        return columns;
    }
    
    public void setColumns(List<T> columns) {
		this.columns = columns;
	}

    public int getThumbnailsPerPage() {
        return thumbnailsPerPage;
    }

    

    public void setThumbnailsPerPage(int thumbnailsPerPage) {
        if (thumbnailsPerPage < 0)
            throw new IllegalArgumentException("thumbnailsPerPage must be >= 0");
        this.thumbnailsPerPage = thumbnailsPerPage;
    }
    
    public String getHeadLine() {
        return headLine;
    }
    
    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }
    
    
    
    public Font getNormalFont() {
        return normalFont;
    }

    public Font getHeaderFont() {
        return headerFont;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setNormalFont(Font normalFont) {
        this.normalFont = normalFont;
    }

    public void setHeaderFont(Font headerFont) {
        this.headerFont = headerFont;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setStartPage(int startPage) {
        if (startPage < 1)
            throw new IllegalArgumentException("startPage must be >= 1");
        this.startPage = startPage;
    }

    public void setEndPage(int endPage) {
        if (endPage < 0)
            throw new IllegalArgumentException("startPage must be >= 0");
        this.endPage = endPage;
    }

    public class SendReportPrintable implements Printable {
        protected int totalOutPages;
        protected Row[] rows;
        protected Fax2ImageConverter reader;
        protected int pageOffset = 0;
        
        protected SendReportPrintable(FaxJob<T> job, ProgressWorker statusWorker) throws IOException, ServerResponseException, UnknownFormatException, ConversionException {
          super();  
          initializeForJob(job, statusWorker);
        }        
        
        public void setPageOffset(int pageOffset) {
            this.pageOffset = pageOffset;
        }
        
        public int getPageOffset() {
            return pageOffset;
        }
        
        protected void initializeForJob(FaxJob<T> job, ProgressWorker statusWorker) throws IOException, ServerResponseException, UnknownFormatException, ConversionException {
            if (Utils.debugMode) {
                log.fine("Generating report for job " + job);
            }
            String msgPrefix = MessageFormat.format(_("Fax {0}: "), job.getIDValue());
            if (statusWorker != null) {
                statusWorker.updateNote(msgPrefix + _("Calculating information..."));
            }
            if (Utils.debugMode) {
                log.fine("Columns: " + columns);
            }
            // Calculate rows for status table
            rows = new Row[columns.size()];
            for (int i=0; i<rows.length; i++) {
                T col = columns.get(i);
                String desc = col.getDescription();
                Object oVal = job.getData(col);
                String sVal;
                if (oVal == null) {
                    sVal = "";
                } else if (col.getDataType() == Date.class) {
                    sVal = col.getDisplayDateFormat().format(oVal);
                } else if (col.getDataType() == Boolean.class) {
                    sVal = ((Boolean)oVal).booleanValue() ? _("yes") : _("no");
                } else {
                    sVal = oVal.toString();
                }
                
                rows[i] = new Row(desc, sVal);
            }
            
            // Create input PDF
            if (statusWorker != null) {
                statusWorker.updateNote(msgPrefix + _("Retrieving list of documents..."));
            }
            log.fine("Retrieving list of documents...");
            Collection<FaxDocument> docs = job.getDocuments();
            if (Utils.debugMode) {
                log.fine("Documents are: " + docs);
            }
            if (statusWorker != null) {
                statusWorker.updateNote(msgPrefix + _("Downloading documents..."));
            }
            log.fine("Downloading documents...");
            List<FormattedFile> files = new ArrayList<FormattedFile>(docs.size());
            for (FaxDocument doc : docs) {
                files.add(doc.getDocument());
            }

            if (statusWorker != null) {
                statusWorker.updateNote(msgPrefix + _("Converting documents to PNG..."));
            }
            reader = new Fax2ImageConverter();
            reader.readFiles(files, startPage-1, endPage-1);
            
            if (reader.getNumberOfPages()==0)
                throw new ConversionException("The report has no pages. Is the selected range of pages valid?");
            
            if (thumbnailsPerPage == 0) {
                totalOutPages = 1;
            } else {
                totalOutPages = (reader.getNumberOfPages() + thumbnailsPerPage - 1) / thumbnailsPerPage;
            }
        }
        
        /**
         * Print a header
         * @param cb
         * @param x
         * @param y
         * @param width
         * @param nPage 
         * @param numPages
         * @return
         * @throws IOException 
         * @throws DocumentException 
         */
        protected float printHeader(Graphics2D g, float x, float y, float width, int nPage, int numPages) {
            g.setFont(headerFont);        
            FontMetrics fm = g.getFontMetrics();
            LineMetrics lm = fm.getLineMetrics(headLine, g);
            g.drawString(headLine, x, y+lm.getAscent());
            
            g.setFont(normalFont);
            String pageText = MessageFormat.format(_("Page {0} of {1}"), nPage, numPages);
            fm = g.getFontMetrics();
            Rectangle2D bbox = fm.getStringBounds(pageText, g);
            g.drawString(pageText, x+width-(float)bbox.getWidth(), y+lm.getAscent());
            
            return y + lm.getHeight()*1.5f;
        }
        
        protected float printRows(Graphics2D g, float x, float y, float width, Row[] rows) {
            g.setFont(normalFont);
            FontMetrics fm = g.getFontMetrics();
            LineMetrics lm = fm.getLineMetrics("X", g);
            float spacer = lm.getHeight() * 0.25f;
            
            y += spacer;
            g.setStroke(new BasicStroke(lineWidth));
            Line2D.Float line = new Line2D.Float(x, y, x+width, y);
            g.draw(line);
            y += spacer;
            
            float maxWidth = 0;
            for (Row row : rows) {
                row.bbox =  fm.getStringBounds(row.description, g);
                if (row.bbox.getWidth() > maxWidth)
                    maxWidth = (float)row.bbox.getWidth();
            }
            for (Row row : rows) {
                g.drawString(row.description + ": " + row.value, x + maxWidth - (float)row.bbox.getWidth(), y+lm.getAscent());
                y += (float)row.bbox.getHeight();
            }
            
            y += spacer;
            line.y1 = line.y2 = y;
            g.draw(line);
            y += spacer;
            
            return y;
        }
        
        /**
         * 
         * @param cb
         * @param writer
         * @param x
         * @param y
         * @param width
         * @param height
         * @param pdfFile
         * @param beginPage first page to output
         * @param maxPages the maximum number of pages to output or 0 for all pages
         * @return The last page processed
         * @throws IOException
         */
        protected int printNUp(Graphics2D g, float x, float y, float width, float height, int beginPage, int maxPages) throws IOException {
            if (reader.getNumberOfPages()==0)
                throw new IOException("No pages in report (reader.NumberOfPages=0).");
            
            BufferedImage firstPage = reader.getImage(0);
            float pageWidth = firstPage.getWidth();
            float pageHeight = firstPage.getHeight();
            
            int numPages = reader.getNumberOfPages() - beginPage + 1;
            if (maxPages > 0 && numPages > maxPages)
                numPages = maxPages;
                
            int numCols = numPages;
            int numRows = 1;
            
            float factor;
            
            while (true) {
                factor = Math.min(
                        width  / (pageWidth  * numRows),
                        height / (pageHeight * numCols)
                        );
            
                float scaledHeight = pageHeight * factor;
                if (height / scaledHeight > (numRows + 1)) {
                    numRows += 1;
                    numCols  = (numPages + numRows - 1) / numRows; // set the number of rows to numPages / numRows, rounded up 
                } else {
                    break;
                }
            }
            

            g.setStroke(new BasicStroke(lineWidth));
            
            float cellWidth  = width  / numCols;
            float cellHeight = height / numRows;
            int iPage = 0;
            for (int row=0; row<numRows; row++) {
                for (int col = 0; col<numCols; col++) {
                    iPage = row * numCols + col + beginPage;
                    if (iPage >= (beginPage + numPages)) {
                        return iPage-1;
                    }
                    BufferedImage page = reader.getImage(iPage-1);
                    
                    factor = Math.min(
                            cellWidth / page.getWidth(),
                            cellHeight / page.getHeight()
                            );
                    
                    float drawWidth  = page.getWidth() * factor;
                    float drawHeight = page.getHeight() * factor;
                    float drawX      = x + col * cellWidth  + (cellWidth  - drawWidth)  / 2;
                    float drawY      = y + row * cellHeight + (cellHeight - drawHeight) / 2;
                    
                    g.drawImage(page, new AffineTransform(factor, 0, 0, factor, drawX, drawY), null);
                    
                    g.draw(new Rectangle2D.Float(drawX, drawY, drawWidth, drawHeight));
                }
            }
            
            return iPage;
        }
        

        public int print(Graphics graphics, PageFormat pf, int iPage) throws PrinterException {
            iPage -= pageOffset;
            
            if (iPage >= totalOutPages)
                return Printable.NO_SUCH_PAGE;
            
            try {
                int firstPage = iPage * thumbnailsPerPage + 1;
                int maxPage = reader.getNumberOfPages();
                if (Utils.debugMode)
                    log.fine("maxPage=" + maxPage + "; totalOutPages=" + totalOutPages + "; thumbnailsPerPage=" + thumbnailsPerPage);


                if (Utils.debugMode)
                    log.fine("Writing page " + iPage + "; firstPage=" + firstPage);

                Graphics2D g = (Graphics2D)graphics;
                float x = (float)pf.getImageableX();
                float y = (float)pf.getImageableY();
                float width  = (float)pf.getImageableWidth() - lineWidth;

                y = printHeader(g, x, y, width, iPage+1, totalOutPages);
                y = printRows(g, x, y, width, rows);

                float height = (float)pf.getImageableHeight() + (float)pf.getImageableY() - y - lineWidth;

                printNUp(g, x, y, width, height, firstPage, Math.min(thumbnailsPerPage, maxPage-firstPage+1));
                return Printable.PAGE_EXISTS;
            } catch (Exception e) {
                throw (PrinterException)new PrinterException("Error printing report page " + (iPage+1)).initCause(e);
            }
        }
        
        public int getNumberOfPages() {
            return totalOutPages;
        }
        
        public void cleanup() {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }
    }
    
    public class SendReportPageable implements Pageable {
        protected PageFormat pageFormat = new PageFormat();
        
        protected SendReportPrintable[] printables;
        protected int totalNumberOfPages;
        protected ProgressWorker worker;

        protected SendReportPageable(FaxJob<T>[] jobs, ProgressWorker worker) throws IOException, ServerResponseException, UnknownFormatException, ConversionException {
            super();  
            createPrintables(jobs, worker);
          };
        
        @SuppressWarnings("unchecked")
        public void createPrintables(FaxJob<T>[] jobs, ProgressWorker worker) throws IOException, ServerResponseException, UnknownFormatException, ConversionException  {
            printables = new SendReport.SendReportPrintable[jobs.length];
            totalNumberOfPages = 0;
            this.worker = worker;
            
            for (int i=0; i<jobs.length; i++) {
                SendReportPrintable srp = createPrintableForJob(jobs[i], worker);
                srp.setPageOffset(totalNumberOfPages);
                
                totalNumberOfPages += srp.getNumberOfPages();
                
                printables[i] = srp;
            }
        }
        
        public void cleanup() {
            if (printables != null) {
                for (SendReport<T>.SendReportPrintable printable : printables) {
                    if (printable != null)
                        printable.cleanup();
                }
                printables = null;
            }
        }
        
        public int getNumberOfPages() {
            return totalNumberOfPages;
        }

        public PageFormat getPageFormat(int pageIndex)
                throws IndexOutOfBoundsException {
            return pageFormat;
        }
        
        public void setPageFormat(PageFormat pageFormat) {
            this.pageFormat = pageFormat;
        }

        public Printable getPrintable(int pageIndex)
                throws IndexOutOfBoundsException {
            if (pageIndex >= totalNumberOfPages)
                throw new IndexOutOfBoundsException();
            
            if (worker != null) {
                worker.updateNote(MessageFormat.format(Utils._("Printing page {0}"), (pageIndex+1)));
                worker.setProgress(pageIndex * 100 /  totalNumberOfPages);
            }
            
            // Find the report this page is in
            for (SendReportPrintable srp : printables) {
                if ((pageIndex - srp.getPageOffset()) < srp.getNumberOfPages()) {
                    // Page is member of the current printable
                    return srp;
                }
            }
            
            throw new IndexOutOfBoundsException("No report found with page " + pageIndex);
        }
    }
    
    static class Row {
        public final String description;
        public final String value;
        public Rectangle2D bbox;
        
        
        public Row(String description, String value) {
            super();
            this.description = description;
            this.value = value;
        }
    }
}
