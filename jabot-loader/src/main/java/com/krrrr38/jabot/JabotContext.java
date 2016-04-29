package com.krrrr38.jabot;

import java.util.List;
import java.util.Optional;

import com.krrrr38.jabot.plugin.adapter.Adapter;
import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.handler.Handler;
import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.message.Sender;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JabotContext {
    private Brain brain;
    private Adapter adapter;
    private List<Handler> handlers;

    public void send(SendMessage message) {
        log.debug("Send message: {}", message);
        adapter.post(message);
    }

    public void receive(ReceiveMessage receiveMessage) {
        log.debug("Receive message: {}", receiveMessage);
        Sender sender = receiveMessage.getSender();
        handlers.stream()
                .reduce(Optional.of(receiveMessage.getMessage()),
                        (maybeMessage, handler) -> maybeMessage
                                .flatMap(message -> handler.receive(sender, message)),
                        (s, s2) -> Optional.empty()); // should not be called parallel
    }

    public void listenAdapter() {
        log.info("Start application");
        adapter.listen();
        destroy();
    }

    public void destroy() {
        log.info("Stop application");
        adapter.beforeDestroy();
        handlers.forEach(Handler::beforeDestroy);
        brain.beforeDestroy();
    }
}
