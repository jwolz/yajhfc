package yajhfc.filters;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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
import yajhfc.YajJob;
import yajhfc.YajJobFilter;

public class OrFilter extends AndFilter {

    public boolean jobIsVisible(YajJob job) {
        if (children.size() == 0)
            return true;
        
        boolean retVal = false;
        for (YajJobFilter yjf: children) {
            retVal = retVal || yjf.jobIsVisible(job);
            if (retVal)
                return true;
        }
        return retVal;
    }

}
