package yajhfc;

import java.util.EventListener;

interface UnreadItemListener extends EventListener {
    public void newItemsAvailable(UnreadItemEvent evt);
}
