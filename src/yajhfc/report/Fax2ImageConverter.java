/**
 * 
 */
package yajhfc.report;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import yajhfc.PaperSize;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.file.GhostScriptMultiFileConverter;
import yajhfc.file.UnknownFormatException;

/**
 * @author jonas
 *
 */
public class Fax2ImageConverter {
    private static final String FILENAME_PATTERN = "page%d.png";
    
    protected File tempDir = null;
    protected List<File> pngFiles = null;
    protected BufferedImage[] images = null;
    protected PaperSize paperSize = PaperSize.A4;
    protected String gsDevice;
    
    public Fax2ImageConverter() {
        this("pngmono");
    }
    
    public Fax2ImageConverter(String gsDevice) {
        this.gsDevice = gsDevice;
    }
    
    /**
     * Read the specified pages (zero-based) from the given list of files
     * @param files
     * @param startPage the first page to read, inclusive
     * @param endPage the last page to read, inclusive; -1 means read all pages
     * @throws IOException
     * @throws UnknownFormatException
     * @throws ConversionException
     */
    public void readFiles(List<FormattedFile> files, int startPage, int endPage)
            throws IOException, UnknownFormatException, ConversionException {
        close();
        
        tempDir = File.createTempFile("report", ".dir");
        tempDir.delete();
        tempDir.mkdir();
        
        PNGMultiFileConverter conv = new PNGMultiFileConverter(gsDevice);
        conv.convertMultipleFiles(files, new File(tempDir, FILENAME_PATTERN), paperSize);
        
        pngFiles = new ArrayList<File>();
        int iPage = 1;
        File png;
        while ((png = new File(tempDir, String.format(FILENAME_PATTERN, iPage))).exists() &&
                (endPage<0 || iPage <= (endPage+1))) {
            if (iPage >= (startPage+1)) {
                pngFiles.add(png);
            }
            iPage++;
        }
        images = new BufferedImage[pngFiles.size()];
    }

    public int getNumberOfPages() {
        return images.length;
    }

    public void close() {
        if (pngFiles != null) {
            images = null;
            pngFiles = null;
            deleteTree(tempDir);
            tempDir = null;
        }
    }
    
    public PaperSize getPaperSize() {
        return paperSize;
    }
    
    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }
    
    public String getGsDevice() {
        return gsDevice;
    }
    
    public void setGsDevice(String gsDevice) {
        this.gsDevice = gsDevice;
    }
    
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    /**
     * Gets the image for the Page iPage (zero-based)
     * @param iPage
     * @return
     * @throws IOException
     */
    public BufferedImage getImage(int iPage) throws IOException {
        BufferedImage img = images[iPage];
        if (img == null) {
            File imgFile = pngFiles.get(iPage);
            return images[iPage] = ImageIO.read(imgFile);
        } else {
            return img;
        }
    }

    private static void deleteTree(File root) {
        if (root.isDirectory()) {
            for (File entry : root.listFiles()) {
                deleteTree(entry);
            }
        }
        
        root.delete();
    }
    
    protected static class PNGMultiFileConverter extends GhostScriptMultiFileConverter {
        protected final String device;
        
        public PNGMultiFileConverter(String device) {
            super();
            this.device = device;
        }

        @Override
        public FileFormat getTargetFormat() {
            return FileFormat.PNG;
        }

        @Override
        protected String[] getAdditionalGSParams() {
            return NO_ADDITIONAL_PARAMETERS;
        }

        @Override
        protected String getGSDevice() {
            return device;
        }
    }
}
