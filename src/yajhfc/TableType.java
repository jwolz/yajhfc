/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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

import yajhfc.model.archive.QueueFileFormat;

/**
 * @author jonas
 *
 */
public enum TableType {
    RECEIVED(RecvFormat.class),
    SENT(JobFormat.class),
    SENDING(JobFormat.class),
    ARCHIVE(QueueFileFormat.class);
    
    private final Class<? extends Enum<? extends FmtItem>> fieldsEnumClass;
    
    private TableType(Class<? extends Enum<? extends FmtItem>> fieldsEnumClass) {
        this.fieldsEnumClass = fieldsEnumClass;
    }

    public Class<? extends Enum<? extends FmtItem>> getFieldsEnumClass() {
        return fieldsEnumClass;
    }
    
    /**
     * The number of tables. Equals values().length
     */
    public static final int TABLE_COUNT = values().length;
}
