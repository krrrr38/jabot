package com.krrrr38.jabot.plugin.brain;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InmemoryBrainTest {
    private Brain brain;

    @Before
    public void setUp() throws Exception {
        brain = new InmemoryBrain();
        brain.setup("jabot", Collections.emptyMap());
    }

    @Test
    public void testAll() throws Exception {
        String namespace = "namespace";
        String key = "key";
        assertThat(brain.isStored(namespace, key), is(false));
        assertThat(brain.get(namespace, key), is(Optional.empty()));
        assertThat(brain.getAll(namespace).isEmpty(), is(true));

        brain.store(namespace, key, "value");
        assertThat(brain.isStored(namespace, key), is(true));
        assertThat(brain.get(namespace, key), is(Optional.of("value")));
        assertThat(brain.getAll(namespace).size(), is(1));

        brain.delete(namespace, key);
        assertThat(brain.isStored(namespace, key), is(false));
        assertThat(brain.get(namespace, key), is(Optional.empty()));
    }
}
