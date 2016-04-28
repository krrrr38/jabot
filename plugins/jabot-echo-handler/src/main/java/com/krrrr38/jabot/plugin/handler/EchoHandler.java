package com.krrrr38.jabot.plugin.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class EchoHandler extends Handler {
    private static final String HANDLER_NAME = "echo";

    @Override
    List<Rule> build(Map<String, String> options) {
        Rule echoRule = new Rule(
                Pattern.compile("\\Aecho\\s+(.+)\\z", Pattern.CASE_INSENSITIVE),
                HANDLER_NAME,
                "Reply your message",
                "echo <message>",
                false,
                strings -> {
                    // strings are nullable regex grouping
                    send(strings[0].trim());
                    // If return Optional.empty, following handlers will be never called.
                    return Optional.empty();
                }
        );

        // EchoHandler define only echoRule.
        return Collections.singletonList(echoRule);
    }

    @Override
    public void afterRegister(List<Handler> handlers) {

    }
}
