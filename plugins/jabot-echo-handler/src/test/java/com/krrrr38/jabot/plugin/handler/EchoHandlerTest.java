package com.krrrr38.jabot.plugin.handler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.krrrr38.jabot.plugin.brain.EmptyBrain;
import com.krrrr38.jabot.plugin.message.SendMessage;

public class EchoHandlerTest {
    private Deque<SendMessage> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() {
        handler = new EchoHandler();
        handler.setup("namespace", new EmptyBrain(), queue::add, null);
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`message` is not caught", handler.receive(null, "message"), is(Optional.of("message")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive(null, "echo foo\n\nbar"), is(Optional.empty()));
        assertThat(queue.size(), is(1));
        assertThat(queue.peekLast().getMessage(), is("foo\n\nbar"));
    }
}