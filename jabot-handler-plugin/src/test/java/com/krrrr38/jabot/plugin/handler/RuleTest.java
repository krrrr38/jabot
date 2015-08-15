package com.krrrr38.jabot.plugin.handler;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RuleTest {
    private Rule rule;

    @Before
    public void setUp() throws Exception {
        rule = new Rule(
                Pattern.compile("sample"),
                "sample",
                "sample description",
                "sample usage",
                false,
                groups -> {
                    return Optional.of("converted");
                }
        );
    }

    @Test
    public void testGetPattern() throws Exception {
        assertThat(rule.getPattern().pattern(), is(Pattern.compile("sample").pattern()));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(rule.getName(), is("sample"));
    }

    @Test
    public void testGetDescription() throws Exception {
        assertThat(rule.getDescription(), is("sample description"));
    }

    @Test
    public void testGetUsage() throws Exception {
        assertThat(rule.getUsage(), is("sample usage"));
    }

    @Test
    public void testIsHidden() throws Exception {
        assertThat(rule.isHidden(), is(false));
    }

    @Test
    public void testApply() throws Exception {
        assertThat("not applied", rule.apply("foo"), is(Optional.of("foo")));
        assertThat("applied", rule.apply("sample"), is(Optional.of("converted")));
    }
}