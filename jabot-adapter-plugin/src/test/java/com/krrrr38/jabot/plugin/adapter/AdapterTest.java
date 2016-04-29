package com.krrrr38.jabot.plugin.adapter;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AdapterTest {
    private Deque<String> receiver = new ArrayDeque<>();
    private Deque<String> sender = new ArrayDeque<>();

    private Adapter adapter = new Adapter() {
        private int counter = 0;

        @Override
        public void afterSetup(Map<String, String> options) {
        }

        @Override
        public void beforeDestroy() {
        }

        @Override
        public String receive() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "value = " + counter++;
        }

        @Override
        public void post(String message) {
            sender.add(message);
        }

        @Override
        public void connectAction() {
            post("connect");
        }
    };

    @Before
    public void setUp() {
        adapter.setup("jabot-adapter", "jabot", receiver::add, new HashMap<>());
    }

    @Test
    public void testListen() throws Exception {
        assertThat(receiver.isEmpty(), is(true));
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            adapter.stop();
            assertThat("receive messages correctly", receiver.size(), is(greaterThan(1)));
        }).start();
        adapter.listen();
        // stop correctly
    }
}