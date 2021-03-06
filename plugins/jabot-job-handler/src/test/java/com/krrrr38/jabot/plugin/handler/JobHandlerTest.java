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

public class JobHandlerTest {
    private Deque<SendMessage> queue = new ArrayDeque<>();
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        Brain brain = new MockInmemoryBrain();
        brain.setup("job-handler", "jabot", Collections.emptyMap());
        handler = new JobHandler();
        handler.setup("job-handler", brain, queue::add, null);
        handler.afterRegister(Collections.emptyList());
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("`foo` is not caught", handler.receive(null, "foo"), is(Optional.of("foo")));
        assertThat(queue.isEmpty(), is(true));

        assertThat(handler.receive(null, "list jobs"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered jobs"));
        assertThat(handler.receive(null, "jobs"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered jobs"));

        assertThat(handler.receive(null, "add job \"* * a * *\" invalid cron syntax"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Failed to register job"));

        assertThat(handler.receive(null, "add job \"1-5 * * * *\" test-message1"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Register new job"));
        assertThat(handler.receive(null, "add job \"*/5 * * * *\" test-message2"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("Register new job"));

        assertThat(handler.receive(null, "delete job 2"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No such job"));

        assertThat(handler.receive(null, "delete job 1"), is(Optional.empty()));
        String deletedMessage = queue.peekLast().getMessage();
        assertThat(deletedMessage, containsString("Deleted job"));
        assertThat(deletedMessage, containsString("test-message2"));

        assertThat(handler.receive(null, "list jobs"), is(Optional.empty()));
        String listMessage = queue.peekLast().getMessage();
        assertThat("job stored correctly", listMessage, containsString("test-message1"));
        assertThat("job deleted correctly", listMessage, not(containsString("test-message2")));

        assertThat(handler.receive(null, "delete all jobs"), is(Optional.empty()));
        assertThat("jobs deleted correctly", queue.peekLast().getMessage(), containsString("Deleted all jobs"));

        assertThat("nothing to be applied", handler.receive(null, "test foo piyo"),
                   is(Optional.of("test foo piyo")));

        assertThat(handler.receive(null, "list jobs"), is(Optional.empty()));
        assertThat(queue.peekLast().getMessage(), containsString("No registered jobs"));
    }

    @Test
    public void testReceiveBrainException() throws Exception {
        assertThat("If raise BrainException, return empty not to pass next handler",
                   handler.receive(null, "add job \"* * * * *\" raise brain exception"), is(Optional.empty()));
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
