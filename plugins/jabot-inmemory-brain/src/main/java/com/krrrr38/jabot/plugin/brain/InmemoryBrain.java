package com.krrrr38.jabot.plugin.brain;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InmemoryBrain extends Brain {
    private final Map<String, Map<String, String>> brain = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getAll(String namespace) {
        return space(namespace);
    }

    @Override
    public Optional<String> get(String namespace, String key) {
        return Optional.ofNullable(space(namespace).get(key));
    }

    @Override
    public boolean store(String namespace, String key, String value) {
        brain.compute(namespace, (namespace1, space) -> {
            if (space == null) {
                space = new ConcurrentHashMap<>();
            }
            space.put(key, value);
            return space;
        });
        return true;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2) {
        store(namespace, key1, value1);
        store(namespace, key2, value2);
        return true;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) {
        store(namespace, key1, value1);
        store(namespace, key2, value2);
        store(namespace, key3, value3);
        return true;
    }

    @Override
    public boolean storeAll(String namespace, Map<String, String> keyvalues) {
        brain.compute(namespace, (namespace1, space) -> {
            if (space == null) {
                space = new ConcurrentHashMap<>();
            }
            space.putAll(keyvalues);
            return space;
        });
        return true;
    }

    @Override
    public boolean delete(String namespace, String key) {
        return space(namespace).remove(key) != null;
    }

    @Override
    public boolean clear(String namespace) {
        brain.put(namespace, new ConcurrentHashMap<>());
        return true;
    }

    @Override
    public boolean isStored(String namespace, String key) {
        return space(namespace).containsKey(key);
    }

    private Map<String, String> space(String namespace) {
        return brain.getOrDefault(namespace, Collections.emptyMap());
    }
}
