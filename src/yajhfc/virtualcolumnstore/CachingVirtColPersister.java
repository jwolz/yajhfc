package yajhfc.virtualcolumnstore;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import yajhfc.model.VirtualColumnType;

public abstract class CachingVirtColPersister extends VirtColPersister {

    protected Map<VirtualColumnType,Integer> columnMap;
    protected Map<String,Object[]> data;
    protected boolean dirty = false;

    public CachingVirtColPersister() {
        super();
        buildColumnMap();
    }

    /**
     * Checks if the values have been loaded
     */
    protected abstract void checkInitialized();
    
    /* (non-Javadoc)
     * @see yajhfc.virtualcolumnstore.VirtColPersister#prepareReadStates()
     */
    @Override
    public void prepareValues() {
        checkInitialized();
    }
    
    protected VirtualColumnType[] getReverseMap() {
        VirtualColumnType[] res = new VirtualColumnType[columnMap.size()];
        for (Entry<VirtualColumnType,Integer> entry : columnMap.entrySet()) {
            res[entry.getValue().intValue()] = entry.getKey();
        }
        return res;
    }

    protected int columnToIndex(VirtualColumnType column) {
        return columnMap.get(column).intValue();
    }

    @Override
    public synchronized Object getValue(String key, VirtualColumnType column) {
        checkInitialized();
        
        Object[] keyData = data.get(key);
        if (keyData == null)
            return null;
        else
            return keyData[columnToIndex(column)];
    }

    @Override
    public synchronized void setValue(String key, VirtualColumnType column, Object value) {
        checkInitialized();
        
        Object[] keyData = data.get(key);
        if (keyData == null)
            keyData = allocateKeyData();
    
        int idx = columnToIndex(column);
        Object oldValue = keyData[idx];
        if (value != oldValue && (oldValue == null || !oldValue.equals(value))) {
            keyData[idx] = value;
            data.put(key, keyData);
            dirty = true;
            valueChanged(key, column, idx, value, oldValue);
        }
    }
    
    protected void valueChanged(String key, VirtualColumnType column, int columnIndex, Object value, Object oldValue) {
        
    }

    protected Object[] allocateKeyData() {
        return new Object[columnMap.size()];
    }

    private void buildColumnMap() {
        columnMap = new EnumMap<VirtualColumnType, Integer>(VirtualColumnType.class);
        
        for (VirtualColumnType vtc : VirtualColumnType.values()) {
            if (vtc.isSaveable()) {
                columnMap.put(vtc, Integer.valueOf(columnMap.size()));
            }
        }
    }

}