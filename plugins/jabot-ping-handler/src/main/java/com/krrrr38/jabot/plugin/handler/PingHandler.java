package com.krrrr38.jabot.plugin.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class PingHandler extends Handler {
    @Override
    List<Rule> build(Map<String, String> options) {
        Rule pingRule = new Rule(
                Pattern.compile("\\Aping\\z", Pattern.CASE_INSENSITIVE),
                "ping",
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

        // PingHandler define only pingRule.
        return Collections.singletonList(pingRule);
    }

    @Override
    public void afterRegister(List<Handler> handlers) {
    }
}
