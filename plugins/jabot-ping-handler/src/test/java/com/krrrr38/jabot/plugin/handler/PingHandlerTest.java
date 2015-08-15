package com.krrrr38.jabot.plugin.handler;

import com.krrrr38.jabot.plugin.brain.EmptyBrain;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PingHandlerTest {
    private Deque<String> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() {
        handler = new PingHandler();
        handler.setup("namespace", new EmptyBrain(), queue::add, null);
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`message` is not caught", handler.receive("message"), is(Optional.of("message")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive("ping"), is(Optional.empty()));
        assertThat("catch success", queue.size(), is(1));
        assertThat("response is correct", queue.peekLast(), is("PONG"));

        assertThat(handler.receive("PING"), is(Optional.empty()));
        assertThat(queue.size(), is(2));
        assertThat(queue.peekLast(), is("PONG"));
    }
}