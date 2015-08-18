package com.krrrr38.jabot.plugin.brain;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InmemoryBrain extends Brain {
    private Map<String, Map<String, String>> brain;

    @Override
    protected void build(Map<String, String> options) throws JabotBrainException {
        brain = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, String> getAll(String namespace) throws JabotBrainException {
        return space(namespace);
    }

    @Override
    public Optional<String> get(String namespace, String key) throws JabotBrainException {
        return Optional.ofNullable(space(namespace).get(key));
    }

    @Override
    public boolean store(String namespace, String key, String value) throws JabotBrainException {
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
    public boolean store(String namespace, String key1, String value1, String key2, String value2) throws JabotBrainException {
        store(namespace, key1, value1);
        store(namespace, key2, value2);
        return true;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) throws JabotBrainException {
        store(namespace, key1, value1);
        store(namespace, key2, value2);
        store(namespace, key3, value3);
        return true;
    }

    @Override
    public boolean storeAll(String namespace, Map<String, String> keyvalues) throws JabotBrainException {
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
    public boolean delete(String namespace, String key) throws JabotBrainException {
        return space(namespace).remove(key) != null;
    }

    @Override
    public boolean clear(String namespace) throws JabotBrainException {
        brain.put(namespace, new ConcurrentHashMap<>());
        return true;
    }

    @Override
    public boolean isStored(String namespace, String key) throws JabotBrainException {
        return space(namespace).containsKey(key);
    }

    private Map<String, String> space(String namespace) {
        return brain.getOrDefault(namespace, Collections.emptyMap());
    }
}
