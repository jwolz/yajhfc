/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.model.servconn;

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
}
