package yajhfc.faxcover.tag;

import java.util.List;

import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;

class IfSomeFilledTag extends IfTag {
    @Override
    protected boolean evaluate(Faxcover arg0, List<ConditionState> conditionStack, String param) {
        String[] childTags = Utils.fastSplit(param, ',');
        for (String sTag : childTags) {
            Tag tag = Tag.availableTags.get(sTag);
            String tagValue = null;
            if (tag != null) {
                tagValue = tag.getValue(arg0, conditionStack, null);
            }
            if (tagValue != null && tagValue.length() > 0) {
                return true;
            }
        }
        return false;
    }
}