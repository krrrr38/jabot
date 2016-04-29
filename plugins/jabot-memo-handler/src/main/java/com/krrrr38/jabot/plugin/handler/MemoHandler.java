package com.krrrr38.jabot.plugin.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.krrrr38.jabot.plugin.message.SendMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoHandler extends Handler {
    private static final String HANDLER_NAME = "memo";

    @Override
    List<Rule> buildRules(Map<String, String> options) {
        return Arrays.asList(LIST_MEMOS, DELETE_MEMO, DELETE_ALL_MEMOS, ADD_MEMO);
    }

    private final Rule LIST_MEMOS =
            new Rule(
                    Pattern.compile("\\A(list )?memos\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Show shared memo key list",
                    "list memos",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        Map<String, String> memos = getAll();
                        if (memos.isEmpty()) {
                            send(new SendMessage("No registered memos"));
                        } else {
                            String header = "=== Following keys are available ===\n";
                            String message = memos.keySet().stream().collect(Collectors.joining(", "));
                            send(new SendMessage(header + message));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_MEMO =
            new Rule(
                    Pattern.compile("\\Adelete memo (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete shared memo by key",
                    "delete memo <key>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String key = strings[0].trim();
                        Optional<String> maybeMemo = get(key);
                        if (maybeMemo.isPresent()) {
                            String memo = maybeMemo.get();
                            delete(key);
                            send(new SendMessage(String.format("Memo Deleted: [%s] %s", key, memo)));
                        } else {
                            send(new SendMessage("No such memo"));
                        }
                        return Optional.empty();
                    })
            );

    private final Rule DELETE_ALL_MEMOS =
            new Rule(
                    Pattern.compile("\\Adelete all memos\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Delete all shared memos",
                    "delete all memos",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        if (clear()) {
                            send(new SendMessage("Deleted all memos"));
                        } else {
                            send(new SendMessage("Failed to clear memos"));
                        }
                        return Optional.empty();
                    })
            );

    private Rule ADD_MEMO =
            new Rule(
                    Pattern.compile("\\Aadd memo (.+?) (.+)\\z", Pattern.CASE_INSENSITIVE),
                    HANDLER_NAME,
                    "Register new memo",
                    "add memo <key> <memo>",
                    false,
                    (sender, strings) -> brainGuard(() -> {
                        String key = strings[0];
                        String memo = strings[1];
                        if (store(key, memo)) {
                            send(new SendMessage(String.format("Registered new memo: [%s] %s", key, memo)));
                        } else {
                            send(new SendMessage("Failed to register memo"));
                        }
                        return Optional.empty();
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
