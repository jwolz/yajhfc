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
package yajhfc.model.servconn;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import yajhfc.Utils;
import yajhfc.model.IconMap;

/**
 * @author jonas
 *
 */
public enum JobState implements IconMap {
    UNDEFINED('?', -1, Utils._("Undefined")),
    FAILED('F', 8, Utils._("Failed")),
    SUSPENDED('T', 1, Utils._("Suspended"), Utils._("Suspended (not being scheduled)")),
    PENDING('P', 2, Utils._("Pending"), Utils._("Pending (waiting for the time to send to arrive)")),
    SLEEPING('S', 3, Utils._("Sleeping"), Utils._("Sleeping (waiting for a scheduled timeout such as a delay between attempts to send)")),
    BLOCKED('B', 4, Utils._("Blocked"), Utils._("Blocked (by concurrent activity to the same destination)")),
    WAITING('W', 5, Utils._("Waiting"), Utils._("Waiting (for resources such as a free modem)")),
    RUNNING('R', 6, Utils._("Running")),
    DONE('D', 7, Utils._("Done"))
    ;
    
    private final char singleCharCode;
    private final int queueCode;
    private final String label;
    private final String description;
    private transient ImageIcon displayIcon = null;
    
    private JobState(char singleCharCode, int queueCode, String label) {
        this(singleCharCode, queueCode, label, label);
    }
    
    private JobState(char singleCharCode, int queueCode, String label,
            String description) {
        this.singleCharCode = singleCharCode;
        this.queueCode = queueCode;
        this.label = label;
        this.description = description;
    }

    public char getSingleCharCode() {
        return singleCharCode;
    }
    
    public int getQueueCode() {
        return queueCode;
    }
    
    
    public String getDescription() {
        return description;
    }

    public ImageIcon getDisplayIcon() {
        if (displayIcon == null) {
            String filename;
            if (singleCharCode == '?') {
                filename = "jobstate_questionmark.gif";
            } else {
                filename = "jobstate_" + singleCharCode + ".gif";
            }
            displayIcon = Utils.loadCustomIcon(filename);
        }
        return displayIcon;
    }

    public String getText() {
        return label;
    }
    
    @Override
    public String toString() {
        return label;
    }
        
    //From man doneq:
    // state: The job scheduling state.  Recognized values are:
    // 1 (suspended, not being scheduled),
    public static final int QUEUESTATE_SUSPENDED = 1;
    // 2 (pending, waiting for the time to send), 
    public static final int QUEUESTATE_PENDING = 2;
    // 3 (sleeping,  waiting for a scheduled timeout),
    public static final int QUEUESTATE_SLEEPING = 3;
    // 4 (blocked, waiting for concurrent activity to the same destination to complete),
    public static final int QUEUESTATE_BLOCKED = 4;
    // 5 (ready, ready to be processed except for available resources),
    public static final int QUEUESTATE_READY = 5;
    // 6 (active, actively being processed by HylaFAX),
    public static final int QUEUESTATE_ACTIVE = 6;
    // 7 (done,  processing completed with success).
    public static final int QUEUESTATE_DONE = 7;
    // 8 (failed, processing completed with a failure).
    public static final int QUEUESTATE_FAILED = 8; 
    
    // Unknown
    public static final int QUEUESTATE_UNKNOWN = -1;
    
    /**
     * Returns the job state corresponding to the given queue code
     * @param intState
     * @return
     */
    public static JobState getJobStateFromQueueCode(int intState) {
        switch (intState) { // Map to sent job one character code
        case QUEUESTATE_ACTIVE:
            return RUNNING;
        case QUEUESTATE_BLOCKED:
            return  BLOCKED;
        case QUEUESTATE_DONE:
            return  DONE;
        case QUEUESTATE_FAILED:
            return FAILED;
        case QUEUESTATE_PENDING:
            return PENDING;
        case QUEUESTATE_READY:
            return WAITING;
        case QUEUESTATE_SLEEPING:
            return SLEEPING;
        case QUEUESTATE_SUSPENDED:
            return SUSPENDED;
        default:
            return UNDEFINED;
        }
    }

    public static final char JOBSTATE_UNDEFINED = '?';
    public static final char JOBSTATE_FAILED = 'F';
    public static final char JOBSTATE_SUSPENDED = 'T';
    public static final char JOBSTATE_PENDING = 'P';
    public static final char JOBSTATE_SLEEPING = 'S';
    public static final char JOBSTATE_BLOCKED = 'B';
    public static final char JOBSTATE_WAITING = 'W';
    public static final char JOBSTATE_RUNNING = 'R';
    public static final char JOBSTATE_DONE = 'D';
    
    /**
     * Returns the job state for the given single character code
     * @param state
     * @return the job state 
     */
    public static JobState getJobStateFromCharCode(char state) {
        switch (state) {
        case JOBSTATE_BLOCKED:
            return BLOCKED;
        case JOBSTATE_DONE:
            return DONE;
        case JOBSTATE_FAILED:
            return FAILED;
        case JOBSTATE_PENDING:
            return PENDING;
        case JOBSTATE_RUNNING:
            return RUNNING;
        case JOBSTATE_SLEEPING:
            return SLEEPING;
        case JOBSTATE_SUSPENDED:
            return SUSPENDED;
        case JOBSTATE_UNDEFINED:
        default:
            return UNDEFINED;
        case JOBSTATE_WAITING:
            return WAITING;
        }
    }
    
    /**
     * Returns the job state for the given value 
     * @param jparmState a value returned from JPARM STATE
     * @return
     */
    public static JobState getJobStateFromJPARMValue(String jparmState) {
        try {
            if ("ACTIVE".equals(jparmState))
                return RUNNING;
            
            return Enum.valueOf(JobState.class, jparmState);
        } catch (Exception e) {
            Logger.getLogger(JobState.class.getName()).log(Level.INFO, "Unknown job state, returning UNDEFINED: " + jparmState);
            return UNDEFINED;
        }
    }
}
