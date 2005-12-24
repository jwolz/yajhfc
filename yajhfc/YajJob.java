package yajhfc;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
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
                        else if (dataClass == Date.class)
                            result = columns.get(col).dateFormat.fmtIn.parse(res);
                        else
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
