package yajhfc.faxcover.tag;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.convrules.EntryToStringRule;

class RuleTag extends Tag {
    protected final boolean isFrom;
    protected final Field ruleField;
    
    @Override
    public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
        try {
            EntryToStringRule entryRule = (EntryToStringRule)ruleField.get(arg0);
            return entryRule.applyRule(isFrom ? arg0.fromData : arg0.toData);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error getting value", e);
            return "";
        }
    }

    protected RuleTag(String ruleFieldName, boolean isFrom) {
        super();
        this.isFrom = isFrom;
        Field field = null;
        try {
            field = Faxcover.class.getField(ruleFieldName);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Invalid field", e);
        }
        this.ruleField = field;
    }
}