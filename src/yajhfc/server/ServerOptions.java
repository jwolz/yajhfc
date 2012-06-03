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
package yajhfc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yajhfc.FaxNotification;
import yajhfc.FaxOptions;
import yajhfc.FaxResolution;
import yajhfc.FaxTimezone;
import yajhfc.IDAndNameOptions;
import yajhfc.PaperSize;
import yajhfc.Password;
import yajhfc.model.servconn.FaxListConnectionType;

/**
 * @author jonas
 *
 */
public class ServerOptions extends IDAndNameOptions {
    
    /**
     * The time zone to use to display date or time
     */
    public FaxTimezone tzone = FaxTimezone.LOCAL;
    /**
     * The HylaFAX server's host name
     */
    public String host = "";
    /**
     * The TCP port to connect to 
     */
    public int port = 4559;
    /**
     * Use passive mode for HylaFAX protocol operations
     */
    public boolean pasv = true;
    /**
     * The user name used to connect to the HylaFAX server
     */
    public String user = System.getProperty("user.name");
    /**
     * The password used to connect to the HylaFAX server
     */
    public final Password pass = new Password();

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
     *  Offset for displayed date values in seconds
     */
    public int dateOffsetSecs = 0;
    
    /**
     * The type of fax list connection used.
     */
    public FaxListConnectionType faxListConnectionType = FaxListConnectionType.HYLAFAX;
    
    /**
     * The location of the spool area for direct access
     */
    public String directAccessSpoolPath = "";
    
    /**
     * Use custom (user-specified) modems?
     */
    public boolean useCustomModems = false;
    
    /**
     * The list of custom modems
     */
    public final List<String> customModems = new ArrayList<String>();
    
    /**
     * The character encoding used by the HylaFAX server
     */
    public String hylaFAXCharacterEncoding = System.getProperty("file.encoding", "UTF-8");
    
    /**
     * The sender identity to be used by this server by default
     */
    public int defaultIdentity = -1;
    
    /**
     * Show the archive table?
     */
    public boolean showArchive = false;
    /**
     * The path to the archive folder
     */
    public String archiveLocation = "";
    
    /**
     * Create a new session for every action?
     */
    public boolean useDisconnectedMode = false;
    
    /**
     * The method to save the read/unread state
     */
    public String persistenceMethod = "local";
    /**
     * Configuration of the method to save the read/unread state
     */
    public String persistenceConfig = "";
    
    
    public static final int MAX_KILLTIME = 142560; // 99 days * 24 h * 60 min
    /**
     * The default kill time in minutes
     */
    public int killTime = 180;
    /**
     * The default maximum number of dials sending a fax
     */
    public int maxDial = 12;
    /**
     * The default maximum number of tries to send a fax
     */
    public int maxTry = 6;
    /**
     * The notification e-mail address sent to the HylaFAX server
     */
    public String notifyAddress = System.getProperty("user.name") +  "@localhost";
    /**
     * When to send a notification e-mail
     */
    public FaxNotification notifyWhen = FaxNotification.DONE_AND_REQUEUE;
    /**
     * The default paper size used to send faxes
     */
    public PaperSize paperSize = PaperSize.A4;
    /**
     * The default resolution used to send faxes 
     */
    public FaxResolution resolution = FaxResolution.HIGH;
    /**
     * The default modem to use
     */
    public String defaultModem = "any";
    /**
     * Archive sent faxes by default?
     */
    public boolean archiveSentFaxes = false;
    /**
     * Custom job options.
     */
    public final Map<String,String> customJobOptions = new TreeMap<String,String>();
    
    /**
     * Characters to filter out from the fax number.
     */
    public String filterFromFaxNr = "-/()[]{}";
    
    /**
     * A prefix prepended to the fax number before sending it to HylaFAX
     */
    public String numberPrefix = "";
    
    public ServerOptions(FaxOptions parent) {
        super(null, parent);
    }
    
    public ServerOptions(ServerOptions toClone) {
        super(null, toClone.parent, toClone.id);
        
        copyFrom(toClone);
    }
    
    @Override
    public String toString() {
        return name + " (" + user + '@' + host + ')';
    }

}
