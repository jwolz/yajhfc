package yajhfc.faxcover.tag;

import java.util.List;

import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.PBEntryField;

class PBFieldTag extends Tag {
    protected final boolean isFrom;
    protected final PBEntryField field;
    
    @Override
    public String getValue(Faxcover arg0, List<ConditionState> conditionStack, String param) {
        return (isFrom ? arg0.fromData : arg0.toData).getField(field);
    }

    protected PBFieldTag(PBEntryField field, boolean isFrom) {
        super();
        this.field = field;
        this.isFrom = isFrom;
    }
}