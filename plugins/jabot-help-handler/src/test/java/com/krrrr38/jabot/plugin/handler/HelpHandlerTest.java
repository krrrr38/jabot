package com.krrrr38.jabot.plugin.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.krrrr38.jabot.plugin.brain.EmptyBrain;

public class HelpHandlerTest {
    private Deque<String> queue = new ArrayDeque<>();
    private Handler helpHandler;

    private List<Handler> handlers = new ArrayList<>();

    @Before
    public void setUp() {
        List<Rule> sampleHandlerRules = new ArrayList<>();
        sampleHandlerRules.add(new Rule(
                Pattern.compile(""), "sample", "sample description",
                "sample usage", false, strings -> Optional.empty()));
        handlers = new ArrayList<>();
        Handler sampleHandler = new Handler() {
            @Override
            List<Rule> buildRules(Map<String, String> options) {
                return sampleHandlerRules;
            }

            @Override
            public void afterRegister(List<Handler> handlers) {
            }

            @Override
            public void afterSetup(Map<String, String> options) {
            }

            @Override
            public void beforeDestroy() {
            }
        };

        helpHandler = new HelpHandler();

        sampleHandler.setup("namespace", new EmptyBrain(), queue::add, null);
        helpHandler.setup("namespace", new EmptyBrain(), queue::add, null);

        handlers.add(sampleHandler);
        handlers.add(helpHandler);

        // to show all handler in `help`, need to register with after hook
        helpHandler.afterRegister(handlers);
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`message` is not caught", helpHandler.receive("message"), is(Optional.of("message")));
        assertThat(queue.isEmpty(), is(true));

        // help all rules
        assertThat(helpHandler.receive("help"), is(Optional.empty()));
        assertThat("catch success", queue.size(), is(1));
        String lastMessage = queue.peekLast();
        assertThat("contains sample rule usage", lastMessage, containsString("sample usage"));
        assertThat("contains help rule usage", lastMessage, containsString("help <name>"));

        // help specific rule
        assertThat(helpHandler.receive("help sample"), is(Optional.empty()));
        assertThat("catch success", queue.size(), is(2));
        lastMessage = queue.peekLast();
        assertThat("contains sample rule usage", lastMessage, containsString("sample usage"));
        assertThat("not contains help rule usage", lastMessage, not(containsString("help <name>")));
    }
}