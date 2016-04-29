package com.krrrr38.jabot.plugin.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.krrrr38.jabot.plugin.brain.EmptyBrain;
import com.krrrr38.jabot.plugin.message.SendMessage;

public class TimeHandlerTest {
    private Deque<SendMessage> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() {
        handler = new TimeHandler();
        handler.setup("time-handler", new EmptyBrain(), queue::add, null);
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`message` is not caught", handler.receive(null, "message"), is(Optional.of("message")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive(null, "time"), is(Optional.empty()));
        assertThat("catch success", queue.size(), is(1));
        assertThat("response is correct", queue.peekLast().getMessage(), containsString("Server time is: "));
    }
}