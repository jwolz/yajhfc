/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc;

import gnu.hylafax.Job;

/**
 * @author jonas
 *
 */
public enum FaxResolution {
    EXTENDED(Utils._("Extended (> 196 lpi)"), Job.RESOLUTION_MEDIUM, true),
    HIGH(Utils._("High (196 lpi)"), Job.RESOLUTION_MEDIUM, false),
    LOW(Utils._("Low (98 lpi)"), Job.RESOLUTION_LOW, false)   
    ;
    
    private final String text;
    private final int resolution;
    private final boolean useXVRes;
    
    public String getText() {
        return text;
    }
    
    public int getResolution() {
        return resolution;
    }
    
    public boolean useXVRes() {
        return useXVRes;
    }
    
    @Override
    public String toString() {
        return text;
    }

    private FaxResolution(String text, int resolution, boolean useXVRes) {
        this.text = text;
        this.resolution = resolution;
        this.useXVRes = useXVRes;
    }

}
