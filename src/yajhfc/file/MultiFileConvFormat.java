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

/**
 * @author jonas
 *
 */
public enum MultiFileConvFormat {
    PDF(new PDFMultiFileConverter()),
    PostScript(new PSMultiFileConverter()),
    TIFF(new TIFFMultiFileConverter()),
    TIFF_DITHER(new TIFFDitherMultiFileConverter(), "TIFF (dithered)");
    
    private final MultiFileConverter converter;
    private final String description;

    private MultiFileConvFormat(MultiFileConverter converter, String description) {
        this.converter = converter;
        this.description = description;
    }
    
    private MultiFileConvFormat(MultiFileConverter converter) {
        this(converter, null);
    }

    public FileFormat getFileFormat() {
        return converter.getTargetFormat();
    }
    
    public MultiFileConverter getConverter() {
        return converter;
    }
    
    @Override
    public String toString() {
        return (description == null) ? super.toString() : description;
    }
}
