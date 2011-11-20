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
package yajhfc.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import yajhfc.FaxNotification;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.JobState;

/**
 * A default implementation for the IconMap interface
 * @author jonas
 *
 */
public class DefaultIconMap implements IconMap, Serializable {
    private static final long serialVersionUID = 1;
    
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
        final String cacheKey = fmtItem.name() + '|' + textData;
        IconMap res = instanceCache.get(cacheKey);
        if (res != null) {
            return res;
        }
        
        if (fmtItem == JobFormat.a || fmtItem == JobFormat.a_desc ||
                fmtItem == QueueFileFormat.state || fmtItem == QueueFileFormat.state_desc ) { // Mapping for job state
            JobState state;
            if (textData.length() == 0) {
                state = JobState.UNDEFINED;
            } else {
                if (fmtItem instanceof JobFormat) {
                    state = JobState.getJobStateFromCharCode(textData.charAt(0));
                } else {
                    state = JobState.getJobStateFromQueueCode(textData.charAt(0) - '0');
                }
            }

            if (fmtItem == JobFormat.a_desc || fmtItem == QueueFileFormat.state_desc) {
                res = state;
            } else {
                res = new DefaultIconMap(textData, state.getDisplayIcon(), state.getDescription());
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
                    res = original;
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
                    res = original;
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
