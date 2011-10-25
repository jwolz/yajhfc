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
package yajhfc.print.tableprint;

/**
 * Possible text Alignments
 * 
 * @author jonas
 *
 */
public enum Alignment {
    LEFT,
    CENTER,
    RIGHT;
    
    public double calculateX(double x, double contentWidth, double cellWidth, double spaceX) {
        switch (this) {
        case LEFT:
        default:
            return (x+spaceX);
        case RIGHT:
            return (x+cellWidth-spaceX-contentWidth);
        case CENTER:
            return (x+(cellWidth-contentWidth)/2);
        }
    }
}
