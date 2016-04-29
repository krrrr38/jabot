package com.krrrr38.jabot.plugin.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HelpHandler extends Handler {
    private static final String HANDLER_NAME = "help";
    private List<Handler> handlers;

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Collections.singletonList(HELP_RULE);
    }

    private final Rule HELP_RULE =
            new Rule(
                    Pattern.compile("\\Ahelp(?: me)?(?: (.+))?\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show help messages",
                    "help <name>",
                    false,
                    help()
            );

    private Function<String[], Optional<String>> help() {
        return strings -> {
            String ruleName = strings[0];
            if (ruleName == null) {
                showRules();
            } else {
                showRule(ruleName.trim());
            }
            return Optional.empty();
        };
    }

    private void showRule(String ruleName) {
        String message = handlers.stream()
                                 .flatMap(handler -> handler.getRules().stream())
                                 .filter(rule -> rule.getName().contains(ruleName))
                                 .filter(rule -> !rule.isHidden())
                                 .map(rule -> String.format(
                                         "[%s] %s\n  Description: %s\n  Pattern: %s",
                                         rule.getName(),
                                         rule.getUsage(),
                                         rule.getDescription(),
                                         rule.getPattern().pattern()))
                                 .collect(Collectors.joining("\n"));
        if (!message.isEmpty()) {
            send(message);
        } else {
            send("help: Nothing to match this keyword");
        }
    }

    private void showRules() {
        String message = handlers.stream()
                                 .flatMap(handler -> handler.getRules().stream())
                                 .filter(rule -> !rule.isHidden())
                                 .map(rule -> String.format("[%s] %s - %s", rule.getName(), rule.getUsage(),
                                                            rule.getDescription()))
                                 .collect(Collectors.joining("\n"));
        send(message);
    }

    @Override
    public void afterRegister(List<Handler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void afterSetup(Map<String, String> options) {
    }

    @Override
    public void beforeDestroy() {
    }
}
