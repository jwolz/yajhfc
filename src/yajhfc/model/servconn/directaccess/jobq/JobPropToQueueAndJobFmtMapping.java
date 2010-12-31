package yajhfc.model.servconn.directaccess.jobq;

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
