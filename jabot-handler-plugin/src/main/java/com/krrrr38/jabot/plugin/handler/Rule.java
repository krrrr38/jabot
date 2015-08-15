package com.krrrr38.jabot.plugin.handler;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    private Pattern pattern;
    private String name;
    private String description;
    private String usage;
    private boolean isHidden = false;
    private Function<String[], Optional<String>> caller;

    /**
     * Handler Rule
     *
     * @param pattern     message pattern. pattern groups would be nullable string to caller arguments.
     * @param name        rule name
     * @param description rule description
     * @param usage       rule usage
     * @param isHidden    if true, hide from help command
     * @param caller      if pattern match, call this. Next rule or handler will use return value. If empty, they wil not be called.
     */
    public Rule(Pattern pattern, String name, String description, String usage, boolean isHidden, Function<String[], Optional<String>> caller) {
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
        this.isHidden = isHidden;
        this.caller = caller;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public boolean isHidden() {
        return isHidden;
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

    @Override
    public String toString() {
        return "Rule{" +
                "pattern=" + pattern +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", usage='" + usage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rule)) return false;

        Rule rule = (Rule) o;

        if (isHidden != rule.isHidden) return false;
        if (!pattern.equals(rule.pattern)) return false;
        if (!name.equals(rule.name)) return false;
        if (!description.equals(rule.description)) return false;
        return usage.equals(rule.usage);
    }

    @Override
    public int hashCode() {
        int result = pattern.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + usage.hashCode();
        result = 31 * result + (isHidden ? 1 : 0);
        return result;
    }
}
