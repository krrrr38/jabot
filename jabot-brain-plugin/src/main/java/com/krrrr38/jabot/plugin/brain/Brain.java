package com.krrrr38.jabot.plugin.brain;

import com.krrrr38.jabot.plugin.Plugin;

import java.util.Map;
import java.util.Optional;

abstract public class Brain implements Plugin {
    private String botName;

    /**
     * build settings. this method is called once when starting application.
     * @param botName
     * @param options
     */
    public void setup(String botName, Map<String, String> options) throws JabotBrainException{
        if (botName == null || botName.isEmpty()) {
            botName = "jabot";
        }
        this.botName = botName;

        build(options);
    }

    protected String getBotName() {
        return botName;
    }

    /**
     * build settings. this method is called once when starting application.
     *
     * @param options
     */
    protected abstract void build(Map<String, String> options) throws JabotBrainException;

    abstract public Map<String, String> getAll(String namespace) throws JabotBrainException;

    abstract public Optional<String> get(String namespace, String key) throws JabotBrainException;

    abstract public boolean store(String namespace, String key, String value) throws JabotBrainException;

    abstract public boolean store(String namespace, String key1, String value1, String key2, String value2) throws JabotBrainException;

    abstract public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) throws JabotBrainException;

    abstract public boolean storeAll(String namespace, Map<String, String> keyvalues) throws JabotBrainException;

    abstract public boolean delete(String namespace, String key) throws JabotBrainException;

    abstract public boolean clear(String namespace) throws JabotBrainException;

    abstract public boolean isStored(String namespace, String key) throws JabotBrainException;
}
