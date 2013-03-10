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

import java.awt.Font;
import java.util.Map;

import javax.print.DocFlavor;

/**
 * @author jonas
 *
 */
public class DefaultConvertersSource extends FileConverterSource {

    /* (non-Javadoc)
     * @see yajhfc.file.FileConverterSource#addFileConvertersTo(java.util.Map)
     */
    @Override
    public void addFileConvertersTo(Map<FileFormat, FileConverter> converters) {
        converters.put(FileFormat.PostScript, FileConverter.IDENTITY_CONVERTER);
        converters.put(FileFormat.PDF, FileConverter.IDENTITY_CONVERTER);
        converters.put(FileFormat.TIFF, new TIFFLibConverter());
        
        converters.put(FileFormat.PNG, new PrintServiceFileConverter(DocFlavor.URL.PNG));
        converters.put(FileFormat.GIF, new PrintServiceFileConverter(DocFlavor.URL.GIF));
        converters.put(FileFormat.JPEG, new PrintServiceFileConverter(DocFlavor.URL.JPEG));
        
        converters.put(FileFormat.HTML, EditorPaneFileConverter.HTML_CONVERTER);
        // Doesn't work very well
        //converters.put(FileFormat.RTF, new EditorPaneFileConverter("text/rtf"));
        
        converters.put(FileFormat.PlainText, new EditorPaneFileConverter("text/plain", new Font("Courier", Font.PLAIN, 10)));
    }

    @Override
    public int priority() {
        return -10000;
    }
}
