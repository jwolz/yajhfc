/**
 * 
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
    
    public ServerOptions(FaxOptions parent) {
        super(null, parent);
    }
    
    public ServerOptions(ServerOptions toClone) {
        super(null, toClone.parent, toClone.id);
        
        copyFrom(toClone);
    }
    
    public void copyFrom(ServerOptions toClone) {
        this.AdminPassword.setObfuscatedPassword(toClone.AdminPassword.getObfuscatedPassword());
        this.archiveLocation = toClone.archiveLocation;
        this.askAdminPassword = toClone.askAdminPassword;
        this.askPassword = toClone.askPassword;
        this.askUsername = toClone.askUsername;
        this.customModems.clear();
        this.customModems.addAll(toClone.customModems);
        this.dateOffsetSecs = toClone.dateOffsetSecs;
        this.defaultIdentity = toClone.defaultIdentity;
        this.directAccessSpoolPath = toClone.directAccessSpoolPath;
        this.faxListConnectionType = toClone.faxListConnectionType;
        this.host = toClone.host;
        this.hylaFAXCharacterEncoding = toClone.hylaFAXCharacterEncoding;
        this.id = toClone.id;
        this.name = toClone.name;
        this.pass.setObfuscatedPassword(toClone.pass.getObfuscatedPassword());
        this.pasv = toClone.pasv;
        this.persistenceConfig = toClone.persistenceConfig;
        this.persistenceMethod = toClone.persistenceMethod;
        this.port = toClone.port;
        this.showArchive = toClone.showArchive;
        this.tzone = toClone.tzone;
        this.useCustomModems = toClone.useCustomModems;
        this.useDisconnectedMode = toClone.useDisconnectedMode;
        this.user = toClone.user;
    }
    
    @Override
    public String toString() {
        return name + " (" + user + '@' + host + ')';
    }

}
