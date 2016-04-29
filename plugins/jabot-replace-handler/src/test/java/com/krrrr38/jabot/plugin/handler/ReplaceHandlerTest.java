package com.krrrr38.jabot.plugin.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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

public class ReplaceHandlerTest {
    private Deque<SendMessage> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        Brain brain = new MockInmemoryBrain();
        brain.setup("replace-handler", "jabot", Collections.emptyMap());
        handler = new ReplaceHandler();
        handler.setup("replace-handler", brain, queue::add, null);
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`foo` is not caught", handler.receive(null, "foo"), is(Optional.of("foo")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive(null, "list patterns"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered replace patterns"));
        assertThat(handler.receive(null, "patterns"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered replace patterns"));

        assertThat(handler.receive(null, "replace foo with bar"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Registered pattern"));
        assertThat(handler.receive(null, "replace piyo with zzz"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Registered pattern"));

        assertThat(handler.receive(null, "list patterns"), is(Optional.empty()));
        assertThat("pattern registered correctly", queue.peekLast().getMessage(), containsString("foo → bar"));

        assertThat("`foo` is replaced to `bar`", handler.receive(null, "foo"), is(Optional.of("bar")));
        assertThat("`piyo` is replaced to `zzz`", handler.receive(null, "test foo piyo"),
                   is(Optional.of("test bar zzz")));

        assertThat(handler.receive(null, "delete pattern foo"), is(Optional.empty()));
        assertThat("pattern deleted correctly", queue.peekLast().getMessage(), containsString("Deleted"));

        assertThat("`foo` pattern is not applied", handler.receive(null, "test foo piyo"),
                   is(Optional.of("test foo zzz")));

        assertThat(handler.receive(null, "delete all patterns"), is(Optional.empty()));
        assertThat("patterns deleted correctly", queue.peekLast().getMessage(),
                   containsString("Deleted all patterns"));

        assertThat("nothing to be applied", handler.receive(null, "test foo piyo"),
                   is(Optional.of("test foo piyo")));

        assertThat(handler.receive(null, "list patterns"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered replace patterns"));
    }

    @Test
    public void testReceiveBrainException() throws Exception {
        assertThat("If raise BrainException, return empty not to pass next handler",
                   handler.receive(null, "replace raise with exception"), is(Optional.empty()));
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
            if (key.equals("raise") && value.equals("exception")) {
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