package yajhfc.faxcover.tag;

import java.util.List;

import yajhfc.faxcover.Faxcover;


public abstract class IfTag extends ConditionalTag { 
    protected abstract boolean evaluate(Faxcover arg0, List<ConditionState> conditionStack, String param);
    
    @Override
    public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
        if (param == null) {
            log.info("Found If without an parameter!");
            return "Found If without an parameter!";
        }
        boolean val = evaluate(arg0, conditionStack, param);
        conditionStack.add(new ConditionState(val));
        if (val) {
            return "";
        } else {
            return "<!-- ";
        }
    }
}