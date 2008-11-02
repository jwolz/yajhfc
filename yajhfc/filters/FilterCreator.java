package yajhfc.filters;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.FmtItem;
import yajhfc.FmtItemList;
import yajhfc.IconMap;
import yajhfc.YajJob;
import yajhfc.utils;

public class FilterCreator {
    //TODO: Try to generalize more...
    private static final Logger log = Logger.getLogger(FilterCreator.class.getName());
    
    private static String[] booleanOperators = {
        utils._("is set"),
        utils._("is not set")
    };
    
    public static Object[] getOperators(Class<?> cl) {
        if (cl == Integer.class || cl == Float.class || cl == Date.class || cl == Double.class) {
            return ComparableFilterOperator.values();
        } else if (cl == String.class || cl == IconMap.class) {
            return StringFilterOperator.values();
        } else if (cl == Boolean.class) {
            return booleanOperators;
        } else
            return null;
    }
    
    public static boolean isInputEnabled(Class<?> cl) {
        return (cl != Boolean.class && cl != Void.class);
    }
    
    public static <V extends FilterableObject,K extends FmtItem> Filter<V,K> getFilter(K column, Object selectedOperator, String input) throws ParseException {
        Class<?> dataClass = column.getDataType();
        if (dataClass == Integer.class) {
            return new ComparableFilter<V,K>(column, (ComparableFilterOperator)selectedOperator, Integer.valueOf(input));
        } else if (dataClass == Float.class) {
            return new ComparableFilter<V,K>(column, (ComparableFilterOperator)selectedOperator, Float.valueOf(input));
        } else if (dataClass == Double.class) {
            return new ComparableFilter<V,K>(column, (ComparableFilterOperator)selectedOperator, Double.valueOf(input));
        } else if (dataClass == Date.class) {
            return new ComparableFilter<V,K>(column, (ComparableFilterOperator)selectedOperator, column.dateFormat.fmtOut.parse(input));
        } else if (dataClass == String.class || dataClass == IconMap.class) {
            return new StringFilter<V,K>(column, (StringFilterOperator)selectedOperator, input, true);
        } else if (dataClass == Boolean.class) {
            return new ComparableFilter<V,K>(column, ComparableFilterOperator.EQUAL, selectedOperator == booleanOperators[0]);
        } else
            return null;
    }
    
    public static <V extends FilterableObject,K extends FilterKey> K columnFromFilter(Filter<V,K> filter) {
        if (filter instanceof ComparableFilter)
            return ((ComparableFilter<V,K>)filter).getColumn();
        else if (filter instanceof StringFilter) {
            return ((StringFilter<V,K>)filter).getColumn();
        } else
            return null;
    }
     
    public static <V extends FilterableObject,K extends FilterKey> Object operatorFromFilter(Filter<V,K> filter) {
        if (filter instanceof ComparableFilter) {
            ComparableFilter<V,K> cf = (ComparableFilter<V,K>)filter;
            if (cf.getColumn().getDataType() != Boolean.class)
                return cf.getOperator();
            else {
                if (((Boolean)cf.getCompareValue()).booleanValue())
                    return booleanOperators[0];
                else
                    return booleanOperators[1];
            }
        } else if (filter instanceof StringFilter) {
            return ((StringFilter<V,K>)filter).getOperator();
        } else
            return null;
    }
    
    public static <V extends FilterableObject,K extends FmtItem> String inputFromFilter(Filter<V,K> filter) {
        if (filter instanceof ComparableFilter) {
            ComparableFilter<V,K> cf = (ComparableFilter<V,K>)filter;
            if (cf.getColumn().dataClass == Date.class)
                return cf.getColumn().dateFormat.fmtOut.format(cf.getCompareValue());
            else if (cf.getColumn().dataClass == Boolean.class)
                return "";
            else
                return cf.getCompareValue().toString();
        } else if (filter instanceof StringFilter) {
            return ((StringFilter<V,K>)filter).getCompareValue().toString();
        } else
            return "";
    }
    
    /**
     * "Convert" filter (an instance of AndFilter or OrFilter) into a String
     * for storage.
     * Format: (&|\|)!(c|s)$col$op$val!(c|s)col$op$val...!
     * @param filter
     * @return
     */
    public static String filterToString(Filter<YajJob,FmtItem> filter) {
        if (filter == null || !(filter instanceof AndFilter))
            return null;
        
        StringBuffer res = new StringBuffer();
        if (filter instanceof OrFilter) {
            res.append('|');
        } else {
            res.append('&');
        }
        res.append('!');
        for (Filter<YajJob,FmtItem> yjf: ((AndFilter<YajJob,FmtItem>)filter).getChildList()) {
            if (yjf instanceof ComparableFilter) {
                ComparableFilter<YajJob,FmtItem> cf = (ComparableFilter<YajJob,FmtItem>)yjf;
                res.append("c$");
                res.append(cf.getColumn().fmt).append('$');
                res.append(cf.getOperator().name()).append('$');
                String val;
                if (cf.getColumn().dataClass == Date.class)
                    val = cf.getColumn().dateFormat.fmtIn.format((Date)cf.getCompareValue());
                else
                    val = cf.getCompareValue().toString();
                res.append(utils.escapeChars(val, "$!", '~')).append('$');
                res.append('!');
            } else if (yjf instanceof StringFilter) {
                StringFilter<YajJob,FmtItem> sf = (StringFilter<YajJob,FmtItem>)yjf;
                res.append("s$");
                res.append(sf.getColumn().fmt).append('$');
                res.append(sf.getOperator().name()).append('$');
                res.append(utils.escapeChars(sf.getCompareValue().toString(), "$!", '~')).append('$');
                res.append('!');
            } else
                log.log(Level.WARNING, "Unknown filter type for filterToString: " + yjf.getClass().getName());
        }
        
        return res.toString();
    }
    
    @SuppressWarnings("unchecked")
    public static Filter<YajJob,FmtItem> stringToFilter(String spec, FmtItemList columns) {
        String [] flt1 = utils.fastSplit(spec, '!'); //spec.split("!");
        
        AndFilter af;
        if (flt1[0].equals("|")) {
            af = new OrFilter<YajJob,FmtItem>();
        } else if (flt1[0].equals("&")) {
            af = new AndFilter<YajJob,FmtItem>();
        } else {
            log.log(Level.WARNING, "Unknown And/Or specification in stringToFilter: " + flt1[0]);
            return null;
        }
        
        for (int i = 1; i < flt1.length; i++) {
            String[] flt2 = utils.fastSplit(flt1[i], '$'); //flt1[i].split("\\$");
            if (flt2.length != 4) {
                log.log(Level.WARNING, "Unknown filter specification in stringToFilter: " + flt1[i]);
                continue;
            }
            
            FmtItem col = null;
            for (FmtItem fi: columns.getCompleteView()) {
                if (fi.fmt.equals(flt2[1])) {
                    col = fi;
                    break;
                }
            }
            if (col == null) {
                log.log(Level.WARNING, "Unknown column in stringToFilter: " + flt1[i]);
                continue;
            }
            
            if (flt2[0].equals("c")) {
                String strData = utils.unEscapeChars(flt2[3], "$!", '~');
                Comparable compVal;
                if (col.dataClass == Integer.class) 
                    compVal = Integer.valueOf(strData);
                else if (col.dataClass == Float.class)
                    compVal = Float.valueOf(strData);
                else if (col.dataClass == Double.class)
                    compVal = Double.valueOf(strData);
                else if (col.dataClass == Boolean.class)
                    compVal = Boolean.valueOf(strData);
                else if (col.dataClass == Date.class) {
                    try {
                        compVal = col.dateFormat.fmtIn.parse(strData);
                    } catch (ParseException e) {
                        log.log(Level.WARNING, "Unknown date format in stringToFilter: " + strData);
                        continue;
                    }
                } else {
                    log.log(Level.WARNING, "Unknown data class in stringToFilter: " + col.dataClass.getName());
                    continue;
                }                        
                
                try {
                    af.addChild(new ComparableFilter<YajJob,FmtItem>(col, ComparableFilterOperator.valueOf(ComparableFilterOperator.class, flt2[2]), compVal));
                } catch (RuntimeException e) {
                    log.log(Level.WARNING, "Exception in stringToFilter: ", e);
                    continue;
                }
            } else if (flt2[0].equals("s")) {
                try {
                    af.addChild(new StringFilter<YajJob,FmtItem>(col, StringFilterOperator.valueOf(StringFilterOperator.class, flt2[2]), utils.unEscapeChars(flt2[3], "$!", '~'), true));
                } catch (RuntimeException e) {
                    log.log(Level.WARNING, "Exception in stringToFilter: ",  e);
                    continue;
                }
            } else {
                log.log(Level.WARNING, "Unknown filter type in stringToFilter: " + flt1[i]);
                continue;
            }  
        }
     
        return af;
    }
}
