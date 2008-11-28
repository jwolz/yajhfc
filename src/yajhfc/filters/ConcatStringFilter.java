/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.filters;

/**
 * @author jonas
 *
 */
public class ConcatStringFilter<V extends FilterableObject, K extends FilterKey> extends AbstractStringFilter<V, K> {

    protected Object[] concatVals;
    protected Object[] resolvedConcatVals;
    protected Class<K> keyClass;
    
    /**
     * Creates a new ConcatStringFilter. When a match is performed the objects specified
     * in concatVals are concatenated first as follows:
     * If an element is instanceof keyClass, it is treated as column and the respective value is fetched,
     * else simply the element's toString() method is called
     * @param concatVals
     * @param operator
     * @param compareValue
     * @param caseSensitive
     */
    public ConcatStringFilter(Class<K> keyClass, Object[] concatVals, StringFilterOperator operator,
            String compareValue, boolean caseSensitive) {
        super(operator, compareValue, caseSensitive);
        this.concatVals = concatVals;
        this.keyClass = keyClass;
    }

    @SuppressWarnings("unchecked")
    public void initFilter(FilterKeyList<K> columns) {
        resolvedConcatVals = new Object[concatVals.length];
        for (int i=0; i < concatVals.length; i++) {
            Object val = concatVals[i];
            if (keyClass.isInstance(val)) {
                resolvedConcatVals[i] = columns.translateKey((K)val);
            } else {
                resolvedConcatVals[i] = null;
            }
        }
    }

    public boolean matchesFilter(V filterObj) {
        StringBuilder matchValue = new StringBuilder();
        for (int i=0; i < resolvedConcatVals.length; i++) {
            Object resolvedKey = resolvedConcatVals[i];
            if (resolvedKey == null) {
                matchValue.append(concatVals[i]);
            } else {
                matchValue.append(filterObj.getFilterData(resolvedKey));
            }
        }
        return doActualMatch(matchValue.toString());
    }

    @SuppressWarnings("unchecked")
    public boolean validate(FilterKeyList<K> columns) {
        for (int i=0; i < concatVals.length; i++) {
            Object val = concatVals[i];
            if (keyClass.isInstance(val)) {
                if (!columns.containsKey((K)val)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Object[] getConcatVals() {
        return concatVals;
    }
}
