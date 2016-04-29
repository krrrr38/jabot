package com.krrrr38.jabot.plugin.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.message.SendMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReplaceHandler extends Handler {
    private static final String HANDLER_NAME = "replace";

    private Map<String, String> patterns = new ConcurrentHashMap<>();

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Arrays.asList(LIST_RULE, DELETE_RULE, DELETE_ALL_RULE, REGISTER_RULE, REPLACE_RULE);
    }

    private final Rule LIST_RULE =
            new Rule(
                    Pattern.compile("\\A(list )?patterns\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show registered patterns to replace",
                    "replace patterns",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        Map<String, String> patterns = getAll();
                        if (patterns.isEmpty()) {
                            send(new SendMessage("No registered replace patterns"));
                        } else {
                            String header = "=== Replace Patterns ===\n";
                            String message = patterns.entrySet().stream().map(e -> {
                                return String.format("%s → %s", e.getKey(), e.getValue());
                            }).collect(Collectors.joining("\n"));
                            send(new SendMessage(header + message));
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
                    (sender, strings) -> brainGuard(() -> {
                        String fromPattern = strings[0].trim();
                        String toPattern = patterns.remove(fromPattern);
                        if (toPattern != null) {
                            delete(fromPattern);
                            send(new SendMessage(String.format("Deleted: %s → %s", fromPattern, toPattern)));
                        } else {
                            send(new SendMessage("No such pattern"));
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
                    (sender, strings) -> brainGuard(() -> {
                        if (clear()) {
                            send(new SendMessage("Deleted all patterns"));
                            patterns = new ConcurrentHashMap<>();
                        } else {
                            send(new SendMessage("Failed to clear patterns"));
                        }
                        return Optional.empty();
                    })
            );

    private Rule REGISTER_RULE =
            new Rule(
                    Pattern.compile("\\Areplace (.+?) with (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Register replace pattern",
                    "replace <from> with <to>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String from = strings[0];
                        String to = strings[1];
                        if (store(from, to)) {
                            patterns.put(from, to);
                            send(new SendMessage(String.format("Registered pattern: %s → %s", from, to)));
                        } else {
                            send(new SendMessage(
                                    String.format("Failed to registry pattern: %s → %s", from, to)));
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
                    (sender, strings) -> brainGuard(() -> {
                        String message = strings[0];
                        for (Map.Entry<String, String> entry : patterns.entrySet()) {
                            message = message.replace(entry.getKey(), entry.getValue());
                        }
                        return Optional.of(message);
                    })
            );

    @Override
    public void afterSetup(Map<String, String> options) {
        try {
            patterns = Collections.synchronizedMap(getAll());
        } catch (JabotBrainException e) {
            log.error("Failed to load patterns: " + e.getMessage(), e);
            patterns = new ConcurrentHashMap<>();
        }
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public void afterRegister(List<Handler> handlers) {
    }
}
