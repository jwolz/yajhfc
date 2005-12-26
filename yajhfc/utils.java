package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import gnu.hylafax.HylaFAXClientProtocol;
import gnu.hylafax.Job;
import gnu.hylafax.Pagesize;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

public class utils {
    public static final String AppName = "Yet Another Java HylaFAX Client (YajHFC)";
    public static final String AppShortName = "YajHFC";
    public static final String AppCopyright = "Copyright © 2005 by Jonas Wolz";
    public static final String AppVersion = "0.2.2";
    public static final String AuthorEMail = "Jonas Wolz &lt;jwolz@freenet.de&gt;";
    public static final String HomepageURL = "http://www.yajhfc.de.vu/"; 
    
    private static FaxOptions theoptions = null;
    private static ResourceBundle msgs = null;
    private static boolean TriedMsgLoad = false;
    private static String confdir = null;
    
    public static final FmtItem jobfmt_JobID =
        new FmtItem("j", _("ID"), _("Job identifier"), Integer.class);
    
    public static final FmtItem[] jobfmts =
    { 
            new FmtItem("A", _("SubAddress"), _("Destination SubAddress")), 
            new FmtItem("B", _("Password"), _("Destination Password")), 
            new FmtItem("C", _("Company"), _("Destination company name")), 
            new FmtItem("D", _("Dials: toal/max."), _("Total # dials/maximum # dials")), 
            new FmtItem("E", _("Speed"), _("Desired signalling rate"), Integer.class), 
            new FmtItem("F", _("Tagline format"), _("Client-specific tagline format string")), 
            new FmtItem("G", _("Desired min-scanline time")), 
            new FmtItem("H", _("Desired data format")), 
            new FmtItem("I", _("Priority"), _("Client-specified scheduling priority"), Integer.class), 
            new FmtItem("J", _("Tag string"), _("Client-specified job tag string")), 
            new FmtItem("K", _("Use ECM?"), _("Desired use of ECM (one-character symbol)")), 
            new FmtItem("L", _("Location"), _("Destination geographic location")), 
            new FmtItem("M", _("Sender e-mail"), _("Notification e-mail address")), 
            new FmtItem("N", _("Private Tagline?"), _("Desired use of private tagline (one-character symbol)")), 
            new FmtItem("O", _("Use continuation cover"), _("Whether to use continuation cover page (one-character symbol)")), 
            new FmtItem("P", _("Pages done/total"), _("# pages transmitted/total # pages to transmit")), 
            new FmtItem("Q", _("Minimal signalling rate"), _("Client-specified minimum acceptable signalling rate"), Integer.class), 
            new FmtItem("R", _("Receiver"), _("Destination person (receiver)")), 
            new FmtItem("S", _("Sender"), _("Sender's identity")), 
            new FmtItem("T", _("Tries: done/max."), _("Total # tries/maximum # tries")), 
            new FmtItem("U", _("Page chopping threshold (inches)"), Float.class), 
            new FmtItem("V", _("Job done operation")), 
            new FmtItem("W", _("Communication identifier")), 
            new FmtItem("X", _("Job type"), _("Job type (one-character symbol)")), 
            new FmtItem("Y", _("Scheduled time"), _("Scheduled date and time"), new HylaDateField("yyyy/MM/dd HH.mm.ss", _("dd/MM/yyyy HH:mm:ss"))), 
            new FmtItem("Z", _("Scheduled time in seconds since the UNIX epoch")), 
            new FmtItem("a", _("Job state"), _("Job state (one-character symbol)")), 
            new FmtItem("b", _("# consecutive failed tries"), Integer.class), 
            new FmtItem("c", _("Client machine name")), 
            new FmtItem("d", _("Total # dials"), Integer.class), 
            new FmtItem("e", _("Number"), _("Public (external) format of dialstring")), 
            new FmtItem("f", _("# consecutive failed dials")), 
            new FmtItem("g", _("Group identifier")), 
            new FmtItem("h", _("Page chop handling"), _("Page chop handling (one-character symbol)")), 
            new FmtItem("i", _("Scheduling priority"), _("Current scheduling priority")), 
            jobfmt_JobID, 
            new FmtItem("k", _("Job kill time")), 
            new FmtItem("l", _("Page length"), _("Page length in mm"), Integer.class), 
            new FmtItem("m", _("Modem"), _("Assigned modem")), 
            new FmtItem("n", _("Notification"), _("E-mail notification handling (one-character symbol)")), 
            new FmtItem("o", _("Owner"),  _("Job owner")), 
            new FmtItem("p", _("# pages"), _("# pages transmitted"), Integer.class), 
            new FmtItem("q", _("Retry time"), _("Job retry time (MM::SS)")/*, new HylaDateField("mm:ss", _("mm:ss"))*/), 
            new FmtItem("r", _("Resolution"), _("Document resolution in lines/inch"), Integer.class), 
            new FmtItem("s", _("Status"), _("Job status information from last failure")), 
            new FmtItem("t", _("Tries"), _("Total # tries attempted"), Integer.class), 
            new FmtItem("u", _("Max. tries"), _("Maximum # tries"), Integer.class), 
            new FmtItem("v", _("Specified number"), _("Client-specified dialstring")), 
            new FmtItem("w", _("Page width"), _("Page width in mm"), Integer.class), 
            new FmtItem("x", _("Maximum # dials"), Integer.class), 
            new FmtItem("y", _("Pages"), _("Total # pages to transmit"), Integer.class), 
            new FmtItem("z", _("Time to send job"))
    };
    
    public static final FmtItem recvfmt_FileName 
        = new FmtItem("f", _("Filename"), _("Document filename (relative to the recvq directory)"));
    
    public static final FmtItem[] recvfmts = {
            new FmtItem("Y", _("Time/Date"),  _("Extended representation of the time when the receive happened"), new HylaDateField("yyyy:MM:dd HH:mm:ss", _("dd/MM/yyyy HH:mm:ss"))), 
            new FmtItem("a", _("SubAddress"), _("SubAddress received from sender (if any)")), 
            new FmtItem("b", _("Speed"), _("Signalling rate used during receive"), Integer.class), 
            new FmtItem("d", _("Format"), _("Data format used during receive")), 
            new FmtItem("e", _("Error description"), _("Error description if an error occurred during receive")), 
            //new FmtItem("f", "Document filename (relative to the recvq directory)"), 
            recvfmt_FileName,
            new FmtItem("h", _("Time to receive"), _("Time spent receiving document (HH:MM:SS)"), new HylaDateField("m:ss", _("mm:ss"))), 
            new FmtItem("i", _("CIDName"), _("CIDName value for received fax")), 
            new FmtItem("j", _("CIDNumber"), _("CIDNumber value for received fax")), 
            new FmtItem("l", _("Page length"), _("Page length in mm"), Integer.class), 
            new FmtItem("m", _("Fax Protection"), _("Fax-style protection mode string (``-rwxrwx'')")), 
            new FmtItem("n", _("File size"), _("File size (number of bytes)"), Integer.class), 
            new FmtItem("o", _("Owner"), _("File owner")), 
            new FmtItem("p", _("Pages"), _("Number of pages in document"), Integer.class), 
            new FmtItem("q", _("Protection"), _("UNIX-style protection flags")), 
            new FmtItem("r", _("Resolution"), _("Resolution of received data"), Integer.class), 
            new FmtItem("s", _("Sender"), _("Sender identity (TSI)")), 
            new FmtItem("t", _("Date"), _("Compact representation of the time when the receive happened"), new HylaDateField("ddMMMyy", _("dd/MM/yyyy"))), 
            new FmtItem("w", _("Page width"), _("Page width in mm"), Integer.class), 
            new FmtItem("z", _("In progress"), _("A ``*'' if receive is going on; otherwise `` '' (space)"), Boolean.class)
    };
    
    public static final PaperSize[] papersizes = {
            new PaperSize("A4", Pagesize.A4),
            new PaperSize("A5", Pagesize.A5),
            new PaperSize("Letter", Pagesize.LETTER),
            new PaperSize("Legal", Pagesize.LEGAL)
    };
    
    public static final FaxIntProperty[] resolutions = {
            new FaxIntProperty(_("High (196 lpi)"), Job.RESOLUTION_MEDIUM),
            new FaxIntProperty(_("Low (98 lpi)"), Job.RESOLUTION_LOW)      
    };
    
    public static final FaxStringProperty[] notifications = {
            new FaxStringProperty(_("Never"), Job.NOTIFY_NONE),
            new FaxStringProperty(_("Delivered"), Job.NOTIFY_DONE),
            new FaxStringProperty(_("Requeued"), Job.NOTIFY_REQUEUE),
            new FaxStringProperty(_("Delivered or requeued"), Job.NOTIFY_ALL)
    };
    
    public static final FaxStringProperty[] timezones = {
        new FaxStringProperty(_("Local timezone"), HylaFAXClientProtocol.TZONE_LOCAL),
        new FaxStringProperty(_("GMT"), HylaFAXClientProtocol.TZONE_GMT)
    };
    

    public static final FmtItem[] requiredSendingFmts = {
        jobfmt_JobID
    };


    public static final FmtItem[] requiredSentFmts = { 
        jobfmt_JobID
    };


    public static final FmtItem[] requiredRecvFmts = {
      recvfmt_FileName  
    };
    
    public static final int NEWFAX_NOACTION = 0;
    public static final int NEWFAX_BEEP = 1;
    public static final int NEWFAX_TOFRONT = 2;
    public static final int NEWFAX_BOTH = NEWFAX_BEEP | NEWFAX_TOFRONT;
    
    public static final FaxIntProperty[] newFaxActions = {
        new FaxIntProperty(_("No action"), NEWFAX_NOACTION),
        new FaxIntProperty(_("Beep"), NEWFAX_BEEP),
        new FaxIntProperty(_("Bring to front"), NEWFAX_TOFRONT),
        new FaxIntProperty(_("Beep & bring to front"), NEWFAX_BOTH)
    };
    
    public static String VectorToString(Vector v, String delim) {
        StringBuilder s = new StringBuilder();
        Enumeration e = v.elements();
        while (e.hasMoreElements()) {
            s.append(e.nextElement()).append(delim);
        }
        s.delete(s.length() - delim.length(), s.length());
        return s.toString();
    }    
    
    public static FaxOptions getFaxOptions() {
        if (theoptions == null) {
            theoptions = new FaxOptions();
            theoptions.loadFromFile(FaxOptions.getDefaultConfigFileName());
        }
        return theoptions;
    }
    
    public static Locale getLocale() {
        return Locale.getDefault();
    }
    
    public static String _(String key) {
        if (msgs == null)
            if (TriedMsgLoad)
                return key;
            else {
                LoadMessages();
                return _(key);
            }                
        else
            try {
                return msgs.getString(key);
            } catch (Exception e) {
                return key;
            }
    }
    
    private static void LoadMessages() {
        TriedMsgLoad = true;
        try {
            msgs = ResourceBundle.getBundle("yajhfc.i18n.Messages", getLocale());
        } catch (Exception e) {
            msgs = null;
        }
        
    }
    
    public static MyManualMapObject findInArray(MyManualMapObject[] array, Object key) {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(key)) 
                return array[i];
        return null;
    }
    
    public static String minutesToHylaTime(int mins) {
        String result;
        DecimalFormat fmt = new DecimalFormat("00");
        result = fmt.format(mins % 60);
        mins /= 60;
        result = fmt.format(mins % 60) + result;
        mins /= 60;
        result = fmt.format(mins % 60) + result;
        return result;
    }
 
    public static void addUniqueToVec(Vector<FmtItem> vec, FmtItem[] arr) {
        for (int i = 0; i < arr.length; i++)
            if (!vec.contains(arr[i]))
                vec.add(arr[i]);
    }

    public static ImageIcon loadIcon(String name) {
        URL imgURL = utils.class.getResource("/toolbarButtonGraphics/" + name + "16.gif");
        if (imgURL != null)
            return new ImageIcon(imgURL);
        else
            return null;
    }
    
    public static String getConfigDir() {
        if (confdir == null) {
            confdir = System.getProperty("user.home") + File.separator + ".yajhfc" + File.separator;
            File dir = new File(confdir);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
        return confdir;
    }
}


