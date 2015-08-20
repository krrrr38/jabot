package com.krrrr38.jabot.plugin.brain;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RedisBrainTest {
    Brain brain;

    @Before
    public void setUp() throws Exception {
        Map<String, String> options = new HashMap<>();
        options.put("host", "127.0.0.1");
        options.put("port", "6379");
        brain = new RedisBrain();
        brain.setup("redis-brain", "jabot", options);
    }

    @Test
    public void testAll() throws Exception {
        String namespace = "jabot-redis-brain-test";
        String key1 = "key1";
        String key2 = "key2";
        assertThat(brain.isStored(namespace, key1), is(false));
        assertThat(brain.get(namespace, key1), is(Optional.empty()));
        assertThat(brain.getAll(namespace).isEmpty(), is(true));

        brain.store(namespace, key1, "value1");
        brain.store(namespace, key2, "value2");
        assertThat(brain.isStored(namespace, key1), is(true));
        assertThat(brain.get(namespace, key1), is(Optional.of("value1")));
        assertThat(brain.getAll(namespace).size(), is(2));

        brain.delete(namespace, key1);
        assertThat(brain.isStored(namespace, key1), is(false));
        assertThat(brain.getAll(namespace).size(), is(1));

        assertThat(brain.getAll("jabot-redis-brain-other-namespace").isEmpty(), is(true));

        brain.clear(namespace);
        assertThat(brain.getAll(namespace).isEmpty(), is(true));
    }
}