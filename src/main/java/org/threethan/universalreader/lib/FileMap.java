package org.threethan.universalreader.lib;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map which is tied to a file. All writes will be auto saved.<br>
 * The file will be read only on creation or when reloadFromFile() is called manually,
 * so implementations should use a static var to store the instance if they wish to stay in sync.
 * @param <K> Key type
 * @param <V> Value type
 * @author Ethan Medeiros
 */
@SuppressWarnings("NullableProblems")
public class FileMap<K extends Serializable, V extends Serializable> implements Map<K,V> {
    private ConcurrentHashMap<K, V> map = null; //ConcurrentHashMap should be thread-safe
    private final File file;

    /**
     * Creates a new FileMap object
     * @param file File to load from, and auto-save to.
     */
    public FileMap(File file) {
        this.file = file;
    }

    /** Get the map, loading if needed */
    private Map<K, V> getMap() {
        if (map == null) reloadFromFile();
        return map;
    }

    /** Reload from file */
    public void reloadFromFile() {
        if (map != null) map.clear();
        if (file.exists()) map = IOUtils.load(file);
        if (map == null) map = new ConcurrentHashMap<>();
    }

    /** Save to file */
    private void saveToFile() {
        IOUtils.save(file, map);
    }

    @Override
    public int size() {
        return getMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getMap().containsKey(key);
    }


    @Override
    public boolean containsValue(Object value) {
        return getMap().containsValue(value);
    }

    @Override
    @Deprecated // should use getOrDefault
    public V get(Object key) {
        return getMap().get(key);
    }

    @Override
    public V put(K key, V value) {
        V r = getMap().put(key, value);
        saveToFile();
        return r;
    }

    @Override
    public V remove(Object key) {
        V r = getMap().remove(key);
        saveToFile();
        return r;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        getMap().putAll(m);
        saveToFile();
    }

    @Override
    public void clear() {
        getMap().clear();
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    @Override
    public Set<K> keySet() {
        return getMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return getMap().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return getMap().entrySet();
    }
}
