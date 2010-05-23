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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.print.attribute.Attribute;

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

public class FaxOptions extends AbstractFaxOptions {
    
    /**
     * The time zone to use to display date or time
     */
    public FaxTimezone tzone;
    /**
     * The HylaFAX server's host name
     */
    public String host;
    /**
     * The TCP port to connect to 
     */
    public int port;
    /**
     * Use passive mode for HylaFAX protocol operations
     */
    public boolean pasv;
    /**
     * The user name used to connect to the HylaFAX server
     */
    public String user;
    /**
     * The password used to connect to the HylaFAX server
     */
    public final Password pass = new Password();
    
    /**
     * The columns displayed in the "Received" table
     */
    public final FmtItemList<RecvFormat> recvfmt;
    /**
     * The columns displayed in the "Sent" table
     */
    public final FmtItemList<JobFormat> sentfmt;
    /**
     * The columns displayed in the "Transmitting" table
     */
    public final FmtItemList<JobFormat> sendingfmt;
    
    /**
     * Command line for the fax (TIFF) viewer
     */
    public String faxViewer;
    /**
     * Command line for the PostScript viewer
     */
    public String psViewer;
    /**
     * Command line for the PDF viewer
     */
    public String pdfViewer;
    /**
     * true if PDF files shall be viewed using the psViewer instead of pdfViewer
     */
    public boolean viewPDFAsPS = true;
    
    /**
     * The notification e-mail address sent to the HylaFAX server
     */
    public String notifyAddress;
    /**
     * When to send a notification e-mail
     */
    public FaxNotification notifyWhen = null;
    /**
     * The default resolution used to send faxes 
     */
    public FaxResolution resolution = null;
    /**
     * The default paper size used to send faxes
     */
    public PaperSize paperSize = null;
    
    /**
     * The default maximum number of tries to send a fax
     */
    public int maxTry;
    /**
     * The default maximum number of dials sending a fax
     */
    public int maxDial;
    /**
     * The default kill time in minutes
     */
    public int killTime = 180;
    
    /**
     * The screen position of the main window in pixels
     */
    public Rectangle mainWinBounds;
    /**
     * The screen position of the phone book window in pixels
     */
    public Rectangle phoneWinBounds;
    /**
     * The screen position of the custom filter dialog in pixels
     */
    public Rectangle customFilterBounds;
    /**
     * The screen position of the send dialog in pixels
     */
    public Rectangle sendWinBounds;
    /**
     * The screen position of the options dialog in pixels
     */
    public Rectangle optWinBounds;
    
    /**
     * The state (sorting and widths) of the "Received" table's columns
     */
    public String recvColState;
    /**
     * The state (sorting and widths) of the "Sent" table's columns
     */
    public String sentColState;
    /**
     * The state (sorting and widths) of the "Transmitting" table's columns
     */
    public String sendingColState;
    /**
     * The last selected tab in the main window
     */
    public int mainwinLastTab;
    
    /**
     * The update interval of the server status in milliseconds
     */
    public int statusUpdateInterval;
    /**
     * The update interval of the tables in milliseconds
     */
    public int tableUpdateInterval;
    /**
     * The socket timeout in milliseconds
     */
    public int socketTimeout = 90000;
    
    /**
     * The sender's fax number for the cover page
     */
    public String FromFaxNumber;
    /**
     * The sender's voice number for the cover page
     */
    public String FromVoiceNumber;
    /**
     * The sender's name for the cover page
     */
    public String FromName;
    /**
     * The sender's location for the cover page
     */
    public String FromLocation;
    /**
     * The sender's company for the cover page
     */
    public String FromCompany;
    /**
     * The custom cover page to use
     */
    public String CustomCover;
    /**
     * The sender's e-mail address for the cover page
     */
    public String FromEMail = "";
    /**
     * The sender's country for the cover page
     */
    public String FromCountry= "";
    /**
     * The sender's department for the cover page
     */
    public String FromDepartment= "";
    /**
     * The sender's given name for the cover page
     */
    public String FromGivenName= "";
    /**
     * The sender's position for the cover page
     */
    public String FromPosition= "";
    /**
     * The sender's state for the cover page
     */
    public String FromState= "";
    /**
     * The sender's street for the cover page
     */
    public String FromStreet= "";
    /**
     * The sender's title for the cover page
     */
    public String FromTitle= "";
    /**
     * The sender's ZIP code for the cover page
     */
    public String FromZIPCode= "";
    /**
     * The sender's website for the cover page
     */
    public String FromWebsite= "";
    /**
     * Use a cover page by default?
     */
    public boolean useCover;
    /**
     * Use a custom cover page?
     */
    public boolean useCustomCover;
    
    // Basic actions when a new fax is detected.
    // The constants should be powers of 2 to make it possible to combine several of them
    public static final int NEWFAX_NOACTION = 0;
    public static final int NEWFAX_BEEP = 1;
    public static final int NEWFAX_TOFRONT = 2;
    public static final int NEWFAX_VIEWER = 4;
    public static final int NEWFAX_MARKASREAD = 8;
    public static final int NEWFAX_BLINKTRAYICON = 16;
    
    /**
     * The actions to perform when a new fax is received
     */
    public int newFaxAction = FaxOptions.NEWFAX_BEEP | FaxOptions.NEWFAX_TOFRONT | FaxOptions.NEWFAX_BLINKTRAYICON;
    
    /**
     * Show a tray notification when a new fax is received?
     */
    public boolean newFaxTrayNotification = true;
    /**
     * Use the PCL file type bug fix?
     */
    public boolean pclBug = false;
    /**
     * Always ask for password?
     */
    public boolean askPassword = false;
    /**
     * Always ask for admin password?
     */
    public boolean askAdminPassword = true;
    /**
     * Always ask for user name?
     */
    public boolean askUsername = false;
    /**
     * The administrative password used to connect to the HylaFAX server in admin mode
     */
    public final Password AdminPassword = new Password();
    
    /**
     * The filter for the "Received" table
     */
    public String recvFilter = null;
    /**
     * The filter for the "Sent" table
     */
    public String sentFilter = null;
    /**
     * The filter for the "Transmitting" table
     */
    public String sendingFilter = null;
    /**
     * Allow the user to change the fax job filter (all faxes/own faxes/custom filter)?
     */
    public boolean allowChangeFilter = true;
    
    /**
     * The default cover page to use
     */
    public String defaultCover = null;
    /**
     * "true" if the cover page specified by defaultCover shall be used, "false" to use the internal default cover
     */
    public boolean useCustomDefaultCover = false;
    
    /**
     * The UI language
     */
    public YajLanguage locale = YajLanguage.SYSTEM_DEFAULT;
    
    /**
     *  Offset for displayed date values in seconds
     */
    public int dateOffsetSecs = 0;
    
    //public boolean preferRenderedTIFF = false;
    /**
     * Mark failed jobs with a light red background?
     */
    public boolean markFailedJobs = true;
    /**
     * Show row numbers?
     */
    public boolean showRowNumbers = false;
    /**
     * Adjust column widths to fit the window size?
     */
    public boolean adjustColumnWidths = true;
    
    /**
     * The look & feel to use
     */
    public String lookAndFeel = LOOKANDFEEL_SYSTEM;
    public static final String LOOKANDFEEL_SYSTEM = "!system!";
    public static final String LOOKANDFEEL_CROSSPLATFORM = "!crossplatform!"; 
    
    /**
     * The list of phone books to load
     */
    public final List<String> phoneBooks = new ArrayList<String>();
    
    /**
     * Create a new session for every action?
     */
    public boolean useDisconnectedMode = false;
    /**
     * The default modem to use
     */
    public String defaultModem = "any";
    /**
     * Use the fax's subject as USRKEY HylaFAX property
     */
    public boolean regardingAsUsrKey = true;
    /**
     * The last path faxes have been saved to
     */
    public String lastSavePath = "";
    /**
     * Archive sent faxes by default?
     */
    public boolean archiveSentFaxes = false;
    
    /**
     * Style of the send dialog
     */
    public SendWinStyle sendWinStyle = SendWinStyle.SIMPLIFIED;
    /**
     * Is the simplified send dialog in advanced mode?
     */
    public boolean sendWinIsAdvanced = false;
    /**
     * Last path from which fax documents have been added
     */
    public String lastSendWinPath = "";
    /**
     * The method to save the read/unread state
     */
    public String persistenceMethod = "local";
    /**
     * Configuration of the method to save the read/unread state
     */
    public String persistenceConfig = "";
    
    public static final String DEF_TOOLBAR_CONFIG = "Send|---|Show|Delete|---|Refresh|---|Phonebook|---|Resume|Suspend";
    /**
     * Configuration of the main window toolbar
     */
    public String toolbarConfig = DEF_TOOLBAR_CONFIG;
    
    /**
     * Show a tray icon?
     */
    public boolean showTrayIcon = true;
    /**
     * Minimize to tray?
     */
    public boolean minimizeToTray = true;
    /**
     * Minimize to tray when main window is closed?
     */
    public boolean minimizeToTrayOnMainWinClose = true;
    /**
     * Display style of the phone book entries
     */
    public NameRule phonebookDisplayStyle = NameRule.GIVENNAME_NAME;
    /**
     * Format of the name on the cover page 
     */
    public NameRule coverNameRule = NameRule.TITLE_GIVENNAME_NAME_JOBTITLE;
    /**
     * Format of the ZIP code on the cover page 
     */
    public ZIPCodeRule coverZIPCodeRule = ZIPCodeRule.ZIPCODE_LOCATION;
    /**
     * Format of the location on the cover page 
     */
    public LocationRule coverLocationRule = LocationRule.STREET_LOCATION;
    /**
     * Format of the company on the cover page 
     */
    public CompanyRule coverCompanyRule = CompanyRule.DEPARTMENT_COMPANY;
    
    /**
     * Use a bug fix for the PostScript generated by the PrintService classes of a 1.6 JRE
     */
    public boolean useJDK16PSBugfix = true;
    
    /**
     * View faxes as single file? 
     */
    public boolean createSingleFilesForViewing = false;
    /**
     * View/send faxes always in this format?
     */
    public boolean alwaysCreateTargetFormat = false;
    /**
     * Send multiple files as
     */
    public MultiFileMode multiFileSendMode = MultiFileMode.NONE;
    /**
     * Format for viewing/sending
     */
    public FileFormat singleFileFormat = FileFormat.PDF;
    /**
     * Location of the GhostScript executable
     */
    public String ghostScriptLocation;
    /**
     * Location of the tiff2pdf executable
     */
    public String tiff2PDFLocation;
    
    
    // Uncomment for archive support.
    /**
     * Show the archive table?
     */
    public boolean showArchive = false;
    /**
     * The columns displayed in the "Archive" table
     */
    public final FmtItemList<QueueFileFormat> archiveFmt;
    /**
     * The state (sorting and widths) of the "Archive" table's columns
     */
    public String archiveColState = "";
    /**
     * The path to the archive folder
     */
    public String archiveLocation = "";
    /**
     * The filter for the "Archive" table
     */
    public String archiveFilter = null;
    
    /**
     * The date format
     */
    public String dateStyle = DateStyle.FROM_LOCALE;
    /**
     * The time format
     */
    public String timeStyle = DateStyle.FROM_LOCALE;
    
    /**
     * The point of time of the last update check 
     */
    public long lastUpdateCheck = 0;
    
    /**
     * The last seen (newer) update version
     */
    public String lastSeenUpdateVersion = "";
    
    /**
     * Automatically check for updates?
     */
    public boolean automaticallyCheckForUpdate = false;
    
    /**
     * Status bar size in the main window. -1 means "automatically resize"
     */
    public int statusBarSize = -1;

    /**
     * Custom job options.
     */
    public final Map<String,String> customJobOptions = new TreeMap<String,String>();
    
    /**
     * Characters to filter out from the fax number.
     */
    public String filterFromFaxNr = "-/()[]{}";
    
    /**
     * Try to use an alternate shutdown method on Windows to work around a Java bug.
     */
    public boolean useWin32ShutdownManager = true;
    
    /**
     * The list of the least recently used fax numbers
     */
    public final List<String> faxNumbersLRU = new ArrayList<String>();
    
    /**
     * Determines if certain menu entries (Quit, Options, About) are removed on Mac OS X
     */
    public boolean adjustMenusForMacOSX = true;
    
    /**
     * Use custom (user-specified) modems?
     */
    public boolean useCustomModems = false;
    
    /**
     * The list of custom modems
     */
    public final List<String> customModems = new ArrayList<String>();
    
    /**
     * The attributes used for printing
     */
    public Attribute[] printAttributes = null;
    
    public FaxOptions() {
        super(null);
        
        this.host = "";
        this.port = 4559;
        this.user = System.getProperty("user.name");
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
        
        final String defaultViewer = Utils.getSystemViewerCommandLine();
        if (Utils.IS_WINDOWS) {
            this.psViewer = defaultViewer;  //"rundll32.exe URL.DLL,FileProtocolHandler \"%s\"";//"gsview32.exe";
            this.pdfViewer = defaultViewer;
            
            String sysname = System.getProperty("os.name");
            if (sysname.indexOf("XP") >= 0 || sysname.indexOf("Vista") >= 0) 
                this.faxViewer = "rundll32.exe shimgvw.dll,ImageView_Fullscreen %s";
            else
                this.faxViewer = defaultViewer; //"rundll32.exe URL.DLL,FileProtocolHandler \"%s\"";//"kodakimg.exe";
            
            this.ghostScriptLocation = "gswin32c.exe";
            this.tiff2PDFLocation = "tiff2pdf.exe";
        } else {
            if (defaultViewer == null) {
                this.faxViewer = "kfax %s";
                this.psViewer = "gv %s";
                this.pdfViewer = "xpdf %s";
            } else {
                this.faxViewer = defaultViewer;
                this.psViewer = defaultViewer;
                this.pdfViewer = defaultViewer;
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
                Logger.getLogger(getClass().getName()).warning("Unknown PBEntryField: " + field.name());
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
                Logger.getLogger(getClass().getName()).warning("Unknown PBEntryField: " + field.name());
                // Fall through intended
            case Comment:
                break;
            }
        }
    };

    public PBEntryFieldContainer getCoverFrom() {
        return coverFrom;
    }
}
