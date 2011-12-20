package yajhfc.faxcover.tag;

/**
 * State for IfTags
 * @author jonas
 *
 */
public class ConditionState {
    public final boolean ifWasTaken;
    public boolean hadElse = false;
    
    public ConditionState(boolean ifWasTaken) {
        super();
        this.ifWasTaken = ifWasTaken;
    }
}