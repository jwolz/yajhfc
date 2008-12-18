package yajhfc;
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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.file.FormattedFile.FileFormat;
import yajhfc.model.archive.QueueFileFormat;
import yajhfc.options.MultiFileMode;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.CompanyRule;
import yajhfc.phonebook.convrules.LocationRule;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.convrules.ZIPCodeRule;
import yajhfc.send.SendWinStyle;

public class FaxOptions {
    static final Logger log = Logger.getLogger(FaxOptions.class.getName());
    
    public FaxTimezone tzone;
    public String host;
    public int port;
    public boolean pasv;
    public String user;
    public String pass;
    
    public final FmtItemList<RecvFormat> recvfmt;
    public final FmtItemList<JobFormat> sentfmt;
    public final FmtItemList<JobFormat> sendingfmt;
    
    public String faxViewer;
    public String psViewer;
    public String pdfViewer;
    public boolean viewPDFAsPS = true;
    
    public String notifyAddress;
    public FaxNotification notifyWhen = null;
    public FaxResolution resolution = null;
    public PaperSize paperSize = null;
    
    public int maxTry; 
    public int maxDial;
    public int killTime = 180;
    
    public Rectangle mainWinBounds, phoneWinBounds, customFilterBounds;
    public Rectangle sendWinBounds, optWinBounds;
    public String recvColState, sentColState, sendingColState/*, recvReadState*/;
    public int mainwinLastTab;
    
    public int statusUpdateInterval, tableUpdateInterval;
    public int socketTimeout = 90000;
    
    //public String lastPhonebook;
    
    public String FromFaxNumber;
    public String FromVoiceNumber;
    public String FromName;
    public String FromLocation;
    public String FromCompany;
    public String CustomCover;
    public String FromEMail = "";
    public String FromCountry= "";
    public String FromDepartment= "";
    public String FromGivenName= "";
    public String FromPosition= "";
    public String FromState= "";
    public String FromStreet= "";
    public String FromTitle= "";
    public String FromZIPCode= "";
    public String FromWebsite= "";
    public boolean useCover, useCustomCover;
    
    // Basic actions when a new fax is detected.
    // The constants should be powers of 2 to make it possible to combine several of them
    public static final int NEWFAX_NOACTION = 0;
    public static final int NEWFAX_BEEP = 1;
    public static final int NEWFAX_TOFRONT = 2;
    public static final int NEWFAX_VIEWER = 4;
    public static final int NEWFAX_MARKASREAD = 8;
    public static final int NEWFAX_BLINKTRAYICON = 16;
    
    public int newFaxAction = FaxOptions.NEWFAX_BEEP | FaxOptions.NEWFAX_TOFRONT | FaxOptions.NEWFAX_BLINKTRAYICON;
    public boolean pclBug = false;
    public boolean askPassword = false, askAdminPassword = true, askUsername = false;
    public String AdminPassword = "";
    
    public String recvFilter = null, sentFilter = null, sendingFilter = null;
    
    public String defaultCover = null;
    public boolean useCustomDefaultCover = false;
    
    public YajLanguage locale = YajLanguage.SYSTEM_DEFAULT;
    
    // Offset for displayed date values in seconds:
    public int dateOffsetSecs = 0;
    
    //public boolean preferRenderedTIFF = false;
    public boolean markFailedJobs = true;
    public boolean showRowNumbers = false;
    public boolean adjustColumnWidths = true;
    
    public String lookAndFeel = LOOKANDFEEL_SYSTEM;
    public static final String LOOKANDFEEL_SYSTEM = "!system!";
    public static final String LOOKANDFEEL_CROSSPLATFORM = "!crossplatform!"; 
    
    public final List<String> phoneBooks = new ArrayList<String>();
    //public int lastSelectedPhonebook = 0;
    
    public boolean useDisconnectedMode = false;
    public String defaultModem = "any";
    public boolean regardingAsUsrKey = true;
    public String lastSavePath = "";
    
    public SendWinStyle sendWinStyle = SendWinStyle.SIMPLIFIED;
    public boolean sendWinIsAdvanced = false;
    public String lastSendWinPath = "";
    public String persistenceMethod = "local";
    public String persistenceConfig = "";
    
    public static final String DEF_TOOLBAR_CONFIG = "Send|---|Show|Delete|---|Refresh|---|Phonebook|---|Resume|Suspend";
    public String toolbarConfig = DEF_TOOLBAR_CONFIG;
    
    public boolean showTrayIcon = true;
    public boolean minimizeToTray = true;
    public NameRule phonebookDisplayStyle = NameRule.GIVENNAME_NAME;
    public NameRule coverNameRule = NameRule.TITLE_GIVENNAME_NAME_JOBTITLE;
    public ZIPCodeRule coverZIPCodeRule = ZIPCodeRule.ZIPCODE_LOCATION;
    public LocationRule coverLocationRule = LocationRule.STREET_LOCATION;
    public CompanyRule coverCompanyRule = CompanyRule.DEPARTMENT_COMPANY;
    
    public boolean useJDK16PSBugfix = true;
    
    public boolean createSingleFilesForViewing = false;
    public boolean alwaysCreateTargetFormat = false;
    public MultiFileMode multiFileSendMode = MultiFileMode.NONE;
    public FileFormat singleFileFormat = FileFormat.PDF;
    public String ghostScriptLocation;
    public String tiff2PDFLocation;
    
    
    // Uncomment for archive support.
    public boolean showArchive = false;
    public final FmtItemList<QueueFileFormat> archiveFmt;
    public String archiveColState = "";
    public String archiveLocation = "";
    
    public String dateStyle = DateStyle.FROM_LOCALE;
    public String timeStyle = DateStyle.FROM_LOCALE;
    
    public FaxOptions() {
        this.host = "";
        this.port = 4559;
        this.user = System.getProperty("user.name");
        this.pass = "";
        this.pasv = true;
        this.tzone = FaxTimezone.LOCAL;
        
        this.recvfmt = new FmtItemList<RecvFormat>(RecvFormat.values(), RecvFormat.getRequiredFormats());
        this.recvfmt.add(RecvFormat.Y);
        this.recvfmt.add(RecvFormat.s);
        this.recvfmt.add(RecvFormat.e);
        this.recvfmt.add(RecvFormat.h);
        this.recvfmt.add(RecvFormat.p);
        this.recvfmt.add(RecvFormat.f);
        //this.recvFileCol = 2;
        this.recvColState = String.valueOf(this.recvfmt.indexOf(RecvFormat.f) + 1); // 0 means: no sorting
        
        this.sentfmt = new FmtItemList<JobFormat>(JobFormat.values(), JobFormat.getRequiredFormats());
        this.sentfmt.add(JobFormat.o); 
        this.sentfmt.add(JobFormat.e); //e 
        this.sentfmt.add(JobFormat.s);       
        this.sentfmt.add(JobFormat.t);
        this.sentfmt.add(JobFormat.j);
        this.sentfmt.add(JobFormat.a_desc);
        this.sentfmt.add(JobFormat.z);
        
        this.sendingfmt = new FmtItemList<JobFormat>(JobFormat.values(), JobFormat.getRequiredFormats());
        this.sendingfmt.addAll(this.sentfmt);
        
        this.sentColState = this.sendingColState = "";
        
        // Uncomment for archive support.
        this.archiveFmt = new FmtItemList<QueueFileFormat>(QueueFileFormat.values(), QueueFileFormat.getRequiredFormats());
        this.archiveFmt.add(QueueFileFormat.owner);
        this.archiveFmt.add(QueueFileFormat.number);
        this.archiveFmt.add(QueueFileFormat.tottries);
        this.archiveFmt.add(QueueFileFormat.jobid);
        this.archiveFmt.add(QueueFileFormat.state);
        this.archiveFmt.add(QueueFileFormat.status);
        
        if (Utils.IS_WINDOWS) {
            String startCmd = System.getenv("COMSPEC");
            if (startCmd == null) startCmd = "COMMAND";
            startCmd += " /C start \"Viewer\" \"%s\"";
            
            this.psViewer = startCmd;  //"rundll32.exe URL.DLL,FileProtocolHandler \"%s\"";//"gsview32.exe";
            this.pdfViewer = startCmd;
            
            String sysname = System.getProperty("os.name");
            if (sysname.indexOf("XP") >= 0 || sysname.indexOf("Vista") >= 0) 
                this.faxViewer = "rundll32.exe shimgvw.dll,ImageView_Fullscreen %s";
            else
                this.faxViewer = startCmd; //"rundll32.exe URL.DLL,FileProtocolHandler \"%s\"";//"kodakimg.exe";
            
            this.ghostScriptLocation = "gswin32c.exe";
            this.tiff2PDFLocation = "tiff2pdf.exe";
        } else {
            Map<String,String> env = System.getenv();
            if ("true".equals(env.get("KDE_FULL_SESSION"))) {
                this.faxViewer = "kfmclient exec %s";
                this.psViewer = "kfmclient exec %s";
                this.pdfViewer = "kfmclient exec %s";
            } else {
                String gnome = env.get("GNOME_DESKTOP_SESSION_ID");
                if (gnome != null && gnome.length() > 0) {
                    this.faxViewer = "gnome-open %s";
                    this.psViewer = "gnome-open %s";
                    this.pdfViewer = "gnome-open %s";
                } else {
                    this.faxViewer = "kfax %s";
                    this.psViewer = "gv %s";
                    this.pdfViewer = "xpdf %s";
                }
            }
            this.ghostScriptLocation = "gs";
            this.tiff2PDFLocation = "tiff2pdf";
        }
        
        this.resolution = FaxResolution.HIGH;
        this.paperSize = PaperSize.A4;
        this.notifyWhen = FaxNotification.DONE_AND_REQUEUE;
        this.notifyAddress = this.user +  "@localhost"; //+ this.host;
        this.maxTry = 6;
        this.maxDial = 12;  
        
        mainWinBounds = null;
        phoneWinBounds = null;
        mainwinLastTab = 0;
        
        statusUpdateInterval = 2000;
        tableUpdateInterval = 6000;
        //recvReadState = "";
        
        //lastPhonebook = "";
        
        FromFaxNumber = "";
        FromVoiceNumber = "";
        FromName = user;
        FromLocation = "";
        FromCompany = "";
        
        useCover = false;
        useCustomCover = false;
        CustomCover = "";
    }
    
    
    private static final char sep = '|';
    //private static final String sepregex = "\\|";
    
    @SuppressWarnings("unchecked")
    public void storeToFile(File file) {
        Properties p = new Properties();
        java.lang.reflect.Field[] f = FaxOptions.class.getFields();
        
        for (int i = 0; i < f.length; i++) {
            try {
                if (Modifier.isStatic(f[i].getModifiers()))
                    continue;
                
                Object val = f[i].get(this);
                if (val == null)
                    continue;
                
                String name = f[i].getName();
                if ((val instanceof String) || (val instanceof Integer) || (val instanceof Boolean))
                    p.setProperty(name, val.toString());
                else if (val instanceof YajLanguage) {
                    p.setProperty(name, ((YajLanguage)val).getLangCode());
                } else if (val instanceof FmtItemList) {
                    p.setProperty(name, ((FmtItemList)val).saveToString());
                } else if (val instanceof Rectangle) {
                    Rectangle rval = (Rectangle)val;
                    p.setProperty(name, "" + rval.x + sep + rval.y + sep + rval.width + sep + rval.height);
                } else if (val instanceof Point) {
                    Point pval = (Point)val;
                    p.setProperty(name, "" + pval.x + sep + pval.y);
                } else if (val instanceof List) {
                    List lst = (List)val;
                    int idx = 0;
                    for (Object o : lst) {
                        p.setProperty(name + '.' + (++idx), (String)o);
                    }
                } else if (val instanceof Enum) {
                    p.setProperty(name, ((Enum)val).name());
                } else {
                    log.log(Level.WARNING, "Unknown field type " + val.getClass().getName());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Exception reading field: ", e);
            }
        }
        
        try {
            FileOutputStream filout = new FileOutputStream(file);
            p.store(filout, Utils.AppShortName + " " + Utils.AppVersion + " configuration file");
            filout.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Couldn't save file '" + file + "': ", e);
        }
    }
    
    private final PBEntryFieldContainer coverFrom = new PBEntryFieldContainer() {
        public String getField(PBEntryField field) {
            switch (field) {
            case Company:
                return FromCompany;
            case Country:
                return FromCountry;
            case Department:
                return FromDepartment;
            case EMailAddress:
                return FromEMail;
            case FaxNumber:
                return FromFaxNumber;
            case GivenName:
                return FromGivenName;
            case Location:
                return FromLocation;
            case Name:
                return FromName;
            case Position:
                return FromPosition;
            case State:
                return FromState;
            case Street:
                return FromStreet;
            case Title:
                return FromTitle;
            case VoiceNumber:
                return FromVoiceNumber;
            case ZIPCode:
                return FromZIPCode;
            case WebSite:
                return FromWebsite;
            default:
                log.warning("Unknown PBEntryField: " + field.name());
                // Fall through intended
            case Comment:
                return "";
            }
        }
        
        public void setField(PBEntryField field, String value) {
            switch (field) {
            case Company:
                FromCompany = value;
                break;
            case Country:
                FromCountry = value;
                break;
            case Department:
                FromDepartment = value;
                break;
            case EMailAddress:
                FromEMail = value;
                break;
            case FaxNumber:
                FromFaxNumber = value;
                break;
            case GivenName:
                FromGivenName = value;
                break;
            case Location:
                FromLocation = value;
                break;
            case Name:
                FromName = value;
                break;
            case Position:
                FromPosition = value;
                break;
            case State:
                FromState = value;
                break;
            case Street:
                FromStreet = value;
                break;
            case Title:
                FromTitle = value;
                break;
            case VoiceNumber:
                FromVoiceNumber = value;
                break;
            case ZIPCode:
                FromZIPCode = value;
                break;
            case WebSite:
                FromWebsite = value;
                break;
            default:
                log.warning("Unknown PBEntryField: " + field.name());
                // Fall through intended
            case Comment:
                break;
            }
        }
    };

    public PBEntryFieldContainer getCoverFrom() {
        return coverFrom;
    }
    
    @SuppressWarnings("unchecked")
    public void loadFromProperties(Properties p) {

        if (p.size() == 0) {
            log.info("No settings to load found.");
            return;
        }
        
        if (Utils.debugMode) {
            log.config("---- BEGIN preferences dump");
            Utils.dumpProperties(p, log, "pass", "AdminPassword");
            log.config("---- END preferences dump");
        }

        for (Field f : FaxOptions.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            
            try {
                Class<?> fcls = f.getType();
                if (List.class.isAssignableFrom(fcls) 
                        && (!FmtItemList.class.isAssignableFrom(fcls))) {
                    final String fieldName = f.getName();
                    final List<String> list = (List<String>)f.get(this);
                    list.clear();

                    int i = 1;
                    String val;
                    while ((val = p.getProperty(fieldName + '.' + i)) != null) {
                        list.add(val);
                        i++;
                    }
                } else {
                    String val = p.getProperty(f.getName());
                    if (val != null) {
                        if (String.class.isAssignableFrom(fcls))
                            f.set(this, val);
                        else if (Integer.TYPE.isAssignableFrom(fcls))
                            f.setInt(this, Integer.parseInt(val));
                        else if (Boolean.TYPE.isAssignableFrom(fcls))
                            f.setBoolean(this, Boolean.parseBoolean(val));
                        else if (YajLanguage.class.isAssignableFrom(fcls)) {
                            f.set(this, YajLanguage.languageFromLangCode(val));
                        } else if (FmtItemList.class.isAssignableFrom(fcls)) {
                            FmtItemList fim = (FmtItemList)f.get(this);
                            fim.loadFromString(val);
                        } else  if (Rectangle.class.isAssignableFrom(fcls)) {
                            String [] v =  Utils.fastSplit(val, sep);
                            f.set(this, new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3])));
                        } else if (Point.class.isAssignableFrom(fcls)) {
                            String [] v =  Utils.fastSplit(val, sep);
                            f.set(this, new Point(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                        } else if (Enum.class.isAssignableFrom(fcls)) {
                            f.set(this, Enum.valueOf((Class<? extends Enum>)fcls, val));
                        } else {
                            log.log(Level.WARNING, "Unknown field type " + fcls);
                        }
                    }
                }
            } catch (Exception e1) {
                log.log(Level.WARNING, "Couldn't load setting for " + f + ": ", e1);
            }
        }
    }
}
