package com.krrrr38.jabot.mock;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.krrrr38.jabot.plugin.adapter.Adapter;

public class MockAdapter extends Adapter {
    public static Deque<String> queue = new ConcurrentLinkedDeque<>();

    @Override
    public void afterSetup(Map<String, String> options) {
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public String receive() {
        return null;
    }

    @Override
    public void post(String message) {
        queue.add(message);
    }

    @Override
    public void connectAction() {
        queue.add("connect");
    }
}
