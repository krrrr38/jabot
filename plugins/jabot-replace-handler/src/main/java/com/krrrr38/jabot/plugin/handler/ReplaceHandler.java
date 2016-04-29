package com.krrrr38.jabot.plugin.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReplaceHandler extends Handler {
    private static final String HANDLER_NAME = "replace";

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Arrays.asList(LIST_RULE, DELETE_RULE, DELETE_ALL_RULE, REGISTER_RULE, REPLACE_RULE);
    }

    private final Rule LIST_RULE =
            new Rule(
                    Pattern.compile("\\Alist patterns\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show registered patterns to replace",
                    "replace patterns",
                    false,
                    strings -> brainGuard(() -> {
                        Map<String, String> patterns = getAll();
                        if (patterns.isEmpty()) {
                            send("No registered replace patterns");
                        } else {
                            String header = "=== Replace Patterns ===\n";
                            String message = patterns.entrySet().stream().map(e -> {
                                return String.format("%s → %s", e.getKey(), e.getValue());
                            }).collect(Collectors.joining("\n"));
                            send(header + message);
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_RULE =
            new Rule(
                    Pattern.compile("\\Adelete pattern (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete replace pattern with key",
                    "delete pattern <key>",
                    false,
                    strings -> brainGuard(() -> {
                        if (delete(strings[0].trim())) {
                            send("Deleted");
                        } else {
                            send("NotFound");
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_ALL_RULE =
            new Rule(
                    Pattern.compile("\\Adelete all patterns\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete all replace patterns",
                    "delete all patterns",
                    false,
                    strings -> brainGuard(() -> {
                        if (clear()) {
                            send("Deleted all patterns");
                        } else {
                            send("Failed to clear patterns");
                        }
                        return Optional.empty();
                    })
            );

    private Rule REGISTER_RULE =
            new Rule(
                    Pattern.compile("\\Areplace (.+) with (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Register replace pattern",
                    "replace <from> with <to>",
                    false,
                    strings -> brainGuard(() -> {
                        String from = strings[0];
                        String to = strings[1];
                        if (store(from, to)) {
                            send(String.format("Registered pattern: %s → %s", from, to));
                        } else {
                            send(String.format("Failed to registry pattern: %s → %s", from, to));
                        }
                        return Optional.empty();
                    })
            );

    private Rule REPLACE_RULE =
            new Rule(
                    Pattern.compile("\\A(.+)\\z"),
                    HANDLER_NAME,
                    "Reply your message based on registered patterns",
                    "*",
                    false,
                    strings -> brainGuard(() -> {
                        String message = strings[0];
                        for (Map.Entry<String, String> entry : getAll().entrySet()) {
                            message = message.replace(entry.getKey(), entry.getValue());
                        }
                        return Optional.of(message);
                    })
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
