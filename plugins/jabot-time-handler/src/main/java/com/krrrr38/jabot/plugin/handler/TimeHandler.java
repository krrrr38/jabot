package com.krrrr38.jabot.plugin.handler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.krrrr38.jabot.plugin.message.SendMessage;

public final class TimeHandler extends Handler {
    private static final String HANDLER_NAME = "time";

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Collections.singletonList(TIME_RULE);
    }

    private final Rule TIME_RULE =
            new Rule(
                    Pattern.compile("\\Atime\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show server datetime",
                    "time",
                    false,
                    (sender, strings) -> {
                        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                        send(new SendMessage("Server time is: " + now));
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
