package com.krrrr38.jabot.plugin.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.krrrr38.jabot.plugin.message.SendMessage;

public class EchoHandler extends Handler {
    private static final String HANDLER_NAME = "echo";

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        // EchoHandler define only ECHO_RULE.
        return Collections.singletonList(ECHO_RULE);
    }

    private final Rule ECHO_RULE =
            new Rule(
                    Pattern.compile("\\Aecho ([\\s\\S]+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Reply your message",
                    "echo <message>",
                    false,
                    (sender, strings) -> {
                        // strings are nullable regex grouping
                        send(new SendMessage(strings[0].trim()));
                        // If return Optional.empty, following handlers will be never called.
                        return Optional.empty();
                    }
            );

    @Override
    public void afterSetup(Map<String, String> options) {
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public void afterRegister(List<Handler> handlers) {
    }
}
