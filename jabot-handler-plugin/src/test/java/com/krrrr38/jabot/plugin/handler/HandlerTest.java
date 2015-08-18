package com.krrrr38.jabot.plugin.handler;

import com.krrrr38.jabot.plugin.brain.Brain;
import com.krrrr38.jabot.plugin.brain.EmptyBrain;
import com.krrrr38.jabot.plugin.brain.JabotBrainException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HandlerTest {
    private static final String MOCK_BRAIN_NAMESPACE = "namespace";
    private Deque<String> queue = new ArrayDeque<>();
    private Map<String, String> mockBrain = new HashMap();
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        List<Rule> rules = Arrays.asList(
                new Rule(
                        Pattern.compile("sample"),
                        "sample",
                        "sample description",
                        "sample usage",
                        false,
                        groups -> {
                            return Optional.of("converted");
                        }
                )
        );
        handler = new Handler() {
            @Override
            List<Rule> build(Map<String, String> options) {
                return rules;
            }

            @Override
            public void afterRegister(List<Handler> handlers) {
            }
        };
        Brain brain = new MockMemoryBrain();
        brain.setup("jabot", Collections.emptyMap());
        handler.setup(MOCK_BRAIN_NAMESPACE, brain, queue::add, new HashMap<>());

        // fixture;
        mockBrain.put("foo", "bar");
    }

    @Test
    public void testSetup() throws Exception {
        // never success in this section, not to change setUp handler data.
        try {
            handler.setup(null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("namespace required"));
        }
        try {
            handler.setup("", null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("namespace required"));
        }
        try {
            handler.setup("a", null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("brain required"));
        }
        try {
            handler.setup("a", new EmptyBrain(), null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("sender required"));
        }
    }

    @Test
    public void testReceive() throws Exception {
        assertThat("no catch", handler.receive("foo"), is(Optional.of("foo")));
        assertThat("catch success", handler.receive("sample"), is(Optional.of("converted")));
    }

    @Test
    public void testGetRules() throws Exception {
        assertThat("get sample ruel handler", handler.getRules().size(), is(1));
    }

    @Test
    public void testSend() throws Exception {
        assertThat(queue.isEmpty(), is(true));
        handler.send("message");
        assertThat(queue.size(), is(1));
        assertThat(queue.peekLast(), containsString("message"));
    }

    ///////////////////////////////////////////////////////////////////
    // brain

    @Test
    public void testStore() throws Exception {
        String key = "store-key";
        assertThat("confirm not exist", handler.get(key), is(Optional.empty()));
        handler.store(key, "insert");
        assertThat("confirm exit", handler.get(key), is(Optional.of("insert")));
    }

    @Test
    public void testGetAll() throws Exception {
        handler.store("get-all", "bar");
        assertThat(handler.getAll().size(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGet() throws Exception {
        handler.store("get", "bar");
        assertThat(handler.get("get"), is(Optional.of("bar")));
    }

    @Test
    public void testDelete() throws Exception {
        String key = "delete-key";
        handler.store(key, "piyo");
        assertThat("confirm existence", handler.get(key), is(Optional.of("piyo")));

        handler.delete(key);
        assertThat("confirm deleted", handler.get(key), is(Optional.empty()));
    }

    @Test
    public void testIsStored() throws Exception {
        handler.store("is-stored", "true");
        assertThat(handler.isStored("is-stored"), is(true));
        assertThat(handler.isStored("is-not-stored"), is(false));
    }

    class MockMemoryBrain extends Brain {
        Map<String, String> brain;

        @Override
        protected void build(Map<String, String> options) throws JabotBrainException {
            brain = new ConcurrentHashMap<>();
        }

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