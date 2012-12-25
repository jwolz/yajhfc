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
package yajhfc.file;

import java.util.Collection;

import javax.swing.filechooser.FileFilter;

import yajhfc.Utils;
import yajhfc.util.ExampleFileFilter;

public enum FileFormat {
    PostScript(Utils._("Postscript documents"), "ps"),
    PDF(Utils._("PDF documents"), "pdf"),
    PCL(Utils._("PCL files"), "pcl"),
    JPEG(Utils._("JPEG pictures"), "jpeg", "jpg"),
    PNG(Utils._("PNG pictures"),"png"),
    GIF(Utils._("GIF pictures"),"gif"),
    TIFF(Utils._("TIFF pictures"),"tiff", "tif"),
    PlainText(Utils._("Text files"),"txt"),
    XML(Utils._("XML documents"), "xml"),
    FOP(Utils._("XSL:FO documents"), "fo", "xml", "fop"),
    ODT(Utils._("OpenDocument text documents"), "odt"),
    HTML(Utils._("HTML documents"), "html", "htm"),
    RTF(Utils._("RTF documents"), "rtf"),
    CSV(Utils._("CSV files"), "csv", "txt"),
    PJL(Utils._("PJL printer job"), "pjl"),
    Any(Utils._("Any files"), "dat", ExampleFileFilter.ANY_EXTENSION),
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
            return "tiff";
        default:
            return "data";
        }
    }
    
    public FileFilter createFileFilter() {
        return new ExampleFileFilter(this.getPossibleExtensions(), this.getDescription());
    }
    
    public static FileFilter[] createFileFiltersFromFormats(Collection<FileFormat> formats) {
        ExampleFileFilter allSupported = new ExampleFileFilter((String)null, Utils._("All supported file formats"));
        allSupported.setExtensionListInDescription(false);
        
        FileFilter[] filters = new FileFilter[formats.size() + 1];
        filters[0] = allSupported;
        
        int i = 0;
        for (FileFormat ff : formats) {
            for (String ext : ff.getPossibleExtensions()) {
                allSupported.addExtension(ext);
            }
            filters[++i] = ff.createFileFilter();
        }
        
        return filters;
    }
    
}