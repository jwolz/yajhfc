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
    public static final String AppName = "Yet Another Java Hylafax Client (YajHFC)";
    public static final String AppShortName = "YajHFC";
    public static final String AppCopyright = "Copyright © 2005 by Jonas Wolz";
    public static final String AppVersion = "0.2.1";
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

abstract class MyManualMapObject {
    public abstract Object getKey();
    
    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MyManualMapObject)
            return getKey().equals(((MyManualMapObject)obj).getKey());
        else if (obj.getClass().isInstance(getKey()))
            return getKey().equals(obj);
        else if (obj instanceof String) 
            return getKey().equals(stringToKey((String)obj));
        else
            return false;
    }
    
    /**
     * Returns strKey converted into the data format of strKey. 
     * Must be overridden if the Object returned by getKey() is not a String!
     * 
     * @param strKey
     * @return
     */
    public Object stringToKey(String strKey) {
        return strKey;
    }
}

class FmtItem extends MyManualMapObject {
    public String fmt;
    public String longdesc;
    public String desc;
    public Class<?> dataClass;
    public HylaDateField dateFormat = null;
    
    public String toString() {
        return "%" + fmt;
    }
     
    public FmtItem(String fmt, String desc) {
        this(fmt, desc, desc);
    }
    
    public FmtItem(String fmt, String desc, Class<?> dataClass) {
        this(fmt, desc, desc, dataClass);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, HylaDateField dateFormat) {
        this(fmt, desc, longdesc, Date.class);
        this.dateFormat = dateFormat;
    }
    
    
    public FmtItem(String fmt, String desc, String longdesc) {
        this(fmt, desc, longdesc, String.class);
    }
    
    public FmtItem(String fmt, String desc, String longdesc, Class<?> dataClass) {
        this.fmt = fmt;
        this.desc = desc;
        this.longdesc = longdesc;
        this.dataClass = dataClass;
    }
    
    @Override
    public Object getKey() {
        return fmt;
    }
}

class FaxOptions {
    public FaxStringProperty tzone = null;
    public String host;
    public int port;
    public boolean pasv;
    public String user;
    public String pass;
    
    public Vector<FmtItem> recvfmt;
    //public int recvFileCol;
    public Vector<FmtItem> sentfmt;
    public Vector<FmtItem> sendingfmt;
    
    public String faxViewer;
    public String psViewer;
    
    public String notifyAddress;
    public FaxStringProperty notifyWhen = null;
    public FaxIntProperty resolution = null;
    public PaperSize paperSize = null;
    
    public int maxTry; 
    public int maxDial;
    
    public Rectangle mainWinBounds, phoneWinBounds;
    public Point sendWinPos, optWinPos;
    public String recvColState, sentColState, sendingColState/*, recvReadState*/;
    public int mainwinLastTab;
    
    public int statusUpdateInterval, tableUpdateInterval;
    
    public String lastPhonebook;
    
    public String FromFaxNumber;
    public String FromVoiceNumber;
    public String FromName;
    public String FromLocation;
    public String FromCompany;
    public String CustomCover;
    public boolean useCover, useCustomCover;
    
    public FaxIntProperty newFaxAction = utils.newFaxActions[3];
    
    public FaxOptions() {
        this.host = "";
        this.port = 4559;
        this.user = System.getProperty("user.name");
        this.pass = "";
        this.pasv = true;
        this.tzone = utils.timezones[0];
        
        this.recvfmt = new Vector<FmtItem>();
        this.recvfmt.add(utils.recvfmts[0]);  // Y
        this.recvfmt.add(utils.recvfmts[16]); // s
        this.recvfmt.add(utils.recvfmts[4]);  // e
        this.recvfmt.add(utils.recvfmts[6]);  // h
        this.recvfmt.add(utils.recvfmts[13]); // p
        this.recvfmt.add(utils.recvfmt_FileName);
        //this.recvFileCol = 2;
        this.recvColState = String.valueOf(this.recvfmt.indexOf(utils.recvfmt_FileName) + 1); // 0 means: no sorting
        
        this.sentfmt = new Vector<FmtItem>();
        this.sentfmt.add(utils.jobfmts[40]); //o
        this.sentfmt.add(utils.jobfmts[30]); //e 
        this.sentfmt.add(utils.jobfmts[44]);       
        this.sentfmt.add(utils.jobfmts[45]);
        this.sentfmt.add(utils.jobfmt_JobID);
        
        this.sendingfmt = this.sentfmt;
        this.sentColState = this.sendingColState = "";
        
        String sysname = System.getProperty("os.name");
        if (sysname.startsWith("Windows")) {
            this.psViewer = "gsview32.exe";
            if (sysname.indexOf("XP") >= 0) 
                this.faxViewer = "rundll32.exe shimgvw.dll,ImageView_Fullscreen %s";
            else
                this.faxViewer = "kodakimg.exe";
        } else {
            this.faxViewer = "/usr/bin/kfax %s";
            this.psViewer = "/usr/bin/gv %s";
        }
        
        this.resolution = utils.resolutions[0];
        this.paperSize = utils.papersizes[0];
        this.notifyWhen = utils.notifications[2];
        this.notifyAddress = this.user +  "@localhost"; //+ this.host;
        this.maxTry = 6;
        this.maxDial = 12;  
        
        mainWinBounds = null;
        optWinPos = null;
        sendWinPos = null;
        phoneWinBounds = null;
        mainwinLastTab = 0;
        
        statusUpdateInterval = 1000;
        tableUpdateInterval = 5000;
        //recvReadState = "";
        
        lastPhonebook = "";
        
        FromFaxNumber = "";
        FromVoiceNumber = "";
        FromName = user;
        FromLocation = "";
        FromCompany = "";
        
        useCover = false;
        useCustomCover = false;
        CustomCover = "";
    }
    
    
    private static final String sep = "|";
    private static final String sepregex = "\\|";
    
    public void storeToFile(String fileName) {
        Properties p = new Properties();
        java.lang.reflect.Field[] f = FaxOptions.class.getFields();
        
        for (int i = 0; i < f.length; i++) {
            try {
                if (Modifier.isStatic(f[i].getModifiers()) || Modifier.isFinal(f[i].getModifiers()))
                    continue;
                
                Object val = f[i].get(this);
                if (val == null)
                    continue;
                
                String name = f[i].getName();
                if ((val instanceof String) || (val instanceof Integer) || (val instanceof Boolean))
                    p.setProperty(name, val.toString());
                else if (val instanceof MyManualMapObject) 
                    p.setProperty(name, ((MyManualMapObject)val).getKey().toString());
                else if (val instanceof Vector) {
                    Vector vec = (Vector)val;
                    String saveval = "";
                    for (int j = 0; j < vec.size(); j++) 
                        saveval += ((FmtItem)vec.get(j)).fmt + sep;
                    p.setProperty(name, saveval);
                } else if (val instanceof Rectangle) {
                    Rectangle rval = (Rectangle)val;
                    p.setProperty(name, "" + rval.x + sep + rval.y + sep + rval.width + sep + rval.height);
                } else if (val instanceof Point) {
                    Point pval = (Point)val;
                    p.setProperty(name, "" + pval.x + sep + pval.y);
                } else
                    System.err.println("Unknown field type " + val.getClass().getName());
            } catch (Exception e) {
                System.err.println("Exception reading field: " + e.getMessage());
            }
        }
        
        try {
            FileOutputStream filout = new FileOutputStream(fileName);
            p.store(filout, utils.AppShortName + " " + utils.AppVersion + " configuration file");
            filout.close();
        } catch (Exception e) {
            System.err.println("Couldn't save file '" + fileName + "': " + e.getMessage());
        }
    }
    
    public void loadFromFile(String fileName) {
        Properties p = new Properties();
        //System.err.println(fileName);
        try {
            FileInputStream filin = new FileInputStream(fileName);
            p.load(filin);
            filin.close();
        } catch (FileNotFoundException e) {
            return; // No file yet
        } catch (IOException e) {
            System.err.println("Error reading file '" + fileName + "': " + e.getMessage());
            return;
        }
        
        Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            String fName = (String)e.nextElement();
            try {
                Field f = FaxOptions.class.getField(fName);
                Class fcls = f.getType();
                if (String.class.isAssignableFrom(fcls))
                    f.set(this, p.getProperty(fName));
                else if (Integer.TYPE.isAssignableFrom(fcls))
                    f.setInt(this, Integer.parseInt(p.getProperty(fName)));
                else if (Boolean.TYPE.isAssignableFrom(fcls))
                    f.setBoolean(this, Boolean.parseBoolean(p.getProperty(fName)));
                else if (MyManualMapObject.class.isAssignableFrom(fcls)) {
                    MyManualMapObject[] dataarray;
                    
                    if (fName.equals("tzone")) 
                        dataarray = utils.timezones;
                    else if (fName.equals("notifyWhen"))
                        dataarray = utils.notifications;
                    else if (fName.equals("resolution"))
                        dataarray = utils.resolutions;
                    else if (fName.equals("paperSize"))
                        dataarray = utils.papersizes;
                    else if (fName.equals("newFaxAction"))
                        dataarray = utils.newFaxActions;
                    else {
                        System.err.println("Unknown MyManualMapObject field: " + fName);
                        continue;
                    }
                    Object res = utils.findInArray(dataarray, p.getProperty(fName));
                    if (res != null)
                        f.set(this, res);
                    else
                        System.err.println("Unknown value for MyManualMapObject field " + fName);
                }
                else if (Vector.class.isAssignableFrom(fcls)) {
                    String[] fields = p.getProperty(fName).split(sepregex);
                    FmtItem[] dataarray, required;
                    Vector<FmtItem> vecres = new Vector<FmtItem>();
                    
                    if (fName.equals("recvfmt")) {
                        dataarray = utils.recvfmts;
                        required = utils.requiredRecvFmts;
                    } else if (fName.equals("sentfmt")) {
                        dataarray = utils.jobfmts;
                        required = utils.requiredSentFmts;
                    } else if (fName.equals("sendingfmt")) {
                        dataarray = utils.jobfmts;
                        required = utils.requiredSendingFmts;
                    } else {
                        System.err.println("Unknown vector field name: " + fName);
                        continue;
                    }
                    for (int i=0; i < fields.length; i++) {
                        FmtItem res = (FmtItem)utils.findInArray(dataarray, fields[i]);
                        if (res == null) 
                            System.err.println("FmtItem for " + fields[i] + "not found.");
                        else 
                            if (!vecres.contains(res))
                                vecres.add(res);
                    }
                    
                    utils.addUniqueToVec(vecres, required);
                    f.set(this, vecres);
                } else if (Rectangle.class.isAssignableFrom(fcls)) {
                    String [] v =  p.getProperty(fName).split(sepregex);
                    f.set(this, new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3])));
                } else if (Point.class.isAssignableFrom(fcls)) {
                    String [] v =  p.getProperty(fName).split(sepregex);
                    f.set(this, new Point(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                } else
                    System.err.println("Unknown field type " + fcls.getName());
            } catch (Exception e1) {
                System.err.println("Couldn't load setting for " + fName + ": " + e1.getMessage());
            }
        }
    }
    
    public static String getDefaultConfigFileName() {
        return utils.getConfigDir() +  "settings";
    }
}

class PaperSize extends MyManualMapObject {
    String desc;
    Dimension size;
    
    public PaperSize(String desc, Dimension size) {
        this.desc = desc;
        this.size = size;        
    }
    
    public String toString() {
        return desc;
    }
    
    @Override
    public Object getKey() {
        return desc;
    }
}

class FaxStringProperty extends MyManualMapObject {
    String desc;
    String type;
    
    public FaxStringProperty(String desc, String type) {
        this.desc = desc;
        this.type = type;        
    }
    
    public String toString() {
        return desc;
    }
    
    @Override
    public Object getKey() {
        return type;
    }
}

class FaxIntProperty extends MyManualMapObject{
    String desc;
    int type;
    
    public FaxIntProperty(String desc, int type) {
        this.desc = desc;
        this.type = type;        
    }
    
    public String toString() {
        return desc;
    }
    
    @Override
    public Object getKey() {
        return type;
    }
    
    public Object stringToKey(String strKey) {
        return Integer.valueOf(strKey);
    }
}

class IntVerifier extends InputVerifier {
    public int min;
    public int max;
    
    @Override
    public boolean verify(JComponent input) {
        try {
            int val = Integer.parseInt(((JTextComponent)input).getText());
            return ((val >= min) && (val <= max));
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public IntVerifier() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    public IntVerifier(int min, int max) {
        super();
        this.min = min;
        this.max = max;
    }
}

// Text field with Button and FileChooser
class FileTextField extends JComponent implements ActionListener {
    
    private JTextField jTextField;
    private JButton jButton;
    private JFileChooser jFileChooser;
    
    public void actionPerformed(ActionEvent e) {
        jFileChooser.setSelectedFile(new File(readTextFieldFileName()));
        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            writeTextFieldFileName(jFileChooser.getSelectedFile().getPath());
    }
    
    protected String readTextFieldFileName() {
        return getText();
    }
    
    protected void writeTextFieldFileName(String fName) {
        setText(fName);
    }
    
    public String getText() {
        return jTextField.getText();
    }
    
    public void setText(String text) {
        jTextField.setText(text);
    }
    
    public JTextField getJTextField() {
        return jTextField;
    }
    
    public JButton getJButton() {
        return jButton;
    }
    
    public JFileChooser getJFileChooser() {
        return jFileChooser;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jButton.setEnabled(enabled);
        jTextField.setEnabled(enabled);
    }
    
    /**
     * Adds a list of file filters; the "all files" filter is included as the last option.
     * The first one is used as a default.
     */
    public void setFileFilters(FileFilter... filters) {
        jFileChooser.resetChoosableFileFilters();
        
        if (filters.length <= 0)
            return;
        
        for (int i=0; i < filters.length; i++)
            jFileChooser.addChoosableFileFilter(filters[i]);
        
        FileFilter allf = jFileChooser.getAcceptAllFileFilter();
        jFileChooser.removeChoosableFileFilter(allf);
        jFileChooser.addChoosableFileFilter(allf);
        
        jFileChooser.setFileFilter(filters[0]);
    }
    
    public FileTextField() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        
        jTextField = new JTextField();
        
        jButton = new JButton(utils.loadIcon("general/Open"));
        jButton.setToolTipText(utils._("Choose a file using a dialog"));
        jButton.addActionListener(this);
        
        Dimension d = jButton.getPreferredSize();
        Dimension d2 = jTextField.getPreferredSize();
        if (d2.height > d.height)
            d.height = d2.height;
        else
            d2.height = d.height;
        d2.width = Integer.MAX_VALUE;
        
        jButton.setMaximumSize(d);
        jTextField.setMaximumSize(d2);
        
        jFileChooser = new JFileChooser();
        
        add(jTextField);
        add(jButton);
    }
}

class HylaDateField {
    public SimpleDateFormat fmtIn = null;
    public SimpleDateFormat fmtOut = null;
    
    public HylaDateField(String sFmtIn, String sFmtOut) {
        fmtIn = new SimpleDateFormat(sFmtIn);
        fmtOut = new SimpleDateFormat(sFmtOut, utils.getLocale());
    }
    
}
