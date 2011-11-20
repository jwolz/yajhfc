package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
 */

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