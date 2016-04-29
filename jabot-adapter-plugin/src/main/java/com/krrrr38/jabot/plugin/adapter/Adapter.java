package com.krrrr38.jabot.plugin.adapter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import com.krrrr38.jabot.plugin.Plugin;

abstract public class Adapter extends Plugin {
    private volatile boolean running = false;

    private String namespace;
    private String botName;
    private Consumer<String> receiver;

    @Override
    protected String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace plugin namespace
     * @param botName   botname
     * @param receiver  message receiver
     * @param options   adapter options
     */
    public void setup(String namespace, String botName, Consumer<String> receiver, Map<String, String> options) {
        if (botName == null || botName.isEmpty()) {
            botName = "jabot";
        }
        if (receiver == null) {
            throw new IllegalArgumentException("receiver required");
        }
        this.namespace = namespace;
        this.botName = botName;
        this.receiver = receiver;
        afterSetup(options);
    }

    /**
     * connect and receive messages forever
     */
    public final void listen() {
        connectAction();
        running = true;
        while (running) {
            receives().stream().forEach(receiver::accept);
        }
    }

    /**
     * stop listen infinite loop
     */
    public final void stop() {
        running = false;
    }

    /**
     * @return botname
     */
    protected String getBotName() {
        return botName;
    }

    /**
     * receive message
     *
     * @return message
     */
    abstract public String receive();

    /**
     * receive multiple messages.
     * If return multiple messages, please override.
     *
     * @return messages
     */
    public Collection<String> receives() {
        return Collections.singletonList(receive());
    }

    /**
     * send message
     */
    abstract public void post(String message);

    /**
     * When starting to connect, send message and so on.
     */
    abstract public void connectAction();
}
