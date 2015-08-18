package com.krrrr38.jabot.plugin.handler;

import com.krrrr38.jabot.plugin.Plugin;
import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

abstract public class Handler implements Plugin {
    private String namespace;
    private Brain brain;
    private Consumer<String> sender;
    private volatile List<Rule> rules;

    public void setup(String namespace, Brain brain, Consumer<String> sender, Map<String, String> options) {
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("namespace required");
        }
        if (brain == null) {
            throw new IllegalArgumentException("brain required");
        }
        if (sender == null) {
            throw new IllegalArgumentException("sender required");
        }
        this.namespace = namespace;
        this.brain = brain;
        this.sender = sender;
        this.rules = build(options);
    }

    /**
     * @param message
     * @return
     */
    public final Optional<String> receive(String message) {
        // foldLeft[B](z: B)(f: (B, A) => B): B
        return rules.stream().reduce(
                Optional.of(message),
                (maybeMessage, rule) -> maybeMessage.flatMap(rule::apply),
                (s, s2) -> Optional.empty()); // should not be called parallel
    }

    /**
     * Get Handler Rules
     *
     * @return rules
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * build rules. this method is called once when starting application.
     *
     * @param options
     * @return
     */
    abstract List<Rule> build(Map<String, String> options);

    /**
     * Hook and Modify handler comparing with other handlers.
     *
     * @param handlers all handlers
     */
    abstract public void afterRegister(List<Handler> handlers);

    ///////////////////////////////////////////////////////////////////////
    //                         handler utilities                         //
    ///////////////////////////////////////////////////////////////////////

    /**
     * send message through adapter
     *
     * @param message
     */
    protected void send(String message) {
        sender.accept(message);
    }

    /**
     * store value into namespace brain
     *
     * @param key
     * @param value
     */
    protected boolean store(String key, String value) throws JabotBrainException {
        return brain.store(namespace, key, value);
    }

    /**
     * store value into namespace brain
     *
     * @param key1
     * @param value1
     * @param key2
     * @param value2
     */
    protected boolean store(String key1, String value1, String key2, String value2) throws JabotBrainException {
        return brain.store(namespace, key1, value1, key2, value2);
    }

    /**
     * store value into namespace brain
     *
     * @param key1
     * @param value1
     * @param key2
     * @param value2
     * @param key3
     * @param value3
     */
    protected boolean store(String key1, String value1, String key2, String value2, String key3, String value3) throws JabotBrainException {
        return brain.store(namespace, key1, value1, key2, value2, key3, value3);
    }

    /**
     * get all values from namespace brain
     *
     * @return
     */
    protected Map<String, String> getAll() throws JabotBrainException {
        Map<String, String> result = brain.getAll(namespace);
        return result != null ? result : Collections.emptyMap();
    }

    /**
     * get value from namespace brain
     *
     * @param key
     * @return
     */
    protected Optional<String> get(String key) throws JabotBrainException {
        return brain.get(namespace, key);
    }

    /**
     * delete value from namespace brain
     *
     * @param key
     * @return
     */
    protected boolean delete(String key) throws JabotBrainException {
        return brain.delete(namespace, key);
    }

    /**
     * clear namespace brain values
     *
     * @return
     */
    protected boolean clear() throws JabotBrainException {
        return brain.clear(namespace);
    }

    /**
     * check the key is stored in brain
     *
     * @param key
     * @return
     */
    protected boolean isStored(String key) throws JabotBrainException {
        return brain.isStored(namespace, key);
    }

    /**
     * guard JabotBrainException. If it raised, post error message and return Optional.empty not to pass next Handler.
     * <code>
     * // rule caller
     * strings -> brainGuard(() -> {
     *   store(strings[0], strings[1])
     *   return Optional.empty();
     * })
     * </code>
     *
     * @return
     */
    protected Optional<String> brainGuard(BrainHandlerAction f) {
        try {
            return f.get();
        } catch (JabotBrainException e) {
            send(e.getMessage());
            return Optional.empty();
        }
    }

    @FunctionalInterface
    interface BrainHandlerAction {
        /**
         * Gets a rule result which may throw JabotBrainException.
         *
         * @return a result
         */
        Optional<String> get() throws JabotBrainException;
    }
}
