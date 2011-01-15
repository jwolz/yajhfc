/**
 * 
 */
package yajhfc;

import java.util.logging.Logger;

import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * @author jonas
 *
 */
public class SenderIdentity extends IDAndNameOptions implements PBEntryFieldContainer {
    
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
     * The sender's fax number for the cover page
     */
    public String FromFaxNumber = "";
    /**
     * The sender's voice number for the cover page
     */
    public String FromVoiceNumber = "";
    /**
     * The sender's name for the cover page
     */
    public String FromName = System.getProperty("user.name");
    /**
     * The sender's location for the cover page
     */
    public String FromLocation = "";
    /**
     * The sender's company for the cover page
     */
    public String FromCompany = "";
    
    /**
     * The default cover page to use
     */
    public String defaultCover = null;
    /**
     * "true" if the cover page specified by defaultCover shall be used, "false" to use the internal default cover
     */
    public boolean useCustomDefaultCover = false;
    
    
    @SuppressWarnings("fallthrough")
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
    
    @SuppressWarnings("fallthrough")
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
    
    public SenderIdentity(FaxOptions parent) {
        super(null, parent);
    }
    
    public SenderIdentity(SenderIdentity toClone) {
        super(null, toClone.parent, toClone.id);
        copyFrom(toClone);
    }
}
