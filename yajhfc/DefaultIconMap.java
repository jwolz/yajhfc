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
    
    public String getText() {
        return text;
    }
    public Icon getDisplayIcon() {
        return displayIcon;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    public DefaultIconMap(String text, Icon displayIcon) {
        super();
        this.text = text;
        this.displayIcon = displayIcon;
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
            if (textData.equals("?")) {
                filename = "jobstate_questionmark.gif";
            } else {
                filename = "jobstate_" + textData + ".gif";
            }
            res = new DefaultIconMap(textData, utils.loadCustomIcon(filename));
        } else if (fmtItem.fmt.equals("n")) { // Mapping for notification state
            Icon icon;
            char c;
            if (textData.length() == 0) {
                c = ' ';
            } else {
              c = textData.charAt(0);  
            }
            switch (c) {
            case ' ':
                icon = utils.notifications[0].getDisplayIcon(); // never
                break;
            case 'D':
                icon = utils.notifications[1].getDisplayIcon(); // done
                break;
            case 'Q':
                icon = utils.notifications[2].getDisplayIcon(); // requeue
                break;
            case 'A':
                icon = utils.notifications[3].getDisplayIcon(); // all
                break;
            default:
                icon = null;
            }
            res = new DefaultIconMap(textData, icon);
        } else {
            res = new DefaultIconMap(textData, null);
        }
        instanceCache.put(cacheKey, res);
        return res;
    }


}
