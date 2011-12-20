package yajhfc.faxcover.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;

/**
 * Tags (@@tagname@@) used for cover pages. These classes are especially tailored for MarkupFaxcover classes
 * but may also be used in other contexts if applicable.
 * @author jonas
 *
 */
public abstract class Tag {
    static final Logger log = Logger.getLogger(Tag.class.getName());
    

    public static final Map<String,Tag> availableTags = new HashMap<String,Tag>();
    static {
        // Tag names MUST be lower case to allow case insensitive comparison!
    
        // Sender
        availableTags.put("name", new RuleTag("nameRule", false));
        availableTags.put("surname", new PBFieldTag(PBEntryField.Name, false));
        availableTags.put("givenname", new PBFieldTag(PBEntryField.GivenName, false));
        availableTags.put("title", new PBFieldTag(PBEntryField.Title, false));
        availableTags.put("position", new PBFieldTag(PBEntryField.Position, false));
    
        availableTags.put("company", new RuleTag("companyRule", false));
        availableTags.put("companyname", new PBFieldTag(PBEntryField.Company, false));
        availableTags.put("department", new PBFieldTag(PBEntryField.Department, false));
    
        availableTags.put("location", new RuleTag("locationRule", false));
        availableTags.put("street", new PBFieldTag(PBEntryField.Street, false));
        availableTags.put("place", new PBFieldTag(PBEntryField.Location, false));
        availableTags.put("zipcode", new PBFieldTag(PBEntryField.ZIPCode, false));
        availableTags.put("state", new PBFieldTag(PBEntryField.State, false));
        availableTags.put("country", new PBFieldTag(PBEntryField.Country, false));
    
        availableTags.put("faxnumber", new PBFieldTag(PBEntryField.FaxNumber, false));
        availableTags.put("voicenumber", new PBFieldTag(PBEntryField.VoiceNumber, false));
        availableTags.put("email", new PBFieldTag(PBEntryField.EMailAddress, false)); 
        availableTags.put("website", new PBFieldTag(PBEntryField.WebSite, false));
    
        // Recipient:
        availableTags.put("fromname", new RuleTag("nameRule", true));
        availableTags.put("fromsurname", new PBFieldTag(PBEntryField.Name, true));
        availableTags.put("fromgivenname", new PBFieldTag(PBEntryField.GivenName, true));
        availableTags.put("fromtitle", new PBFieldTag(PBEntryField.Title, true));
        availableTags.put("fromposition", new PBFieldTag(PBEntryField.Position, true));
    
        availableTags.put("fromcompany", new RuleTag("companyRule", true));
        availableTags.put("fromcompanyname", new PBFieldTag(PBEntryField.Company, true));
        availableTags.put("fromdepartment", new PBFieldTag(PBEntryField.Department, true));
    
        availableTags.put("fromlocation", new RuleTag("locationRule", true));
        availableTags.put("fromstreet", new PBFieldTag(PBEntryField.Street, true));
        availableTags.put("fromplace", new PBFieldTag(PBEntryField.Location, true));
        availableTags.put("fromzipcode", new PBFieldTag(PBEntryField.ZIPCode, true));
        availableTags.put("fromstate", new PBFieldTag(PBEntryField.State, true));
        availableTags.put("fromcountry", new PBFieldTag(PBEntryField.Country, true));
    
        availableTags.put("fromfaxnumber", new PBFieldTag(PBEntryField.FaxNumber, true));
        availableTags.put("fromvoicenumber", new PBFieldTag(PBEntryField.VoiceNumber, true));
        availableTags.put("fromemail", new PBFieldTag(PBEntryField.EMailAddress, true));
        availableTags.put("fromwebsite", new PBFieldTag(PBEntryField.WebSite, true));
        
        // Misc. tags:
        availableTags.put("subject", new ReflectionTag("regarding"));
        availableTags.put("comments", new ReflectionTag("comments"));
        availableTags.put("date", new Tag() {
            @Override
            public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
                return arg0.dateFmt.format(arg0.coverDate);
            }
        });
        availableTags.put("pagecount", new ReflectionTag("pageCount"));
        availableTags.put("totalpagecount", new Tag() {
            @Override
            public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
                return String.valueOf(arg0.pageCount+1);
            }
        });
        availableTags.put("ccnameandfax", new Tag() {
           @Override
            public String getValue(Faxcover instance,
                    List<ConditionState> conditionStack, String param) {
               if (instance.ccData == null || instance.ccData.length == 0)
                   return "";
               
               StringBuilder res = new StringBuilder();
               for (PBEntryFieldContainer pbe : instance.ccData) {
                   if (pbe != null) {
                       instance.nameRule.applyRule(pbe, res);
                       res.append(" <");
                       res.append(pbe.getField(PBEntryField.FaxNumber));
                       res.append(">; ");
                   }
               }
               if (res.length() > 2)
                   return res.substring(0, res.length() - 2);
               else
                   return "";
            } 
        });
        
        // Conditionals:
        IfTag allFilled = new IfAllFilledTag();
        IfTag someFilled = new IfSomeFilledTag();
        availableTags.put("ifallfilled", allFilled);
        availableTags.put("ifsomefilled", someFilled);
        availableTags.put("ifallempty", new IfNotTag(someFilled));
        availableTags.put("ifsomeempty", new IfNotTag(allFilled));
        availableTags.put("else", new ElseTag());
        availableTags.put("endif", new EndIfTag());
    }


    /**
     * Returns the String the tag should be replaced with
     * @param instance
     * @param conditionStack
     * @param param
     * @return
     */
    public abstract String getValue(Faxcover instance, List<ConditionState> conditionStack, String param);
    
    /**
     * Determines if the value should be copied unmodified or if characters should be escaped
     * @return
     */
    public boolean valueIsRaw() {
        return false;
    }
}