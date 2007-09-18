/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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
package yajhfc;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

/**
 * A default implementation for the IconMap interface
 * @author jonas
 *
 */
public class DefaultIconMap implements IconMap {
    protected String text;
    protected Icon displayIcon;
    protected String description;
    
    public String getText() {
        return text;
    }
    public Icon getDisplayIcon() {
        return displayIcon;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    public DefaultIconMap(String text, Icon displayIcon, String description) {
        super();
        this.text = text;
        this.displayIcon = displayIcon;
        this.description = description;
    }
    
    /**
     * Cache to re-use instances because a lot of identical ones will be requested
     */
    protected static final Map<String,IconMap> instanceCache = new HashMap<String,IconMap>();
    
    public static IconMap getInstance(FmtItem fmtItem, String textData) {
        String cacheKey = fmtItem.fmt + "|" + textData;
        IconMap res = instanceCache.get(cacheKey);
        if (res != null) {
            return res;
        }
        
        if (fmtItem.fmt.equals("a")) { // Mapping for job state
            String filename;
            char state;
            if (textData.length() == 0) {
                state = '?';
            } else {
                state = textData.charAt(0);
            }
            if (state == '?') {
                filename = "jobstate_questionmark.gif";
            } else {
                filename = "jobstate_" + state + ".gif";
            }
            res = new DefaultIconMap(textData, utils.loadCustomIcon(filename), SentYajJob.getDescriptionForJobState(state));
        } else if (fmtItem.fmt.equals("n")) { // Mapping for notification state
            IconMap original;
            
            char c;
            if (textData.length() == 0) {
                c = ' ';
            } else {
              c = textData.charAt(0);  
            }
            switch (c) {
            case ' ':
                original = utils.notifications[0]; // never
                break;
            case 'D':
                original = utils.notifications[1]; // done
                break;
            case 'Q':
                original = utils.notifications[2]; // requeue
                break;
            case 'A':
                original = utils.notifications[3]; // all
                break;
            default:
                original = null;
            }
            if (original == null) {
                res = new DefaultIconMap(textData, null, null);
            } else {
                res = new DefaultIconMap(textData, original.getDisplayIcon(), original.getText());   
            }
        } else {
            res = new DefaultIconMap(textData, null, null);
        }
        instanceCache.put(cacheKey, res);
        return res;
    }


}
