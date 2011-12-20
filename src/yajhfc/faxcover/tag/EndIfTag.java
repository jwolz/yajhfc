package yajhfc.faxcover.tag;

import java.util.List;

import yajhfc.faxcover.Faxcover;


class EndIfTag extends ConditionalTag {
    @Override
    public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
        int size = conditionStack.size();
        if (size == 0) {
            log.warning("Found @@ENDIF@@ without an if!");
            return "Found @@ENDIF@@ without an if!";
        }
        
        ConditionState lastState = conditionStack.remove(size-1);
        boolean writeEndComment = !lastState.ifWasTaken ^ lastState.hadElse;
        
        if (writeEndComment) {
            // Check if this end if is embedded in an already commented out section:
            for (ConditionState state : conditionStack) {
                if (!state.ifWasTaken ^ state.hadElse) {
                    return "---";
                }
            }
            return "-->";
        }

        return "";
    }
    
}