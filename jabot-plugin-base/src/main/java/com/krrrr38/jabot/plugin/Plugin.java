package com.krrrr38.jabot.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract public class Plugin {
    private static final String OPTION_SEPARATOR = ",";
    private static final String REQUIRE_ERROR_FORMAT = "[%s] %s is required option as [%s]";
    private static final String TYPE_ERROR_FORMAT = "[%s] %s is required as [%s]";
    private static final String PATTERN_ERROR_FORMAT = "[%s] %s is matched with pattern: %s";

    protected abstract String getNamespace();

    /**
     * After setup hook
     */
    abstract public void afterSetup(Map<String, String> options);

    /**
     * Before destroy Hook
     */
    abstract public void beforeDestroy();

    ////////////////////////////////////////////////////////////////////////////
    // options utilities

    protected String optionString(Map<String, String> options, String key, String _default) {
        return option(options, key, "String", _default, ThrowableFunction.<String>identity());
    }

    protected String optionString(Map<String, String> options, String key, String _default, Pattern allowPattern) {
        String result = option(options, key, "String", _default, ThrowableFunction.<String>identity());
        if (!allowPattern.matcher(result).matches()) {
            throw new JabotPluginOptionException(String.format(PATTERN_ERROR_FORMAT, getNamespace(), key, allowPattern.toString()));
        }
        return result;
    }

    protected String requireString(Map<String, String> options, String key) {
        return require(options, key, "String", ThrowableFunction.<String>identity());
    }

    protected String requireString(Map<String, String> options, String key, Pattern allowPattern) {
        String result = require(options, key, "String", ThrowableFunction.<String>identity());
        if (!allowPattern.matcher(result).matches()) {
            throw new JabotPluginOptionException(String.format(PATTERN_ERROR_FORMAT, getNamespace(), key, allowPattern.toString()));
        }
        return result;
    }

    protected Integer optionInteger(Map<String, String> options, String key, Integer _default) {
        return option(options, key, "Integer", _default, Integer::parseInt);
    }

    protected Integer requireInteger(Map<String, String> options, String key) {
        return require(options, key, "Integer", Integer::parseInt);
    }

    protected Long optionLong(Map<String, String> options, String key, Long _default) {
        return option(options, key, "Long", _default, Long::parseLong);
    }

    protected Long requireLong(Map<String, String> options, String key) {
        return require(options, key, "Long", Long::parseLong);
    }

    protected boolean optionBoolean(Map<String, String> options, String key, boolean _default) {
        return option(options, key, "Boolean", _default, Boolean::parseBoolean);
    }

    protected boolean requireBoolean(Map<String, String> options, String key) {
        String value = options.get(key);
        if (value == null) {
            throw new JabotPluginOptionException(String.format(REQUIRE_ERROR_FORMAT, getNamespace(), key, "Boolean"));
        } else if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        }
        throw new JabotPluginOptionException(String.format(TYPE_ERROR_FORMAT, getNamespace(), key, "Boolean"));
    }

    protected List<String> optionStringList(Map<String, String> options, String key, List<String> _default) {
        return option(options, key, "Comma separated String", _default, (value) -> {
            return Arrays.asList(value.split(OPTION_SEPARATOR)).stream().map(String::trim).collect(Collectors.toList());
        });
    }

    protected List<String> requireStringList(Map<String, String> options, String key) {
        return require(options, key, "Comma separated String", (value) -> {
            return Arrays.asList(value.split(OPTION_SEPARATOR)).stream().map(String::trim).collect(Collectors.toList());
        });
    }

    protected List<Integer> optionIntegerList(Map<String, String> options, String key, List<Integer> _default) {
        return option(options, key, "Comma separated Integer", _default, (value) -> {
            return Arrays.asList(value.split(OPTION_SEPARATOR)).stream().map(Integer::parseInt).collect(Collectors.toList());
        });
    }

    protected List<Integer> requireIntegerList(Map<String, String> options, String key) {
        return require(options, key, "Comma separated Integer", (value) -> {
            return Arrays.asList(value.split(OPTION_SEPARATOR)).stream().map(Integer::parseInt).collect(Collectors.toList());
        });
    }

    protected List<Long> optionLongList(Map<String, String> options, String key, List<Long> _default) {
        return option(options, key, "Comma separated Long", _default, (value) -> {
            return Arrays.asList(value.split(OPTION_SEPARATOR)).stream().map(Long::parseLong).collect(Collectors.toList());
        });
    }

    protected List<Long> requireLongList(Map<String, String> options, String key) {
        return require(options, key, "Comma separated Long", (value) -> {
            return Arrays.asList(value.split(OPTION_SEPARATOR)).stream().map(Long::parseLong).collect(Collectors.toList());
        });
    }

    /**
     * read value with following methods
     * 1. read value from environment value with NAME_SPACE_OPTION_KEY format
     * 2. if null, read value from settings.yml options
     * 3. if value is ENV['OTHER_ENV'], reread environment value with OTHER_ENV
     * 4. if null, return option value
     */
    private <T> T option(Map<String, String> options, String key, String type, T _default, ThrowableFunction<String, T> converter) {
        String value = System.getenv(getEnvKey(key));
        if (value == null) {
            value = options.get(key);
        }
        value = convertEnvFormatValue(value);
        if (value == null) {
            return _default;
        } else {
            try {
                return converter.apply(value);
            } catch (Exception e) {
                throw new JabotPluginOptionException(String.format(TYPE_ERROR_FORMAT, key, type), e);
            }
        }
    }

    /**
     * read value with following methods
     * 1. read value from environment value with NAME_SPACE_OPTION_KEY format
     * 2. if null, read value from settings.yml options
     * 3. if value is ENV['OTHER_ENV'], reread environment value with OTHER_ENV
     */
    private <T> T require(Map<String, String> options, String key, String type, ThrowableFunction<String, T> converter) {
        String value = System.getenv(getEnvKey(key));
        if (value == null) {
            value = options.get(key);
        }
        value = convertEnvFormatValue(value);
        if (value == null) {
            throw new JabotPluginOptionException(String.format(REQUIRE_ERROR_FORMAT, getNamespace(), key, type));
        } else {
            try {
                return converter.apply(value);
            } catch (Exception e) {
                throw new JabotPluginOptionException(String.format(TYPE_ERROR_FORMAT, getNamespace(), key, type), e);
            }
        }
    }

    // @VisibleForTesting
    String getEnvKey(String key) {
        return toUpperSnakeCase(getNamespace()) + "_" + toUpperSnakeCase(key);
    }

    private static final String CAMEL_CASE_REGEX = "([a-z0-9])([A-Z]+)";
    private static final String SNAKE_CASE_REPLACEMENT = "$1_$2";
    private String toUpperSnakeCase(String key) {
        return key.replaceAll(CAMEL_CASE_REGEX, SNAKE_CASE_REPLACEMENT)
                  .replaceAll("\\s+", "_")
                  .replaceAll("-", "_")
                  .toUpperCase();
    }

    // ENV['OTHER_ENV_VARIABLE'] => System.getEnv("OTHER_ENV_VARIABLE");
    private static final Pattern ENV_FORMAT = Pattern.compile("ENV\\['?([^']*)'?\\]");
    private String convertEnvFormatValue(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = ENV_FORMAT.matcher(value);
        if (matcher.matches()) {
            return System.getenv(matcher.group(1));
        }
        return value;
    }

    @FunctionalInterface
    public interface ThrowableFunction<T, R> {
        R apply(T t) throws Exception;

        static <T> ThrowableFunction<T, T> identity() {
            return t -> t;
        }
    }
}
