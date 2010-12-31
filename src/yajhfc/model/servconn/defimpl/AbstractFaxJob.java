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
package yajhfc.model.servconn.defimpl;

import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.model.DefaultIconMap;
import yajhfc.model.FmtItem;
import yajhfc.model.IconMap;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.JobState;

public abstract class AbstractFaxJob<T extends FmtItem> implements SerializableFaxJob<T> {
    static final Logger log = Logger.getLogger(AbstractFaxJob.class.getName());
    private static final long serialVersionUID = 1;
    
    protected transient AbstractFaxJobList<T> parent;
    protected Object[] data;
    protected String[] rawData;
    protected List<FaxDocument> documents;
    protected List<String> inaccessibleDocuments = null;
    protected JobState state;
    protected boolean read;

    public FaxDocument getCommunicationsLog() throws IOException {
        return null;
    }

    public Object getData(T column) {
        return getData(parent.getColumns().getCompleteView().indexOf(column));
    }

    public Object getFilterData(Object key) {
        return getData((Integer)key);
    }

    public Object getData(int columnIndex) {
        return data[columnIndex];
    }
    
    /**
     * Returns the "raw" value of the given column or null if an invalid column is given
     * @param columnIndex
     * @return
     */
    public String getRawData(T column) {
        return getRawData(parent.getColumns().getCompleteView().indexOf(column));
    }
    
    /**
     * Returns the "raw" value of the given column or null if an invalid column is given
     * @param columnIndex
     * @return
     */
    public String getRawData(int columnIndex) {
        if (rawData == null || columnIndex < 0 || columnIndex >= rawData.length) {
            return null;
        } else {
            return rawData[columnIndex];
        }
    }
    
    public Map<String, String> getJobProperties(String... properties) {
        return null;
    }

    public Collection<FaxDocument> getDocuments() throws IOException, ServerResponseException {
        return getDocuments(null);
    }

    public Collection<FaxDocument> getDocuments(Collection<String> inaccessibleDocs) throws IOException,
            ServerResponseException {
                if (documents == null) {
                    documents = calcDocuments();
                    if (Utils.debugMode)
                        log.fine("Calculated documents for " + getIDValue() + ": " + documents + "; inaccessibleDocs=" + inaccessibleDocuments);
                }
                if (inaccessibleDocs != null && inaccessibleDocuments != null) {
                    inaccessibleDocs.addAll(inaccessibleDocuments);
                }
                return documents;
            }

    /**
     * Calculate (create) the list of documents. The return value is cached later.
     * 
     * If there are any inaccessible documents, create and fill the attribute inaccessibleDocuments here.
     * @return
     */
    protected abstract List<FaxDocument> calcDocuments();

    public abstract Object getIDValue();

    public FaxJobList<T> getParent() {
        return parent;
    }
    
    public void setParent(FaxJobList<T> parent) {
        this.parent = (AbstractFaxJobList<T>)parent;
    }

    public boolean isError() {
        switch (getJobState()) {
        case FAILED:
        case UNDEFINED:
            return true;
        default:
            return false;
        }
    }    

    /**
     * Parses the String value data into the dataClass of the given FmtItem.
     * This is called by getData, its result is cached so this is only called once
     * for each data value.
     * @param fmtItem
     * @param data
     * @return
     */
    protected Object parseValue(T fmtItem, String data) {
        if (data == null) {
            return null;
        } else {
            Class<?> dataClass = fmtItem.getDataType();
    
            if (dataClass == String.class) {
                return data;
            } else {
                data = data.trim();
                if (dataClass == Boolean.class)  { // "*" if true, " " or "N" otherwise
                    //return data.equals("*");
                    // " " would be reduced to "" by trim (-> length() == 0 in this case)
                    return (data.length() == 1 && !data.equals("N"));
                } else if (dataClass == IconMap.class) {
                    return DefaultIconMap.getInstance(fmtItem, data);
                } else if (data.length() > 0) {
                    try {
                        if (dataClass == Integer.class)
                            return Integer.valueOf(Integer.parseInt(data));
                        else if (dataClass == Float.class)
                            return  Float.valueOf(data);
                        else if (dataClass == Double.class)
                            return  Double.valueOf(data);
                        else if (dataClass == Date.class) {
                            Date d = fmtItem.getHylaDateFormat().parse(data);
                            if (d != null && Utils.getFaxOptions().dateOffsetSecs != 0) {
                                Calendar cal = Calendar.getInstance(Utils.getLocale());
                                cal.setTime(d);
                                cal.add(Calendar.SECOND, Utils.getFaxOptions().dateOffsetSecs);
                                d = cal.getTime();
                            }
                            return  d;
                        } else if (dataClass == Long.class) {
                            return Long.valueOf(Long.parseLong(data));
                        } else {
                            log.info("Unsupported data class: " + dataClass);
                            return data;
                        }
                    } catch (NumberFormatException e) {
                        log.log(Level.WARNING, "Not a number: " + data + ": ", e);
                        //return  Float.NaN;
                        return null;
                    } catch (ParseException e) {
                        log.log(Level.WARNING, "Not a parseable date for column '" + fmtItem + "': " + data + ": ", e);
                        return null;
                    }    
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Reloads the data from the given String array
     * @param strData
     */
    protected void reloadData(String[] strData) {
        if (Utils.debugMode) {
            log.finest("Raw data is: " + Arrays.toString(strData));
        }
        rawData = strData;
        List<T> completeView = parent.getColumns().getCompleteView();
        data = new Object[completeView.size()];
        for (int i = 0; i < data.length ; i++) {
            if (i < strData.length) {
                data[i] = parseValue(completeView.get(i), strData[i]);
            } else {
                data[i] = null;
            }
        }
        if (Utils.debugMode) {
            log.finest("Parsed data is: " + Arrays.toString(data));
        }
        state = calculateJobState();
    }

    /**
     * Calculates this job's state
     * @param data
     * @return
     */
    protected abstract JobState calculateJobState();
    
    public JobState getJobState() {
        return state;
    }
    
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean isRead) {
        if (read != isRead) {
            read = isRead;
            parent.fireReadStateChanged(this, !isRead, isRead);
        }
    }

    public void initializeRead(boolean isRead) {
        read = isRead;
    }
    
    protected AbstractFaxJob(AbstractFaxJobList<T> parent) {
        this.parent = parent;
    }
    
    protected AbstractFaxJob(AbstractFaxJobList<T> parent, String[] data) {
        this.parent = parent;
        reloadData(data);
    }
    
    @Override
    public String toString() {
        return getIDValue().toString();
    }
    
}