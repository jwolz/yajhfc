package yajhfc.faxcover.tag;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import yajhfc.faxcover.Faxcover;


class ReflectionTag extends Tag {
    protected final Field field;

    @Override
    public String getValue(Faxcover instance, List<ConditionState> conditionStack, String param) {
        try {
            return field.get(instance).toString();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error getting value", e);
            return "";
        }
    }

    protected ReflectionTag(String fieldName) {
        super();
        Field rField = null;
        try {
            rField = Faxcover.class.getField(fieldName);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Invalid field", e);
        }
        this.field = rField;
    }

}