package com.krrrr38.jabot.mock;

import com.krrrr38.jabot.plugin.adapter.Adapter;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MockAdapter extends Adapter {
    public static Deque<String> queue = new ConcurrentLinkedDeque<>();

    @Override
    protected void build(Map<String, String> options) {
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
