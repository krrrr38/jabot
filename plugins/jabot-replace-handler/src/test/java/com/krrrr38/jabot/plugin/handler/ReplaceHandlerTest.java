package com.krrrr38.jabot.plugin.handler;

import com.krrrr38.jabot.plugin.brain.Brain;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ReplaceHandlerTest {
    private Deque<String> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() {
        handler = new ReplaceHandler();
        handler.setup("replace-handler", new MockInmemoryBrain(), queue::add, null);
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`foo` is not caught", handler.receive("foo"), is(Optional.of("foo")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive("list patterns"), is(Optional.empty()));
        assertThat(queue.peekLast(), containsString("No registry replace patterns"));

        assertThat(handler.receive("replace foo with bar"), is(Optional.empty()));
        assertThat(queue.peekLast(), containsString("Registered pattern"));
        assertThat(handler.receive("replace piyo with zzz"), is(Optional.empty()));
        assertThat(queue.peekLast(), containsString("Registered pattern"));

        assertThat(handler.receive("list patterns"), is(Optional.empty()));
        assertThat("pattern registered correctly", queue.peekLast(), containsString("foo → bar"));

        assertThat("`foo` is replaced to `bar`", handler.receive("foo"), is(Optional.of("bar")));
        assertThat("`piyo` is replaced to `zzz`", handler.receive("test foo piyo"), is(Optional.of("test bar zzz")));

        assertThat(handler.receive("delete pattern foo"), is(Optional.empty()));
        assertThat("pattern deleted correctly", queue.peekLast(), containsString("Deleted"));

        assertThat("`foo` pattern is not applied", handler.receive("test foo piyo"), is(Optional.of("test foo zzz")));

        assertThat(handler.receive("delete all patterns"), is(Optional.empty()));
        assertThat("patterns deleted correctly", queue.peekLast(), containsString("Deleted all patterns"));

        assertThat("nothing to be applied", handler.receive("test foo piyo"), is(Optional.of("test foo piyo")));

        assertThat(handler.receive("list patterns"), is(Optional.empty()));
        assertThat(queue.peekLast(), containsString("No registry replace patterns"));
    }

    static class MockInmemoryBrain extends Brain {
        private Map<String, String> brain = new ConcurrentHashMap<>();

        @Override
        public Map<String, String> getAll(String namespace) {
            return brain;
        }

        @Override
        public Optional<String> get(String namespace, String key) {
            return Optional.ofNullable(brain.get(key));
        }

        @Override
        public boolean store(String namespace, String key, String value) {
            brain.put(key, value);
            return true;
        }

        @Override
        public boolean store(String namespace, String key1, String value1, String key2, String value2) {
            brain.put(key1, value1);
            brain.put(key2, value2);
            return true;
        }

        @Override
        public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) {
            brain.put(key1, value1);
            brain.put(key2, value2);
            brain.put(key3, value3);
            return true;
        }

        @Override
        public boolean storeAll(String namespace, Map<String, String> keyvalues) {
            brain.putAll(keyvalues);
            return true;
        }

        @Override
        public boolean delete(String namespace, String key) {
            return brain.remove(key) != null;
        }

        @Override
        public boolean clear(String namespace) {
            brain = new ConcurrentHashMap<>();
            return true;
        }

        @Override
        public boolean isStored(String namespace, String key) {
            return brain.containsKey(key);
        }
    }
}