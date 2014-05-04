package yajhfc;
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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.print.attribute.Attribute;

import yajhfc.file.FileFormat;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.file.textextract.RecipientExtractionMode;
import yajhfc.file.textextract.HylaToTextConverter;
import yajhfc.model.FmtItemList;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.options.MultiFileMode;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.CompanyRule;
import yajhfc.phonebook.convrules.LocationRule;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.ZIPCodeRule;
import yajhfc.send.SendWinStyle;
import yajhfc.server.ServerOptions;
import yajhfc.util.AcceleratorKeys;

public class FaxOptions extends AbstractFaxOptions implements Cloneable {
        
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
     * Whether to use a custom fax viewer or the system default
     */
    public boolean useCustomFaxViewer = false;
    /**
     * Command line for the PostScript viewer
     */
    public String psViewer;
    /**
     * Whether to use a custom PS viewer or the system default
     */
    public boolean useCustomPSViewer = false;
    /**
     * Command line for the PDF viewer
     */
    public String pdfViewer;
    /**
     * Whether to use a custom PDF viewer or the system default
     */
    public boolean useCustomPDFViewer = false;
    
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
     * The custom cover page to use
     */
    public String CustomCover;

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
     * The UI language
     */
    public YajLanguage locale = YajLanguage.SYSTEM_DEFAULT;
    
    
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
     * Use the fax's subject as USRKEY HylaFAX property
     */
    public boolean regardingAsUsrKey = true;
    /**
     * The last path faxes have been saved to
     */
    public String lastSavePath = "";
    
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
    
    public static final String DEF_TOOLBAR_CONFIG = "Send|---|Show|Delete|---|Refresh|---|Phonebook|---|Resume|Suspend";
    /**
     * Configuration of the main window toolbar
     */
    public String toolbarConfig = DEF_TOOLBAR_CONFIG;
    
    /**
     * The map containing the keyboard accelerator mappings for the main window
     */
    public final Map<String,String> keyboardAccelerators = new TreeMap<String,String>();
    
    /**
     * The map containing the keyboard accelerator mappings for the phonebook window
     */
    public final Map<String,String> pbwinKeyboardAccelerators = new TreeMap<String,String>();
    
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
    public String phonebookDisplayStyle = "GIVENNAME_NAME";
    /**
     * Display style of the phone book entries
     */
    public String phonebookSortOrder = "";
    
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
     * View faxes always in this format?
     */
    public boolean alwaysCreateTargetFormatForViewing = false;
    /**
     * Format for viewing
     */
    public MultiFileConvFormat singleFileFormatForViewing = MultiFileConvFormat.PDF;
    
    /**
     * Send faxes always in this format?
     */
    public boolean alwaysCreateTargetFormat = false;
    /**
     * Send multiple files as
     */
    public MultiFileMode multiFileSendMode = MultiFileMode.NONE;
    /**
     * Format for sending
     */
    public MultiFileConvFormat singleFileFormat = MultiFileConvFormat.PDF;
    /**
     * Location of the GhostScript executable
     */
    public String ghostScriptLocation;
    /**
     * Location of the tiff2pdf executable
     */
    public String tiff2PDFLocation;
    
    /**
     * The columns displayed in the "Archive" table
     */
    public final FmtItemList<QueueFileFormat> archiveFmt;
    /**
     * The state (sorting and widths) of the "Archive" table's columns
     */
    public String archiveColState = "";
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
     * The attributes used for printing
     */
    public Attribute[] printAttributes = null;
    
    /**
     * Use the same column widths as on screen in the printout
     */
    public boolean faxprintColumnWidthAsOnScreen = true;
    /**
     * Mark error faxes as on screen
     */
    public boolean faxprintMarkErrors = true;
    /**
     * Mark unread faxes as on screen
     */
    public boolean faxprintMarkUnread = true;
    
    /**
     * The phone book fields to print
     */
    public final List<PBEntryField> pbprintPrintColumns = new ArrayList<PBEntryField>();
    
    /**
     * The position and size of the log viewer window
     */
    public Rectangle logViewerBounds =  null;
   
    /**
     * Whether to use the FORM HylaFAX command or not.
     */
    public boolean sendFORMCommand = true;
    
    /**
     * Custom file converters.
     */
    public final Map<String,String> customFileConverters = new TreeMap<String,String>();
    
    /**
     * Automatically reconnect after connection loss
     */
    public boolean autoReconnect = true;
    
    /**
     * Use a cache for the fax jobs lists
     */
    public boolean useFaxListCache = true;
    
    /**
     * The known HylaFAX itemsListModel
     */
    public final List<ServerOptions> servers = new ArrayList<ServerOptions>();
    
    /**
     * The known sender identities
     */
    public final List<SenderIdentity> identities = new ArrayList<SenderIdentity>();
    
    /**
     * The last selected server ID
     */
    public int lastServerID = -1;
    
    /**
     * The update interval of the server status in milliseconds
     */
    public int statusUpdateInterval = 3000;
    /**
     * The update interval of the tables in milliseconds
     */
    public int tableUpdateInterval = 20000;
    /**
     * The socket timeout in milliseconds
     */
    public int socketTimeout = 90000;
    
    /**
     * The settings used for "save table as CSV"
     */
    public String csvExportSettings = "";
    
    /**
     * The path last used for "save table as"
     */
    public String lastExportSavePath = "";
    
    /**
     * The format last used for "save table as"
     */
    public FileFormat lastExportFormat = FileFormat.CSV;
    
    /**
     * Specify the paper size for tiff2pdf/tiff2ps
     */
    public boolean usePaperSizeForTIFF2Any = true;
    
    /**
     * Show the quick search bar in MainWin?
     */
    public boolean showQuickSearchbar = true;
    
    /**
     * Show the toolbar in MainWin?
     */
    public boolean showToolbar = true;
    
    
    /**
     * Extract fax numbers from documents by default?
     */
    public RecipientExtractionMode extractRecipients = RecipientExtractionMode.NO;
    
    /**
     * The hyla to text converter to use
     */
    public String hylaToTextConverter = HylaToTextConverter.DEFAULT_CONVERTER;
    
    /**
     * Path to the pstotext executable
     */
    public String pstotextPath = (PlatformInfo.IS_WINDOWS ? "pstotxt3.exe" : "pstotext");
    
    /**
     * Path to the pdftotext executable
     */
    public String pdftotextPath = "pdftotext";
    
    /**
     * Expand phone books on load
     */
    public boolean expandPhoneBooksOnLoad = true;
    
    
    /**
     * Dialogs not to ask again
     */
    public final List<String> dialogsDoNotAskAgain = new ArrayList<String>();
    
    
    public static final String[] DEFAULT_RECIPIENT_EXTRACTION_TAGS = {
        "Ffax",
        "Frecipient",
        "Mmail",
        "Mmailrecipient",
        "Ssubject"
    };
    /**
     * Tag names for recipient extraction
     */
    public final List<String> recipientExtractionTags = new ArrayList<String>();
    
    /**
     * Is the colon mandatory in the tags?
     */
    public boolean recipientExtractionTagMandatoryColon = false;
    
    public FaxOptions() {
        super(null);
        
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
        this.sentfmt.add(JobFormat.Y);
        
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
        
        final String defaultViewer = PlatformInfo.getSystemViewerCommandLine();
        if (PlatformInfo.IS_WINDOWS) {
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
        
        mainWinBounds = null;
        phoneWinBounds = null;
        mainwinLastTab = 0;
        
        useCover = false;
        useCustomCover = false;
        CustomCover = "";
        
        keyboardAccelerators.putAll(AcceleratorKeys.DEFAULT_MAINWIN_MAPPING);
        pbwinKeyboardAccelerators.putAll(AcceleratorKeys.DEFAULT_PBWIN_MAPPING);
        
        Collections.addAll(recipientExtractionTags, DEFAULT_RECIPIENT_EXTRACTION_TAGS);
    }
    
    /**
     * An array of arrays specifying settings whose defaults should be copied from other
     * settings when a config file is loaded that does contain the specified settings but not
     * the other ones (i.e. when either both or none are present, nothing is done)
     * 
     * Format of the "sub-arrays": { setting_to_copy_from, copyto1, copyto2, ...}
     */
    private static final String[][] copyDefaultSettings = {
    	{"alwaysCreateTargetFormat", "alwaysCreateTargetFormatForViewing"},
    	{"singleFileFormat",         "singleFileFormatForViewing"},
    };
    @Override
    public void loadFromProperties(Properties p) {
    	Properties p2 = null;
    	for (String[] sub : copyDefaultSettings) {
    		String defValue = p.getProperty(sub[0]);
    		if (defValue != null) {
    			for (int i=1; i<sub.length; i++) {
    				String prop = sub[i];
    				if (p.getProperty(prop) == null) {
    					if (p2==null) {
    						p2 = new Properties(p);
    					}
    					p2.setProperty(prop, defValue);
    				}
    			}
    		}
    	}
    	super.loadFromProperties(p2 == null ? p : p2);
    	
        if (p.containsKey("host")) {
            // Copy server settings over
            ServerOptions so = new ServerOptions(this);
            so.name = Utils._("Default");
            so.loadFromProperties(p);
            servers.add(so);
        }
        
        if (p.containsKey("FromName")) {
            // Copy identity over
            SenderIdentity si = new SenderIdentity(this);
            si.name = Utils._("Default");
            si.loadFromProperties(p);
            identities.add(si);
        }
        
        if (servers.size() == 0) {
            // Add a default server
            ServerOptions so = new ServerOptions(this);
            so.name = Utils._("Default");
            servers.add(so);
        } else {
            int count = IDAndNameOptions.removeDuplicates(servers);
            if (count > 0) {
                log.severe("" + count + " duplicate servers removed!");
            }
        }

        if (identities.size() == 0) {
            // Add a default identity
            SenderIdentity si = new SenderIdentity(this);
            si.name = Utils._("Default");
            identities.add(si);
        } else {
            int count = IDAndNameOptions.removeDuplicates(identities);
            if (count > 0) {
                log.severe("" + count + " duplicate identities removed!");
            }
        }
        
        
        // Make sure there is at least one entry for each tag type
        
        // 1. collect the tag types from the config file
        char[] configTags = new char[recipientExtractionTags.size()];
        for (int i=0; i<recipientExtractionTags.size(); i++) {
            String tag = recipientExtractionTags.get(i);
            if (tag.length() >= 1) {
                configTags[i] = tag.charAt(0);
            }
        }
        
        // 2. check if for each default type there is one available in the config
        for (String tagStr : DEFAULT_RECIPIENT_EXTRACTION_TAGS) {
            char tag = tagStr.charAt(0);
            boolean exists = false;
            for (char confTag : configTags) {
                if (tag==confTag) {
                    exists=true;
                    break;
                }
            }
            if (!exists) {
                log.info("Adding recipient extraction tag " + tagStr);
                recipientExtractionTags.add(tagStr);
            }
        }
    }
    
    @Override
    public FaxOptions clone()  {
        try {
            return (FaxOptions)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the default server to use as a fall back
     * @return
     */
    public ServerOptions getDefaultServer() {
        if (servers.size() == 0)
            return null;
        return servers.get(0);
    }
    
    /**
     * Returns the default identity to use as a fall back
     * @return
     */
    public SenderIdentity getDefaultIdentity() {
        if (identities.size() == 0)
            return null;
        return identities.get(0);
    }
}
