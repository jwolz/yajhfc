/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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
package yajhfc.export;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class ImageExportManager {
    private static final Logger log = Logger.getLogger(ImageExportManager.class.getName());
    
    private final String outFormat = "png";
    protected final Map<Image,String> imageCache = new HashMap<Image,String>();
    protected final File htmlDoc;
    protected File outDir;
    protected int imageCounter = 0;
    
    /**
     * Returns the path of the specified image. May return null if the image could not be saved.
     * @param image
     * @return
     */
    public String getRelativePathFor(Image image) {
        String path = imageCache.get(image);
        if (path == null) {
            String fileName = String.format("image%05d.%s", imageCounter++, outFormat);
            
            File outputDir = getOutputDir();
            RenderedImage rImg = getRenderedImageFor(image);
            
            try {
                ImageIO.write(rImg, outFormat, new File(outputDir, fileName));
                path = outputDir.getName() + '/' + fileName;
            } catch (IOException e) {
                log.log(Level.WARNING, "Error exporting image " + fileName  + " to " + outputDir, e);
                path = "";
            }
            imageCache.put(image, path);
        }
        return (path == "") ? null : path;
    }
    
    protected RenderedImage getRenderedImageFor(Image img) {
        if (img instanceof RenderedImage) {
            return (RenderedImage)img;
        } else {
            BufferedImage bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            bufImg.getGraphics().drawImage(img, 0, 0, null);
            return bufImg;
        }
    }
    
    protected File getOutputDir() {
        if (outDir == null) {
            File baseDir = htmlDoc.getParentFile();
            String baseName = htmlDoc.getName();
            int pos = baseName.lastIndexOf('.');
            if (pos > 0) { // Cut off the extension if present
                baseName = baseName.substring(0, pos);
            }

            int i = 1;
            File outDir;
            String outName;
            MessageFormat dirNameFormat = new MessageFormat(Utils._("{0}-files") + "{1,choice,1#|1< ({1,number,integer})}");
            do {
                outName = dirNameFormat.format(new Object[] { baseName, i++ });
                outDir = new File(baseDir, outName);
            } while (outDir.exists());
            outDir.mkdir();
            
            this.outDir = outDir;
        }
        return outDir;
    }
    
    public void resetCache() {
        imageCache.clear();
        imageCounter = 0;
    }
    
    public ImageExportManager(File htmlDoc) {
        super();
        this.htmlDoc = htmlDoc;
    }
    
}
