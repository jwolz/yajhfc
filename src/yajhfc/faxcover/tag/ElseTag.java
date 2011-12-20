package yajhfc.faxcover.tag;

import java.util.List;

import yajhfc.faxcover.Faxcover;


class ElseTag extends ConditionalTag {        
    @Override
    public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
        int size = conditionStack.size();
        if (size == 0) {
            log.warning("Found @@ELSE@@ without an if!");
            return "Found @@ELSE@@ without an if!";
        }
        
        ConditionState state = conditionStack.get(size-1);
        if (state.hadElse) {
            log.warning("Found more than one @@ELSE@@ for an IF!");
            return "Found more than one @@ELSE@@ for an IF!";
        } else {
            state.hadElse = true;
            if (state.ifWasTaken) { // Last if was taken
                return "<!-- ";
            } else {
                // Check if this else is embedded in an already commented out section:
                for (int i = size - 2; i >= 0; i--) {
                    state = conditionStack.get(i);
                    if (!state.ifWasTaken ^ state.hadElse) {
                        return "---";
                    }
                }
                return "-->";
            }
        }
    }
}