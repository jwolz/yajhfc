package yajhfc.model.servconn.directaccess.jobq;
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

import java.util.HashMap;
import java.util.Map;

import yajhfc.model.JobFormat;
import yajhfc.model.jobq.QueueFileFormat;

/**
 * Defines a mapping of job properties as used in HylaFaxClient.getProperty()
 * to JobFormat/QueueFileFormats
 * 
 *  NOTE: Currently only the properties used to resend a fax are defined
 *            This list will be extended as necessary.
 * @author jonas
 *
 */
public class JobPropToQueueAndJobFmtMapping {
    /**
     * The mapped job property
     */
    public final String jobProperty;
    /**
     * The job format with the same information. May be null
     */
    public final JobFormat jobFormat;
    /**
     * The queue file property with the same information. May be null
     */
    public final QueueFileFormat queueFormat;
    
    
    JobPropToQueueAndJobFmtMapping(String jobProperty, JobFormat jobFormat,
            QueueFileFormat queueFormat) {
        super();
        this.jobProperty = jobProperty;
        this.jobFormat = jobFormat;
        this.queueFormat = queueFormat;
    }
    
    private static final Map<String,JobPropToQueueAndJobFmtMapping> map;
    static {
        map = new HashMap<String, JobPropToQueueAndJobFmtMapping>();
        put(new JobPropToQueueAndJobFmtMapping("DIALSTRING", JobFormat.v, QueueFileFormat.number));
        put(new JobPropToQueueAndJobFmtMapping("EXTERNAL", JobFormat.e, QueueFileFormat.external));
        put(new JobPropToQueueAndJobFmtMapping("TOUSER", JobFormat.R, QueueFileFormat.receiver));
        put(new JobPropToQueueAndJobFmtMapping("TOCOMPANY", JobFormat.C, QueueFileFormat.company));
        put(new JobPropToQueueAndJobFmtMapping("TOLOCATION", JobFormat.L, QueueFileFormat.location));
        put(new JobPropToQueueAndJobFmtMapping("TOVOICE", null, QueueFileFormat.voice));
        put(new JobPropToQueueAndJobFmtMapping("REGARDING", null, QueueFileFormat.regarding));
       
    }
    
    private static void put(JobPropToQueueAndJobFmtMapping mapping) {
        map.put(mapping.jobProperty, mapping);
    }
    
    /**
     * Returns the mapping for the specified job property
     * @param jobProperty
     * @return
     */
    public static JobPropToQueueAndJobFmtMapping getMappingFor(String jobProperty) {
        return map.get(jobProperty);
    }
}
