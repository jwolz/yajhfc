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
package yajhfc.model.servconn.directaccess.jobq;

import java.text.DateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import yajhfc.model.FmtItemList;
import yajhfc.model.JobFormat;
import yajhfc.model.jobq.QueueFileFormat;
import yajhfc.model.servconn.FaxJob;

/**
 * This class implements a mapping of JobFormats to the corresponding QueueFileFormats
 * @author jonas
 *
 */
public class JobToQueueMapping {
    protected final JobFormat jobFmt;
    protected final QueueFileFormat[] sourceProps;
    
    public Object mapParsedData(FaxJob<QueueFileFormat> job) {
        return job.getData(sourceProps[0]);
    }
    
    public JobFormat getJobFormat() {
        return jobFmt;
    }
    
    public QueueFileFormat[] getSourceProperties() {
        return sourceProps;
    }
    
    protected JobToQueueMapping(JobFormat jobFmt, QueueFileFormat... sourceProps) {
        super();
        this.jobFmt = jobFmt;
        this.sourceProps = sourceProps;
    }

    private static final Map<JobFormat, JobToQueueMapping> jobToQueueMap = new EnumMap<JobFormat, JobToQueueMapping>(JobFormat.class);
    static {
        put(new JobToQueueMapping(JobFormat.A, QueueFileFormat.subaddr));
        put(new JobToQueueMapping(JobFormat.B, QueueFileFormat.passwd));
        put(new JobToQueueMapping(JobFormat.C, QueueFileFormat.company));
        put(new StringFormatMapping(JobFormat.D, "%2d:%-2d", QueueFileFormat.totdials, QueueFileFormat.maxdials));
        put(new JobToQueueMapping(JobFormat.E, QueueFileFormat.desiredbr));
        put(new JobToQueueMapping(JobFormat.F, QueueFileFormat.tagline));
        put(new JobToQueueMapping(JobFormat.G, QueueFileFormat.desiredst));
        put(new JobToQueueMapping(JobFormat.H, QueueFileFormat.desireddf));
        put(new JobToQueueMapping(JobFormat.I, QueueFileFormat.schedpri));
        put(new JobToQueueMapping(JobFormat.J, QueueFileFormat.jobtag));
        put(new CharLookupMapping(JobFormat.K, "D HF", QueueFileFormat.desiredec));
        put(new JobToQueueMapping(JobFormat.L, QueueFileFormat.location));
        put(new JobToQueueMapping(JobFormat.M, QueueFileFormat.mailaddr));
        put(new JobToQueueMapping(JobFormat.N, QueueFileFormat.desiredtl));
        put(new JobToQueueMapping(JobFormat.O, QueueFileFormat.useccover));
        put(new StringFormatMapping(JobFormat.P, "%2d:%-2d", QueueFileFormat.npages, QueueFileFormat.totpages));
        put(new JobToQueueMapping(JobFormat.Q, QueueFileFormat.minbr));
        put(new JobToQueueMapping(JobFormat.R, QueueFileFormat.receiver));
        put(new JobToQueueMapping(JobFormat.S, QueueFileFormat.sender));
        put(new StringFormatMapping(JobFormat.T, "%2d:%-2d", QueueFileFormat.tottries, QueueFileFormat.maxtries));
        put(new JobToQueueMapping(JobFormat.U, QueueFileFormat.chopthreshold));
        put(new JobToQueueMapping(JobFormat.V, QueueFileFormat.doneop));
        put(new JobToQueueMapping(JobFormat.W, QueueFileFormat.commid));
        put(new JobToQueueMapping(JobFormat.X, QueueFileFormat.jobtype) {
            @Override
            public Object mapParsedData(FaxJob<QueueFileFormat> job) {
                String data = (String)job.getData(QueueFileFormat.jobtype);
                if (data == null || data.length() == 0)
                    return "";
                else
                    return String.valueOf(Character.toUpperCase(data.charAt(0)));
            }
        });
        put(new JobToQueueMapping(JobFormat.Y, QueueFileFormat.tts));
        put(new JobToQueueMapping(JobFormat.Z, QueueFileFormat.tts));
        put(new JobToQueueMapping(JobFormat.a, QueueFileFormat.state)); 
        put(new JobToQueueMapping(JobFormat.a_desc, QueueFileFormat.state_desc));
        put(new JobToQueueMapping(JobFormat.b, QueueFileFormat.ntries));
        put(new JobToQueueMapping(JobFormat.c, QueueFileFormat.client));
        put(new JobToQueueMapping(JobFormat.d, QueueFileFormat.totdials));
        put(new JobToQueueMapping(JobFormat.e, QueueFileFormat.external));
        put(new JobToQueueMapping(JobFormat.f, QueueFileFormat.ndials));
        put(new JobToQueueMapping(JobFormat.g, QueueFileFormat.groupid));
        put(new JobToQueueMapping(JobFormat.h, QueueFileFormat.pagechop) {
            @Override
            public Object mapParsedData(FaxJob<QueueFileFormat> job) {
                String data = (String)job.getData(QueueFileFormat.jobtype);
                if (data == null || data.length() == 0) {
                    return " ";
                } else {
                    if ("default".equals(data)) {
                        return "D";
                    } else if ("all".equals(data)) {
                        return "A";
                    } else if ("last".equals(data)) {
                        return "L";
                    } else {
                        return " ";
                    }
                }
            }
        });
        put(new JobToQueueMapping(JobFormat.i, QueueFileFormat.priority));
        put(new JobToQueueMapping(JobFormat.j, QueueFileFormat.jobid));
        put(new DateFormatMapping(JobFormat.k, QueueFileFormat.killtime, DateFormat.getTimeInstance()));
        put(new JobToQueueMapping(JobFormat.l, QueueFileFormat.pagelength));
        put(new JobToQueueMapping(JobFormat.m, QueueFileFormat.modem));
        put(new JobToQueueMapping(JobFormat.n, QueueFileFormat.notify));
        put(new JobToQueueMapping(JobFormat.n_desc, QueueFileFormat.notify_desc));
        put(new JobToQueueMapping(JobFormat.o, QueueFileFormat.owner));
        put(new JobToQueueMapping(JobFormat.p, QueueFileFormat.npages));
        put(new JobToQueueMapping(JobFormat.q, QueueFileFormat.retrytime) {
            @Override
            public Object mapParsedData(FaxJob<QueueFileFormat> job) {
                Number value = (Number)job.getData(sourceProps[0]);
                if (value != null && value.intValue() > 0) {
                    return new Date(value.longValue() * 1000);
                } else
                    return null;
            }
        });
        put(new JobToQueueMapping(JobFormat.r, QueueFileFormat.resolution));
        put(new JobToQueueMapping(JobFormat.s, QueueFileFormat.status));
        put(new JobToQueueMapping(JobFormat.t, QueueFileFormat.tottries));
        put(new JobToQueueMapping(JobFormat.u, QueueFileFormat.maxtries));
        put(new JobToQueueMapping(JobFormat.v, QueueFileFormat.number));
        put(new JobToQueueMapping(JobFormat.w, QueueFileFormat.pagewidth));
        put(new JobToQueueMapping(JobFormat.x, QueueFileFormat.maxdials));
        put(new JobToQueueMapping(JobFormat.y, QueueFileFormat.totpages));
        put(new DateFormatMapping(JobFormat.z, QueueFileFormat.tts, DateFormat.getDateTimeInstance()));
        put(new JobToQueueMapping(JobFormat._0, QueueFileFormat.usexvres));
        
    }
    
    private static void put(JobToQueueMapping mapping) {
        jobToQueueMap.put(mapping.getJobFormat(), mapping);
    }
    
    public static JobToQueueMapping getMappingFor(JobFormat jf) {
        return jobToQueueMap.get(jf);
    }
    
    public static void getRequiredFormats(FmtItemList<JobFormat> src, FmtItemList<QueueFileFormat> dst) {
        dst.clear();
        for (JobFormat fmt : src.getCompleteView()) {
            JobToQueueMapping mapping = jobToQueueMap.get(fmt);
            for (QueueFileFormat qfmt : mapping.getSourceProperties()) {
                dst.add(qfmt);
            }
        }
    }
    
    static class CharLookupMapping extends JobToQueueMapping {
        protected final String lookupString;
        
        protected CharLookupMapping(JobFormat hylaFmt, String lookupString,
                QueueFileFormat sourceProps) {
            super(hylaFmt, sourceProps);
            this.lookupString = lookupString;
        }
        
        @Override
        public Object mapParsedData(FaxJob<QueueFileFormat> job) {
            Number value = (Number)job.getData(sourceProps[0]);
            if (value != null)
                return String.valueOf(lookupString.charAt(value.intValue()));
            else
                return null;
        }
    }
    
    
    static class StringFormatMapping extends JobToQueueMapping {
        protected final String formatString;
        
        protected StringFormatMapping(JobFormat hylaFmt, String formatString,
                QueueFileFormat... sourceProps) {
            super(hylaFmt, sourceProps);
            this.formatString = formatString;
        }
        
        @Override
        public Object mapParsedData(FaxJob<QueueFileFormat> job) {
            Object[] data = new Object[sourceProps.length];
            for (int i=0; i<data.length; i++) {
                data[i] = job.getData(sourceProps[i]);
            }
            return String.format(formatString, data);
        }
    }
    
    static class DateFormatMapping extends JobToQueueMapping {
        protected final DateFormat format;
        
        protected DateFormatMapping(JobFormat jobFmt,
                QueueFileFormat sourceProp, DateFormat format) {
            super(jobFmt, sourceProp);
            this.format = format;
        }
        
        @Override
        public Object mapParsedData(FaxJob<QueueFileFormat> job) {
            Date value = (Date)job.getData(sourceProps[0]);
            if (value != null)
                return format.format(value);
            else
                return null;
        }
    }
}
