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
package yajhfc.model.servconn.defimpl;

import java.util.Comparator;

import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;

/**
 * A comparator to compare two FaxJobs using their ID value
 * @author jonas
 *
 */
public class JobIDComparator implements Comparator<FaxJob<? extends FmtItem>> {
    @SuppressWarnings("unchecked")
    public int compare(FaxJob<? extends FmtItem> o1, FaxJob<? extends FmtItem> o2) {
        if (o1 == o2)
            return 0;
        if (o1 == null) // Implicit: o2 != null
            return -1;
        if (o2 == null) // Implicit: o1 != null
            return 1;
        return ((Comparable)o1.getIDValue()).compareTo(o2.getIDValue());
    }
    
    public static final JobIDComparator INSTANCE = new JobIDComparator();
}
