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
package yajhfc.model.servconn.defimpl;

import java.io.Serializable;

import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJob;
import yajhfc.model.servconn.FaxJobList;

/**
 * A fax job that is serializable
 * @author jonas
 *
 */
public interface SerializableFaxJob<T extends FmtItem> extends FaxJob<T>, Serializable {
   
    /**
     * Used to set the parent after the job has been deserialized
     * @param parent
     */
    public void setParent(FaxJobList<T> parent);

}
