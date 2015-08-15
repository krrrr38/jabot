package com.krrrr38.jabot.plugin.adapter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdapterTest {
    private Deque<String> receiver = new ArrayDeque<>();
    private Deque<String> sender = new ArrayDeque<>();

    private Adapter adapter = new Adapter() {
        private int counter = 0;

        @Override
        protected void build(Map<String, String> options) {
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
        adapter.setup("jabot", receiver::add, new HashMap<>());
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
            assertThat("receive connect message", receiver.getFirst(), is("connect"));
            assertThat("receive messages correctly", receiver.size(), is(greaterThan(1)));
        }).start();
        adapter.listen();
        // stop correctly
    }
}