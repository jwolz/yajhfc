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
package yajhfc.model;

import static yajhfc.Utils._;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;

import yajhfc.DateKind;
import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public enum RecvFormat implements FmtItem {
    /**
     * Time/Date
     */
    Y("Y", _("Time/Date"),  _("Extended representation of the time when the receive happened"), Utils.HYLA_LONG_DATE_FORMAT, DateKind.DATE_AND_TIME), 
    /**
     * SubAddress
     */
    a("a", _("SubAddress"), _("SubAddress received from sender (if any)")), 
    /**
     * Speed
     */
    b("b", _("Speed"), _("Signalling rate used during receive"), Integer.class), 
    /**
     * Format
     */
    d("d", _("Format"), _("Data format used during receive")), 
    /**
     * Error description
     */
    e("e", _("Error description"), _("Error description if an error occurred during receive")),
    /**
     * Filename
     */
    f("f", _("Filename"), _("Document filename (relative to the recvq directory)")),
    /**
     * Time to receive
     */
    h("h", _("Time to receive"), _("Time spent receiving document (HH:MM:SS)"), Utils.HYLA_DURATION_FORMAT, DateKind.DURATION), 
    /**
     * CIDName
     */
    i("i", _("CIDName"), _("CIDName value for received fax")), 
    /**
     * CIDNumber
     */
    j("j", _("CIDNumber"), _("CIDNumber value for received fax")), 
    /**
     * Page length
     */
    l("l", _("Page length"), _("Page length in mm"), Integer.class), 
    /**
     * Fax Protection
     */
    m("m", _("Fax Protection"), _("Fax-style protection mode string (``-rwxrwx'')")), 
    /**
     * File size
     */
    n("n", _("File size"), _("File size (number of bytes)"), Integer.class), 
    /**
     * Owner
     */
    o("o", _("Owner"), _("File owner")),
    /**
     * Pages
     */
    p("p", _("Pages"), _("Number of pages in document"), Integer.class), 
    /**
     * Protection
     */
    //q("q", _("Protection"), _("UNIX-style protection flags")), 
    /**
     * Resolution
     */
    r("r", _("Resolution"), _("Resolution of received data"), Integer.class), 
    /**
     * Sender
     */
    s("s", _("Sender"), _("Sender identity (TSI)")), 
    /**
     * Date
     */
    t("t", _("Date"), _("Compact representation of the time when the receive happened")), 
    /**
     * Page width
     */
    w("w", _("Page width"), _("Page width in mm"), Integer.class), 
    /**
     * In progress
     */
    z("z", _("In progress"), _("A ``*'' if receive is going on; otherwise `` '' (space)"), Boolean.class),
    /**
     * Time in seconds since the UNIX epoch (undocumented)
     */
    Z("Z", _("Time/Date (UNIX)"), _("Time in seconds since the UNIX epoch"), Utils.HYLA_UNIX_DATE_FORMAT, DateKind.DATE_AND_TIME),
    
    /**
     * Virtual column: read/unread state
     */
    virt_read(null, _("Read"), _("Has the fax been read by a YajHFC user?"), Boolean.class, null, null, false, VirtualColumnType.READ),
    /**
     * Virtual column: user comment
     */
    virt_comment(null, _("User comment"), _("Comment added by a YajHFC user"), String.class, null, null, false, VirtualColumnType.USER_COMMENT),
    ;
    private final String description;
    private final String hylaFmt;
    private final String longDescription;
    private final Class<?> dataType;
    private final DateFormat hylaDateFormat;
    private final DateKind displayDateFormat;
    private final boolean readOnly;
    private final VirtualColumnType virtualColumnType;
    
    public String getDescription() {
        return description;
    }
    public String getHylaFmt() {
        return hylaFmt;
    }
    public String getLongDescription() {
        return longDescription;
    }
    public Class<?> getDataType() {
        return dataType;
    }
    public DateFormat getHylaDateFormat() {
        return hylaDateFormat;
    }
    public DateFormat getDisplayDateFormat() {
        if (displayDateFormat == null)
            return null;
        
        return DateKind.getInstanceFromKind(displayDateFormat);
    }
    public Format getFormat() {
        if (dataType == Date.class) {
            return getDisplayDateFormat();
        } else {
            return null;
        }
    }
    public boolean isReadOnly() {
        return readOnly;
    }
    public VirtualColumnType getVirtualColumnType() {
        return virtualColumnType;
    }
    
    private RecvFormat(String hylaFmt, String description) {
        this(hylaFmt, description, description);
    }
    
    private RecvFormat(String hylaFmt, String description,
            String longDescription) {
        this(hylaFmt, description, longDescription, String.class, null, null);
    }
    
    private RecvFormat(String hylaFmt, String description,
            String longDescription, DateFormat hylaDateFormat, DateKind displayDateFormat) {
        this(hylaFmt, description, longDescription, Date.class, hylaDateFormat, displayDateFormat);
    }
    
    private RecvFormat(String hylaFmt, String description,
            String longDescription, Class<?> dataType) {
        this(hylaFmt, description, longDescription, dataType, null, null);
    }
    
    private RecvFormat(String hylaFmt, String description, Class<?> dataType) {
        this(hylaFmt, description, description, dataType, null, null);
    }
    
    private RecvFormat(String hylaFmt, String description,
            String longDescription, Class<?> dataType,
            DateFormat hylaDateFormat, DateKind displayDateFormat) {
        this(hylaFmt, description, longDescription, dataType, hylaDateFormat, displayDateFormat, true, VirtualColumnType.NONE);
    }
    
    private RecvFormat(String hylaFmt, String description,
            String longDescription, Class<?> dataType,
            DateFormat hylaDateFormat, DateKind displayDateFormat, boolean readOnly, VirtualColumnType virtualColumnType) {
        this.hylaFmt = hylaFmt;
        this.description = description;
        this.longDescription = longDescription;
        this.dataType = dataType;
        this.hylaDateFormat = hylaDateFormat;
        this.displayDateFormat = displayDateFormat;
        this.readOnly = readOnly;
        this.virtualColumnType = virtualColumnType;
    }

    private static final RecvFormat[] requiredFormats = {
        RecvFormat.f,
        RecvFormat.o,
        RecvFormat.z,
        RecvFormat.e,
        RecvFormat.virt_comment,
        RecvFormat.virt_read
    };
    public static RecvFormat[] getRequiredFormats() {
        return requiredFormats;
    }
}
