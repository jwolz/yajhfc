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
package yajhfc.util;

import java.awt.Event;
import java.awt.Toolkit;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * @author jonas
 *
 */
public class AcceleratorKeys {
    public static final Map<String,String> DEFAULT_MAINWIN_MAPPING = new TreeMap<String,String>();
    public static final Map<String,String> DEFAULT_PBWIN_MAPPING = new TreeMap<String,String>();
    static {
        StringBuilder sb = new StringBuilder();
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if ((menuMask & Event.SHIFT_MASK) != 0) {
            sb.append("shift ");
        }
        if ((menuMask & Event.CTRL_MASK) != 0) {
            sb.append("ctrl ");
        }
        if ((menuMask & Event.META_MASK) != 0) {
            sb.append("meta ");
        }        
        if ((menuMask & Event.ALT_MASK) != 0) {
            sb.append("alt ");
        }
        String menuModifier = sb.toString();

        DEFAULT_MAINWIN_MAPPING.put("AdminMode","shift pressed F12");
        DEFAULT_MAINWIN_MAPPING.put("AnswerCall","pressed F11");
        DEFAULT_MAINWIN_MAPPING.put("ClipCopy",menuModifier + "pressed C");
        DEFAULT_MAINWIN_MAPPING.put("Delete","pressed DELETE");
        DEFAULT_MAINWIN_MAPPING.put("Exit",menuModifier + "pressed Q");
        DEFAULT_MAINWIN_MAPPING.put("FaxRead",menuModifier + "pressed R");
        DEFAULT_MAINWIN_MAPPING.put("FaxSave",menuModifier + "pressed S");
        DEFAULT_MAINWIN_MAPPING.put("Forward","pressed F4");
        DEFAULT_MAINWIN_MAPPING.put("Phonebook","pressed F9");
        DEFAULT_MAINWIN_MAPPING.put("Poll","pressed F3");
        DEFAULT_MAINWIN_MAPPING.put("PrintTable",menuModifier + "pressed P");
        DEFAULT_MAINWIN_MAPPING.put("Readme","pressed F1");
        DEFAULT_MAINWIN_MAPPING.put("Reconnect","pressed F12");
        DEFAULT_MAINWIN_MAPPING.put("Refresh","pressed F5");
        DEFAULT_MAINWIN_MAPPING.put("Resend","shift pressed F4");
        DEFAULT_MAINWIN_MAPPING.put("Resume","pressed F6");
        DEFAULT_MAINWIN_MAPPING.put("SaveAsPDF","shift " + menuModifier + "pressed S");
        DEFAULT_MAINWIN_MAPPING.put("SearchFax",menuModifier + "pressed F");
        DEFAULT_MAINWIN_MAPPING.put("Send","pressed F2");
        DEFAULT_MAINWIN_MAPPING.put("Suspend","pressed F7");
        DEFAULT_MAINWIN_MAPPING.put("ViewLog",menuModifier + "pressed L");
        DEFAULT_MAINWIN_MAPPING.put("view_custom",menuModifier+"shift pressed F");
        DEFAULT_MAINWIN_MAPPING.put("view_all",menuModifier+"pressed A");
        
        
        DEFAULT_PBWIN_MAPPING.put("Print",menuModifier + "pressed P");
        DEFAULT_PBWIN_MAPPING.put("Close","alt pressed F4");
        DEFAULT_PBWIN_MAPPING.put("SearchEntry",menuModifier + "pressed F");
        DEFAULT_PBWIN_MAPPING.put("RemoveEntry",menuModifier + "pressed DELETE");
        DEFAULT_PBWIN_MAPPING.put("AddEntry",menuModifier + "pressed N");
        DEFAULT_PBWIN_MAPPING.put("AddDistList",menuModifier + "shift pressed N");
        DEFAULT_PBWIN_MAPPING.put("FilterEntries",menuModifier+"shift pressed F");
        DEFAULT_PBWIN_MAPPING.put("ShowAllEntries",menuModifier+"pressed A");
    }
    
    public static void saveToMap(Map<String,String> map, Map<String,Action> availableActions) {
        map.clear();
        for (Action act : availableActions.values()) {
            KeyStroke ks = (KeyStroke)act.getValue(Action.ACCELERATOR_KEY);
            if (ks != null) {
                map.put((String)act.getValue(Action.ACTION_COMMAND_KEY), ks.toString());
            }
        }
    }
    
    public static void loadFromMap(Map<String,String> map, Map<String,Action> availableActions) {
        for (Action act : availableActions.values()) {
            String key = map.get(act.getValue(Action.ACTION_COMMAND_KEY));
            if (key == null) {
                act.putValue(Action.ACCELERATOR_KEY, null);
            } else {
                act.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
            }
        }
    }
}
