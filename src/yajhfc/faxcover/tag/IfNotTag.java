package yajhfc.faxcover.tag;

import java.util.List;

import yajhfc.faxcover.Faxcover;


class IfNotTag extends IfTag {
    protected final IfTag wrapped;
    
    @Override
    protected boolean evaluate(Faxcover arg0, List<ConditionState> arg1,
            String arg2) {
        return !wrapped.evaluate(arg0, arg1, arg2);
    }

    public IfNotTag(IfTag wrapped) {
        super();
        this.wrapped = wrapped;
    }
}