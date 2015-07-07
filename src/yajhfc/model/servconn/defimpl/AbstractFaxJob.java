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
import yajhfc.model.VirtualColumnType;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.model.servconn.FaxJobList;
import yajhfc.model.servconn.HylafaxWorker;
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
    
    public void setData(T column, Object value) {
        setData(column, value, true);
    }
    
    public void setData(T column, Object value, boolean fireEvent) {
        setData(parent.getColumns().getCompleteView().indexOf(column), value, fireEvent);
    }
    
    public void setData(int columnIndex, Object value) {
        setData(columnIndex, value, true);
    }
    
    public void setData(int columnIndex, Object value, boolean fireEvent) {
        T column = parent.getColumns().getCompleteView().get(columnIndex);
        if (column.isReadOnly())
            throw new UnsupportedOperationException("Column " + column.name() + " is read only!");
        if (value != null && !column.getDataType().isInstance(value))
            throw new ClassCastException("value is of type " + value.getClass() + ", but column " + column.name() + " has type " + column.getDataType());
        
        Object oldValue = data[columnIndex];
        if (oldValue != value && (oldValue == null || !oldValue.equals(value))) {
            data[columnIndex] = value;
            
            if (fireEvent) {
                parent.fireColumnChanged(this, column, columnIndex, oldValue, value);
            }
        }
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
    
    public Object doHylafaxWork(HylafaxWorker worker)
            throws IOException, ServerResponseException {
        throw new UnsupportedOperationException("Not a HylaFAX job");
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

    
    private static int parseInt(String s) {
        long v = Long.parseLong(s);
        if (v <= (long)Integer.MAX_VALUE) {
            return (int)v;
        } else {
            // Make two's complement of value and return it as a negative integer
            return -((int)(v ^ 0xffffffffl) + 1);
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
                            return Integer.valueOf(parseInt(data));
                        else if (dataClass == Float.class)
                            return  Float.valueOf(data);
                        else if (dataClass == Double.class)
                            return  Double.valueOf(data);
                        else if (dataClass == Date.class) {
                            Date d = fmtItem.getHylaDateFormat().parse(data);
                            if (d != null && parent.getParent().getOptions().dateOffsetSecs != 0) {
                                Calendar cal = Calendar.getInstance(Utils.getLocale());
                                cal.setTime(d);
                                cal.add(Calendar.SECOND, parent.getParent().getOptions().dateOffsetSecs);
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
    
    public JobState getCurrentJobState() {
        return getJobState();
    }
    
    public boolean isRead() {
        final int readColumn = parent.getColumns().getVirtualColumnIndex(VirtualColumnType.READ);
        if (readColumn < 0)
            throw new UnsupportedOperationException("This type of fax job doe not support a read/unread state!");
 
        final Boolean isRead = (Boolean)getData(readColumn);
        return (isRead != null && isRead.booleanValue());
    }

    public void setRead(boolean isRead) {
        setRead(isRead, true);
    }

    public void setRead(boolean isRead, boolean fireEvent) {
        final int readColumn = parent.getColumns().getVirtualColumnIndex(VirtualColumnType.READ);
        if (readColumn < 0) {
            //throw new UnsupportedOperationException("This type of fax job does not support a read/unread state!");
            log.fine("This type of fax job does not support a read/unread state!");
            return;
        }
        setData(readColumn, Boolean.valueOf(isRead), fireEvent);
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
        Object id = getIDValue();
        return (id == null) ? "null" : id.toString();
    }
    
}