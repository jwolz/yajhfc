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

import static yajhfc.Utils._;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;

/**
 * @author jonas
 *
 */
public enum JobFormat implements FmtItem {
    /**
     * SubAddress
     */
    A("A", _("SubAddress"), _("Destination SubAddress")), 
    /**
     * Password
     */
    B("B", _("Password"), _("Destination Password")), 
    /**
     * Company
     */
    C("C", _("Company"), _("Destination company name")), 
    /**
     * Dials: total/max.
     */
    D("D", _("Dials: total/max."), _("Total # dials/maximum # dials")), 
    /**
     * Speed
     */
    E("E", _("Speed"), _("Desired signalling rate"), Integer.class), 
    /**
     * Tagline format
     */
    F("F", _("Tagline format"), _("Client-specific tagline format string")), 
    /**
     * Desired min-scanline time
     */
    G("G", _("Desired min-scanline time"), Utils._("Desired minimum scanline time"), Integer.class), 
    /**
     * Desired data format
     */
    H("H", _("Desired data format")), 
    /**
     * Priority
     */
    I("I", _("Priority"), _("Client-specified scheduling priority"), Integer.class), 
    /**
     * Tag string
     */
    J("J", _("Tag string"), _("Client-specified job tag string")), 
    /**
     * Use ECM?
     */
    K("K", _("Use ECM?"), _("Desired use of ECM (one-character symbol)")), 
    /**
     * Location
     */
    L("L", _("Location"), _("Destination geographic location")), 
    /**
     * Sender e-mail
     */
    M("M", _("Sender e-mail"), _("Notification e-mail address")), 
    /**
     * Private Tagline?
     */
    N("N", _("Private Tagline?"), _("Desired use of private tagline (one-character symbol)")), 
    /**
     * Use continuation cover
     */
    O("O", _("Use continuation cover"), _("Whether to use continuation cover page (one-character symbol)")), 
    /**
     * Pages done/total
     */
    P("P", _("Pages done/total"), _("# pages transmitted/total # pages to transmit")), 
    /**
     * Minimal signalling rate
     */
    Q("Q", _("Minimal signalling rate"), _("Client-specified minimum acceptable signalling rate"), Integer.class), 
    /**
     * Receiver
     */
    R("R", _("Receiver"), _("Destination person (receiver)")), 
    /**
     * Sender
     */
    S("S", _("Sender"), _("Sender's identity")), 
    /**
     * Tries: done/max.
     */
    T("T", _("Tries: done/max."), _("Total # tries/maximum # tries")), 
    /**
     * Page chopping threshold (inches)
     */
    U("U", _("Page chopping threshold (inches)"), Float.class), 
    /**
     * Job done operation
     */
    V("V", _("Job done operation"), Utils._("Operation to perform when job is done")), 
    /**
     * Communication identifier
     */
    W("W", _("Communication identifier")), 
    /**
     * Job type
     */
    X("X", _("Job type"), _("Job type (one-character symbol)")), 
    /**
     * Scheduled time
     */
    Y("Y", _("Scheduled time"), _("Scheduled date and time"), Utils.HYLA_LONG_DATE_FORMAT, DateKind.DATE_AND_TIME), 
    /**
     * Scheduled time in seconds since the UNIX epoch
     */
    Z("Z", _("Scheduled time (UNIX)"), _("Scheduled time in seconds since the UNIX epoch"), Utils.HYLA_UNIX_DATE_FORMAT, DateKind.DATE_AND_TIME), 
    /**
     * Job state
     */
    a("a", _("Job state"), _("Job state (one-character symbol)"), IconMap.class),
    /**
     * # consecutive failed tries
     */
    b("b", _("# consecutive failed tries"), Utils._("Number of consecutive failed attempts to place call"), Integer.class), 
    /**
     * Client machine name
     */
    c("c", _("Client machine name")), 
    /**
     * Total # dials
     */
    d("d", _("Total # dials"), Utils._("Total number of phone calls"), Integer.class), 
    /**
     * Number
     */
    e("e", _("Number"), _("Public (external) format of dialstring")), 
    /**
     * # consecutive failed dials
     */
    f("f", _("# consecutive failed dials")), 
    /**
     * Group identifier
     */
    g("g", _("Group identifier")), 
    /**
     * Page chop handling
     */
    h("h", _("Page chop handling"), _("Whitespace truncation handling")), 
    /**
     * Scheduling priority
     */
    i("i", _("Scheduling priority"), _("Current scheduling priority")), 
    /**
     * ID
     */
    j("j", _("ID"), _("Job identifier"), Integer.class),
    /**
     * Job kill time
     */
    k("k", _("Job kill time"), _("Time to give up trying to send job")), // Utils.HYLA_TIME_ONLY_FORMAT, DateKind.TIME_ONLY), 
    /**
     * Page length
     */
    l("l", _("Page length"), _("Page length in mm"), Integer.class), 
    /**
     * Modem
     */
    m("m", _("Modem"), _("Assigned modem")), 
    /**
     * Notification
     */
    n("n", _("Notification"), _("E-mail notification handling (one-character symbol)"), IconMap.class), 
    /**
     * Owner
     */
    o("o", _("Owner"),  _("Job owner")),
    /**
     * # pages
     */
    p("p", _("# pages"), _("Number of pages transmitted"), Integer.class), 
    /**
     * Retry time
     */
    q("q", _("Retry time"), _("Job retry time"), Utils.HYLA_TIME_ONLY_FORMAT, DateKind.DURATION), 
    /**
     * Resolution
     */
    r("r", _("Resolution"), _("Document resolution in lines/inch"), Integer.class), 
    /**
     * Status
     */
    s("s", _("Status"), _("Job status information from last failure")),
    /**
     * Tries
     */
    t("t", _("Tries"), _("Total number of attempts to send job"), Integer.class), 
    /**
     * Max. tries
     */
    u("u", _("Max. tries"), _("Maximum number of attempts to send job"), Integer.class), 
    /**
     * Specified number
     */
    v("v", _("Specified number"), _("Client-specified dialstring")), 
    /**
     * Page width
     */
    w("w", _("Page width"), _("Page width in mm"), Integer.class), 
    /**
     * Maximum # dials
     */
    x("x", _("Maximum # dials"), _("Maximum number of times to dial"), Integer.class), 
    /**
     * Pages
     */
    y("y", _("Pages"), _("Total # pages to transmit"), Integer.class), 
    /**
     * Time to send job
     */
    z("z", _("Time to send job")), // Utils.HYLA_TIME_ONLY_FORMAT, DateKind.TIME_ONLY),
    /**
     * Job state (icon only)
     */
    a_desc("a", _("State"), _("Job state (long description)"), IconMap.class),
    /**
     * Notification (icon only)
     */
    n_desc("n", _("Notification when"), _("E-mail notification handling (long description)"), IconMap.class),
    ;
    
    private final String description;
    private final String hylaFmt;
    private final String longDescription;
    private final Class<?> dataType;
    private final DateFormat hylaDateFormat;
    private final DateKind displayDateFormat;
    
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
        return DateKind.getInstanceFromKind(displayDateFormat);
    }
    public Format getFormat() {
        if (dataType == Date.class) {
            return getDisplayDateFormat();
        } else {
            return null;
        }
    }
    
    private JobFormat(String hylaFmt, String description) {
        this(hylaFmt, description, description);
    }
    
    private JobFormat(String hylaFmt, String description,
            String longDescription) {
        this(hylaFmt, description, longDescription, String.class, null, null);
    }
    
    private JobFormat(String hylaFmt, String description, DateFormat hylaDateFormat, DateKind displayDateFormat) {
        this(hylaFmt, description, description, Date.class, hylaDateFormat, displayDateFormat);
    }
    
    private JobFormat(String hylaFmt, String description,
            String longDescription, DateFormat hylaDateFormat, DateKind displayDateFormat) {
        this(hylaFmt, description, longDescription, Date.class, hylaDateFormat, displayDateFormat);
    }
    
    private JobFormat(String hylaFmt, String description,
            String longDescription, Class<?> dataType) {
        this(hylaFmt, description, longDescription, dataType, null, null);
    }
    
    private JobFormat(String hylaFmt, String description, Class<?> dataType) {
        this(hylaFmt, description, description, dataType, null, null);
    }
    
    private JobFormat(String hylaFmt, String description,
            String longDescription, Class<?> dataType,
            DateFormat hylaDateFormat, DateKind displayDateFormat) {
        this.hylaFmt = hylaFmt;
        this.description = description;
        this.longDescription = longDescription;
        this.dataType = dataType;
        this.hylaDateFormat = hylaDateFormat;
        this.displayDateFormat = displayDateFormat;
    }

    private static final JobFormat[] requiredFormats = {
        JobFormat.j,
        JobFormat.o,
        JobFormat.a
        
    };
    public static JobFormat[] getRequiredFormats() {
        return requiredFormats;
    }
}
