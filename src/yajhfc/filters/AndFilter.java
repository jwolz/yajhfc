package yajhfc.filters;
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
import java.util.ArrayList;
import java.util.List;


public class AndFilter<V extends FilterableObject, K extends FilterKey> implements Filter<V, K>, CombinationFilter<V, K> {

    protected ArrayList<Filter<V, K> > children = new ArrayList<Filter<V, K> >();
   
    /* (non-Javadoc)
     * @see yajhfc.filters.CombinationFilter#addChild(yajhfc.filters.Filter)
     */
    public void addChild(Filter<V, K>  child) {
        children.add(child);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.filters.CombinationFilter#childCount()
     */
    public int childCount() {
        return children.size();
    }
    
    /* (non-Javadoc)
     * @see yajhfc.filters.CombinationFilter#getChild(int)
     */
    public Filter<V, K>  getChild(int index) {
        return children.get(index);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.filters.CombinationFilter#getChildList()
     */
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
