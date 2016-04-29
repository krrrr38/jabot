package com.krrrr38.jabot.plugin;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class PluginTest {
    private static final String NAMESPACE = "mock-plugin";
    private final Plugin plugin = new MockPlugin();
    private Map<String, String> options;
    private final String STRING_KEY = "string-key";
    private final String STRING_LIST_KEY = "string-list-key";
    private final String INTEGER_KEY = "integer-key";
    private final String INTEGER_LIST_KEY = "integer-list-key";
    private final String LONG_KEY = "long-key";
    private final String LONG_LIST_KEY = "long-list-key";
    private final String BOOLEAN_KEY = "boolean-key";

    @Before
    public void setUp() {
        options = new HashMap<>();
        options.put(STRING_KEY, "string-value");
        options.put(STRING_LIST_KEY, "value1, value2");
        options.put(INTEGER_KEY, "100");
        options.put(INTEGER_LIST_KEY, "100,200");
        options.put(LONG_KEY, "100");
        options.put(LONG_LIST_KEY, "100,200");
        options.put(BOOLEAN_KEY, "true");
    }

    @Test
    public void testOptionString() throws Exception {
        assertThat(plugin.optionString(options, STRING_KEY, "default"), is("string-value"));
        assertThat(plugin.optionString(options, "invalid", "default"), is("default"));
        try {
            // invalid pattern
            plugin.optionString(options, STRING_KEY, "default", Pattern.compile("invalid-string-value"));
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("invalid-string-value"));
        }
        assertThat(plugin.optionString(options, STRING_KEY, "default", Pattern.compile("strin.*alue")),
                   is("string-value"));
    }

    @Test
    public void testRequireString() throws Exception {
        assertThat(plugin.requireString(options, STRING_KEY), is("string-value"));
        try {
            // missing key
            plugin.requireString(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("String"));
        }
        try {
            // invalid pattern
            assertThat(plugin.requireString(options, STRING_KEY, Pattern.compile("invalid-string-value")),
                       is("string-value"));
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("invalid-string-value"));
        }
        assertThat(plugin.requireString(options, STRING_KEY, Pattern.compile("strin.*alue")),
                   is("string-value"));

    }

    @Test
    public void testOptionInteger() throws Exception {
        assertThat(plugin.optionInteger(options, INTEGER_KEY, 999), is(100));
        assertThat(plugin.optionInteger(options, "invalid", 999), is(999));
    }

    @Test
    public void testRequireInteger() throws Exception {
        assertThat(plugin.requireInteger(options, INTEGER_KEY), is(100));
        try {
            // type error
            plugin.requireInteger(options, STRING_KEY);
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Integer"));
        }
        try {
            // missing key
            plugin.requireInteger(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Integer"));
        }
    }

    @Test
    public void testOptionLong() throws Exception {
        assertThat(plugin.optionLong(options, LONG_KEY, 999L), is(100L));
        assertThat(plugin.optionLong(options, "invalid", 999L), is(999L));
    }

    @Test
    public void testRequireLong() throws Exception {
        assertThat(plugin.requireLong(options, LONG_KEY), is(100L));
        try {
            // type error
            plugin.requireLong(options, STRING_KEY);
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Long"));
        }
        try {
            // missing key
            plugin.requireLong(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Long"));
        }
    }

    @Test
    public void testOptionBoolean() throws Exception {
        assertThat(plugin.optionBoolean(options, BOOLEAN_KEY, false), is(true));
        assertThat(plugin.optionBoolean(options, "invalid", true), is(true));
    }

    @Test
    public void testRequireBoolean() throws Exception {
        assertThat(plugin.requireBoolean(options, BOOLEAN_KEY), is(true));
        try {
            // type error
            plugin.requireBoolean(options, STRING_KEY);
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Boolean"));
        }
        try {
            // missing key
            plugin.requireBoolean(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Boolean"));
        }
    }

    @Test
    public void testOptionStringList() throws Exception {
        assertThat(plugin.optionStringList(options, STRING_LIST_KEY, Arrays.asList("v1", "v2")),
                   is(Arrays.asList("value1", "value2")));
        assertThat(plugin.optionStringList(options, "invalid", Arrays.asList("v1", "v2")),
                   is(Arrays.asList("v1", "v2")));
    }

    @Test
    public void testRequireStringList() throws Exception {
        assertThat(plugin.requireStringList(options, STRING_LIST_KEY), is(Arrays.asList("value1", "value2")));
        try {
            // missing key
            plugin.requireStringList(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Comma separated String"));
        }
    }

    @Test
    public void testOptionIntegerList() throws Exception {
        assertThat(plugin.optionIntegerList(options, INTEGER_LIST_KEY, Arrays.asList(800, 900)),
                   is(Arrays.asList(100, 200)));
        assertThat(plugin.optionIntegerList(options, "invalid", Arrays.asList(800, 900)),
                   is(Arrays.asList(800, 900)));
    }

    @Test
    public void testRequireIntegerList() throws Exception {
        assertThat(plugin.requireIntegerList(options, INTEGER_LIST_KEY), is(Arrays.asList(100, 200)));
        try {
            // type error
            plugin.requireIntegerList(options, STRING_KEY);
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Comma separated Integer"));
        }
        try {
            // missing key
            plugin.requireIntegerList(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Comma separated Integer"));
        }
    }

    @Test
    public void testOptionLongList() throws Exception {
        assertThat(plugin.optionLongList(options, LONG_LIST_KEY, Arrays.asList(800L, 900L)),
                   is(Arrays.asList(100L, 200L)));
        assertThat(plugin.optionLongList(options, "invalid", Arrays.asList(800L, 900L)),
                   is(Arrays.asList(800L, 900L)));
    }

    @Test
    public void testRequireLongList() throws Exception {
        assertThat(plugin.requireLongList(options, LONG_LIST_KEY), is(Arrays.asList(100L, 200L)));
        try {
            // type error
            plugin.requireLongList(options, STRING_KEY);
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Comma separated Long"));
        }
        try {
            // missing key
            plugin.requireLongList(options, "invalid");
            fail();
        } catch (JabotPluginOptionException e) {
            String message = e.getMessage();
            assertThat(message, containsString(NAMESPACE));
            assertThat(message, containsString("Comma separated Long"));
        }
    }

    static class MockPlugin extends Plugin {
        @Override
        protected String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public void afterSetup(Map<String, String> options) {
        }

        @Override
        public void beforeDestroy() {
        }
    }
}