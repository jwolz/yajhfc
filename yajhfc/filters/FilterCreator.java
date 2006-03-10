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
import java.util.Vector;

import yajhfc.FmtItem;
import yajhfc.YajJobFilter;
import yajhfc.utils;

public class FilterCreator {
    
    private static String[] booleanOperators = {
        utils._("is set"),
        utils._("is not set")
    };
    
    public static Object[] getOperators(Class<?> cl) {
        if (cl == Integer.class || cl == Float.class || cl == Date.class || cl == Double.class) {
            return ComparableFilterOperator.values();
        } else if (cl == String.class) {
            return StringFilterOperator.values();
        } else if (cl == Boolean.class) {
            return booleanOperators;
        } else
            return null;
    }
    
    public static boolean isInputEnabled(Class<?> cl) {
        return (cl != Boolean.class && cl != Void.class);
    }
    
    public static YajJobFilter getFilter(FmtItem column, Object selectedOperator, String input) throws ParseException {
        if (column.dataClass == Integer.class) {
            return new ComparableFilter(column, (ComparableFilterOperator)selectedOperator, Integer.valueOf(input));
        } else if (column.dataClass == Float.class) {
            return new ComparableFilter(column, (ComparableFilterOperator)selectedOperator, Float.valueOf(input));
        } else if (column.dataClass == Double.class) {
            return new ComparableFilter(column, (ComparableFilterOperator)selectedOperator, Double.valueOf(input));
        } else if (column.dataClass == Date.class) {
            return new ComparableFilter(column, (ComparableFilterOperator)selectedOperator, column.dateFormat.fmtOut.parse(input));
        } else if (column.dataClass == String.class) {
            return new StringFilter(column, (StringFilterOperator)selectedOperator, input);
        } else if (column.dataClass == Boolean.class) {
            return new ComparableFilter(column, ComparableFilterOperator.EQUAL, selectedOperator == booleanOperators[0]);
        } else
            return null;
    }
    
    public static FmtItem columnFromFilter(YajJobFilter filter) {
        if (filter instanceof ComparableFilter)
            return ((ComparableFilter)filter).getColumn();
        else if (filter instanceof StringFilter) {
            return ((StringFilter)filter).getColumn();
        } else
            return null;
    }
     
    public static Object operatorFromFilter(YajJobFilter filter) {
        if (filter instanceof ComparableFilter) {
            ComparableFilter cf = (ComparableFilter)filter;
            if (cf.getColumn().dataClass != Boolean.class)
                return cf.getOperator();
            else {
                if (((Boolean)cf.getCompareValue()).booleanValue())
                    return booleanOperators[0];
                else
                    return booleanOperators[1];
            }
        } else if (filter instanceof StringFilter) {
            return ((StringFilter)filter).getOperator();
        } else
            return null;
    }
    
    public static String inputFromFilter(YajJobFilter filter) {
        if (filter instanceof ComparableFilter) {
            ComparableFilter cf = (ComparableFilter)filter;
            if (cf.getColumn().dataClass == Date.class)
                return cf.getColumn().dateFormat.fmtOut.format(cf.getCompareValue());
            else if (cf.getColumn().dataClass == Boolean.class)
                return "";
            else
                return cf.getOperator().toString();
        } else if (filter instanceof StringFilter) {
            return ((StringFilter)filter).getCompareValue().toString();
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
    public static String filterToString(YajJobFilter filter) {
        if (filter == null || !(filter instanceof AndFilter))
            return null;
        
        StringBuffer res = new StringBuffer();
        if (filter instanceof OrFilter) {
            res.append('|');
        } else {
            res.append('&');
        }
        res.append('!');
        for (YajJobFilter yjf: ((AndFilter)filter).getChildList()) {
            if (yjf instanceof ComparableFilter) {
                ComparableFilter cf = (ComparableFilter)yjf;
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
                StringFilter sf = (StringFilter)yjf;
                res.append("s$");
                res.append(sf.getColumn().fmt).append('$');
                res.append(sf.getOperator().name()).append('$');
                res.append(utils.escapeChars(sf.getCompareValue().toString(), "$!", '~')).append('$');
                res.append('!');
            } else
                System.err.println("Unknown filter type for filterToString: " + yjf.getClass().getName());
        }
        
        return res.toString();
    }
    
    public static YajJobFilter stringToFilter(String spec, Vector<FmtItem> columns) {
        String [] flt1 = spec.split("!");
        
        AndFilter af;
        if (flt1[0].equals("|")) {
            af = new OrFilter();
        } else if (flt1[0].equals("&")) {
            af = new OrFilter();
        } else {
            System.err.println("Unknown And/Or specification in stringToFilter: " + flt1[0]);
            return null;
        }
        
        for (int i = 1; i < flt1.length; i++) {
            String[] flt2 = flt1[i].split("\\$");
            if (flt2.length != 4) {
                System.err.println("Unknown filter specification in stringToFilter: " + flt1[i]);
                continue;
            }
            
            FmtItem col = null;
            for (FmtItem fi: columns) {
                if (fi.fmt.equals(flt2[1])) {
                    col = fi;
                    break;
                }
            }
            if (col == null) {
                System.err.println("Unknown column in stringToFilter: " + flt1[i]);
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
                        System.err.println("Unknown date format in stringToFilter: " + strData);
                        continue;
                    }
                } else {
                    System.err.println("Unknown data class in stringToFilter: " + col.dataClass.getName());
                    continue;
                }                        
                
                try {
                    af.addChild(new ComparableFilter(col, ComparableFilterOperator.valueOf(ComparableFilterOperator.class, flt2[2]), compVal));
                } catch (RuntimeException e) {
                    System.err.println("Exception in stringToFilter: "  + e.toString());
                    continue;
                }
            } else if (flt2[0].equals("s")) {
                try {
                    af.addChild(new StringFilter(col, StringFilterOperator.valueOf(StringFilterOperator.class, flt2[2]), utils.unEscapeChars(flt2[3], "$!", '~')));
                } catch (RuntimeException e) {
                    System.err.println("Exception in stringToFilter: "  + e.toString());
                    continue;
                }
            } else {
                System.err.println("Unknown filter type in stringToFilter: " + flt1[i]);
                continue;
            }  
        }
     
        return af;
    }
}
