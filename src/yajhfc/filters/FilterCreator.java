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
import yajhfc.Utils;
import yajhfc.model.YajJob;

public class FilterCreator {
    //TODO: Try to generalize more...
    private static final Logger log = Logger.getLogger(FilterCreator.class.getName());
    
    private static String[] booleanOperators = {
        Utils._("is set"),
        Utils._("is not set")
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
            return new ComparableFilter<V,K>(column, (ComparableFilterOperator)selectedOperator, column.getDisplayDateFormat().parse(input));
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
            if (cf.getColumn().getDataType() == Date.class)
                return cf.getColumn().getDisplayDateFormat().format(cf.getCompareValue());
            else if (cf.getColumn().getDataType() == Boolean.class)
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
    public static <T extends FmtItem> String filterToString(Filter<YajJob<T>,T> filter) {
        if (filter == null || !(filter instanceof AndFilter))
            return null;
        
        StringBuffer res = new StringBuffer();
        if (filter instanceof OrFilter) {
            res.append('|');
        } else {
            res.append('&');
        }
        res.append('!');
        for (Filter<YajJob<T>,T> yjf: ((AndFilter<YajJob<T>,T>)filter).getChildList()) {
            if (yjf instanceof ComparableFilter) {
                ComparableFilter<YajJob<T>,T> cf = (ComparableFilter<YajJob<T>,T>)yjf;
                res.append("c$");
                res.append(cf.getColumn().getHylaFmt()).append('$');
                res.append(cf.getOperator().name()).append('$');
                String val;
                if (cf.getColumn().getDataType() == Date.class)
                    val = cf.getColumn().getHylaDateFormat().format((Date)cf.getCompareValue());
                else
                    val = cf.getCompareValue().toString();
                res.append(Utils.escapeChars(val, "$!", '~')).append('$');
                res.append('!');
            } else if (yjf instanceof StringFilter) {
                StringFilter<YajJob<T>,T> sf = (StringFilter<YajJob<T>,T>)yjf;
                res.append("s$");
                res.append(sf.getColumn().getHylaFmt()).append('$');
                res.append(sf.getOperator().name()).append('$');
                res.append(Utils.escapeChars(sf.getCompareValue().toString(), "$!", '~')).append('$');
                res.append('!');
            } else
                log.log(Level.WARNING, "Unknown filter type for filterToString: " + yjf.getClass().getName());
        }
        
        return res.toString();
    }
    
    @SuppressWarnings("unchecked")
    public static  <T extends FmtItem> Filter<YajJob<T>,T> stringToFilter(String spec, FmtItemList<T> columns) {
        String [] flt1 = Utils.fastSplit(spec, '!'); //spec.split("!");
        
        AndFilter af;
        if (flt1[0].equals("|")) {
            af = new OrFilter<YajJob<T>,T>();
        } else if (flt1[0].equals("&")) {
            af = new AndFilter<YajJob<T>,T>();
        } else {
            log.log(Level.WARNING, "Unknown And/Or specification in stringToFilter: " + flt1[0]);
            return null;
        }
        
        for (int i = 1; i < flt1.length; i++) {
            String[] flt2 = Utils.fastSplit(flt1[i], '$'); //flt1[i].split("\\$");
            if (flt2.length != 4) {
                log.log(Level.WARNING, "Unknown filter specification in stringToFilter: " + flt1[i]);
                continue;
            }
            
            T col = null;
            for (T fi: columns.getCompleteView()) {
                if (fi.getHylaFmt().equals(flt2[1])) {
                    col = fi;
                    break;
                }
            }
            if (col == null) {
                log.log(Level.WARNING, "Unknown column in stringToFilter: " + flt1[i]);
                continue;
            }
            
            if (flt2[0].equals("c")) {
                String strData = Utils.unEscapeChars(flt2[3], "$!", '~');
                Comparable compVal;
                Class<?> dataClass = col.getDataType();
                if (dataClass == Integer.class) 
                    compVal = Integer.valueOf(strData);
                else if (dataClass == Float.class)
                    compVal = Float.valueOf(strData);
                else if (dataClass == Double.class)
                    compVal = Double.valueOf(strData);
                else if (dataClass == Boolean.class)
                    compVal = Boolean.valueOf(strData);
                else if (dataClass == Date.class) {
                    try {
                        compVal = col.getHylaDateFormat().parse(strData);
                    } catch (ParseException e) {
                        log.log(Level.WARNING, "Unknown date format in stringToFilter: " + strData);
                        continue;
                    }
                } else {
                    log.log(Level.WARNING, "Unknown data class in stringToFilter: " + dataClass.getName());
                    continue;
                }                        
                
                try {
                    af.addChild(new ComparableFilter<YajJob<T>,T>(col, ComparableFilterOperator.valueOf(ComparableFilterOperator.class, flt2[2]), compVal));
                } catch (RuntimeException e) {
                    log.log(Level.WARNING, "Exception in stringToFilter: ", e);
                    continue;
                }
            } else if (flt2[0].equals("s")) {
                try {
                    af.addChild(new StringFilter<YajJob<T>,T>(col, StringFilterOperator.valueOf(StringFilterOperator.class, flt2[2]), Utils.unEscapeChars(flt2[3], "$!", '~'), true));
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
