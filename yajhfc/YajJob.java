package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.filters.FilterableObject;

public abstract class YajJob implements FilterableObject {
    private static final Logger log = Logger.getLogger(YajJob.class.getName());
    
    protected String[] stringData;
    protected Object[] parsedData;
    protected FmtItemList columns;
    // Placeholder to mark "null"-Values in parsedData:
    protected final static Object nullObject = new Object();
    
    public void setStringDataArray(String[] newData) {
        if (!Arrays.equals(newData, stringData)) {
            stringData = newData;
            Arrays.fill(parsedData, null);
        }
    }
    
    public String getStringData(int col) {
        if (col >= stringData.length && col < columns.getCompleteView().size())
            return "";
        else if (col >= columns.getCompleteView().size())
            throw new ArrayIndexOutOfBoundsException(col);
        else
            return stringData[col];
    }
    
    public Object getData(int col) {
        Object result;
        
        result = parsedData[col];
        
        if (result == null) { // Not parsed
            String res = getStringData(col);
            FmtItem fmtItem = columns.getCompleteView().get(col);
            parsedData[col] = result = parseValue(fmtItem, res);
        }        
        return (result == nullObject) ? null : result;
    }
    
    public Object getFilterData(Object key) {
        return getData((Integer)key);
    }
    
    /**
     * Parses the String value data into the dataClass of the given FmtItem.
     * This is called by getData, its result is cached so this is only called once
     * for each data value.
     * @param fmtItem
     * @param data
     * @return
     */
    protected Object parseValue(FmtItem fmtItem, String data) {
        if (data == null) {
            return  nullObject;
        } else {
            Class<?> dataClass = fmtItem.dataClass;

            if (dataClass == String.class) {
                return data;
            } else {
                data = data.trim();
                if (dataClass == Boolean.class)  { // "*" if true, " " otherwise
                    return data.equals("*");
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
                            Date d = fmtItem.dateFormat.fmtIn.parse(data);
                            if (d != null && utils.getFaxOptions().dateOffsetSecs != 0) {
                                Calendar cal = Calendar.getInstance(utils.getLocale());
                                cal.setTime(d);
                                cal.add(Calendar.SECOND, utils.getFaxOptions().dateOffsetSecs);
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
                        return  nullObject;
                    } catch (ParseException e) {
                        log.log(Level.WARNING, "Not a parseable date: " + data + ": ", e);
                        return  nullObject;
                    }    
                } else {
                    return nullObject;
                }
            }
        }
    }
    
    public FmtItemList getColumns() {
        return columns;
    }
    
    public void setColumns(FmtItemList columns) {
        this.columns = columns;
        parsedData = new Object[columns.getCompleteView().size()];
    }
    
    /**
     * Returns if this Job had an error.
     * @return
     */
    public boolean isError() {
        // Also update mainwin.MenuViewListener if this is changed!
        return false;
    }
    
    /**
     * Returns the associated Files for this Job.
     * @param hyfc
     * HylaFAXClient to use as connection.
     */
    public abstract List<HylaServerFile> getServerFilenames(HylaFAXClient hyfc) throws IOException, ServerResponseException;
    
    public abstract void delete(HylaFAXClient hyfc) throws IOException, ServerResponseException;
    
    /**
     * Returns a value that can be used as a identifier for this Job.
     */ 
    public abstract Object getIDValue();
    
    public YajJob(FmtItemList cols, String[] stringData) {
        setColumns(cols);
        setStringDataArray(stringData);
    }
}
