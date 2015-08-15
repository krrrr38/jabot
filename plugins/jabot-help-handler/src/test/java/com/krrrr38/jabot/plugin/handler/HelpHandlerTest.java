package com.krrrr38.jabot.plugin.handler;

import com.krrrr38.jabot.plugin.brain.EmptyBrain;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
            List<Rule> build(Map<String, String> options) {
                return sampleHandlerRules;
            }

            @Override
            public void afterRegister(List<Handler> handlers) {
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