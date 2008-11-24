package yajhfc.phonebook.convrules;


public interface EntryToStringRule {

    /**
     * Converts the entries content to String using this rule
     * @param entry
     * @return
     */
    public abstract String applyRule(PBEntryFieldContainer entry);

    /**
     * Converts the entries content to String using this rule and appends the result
     * to the StringBuilder 
     * @param entry
     * @param appendTo
     * @return the number of characters appended
     */
    public abstract int applyRule(PBEntryFieldContainer entry, StringBuilder appendTo);

}