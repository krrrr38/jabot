package com.krrrr38.jabot;

import com.krrrr38.jabot.plugin.adapter.Adapter;
import com.krrrr38.jabot.plugin.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class JabotContext {
    private static final Logger logger = LoggerFactory.getLogger(JabotContext.class);

    private Adapter adapter;
    private List<Handler> handlers;

    void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public void send(String message) {
        logger.debug("Send message: {}", message);
        adapter.post(message);
    }

    public void receive(String message) {
        logger.debug("Receive message: {}", message);
        handlers.stream()
                .reduce(Optional.of(message),
                        (maybeMessage, handler) -> maybeMessage.flatMap(handler::receive),
                        (s, s2) -> Optional.empty()); // should not be called parallel
    }

    public void listenAdapter() {
        adapter.listen();
    }
}
