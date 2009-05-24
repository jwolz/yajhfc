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
import java.util.ArrayList;
import java.util.List;


public class AndFilter<V extends FilterableObject, K extends FilterKey> implements Filter<V, K> {

    protected ArrayList<Filter<V, K> > children = new ArrayList<Filter<V, K> >();
   
    public void addChild(Filter<V, K>  child) {
        children.add(child);
    }
    
    public int childCount() {
        return children.size();
    }
    
    public Filter<V, K>  getChild(int index) {
        return children.get(index);
    }
    
    public List<Filter<V, K> > getChildList() {
        return children;
    }
    
    public boolean matchesFilter(V filterObj) {
        if (children.size() == 0)
            return true;
        
        for (Filter<V, K>  yjf: children) {
            if (!yjf.matchesFilter(filterObj))
                return false;
        }
        return true;
    }

    public void initFilter(FilterKeyList<K> columns) {
        for (Filter<V, K>  yjf: children) {
            yjf.initFilter(columns);
        }
    }

    public boolean validate(FilterKeyList<K> columns) {
        for (int i = children.size()-1; i >= 0; i--) {
            if (!children.get(i).validate(columns)) {
                children.remove(i);
            }
        }
        return (children.size() > 0);
    }
    
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        this.toString(res);
        return res.toString();
    }
    
    protected String getToStringSymbol() {
        return "AND";
    }
    
    public void toString(StringBuilder appendTo) {
        for (int i = 0; i < children.size(); i++) {
            appendTo.append('(');
            children.get(i).toString(appendTo);
            appendTo.append(')');
            if (i < children.size() - 1) {
                appendTo.append(' ').append(getToStringSymbol()).append(' ');
            }
        }
    }
    
    public AndFilter() {
        super();
    }
    
    public AndFilter(Filter<V, K>... children) {
        for (Filter<V,K> f : children) {
            addChild(f);
        }
    }
}
