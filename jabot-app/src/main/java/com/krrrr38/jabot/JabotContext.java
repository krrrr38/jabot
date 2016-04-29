package com.krrrr38.jabot;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krrrr38.jabot.plugin.adapter.Adapter;
import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.handler.Handler;

import lombok.Data;

@Data
public class JabotContext {
    private static final Logger logger = LoggerFactory.getLogger(JabotContext.class);

    private Brain brain;
    private Adapter adapter;
    private List<Handler> handlers;

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
        handlers.forEach(Handler::beforeDestroy);
        adapter.beforeDestroy();
        brain.beforeDestroy();
    }
}
