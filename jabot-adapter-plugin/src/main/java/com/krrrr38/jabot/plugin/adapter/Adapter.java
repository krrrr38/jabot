package com.krrrr38.jabot.plugin.adapter;

import com.krrrr38.jabot.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

abstract public class Adapter implements Plugin {
    private volatile boolean running = false;

    private String botName;
    private Consumer<String> receiver;

    public void setup(String botName, Consumer<String> receiver, Map<String, String> options) {
        if (botName == null || botName.isEmpty()) {
            botName = "jabot";
        }
        if (receiver == null) {
            throw new IllegalArgumentException("receiver required");
        }
        this.botName = botName;
        this.receiver = receiver;

        build(options);
    }

    public final void listen() {
        connectAction();
        running = true;
        while (running) {
            receives().stream().forEach(receiver::accept);
        }
    }

    public final void stop() {
        running = false;
    }

    protected String getBotName() {
        return botName;
    }

    /**
     * build settings. this method is called once when starting application.
     *
     * @param options
     */
    protected abstract void build(Map<String, String> options);

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
