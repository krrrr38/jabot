package com.krrrr38.jabot.plugin.brain;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class EmptyBrain extends Brain {
    @Override
    public Map<String, String> getAll(String namespace) {
        return Collections.emptyMap();
    }

    @Override
    public Optional<String> get(String namespace, String key) {
        return Optional.empty();
    }

    @Override
    public boolean store(String namespace, String key, String value) {
        return false;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2) {
        return false;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) {
        return false;
    }

    @Override
    public boolean storeAll(String namespace, Map<String, String> keyvalues) {
        return false;
    }

    @Override
    public boolean delete(String namespace, String key) {
        return false;
    }

    @Override
    public boolean clear(String namespace) {
        return false;
    }

    @Override
    public boolean isStored(String namespace, String key) {
        return false;
    }
}
