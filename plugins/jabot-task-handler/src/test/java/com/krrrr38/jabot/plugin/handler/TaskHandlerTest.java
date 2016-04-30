package com.krrrr38.jabot.plugin.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.message.Sender;

public class TaskHandlerTest {
    private Deque<SendMessage> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        Brain brain = new MockInmemoryBrain();
        brain.setup("task-handler", "jabot", Collections.emptyMap());
        handler = new TaskHandler();
        handler.setup("task-handler", brain, queue::add, null);
    }

    @Test
    public void testReceive() throws Exception {
        Sender sender = new Sender("user-id", "mention-id", "name", "email");
        assertThat("`foo` is not caught", handler.receive(sender, "foo"), is(Optional.of("foo")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive(sender, "list tasks"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered tasks"));
        assertThat(handler.receive(sender, "tasks"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered tasks"));

        assertThat(handler.receive(sender, "add task message1"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Registered new task"));
        assertThat(handler.receive(sender, "add task message2"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Registered new task"));
        assertThat(handler.receive(sender, "add task message3"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Registered new task"));

        assertThat(handler.receive(sender, "delete task 1000"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No task deleted"));

        assertThat(handler.receive(sender, "delete task 1 2 1 2 1"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Task Deleted: message2"));

        assertThat(handler.receive(sender, "list tasks"), is(Optional.empty()));
        String listMessage = queue.peekLast().getMessage();
        assertThat("task stored correctly", listMessage, containsString("message1"));
        assertThat("task deleted correctly", listMessage, not(containsString("message2")));
        assertThat("task deleted correctly", listMessage, not(containsString("message3")));

        Sender otherSender = new Sender("other-id", "", "", "");
        assertThat(handler.receive(otherSender, "list tasks"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered tasks"));

        assertThat(handler.receive(sender, "delete all tasks"), is(Optional.empty()));
        assertThat("all tasks deleted correctly", queue.peekLast().getMessage(),
                   containsString("Deleted name's all tasks"));

        assertThat("nothing to be applied", handler.receive(sender, "test foo piyo"),
                   is(Optional.of("test foo piyo")));

        assertThat(handler.receive(sender, "list tasks"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered tasks"));
    }

    @Test
    public void testReceiveBrainException() throws Exception {
        Sender sender = new Sender("user-id", "mention-id", "name", "email");
        assertThat("If raise BrainException, return empty not to pass next handler",
                   handler.receive(sender, "add task raise brain exception"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("raise error"));
    }

    static class MockInmemoryBrain extends Brain {
        private Map<String, String> brain;

        @Override
        public void afterSetup(Map<String, String> options) {
            brain = new ConcurrentHashMap<>();
        }

        @Override
        public void beforeDestroy() {
        }

        @Override
        public Map<String, String> getAll(String namespace) throws JabotBrainException {
            return brain;
        }

        @Override
        public Optional<String> get(String namespace, String key) throws JabotBrainException {
            return Optional.ofNullable(brain.get(key));
        }

        @Override
        public boolean store(String namespace, String key, String value) throws JabotBrainException {
            if (value.contains("raise brain exception")) {
                throw new JabotBrainException("raise error");
            }
            brain.put(key, value);
            return true;
        }

        @Override
        public boolean store(String namespace, String key1, String value1, String key2, String value2)
                throws JabotBrainException {
            brain.put(key1, value1);
            brain.put(key2, value2);
            return true;
        }

        @Override
        public boolean store(String namespace, String key1, String value1, String key2, String value2,
                             String key3, String value3) throws JabotBrainException {
            brain.put(key1, value1);
            brain.put(key2, value2);
            brain.put(key3, value3);
            return true;
        }

        @Override
        public boolean storeAll(String namespace, Map<String, String> keyvalues) throws JabotBrainException {
            brain.putAll(keyvalues);
            return true;
        }

        @Override
        public boolean delete(String namespace, String key) throws JabotBrainException {
            return brain.remove(key) != null;
        }

        @Override
        public boolean clear(String namespace) throws JabotBrainException {
            brain = new ConcurrentHashMap<>();
            return true;
        }

        @Override
        public boolean isStored(String namespace, String key) throws JabotBrainException {
            return brain.containsKey(key);
        }
    }
}