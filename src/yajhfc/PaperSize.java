package yajhfc;
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
 */

import gnu.hylafax.Pagesize;

import java.awt.Dimension;

public enum PaperSize {
    A4("A4", Pagesize.A4),
    A5("A5", Pagesize.A5),
    LETTER("Letter", Pagesize.LETTER),
    LEGAL("Legal", Pagesize.LEGAL)
    ;
    
    private final String desc;
    private final Dimension size;
    
    private PaperSize(String desc, Dimension size) {
        this.desc = desc;
        this.size = size;        
    }
    
    public String toString() {
        return desc;
    }
    
    /**
     * Returns the dimensions of the papersize in mm
     * @return
     */
    public Dimension getSize() {
        return size;
    }
    
    /**
     * Returns the papersize's width in inches
     * @return
     */
    public float getWidthInches() {
        return size.width/25.4f;
    }
    
    /**
     * Returns the papersize's height in inches
     * @return
     */
    public float getHeightInches() {
        return size.height/25.4f;
    }
    
    public String getKey() {
        return name();
    }
}





