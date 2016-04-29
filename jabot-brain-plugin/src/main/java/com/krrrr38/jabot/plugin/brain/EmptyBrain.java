package com.krrrr38.jabot.plugin.brain;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public
class EmptyBrain extends Brain {
    @Override
    public void afterSetup(Map<String, String> options) {
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public Map<String, String> getAll(String namespace) throws JabotBrainException {
        return Collections.emptyMap();
    }

    @Override
    public Optional<String> get(String namespace, String key) throws JabotBrainException {
        return Optional.empty();
    }

    @Override
    public boolean store(String namespace, String key, String value) throws JabotBrainException {
        return false;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2) throws JabotBrainException {
        return false;
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) throws JabotBrainException {
        return false;
    }

    @Override
    public boolean storeAll(String namespace, Map<String, String> keyvalues) throws JabotBrainException {
        return false;
    }

    @Override
    public boolean delete(String namespace, String key) throws JabotBrainException {
        return false;
    }

    @Override
    public boolean clear(String namespace) throws JabotBrainException {
        return false;
    }

    @Override
    public boolean isStored(String namespace, String key) throws JabotBrainException {
        return false;
    }
}
