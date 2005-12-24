package yajhfc;

import java.util.EventObject;
import java.util.Set;

class UnreadItemEvent extends EventObject {
    private Set<Object> items = null;
    private boolean oldDataNull;
    
    public Set<Object> getItems() {
        return items;
    }
    
    public boolean isOldDataNull() {
        return oldDataNull;
    }
    
    public UnreadItemEvent(Object source, Set<Object> items, boolean oldDataNull) {
        super(source);
        this.items = items;
        this.oldDataNull = oldDataNull;
    }
}
