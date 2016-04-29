package com.krrrr38.jabot.mock;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.krrrr38.jabot.plugin.message.ReceiveMessage;
import com.krrrr38.jabot.plugin.message.SendMessage;
import com.krrrr38.jabot.plugin.adapter.Adapter;

public class MockAdapter extends Adapter {
    public static Deque<SendMessage> queue = new ConcurrentLinkedDeque<>();

    @Override
    public void afterSetup(Map<String, String> options) {
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public ReceiveMessage receive() {
        return null;
    }

    @Override
    public void post(SendMessage sendMessage) {
        queue.add(sendMessage);
    }

    @Override
    public void connectAction() {
        queue.add(new SendMessage("connect"));
    }
}
