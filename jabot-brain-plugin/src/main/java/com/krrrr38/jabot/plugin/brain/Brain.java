package com.krrrr38.jabot.plugin.brain;

import com.krrrr38.jabot.plugin.Plugin;

import java.util.Map;
import java.util.Optional;

abstract public class Brain implements Plugin {
    // XXX we should define BrainException which would be thrown by all methods.

    abstract public Map<String, String> getAll(String namespace);

    abstract public Optional<String> get(String namespace, String key);

    abstract public boolean store(String namespace, String key, String value);

    abstract public boolean store(String namespace, String key1, String value1, String key2, String value2);

    abstract public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3);

    abstract public boolean storeAll(String namespace, Map<String, String> keyvalues);

    abstract public boolean delete(String namespace, String key);

    abstract public boolean clear(String namespace);

    abstract public boolean isStored(String namespace, String key);
}
