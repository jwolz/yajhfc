/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.file;

import yajhfc.Utils;

public enum FileFormat {
    PostScript(Utils._("Postscript documents"), "ps"),
    PDF(Utils._("PDF documents"), "pdf"),
    PCL(Utils._("PCL files"), "pcl"),
    JPEG(Utils._("JPEG pictures"), "jpeg", "jpg"),
    PNG(Utils._("PNG pictures"),"png"),
    GIF(Utils._("GIF pictures"),"gif"),
    TIFF(Utils._("TIFF pictures"),"tiff", "tif"),
    TIFF_DITHER(Utils._("TIFF pictures"), "TIFF (dithered)", "tiff", new String[] { "tiff", "tif" }),
    PlainText(Utils._("Text files"),"txt"),
    XML(Utils._("XML documents"), "xml"),
    FOP(Utils._("XSL:FO documents"), "fo", "xml", "fop"),
    ODT(Utils._("OpenDocument text documents"), "odt"),
    HTML(Utils._("HTML documents"), "html", "htm"),
    RTF(Utils._("RTF documents"), "rtf"),
    Unknown(Utils._("Unknown files"), "");
    
    private String defaultExt;
    private String[] possibleExts;
    private String description;
    private String shortDesc;
    
    private FileFormat(String description, String... possibleExts) {
        this(description, null, possibleExts[0], possibleExts);
    }
    
    private FileFormat(String description, String shortDesc, String defaultExt, String[] possibleExts) {
        this.defaultExt = defaultExt;
        this.possibleExts = possibleExts;
        this.description = description;
        this.shortDesc = shortDesc;
    }
    
    public String getDefaultExtension() {
        return defaultExt;
    }
    
    public String getDescription() {
        return description;
        //return MessageFormat.format(Utils._("{0} files"), toString());
    }
    
    public String[] getPossibleExtensions() {
        return possibleExts;
    }
    
    @Override
    public String toString() {
    	if (shortDesc != null) {
    		return shortDesc;
    	} else {
    		return super.toString();
    	}
    }
    
    /**
     * Returns the appropriate HylaFAX file format "code" for the given file format
     * @param format
     * @return
     */
    public String getHylaFAXFormatString() {
        switch (this) {
        case PDF:
            return "pdf";
        case PostScript:
            return "ps";
        case TIFF:
        case TIFF_DITHER:
            return "tiff";
        default:
            return "data";
        }
    }
}