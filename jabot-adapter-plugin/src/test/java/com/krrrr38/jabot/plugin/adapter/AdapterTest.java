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

import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.message.Sender;

public class AdapterTest {
    private Deque<ReceiveMessage> receiver = new ArrayDeque<>();
    private Deque<SendMessage> sender = new ArrayDeque<>();

    private Adapter adapter = new Adapter() {
        private int counter = 0;

        @Override
        public void afterSetup(Map<String, String> options) {
        }

        @Override
        public void beforeDestroy() {
        }

        @Override
        public ReceiveMessage receive() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new ReceiveMessage(new Sender("id", "mid", "name", "email"), "value = " + counter++);
        }

        @Override
        public void post(SendMessage sendMessage) {
            sender.add(sendMessage);
        }

        @Override
        public void connectAction() {
            post(new SendMessage("connect"));
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