package yajhfc.filters;

import java.util.List;

/**
 * A filter combining the results of multiple "child" filters (e.g. by logical AND or OR)
 * @author jonas
 *
 * @param <V>
 * @param <K>
 */
public interface CombinationFilter<V extends FilterableObject, K extends FilterKey> extends Filter<V,K> {

    public void addChild(Filter<V, K> child);

    public int childCount();

    public Filter<V, K> getChild(int index);

    public List<Filter<V, K>> getChildList();

}