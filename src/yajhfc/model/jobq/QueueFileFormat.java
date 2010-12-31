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
package yajhfc.model.jobq;

import static yajhfc.Utils._;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;

import yajhfc.DateKind;
import yajhfc.Utils;
import yajhfc.model.FmtItem;
import yajhfc.model.IconMap;

/**
 * @author jonas
 *
 */
public enum QueueFileFormat implements FmtItem {
        chopthreshold("chopthreshold", Utils._("Page chopping threshold (inches)"), Float.class), 
        commid("commid", Utils._("Communication identifier for last call"), String.class), 
        company("company", _("Company"), _("Destination company name"), String.class), 
        client("client", Utils._("Host that submitted the job"), String.class), 
        cover("cover", Utils._("Continuation coverpage file"), String.class), 
        dataformat("dataformat", Utils._("Data format used to transmit a facsimile"), String.class), 
        desiredbr("desiredbr", Utils._("Desired transmit speed"), Integer.class), 
        desireddf("desireddf", Utils._("Desired data format"), Integer.class), 
        desiredec("desiredec", _("ECM"), Utils._("Desired use of Error Correction Mode (ECM)"), Integer.class), 
        desiredst("desiredst", _("Desired min-scanline time"), Utils._("Desired minimum scanline time"), Integer.class), 
        desiredtl("desiredtl", _("Tagline"),Utils._("Whether or not to use tagline parameter"), Boolean.class), 
        doneop("doneop", _("Job done operation"), Utils._("Operation to perform when job is done"), String.class), 
        external("external", _("Number"), Utils._("Public (external) format of dialstring"), String.class), 
        fax("fax", Utils._("Ready"), Utils._("Document ready for transmission"), String.class), 
        groupid("groupid", _("Group identifier"), Utils._("HylaFAX job group identifier"), Integer.class), 
        jobid("jobid", _("ID"), Utils._("HylaFAX job identifier"), Integer.class), 
        jobtag("jobtag", _("Tag string"), Utils._("Client-specified job tag string"), String.class), 
        jobtype("jobtype",  _("Job type"), Utils._("Job type identification string"), String.class), 
        killtime("killtime", _("Job kill time"), Utils._("Time to give up trying to send job"), Utils.HYLA_UNIX_DATE_FORMAT_GMT, DateKind.DATE_AND_TIME), 
        location("location", _("Location"), Utils._("Destination geographic location"), String.class), 
        mailaddr("mailaddr",  _("Sender e-mail"), Utils._("Email address of sender"), String.class), 
        maxdials("maxdials", _("Maximum # dials"), Utils._("Maximum number of times to dial"), Integer.class), 
        maxtries("maxtries", _("Max. tries"), Utils._("Maximum number of attempts to send job"), Integer.class), 
        minbr("minbr", _("Minimum speed"), Utils._("Minimum required transmit speed"), Integer.class), 
        modem("modem", _("Modem"), Utils._("Outgoing modem to use"), String.class), 
        ndials("ndials", _("# consecutive failed tries"), Utils._("Number of consecutive failed attempts to place call"), Integer.class), 
        notify("notify", _("Notification"), Utils._("Email notification specification"), IconMap.class), 
        npages("npages", _("# pages"), Utils._("Number of pages transmitted"), Integer.class), 
        ntries("ntries", _("Tries (current page)"), Utils._("Number of attempts to send current page"), Integer.class), 
        number("number", _("Specified number"), _("Client-specified dialstring"), String.class), 
        owner("owner", _("Owner"),  _("Job owner"), String.class), 
        page("page", _("Page PIN"), Utils._("PIN in a page operation"), String.class), 
        pagechop("pagechop", _("Page chop handling"), Utils._("Whitespace truncation handling"), Integer.class), 
        pagehandling("pagehandling", _("Page handling"), Utils._("Page analysis information"), String.class), 
        pagelength("pagelength", _("Page length"), _("Page length in mm"), Integer.class), 
        pagewidth("pagewidth", _("Page width"), _("Page width in mm"), Integer.class), 
        passwd("passwd", _("Password"), Utils._("Destination Password"), String.class), 
        poll("poll", Utils._("Polling request"), String.class), 
        priority("priority", _("Priority"), _("Client-specified scheduling priority"), Integer.class), 
        receiver("receiver", _("Receiver"), _("Destination person (receiver)"), String.class), 
        resolution("resolution", _("Resolution"), _("Document resolution in lines/inch"), Integer.class), 
        retrytime("retrytime", _("Retry time"), Utils._("Time to use between job retries"), Integer.class), 
        returned("returned", _("Returned status"), Utils._("Indicates status return value for the job"), Integer.class), 
        schedpri("schedpri", _("Scheduling priority"), _("Current scheduling priority"), Integer.class), 
        sender("sender", _("Sender"), _("Sender's identity"), String.class), 
        signalrate("signalrate", _("Speed"), Utils._("Signalling rate at which a facsimile was sent"), String.class), 
        state("state", Utils._("Job state"), IconMap.class), 
        status("status", _("Status"), _("Job status information from last failure"), String.class), 
        subaddr("subaddr", _("SubAddress"), _("Destination SubAddress"), String.class), 
        tagline("tagline", _("Tagline format"), Utils._("Client-specific tagline format string"), String.class), 
        totdials("totdials", _("Total # dials"), Utils._("Total number of phone calls"), Integer.class), 
        totpages("totpages", _("Pages"), Utils._("Total # pages to transmit"), Integer.class), 
        tottries("tottries", _("Tries"), Utils._("Total number of attempts to send job"), Integer.class), 
        tts("tts", Utils._("Time to send job"), Utils.HYLA_UNIX_DATE_FORMAT_GMT, DateKind.DATE_AND_TIME), 
        useccover("useccover", _("Use continuation cover"), Utils._("Whether or not to use a continuation cover page"), Boolean.class), 
        usexvres("usexvres", _("Use max. vertical resolution"), Utils._("Whether or not to use highest vertical resolution"), Boolean.class),
        state_desc("state", _("State"), _("Job state (long description)"), IconMap.class),
        notify_desc("notify",_("Notification when"), _("E-mail notification handling (long description)"), IconMap.class),
        // the following properties are not officially documented in doneq(5)/sendq(5)
        voice("voice", _("Recipient phone"), _("Recipient's telephone (voice) number"), String.class),
        regarding("regarding", _("Subject"), _("Subject (\"Regarding\") of the fax"), String.class),
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
        
        private QueueFileFormat(String hylaFmt, String description,
                DateFormat hylaDateFormat, DateKind displayDateFormat) {
            this(hylaFmt, description, description, Date.class, hylaDateFormat, displayDateFormat);
        }
        
        private QueueFileFormat(String hylaFmt, String description, String longDesc,
                DateFormat hylaDateFormat, DateKind displayDateFormat) {
            this(hylaFmt, description, longDesc, Date.class, hylaDateFormat, displayDateFormat);
        }
        
        private QueueFileFormat(String hylaFmt, String description,
                String longDescription, Class<?> dataType) {
            this(hylaFmt, description, longDescription, dataType, null, null);
        }
        
        private QueueFileFormat(String hylaFmt, String description, Class<?> dataType) {
            this(hylaFmt, description, description, dataType, null, null);
        }
        
        private QueueFileFormat(String hylaFmt, String description,
                String longDescription, Class<?> dataType,
                DateFormat hylaDateFormat, DateKind displayDateFormat) {
            this.hylaFmt = hylaFmt;
            this.description = description;
            this.longDescription = longDescription;
            this.dataType = dataType;
            this.hylaDateFormat = hylaDateFormat;
            this.displayDateFormat = displayDateFormat;
        }

        private static final QueueFileFormat[] requiredFormats = {
            QueueFileFormat.owner,
            QueueFileFormat.state,
            QueueFileFormat.commid
        };
        public static QueueFileFormat[] getRequiredFormats() {
            return requiredFormats;
        }
}
