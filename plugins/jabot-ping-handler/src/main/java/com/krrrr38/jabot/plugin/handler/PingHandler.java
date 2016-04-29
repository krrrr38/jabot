package com.krrrr38.jabot.plugin.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class PingHandler extends Handler {
    private static final String HANDLER_NAME = "ping";

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        // PingHandler define only PING_RULE.
        return Collections.singletonList(PING_RULE);
    }

    private final Rule PING_RULE =
            new Rule(
                    Pattern.compile("\\Aping\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Return PONG to PING",
                    "ping",
                    false,
                    strings -> {
                        // strings are groupings of regex pattern.
                        // the values are nullable.

                        // send "PONG" message
                        send("PONG");

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
