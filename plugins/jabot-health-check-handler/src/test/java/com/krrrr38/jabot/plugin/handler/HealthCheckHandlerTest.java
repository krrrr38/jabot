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

public class HealthCheckHandlerTest {
    private Deque<SendMessage> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        Brain brain = new MockInmemoryBrain();
        brain.setup("health-check-handler", "jabot", Collections.emptyMap());
        handler = new MockHealthCheckHandler();
        handler.setup("health-check-handler", brain, queue::add, Collections.emptyMap());
        handler.afterRegister(Collections.emptyList());
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`foo` is not caught", handler.receive(null, "foo"), is(Optional.of("foo")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive(null, "list health-check"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered health check"));
        assertThat(handler.receive(null, "health-check"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered health check"));

        assertThat(handler.receive(null, "add health-check GET http://example.com/bar"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Register new health check"));
        assertThat(handler.receive(null, "add health-check HEAD http://example.com/foo/?paaa=bar#bbb my memo"),
                   is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Register new health check"));

        assertThat(handler.receive(null, "suspend health-check 100"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No such health check"));
        assertThat(handler.receive(null, "suspend health-check 2147483648"), is(Optional.of("suspend health-check 2147483648")));
        assertThat(handler.receive(null, "suspend health-check 1"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(),
                   containsString("Suspend health check: HEAD http://example.com/foo/?paaa=bar#bbb my memo"));
        assertThat(handler.receive(null, "suspend health-check 1"), is(Optional.empty())); // again
        assertThat(queue.peekLast().getMessage(),
                   containsString("Suspend health check: HEAD http://example.com/foo/?paaa=bar#bbb my memo"));
        assertThat(handler.receive(null, "suspend all health-check"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Suspend all health check"));

        assertThat(handler.receive(null, "resume health-check 100"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No such health check"));
        assertThat(handler.receive(null, "resume health-check 1"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(),
                   containsString("Resume health check: HEAD http://example.com/foo/?paaa=bar#bbb my memo"));
        assertThat(handler.receive(null, "resume health-check 1"), is(Optional.empty())); // again
        assertThat(queue.peekLast().getMessage(),
                   containsString("Resume health check: HEAD http://example.com/foo/?paaa=bar#bbb my memo"));
        assertThat(handler.receive(null, "resume all health-check"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Resume all health check"));

        assertThat(handler.receive(null, "delete health-check 1"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(),
                   containsString("Deleted health check: HEAD http://example.com/foo/?paaa=bar#bbb my memo"));

        assertThat(handler.receive(null, "list health-check"), is(Optional.empty()));
        String listMessage = queue.peekLast().getMessage();
        assertThat(listMessage, containsString("http://example.com/bar"));
        assertThat(listMessage, not(containsString("http://example.com/foo")));

        assertThat(handler.receive(null, "delete all health-check"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Deleted all health check"));

        assertThat("nothing to be applied", handler.receive(null, "test foo piyo"),
                   is(Optional.of("test foo piyo")));

        assertThat(handler.receive(null, "list health-check"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered health check"));
    }

    @Test
    public void testReceiveBrainException() throws Exception {
        assertThat("If raise BrainException, return empty not to pass next handler",
                   handler.receive(null, "add health-check GET http://example.com raise brain exception"),
                   is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("raise error"));
    }

    static class MockHealthCheckHandler extends HealthCheckHandler {
        @Override
        protected boolean healthCheck(HealthCheckJob job) {
            // do nothing;
            return true;
        }
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
