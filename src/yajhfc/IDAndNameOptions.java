package yajhfc;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

public abstract class IDAndNameOptions extends AbstractFaxOptions {

    /**
     * This element's id
     */
    public int id;
    /**
     * This element's display name
     */
    public String name;
    /**
     * The parent options
     */
    protected final FaxOptions parent;

    @Override
    public void loadFromProperties(Properties p, String prefix) {
        super.loadFromProperties(p, prefix);
        newIDLoaded(id);
    }
    
    protected IDAndNameOptions(String propertyPrefix, FaxOptions parent, int id) {
        super(propertyPrefix);
        this.parent = parent;
        this.id = id;
        this.name = getClass().getSimpleName() + '-' + id;
    }
    
    public IDAndNameOptions(String propertyPrefix, FaxOptions parent) {
        this(propertyPrefix, parent, getNewID());
    }

    private static int maxID = 0;

    public FaxOptions getParent() {
        return parent;
    }

    public void generateNewID() {
        id = getNewID();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        IDAndNameOptions other = (IDAndNameOptions)obj;
        return (other.id == id) && (other.getClass() == getClass());
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public String toString() {
        return name;
    }

    /**
     * Generates a new ID
     * @return
     */
    protected static synchronized int getNewID() {
        return ++maxID;
    }

    /**
     * This method must be called when a new id has been loaded
     * @param id
     */
    protected static synchronized void newIDLoaded(int id) {
        if (id > maxID)
            maxID = id;
    }

    
    /**
     * Returns the item having the specified id or null if none with that id could be found
     * @param id
     * @return
     */
    public static <E extends IDAndNameOptions> E getItemByID(Collection<E> items, int id) {
        for (E item : items) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Returns the item having the specified name or null if none with that id could be found
     * @param id
     * @return
     */
    public static <E extends IDAndNameOptions> E getItemByName(Collection<E> items, String name) {
        for (E item : items) {
            if (name.equals(item.name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Returns the item having the specified name or null if none with that id could be found
     * @param id
     * @return
     */
    public static <E extends IDAndNameOptions> E getItemByNameIgnoreCase(Collection<E> items, String name) {
        for (E item : items) {
            if (name.equalsIgnoreCase(item.name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Returns the item having an encoding as found on the command line:
     * Specify either the name (e.g. \"My identity\"), the list index (0 is the first element) or the ID in the form \"#ID\".
     * @param id
     * @return
     */
    public static <E extends IDAndNameOptions> E getItemFromCommandLineCoding(List<E> items, String name) {
        if (name.startsWith("#")) {
            try {
                return getItemByID(items, Integer.parseInt(name.substring(1)));
            } catch (NumberFormatException e) {
                return getItemByName(items, name);
            }
        } else if (Utils.isStringNumeric(name)) {
            try {
                return items.get(Integer.parseInt(name));
            } catch (Exception e) {
                return null;
            }
        } else {
            return getItemByName(items, name);
        }
    }

    /**
     * Returns true if there are duplicate IDs in the list
     * @param list
     * @return
     */
    public static boolean checkForDuplicates(List<? extends IDAndNameOptions> list) {
        for (int i=0; i<list.size()-1; i++) {
            IDAndNameOptions o1 = list.get(i);
            for (int j=i+1; j<list.size(); j++) {
                IDAndNameOptions o2 = list.get(j);
                if (o1.id == o2.id) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Removes duplicates from the list by removing the items with the larger index.
     * @param list
     * @return the number of removed duplicates
     */
    public static int removeDuplicates(List<? extends IDAndNameOptions> list) {
        int rv = 0;
        for (int i=0; i<list.size()-1; i++) {
            IDAndNameOptions o1 = list.get(i);
            for (int j=i+1; j<list.size(); j++) {
                IDAndNameOptions o2 = list.get(j);
                if (o1.id == o2.id) {
                    list.remove(j);
                    rv++;
                }
            }
        }
        return rv;
    }
}