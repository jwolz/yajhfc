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

import javax.swing.ImageIcon;

import yajhfc.model.SentYajJob;
import yajhfc.model.archive.ArchiveYajJob;
import yajhfc.model.archive.QueueFileFormat;

/**
 * A default implementation for the IconMap interface
 * @author jonas
 *
 */
public class DefaultIconMap implements IconMap {
    protected String text;
    protected ImageIcon displayIcon;
    protected String description;
    
    public String getText() {
        return text;
    }
    public ImageIcon getDisplayIcon() {
        return displayIcon;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return text;
    }
    
    public DefaultIconMap(String text, ImageIcon displayIcon, String description) {
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
        final String cacheKey = fmtItem.name() + "|" + textData;
        IconMap res = instanceCache.get(cacheKey);
        if (res != null) {
            return res;
        }
        
        if (fmtItem == JobFormat.a || fmtItem == JobFormat.a_desc ||
                fmtItem == QueueFileFormat.state || fmtItem == QueueFileFormat.state_desc ) { // Mapping for job state
            String filename;
            char state;
            if (textData.length() == 0) {
                state = '?';
            } else {
                if (fmtItem instanceof JobFormat) {
                    state = textData.charAt(0);
                } else {
                    state = ArchiveYajJob.mapArchiveStatusToOneCharCode(textData.charAt(0) - '0');
                }
            }
            if (state == '?') {
                filename = "jobstate_questionmark.gif";
            } else {
                filename = "jobstate_" + state + ".gif";
            }
            
            final String description = SentYajJob.getDescriptionForJobState(state);
            if (fmtItem == JobFormat.a_desc || fmtItem == QueueFileFormat.state_desc) {
                res = new DefaultIconMap(SentYajJob.getLabelForJobState(state), Utils.loadCustomIcon(filename), description);
            } else {
                res = new DefaultIconMap(textData, Utils.loadCustomIcon(filename), description);
            }
        } else if (fmtItem == JobFormat.n || fmtItem == JobFormat.n_desc) { // Mapping for notification state
            IconMap original;
            
            char c;
            if (textData.length() == 0) {
                c = ' ';
            } else {
              c = textData.charAt(0);  
            }
            switch (c) {
            case ' ':
                original = FaxNotification.NEVER; // never
                break;
            case 'D':
                original = FaxNotification.DONE; // done
                break;
            case 'Q':
                original = FaxNotification.REQUEUE; // requeue
                break;
            case 'A':
                original = FaxNotification.DONE_AND_REQUEUE; // all
                break;
            default:
                original = null;
            }
            if (original == null) {
                res = new DefaultIconMap(textData, null, null);
            } else {
                if (fmtItem == JobFormat.n_desc) {
                    res = new DefaultIconMap(original.getText(), original.getDisplayIcon(), original.getText());
                } else {
                    res = new DefaultIconMap(textData, original.getDisplayIcon(), original.getText());
                }
            }
        } else if (fmtItem == QueueFileFormat.notify || fmtItem == QueueFileFormat.notify_desc){
            IconMap original;
            
            if (textData.equals("none")) {
                original = FaxNotification.NEVER;
            } else if (textData.equals("when requeued")) {
                original = FaxNotification.REQUEUE;
            } else if (textData.equals("when done")) {
                original = FaxNotification.DONE;
            } else if (textData.equals("when done+requeued")) {
                original = FaxNotification.DONE_AND_REQUEUE;
            } else {
                original = null;
            }
            if (original == null) {
                res = new DefaultIconMap(textData, null, null);
            } else {
                if (fmtItem == QueueFileFormat.notify_desc) {
                    res = new DefaultIconMap(original.getText(), original.getDisplayIcon(), original.getText());
                } else {
                    res = new DefaultIconMap(textData, original.getDisplayIcon(), original.getText());
                }
            }
        } else {
            res = new DefaultIconMap(textData, null, null);
        }
        instanceCache.put(cacheKey, res);
        return res;
    }


}
