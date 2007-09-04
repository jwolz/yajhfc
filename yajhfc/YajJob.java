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
import java.util.Vector;

public abstract class YajJob {
    protected String[] stringData;
    protected Object[] parsedData;
    protected Vector<FmtItem> columns;
    // Placeholder to mark "null"-Values in parsedData:
    protected final static Object nullObject = new Object();
    
    public void setStringDataArray(String[] newData) {
        if (!Arrays.equals(newData, stringData)) {
            stringData = newData;
            parsedData = new Object[newData.length];
        }
    }
    
    public String getStringData(int col) {
        if (col >= stringData.length && col < columns.size())
            return null;
        else if (col >= columns.size())
            throw new ArrayIndexOutOfBoundsException(col);
        else
            return stringData[col];
    }
    
    public Object getData(int col) {
        Object result;
        
        if (col >= stringData.length && col < columns.size())
            return null;
        else if (col >= columns.size())
            throw new ArrayIndexOutOfBoundsException(col);
        else
            result = parsedData[col];
        
        if (result == null) { // Not parsed
            String res = getStringData(col);
            Class<?> dataClass = columns.get(col).dataClass;
            
            if (dataClass == String.class) {
                result = res;
            } else {
                res = res.trim();
                if (dataClass == Boolean.class)  { // "*" if true, " " otherwise
                    result = res.equals("*");
                } else if (res.length() > 0) {
                    try {
                        if (dataClass == Integer.class)
                            result = Integer.valueOf(res);
                        else if (dataClass == Float.class)
                            result = Float.valueOf(res);
                        else if (dataClass == Double.class)
                            result = Double.valueOf(res);
                        else if (dataClass == Date.class) {
                            Date d = columns.get(col).dateFormat.fmtIn.parse(res);
                            if (d != null && utils.getFaxOptions().dateOffsetSecs != 0) {
                                Calendar cal = Calendar.getInstance(utils.getLocale());
                                cal.setTime(d);
                                cal.add(Calendar.SECOND, utils.getFaxOptions().dateOffsetSecs);
                                d = cal.getTime();
                            }
                            result = d;
                        } else
                            result = res;
                    } catch (NumberFormatException e) {
                        System.err.println("Not a number: " + res);
                        //result = Float.NaN;
                        result = nullObject;
                    } catch (ParseException e) {
                        System.err.println("Not a parseable date: " + res);
                        result = nullObject;
                    }    
                } else
                    result = nullObject;
            }
            parsedData[col] = result;
        }        
        return (result == nullObject) ? null : result;
    }
    
    public Vector<FmtItem> getColumns() {
        return columns;
    }
    
    public void setColumns(Vector<FmtItem> columns) {
        this.columns = columns;
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
    
    public YajJob(Vector<FmtItem> cols, String[] stringData) {
        setColumns(cols);
        setStringDataArray(stringData);
    }
}
