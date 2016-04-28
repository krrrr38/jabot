package com.krrrr38.jabot.plugin.handler;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Value;

@Value
public class Rule {
    private Pattern pattern;
    private String name;
    private String description;
    private String usage;
    private boolean hidden;
    private Function<String[], Optional<String>> caller;

    /**
     * Handler Rule
     *
     * @param pattern     message pattern. pattern groups would be nullable string to caller arguments.
     * @param name        handler name
     * @param description rule description
     * @param usage       rule usage
     * @param hidden    if true, hide from help command
     * @param caller      if pattern match, call this. Next rule or handler will use return value. If empty, they wil not be called.
     */
    public Rule(Pattern pattern, String name, String description, String usage, boolean hidden, Function<String[], Optional<String>> caller) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern required");
        }
        if (name == null) {
            throw new IllegalArgumentException("name required");
        }
        if (description == null) {
            throw new IllegalArgumentException("description required");
        }
        if (usage == null) {
            throw new IllegalArgumentException("usage required");
        }
        if (caller == null) {
            throw new IllegalArgumentException("caller required");
        }

        this.pattern = pattern;
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.hidden = hidden;
        this.caller = caller;
    }

    public Optional<String> apply(String message) {
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            int groupCount = matcher.groupCount();
            String[] args = new String[groupCount];
            for (int i = 0; i < groupCount; i++) {
                args[i] = matcher.group(i + 1);
            }
            return caller.apply(args);
        } else {
            return Optional.of(message);
        }
    }
}
