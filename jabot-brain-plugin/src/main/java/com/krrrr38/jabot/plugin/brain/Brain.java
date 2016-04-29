package com.krrrr38.jabot.plugin.brain;

import java.util.Map;
import java.util.Optional;

import com.krrrr38.jabot.plugin.Plugin;

abstract public class Brain extends Plugin {
    private String namespace;
    private String botName;

    @Override
    protected String getNamespace() {
        return namespace;
    }

    /**
     * build settings. this method is called once when starting application.
     *
     * @param botName botname
     * @param options brain options
     */
    public void setup(String namespace, String botName, Map<String, String> options) throws JabotBrainException {
        if (botName == null || botName.isEmpty()) {
            botName = "jabot";
        }
        this.namespace = namespace;
        this.botName = botName;
        afterSetup(options);
    }

    /**
     * @return botname
     */
    protected String getBotName() {
        return botName;
    }

    /**
     * get namespace all keys &amp; values
     *
     * @param namespace plugin namespace
     * @return all keys &amp; values
     * @throws JabotBrainException brain exception
     */
    abstract public Map<String, String> getAll(String namespace) throws JabotBrainException;

    /**
     * get namespace value of param key
     *
     * @param namespace plugin namespace
     * @param key       stored key
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public Optional<String> get(String namespace, String key) throws JabotBrainException;

    /**
     * set namespace value of param key
     *
     * @param namespace plugin namespace
     * @param key       stored key
     * @param value     stored value
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean store(String namespace, String key, String value) throws JabotBrainException;

    /**
     * set namespace values of param keys
     *
     * @param namespace plugin namespace
     * @param key1      stored key1
     * @param value1    stored value1
     * @param key2      stored key2
     * @param value2    stored value2
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean store(String namespace, String key1, String value1, String key2, String value2) throws JabotBrainException;

    /**
     * set namespace values of param keys
     *
     * @param namespace plugin namespace
     * @param key1      stored key1
     * @param value1    stored value1
     * @param key2      stored key2
     * @param value2    stored value2
     * @param key3      stored key3
     * @param value3    stored value3
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) throws JabotBrainException;

    /**
     * set namespace values of param keys
     *
     * @param namespace plugin namespace
     * @param keyvalues stored key values
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean storeAll(String namespace, Map<String, String> keyvalues) throws JabotBrainException;

    /**
     * delete namespace value of param key
     *
     * @param namespace plugin namespace
     * @param key       stored key
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean delete(String namespace, String key) throws JabotBrainException;

    /**
     * clear namespace all keys &amp; values
     *
     * @param namespace plugin namespace
     * @return success or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean clear(String namespace) throws JabotBrainException;

    /**
     * check the value of param key is stored
     *
     * @param namespace plugin namespace
     * @param key       stored key
     * @return stored or not
     * @throws JabotBrainException brain exception
     */
    abstract public boolean isStored(String namespace, String key) throws JabotBrainException;
}
