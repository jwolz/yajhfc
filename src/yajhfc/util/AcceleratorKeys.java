/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package yajhfc.util;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.KeyStroke;

import yajhfc.FaxOptions;

/**
 * @author jonas
 *
 */
public class AcceleratorKeys {
    public static Map<String,String> DEFAULT_MAPPING = new TreeMap<String,String>();
    static {
        DEFAULT_MAPPING.put("AdminMode","shift pressed F12");
        DEFAULT_MAPPING.put("AnswerCall","pressed F11");
        DEFAULT_MAPPING.put("ClipCopy","ctrl pressed C");
        DEFAULT_MAPPING.put("Delete","pressed DELETE");
        DEFAULT_MAPPING.put("Exit","ctrl pressed Q");
        DEFAULT_MAPPING.put("FaxRead","ctrl pressed R");
        DEFAULT_MAPPING.put("FaxSave","ctrl pressed S");
        DEFAULT_MAPPING.put("Forward","pressed F4");
        DEFAULT_MAPPING.put("Phonebook","pressed F9");
        DEFAULT_MAPPING.put("Poll","pressed F3");
        DEFAULT_MAPPING.put("PrintTable","ctrl pressed P");
        DEFAULT_MAPPING.put("Readme","pressed F1");
        DEFAULT_MAPPING.put("Reconnect","pressed F12");
        DEFAULT_MAPPING.put("Refresh","pressed F5");
        DEFAULT_MAPPING.put("Resend","shift pressed F4");
        DEFAULT_MAPPING.put("Resume","pressed F6");
        DEFAULT_MAPPING.put("SaveAsPDF","shift ctrl pressed S");
        DEFAULT_MAPPING.put("SearchFax","ctrl pressed F");
        DEFAULT_MAPPING.put("Send","pressed F2");
        DEFAULT_MAPPING.put("Suspend","pressed F7");
        DEFAULT_MAPPING.put("ViewLog","ctrl pressed L");
    }
    
    public static void saveToOptions(FaxOptions fo, Map<String,Action> availableActions) {
        saveToMap(fo.keyboardAccelerators, availableActions);
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

    public static void loadFromOptions(FaxOptions fo, Map<String,Action> availableActions) {
        loadFromMap(fo.keyboardAccelerators, availableActions);
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
